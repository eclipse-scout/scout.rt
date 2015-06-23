scout.KeystrokeManager = function() {
  this._adaptersToDraw = [];
  this._adapters = []; // FIXME BSH Keystroke | Check if we really need this
  var that = this;
  //f1-help
  $(document).keydown(function(event) {
    if (event.which === scout.keys.F1) {
      drawKeyBox();
      event.preventDefault();
    }
    that._adaptersToDraw = [];
  });

  $(document).keyup(function(event) {
    if (event.which === scout.keys.F1) {
      removeKeyBox();
      event.preventDefault();
    }
    that._adaptersToDraw = [];
  });

  $(window).blur(function() {
    removeKeyBox();
    that._adaptersToDraw = [];
  });

  function removeKeyBox() {
    var i, adapter;
    for (i = 0; i < that._adapters.length; i++) {
      adapter = that._adapters[i];
      if (adapter.removeKeyBox) {
        adapter.removeKeyBox();
      }
    }
  }

  function drawKeyBox() {
    var i, adapter, drawedKeys = {};
    for (i = 0; i < that._adaptersToDraw.length; i++) {
      adapter = that._adaptersToDraw[i];
      if (adapter.drawKeyBox) {
        adapter.drawKeyBox(drawedKeys);
      }
    }
  }
};

scout.KeystrokeManager.prototype.installAdapter = function($element, adapter) {
  if (!$element) {
    throw new Error("missing argument '$element'");
  }
  if (!adapter) {
    throw new Error("missing argument 'adapter'");
  }
  if (this.isAdapterInstalled(adapter)) {
    return;
  }

  var controller = function(event) {
    if (event.originalEvent&&event.originalEvent.anchorReached) {
      return;
    }
    //Trace adapter if it is affected when key pressed.
    this._adaptersToDraw.push(adapter);

    //if bubble up should be prevented then continue and prevent bubble up also if no key strokes are registered
    if ((!adapter.keyStrokes || !adapter.accept(event)) && !adapter.preventBubbleUp(event)) {
      return;
    }
    var stopImmediate = false;
    var bubbleUp = true;
    for (var i = 0; i < adapter.keyStrokes.length; i++) {
      var keyStroke = adapter.keyStrokes[i];
      if (keyStroke.accept && keyStroke.accept(event)) {
        keyStroke.handle(event);
        bubbleUp = bubbleUp && keyStroke.bubbleUp;
        if (keyStroke.removeKeyBox) {
          adapter.removeKeyBox();
        }
        if(keyStroke.stopImmediate){
          stopImmediate = true;
          break;
        }
      }
    }
    if (adapter.anchorKeyStrokeAdapter) {
      //append information about anchor reached to original event to provide information to all listeners upwards.
      event.originalEvent.anchorReached = true;
    }
    if (!bubbleUp || adapter.preventBubbleUp(event)) {
      adapter.removeKeyBox();
      this._adaptersToDraw = [];
      if(stopImmediate){
        event.stopImmediatePropagation();
      }
      event.stopPropagation();
      scout.focusManager.validateFocus(adapter.uiSessionId(), 'KeyStrokeManager');
    }
  }.bind(this);

  this._adapters.push(adapter);
  adapter.$target = $element;
  adapter.controller = controller;
  $element.keydown(controller);
};

scout.KeystrokeManager.prototype.isAdapterInstalled = function(adapter) {
  return this._adapters.indexOf(adapter) > -1;
};

scout.KeystrokeManager.prototype.uninstallAdapter = function(adapter) {
  if (!adapter) {
    throw new Error("missing argument 'adapter'");
  }

  scout.arrays.remove(this._adapters, adapter);
  if (adapter.$target) {
    adapter.$target.off('keydown', undefined, adapter.controller);
  }
};

//Singleton
scout.keyStrokeManager = new scout.KeystrokeManager();
