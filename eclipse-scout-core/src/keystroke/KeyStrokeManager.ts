/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, arrays, EventEmitter, filters, InitModelOf, Key, keys, KeyStroke, KeyStrokeContext, KeyStrokeManagerEventMap, KeyStrokeModel, ObjectModel, Session, SomeRequired, ValueField, VirtualKeyStrokeEvent} from '../index';
import $ from 'jquery';

export class KeyStrokeManager extends EventEmitter implements KeyStrokeManagerModel {
  declare model: KeyStrokeManagerModel;
  declare initModel: SomeRequired<this['model'], 'session'>;
  declare eventMap: KeyStrokeManagerEventMap;
  declare self: KeyStrokeManager;

  session: Session;
  helpKeyStroke: KeyStrokeModel;
  swallowF1: boolean;
  filters: ({ filter(keyStroke: KeyStroke): boolean })[];
  protected _helpRendered: boolean;
  protected _renderedKeys: Key[];

  constructor() {
    super();
    this.session = null;
    this.helpKeyStroke = KeyStroke.parseKeyStroke('F1');
    this.swallowF1 = true;
    this._helpRendered = false;
    this._renderedKeys = [];
    this.filters = [];
  }

  init(model: InitModelOf<this>) {
    this.session = model.session;
    this.installTopLevelKeyStrokeHandlers(this.session.$entryPoint);
  }

  installTopLevelKeyStrokeHandlers($container: JQuery) {
    let
      myWindow = $container.window(true),
      // Swallow F1 (online help) keystroke
      helpHandler = event => event.which !== keys.F1,
      // Swallow Backspace (browser navigation) keystroke
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
      myWindow['onhelp'] = filters.returnFalse;
    }
  }

  /**
   * Installs the given keystroke context. This method has no effect if the context is null, or already installed.
   */
  installKeyStrokeContext(keyStrokeContext: KeyStrokeContext) {
    if (!keyStrokeContext) {
      return;
    }

    let handler = keyStrokeContext._handler;
    if (handler) {
      return; // context already installed
    }

    if (!keyStrokeContext.$getBindTarget()) {
      throw new Error('missing bind-target for KeyStrokeContext: ' + keyStrokeContext);
    }

    handler = this._onKeyEvent.bind(this, keyStrokeContext);
    handler.$target = keyStrokeContext.$getBindTarget();
    handler.$target.on('keydown', handler);
    handler.$target.on('keyup', handler);

    keyStrokeContext._handler = handler;
  }

  /**
   * Uninstalls the given keystroke context. This method has no effect if the context is null, or not installed.
   */
  uninstallKeyStrokeContext(keyStrokeContext: KeyStrokeContext) {
    if (!keyStrokeContext) {
      return;
    }
    let handler = keyStrokeContext._handler;
    if (!handler) {
      return; // context not installed
    }

    handler.$target.off('keydown', handler);
    handler.$target.off('keyup', handler);
    handler.$target = null;

    keyStrokeContext._handler = null;
  }

  /**
   * Visualizes the keys supported by the given keyStrokeContext.
   */
  protected _renderKeys(keyStrokeContext: KeyStrokeContext, event: KeyboardEventWithMetaData) {
    let descendantContexts = event.originalEvent.keyStrokeContexts || [];
    let immediatePropagationStoppedKeys = [];

    keyStrokeContext.keyStrokes
      .filter(keyStroke => {
        let render = keyStroke.renderingHints.render;
        return (typeof render === 'function' ? render.call(keyStroke) : render);
      })
      .forEach(keyStroke => {
        keyStroke.enabledByFilter = this._filter(keyStroke);
        let $drawingArea = (keyStroke.field ? keyStroke.field.$container : null) || keyStrokeContext.$getScopeTarget(); // Precedence: keystroke's field container, or the scope target otherwise.
        let keys = keyStroke.keys(); // Get all keys which are handled by the keystroke. Typically, this is a single key.
        keys.forEach(key => {
          let virtualKeyStrokeEvent = new VirtualKeyStrokeEvent(key.which, key.ctrl, key.alt, key.shift, key.keyStrokeMode, event);

          if (immediatePropagationStoppedKeys.indexOf(key.toKeyStrokeString()) < 0 && keyStrokeContext.accept(virtualKeyStrokeEvent) &&
            keyStroke.accept(virtualKeyStrokeEvent) && !this._isPreventedByDescendantContext(key, event, descendantContexts)) {
            if (key.render($drawingArea, virtualKeyStrokeEvent)) {
              this._renderedKeys.push(key);
            }
            // If immediate propagation is stopped, keystrokes on the same level which react to the same key won't be executed -> make sure they won't be displayed either
            if (virtualKeyStrokeEvent.isImmediatePropagationStopped()) {
              immediatePropagationStoppedKeys.push(key.toKeyStrokeString());
            }
          }
        });
      });

    descendantContexts.push(keyStrokeContext); // Register this keyStrokeContext within the event, so that superior keyStrokeContexts can validate their keys (e.g. not swallowed by a descendant keyStrokeContext).
    event.originalEvent.keyStrokeContexts = descendantContexts;
  }

  protected _isPreventedByDescendantContext(key: Key, event: KeyboardEventWithMetaData, descendantContexts: KeyStrokeContext[]): boolean {
    let virtualKeyStrokeEvent = new VirtualKeyStrokeEvent(key.which, key.ctrl, key.alt, key.shift, key.keyStrokeMode, event);

    // Check whether any descendant keyStrokeContext prevents this keystroke from execution.
    return descendantContexts.some(descendantContext => {
      // Ask descendant keyStrokeContext whether this event is swallowed.
      descendantContext.accept(virtualKeyStrokeEvent);
      if (virtualKeyStrokeEvent.isAnyPropagationStopped()) {
        return true;
      }

      // Ask keystrokes of descendant keyStrokeContext whether this event is swallowed.
      return descendantContext.keyStrokes.some(descendantKeyStroke => {
        descendantKeyStroke.accept(virtualKeyStrokeEvent);
        return virtualKeyStrokeEvent.isAnyPropagationStopped();
      });
    });
  }

  /**
   * Handles the keystroke event by the keyStrokeContext's keystroke handlers, but returns immediately once a keystroke requests immediate stop of propagation.
   */
  protected _handleKeyStrokeEvent(keyStrokeContext: KeyStrokeContext, event: JQuery.KeyboardEventBase) {
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
    // of a keystroke, all its keystrokes on the context are deleted. Which means no keystroke is processed
    // anymore. However: creating a copy can be dangerous too, because the handle function must deal with
    // the situation that the widget to which the keystroke belongs, is suddenly destroyed.
    let keyStrokesCopy = keyStrokeContext.keyStrokes.slice();
    keyStrokesCopy.some(keyStroke => {
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
    });
  }

  protected _filter(keyStroke: KeyStroke): boolean {
    for (let i = 0; i < this.filters.length; i++) {
      if (!this.filters[i].filter(keyStroke)) {
        return false;
      }
    }
    return true;
  }

  invokeAcceptInputOnActiveValueField(keyStroke: KeyStroke, keyStrokeContext: KeyStrokeContext): boolean {
    return !keyStroke.preventInvokeAcceptInputOnActiveValueField && (keyStroke.invokeAcceptInputOnActiveValueField || keyStrokeContext.invokeAcceptInputOnActiveValueField);
  }

  protected _isHelpKeyStroke(event: JQuery.KeyboardEventBase): boolean {
    return KeyStroke.acceptEvent(this.helpKeyStroke, event);
  }

  protected _installHelpDisposeListener(event: JQuery.KeyboardEventBase): boolean {
    let helpDisposeHandler,
      $currentTarget = $(event.currentTarget),
      $myWindow = $currentTarget.window(),
      $topLevelContainer = $currentTarget.entryPoint();

    helpDisposeHandler = function() {
      $topLevelContainer.off('keyup', helpDisposeHandler);
      $myWindow.off('blur', helpDisposeHandler);
      this._helpRendered = false;
      this._renderedKeys.forEach(key => key.remove());
      this._renderedKeys = [];
    }.bind(this);

    $topLevelContainer.on('keyup', helpDisposeHandler);
    $myWindow.on('blur', helpDisposeHandler); // once the current browser tab/window is left

    return false;
  }

  protected _onKeyEvent(keyStrokeContext: KeyStrokeContext, event: KeyboardEventWithMetaData): boolean {
    // check if scopeTarget is covered by glass pane
    if (this.session.focusManager.isElementCovertByGlassPane(keyStrokeContext.$getScopeTarget())) {
      // check if any action with 'keyStrokeFirePolicy=IAction.KeyStrokeFirePolicy.ALWAYS' is in keyStrokeContext
      let keyStrokeFirePolicyAlways = $.grep(keyStrokeContext.keyStrokes, k => k.keyStrokeFirePolicy === Action.KeyStrokeFirePolicy.ALWAYS); // (will at least return an empty array)
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

  addFilter(filter: { filter(keyStroke: KeyStroke): boolean }) {
    arrays.pushSet(this.filters, filter);
  }

  removeFilter(filter: { filter(keyStroke: KeyStroke): boolean }) {
    arrays.remove(this.filters, filter);
  }
}

export interface KeyStrokeManagerModel extends ObjectModel<KeyStrokeManager> {
  session?: Session;
}

export interface KeyboardEventWithMetaData extends JQuery.KeyboardEventBase {
  originalEvent?: KeyboardEvent & { renderingHelp?: boolean; keyStrokeContexts?: KeyStrokeContext[] };
}
