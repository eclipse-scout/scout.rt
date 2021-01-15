/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {App, arrays, comparators, defaultValues, EventSupport, objects, PropertyChangeEventFilter, RemoteEvent, scout, strings, Widget, WidgetEventTypeFilter} from '../index';
import $ from 'jquery';

/**
 * A model adapter is the connector with the server, it takes the events sent from the server and calls the corresponding methods on the widget.
 * It also sends events to the server whenever an action happens on the widget.
 */
export default class ModelAdapter {
  constructor() {
    this.id = null;
    this.objectType = null;
    this.initialized = false;
    this.attached = false;
    this.destroyed = false;
    this.widget = null;
    this._enabledBeforeOffline = true;

    /**
     * Widget properties which should be sent to server on property change.
     */
    this._remoteProperties = [];
    this._widgetListener = null;

    this._propertyChangeEventFilter = new PropertyChangeEventFilter();
    this._widgetEventTypeFilter = new WidgetEventTypeFilter();
    this.events = new EventSupport();
    this.session = null;
  }

  init(model) {
    this._init(model);
    this.initialized = true;
  }

  /**
   * @param model expects a plain-object with properties: id, session
   */
  _init(model) {
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

  createWidget(adapterData, parent) {
    let model = this._initModel(adapterData, parent);
    this.widget = this._createWidget(model);
    this._attachWidget();
    this._postCreateWidget();
    return this.widget;
  }

  /**
   * Override this method to do something right after the widget has been created and has been
   * attached to the remote adapter. The default impl. does nothing.
   */
  _postCreateWidget() {
    // NOP
  }

  _initModel(model, parent) {
    // Make a copy to prevent a modification of the given model
    let deepCopy = this.session.adapterExportEnabled;
    model = $.extend(deepCopy, {}, model);

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
  _initProperties(model) {
    // NOP
  }

  /**
   * @returns A new widget instance. The default impl. uses calls scout.create() with property objectType from given model.
   */
  _createWidget(model) {
    let widget = scout.create(model);
    widget._addCloneProperties(['modelClass', 'classId']);
    return widget;
  }

  _attachWidget() {
    if (this._widgetListener) {
      return;
    }
    this._widgetListener = {
      func: this._onWidgetEventInternal.bind(this)
    };
    this.widget.addListener(this._widgetListener);
    this.attached = true;
    this.events.trigger('attach');
  }

  _detachWidget() {
    if (!this._widgetListener) {
      return;
    }
    this.widget.removeListener(this._widgetListener);
    this._widgetListener = null;
    this.attached = false;
    this.events.trigger('detach');
  }

  goOffline() {
    this.widget.visitChildren(child => {
      if (child.modelAdapter) {
        child.modelAdapter._goOffline();
      }
    });
  }

  _goOffline() {
    // NOP may be implemented by subclasses
  }

  goOnline() {
    this.widget.visitChildren(child => {
      if (child.modelAdapter) {
        child.modelAdapter._goOnline();
      }
    });
  }

  _goOnline() {
    // NOP may be implemented by subclasses
  }

  isRemoteProperty(propertyName) {
    return this._remoteProperties.indexOf(propertyName) > -1;
  }

  _addRemoteProperties(properties) {
    this._addProperties('_remoteProperties', properties);
  }

  _removeRemoteProperties(properties) {
    this._removeProperties('_remoteProperties', properties);
  }

  _addProperties(propertyName, properties) {
    if (Array.isArray(properties)) {
      this[propertyName] = this[propertyName].concat(properties);
    } else {
      this[propertyName].push(properties);
    }
  }

  _removeProperties(propertyName, properties) {
    properties = arrays.ensure(properties);
    arrays.removeAll(this[propertyName], properties);
  }

  /**
   * @returns Creates a Event object from the current adapter instance and
   *   sends the event by using the Session#sendEvent() method. Local objects may
   *   set a different remoteHandler to call custom code instead of the Session#sendEvent()
   *   method.
   *
   * @param type of event
   * @param data of event
   * @param options (optional) options according to the following table:
   *
   * Option name         Default value   Description
   * -----------------------------------------------------------------------------------------
   * delay               0               Delay in milliseconds before the event is sent.
   *
   * coalesce            undefined       Coalesce function added to event-object.
   *
   * showBusyIndicator   undefined       Whether sending the event should block the UI
   *                     (true*)         after a certain delay.
   *                                     * The default value 'undefined' means that the
   *                                       default value ('true') is determined in Session.js.
   *                                       We don't write it explicitly to the event here
   *                                       because that would break many Jasmine tests.
   */
  _send(type, data, options) {
    // Legacy fallback with all options as arguments
    let opts = {};
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
  _sendProperty(propertyName, value) {
    let data = {};
    data[propertyName] = value;
    this._send('property', data);
  }

  /**
   * Adds a custom filter for events.
   */
  addFilterForWidgetEvent(filter) {
    this._widgetEventTypeFilter.addFilter(filter);
  }

  /**
   * Adds a filter which only checks the type of the event.
   */
  addFilterForWidgetEventType(eventType) {
    this._widgetEventTypeFilter.addFilterForEventType(eventType);
  }

  /**
   * Adds a filter which checks the name and value of every property in the given properties array.
   */
  addFilterForProperties(properties) {
    this._propertyChangeEventFilter.addFilterForProperties(properties);
  }

  /**
   * Adds a filter which only checks the property name and ignores the value.
   */
  addFilterForPropertyName(propertyName) {
    this._propertyChangeEventFilter.addFilterForPropertyName(propertyName);
  }

  _isPropertyChangeEventFiltered(propertyName, value) {
    if (value instanceof Widget) {
      // In case of a remote widget property use the id, otherwise it would always return false
      value = value.id;
    }
    return this._propertyChangeEventFilter.filter(propertyName, value);
  }

  _isWidgetEventFiltered(event) {
    return this._widgetEventTypeFilter.filter(event);
  }

  resetEventFilters() {
    this._propertyChangeEventFilter.reset();
    this._widgetEventTypeFilter.reset();
  }

  _onWidgetPropertyChange(event) {
    let propertyName = event.propertyName;
    let value = event.newValue;

    // TODO [7.0] cgu This does not work if value will be converted into another object (e.g DateRange.ensure(selectionRange) in Planner.js)
    // -> either do the check in this._send() or extract ensure into separate method and move the call of addFilterForProperties.
    // The advantage of the first one would be simpler filter functions (e.g. this.widget._nodesToIds(this.widget.selectedNodes) in Tree.js)
    if (this._isPropertyChangeEventFiltered(propertyName, value)) {
      return;
    }

    if (this.isRemoteProperty(propertyName)) {
      value = this._prepareRemoteProperty(propertyName, value);
      this._callSendProperty(propertyName, value);
    }
  }

  _prepareRemoteProperty(propertyName, value) {
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

  _callSendProperty(propertyName, value) {
    let sendFuncName = '_send' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[sendFuncName]) {
      this[sendFuncName](value);
    } else {
      this._sendProperty(propertyName, value);
    }
  }

  _onWidgetDestroy() {
    this.destroy();
  }

  /**
   * Do not override this method. Widget event filtering is done here, before _onWidgetEvent is called.
   */
  _onWidgetEventInternal(event) {
    if (!this._isWidgetEventFiltered(event)) {
      this._onWidgetEvent(event);
    }
  }

  _onWidgetEvent(event) {
    if (event.type === 'destroy') {
      this._onWidgetDestroy(event);
    } else if (event.type === 'propertyChange') {
      this._onWidgetPropertyChange(event);
    }
  }

  _syncPropertiesOnPropertyChange(newProperties) {
    let orderedPropertyNames = this._orderPropertyNamesOnSync(newProperties);
    orderedPropertyNames.forEach(function(propertyName) {
      let value = newProperties[propertyName];
      let syncFuncName = '_sync' + strings.toUpperCaseFirstLetter(propertyName);
      if (this[syncFuncName]) {
        this[syncFuncName](value);
      } else {
        this.widget.callSetter(propertyName, value);
      }
    }, this);
  }

  /**
   * May be overridden to return a custom order of how the properties will be set.
   */
  _orderPropertyNamesOnSync(newProperties) {
    return Object.keys(newProperties);
  }

  _createPropertySortFunc(order) {
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
   * Called by Session.js for every event from the model
   */
  onModelEvent(event) {
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
  onModelPropertyChange(event) {
    this.addFilterForProperties(event.properties);
    this._syncPropertiesOnPropertyChange(event.properties);
  }

  /**
   * The default impl. only logs a warning that the event is not supported.
   */
  onModelAction(event) {
    if (event.type === 'scrollToTop') {
      this.widget.scrollToTop({animate: event.animate});
    } else if (event.type === 'reveal') {
      this.widget.reveal({animate: event.animate});
    } else {
      $.log.warn('Model action "' + event.type + '" is not supported by model-adapter ' + this.objectType);
    }
  }

  toString() {
    return 'ModelAdapter[objectType=' + this.objectType + ' id=' + this.id + ']';
  }

  /**
   * This method is used to modify adapterData before the data is exported (as used for JSON export).
   */
  exportAdapterData(adapterData) {
    // use last part of class-name as ID (because that's better than having only a number as ID)
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
  static modifyWidgetPrototype() {
    if (!App.get().remote) {
      return;
    }

    // _createChild
    objects.replacePrototypeFunction(Widget, '_createChild', function(model) {
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
  }
}

App.addListener('bootstrap', ModelAdapter.modifyWidgetPrototype);
