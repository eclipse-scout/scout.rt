/**
 * The keystroke manager exists once per session, and ensures proper keystroke handling.
 */
scout.KeyStrokeManager = function(session) {
  this._keyStrokeManagers = {};

  // Prevent the browser from displaying its help on F1.
  if (window.onhelp) {
    window.onhelp = $.returnFalse;
  }
};


scout.KeyStrokeManager.prototype._isHelpKeyStroke = function(event) {
  return event.which === scout.keys.F1;
};

/**
 * Installs keystroke handling for the given session.
 */
scout.KeyStrokeManager.prototype.installManagerForSession = function(session) {
  var keyStrokeManager = {
      _adapters: [] // FIXME BSH Keystroke | Check if we really need this
    };
  this._keyStrokeManagers[session.uiSessionId] = keyStrokeManager;

  var preventHelpKeyStroke = function(event) {
    if (scout.keyStrokeManager._isHelpKeyStroke(event)) {
      event.preventDefault();
    }
  };
  session.$entryPoint.keydown(preventHelpKeyStroke);
  session.$entryPoint.keyup(preventHelpKeyStroke);
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

  var keyStrokeHandler = function(event) {
    if (event.originalEvent && event.originalEvent.anchorReached) {
      return;
    }

    if (adapter.anchorKeyStrokeAdapter) {
      // Append information about anchor reached to original event to provide information to all listeners upwards.
      event.originalEvent.anchorReached = true;
    }

    // Check whether keystroke is valid.
    if (!adapter.keyStrokes || !adapter.accept(event)) {
      if (adapter.preventBubbleUp(event)) {
        event.stopPropagation();
      }
      return;
    }

    // Draw keystrokes on help keystroke.
    if (scout.keyStrokeManager._isHelpKeyStroke(event) && adapter.drawKeyBox) {
      drawKeyStrokes(adapter, event.target);
    }

    // Delegate control to registered keystrokes, but break once a keystroke requests immediate stop of propagation.
    var stopBubble = adapter.preventBubbleUp(event),
        stopImmediate = adapter.keyStrokes
            .filter(function(keyStroke) {
              return keyStroke.accept && keyStroke.accept(event);
            })
            .some(function(keyStroke) {
              // Handle the keystroke.
              keyStroke.handle(event);

              // Store whether to bubble up.
              stopBubble = stopBubble || !keyStroke.bubbleUp;

              // Break on 'stopImmediate'.
              return keyStroke.stopImmediate; // 'some-loop' breaks on true.
            });

    // Control event propagation.
    if (stopImmediate) {
      event.stopImmediatePropagation();
    }
    if (stopBubble) {
      event.stopPropagation();
      // FIXME [dwi] why validate focus?? Smartfield does not work that way
      // FIXME [dwi] without this, arrow-down on table does not work correctly!!
//      scout.focusManager.validateFocus(adapter.uiSessionId());
    }
  },
  drawKeyStrokes = function(adapter, target) {
    // Draws available keystrokes.
    adapter.drawKeyBox({});

    // Registers 'key-up' and 'window-blur' handler to remove drawn keystrokes.
    var keyUpHandler = function(event) {
      removeKeyBoxAndHandlers(scout.keyStrokeManager._isHelpKeyStroke(event)); // only on 'key-up' for 'help keystroke'
    },
    windowBlurHandler = function(event) {
      removeKeyBoxAndHandlers(true);
    },
    removeKeyBoxAndHandlers = function(doit) {
      if (doit) {
        $(target).off('keyup', keyUpHandler);
        $(window).off('blur', windowBlurHandler);
        adapter.removeKeyBox();
      }
    };

    $(target).on('keyup', keyUpHandler); // once the respective 'key-up' event occurs
    $(window).on('blur', windowBlurHandler); // once the current browser tab/window is left
  };

  $element.keydown(keyStrokeHandler);

  adapter.$target = $element;
  adapter.controller = keyStrokeHandler;

  keyStrokeManager._adapters.push(adapter);
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

// Singleton
scout.keyStrokeManager = new scout.KeyStrokeManager();
