scout.KeyStrokeManager = function(session) {
  this.session = session;
  this.$entryPoint = session.$entryPoint;
  this._helpRendered = false;
  this._renderedKeys = [];

  // Prevent browser to interpret F1 (help).
  this.$entryPoint.keydown(swallowHelpKeyStroke.bind(this));
  this.$entryPoint.keyup(swallowHelpKeyStroke.bind(this));
  if ('onhelp' in window) {
    window.onhelp = scout.filters.returnFalse;
  }

  // Prevent browser to interpret backspace.
  this.$entryPoint.keydown(swallowBackspaceStroke.bind(this));
  this.$entryPoint.keyup(swallowBackspaceStroke.bind(this));

  // ===== Helper functions ===== //

  function swallowHelpKeyStroke(event) {
    return !this._isHelpKeyStroke(event);
  }

  function swallowBackspaceStroke(event) {
    return (event.which !== scout.keys.BACKSPACE);
  }
};

/**
 * Installs the given keystroke context. This method has no effect if the context is null, or already installed.
 */
scout.KeyStrokeManager.prototype.installKeyStrokeContext = function(keyStrokeContext) {
  if (!keyStrokeContext) {
    return;
  }

  if (keyStrokeContext._handler) {
    return; // context already installed
  }

  if (!keyStrokeContext.$getBindTarget()) {
    throw new Error('missing bind-target for KeyStrokeContext: ' + keyStrokeContext);
  }

  keyStrokeContext._handler = function(event) {
    if (this.session.focusManager.isElementCovertByGlassPane(keyStrokeContext.$getScopeTarget())) {
      return;
    }

    if (this._isHelpKeyStroke(event)) {
      if (event.originalEvent.renderingHelp || !this._helpRendered) {
        event.originalEvent.renderingHelp = true; // flag to let superior keyStrokeContexts render their help keys
        this._helpRendered = true; // flag to only render help once, if help key is held down
        this._installHelpDisposeListener();
        this._renderKeys(keyStrokeContext, event);
      }
    } else {
      this._handleKeyStrokeEvent(keyStrokeContext, event);
    }
  }.bind(this);

  keyStrokeContext._handler.$target = keyStrokeContext.$getBindTarget();
  keyStrokeContext._handler.$target.keydown(keyStrokeContext._handler);
  keyStrokeContext._handler.$target.keyup(keyStrokeContext._handler);
};

/**
 * Uninstalls the given keystroke context. This method has no effect if the context is null, or not installed.
 */
scout.KeyStrokeManager.prototype.uninstallKeyStrokeContext = function(keyStrokeContext) {
  if (!keyStrokeContext) {
    return;
  }
  if (!keyStrokeContext._handler) {
    return; // context not installed
  }

  keyStrokeContext._handler.$target.off('keydown', keyStrokeContext._handler);
  keyStrokeContext._handler.$target.off('up', keyStrokeContext._handler);
  keyStrokeContext._handler.$target = null;
  keyStrokeContext._handler = null;
};

/**
 * Visualizes the keys supported by the given keyStrokeContext.
 */
scout.KeyStrokeManager.prototype._renderKeys = function(keyStrokeContext, event) {
  var descendantContexts = event.originalEvent.keyStrokeContexts || [];

  keyStrokeContext.keyStrokes
    .filter(function(keyStroke) {
      var render = keyStroke.renderingHints.render;
      return (typeof render === 'function' ? render.call(keyStroke) : render);
    })
    .forEach(function(keyStroke) {
      var $drawingArea = (keyStroke.field ? keyStroke.field.$container : null) || keyStrokeContext.$getScopeTarget(); // Precedence: keystroke's field container, or the scope target otherwise.
      var keys = keyStroke.keys(); // Get all keys which are handled by the keystroke. Typically, this is a single key.

      keys.forEach(function(key) {
        var virtualKeyStrokeEvent = new scout.VirtualKeyStrokeEvent(key.which, key.ctrl, key.alt, key.shift, key.keyStrokeMode, event.target);

        if (keyStrokeContext.accept(virtualKeyStrokeEvent) &&
          keyStroke.accept(virtualKeyStrokeEvent) && !this._isPreventedByDescendantContext(key, event.target, descendantContexts)) {
          if (key.render($drawingArea, virtualKeyStrokeEvent)) {
            this._renderedKeys.push(key);
          }
        }
      }, this);
    }, this);

  descendantContexts.push(keyStrokeContext); // Register this keyStrokeContext within the event, so that superior keyStrokeContexts can validate their keys (e.g. not swallowed by a descendant keyStrokeContext).
  event.originalEvent.keyStrokeContexts = descendantContexts;
};

scout.KeyStrokeManager.prototype._isPreventedByDescendantContext = function(key, target, descendantContexts) {
  var virtualKeyStrokeEvent = new scout.VirtualKeyStrokeEvent(key.which, key.ctrl, key.alt, key.shift, key.keyStrokeMode, target);

  // Check whether any descendant keyStrokeContext prevents this keystroke from execution.
  return descendantContexts.some(function(descendantContext) {
    // Ask descendant keyStrokeContext whether this event is swallowed.
    descendantContext.accept(virtualKeyStrokeEvent);
    if (virtualKeyStrokeEvent.isAnyPropagationStopped()) {
      return true;
    }

    // Ask keystrokes of descendant keyStrokeContext whether this event is swallowed.
    return descendantContext.keyStrokes.some(function(descendantKeyStroke) {
      descendantKeyStroke.accept(virtualKeyStrokeEvent);
      return virtualKeyStrokeEvent.isAnyPropagationStopped();
    }, this);
  }, this);
};

/**
 * Handles the keystroke event by the keyStrokeContext's keystroke handlers, but returns immediately once a keystroke requests immediate stop of propagation.
 */
scout.KeyStrokeManager.prototype._handleKeyStrokeEvent = function(keyStrokeContext, event) {
  if (!keyStrokeContext.accept(event)) {
    return;
  }

  keyStrokeContext.keyStrokes.some(function(keyStroke) {
    if (!keyStroke.accept(event)) {
      return false;
    }

    // Before handling the keystroke, accept the input of a potential active value field.
    if (!keyStroke.preventInvokeAcceptInputOnActiveValueField && (keyStroke.invokeAcceptInputOnActiveValueField || keyStrokeContext.invokeAcceptInputOnActiveValueField)) {
      scout.ValueField.invokeValueFieldAcceptInput();
    }

    // Handle the keystroke.
    keyStroke.handle(event);

    // Break on 'stopImmediate'.
    return event.isImmediatePropagationStopped(); // 'some-loop' breaks on true.
  });
};

scout.KeyStrokeManager.prototype._isHelpKeyStroke = function(event) {
  return event.which === scout.keys.F1;
};

scout.KeyStrokeManager.prototype._installHelpDisposeListener = function() {
  var helpDisposeListener = function() {
    this.$entryPoint.off('keyup', helpDisposeListener);
    $(window).off('blur', helpDisposeListener);
    this._helpRendered = false;

    this._renderedKeys.forEach(function(key) {
      key.remove();
    });
    this._renderedKeys = [];
  }.bind(this);

  this.$entryPoint.on('keyup', helpDisposeListener);
  $(window).on('blur', helpDisposeListener); // once the current browser tab/window is left

  return false;
};
