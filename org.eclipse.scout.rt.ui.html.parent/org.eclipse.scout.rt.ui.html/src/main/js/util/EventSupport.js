scout.EventSupport = function() {
  this._eventListeners = [];
};

scout.EventSupport.prototype.on = function(type, func) {
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
    var remove = false;
    if (func && type) {
      remove = func === listener.func && type === listener.type;
    } else if (func) {
      remove = func === listener.func;
    } else if (type) {
      remove = type === listener.type;
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

  var listener;
  for (var i = 0; i < this._eventListeners.length; i++) {
    listener = this._eventListeners[i];
    if (!listener.type || listener.type === event.type) {
      listener.func(event);
    }
  }
};
