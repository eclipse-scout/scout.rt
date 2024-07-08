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
  Constructor, Event, EventHandler, EventListener, EventMapOf, Form, HybridActionContextElement, HybridActionEvent, HybridManagerEventMap, HybridManagerWidgetAddEvent, HybridManagerWidgetRemoveEvent, InitModelOf, ObjectOrChildModel, scout,
  Session, UuidPool, Widget
} from '../../index';

/**
 * A utility to invoke remote Java actions to simplify the interaction of Scout JS and Scout Classic code
 * to facilitate the creation of hybrid applications.
 */
export class HybridManager extends Widget {
  declare eventMap: HybridManagerEventMap;
  declare self: HybridManager;

  widgets: Record<string, Widget>;

  constructor() {
    super();

    this.widgets = {};
  }

  // static helpers

  static get(session: Session, wait?: false): HybridManager;
  static get(session: Session, wait: true): JQuery.Promise<HybridManager>;
  static get(session: Session, wait?: boolean): HybridManager | JQuery.Promise<HybridManager> {
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

  static getContextWidget<W extends Widget>(contextElements: Record<string, HybridActionContextElement>, key: string, widgetType?: Constructor<W>): W {
    if (contextElements?.[key]?.widget) {
      return scout.assertInstance(contextElements[key].widget, widgetType);
    }
    return null;
  }

  static getContextElement<E>(contextElements: Record<string, HybridActionContextElement>, key: string, elementType?: Constructor<E>): E {
    if (contextElements?.[key]?.element) {
      return scout.assertInstance(contextElements[key].element, elementType);
    }
    return null;
  }

  // init

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setWidgets(this.widgets);
  }

  // widgets

  protected _setWidgets(widgets: Record<string, ObjectOrChildModel<Widget>>) {
    widgets = this._ensureWidgets(widgets);

    const removedWidgets: Record<string, Widget> = {};
    for (const [id, widget] of Object.entries(this.widgets)) {
      if (!widgets[id]) {
        removedWidgets[id] = widget;
      }
    }
    const addedWidgets: Record<string, Widget> = {};
    for (const [id, widget] of Object.entries(widgets as Record<string, Widget>)) {
      if (!this.widgets[id]) {
        addedWidgets[id] = widget;
      }
    }
    this._destroyOrUnlinkChildren(Object.values(removedWidgets));

    this._setProperty('widgets', widgets);

    Object.entries(addedWidgets).forEach(([id, widget]) => this._triggerWidgetAdd(id, widget));
    Object.entries(removedWidgets).forEach(([id, widget]) => this._triggerWidgetRemove(id, widget));
  }

  protected _ensureWidgets(modelsOrWidgets: Record<string, ObjectOrChildModel<Widget>>): Record<string, Widget> {
    const result: Record<string, Widget> = {};
    Object.keys(modelsOrWidgets).forEach(id => {
      // Create new child widget(s)
      result[id] = this._createChildren(modelsOrWidgets[id]);
    });
    return result;
  }

  protected _triggerWidgetAdd(id: string, widget: Widget) {
    this.trigger(`widgetAdd:${id}`, {widget} as HybridManagerWidgetAddEvent);
  }

  protected _triggerWidgetRemove(id: string, widget: Widget) {
    this.trigger(`widgetRemove:${id}`, {widget} as HybridManagerWidgetRemoveEvent);
  }

  // hybrid events (java to js)

  /** @internal */
  onHybridEvent(id: string, eventType: string, data: object, contextElements: Record<string, HybridActionContextElement>) {
    this.trigger(`${eventType}:${id}`, {
      data,
      contextElements
    });
  }

  /** @internal */
  onHybridWidgetEvent(id: string, eventType: string, data: object) {
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

  protected _onHybridWidgetEvent(widget: Widget, eventType: string, data: object) {
    widget.trigger(eventType, {data});
  }

  protected _onHybridFormEvent(form: Form, eventType: string, data: object) {
    if (eventType === 'reset') {
      form.setData(data);
      form.trigger('reset');
    } else if (eventType === 'save') {
      form.setData(data);
      form.trigger('save');
    } else if (eventType === 'close') {
      form.trigger('close');
    } else {
      this._onHybridWidgetEvent(form, eventType, data);
    }
  }

  // hybrid actions (js to java)

  protected _createEventId(): string {
    return UuidPool.take(this.session);
  }

  /**
   * @deprecated use {@link callAction} instead.
   */
  triggerHybridAction(eventType: string, data?: object): string {
    return this.callAction(eventType, data);
  }

  /**
   * Calls the hybrid action that matches the given action type.
   *
   * @returns the id of the triggered hybrid action
   * @see IHybridAction.java
   */
  callAction(actionType: string, data?: object, contextElements?: Record<string, HybridActionContextElement>): string {
    const id = this._createEventId();
    this.trigger('hybridAction', {data: {id, actionType, contextElements, data}} as HybridActionEvent);
    return id;
  }

  /**
   * @deprecated use {@link callActionAndWait} instead.
   */
  async triggerHybridActionAndWait(eventType: string, data?: object): Promise<object> {
    return this.callActionAndWait(eventType, data);
  }

  /**
   * Calls the hybrid action that matches the given action type and returns a promise that will be resolved once the corresponding hybridActionEnd event arrives.
   *
   * @returns a promise that will be resolved once the corresponding hybridActionEnd event arrives.
   * @see IHybridAction
   * @see AbstractHybridAction.fireHybridActionEndEvent
   */
  callActionAndWait(actionType: string, data?: object, contextElements?: Record<string, HybridActionContextElement>): JQuery.Promise<object> {
    return this.callActionAndWaitWithContext(actionType, data, contextElements).then(result => result.data);
  }

  callActionAndWaitWithContext(actionType: string, data?: object, contextElements?: Record<string, HybridActionContextElement>): JQuery.Promise<HybridManagerActionEndEventResult> {
    const id = this.callAction(actionType, data, contextElements);
    return this.when(`hybridActionEnd:${id}`).then(event => {
      return {
        data: event.data,
        contextElements: event.contextElements
      };
    });
  }

  /**
   * Calls the form hybrid action with the action type `openForm:${modelVariant}` to create, start and show the requested form.
   *
   * @param modelVariant the suffix for the actionType so the correct hybrid action can be resolved
   * @param data a data object that will be passed to the hybrid action
   * @returns a promise that will be resolved once the form has been created
   */
  openForm(modelVariant: string, data?: object): JQuery.Promise<Form> {
    const id = this.callAction(`openForm:${modelVariant}`, data);
    return this.when(`widgetAdd:${id}`).then(event => event.widget as Form);
  }

  /**
   * Calls the form hybrid action with the action type `createForm:${modelVariant}` to create and start the requested form without showing it.
   *
   * @param modelVariant the suffix for the actionType so the correct hybrid action can be resolved
   * @param data a data object that will be passed to the hybrid action
   * @returns a promise that will be resolved once the form has been created
   */
  createForm(modelVariant: string, data?: object): JQuery.Promise<Form> {
    const id = this.callAction(`createForm:${modelVariant}`, data);
    return this.when(`widgetAdd:${id}`).then(event => event.widget as Form);
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
  data: object;
  contextElements: Record<string, HybridActionContextElement>;
}
