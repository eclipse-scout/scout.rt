/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, arrays, KeyStroke, keyStrokeModifier, objects, scout} from '../index';
import $ from 'jquery';

export default class KeyStrokeContext {

  constructor(options) {
    /*
     * Holds the target where to bind this context as keydown listener.
     * This can either be a static value or a function to resolve the target.
     */
    this.$bindTarget = null;
    /*
     * Holds the scope of this context and is used to determine the context's accessibility, meaning not covert by a glasspane.
     * This can either be a static value or a function to resolve the target.
     */
    this.$scopeTarget = null;
    /*
     * Holds the keystrokes registered within this context.
     */
    this.keyStrokes = [];
    /*
     * Array of interceptors to participate in setting 'stop propagation' flags.
     */
    this.stopPropagationInterceptors = [];

    /*
     * Arrays with combinations of keys to prevent from bubbling up in the DOM tree.
     */
    this._stopPropagationKeys = {};

    /*
     * Indicates whether to invoke 'acceptInput' on a currently focused value field prior handling the keystroke.
     */
    this.invokeAcceptInputOnActiveValueField = false;

    options = options || {};
    $.extend(this, options);
  }

  /**
   * Registers the given keys as 'stopPropagation' keys, meaning that any keystroke event with that key and matching the modifier bit mask is prevented from bubbling the DOM tree up.
   *
   * @param {number} modifierBitMask bitwise OR'ing together modifier constants to match a keystroke event. (KeyStrokeModifier.js)
   * @param {[number]} keys the keys to match a keystroke event.
   */
  registerStopPropagationKeys(modifierBitMask, keys) {
    this._stopPropagationKeys[modifierBitMask] = this._stopPropagationKeys[modifierBitMask] || [];
    arrays.pushAll(this._stopPropagationKeys[modifierBitMask], keys);
  }

  /**
   * Unregisters the given keys as 'stopPropagation' keys.
   *
   * @param {number} modifierBitMask bitwise OR'ing together modifier constants to match a keystroke event. (KeyStrokeModifier.js)
   * @param {[number]} keys the keys to match a keystroke event.
   */
  unregisterStopPropagationKeys(modifierBitMask, keys) {
    if (!this._stopPropagationKeys[modifierBitMask]) {
      return;
    }
    arrays.removeAll(this._stopPropagationKeys[modifierBitMask], keys);
  }

  /**
   * Use this method to register an interceptor to set propagation flags on context level.
   */
  registerStopPropagationInterceptor(interceptor) {
    this.stopPropagationInterceptors.push(interceptor);
  }

  /**
   * Returns true if this event is handled by this context, and if so sets the propagation flags accordingly.
   */
  accept(event) {
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
  _applyPropagationFlags(event) {
    let modifierBitMask = keyStrokeModifier.toModifierBitMask(event);
    let keys = this._stopPropagationKeys[modifierBitMask];

    if (keys && scout.isOneOf(event.which, keys)) {
      event.stopPropagation();
    }

    // Let registered interceptors participate.
    this.stopPropagationInterceptors.forEach(interceptor => {
      interceptor(event);
    }, this);
  }

  _accept(event) {
    return true;
  }

  registerKeyStroke(keyStroke) {
    this.registerKeyStrokes(keyStroke);
  }

  /**
   * Registers the given keystroke(s) if not installed yet.
   */
  registerKeyStrokes(keyStrokes) {
    arrays.ensure(keyStrokes)
      .map(this._resolveKeyStroke, this)
      .filter(function(ks) {
        return this.keyStrokes.indexOf(ks) === -1; // must not be registered yet
      }, this)
      .forEach(function(ks) {
        this.keyStrokes.push(ks);

        // Registers a destroy listener, so that the keystroke is uninstalled once its field is destroyed.
        if (ks.field && !ks.destroyListener) {
          ks.destroyListener = function(event) {
            this.unregisterKeyStroke(ks);
            ks.destroyListener = null;
          }.bind(this);
          ks.field.one('destroy', ks.destroyListener);
        }
      }, this);
  }

  /**
   * Uninstalls the given keystroke. Has no effect if not installed.
   */
  unregisterKeyStroke(keyStroke) {
    this.unregisterKeyStrokes(keyStroke);
  }

  unregisterKeyStrokes(keyStrokes) {
    arrays.ensure(keyStrokes)
      .map(this._resolveKeyStroke, this)
      .forEach(function(ks) {
        if (arrays.remove(this.keyStrokes, ks) && ks.field && ks.destroyListener) {
          ks.field.off('destroy', ks.destroyListener);
          ks.destroyListener = null;
        }
      }, this);
  }

  _resolveKeyStroke(keyStroke) {
    if (keyStroke instanceof KeyStroke) {
      return keyStroke;
    } else if (keyStroke instanceof Action) {
      return keyStroke.actionKeyStroke;
    }
    throw new Error('unsupported keystroke: ' + keyStroke);
  }

  /**
   * Returns the $target where to bind this context as keydown listener.
   */
  $getBindTarget() {
    return (typeof this.$bindTarget === 'function' ? this.$bindTarget() : this.$bindTarget);
  }

  /**
   * Returns the scope of this context and is used to determine the context's accessibility, meaning not covert by a glasspane.
   */
  $getScopeTarget() {
    return (typeof this.$scopeTarget === 'function' ? this.$scopeTarget() : this.$scopeTarget);
  }

  clone() {
    return new KeyStrokeContext(objects.copyOwnProperties(this, {}));
  }
}
