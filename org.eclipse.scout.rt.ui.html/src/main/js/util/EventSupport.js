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
import {objects} from '../index';
import {arrays} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';

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
 * Registers the given func for the event specified by the type param.
 *
 * @param {string} type event-name
 * @param {function} func callback function executed when event is triggered. An event object is passed to the func as first parameter
 * @param {function} [origFunc] (optional) used internally when func is registered with one(). The property is set on the listener
 *   object so the event-handler can be de-registered by using the original function.
 */
on(type, func, origFunc) {
  this._assertFunc(func);
  var listener = {
    type: type,
    func: func,
    origFunc: origFunc
  };
  this.addListener(listener);
  return listener;
}

/**
 * Registers the given func for the event specified by the type param.
 * The event is only triggered one time, and after that it is automatically de-registered by calling the off() function.
 *
 * @param {string} type event-name
 * @param {function} func callback function executed when event is triggered. An event object is passed to the func as first parameter
 */
one(type, func) {
  this._assertFunc(func);
  var that = this,
    offFunc = function(event) {
      that.off(type, offFunc);
      func(event);
    }.bind(this);
  return this.on(type, offFunc, func);
}

off(type, func) {
  if (!type && !func) {
    return;
  }

  for (var i = this._eventListeners.length - 1; i >= 0; i--) {
    var listener = this._eventListeners[i];
    var funcMatches = (func === listener.func || func === listener.origFunc);
    var typeMatches = (type === listener.type);
    var remove = false;
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
  var deferred = $.Deferred();
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
  var count = 0;
  this._eventListeners.forEach(function(listener) {
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

  var i, listener, listeners = this._eventListeners.slice();
  for (i = 0; i < listeners.length; i++) {
    listener = listeners[i];
    if (!listener.type || typeMatches.call(this, event, listener.type)) {
      listener.func(event);
    }
  }

  // ---- Helper functions -----

  function typeMatches(event, listenerType) {
    var eventType = event.type;
    var types = listenerType.split(' ');
    // support for multi type definition 'type1 type2 [...]'
    for (var i = 0; i < types.length; i++) {
      if (eventType === types[i]) {
        return true;
      }
      if (this._subTypeMatches(event, types[i])) {
        return true;
      }
    }
    return false;
  }
}

_subTypeMatches(event, listenerType) {
  if (listenerType.indexOf(':') < 0) {
    return false;
  }
  var parts = listenerType.split(':');
  var type = parts[0];
  var subType = parts[1];
  var predicate = this._subTypePredicates[type];
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
