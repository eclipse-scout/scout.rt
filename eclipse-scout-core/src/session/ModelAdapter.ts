/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AdapterData, App, arrays, ChildModelOf, comparators, defaultValues, Event, EventEmitter, EventListener, FullModelOf, InitModelOf, ModelAdapterEventMap, ModelAdapterModel, ObjectModel, objects, Predicate, PropertyChangeEvent,
  PropertyChangeEventFilter, RemoteEvent, scout, Session, SomeRequired, strings, Widget, WidgetEventTypeFilter
} from '../index';
import $ from 'jquery';

/**
 * A model adapter is the connector with the server, it takes the events sent from the server and calls the corresponding methods on the widget.
 * It also sends events to the server whenever an action happens on the widget.
 */
export class ModelAdapter extends EventEmitter implements ModelAdapterModel, ModelAdapterLike {
  declare model: ModelAdapterModel;
  declare initModel: SomeRequired<this['model'], 'session' | 'id'>;
  declare eventMap: ModelAdapterEventMap;
  declare self: ModelAdapter;
  id: string;
  objectType: string;
  initialized: boolean;
  attached: boolean;
  destroyed: boolean;
  widget: Widget;
  session: Session;

  protected _enabledBeforeOffline: boolean;
  /**
   * Widget properties which should be sent to server on property change.
   */
  protected _remoteProperties: string[];
  /**
   * Properties that need to be synced in a specific order.
   */
  protected _orderedProperties: string[];
  protected _widgetListener: EventListener;
  protected _propertyChangeEventFilter: PropertyChangeEventFilter;
  protected _widgetEventTypeFilter: WidgetEventTypeFilter;

  constructor() {
    super();

    this.id = null;
    this.objectType = null;
    this.initialized = false;
    this.attached = false;
    this.destroyed = false;
    this.widget = null;
    this._enabledBeforeOffline = true;
    this._remoteProperties = [];
    this._orderedProperties = [];
    this._widgetListener = null;
    this._propertyChangeEventFilter = new PropertyChangeEventFilter();
    this._widgetEventTypeFilter = new WidgetEventTypeFilter();
    this.session = null;
  }

  init(model: InitModelOf<this>) {
    this._init(model);
    this.initialized = true;
  }

  protected _init(model: InitModelOf<this>) {
    scout.assertParameter('id', model.id);
    scout.assertParameter('session', model.session);
    $.extend(this, model);
    this.session.registerModelAdapter(this);
  }

  destroy() {
    this._detachWidget();
    this.widget.destroy();
    this.widget = null;
    this.session.unregisterModelAdapter(this);
    this.destroyed = true;
  }

  createWidget<T extends Widget>(adapterData: ChildModelOf<Widget>, parent: Widget): T {
    let model = this._initModel(adapterData, parent);
    this.widget = this._createWidget(model);
    this._attachWidget();
    this._postCreateWidget();
    return this.widget as T;
  }

  /**
   * Override this method to do something right after the widget has been created and has been
   * attached to the remote adapter. The default impl. does nothing.
   */
  /** @internal */
  _postCreateWidget() {
    // NOP
  }

  protected _initModel(m: ChildModelOf<Widget>, parent: Widget): FullModelOf<Widget> {
    // Make a copy to prevent a modification of the given model
    let deepCopy = this.session.adapterExportEnabled as true;
    let model: any = $.extend(deepCopy, {}, m) as FullModelOf<Widget>;

    // Fill in the missing default values
    defaultValues.applyTo(model);

    model.parent = parent;
    model.owner = parent; // Set it explicitly because server sends owner in inspector mode -> ignore the owner sent by server.
    model.modelAdapter = this;

    if (model.global) {
      // Use the root adapter as owner if global is set to true
      model.owner = this.session.getModelAdapter('1').widget;
    }

    this._initProperties(model);

    return model;
  }

  /**
   * Override this method to call _sync* methods of the ModelAdapter _before_ the widget is created.
   */
  protected _initProperties(model: ObjectModel) {
    // NOP
  }

  /**
   * @returns A new widget instance. The default impl. uses calls scout.create() with property objectType from given model.
   */
  protected _createWidget(model: FullModelOf<Widget>): Widget {
    let widget = scout.create(model);
    widget._addCloneProperties(['modelClass', 'classId']);
    return widget;
  }

  /** @internal */
  _attachWidget() {
    if (this._widgetListener) {
      return;
    }
    this._widgetListener = {
      func: this._onWidgetEventInternal.bind(this)
    };
    this.widget.addListener(this._widgetListener);
    this.attached = true;
    this.trigger('attach');
  }

  protected _detachWidget() {
    if (!this._widgetListener) {
      return;
    }
    this.widget.removeListener(this._widgetListener);
    this._widgetListener = null;
    this.attached = false;
    this.trigger('detach');
  }

  goOffline() {
    this.widget.visitChildren(child => {
      if (child.modelAdapter) {
        child.modelAdapter._goOffline();
      }
    });
  }

  protected _goOffline() {
    // NOP may be implemented by subclasses
  }

  goOnline() {
    this.widget.visitChildren(child => {
      if (child.modelAdapter) {
        child.modelAdapter._goOnline();
      }
    });
  }

  protected _goOnline() {
    // NOP may be implemented by subclasses
  }

  isRemoteProperty(propertyName: string): boolean {
    return this._remoteProperties.indexOf(propertyName) > -1;
  }

  protected _addRemoteProperties(properties: string[] | string) {
    this._addProperties('_remoteProperties', properties);
  }

  protected _removeRemoteProperties(properties: string[] | string) {
    this._removeProperties('_remoteProperties', properties);
  }

  protected _addOrderedProperties(properties: string[] | string) {
    this._addProperties('_orderedProperties', properties);
  }

  protected _addProperties(propertyName: string, properties: string[] | string) {
    if (Array.isArray(properties)) {
      this[propertyName] = this[propertyName].concat(properties);
    } else {
      this[propertyName].push(properties);
    }
  }

  protected _removeProperties(propertyName: string, properties: string[] | string) {
    properties = arrays.ensure(properties);
    arrays.removeAll(this[propertyName], properties);
  }

  /**
   * Creates an Event object from the current adapter instance and sends the event by using the Session#sendEvent() method.
   * Local objects may set a different remoteHandler to call custom code instead of the Session#sendEvent() method.
   *
   * @param type of event
   * @param data of event
   */
  protected _send<Data extends Record<PropertyKey, any>>(type: string, data?: Data, options?: ModelAdapterSendOptions<Data>) {
    // Legacy fallback with all options as arguments
    let opts = {} as ModelAdapterSendOptions<Data>;
    if (arguments.length > 2) {
      if (options !== null && typeof options === 'object') {
        opts = options;
      } else {
        // eslint-disable-next-line prefer-rest-params
        opts.delay = arguments[2];
        // eslint-disable-next-line prefer-rest-params
        opts.coalesce = arguments[3];
        // eslint-disable-next-line prefer-rest-params
        opts.showBusyIndicator = arguments[4];
      }
    }
    options = opts;
    // (End legacy fallback)

    let event = new RemoteEvent(this.id, type, data);
    // The following properties will not be sent to the server, see Session._requestToJson().
    if (options.coalesce !== undefined) {
      event.coalesce = options.coalesce;
    }
    if (options.showBusyIndicator !== undefined) {
      event.showBusyIndicator = options.showBusyIndicator;
    }
    if (options.newRequest !== undefined) {
      event.newRequest = options.newRequest;
    }
    this.session.sendEvent(event, options.delay);
  }

  /**
   * Sends the given value as property event to the server.
   */
  protected _sendProperty(propertyName: string, value: any) {
    let data = {};
    data[propertyName] = value;
    this._send('property', data);
  }

  /**
   * Adds a custom filter for events.
   */
  addFilterForWidgetEvent(filter: Predicate<Event>) {
    this._widgetEventTypeFilter.addFilter(filter);
  }

  /**
   * Adds a filter which only checks the type of the event.
   */
  addFilterForWidgetEventType(eventType: string) {
    this._widgetEventTypeFilter.addFilterForEventType(eventType);
  }

  /**
   * Adds a filter which checks the name and value of every property in the given properties.
   */
  addFilterForProperties(properties: Record<string, any>) {
    this._propertyChangeEventFilter.addFilterForProperties(properties);
  }

  /**
   * Adds a filter which only checks the property name and ignores the value.
   */
  addFilterForPropertyName(propertyName: string) {
    this._propertyChangeEventFilter.addFilterForPropertyName(propertyName);
  }

  protected _isPropertyChangeEventFiltered(propertyName: string, value: any): boolean {
    if (value instanceof Widget) {
      // In case of a remote widget property use the id, otherwise it would always return false
      value = value.id;
    }
    return this._propertyChangeEventFilter.filter(propertyName, value);
  }

  protected _isWidgetEventFiltered(event: Event<Widget>): boolean {
    return this._widgetEventTypeFilter.filter(event);
  }

  resetEventFilters() {
    this._propertyChangeEventFilter.reset();
    this._widgetEventTypeFilter.reset();
  }

  protected _onWidgetPropertyChange(event: PropertyChangeEvent<any, Widget>) {
    let propertyName = event.propertyName;
    let value = event.newValue;

    // TODO [7.0] cgu This does not work if value will be converted into another object (e.g DateRange.ensure(selectionRange) in Planner.js)
    // -> either do the check in this._send() or extract ensure into separate method and move the call of addFilterForProperties.
    // The advantage of the first one would be simpler filter functions (e.g. this.widget.nodesToIds(this.widget.selectedNodes) in Tree.js)
    if (this._isPropertyChangeEventFiltered(propertyName, value)) {
      return;
    }

    if (this.isRemoteProperty(propertyName)) {
      value = this._prepareRemoteProperty(propertyName, value);
      this._callSendProperty(propertyName, value);
    }
  }

  protected _prepareRemoteProperty(propertyName: string, value: any): any {
    if (!value || !this.widget.isWidgetProperty(propertyName)) {
      return value;
    }

    if (!Array.isArray(value)) {
      return value.modelAdapter.id;
    }

    return value.map(widget => {
      return widget.modelAdapter.id;
    });
  }

  protected _callSendProperty(propertyName: string, value: any) {
    let sendFuncName = '_send' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[sendFuncName]) {
      this[sendFuncName](value);
    } else {
      this._sendProperty(propertyName, value);
    }
  }

  protected _onWidgetDestroy(event: Event<Widget>) {
    this.destroy();
  }

  /**
   * Do not override this method. Widget event filtering is done here, before _onWidgetEvent is called.
   */
  protected _onWidgetEventInternal(event: Event<Widget>) {
    if (!this._isWidgetEventFiltered(event)) {
      this._onWidgetEvent(event);
    }
  }

  protected _onWidgetEvent(event: Event<Widget>) {
    if (event.type === 'destroy') {
      this._onWidgetDestroy(event);
    } else if (event.type === 'propertyChange') {
      this._onWidgetPropertyChange(event as PropertyChangeEvent<any, Widget>);
    }
  }

  protected _syncPropertiesOnPropertyChange(newProperties: Record<string, any>) {
    let orderedPropertyNames = this._orderPropertyNamesOnSync(newProperties);
    orderedPropertyNames.forEach(propertyName => {
      let value = newProperties[propertyName];
      let syncFuncName = '_sync' + strings.toUpperCaseFirstLetter(propertyName);
      if (this[syncFuncName]) {
        this[syncFuncName](value);
      } else {
        this._writeProperty(propertyName, value);
      }
    });
  }

  protected _writeProperty(propertyName: string, value: any) {
    this.widget.callSetter(propertyName, value);
  }

  /**
   * Orders the properties based on {@link _orderedProperties}.
   *
   * @returns the ordered property names.
   */
  protected _orderPropertyNamesOnSync(newProperties: Record<string, any>): string[] {
    let propertyNames = Object.keys(newProperties);
    if (this._orderedProperties.length > 0) {
      propertyNames = propertyNames.sort(this._createPropertySortFunc(this._orderedProperties));
    }
    return propertyNames;
  }

  protected _createPropertySortFunc(order: string[]): (a: string, b: string) => number {
    return (a, b) => {
      let ia = order.indexOf(a);
      let ib = order.indexOf(b);
      if (ia > -1 && ib > -1) { // both are in the list
        return ia - ib;
      }
      if (ia > -1) { // B is not in list
        return -1;
      }
      if (ib > -1) { // A is not in list
        return 1;
      }
      return comparators.TEXT.compare(a, b); // both are not in list
    };
  }

  /**
   * Called by {@link Session} for every event from the model
   */
  onModelEvent(event: RemoteEvent) {
    if (!event) {
      return;
    }
    if (event.type === 'property') { // Special handling for 'property' type
      this.onModelPropertyChange(event);
    } else {
      this.onModelAction(event);
    }
  }

  /**
   * Processes the JSON event from the server and calls the corresponding setter of the widget for each property.
   */
  onModelPropertyChange(event: RemoteEvent) {
    this.addFilterForProperties(event.properties);
    this._syncPropertiesOnPropertyChange(event.properties);
  }

  onModelAction(event: RemoteEvent) {
    if (event.type === 'scrollToTop') {
      this.widget.scrollToTop({animate: event.animate});
    } else if (event.type === 'reveal') {
      this.widget.reveal({animate: event.animate});
    } else {
      $.log.warn('Model action "' + event.type + '" is not supported by model-adapter ' + this.objectType);
    }
  }

  override toString(): string {
    return 'ModelAdapter[objectType=' + this.objectType + ' id=' + this.id + ']';
  }

  /**
   * This method is used to modify adapterData before the data is exported (as used for JSON export).
   */
  exportAdapterData(adapterData: AdapterData): AdapterData {
    // use last part of class-name as ID (because that's better than having only a number for ID)
    let modelClass = adapterData.modelClass;
    if (modelClass) {
      let pos = Math.max(0,
        modelClass.lastIndexOf('$') + 1,
        modelClass.lastIndexOf('.') + 1);
      adapterData.id = modelClass.substring(pos);
    }
    delete adapterData.owner;
    delete adapterData.classId;
    delete adapterData.modelClass;
    return adapterData;
  }

  /**
   * Static method to modify the prototype of Widget.
   */
  static modifyWidgetPrototype(event: Event) {
    if (!App.get().remote) {
      return;
    }

    // _createChild
    objects.replacePrototypeFunction(Widget, '_createChild', function(this: Widget & { _createChildOrig }, model) {
      if (model instanceof Widget) {
        return model;
      }

      // Remote case
      // If the widget has a model adapter use getOrCreateWidget of the session to resolve the child widget
      // The model normally is a String containing the (remote) object ID.
      // If it is not a string it may be a local model -> use default local case instead
      if (this.modelAdapter && typeof model === 'string') {
        return this.session.getOrCreateWidget(model, this);
      }

      // Local case (default)
      return this._createChildOrig(model);
    }, true); // <-- true = keep original function

    // resolveTextKeys
    objects.replacePrototypeFunction(Widget, 'resolveTextKeys', function(this: Widget & { resolveTextKeysOrig }, properties: string[]) {
      if (this.modelAdapter) {
        // Never resolve '${textKey:...}' references in texts from the server
        return;
      }
      return this.resolveTextKeysOrig(properties);
    }, true);
  }
}

App.addListener('bootstrap', ModelAdapter.modifyWidgetPrototype);

export interface ModelAdapterLike {
  widget: Widget;

  onModelEvent(event: RemoteEvent): void;

  resetEventFilters(): void;

  destroy(): void;

  exportAdapterData(adapterData: AdapterData): AdapterData;
}

export interface ModelAdapterSendOptions<Data> {

  /**
   * Delay in milliseconds before the event is sent. Default is 0.
   */
  delay?: number;

  /**
   * Coalesce function added to event-object. Default: none.
   */
  coalesce?(this: RemoteEvent & Data, event: RemoteEvent & Data): boolean;

  /**
   * Whether sending the event should block the UI after a certain delay.
   * The default value 'undefined' means that the default value ('true') is determined in the {@link Session}.
   * We don't write it explicitly to the event here because that would break many Jasmine tests.
   */
  showBusyIndicator?: boolean;

  /**
   * Default is false.
   */
  newRequest?: boolean;
}
