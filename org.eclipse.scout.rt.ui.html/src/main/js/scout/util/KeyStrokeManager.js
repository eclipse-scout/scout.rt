/**
 * The keystroke manager exists once per session, and ensures proper keystroke handling.
 */
scout.KeyStrokeManager = function() {
  // Prevent the browser from displaying its help on F1.
  if (window.onhelp) {
    window.onhelp = $.returnFalse;
  }
};

scout.KeyStrokeManager.prototype._isHelpKeyStroke = function(event) {
  return event.which === scout.keys.F1;
};

/**
 * Initializes keystroke handing for the given session.
 */
scout.KeyStrokeManager.prototype.init = function($entryPoint) {
  var preventHelpKeyStroke = function(event) {
    if (scout.keyStrokeManager._isHelpKeyStroke(event)) {
      event.preventDefault();
    }
  };
  $entryPoint.keydown(preventHelpKeyStroke);
  $entryPoint.keyup(preventHelpKeyStroke);
};

/**
 * Installs the given keystroke adapter. This method has no effect if the adapter is null, or already installed.
 */
scout.KeyStrokeManager.prototype.installAdapter = function(session, $element, adapter) {
  if (!adapter) {
    return; // no adapter to install
  }

  if (adapter.handler) {
    return; // adapter already installed
  }

  if (!$element) {
    throw new Error('missing argument \'$element\'');
  }

  adapter.handler = function(event) {
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

    function drawKeyStrokes(adapter, target) {
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
    }
  };

  adapter.handler.$target = $element;
  $element.keydown(adapter.handler);
};

scout.KeyStrokeManager.prototype.uninstallAdapter = function(adapter) {
  if (!adapter) {
    return; // no adapter to uninstall
  }
  if (!adapter.handler) {
    return; // adapter already uninstalled
  }

  adapter.handler.$target.off('keydown', adapter.handler);
  adapter.handler.$target = null;
  adapter.handler = null;
};

// Singleton
scout.keyStrokeManager = new scout.KeyStrokeManager();
