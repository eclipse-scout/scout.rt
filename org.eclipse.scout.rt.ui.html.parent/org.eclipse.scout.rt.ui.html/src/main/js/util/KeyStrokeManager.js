scout.KeystrokeManager = function() {
  this._adapters = []; // FIXME BSH Keystroke | Check if we really need this
  //f1-help
  $(document).keydown(function(event) {
    if (event.which === scout.keys.F1) {
            drawKeyBox();
            event.preventDefault();
    }
  });

  $(document).keyup(function(event) {
    if (event.which === scout.keys.F1) {
      removeKeyBox();
      event.preventDefault();
    }
  });

  $(window).blur(function() {
    removeKeyBox();
  });

  var that = this;

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
    var i, adapter;
    for (i = 0; i < that._adapters.length; i++) {
      adapter = that._adapters[i];
      if (adapter.drawKeyBox) {
        adapter.drawKeyBox();
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
    var i, keyStroke;
    var bubbleUp = true;
    //if bubble up should be prevented then continue and prevent bubble up also if no key strokes are registered
    if ((!adapter.keyStrokes || !adapter.accept(event)) && !adapter.preventBubbleUp(event)) {
      return;
    }
    for (i = 0; i < adapter.keyStrokes.length; i++) {
      keyStroke = adapter.keyStrokes[i];
      if (keyStroke.accept && keyStroke.accept(event)) {
        keyStroke.handle(event);
        bubbleUp = bubbleUp && keyStroke.bubbleUp;
        if (keyStroke.removeKeyBox) {
          adapter.removeKeyBox();
        }
      }
    }
    if (!bubbleUp || adapter.preventBubbleUp(event)) {
      adapter.removeKeyBox();
      event.stopPropagation();
    }
  };

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
