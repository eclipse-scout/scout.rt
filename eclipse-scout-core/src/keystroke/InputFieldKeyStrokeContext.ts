/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStrokeContext, keyStrokeModifier, ScoutKeyboardEvent} from '../index';
import $ from 'jquery';

/**
 * Keystroke context used for input fields.
 */
export class InputFieldKeyStrokeContext extends KeyStrokeContext {

  constructor(multiline?: boolean) {
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

  setMultiline(multiline?: boolean) {
    let multilineNavigationKeys = [
      keys.UP,
      keys.DOWN
    ];
    this.toggleStopPropagationKeys(keyStrokeModifier.CTRL, multilineNavigationKeys, multiline);
    this.toggleStopPropagationKeys(keyStrokeModifier.CTRL | keyStrokeModifier.SHIFT, multilineNavigationKeys, multiline);
    this.toggleStopPropagationKeys(keyStrokeModifier.SHIFT, multilineNavigationKeys, multiline);
    this.toggleStopPropagationKeys(keyStrokeModifier.NONE, multilineNavigationKeys, multiline);
  }

  protected override _applyPropagationFlags(event: ScoutKeyboardEvent) {
    super._applyPropagationFlags(event);

    if (event.isPropagationStopped()) {
      return;
    }

    if (this._isInputEvent(event)) {
      event.stopPropagation();
    }
  }

  protected _isInputEvent(event: ScoutKeyboardEvent): boolean {
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

  isInput($element: HTMLElement | JQuery): boolean {
    let $elem = $.ensure($element);
    if (!$elem.length) {
      return false;
    }
    return $elem[0].isContentEditable
      || $elem.is('textarea')
      || $elem.is('input') && !this.isButton($elem);
  }

  isButton($element: HTMLElement | JQuery): boolean {
    let $elem = $.ensure($element);
    let buttonTypes = ['button', 'input[type=button]', 'input[type=checkbox', 'input[type=color]', 'input[type=file]', 'input[type=image]', 'input[type=radio]', 'input[type=reset]', 'input[type=submit]'];
    return buttonTypes.some(type => $elem.is(type));
  }

  protected _isNumberKeyStroke(event: ScoutKeyboardEvent): boolean {
    return !event.ctrlKey && event.which >= keys[0] && event.which <= keys[9];
  }

  protected _isLetterKeyStroke(event: ScoutKeyboardEvent): boolean {
    return !event.ctrlKey && event.which >= keys.A && event.which <= keys.Z;
  }
}
