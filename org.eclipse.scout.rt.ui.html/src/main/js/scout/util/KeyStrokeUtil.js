/**
 * Utility to install keystroke handling on DOM elements in the form of 'scout.AbstractKeyStrokeAdapter's.
 */
scout.KeyStrokeUtil = function() {
};

/**
 * Initializes keystroke handing for the given session.
 */
scout.KeyStrokeUtil.prototype.init = function($entryPoint) {
  // Prevent browser to interpret F1 (help).
  if (window.onhelp) {
    window.onhelp = scout.Filters.returnFalse;
  }
  var swallowHelpKeyStroke = function(event) {
    return !this._isHelpKeyStroke(event);
  }.bind(this);
  $entryPoint.keydown(swallowHelpKeyStroke);
  $entryPoint.keyup(swallowHelpKeyStroke);

  // Prevent browser to interpret backspace.
  var swallowBackspaceStroke = function(event) {
    return (event.which !== scout.keys.BACKSPACE);
  };
  $entryPoint.keydown(swallowBackspaceStroke);
  $entryPoint.keyup(swallowBackspaceStroke);
};

/**
 * Installs the given keystroke adapter. This method has no effect if the adapter is null, or already installed.
 *
 * @param adapter
 *        the adapter to be installed
 * @param $target
 *        indicates on which target element to listen for keystroke events, or null to listen globally on $entryPoint
 */
scout.KeyStrokeUtil.prototype.installAdapter = function(session, adapter, $target) {
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
    if (this._isHelpKeyStroke(event) && adapter.drawKeyBox) {
      this._drawKeyStrokes(adapter, event.target);
    }

    // Handle event by respective keystroke.
    this._handleKeyStrokeEvent(adapter, event);
  }.bind(this);

  // Install the keystroke, either on the given $target, or on $entryPoint for global keystrokes.
  adapter._handler.$target = $target || session.$entryPoint;
  adapter._handler.$target.keydown(adapter._handler);
};

scout.KeyStrokeUtil.prototype.uninstallAdapter = function(adapter) {
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

scout.KeyStrokeUtil.prototype._isHelpKeyStroke = function(event) {
  return event.which === scout.keys.F1;
};

scout.KeyStrokeUtil.prototype._drawKeyStrokes = function(adapter, target) {
  // Draws available keystrokes.
  adapter.drawKeyBox({});

  // Registers 'key-up' and 'window-blur' handler to remove drawn keystrokes.
  var keyUpHandler = function(event) {
    removeKeyBoxAndHandlers(this._isHelpKeyStroke(event)); // only on 'key-up' for 'help keystroke'
  }.bind(this);
  var windowBlurHandler = function(event) {
    removeKeyBoxAndHandlers(true);
  }.bind(this);

  $(target).on('keyup', keyUpHandler); // once the respective 'key-up' event occurs
  $(window).on('blur', windowBlurHandler); // once the current browser tab/window is left

  // ----- Helper functions -----

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
scout.KeyStrokeUtil.prototype._handleKeyStrokeEvent = function(adapter, event) {
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

//Singleton
scout.keyStrokeUtil = new scout.KeyStrokeUtil();
