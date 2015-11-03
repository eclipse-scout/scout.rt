/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

scout.EventSupport.prototype.on = function(type, func) {
  if (!func) {
    throw new Error('Missing callback function');
  }

  var listener = {
    type: type,
    func: func
  };
  this.addListener(listener);
  return listener;
};

scout.EventSupport.prototype.off = function(type, func) {
  if (!type && !func) {
    return;
  }

  var listeners = this._eventListeners.slice();
  for (var i = 0; i < listeners.length; i++) {
    var listener = listeners[i];
    var funcMatches = (func === listener.func);
    var typeMatches = (type === listener.type);
    var remove = false;
    if (func && type) {
      remove = (funcMatches && typeMatches);
    } else if (func) {
      remove = funcMatches;
    } else if (type) {
      remove = typeMatches;
    }

    if (remove) {
      scout.arrays.remove(this._eventListeners, listener);
    }
  }
};

scout.EventSupport.prototype.addListener = function(listener) {
  this._eventListeners.push(listener);
};

scout.EventSupport.prototype.removeListener = function(listener) {
  scout.arrays.remove(this._eventListeners, listener);
};

scout.EventSupport.prototype.trigger = function(type, event) {
  event = event || {};
  event.type = type;

  var listeners = this._eventListeners.slice();
  for (var i = 0; i < listeners.length; i++) {
    var listener = listeners[i];
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
