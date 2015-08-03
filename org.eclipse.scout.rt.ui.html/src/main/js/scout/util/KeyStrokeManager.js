/**
 * The keystroke manager exists once per session, and ensures proper keystroke handling.
 */
scout.KeyStrokeManager = function(session) {
  this._keyStrokeManagers = {};

  // Disable default help on IE.
  if (window.onhelp) {
    window.onhelp = $.returnFalse; // (jQuery cannot bind 'onhelp' event)
  }
};

/**
 * Installs keystroke handling for the given session.
 */
scout.KeyStrokeManager.prototype.installManagerForSession = function(session) {
  var keyStrokeManager = {
      session: session,
      _adaptersToDraw:  [],
      _adapters: [] // FIXME BSH Keystroke | Check if we really need this
    };
  this._keyStrokeManagers[session.uiSessionId] = keyStrokeManager;

  var $entryPoint = session.$entryPoint;

  // F1-help (key down)
  $(document).keydown(function(event) {
    if (event.which === scout.keys.F1) {
      _drawKeyBox();
      event.preventDefault();
    }
    keyStrokeManager._adaptersToDraw = [];
  });

  // F1-help (key up)
  $(document).keyup(function(event) {
    if (event.which === scout.keys.F1) {
      _removeKeyBox();
      event.preventDefault();
    }
    keyStrokeManager._adaptersToDraw = [];
  });

  // blur
  $(window).blur(function() {
    _removeKeyBox();
    keyStrokeManager._adaptersToDraw = [];
  });

  function _removeKeyBox() {
    keyStrokeManager._adapters
        .filter(function(adapter) {
          return adapter.removeKeyBox; // TODO [dwi] check whether necessary
        })
        .forEach(function(adapter) {
          adapter.removeKeyBox();
        });
  }

  function _drawKeyBox() {
    keyStrokeManager._adaptersToDraw
        .filter(function(adapter) {
          return adapter.drawKeyBox; // TODO [dwi] check whether necessary
        })
        .forEach(function(adapter) {
          adapter.drawKeyBox({});
        });
  }
};

scout.KeyStrokeManager.prototype.installAdapter = function(session, $element, adapter) {
  var keyStrokeManager = this._keyStrokeManagers[session.uiSessionId];

  if (!keyStrokeManager) {
    throw new Error('no keystroke manager found for session: ' + session.uiSessionId);
  }
  if (!$element) {
    throw new Error('missing argument \'$element\'');
  }
  if (!adapter) {
    throw new Error('missing argument \'adapter\'');
  }
  if (this.isAdapterInstalled(session, adapter)) {
    return;
  }

  var controller = function(event) {
    if (event.originalEvent&&event.originalEvent.anchorReached) {
      return;
    }
    //Trace adapter if it is affected when key pressed.
    keyStrokeManager._adaptersToDraw.push(adapter);

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
      keyStrokeManager._adaptersToDraw = [];
      if(stopImmediate){
        event.stopImmediatePropagation();
      }
      event.stopPropagation();
      // FIXME [dwi] why validate focus?? Smartfield does not work that way
      // FIXME [dwi] without this, arrow-down on table does not work correctly!!
//      scout.focusManager.validateFocus(adapter.uiSessionId());
    }
  }.bind(this);

  keyStrokeManager._adapters.push(adapter);
  adapter.$target = $element;
  adapter.controller = controller;
  $element.keydown(controller);
};

scout.KeyStrokeManager.prototype.isAdapterInstalled = function(session, adapter) {
  return this._keyStrokeManagers[session.uiSessionId]._adapters.indexOf(adapter) > -1;
};

scout.KeyStrokeManager.prototype.uninstallAdapter = function(session, adapter) {
  var keyStrokeManager = this._keyStrokeManagers[session.uiSessionId];
  if (!keyStrokeManager) {
    throw new Error('no keystroke manager found for session: ' + session.uiSessionId);
  }
  if (!adapter) {
    throw new Error('missing argument \'adapter\'');
  }


  scout.arrays.remove(keyStrokeManager._adapters, adapter);
  if (adapter.$target) {
    adapter.$target.off('keydown', undefined, adapter.controller);
  }
};

//Singleton
scout.keyStrokeManager = new scout.KeyStrokeManager();
