/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStrokeContext, keyStrokeModifier} from '../index';
import $ from 'jquery';

/**
 * Keystroke context used for input fields.
 */
export default class InputFieldKeyStrokeContext extends KeyStrokeContext {

  constructor(multiline) {
    super();

    this.invokeAcceptInputOnActiveValueField = true;

    let navigationKeys = [
      keys.RIGHT,
      keys.LEFT,
      keys.HOME,
      keys.END
    ];
    this.registerStopPropagationKeys(keyStrokeModifier.CTRL, [
      keys.A,
      keys.C,
      keys.Y,
      keys.V,
      keys.Z,
      keys.BACKSPACE
    ].concat(navigationKeys));
    this.registerStopPropagationKeys(keyStrokeModifier.CTRL | keyStrokeModifier.SHIFT, navigationKeys);
    this.registerStopPropagationKeys(keyStrokeModifier.SHIFT, navigationKeys);
    this.registerStopPropagationKeys(keyStrokeModifier.NONE, [
      keys.SEMICOLON,
      keys.DASH,
      keys.COMMA,
      keys.POINT,
      keys.FORWARD_SLASH,
      keys.OPEN_BRACKET,
      keys.BACK_SLASH,
      keys.CLOSE_BRACKET,
      keys.SINGLE_QUOTE,
      keys.MULTIPLY,
      keys.ADD,
      keys.SUBTRACT,
      keys.DECIMAL_POINT,
      keys.DIVIDE,
      keys.NUMPAD_0,
      keys.NUMPAD_1,
      keys.NUMPAD_2,
      keys.NUMPAD_3,
      keys.NUMPAD_4,
      keys.NUMPAD_5,
      keys.NUMPAD_6,
      keys.NUMPAD_7,
      keys.NUMPAD_8,
      keys.NUMPAD_9,
      keys.MULTIPLY,
      keys.BACKSPACE,
      keys.DELETE,
      keys.SPACE
    ].concat(navigationKeys));
    this.setMultiline(multiline);
  }

  setMultiline(multiline) {
    let multilineNavigationKeys = [
      keys.UP,
      keys.DOWN
    ];
    this.toggleStopPropagationKeys(keyStrokeModifier.CTRL, multilineNavigationKeys, multiline);
    this.toggleStopPropagationKeys(keyStrokeModifier.CTRL | keyStrokeModifier.SHIFT, multilineNavigationKeys, multiline);
    this.toggleStopPropagationKeys(keyStrokeModifier.SHIFT, multilineNavigationKeys, multiline);
    this.toggleStopPropagationKeys(keyStrokeModifier.NONE, multilineNavigationKeys, multiline);
  }

  _applyPropagationFlags(event) {
    super._applyPropagationFlags(event);

    if (event.isPropagationStopped()) {
      return;
    }

    if (this._isInputEvent(event)) {
      event.stopPropagation();
    }
  }

  _isInputEvent(event) {
    if (!this.isInput(event.target)) {
      return false;
    }
    if (this._isLetterKeyStroke(event)) {
      return true;
    }
    if (this._isNumberKeyStroke(event)) {
      return true;
    }
    return false;
  }

  isInput($element) {
    // noinspection JSDeprecatedSymbols
    return this._isInputField($element);
  }

  /**
   * @deprecated use isInput instead
   */
  _isInputField($element) {
    let $elem = $.ensure($element);
    if (!$elem.length) {
      return false;
    }
    return $elem[0].isContentEditable
      || $elem.is('textarea')
      || $elem.is('input') && !this.isButton($elem);
  }

  isButton($element) {
    let $elem = $.ensure($element);
    let buttonTypes = ['button', 'input[type=button]', 'input[type=checkbox', 'input[type=color]', 'input[type=file]', 'input[type=image]', 'input[type=radio]', 'input[type=reset]', 'input[type=submit]'];
    return buttonTypes.some(type => $elem.is(type));
  }

  _isNumberKeyStroke(event) {
    return !event.ctrlKey && event.which >= keys[0] && event.which <= keys[9];
  }

  _isLetterKeyStroke(event) {
    return !event.ctrlKey && event.which >= keys.A && event.which <= keys.Z;
  }
}
