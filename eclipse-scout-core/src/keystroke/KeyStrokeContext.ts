/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, arrays, EventHandler, KeyboardEventWithMetaData, KeyStroke, KeyStrokeContextOptions, keyStrokeModifier, scout, ScoutKeyboardEvent} from '../index';
import $ from 'jquery';

/**
 * Contains a list of {@link KeyStroke}s that can be activated by pressing the corresponding keys if the focus is inside the {@link $bindTarget}.
 */
export class KeyStrokeContext implements KeyStrokeContextOptions {
  invokeAcceptInputOnActiveValueField: boolean;
  keyStrokes: KeyStroke[];
  stopPropagationInterceptors: ((event: ScoutKeyboardEvent) => void)[];
  $bindTarget: JQuery | (() => JQuery);
  $scopeTarget: JQuery | (() => JQuery);

  /** @internal */
  _handler: ((event: KeyboardEventWithMetaData) => boolean) & { $target?: JQuery };

  /**
   * Arrays with combinations of keys to prevent from bubbling up in the DOM tree.
   */
  protected _stopPropagationKeys: Record<number, number[]>;

  constructor(options?: KeyStrokeContextOptions) {
    this.$bindTarget = null;
    this.$scopeTarget = null;
    this.keyStrokes = [];
    this.stopPropagationInterceptors = [];
    this._stopPropagationKeys = {};
    this.invokeAcceptInputOnActiveValueField = false;

    options = options || {} as KeyStrokeContextOptions;
    $.extend(this, options);
  }

  /**
   * Registers the given keys as 'stopPropagation' keys, meaning that any keystroke event with that key and matching the modifier bit mask is prevented from bubbling the DOM tree up.
   *
   * @param modifierBitMask bitwise OR modifier constants to match a keystroke event. (KeyStrokeModifier.js)
   * @param keys the keys to match a keystroke event.
   */
  registerStopPropagationKeys(modifierBitMask: number, keys: number[]) {
    this._stopPropagationKeys[modifierBitMask] = this._stopPropagationKeys[modifierBitMask] || [];
    arrays.pushAll(this._stopPropagationKeys[modifierBitMask], keys);
  }

  /**
   * Unregisters the given keys as 'stopPropagation' keys.
   *
   * @param modifierBitMask bitwise OR modifier constants to match a keystroke event. (KeyStrokeModifier.js)
   * @param keys the keys to match a keystroke event.
   */
  unregisterStopPropagationKeys(modifierBitMask: number, keys: number[]) {
    if (!this._stopPropagationKeys[modifierBitMask]) {
      return;
    }
    arrays.removeAll(this._stopPropagationKeys[modifierBitMask], keys);
  }

  /**
   * Use this method to register an interceptor to set propagation flags on context level.
   */
  registerStopPropagationInterceptor(interceptor: (event: ScoutKeyboardEvent) => void) {
    this.stopPropagationInterceptors.push(interceptor);
  }

  /**
   * Returns true if this event is handled by this context, and if so sets the propagation flags accordingly.
   */
  accept(event: ScoutKeyboardEvent): boolean {
    // Check whether this event is accepted.
    if (!this._accept(event)) {
      return false;
    }

    // Apply propagation flags to the event.
    this._applyPropagationFlags(event);

    return true;
  }

  /**
   * Sets the propagation flags to the given event.
   */
  protected _applyPropagationFlags(event: ScoutKeyboardEvent) {
    let modifierBitMask = keyStrokeModifier.toModifierBitMask(event);
    let keys = this._stopPropagationKeys[modifierBitMask];

    if (keys && scout.isOneOf(event.which, keys)) {
      event.stopPropagation();
    }

    // Let registered interceptors participate.
    this.stopPropagationInterceptors.forEach(interceptor => interceptor(event));
  }

  protected _accept(event: ScoutKeyboardEvent): boolean {
    return true;
  }

  registerKeyStroke(keyStroke: KeyStroke | Action) {
    this.registerKeyStrokes(keyStroke);
  }

  /**
   * Registers the given keystroke(s) if not installed yet.
   */
  registerKeyStrokes(keyStrokes: KeyStroke | KeyStroke[] | Action | Action[]) {
    arrays.ensure(keyStrokes)
      .map(this._resolveKeyStroke, this)
      .filter(ks => this.keyStrokes.indexOf(ks) === -1) // must not be registered yet
      .forEach(keystroke => {
        this.keyStrokes.push(keystroke);
        let ks: KeyStroke & { destroyListener?: EventHandler };
        ks = keystroke;
        // Registers a destroy listener, so that the keystroke is uninstalled once its field is destroyed.
        if (ks.field && !ks.destroyListener) {
          ks.destroyListener = event => {
            this.unregisterKeyStroke(ks);
            ks.destroyListener = null;
          };
          ks.field.one('destroy', ks.destroyListener);
        }
      });
  }

  /**
   * Uninstalls the given keystroke. Has no effect if not installed.
   */
  unregisterKeyStroke(keyStroke: KeyStroke | Action) {
    this.unregisterKeyStrokes(keyStroke);
  }

  unregisterKeyStrokes(keyStrokes: KeyStroke | KeyStroke[] | Action | Action[]) {
    arrays.ensure(keyStrokes)
      .map(this._resolveKeyStroke, this)
      .forEach(keystroke => {
        let ks: KeyStroke & { destroyListener?: EventHandler };
        ks = keystroke;
        if (arrays.remove(this.keyStrokes, ks) && ks.field && ks.destroyListener) {
          ks.field.off('destroy', ks.destroyListener);
          ks.destroyListener = null;
        }
      });
  }

  protected _resolveKeyStroke(keyStroke: KeyStroke | Action): KeyStroke {
    if (keyStroke instanceof KeyStroke) {
      return keyStroke;
    }
    if (keyStroke instanceof Action) {
      return keyStroke.actionKeyStroke;
    }
    throw new Error('unsupported keystroke: ' + keyStroke);
  }

  /**
   * Returns the $target where to bind this context as keydown listener.
   */
  $getBindTarget(): JQuery {
    return (typeof this.$bindTarget === 'function' ? this.$bindTarget() : this.$bindTarget);
  }

  /**
   * Returns the scope of this context and is used to determine the context's accessibility, meaning not covert by a glasspane.
   */
  $getScopeTarget(): JQuery {
    return (typeof this.$scopeTarget === 'function' ? this.$scopeTarget() : this.$scopeTarget);
  }

  clone(): KeyStrokeContext {
    return new KeyStrokeContext($.extend({}, this));
  }
}
