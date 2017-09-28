/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.EventSupport = function() {
  this._eventListeners = [];
};

scout.EventSupport.prototype._assertFunc = function(func) {
  if (!func) {
    throw new Error('Missing callback function');
  }
};

/**
 * Registers the given func for the event specified by the type param.
 *
 * @param type event-name
 * @param func callback function executed when event is triggered. An event object is passed to the func as first parameter
 * @param origFunc (optional) used internally when func is registered with one(). The property is set on the listener
 *   object so the event-handler can be de-registered by using the original function.
 */
scout.EventSupport.prototype.on = function(type, func, origFunc) {
  this._assertFunc(func);
  var listener = {
    type: type,
    func: func,
    origFunc: origFunc
  };
  this.addListener(listener);
  return listener;
};

/**
 * Registers the given func for the event specified by the type param.
 * The event is only triggered one time, and after that it is automatically de-registered by calling the off() function.
 *
 * @param type event-name
 * @param func callback function executed when event is triggered. An event object is passed to the func as first parameter
 */
scout.EventSupport.prototype.one = function(type, func) {
  this._assertFunc(func);
  var that = this,
    offFunc = function(event) {
      that.off(type, offFunc);
      func(event);
    }.bind(this);
  return this.on(type, offFunc, func);
};

scout.EventSupport.prototype.off = function(type, func) {
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
};

scout.EventSupport.prototype.addListener = function(listener) {
  this._eventListeners.push(listener);
};

scout.EventSupport.prototype.removeListener = function(listener) {
  scout.arrays.remove(this._eventListeners, listener);
};

scout.EventSupport.prototype.count = function(type) {
  var count = 0;
  this._eventListeners.forEach(function(listener) {
    if (type && type !== listener.type) {
      return;
    }
    count++;
  });
  return count;
};

scout.EventSupport.prototype.trigger = function(type, event) {
  event = event || {};
  event.type = type;

  var i, listener, listeners = this._eventListeners.slice();
  for (i = 0; i < listeners.length; i++) {
    listener = listeners[i];
    if (!listener.type || typeMatches(event.type, listener.type)) {
      listener.func(event);
    }
  }

  // ---- Helper functions -----

  function typeMatches(eventType, listenerType) {
    var i, types = listenerType.split(' ');
    if (types.length === 1) {
      return eventType === listenerType;
    }
    // support for multi type definition 'type1 type2 [...]'
    for (i = 0; i < types.length; i++) {
      if (eventType === types[i]) {
        return true;
      }
    }
    return false;
  }
};
