/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.KeyStrokeManager = function(session) {
  var $mainEntryPoint = session.$entryPoint;
  this.session = session;
  this._helpRendered = false;
  this._renderedKeys = [];

  this.installTopLevelKeyStrokeHandlers($mainEntryPoint);
};

scout.KeyStrokeManager.prototype.installTopLevelKeyStrokeHandlers = function($container) {
  var
    myWindow = $container.window(true),
    // Swallow F1 (online help) key stroke
    helpHandler = function(event) {
      return !this._isHelpKeyStroke(event);
    }.bind(this),
    // Swallow Backspace (browser navigation) key stroke
    backspaceHandler = function(event) {
      return event.which !== scout.keys.BACKSPACE;
    }.bind(this);

  $container
    .keydown(helpHandler)
    .keyup(helpHandler);
  $container
    .keydown(backspaceHandler)
    .keyup(backspaceHandler);

  if ('onhelp' in myWindow) {
    myWindow.onhelp = scout.filters.returnFalse;
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

  keyStrokeContext._handler = this._onKeyEvent.bind(this, keyStrokeContext);
  keyStrokeContext._handler.$target = keyStrokeContext.$getBindTarget();
  keyStrokeContext._handler.$target.on('keydown', keyStrokeContext._handler);
  keyStrokeContext._handler.$target.on('keyup', keyStrokeContext._handler);
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
  keyStrokeContext._handler.$target.off('keyup', keyStrokeContext._handler);
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

  // We create a copy of the keyStrokes array, because when a widget is disposed in the handle function
  // of a keystroke, all its keystrokes on the context are deleted. Which means no key stroke is processed
  // anymore. However: creating a copy can be dangerous too, because the handle function must deal with
  // the situation that the widget to which the keystroke belongs, is suddenly destroyed.
  var keyStrokesCopy = keyStrokeContext.keyStrokes.slice();
  keyStrokesCopy.some(function(keyStroke) {
    // Handle numpad keystroke
    event.which = event.which >= 96 && event.which <= 105 ? event.which - 48 : event.which;
    if (!keyStroke.accept(event)) {
      return false;
    }

    // Before handling the keystroke, accept the input of a potential active value field
    if (!keyStroke.preventInvokeAcceptInputOnActiveValueField && (keyStroke.invokeAcceptInputOnActiveValueField || keyStrokeContext.invokeAcceptInputOnActiveValueField)) {
      scout.ValueField.invokeValueFieldAcceptInput(event.target);
    }

    // Handle the keystroke
    keyStroke.invokeHandle(event);

    // Break on 'stopImmediate'.
    return event.isImmediatePropagationStopped(); // 'some-loop' breaks on true.
  }, this);
};

scout.KeyStrokeManager.prototype._isHelpKeyStroke = function(event) {
  return event.which === scout.keys.F1;
};

scout.KeyStrokeManager.prototype._installHelpDisposeListener = function(event) {
  var helpDisposeHandler,
    $currentTarget = $(event.currentTarget),
    $myWindow = $currentTarget.window(),
    $topLevelContainer = $currentTarget.entryPoint();

  helpDisposeHandler = function() {
    $topLevelContainer.off('keyup', helpDisposeHandler);
    $myWindow.off('blur', helpDisposeHandler);
    this._helpRendered = false;
    this._renderedKeys.forEach(function(key) {
      key.remove();
    });
    this._renderedKeys = [];
  }.bind(this);

  $topLevelContainer.on('keyup', helpDisposeHandler);
  $myWindow.on('blur', helpDisposeHandler); // once the current browser tab/window is left

  return false;
};

scout.KeyStrokeManager.prototype._onKeyEvent = function(keyStrokeContext, event) {
  // check if scopeTarget is covered by glass pane
  if (this.session.focusManager.isElementCovertByGlassPane(keyStrokeContext.$getScopeTarget())) {
    // check if any action with 'keyStrokeFirePolicy=IAction.KEYSTROKE_FIRE_POLICY_ALWAYS' is in keyStrokeContext
    var keyStrokeFirePolicyAlways = $.grep(keyStrokeContext.keyStrokes, function(k) { // (will at least return an empty array)
      return k.field && k.field.keyStrokeFirePolicy === scout.Action.KEYSTROKE_FIRE_POLICY_ALWAYS;
    });
    if (keyStrokeFirePolicyAlways.length === 0) {
      return;
    }
    // copy current keyStrokeContext and replace keyStrokes with filtered array 'keyStrokeFirePolicyAlways'
    keyStrokeContext = keyStrokeContext.clone();
    keyStrokeContext.keyStrokes = keyStrokeFirePolicyAlways;
  }

  if (this._isHelpKeyStroke(event)) {
    if (event.originalEvent.renderingHelp || !this._helpRendered) {
      event.originalEvent.renderingHelp = true; // flag to let superior keyStrokeContexts render their help keys
      this._helpRendered = true; // flag to only render help once, if help key is held down
      this._installHelpDisposeListener(event);
      this._renderKeys(keyStrokeContext, event);
    }
  } else {
    this._handleKeyStrokeEvent(keyStrokeContext, event);
  }
};
