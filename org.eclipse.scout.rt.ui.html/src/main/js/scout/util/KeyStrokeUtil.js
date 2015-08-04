/**
 * Utility to install keystroke handling on DOM elements in the form of 'scout.AbstractKeyStrokeAdapter's.
 */
scout.KeyStrokeUtil = function() {
};

/**
 * Initializes keystroke handing for the given session.
 */
scout.KeyStrokeUtil.init = function($entryPoint) {
  // Disable browser help.
  if (window.onhelp) {
    window.onhelp = $.returnFalse;
  }
  var preventHelpKeyStroke = function(event) {
    if (scout.KeyStrokeUtil._isHelpKeyStroke(event)) {
      event.preventDefault();
    }
  };
  $entryPoint.keydown(preventHelpKeyStroke);
  $entryPoint.keyup(preventHelpKeyStroke);
};

/**
 * Installs the given keystroke adapter globally on $entryPoint.
 */
scout.KeyStrokeUtil.installAdapter = function(session, adapter) {
  scout.KeyStrokeUtil.installAdapter(session, adapter, session.$entryPoint, true);
};

/**
 * Installs the given keystroke adapter. This method has no effect if the adapter is null, or already installed.
 */
scout.KeyStrokeUtil.installAdapter = function(session, adapter, $element, global) {
  if (!adapter) {
    return; // no adapter to install
  }

  if (adapter._handler) {
    return; // adapter already installed
  }

  if (!$element) {
    throw new Error('missing argument \'$element\'');
  }

  adapter._handler = function(event) {
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
    if (scout.KeyStrokeUtil._isHelpKeyStroke(event) && adapter.drawKeyBox) {
      scout.KeyStrokeUtil._drawKeyStrokes(adapter, event.target);
    }

    // Delegate control to registered keystrokes, but break once a keystroke requests immediate stop of propagation.
    scout.KeyStrokeUtil._handleKeyStroke(adapter, event);
  };

  // Install the keystroke, either on the given $element, or on $entryPoint for global keystrokes.
  adapter._handler.$target = (global ? session.$entryPoint : $element);
  adapter._handler.$target.keydown(adapter._handler);
};

scout.KeyStrokeUtil.uninstallAdapter = function(adapter) {
  if (!adapter) {
    return; // no adapter to uninstall
  }
  if (!adapter._handler) {
    return; // adapter already uninstalled
  }

  adapter._handler.$target.off('keydown', adapter._handler);
  adapter._handler.$target = null;
  adapter._handler = null;
};

scout.KeyStrokeUtil._isHelpKeyStroke = function(event) {
  return event.which === scout.keys.F1;
};

scout.KeyStrokeUtil._drawKeyStrokes = function(adapter, target) {
  // Draws available keystrokes.
  adapter.drawKeyBox({});

  // Registers 'key-up' and 'window-blur' handler to remove drawn keystrokes.
  var keyUpHandler = function(event) {
    removeKeyBoxAndHandlers(scout.KeyStrokeUtil._isHelpKeyStroke(event)); // only on 'key-up' for 'help keystroke'
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

scout.KeyStrokeUtil._handleKeyStroke = function(adapter, event) {
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

  if (stopImmediate) {
    event.stopImmediatePropagation();
  }
  if (stopBubble) {
    event.stopPropagation();
    // FIXME [dwi] why validate focus?? Smartfield does not work that way
    // FIXME [dwi] without this, arrow-down on table does not work correctly!!
//    scout.focusManager.validateFocus(adapter.uiSessionId());
  }
};
