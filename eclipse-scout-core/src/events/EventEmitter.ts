/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, EventListener, EventMap, EventSupport} from '../index';

export interface EventHandler<K extends Event = Event> {
  (event: K): void;
}

export type EventMapOf<T> = T extends { eventMap: infer Map } ? Map : object;
/** Omits all properties that cannot be passed as part of the event model when the event is triggered. */
export type EventModel<T> = Omit<T, 'source' | 'defaultPrevented' | 'type' | 'preventDefault'>;

export default class EventEmitter {
  events: EventSupport;
  declare eventMap: EventMap;

  constructor() {
    this.events = this._createEventSupport();
  }

  protected _createEventSupport(): EventSupport {
    return new EventSupport();
  }

  // Cannot use this as for other methods, see https://stackoverflow.com/questions/73648158/type-error-when-using-this-with-a-custom-type
  trigger<K extends string & keyof EventMapOf<EventEmitter>>(type: K, eventOrModel?: Event | EventModel<EventMapOf<EventEmitter>[K]>): EventMapOf<EventEmitter>[K] {
    let event;
    if (eventOrModel instanceof Event) {
      event = eventOrModel;
    } else {
      event = new Event(eventOrModel);
    }
    event.source = this;
    this.events.trigger(type, event);
    return event;
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   * The function will only be called once. After that it is automatically de-registered using {@link off}.
   *
   * @param type One or more event names separated by space.
   * @param handler Event handler executed when the event is triggered. An event object is passed to the function as first parameter
   */
  one<K extends string & keyof EventMapOf<this>>(type: K, handler: EventHandler<EventMapOf<this>[K] & Event<this>>) {
    this.events.one(type, handler);
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   *
   * @param type One or more event names separated by space.
   * @param handler Event handler executed when the event is triggered. An event object is passed to the function as first parameter.
   **/
  on<K extends string & keyof EventMapOf<this>>(type: K, handler: EventHandler<(EventMapOf<this>)[K] & Event<this>>): EventListener {
    return this.events.on(type, handler);
  }

  /**
   * De-registers the given event handler for the event specified by the type param.
   *
   * @param type One or more event names separated by space.<br/>
   *      Important: the string must be equal to the one used for {@link on} or {@link one}. This also applies if a string containing multiple types separated by space was used.
   * @param handler The exact same event handler that was used for registration using {@link on} or {@link one}.
   *      If no handler is specified, all handlers are de-registered for the given type.
   */
  off<K extends string & keyof EventMapOf<this>>(type: K, handler?: EventHandler<EventMapOf<this>[K]>) {
    this.events.off(type, handler);
  }

  /**
   * Adds an event handler using {@link one} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   */
  when<K extends string & keyof EventMapOf<this>>(type: K): JQuery.Promise<EventMapOf<this>[K] & Event<this>> {
    return this.events.when(type) as JQuery.Promise<EventMapOf<this>[K] & Event<this>>;
  }

  addListener(listener: EventListener) {
    this.events.addListener(listener);
  }

  removeListener(listener: EventListener) {
    this.events.removeListener(listener);
  }
}
