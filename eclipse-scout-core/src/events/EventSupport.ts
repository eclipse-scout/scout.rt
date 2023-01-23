/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Event, EventHandler, EventListener, objects, scout} from '../index';
import $ from 'jquery';

export type EventSubTypePredicate = (type, subType) => boolean;

export class EventSupport {
  protected _eventListeners: EventListener[];
  protected _subTypePredicates: EventSubTypePredicate[];

  constructor() {
    this._eventListeners = [];
    this._subTypePredicates = objects.createMap();
  }

  protected _assertFunc(func: EventHandler) {
    if (!func) {
      throw new Error('Missing callback function');
    }
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   *
   * @param type One or more event names separated by space.
   * @param func Event handler executed when the event is triggered. An event object is passed to the function as first parameter.
   * @param origFunc Used internally when func is registered with {@link one}. The property is set on the listener
   *   object so the event-handler can be de-registered by using the original function.
   */
  on(type: string, func: EventHandler, origFunc?: EventHandler): EventListener {
    this._assertFunc(func);
    let listener = {
      type: type,
      func: func,
      origFunc: origFunc
    };
    this.addListener(listener);
    return listener;
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   * The function will only be called once. After that it is automatically de-registered using {@link off}.
   *
   * @param type One or more event names separated by space.
   * @param func Event handler executed when the event is triggered. An event object is passed to the function as first parameter
   */
  one(type: string, func: EventHandler): EventListener {
    this._assertFunc(func);
    let offFunc = event => {
      this.off(type, offFunc);
      func(event);
    };
    return this.on(type, offFunc, func);
  }

  /**
   * De-registers the given event handler for the event specified by the type param.
   *
   * @param type One or more event names separated by space.<br/>
   *      Important: the string must be equal to the one used for {@link on} or {@link one}. This also applies if a string containing multiple types separated by space was used.
   * @param func The exact same event handler that was used for registration using {@link on} or {@link one}.
   *      If no handler is specified, all handlers are de-registered for the given type.
   */
  off(type: string, func?: EventHandler) {
    if (!type && !func) {
      return;
    }

    for (let i = this._eventListeners.length - 1; i >= 0; i--) {
      let listener = this._eventListeners[i];
      let funcMatches = (func === listener.func || func === listener.origFunc);
      let typeMatches = (type === listener.type);
      let remove = false;
      if (func && type) {
        remove = (funcMatches && typeMatches);
      } else if (func) {
        remove = funcMatches;
      } else { // always type. all other cases have been checked above
        remove = typeMatches;
      }

      if (remove) {
        this._eventListeners.splice(i, 1);
      }
    }
  }

  /**
   * Adds an event handler using {@link one} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   */
  when(type: string):JQuery.Promise<Event> {
    let deferred = $.Deferred();
    this.one(type, deferred.resolve.bind(deferred));
    return deferred.promise();
  }

  addListener(listener: EventListener) {
    this._eventListeners.push(listener);
  }

  removeListener(listener: EventListener) {
    arrays.remove(this._eventListeners, listener);
  }

  count(type: string, func: EventHandler): number {
    let count = 0;
    this._eventListeners.forEach(listener => {
      if (type && type !== listener.type) {
        return;
      }
      if (func && func !== listener.func) {
        return;
      }
      count++;
    });
    return count;
  }

  trigger(type: string, event?: Event) {
    event = event || {} as Event;
    event.type = type;

    // Create copy because firing a trigger might modify the list of listeners
    let listeners = this._eventListeners.slice();
    // Use traditional "for" loop to reduce size of stack trace
    for (let i = 0; i < listeners.length; i++) {
      let listener = listeners[i];
      if (!listener.type || this._typeMatches(event, listener.type)) {
        listener.func(event);
      }
    }
  }

  protected _typeMatches(event: Event, listenerType: string): boolean {
    let eventType = event.type;
    let types = listenerType.split(' ');
    // support for multi type definition 'type1 type2 [...]'
    for (let i = 0; i < types.length; i++) {
      if (eventType === types[i]) {
        return true;
      }
      if (this._subTypeMatches(event, types[i])) {
        return true;
      }
    }
    return false;
  }

  protected _subTypeMatches(event: Event, listenerType: string): boolean {
    if (listenerType.indexOf(':') < 0) {
      return false;
    }
    let parts = listenerType.split(':');
    let type = parts[0];
    let subType = parts[1];
    let predicate = this._subTypePredicates[type];
    if (!predicate) {
      return;
    }
    return predicate(event, subType);
  }

  /**
   *
   * @param type the type which could contain a sub type
   * @param predicate the predicate which will be tested when an event with the given type is triggered.
   */
  registerSubTypePredicate(type: string, predicate: EventSubTypePredicate) {
    scout.assertParameter('type', type);
    scout.assertParameter('predicate', predicate);
    this._subTypePredicates[type] = predicate;
  }
}
