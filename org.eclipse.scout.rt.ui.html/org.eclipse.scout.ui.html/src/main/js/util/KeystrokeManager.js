//FIXME keystrokes should consider focused fields. Maybe better to attach those keystrokes directly to the field? What with focused table?
scout.KeystrokeManager = function() {

  this._adapters = [];

  // alt and f1-help
  $(document).keydown(function(event) {
    if (event.which == 18) {
      removeKeyBox();
      drawKeyBox();
    }
  });

  $(document).keyup(function(event) {
    if (event.which == 18) {
      removeKeyBox();
      return false;
    }
  });

  $(document).blur(function() {
    removeKeyBox();
  });

  var that = this;
  $(document).keydown(function(event) {
    var i, j, adapter, handler,
      bubbleUp = true;

    for (i = 0; i < that._adapters.length; i++) {
      adapter = that._adapters[i];

      if (!adapter.handlers) {
        continue;
      }
      for (j = 0; j < adapter.handlers.length; j++) {
        handler = adapter.handlers[j];
        var accepted = false;
        if (handler.keycodes) {
          if(Array.isArray(handler.keycodes) && handler.keycodes.indexOf(event.which) > -1) {
            accepted = true;
          }
          else if(handler.keycodes == event.which) {
            accepted = true;
          }
        }
        else if (handler.keycodeRangeStart && handler.keycodeRangeEnd &&
          handler.keycodeRangeStart <= event.which && event.which <= handler.keycodeRangeEnd) {
          accepted = true;
        }
        if (accepted) {
          bubbleUp = bubbleUp && handler.handle(event.which);
          if (handler.removeKeyBox) {
            removeKeyBox();
          }
        }
      }
    }
    return bubbleUp;
  });

  function removeKeyBox() {
    $('.key-box').remove();

    var i, handler;
    for (i = 0; i < that._adapters.length; i++) {
      handler = that._adapters[i];
      if (handler.removeKeyBox) {
        handler.removeKeyBox();
      }
    }
  }

  function drawKeyBox() {
    var i, handler;
    for (i = 0; i < that._adapters.length; i++) {
      handler = that._adapters[i];
      handler.drawKeyBox();
    }
  }
};

scout.KeystrokeManager.prototype.addAdapter = function(adapter) {
  this._adapters.push(adapter);
};

scout.KeystrokeManager.prototype.removeAdapter = function(adapter) {
  scout.arrays.remove(this._adapters, adapter);
};

//Singleton
scout.keystrokeManager = new scout.KeystrokeManager();
