/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, arrays, EventSupport, filters as filters_1, keys, KeyStroke, ValueField, VirtualKeyStrokeEvent} from '../index';
import $ from 'jquery';

export default class KeyStrokeManager {

  constructor() {
    this.session = null;
    this.helpKeyStroke = KeyStroke.parseKeyStroke('F1');
    this.swallowF1 = true;
    this._helpRendered = false;
    this._renderedKeys = [];
    this.events = this._createEventSupport();
    this.filters = [];
  }

  init(model) {
    this.session = model.session;
    this.installTopLevelKeyStrokeHandlers(this.session.$entryPoint);
  }

  installTopLevelKeyStrokeHandlers($container) {
    let
      myWindow = $container.window(true),
      // Swallow F1 (online help) key stroke
      helpHandler = event => event.which !== keys.F1,
      // Swallow Backspace (browser navigation) key stroke
      backspaceHandler = event => event.which !== keys.BACKSPACE;

    if (this.swallowF1) {
      $container
        .keydown(helpHandler)
        .keyup(helpHandler);
    }
    $container
      .keydown(backspaceHandler)
      .keyup(backspaceHandler);

    if ('onhelp' in myWindow) {
      myWindow.onhelp = filters_1.returnFalse;
    }
  }

  /**
   * Installs the given keystroke context. This method has no effect if the context is null, or already installed.
   */
  installKeyStrokeContext(keyStrokeContext) {
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
  }

  /**
   * Uninstalls the given keystroke context. This method has no effect if the context is null, or not installed.
   */
  uninstallKeyStrokeContext(keyStrokeContext) {
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
  }

  /**
   * Visualizes the keys supported by the given keyStrokeContext.
   */
  _renderKeys(keyStrokeContext, event) {
    let descendantContexts = event.originalEvent.keyStrokeContexts || [];
    let immediatePropagationStoppedKeys = [];

    keyStrokeContext.keyStrokes
      .filter(keyStroke => {
        let render = keyStroke.renderingHints.render;
        return (typeof render === 'function' ? render.call(keyStroke) : render);
      })
      .forEach(function(keyStroke) {
        keyStroke.enabledByFilter = this._filter(keyStroke);
        let $drawingArea = (keyStroke.field ? keyStroke.field.$container : null) || keyStrokeContext.$getScopeTarget(); // Precedence: keystroke's field container, or the scope target otherwise.
        let keys = keyStroke.keys(); // Get all keys which are handled by the keystroke. Typically, this is a single key.
        keys.forEach(function(key) {
          let virtualKeyStrokeEvent = new VirtualKeyStrokeEvent(key.which, key.ctrl, key.alt, key.shift, key.keyStrokeMode, event.target);

          if (immediatePropagationStoppedKeys.indexOf(key.toKeyStrokeString()) < 0 && keyStrokeContext.accept(virtualKeyStrokeEvent) &&
            keyStroke.accept(virtualKeyStrokeEvent) && !this._isPreventedByDescendantContext(key, event.target, descendantContexts)) {
            if (key.render($drawingArea, virtualKeyStrokeEvent)) {
              this._renderedKeys.push(key);
            }
            // If immediate propagation is stopped, key strokes on the same level which react to the same key won't be executed -> make sure they won't be displayed either
            if (virtualKeyStrokeEvent.isImmediatePropagationStopped()) {
              immediatePropagationStoppedKeys.push(key.toKeyStrokeString());
            }
          }
        }, this);
      }, this);

    descendantContexts.push(keyStrokeContext); // Register this keyStrokeContext within the event, so that superior keyStrokeContexts can validate their keys (e.g. not swallowed by a descendant keyStrokeContext).
    event.originalEvent.keyStrokeContexts = descendantContexts;
  }

  _isPreventedByDescendantContext(key, target, descendantContexts) {
    let virtualKeyStrokeEvent = new VirtualKeyStrokeEvent(key.which, key.ctrl, key.alt, key.shift, key.keyStrokeMode, target);

    // Check whether any descendant keyStrokeContext prevents this keystroke from execution.
    return descendantContexts.some(function(descendantContext) {
      // Ask descendant keyStrokeContext whether this event is swallowed.
      descendantContext.accept(virtualKeyStrokeEvent);
      if (virtualKeyStrokeEvent.isAnyPropagationStopped()) {
        return true;
      }

      // Ask keystrokes of descendant keyStrokeContext whether this event is swallowed.
      return descendantContext.keyStrokes.some(descendantKeyStroke => {
        descendantKeyStroke.accept(virtualKeyStrokeEvent);
        return virtualKeyStrokeEvent.isAnyPropagationStopped();
      }, this);
    }, this);
  }

  /**
   * Handles the keystroke event by the keyStrokeContext's keystroke handlers, but returns immediately once a keystroke requests immediate stop of propagation.
   */
  _handleKeyStrokeEvent(keyStrokeContext, event) {
    if (!keyStrokeContext.accept(event)) {
      return;
    }

    if (keyStrokeContext.keyStrokes.length < 1) {
      return;
    }

    // Handle numpad keystroke
    if (event.which >= 96 && event.which <= 105) {
      event.which = event.which - 48;
    }

    // We create a copy of the keyStrokes array, because when a widget is disposed in the handle function
    // of a keystroke, all its keystrokes on the context are deleted. Which means no key stroke is processed
    // anymore. However: creating a copy can be dangerous too, because the handle function must deal with
    // the situation that the widget to which the keystroke belongs, is suddenly destroyed.
    let keyStrokesCopy = keyStrokeContext.keyStrokes.slice();
    keyStrokesCopy.some(function(keyStroke) {
      if (!keyStroke.accept(event)) {
        return false;
      }

      // Before handling the keystroke, accept the input of a potential active value field
      if (this.invokeAcceptInputOnActiveValueField(keyStroke, keyStrokeContext)) {
        ValueField.invokeValueFieldAcceptInput(event.target);
      }

      if (!this._filter(keyStroke)) {
        return true; // 'some-loop' breaks on true
      }

      this.trigger('keyStroke', {
        keyStroke: keyStroke,
        keyStrokeContext: keyStrokeContext
      });

      // Handle the keystroke
      keyStroke.invokeHandle(event);

      // Break on 'stopImmediate'.
      return event.isImmediatePropagationStopped(); // 'some-loop' breaks on true
    }, this);
  }

  _filter(keyStroke) {
    for (let i = 0; i < this.filters.length; i++) {
      if (!this.filters[i].filter(keyStroke)) {
        return false;
      }
    }
    return true;
  }

  invokeAcceptInputOnActiveValueField(keyStroke, keyStrokeContext) {
    return !keyStroke.preventInvokeAcceptInputOnActiveValueField && (keyStroke.invokeAcceptInputOnActiveValueField || keyStrokeContext.invokeAcceptInputOnActiveValueField);
  }

  _isHelpKeyStroke(event) {
    return KeyStroke.acceptEvent(this.helpKeyStroke, event);
  }

  _installHelpDisposeListener(event) {
    let helpDisposeHandler,
      $currentTarget = $(event.currentTarget),
      $myWindow = $currentTarget.window(),
      $topLevelContainer = $currentTarget.entryPoint();

    helpDisposeHandler = function() {
      $topLevelContainer.off('keyup', helpDisposeHandler);
      $myWindow.off('blur', helpDisposeHandler);
      this._helpRendered = false;
      this._renderedKeys.forEach(key => {
        key.remove();
      });
      this._renderedKeys = [];
    }.bind(this);

    $topLevelContainer.on('keyup', helpDisposeHandler);
    $myWindow.on('blur', helpDisposeHandler); // once the current browser tab/window is left

    return false;
  }

  _onKeyEvent(keyStrokeContext, event) {
    // check if scopeTarget is covered by glass pane
    if (this.session.focusManager.isElementCovertByGlassPane(keyStrokeContext.$getScopeTarget())) {
      // check if any action with 'keyStrokeFirePolicy=IAction.KeyStrokeFirePolicy.ALWAYS' is in keyStrokeContext
      let keyStrokeFirePolicyAlways = $.grep(keyStrokeContext.keyStrokes, k => { // (will at least return an empty array)
        return k.keyStrokeFirePolicy === Action.KeyStrokeFirePolicy.ALWAYS;
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
  }

  addFilter(filter) {
    arrays.pushSet(this.filters, filter);
  }

  removeFilter(filter) {
    arrays.remove(this.filters, filter);
  }

  // --- Event handling methods ---
  _createEventSupport() {
    return new EventSupport();
  }

  trigger(type, event) {
    event = event || {};
    event.source = this;
    this.events.trigger(type, event);
  }

  one(type, func) {
    this.events.one(type, func);
  }

  on(type, func) {
    return this.events.on(type, func);
  }

  off(type, func) {
    this.events.off(type, func);
  }

  addListener(listener) {
    this.events.addListener(listener);
  }

  removeListener(listener) {
    this.events.removeListener(listener);
  }
}
