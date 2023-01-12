/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, EventListener, EventMap, EventSupport} from '../index';

export interface EventHandler<K extends Event = Event> {
  (event: K): void;
}

export type EventMapOf<T> = T extends { eventMap: infer TMap } ? TMap : object;
/** Omits all properties that cannot be passed as part of the event model when the event is triggered. */
export type EventModel<T> = Omit<T, 'source' | 'defaultPrevented' | 'type' | 'preventDefault'>;

export class EventEmitter {
  events: EventSupport;
  declare eventMap: EventMap;
  declare self: EventEmitter; // Reading the event map with this does not work when the class has a generic -> The class has to define self with any as generic value

  constructor() {
    this.events = this._createEventSupport();
  }

  protected _createEventSupport(): EventSupport {
    return new EventSupport();
  }

  // Cannot use this as for other methods, see https://stackoverflow.com/questions/73648158/type-error-when-using-this-with-a-custom-type
  trigger<K extends string & keyof EventMapOf<EventEmitter>>(type: K, eventOrModel?: Event | EventModel<EventMapOf<EventEmitter>[K]>): EventMapOf<EventEmitter>[K] {
    let event: Event;
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
  one<K extends string & keyof EventMapOf<this['self']>>(type: K, handler: EventHandler<EventMapOf<this>[K] & Event<this>>) {
    this.events.one(type, handler);
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   *
   * @param type One or more event names separated by space.
   * @param handler Event handler executed when the event is triggered. An event object is passed to the function as first parameter.
   **/
  on<K extends string & keyof EventMapOf<this['self']>>(type: K, handler: EventHandler<EventMapOf<this>[K] & Event<this>>): EventListener {
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
  off<K extends string & keyof EventMapOf<this['self']>>(type: K, handler?: EventHandler<EventMapOf<this>[K] & Event<this>>) {
    this.events.off(type, handler);
  }

  /**
   * Adds an event handler using {@link one} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   */
  when<K extends string & keyof EventMapOf<this['self']>>(type: K): JQuery.Promise<EventMapOf<this>[K] & Event<this>> {
    return this.events.when(type) as JQuery.Promise<EventMapOf<this>[K] & Event<this>>;
  }

  addListener(listener: EventListener) {
    this.events.addListener(listener);
  }

  removeListener(listener: EventListener) {
    this.events.removeListener(listener);
  }
}
