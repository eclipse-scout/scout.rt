/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AnyDoEntity, App, arrays, DisposeWidgetsHybridActionDo, Event, EventHandler, EventListener, EventMapOf, Extension, Form, HybridActionContextElements, HybridActionEvent, HybridManagerEventMap, HybridManagerWidgetAddEvent,
  HybridManagerWidgetRemoveEvent, InitModelOf, ObjectOrChildModel, objects, scout, Session, strings, UuidPool, Widget
} from '../../index';

/**
 * A utility to invoke remote Java actions to simplify the interaction of Scout JS and Scout Classic code
 * to facilitate the creation of hybrid applications.
 */
export class HybridManager extends Widget {
  declare eventMap: HybridManagerEventMap;
  declare self: HybridManager;

  widgets: Record<string, Widget>;

  /**
   * Set of {@link HybridManagerWidget}s that will be disposed in the next batch.
   * See {@link #_callDisposeWidgets} for more information.
   */
  protected _widgetsToBeDisposed: Set<HybridManagerWidget> = null;
  /**
   * Set of ids that were scheduled in former batches.
   * See {@link #_callDisposeWidgets} for more information.
   */
  protected _widgetIdsBeingDisposed = new Set<string>();

  constructor() {
    super();

    this.widgets = {};
  }

  // static helpers

  static get(session?: Session, wait?: false): HybridManager;
  static get(session?: Session, wait?: true): JQuery.Promise<HybridManager>;
  static get(session?: Session, wait?: boolean): HybridManager | JQuery.Promise<HybridManager> {
    session = session || App.get().sessions[0];
    scout.assertParameter('session', session);

    const findHybridManager = () => session.desktop.addOns.find(addOn => addOn instanceof HybridManager) as HybridManager;

    if (!wait) {
      return findHybridManager();
    }

    if (session.desktop.initialized) {
      return $.resolvedPromise(findHybridManager());
    }

    const deferred = $.Deferred();
    session.desktop.one('init', e => deferred.resolve(findHybridManager()));
    return deferred.promise();
  }

  // init

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setWidgets(this.widgets);
  }

  // widgets

  protected _setWidgets(widgets: Record<string, ObjectOrChildModel<Widget>>) {
    const presentWidgetIds = new Set<string>();
    for (const [id, widgetOrModel] of Object.entries(widgets)) {
      let widgetId: string;
      if (typeof widgetOrModel === 'string') {
        widgetId = widgetOrModel;
      } else if (objects.isObject(widgetOrModel)) {
        widgetId = widgetOrModel.id;
      }

      if (!widgetId?.length) {
        continue;
      }

      // collect widget id
      presentWidgetIds.add(widgetId);

      if (this._widgetIdsBeingDisposed.has(widgetId)) {
        // do not accept disposed widgets
        delete widgets[id];
      }
    }

    for (const id of this._widgetIdsBeingDisposed) {
      if (!presentWidgetIds.has(id)) {
        // widget is no longer present -> no need to remember id any longer
        this._widgetIdsBeingDisposed.delete(id);
      }
    }

    widgets = this._ensureWidgets(widgets);

    const removedWidgets: Record<string, HybridManagerWidget> = {};
    for (const [id, widget] of Object.entries(this.widgets)) {
      if (!widgets[id] || widgets[id] !== widget) {
        removedWidgets[id] = widget;
        this._uninstallHybridManagerWidget(widget);
      }
    }
    const addedWidgets: Record<string, HybridManagerWidget> = {};
    for (const [id, widget] of Object.entries(widgets as Record<string, Widget>)) {
      if (!this.widgets[id] || this.widgets[id] !== widget) {
        addedWidgets[id] = widget;
        this._installHybridManagerWidget(widget, id);
      }
    }
    this._destroyOrUnlinkChildren(Object.values(removedWidgets));

    this._setProperty('widgets', widgets);

    Object.entries(addedWidgets).forEach(([id, widget]) => {
      this._triggerWidgetAdd(id, widget);
    });
    Object.entries(removedWidgets).forEach(([id, widget]) => {
      this._triggerWidgetRemove(id, widget);
    });
  }

  protected _ensureWidgets(modelsOrWidgets: Record<string, ObjectOrChildModel<Widget>>): Record<string, Widget> {
    const result: Record<string, Widget> = {};
    Object.keys(modelsOrWidgets).forEach(id => {
      // Create new child widget(s)
      result[id] = this._createChildren(modelsOrWidgets[id]);
    });
    return result;
  }

  protected _installHybridManagerWidget(widget: HybridManagerWidget, remoteId: string) {
    scout.assertParameter('widget', widget);
    scout.assertParameter('remoteId', strings.nullIfEmpty(remoteId));

    // mark widget with remote id
    widget.__remoteId = remoteId;
  }

  protected _uninstallHybridManagerWidget(widget: HybridManagerWidget) {
    // clear remote id marker
    delete widget?.__remoteId;
  }

  protected _triggerWidgetAdd(id: string, widget: Widget) {
    this.trigger(`widgetAdd:${id}`, {widget} as HybridManagerWidgetAddEvent);
  }

  protected _triggerWidgetRemove(id: string, widget: Widget) {
    this.trigger(`widgetRemove:${id}`, {widget} as HybridManagerWidgetRemoveEvent);
  }

  // hybrid events (java to js)

  /** @internal */
  onHybridEvent(id: string, eventType: string, data: AnyDoEntity, contextElements: HybridActionContextElements) {
    this.trigger(`${eventType}:${id}`, {data, contextElements});
  }

  /** @internal */
  onHybridWidgetEvent(id: string, eventType: string, data: AnyDoEntity) {
    const widget = this.widgets[id];
    if (!widget) {
      return;
    }
    if (widget instanceof Form) {
      this._onHybridFormEvent(widget, eventType, data);
    } else {
      this._onHybridWidgetEvent(widget, eventType, data);
    }
  }

  protected _onHybridWidgetEvent(widget: Widget, eventType: string, data: AnyDoEntity) {
    widget.trigger(eventType, {data});
  }

  protected _onHybridFormEvent(form: HybridManagerForm, eventType: string, data: AnyDoEntity) {
    if (eventType === 'data') {
      form.setData(data);
    } else if (eventType === 'reset') {
      form.setData(data);
      form.trigger('reset');
    } else if (eventType === 'save') {
      form.setData(data);
      form.trigger('save');
    } else if (eventType === 'close') {
      if (!form.__closeTriggered) { // form.close() may be called by JS code, don't trigger close again
        form.trigger('close');
      }
    } else {
      this._onHybridWidgetEvent(form, eventType, data);
    }
  }

  // hybrid actions (js to java)

  protected _createEventId(): string {
    return UuidPool.take(this.session);
  }

  /**
   * Calls the hybrid action that matches the given action type.
   *
   * @returns the id of the triggered hybrid action
   * @see IHybridAction.java
   */
  callAction(actionType: string, data?: AnyDoEntity, contextElements?: HybridActionContextElements): string {
    const id = this._createEventId();
    this.trigger('hybridAction', {data: {id, actionType, contextElements, data}} as HybridActionEvent);
    return id;
  }

  /**
   * Calls the hybrid action that matches the given action type and returns a promise that will be resolved once the corresponding hybridActionEnd event arrives.
   * The resolved value consist of the `data` value sent back from the server. To access the `contextElements`, use {@link callActionAndWaitWithContext} instead.
   *
   * @returns a promise that will be resolved with the result `data` once the corresponding hybridActionEnd event arrives.
   * @see IHybridAction
   * @see AbstractHybridAction.fireHybridActionEndEvent
   */
  callActionAndWait(actionType: string, data?: AnyDoEntity, contextElements?: HybridActionContextElements): JQuery.Promise<AnyDoEntity> {
    return this.callActionAndWaitWithContext(actionType, data, contextElements)
      .then(result => result.data);
  }

  /**
   * Calls the hybrid action that matches the given action type and returns a promise that will be resolved once the corresponding hybridActionEnd event arrives.
   * The resolved value is an object with the `data` and `contextElements` values sent back from the user. {@link callActionAndWait} can be used instead if only
   * the content of the `data` attribute is relevant.
   *
   * @returns a promise that will be resolved with the entire result object once the corresponding hybridActionEnd event arrives.
   * @see IHybridAction
   * @see AbstractHybridAction.fireHybridActionEndEvent
   */
  callActionAndWaitWithContext(actionType: string, data?: AnyDoEntity, contextElements?: HybridActionContextElements): JQuery.Promise<HybridManagerActionEndEventResult> {
    const id = this.callAction(actionType, data, contextElements);
    return this.when(`hybridActionEnd:${id}`).then(event => ({
      data: event.data,
      contextElements: event.contextElements
    }));
  }

  /**
   * Calls the form hybrid action with the action type `openForm:${modelVariant}` to create, start and show the requested form.
   *
   * @param modelVariant the suffix for the actionType so the correct hybrid action can be resolved
   * @param data a data object that will be passed to the hybrid action
   * @returns a promise that will be resolved once the form has been created
   */
  openForm(modelVariant: string, data?: AnyDoEntity): JQuery.Promise<Form> {
    const id = this.callAction(`scout.openForm:${modelVariant}`, data);
    return this.when(`widgetAdd:${id}`).then(event => this._onFormAdd(event.widget as Form));
  }

  /**
   * Calls the form hybrid action with the action type `createForm:${modelVariant}` to create and start the requested form without showing it.
   *
   * @param modelVariant the suffix for the actionType so the correct hybrid action can be resolved
   * @param data a data object that will be passed to the hybrid action
   * @returns a promise that will be resolved once the form has been created
   */
  createForm(modelVariant: string, data?: AnyDoEntity): JQuery.Promise<Form> {
    const id = this.callAction(`scout.createForm:${modelVariant}`, data);
    return this.when(`widgetAdd:${id}`).then(event => this._onFormAdd(event.widget as Form));
  }

  protected _onFormAdd(form: HybridManagerForm) {
    form.one('close', () => {
      form.__closeTriggered = true;
    });
    return form;
  }

  /**
   * Calls the hybrid action with the action type 'scout.DisposeWidgets' to dispose the given widgets on the UI server.
   */
  disposeWidgets(widgets: Widget | Widget[]) {
    this._callDisposeWidgets(widgets);
  }

  /**
   * Calls the hybrid action with the action type 'scout.DisposeWidgets' to dispose the given widgets on the UI server.
   * The given widgets are collected in {@link #_widgetsToBeDisposed} and sent in one hybrid action.
   * After the hybrid action is called the ids of the {@link HybridManagerWidget}s in {@link #_widgetsToBeDisposed} are transferred to {@link #_widgetIdsBeingDisposed} and {@link #_widgetsToBeDisposed} is reset.
   */
  protected _callDisposeWidgets(widgets: HybridManagerWidget | HybridManagerWidget[]) {
    // filter remote widgets that are not being disposed already
    widgets = arrays.ensure(widgets).filter(widget => !!widget.__remoteId && !this._widgetIdsBeingDisposed.has(widget.__remoteId));

    // nothing to dispose
    if (!widgets.length) {
      return;
    }

    // if next batch was not created already, create it and queue microtask to sent hybrid action
    if (!this._widgetsToBeDisposed) {
      this._widgetsToBeDisposed = new Set<HybridManagerWidget>();
      queueMicrotask(() => {
        // collect remote ids, transfer widget ids to this._widgetsToBeDisposed and reset next batch
        const remoteIds: string[] = [];
        for (const widget of [...this._widgetsToBeDisposed]) {
          if (!widget.__remoteId) {
            continue;
          }
          remoteIds.push(widget.__remoteId);
          this._widgetIdsBeingDisposed.add(widget.id);
        }
        this._widgetsToBeDisposed = null;

        // no widgets to dispose
        if (!remoteIds.length) {
          return;
        }

        // call hybrid action
        this.callAction('scout.DisposeWidgets', scout.create(DisposeWidgetsHybridActionDo, {ids: remoteIds}));
      });
    }

    // add remote id to next batch
    for (const widget of widgets) {
      this._widgetsToBeDisposed.add(widget);
    }
  }

  // event support

  override one<K extends string & keyof EventMapOf<this['self']>>(type: K | `${K}:${string}`, handler: EventHandler<EventMapOf<this>[K] & Event<this>>) {
    super.one(type as K, handler);
  }

  override on<K extends string & keyof EventMapOf<this['self']>>(type: K | `${K}:${string}`, handler: EventHandler<EventMapOf<this>[K] & Event<this>>): EventListener {
    return super.on(type as K, handler);
  }

  override off<K extends string & keyof EventMapOf<this['self']>>(type: K | `${K}:${string}`, handler?: EventHandler<EventMapOf<this>[K] & Event<this>>) {
    super.off(type as K, handler);
  }

  override when<K extends string & keyof EventMapOf<this['self']>>(type: K | `${K}:${string}`): JQuery.Promise<EventMapOf<this>[K] & Event<this>> {
    return super.when(type as K);
  }
}

export interface HybridManagerActionEndEventResult {
  data: AnyDoEntity;
  contextElements?: HybridActionContextElements;
}

interface HybridManagerForm extends Form {
  /**
   * @returns true if {@link FormEventMap.close} event was triggered at least once for this form.
   */
  __closeTriggered?: boolean;
}

export interface HybridManagerWidget extends Widget {
  __remoteId?: string;
}

export class HybridManagerWidgetExtension extends Extension<HybridManagerWidget> {

  init() {
    this.extend(Widget.prototype, 'destroy');
  }

  destroy() {
    if (this.extended.__remoteId) {
      this.extended.destroying = true;
      HybridManager.get(this.extended.session).disposeWidgets(this.extended);
      return;
    }
    this.next();
  }
}

App.addListener('installExtensions', () => {
  Extension.install(HybridManagerWidgetExtension);
});
