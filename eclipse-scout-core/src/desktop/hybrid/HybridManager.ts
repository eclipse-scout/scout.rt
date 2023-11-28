/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Event, EventHandler, EventListener, EventMapOf, Form, HybridManagerEventMap, InitModelOf, ObjectOrChildModel, Session, UuidPool, Widget} from '../../index';

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

  // init

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setWidgets(this.widgets);
  }

  // widgets

  protected _setWidgets(widgets: Record<string, ObjectOrChildModel<Widget>>) {
    const oldWidgets = $.extend(true, {}, this.widgets);
    const oldWidgetIds = Object.keys(oldWidgets);

    widgets = this._ensureWidgets(widgets);

    this._setProperty('widgets', widgets);

    const widgetIdsAdded = [], widgetIdsRemoved = [...oldWidgetIds];

    Object.keys(this.widgets).forEach(id => {
      if (!arrays.remove(widgetIdsRemoved, id)) {
        widgetIdsAdded.push(id);
      }
    });

    widgetIdsAdded.forEach(id => this._triggerWidgetAdd(id, this.widgets[id]));
    widgetIdsRemoved.forEach(id => this._triggerWidgetRemove(id, oldWidgets[id]));
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
    this.trigger(`widgetAdd:${id}`, {widget});
  }

  protected _triggerWidgetRemove(id: string, widget: Widget) {
    this.trigger(`widgetRemove:${id}`, {widget});
  }

  // hybrid events (java to js)

  /** @internal */
  onHybridEvent(id: string, eventType: string, data: object) {
    this.trigger(`${eventType}:${id}`, {data});
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

  _onHybridWidgetEvent(widget: Widget, eventType: string, data: object) {
    widget.trigger(eventType, {data});
  }

  _onHybridFormEvent(form: Form, eventType: string, data: object) {
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

  // TODO CGU call instead of trigger would be a better name, actionType instead of eventType, maybe just call ist callAction
  /**
   * @returns the id of the triggered hybrid action
   */
  triggerHybridAction(eventType: string, data?: object): string {
    const id = this._createEventId();
    this.trigger('hybridAction', {data: {id, eventType, data}});
    return id;
  }

  async triggerHybridActionAndWait(eventType: string, data?: object): Promise<object> {
    const id = this.triggerHybridAction(eventType, data);
    const r = await this.when(`hybridActionEnd:${id}`);
    return r.data;
  }

  async openForm(modelVariant: string, data?: object): Promise<Form> {
    const id = this.triggerHybridAction(`openForm:${modelVariant}`, data);
    const r = await this.when(`widgetAdd:${id}`);
    return r.widget as Form;
  }

  async createForm(modelVariant: string, data?: object): Promise<Form> {
    const id = this.triggerHybridAction(`createForm:${modelVariant}`, data);
    const r = await this.when(`widgetAdd:${id}`);
    return r.widget as Form;
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
