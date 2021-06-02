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
import {arrays, objects, scout} from '../index';
import $ from 'jquery';

export default class EventSupport {

  constructor() {
    this._eventListeners = [];
    this._subTypePredicates = objects.createMap();
  }

  _assertFunc(func) {
    if (!func) {
      throw new Error('Missing callback function');
    }
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   *
   * @param {string} type One or more event names separated by space.
   * @param {function} func Event handler executed when the event is triggered. An event object is passed to the function as first parameter.
   * @param {function} [origFunc] (optional) used internally when func is registered with one(). The property is set on the listener
   *   object so the event-handler can be de-registered by using the original function.
   */
  on(type, func, origFunc) {
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
   * @param {string} type One or more event names separated by space.
   * @param {function} func Event handler executed when the event is triggered. An event object is passed to the function as first parameter
   */
  one(type, func) {
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
   * @param {string} type One or more event names separated by space.<br/>
   *      Important: the string must be equal to the one used for {@link on} or {@link one}. This also applies if a string containing multiple types separated by space was used.
   * @param {function} [func] The exact same event handler that was used for registration using {@link on} or {@link one}.
   *      If no handler is specified, all handlers are de-registered for the given type.
   */
  off(type, func) {
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
   * Adds an event handler using {@link #one()} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   */
  when(type) {
    let deferred = $.Deferred();
    this.one(type, deferred.resolve.bind(deferred));
    return deferred.promise();
  }

  addListener(listener) {
    this._eventListeners.push(listener);
  }

  removeListener(listener) {
    arrays.remove(this._eventListeners, listener);
  }

  count(type, func) {
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

  trigger(type, event) {
    event = event || {};
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

  _typeMatches(event, listenerType) {
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

  _subTypeMatches(event, listenerType) {
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
   * @param {string} type the type which could contain a sub type
   * @param {function} predicate the predicate which will be tested when an event with the given type is triggered. The function has two parameters: event and subType
   */
  registerSubTypePredicate(type, predicate) {
    scout.assertParameter('type', type);
    scout.assertParameter('predicate', predicate);
    this._subTypePredicates[type] = predicate;
  }
}
