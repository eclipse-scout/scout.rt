/**
 * Utility to install keystroke handling on DOM elements in the form of 'scout.AbstractKeyStrokeAdapter's.
 */
scout.keyStrokeUtils = function() {
};

/**
 * Disables some keystrokes (F1, Backspace) to be interpreted by the browser.
 */
scout.keyStrokeUtils.disableBrowserKeyStrokes = function($entryPoint) {
  // Prevent browser to interpret F1 (help).
  $entryPoint.keydown(swallowHelpKeyStroke);
  $entryPoint.keyup(swallowHelpKeyStroke);
  if (window.onhelp) {
    window.onhelp = scout.Filters.returnFalse;
  }

  // Prevent browser to interpret backspace.
  $entryPoint.keydown(swallowBackspaceStroke);
  $entryPoint.keyup(swallowBackspaceStroke);

  // ===== Helper functions ===== //

  function swallowHelpKeyStroke(event) {
    return !scout.keyStrokeUtils._isHelpKeyStroke(event);
  }

  function swallowBackspaceStroke(event) {
    return (event.which !== scout.keys.BACKSPACE);
  }
};

/**
 * Installs the given keystroke adapter. This method has no effect if the adapter is null, or already installed.
 *
 * @param adapter
 *        the adapter to be installed
 * @param $target
 *        indicates on which target element to listen for keystroke events, or null to listen globally on $entryPoint
 */
scout.keyStrokeUtils.installAdapter = function(session, adapter, $target) {
  if (!adapter) {
    return; // no adapter to install
  }

  if (adapter._handler) {
    return; // adapter already installed
  }

  adapter._handler = function(event) {
    // Check whether the keystroke is handled by this adapter.
    if (!adapter.keyStrokes ||
        !adapter.accept(event) ||
        scout.focusManager._isElementCovertByGlassPane(adapter._srcElement.$container, session.uiSessionId)) {
      if (adapter.preventBubbleUp(event)) {
        event.stopPropagation(); // prevent superior adapters to handle this event
      }
      return; // do not return ' false' so that the event can bubble up and handled by a superior adapter
    }

    // Draw keystrokes on help keystroke.
    if (scout.keyStrokeUtils._isHelpKeyStroke(event) && adapter.drawKeyBox) {
      scout.keyStrokeUtils._drawKeyStrokes(adapter, event.target);
    }

    // Handle event by respective keystroke.
    scout.keyStrokeUtils._handleKeyStrokeEvent(adapter, event);
  };

  // Install the keystroke, either on the given $target, or on $entryPoint for global keystrokes.
  adapter._handler.$target = $target || session.$entryPoint;
  adapter._handler.$target.keydown(adapter._handler);
};

/**
 * Uninstalls the given keystroke adapter, meaning that its 'keydown-listener' is removed.
 */
scout.keyStrokeUtils.uninstallAdapter = function(adapter) {
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

/**
 * Returns whether the event describes a 'help keystroke'.
 */
scout.keyStrokeUtils._isHelpKeyStroke = function(event) {
  return event.which === scout.keys.F1;
};

/**
 * Visualizes the keystrokes provided by the given adapter.
 */
scout.keyStrokeUtils._drawKeyStrokes = function(adapter, target) {
  // Draws available keystrokes.
  adapter.drawKeyBox({});

  // Registers 'key-up' and 'window-blur' handler to remove drawn keystrokes.
  var keyUpHandler = function(event) {
    removeKeyBoxAndHandlers(scout.keyStrokeUtils._isHelpKeyStroke(event)); // only on 'key-up' for 'help keystroke'
  },
  windowBlurHandler = function(event) {
    removeKeyBoxAndHandlers(true);
  };

  $(target).on('keyup', keyUpHandler); // once the respective 'key-up' event occurs
  $(window).on('blur', windowBlurHandler); // once the current browser tab/window is left

  // ===== Helper functions ===== //

  function removeKeyBoxAndHandlers(doit) {
    if (doit) {
      $(target).off('keyup', keyUpHandler);
      $(window).off('blur', windowBlurHandler);
      adapter.removeKeyBox();
    }
  }
};

/**
 * Handles the keystroke event by the adapter's keystroke handlers, but returns immediately once a keystroke requests immediate stop of propagation.
 */
scout.keyStrokeUtils._handleKeyStrokeEvent = function(adapter, event) {
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
  }
};
