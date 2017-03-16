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
/**
 * Keystroke context used for input fields.
 */
scout.InputFieldKeyStrokeContext = function() {
  scout.InputFieldKeyStrokeContext.parent.call(this);

  this.invokeAcceptInputOnActiveValueField = true;

  this.registerStopPropagationKeys(scout.keyStrokeModifier.CTRL, [
    scout.keys.A,
    scout.keys.C,
    scout.keys.Y,
    scout.keys.V,
    scout.keys.Z,
    scout.keys.RIGHT,
    scout.keys.BACKSPACE,
    scout.keys.LEFT,
    scout.keys.HOME,
    scout.keys.END,
    scout.keys.NUMPAD_4,
    scout.keys.NUMPAD_6
  ]);

  this.registerStopPropagationKeys(scout.keyStrokeModifier.CTRL | scout.keyStrokeModifier.SHIFT, [ // NOSONAR
    scout.keys.RIGHT,
    scout.keys.BACKSPACE,
    scout.keys.LEFT,
    scout.keys.HOME,
    scout.keys.END,
    scout.keys.NUMPAD_4,
    scout.keys.NUMPAD_6
  ]);

  this.registerStopPropagationKeys(scout.keyStrokeModifier.SHIFT, [
    scout.keys.RIGHT,
    scout.keys.BACKSPACE,
    scout.keys.LEFT,
    scout.keys.HOME,
    scout.keys.END,
    scout.keys.NUMPAD_4,
    scout.keys.NUMPAD_6
  ]);
  this.registerStopPropagationKeys(scout.keyStrokeModifier.NONE, [
    scout.keys.SEMICOLON,
    scout.keys.DASH,
    scout.keys.COMMA,
    scout.keys.POINT,
    scout.keys.FORWARD_SLASH,
    scout.keys.OPEN_BRACKET,
    scout.keys.BACK_SLASH,
    scout.keys.CLOSE_BRACKET,
    scout.keys.SINGLE_QUOTE,
    scout.keys.MULTIPLY,
    scout.keys.ADD,
    scout.keys.SUBTRACT,
    scout.keys.DECIMAL_POINT,
    scout.keys.DIVIDE,
    scout.keys.NUMPAD_0,
    scout.keys.NUMPAD_1,
    scout.keys.NUMPAD_2,
    scout.keys.NUMPAD_3,
    scout.keys.NUMPAD_4,
    scout.keys.NUMPAD_5,
    scout.keys.NUMPAD_6,
    scout.keys.NUMPAD_7,
    scout.keys.NUMPAD_8,
    scout.keys.NUMPAD_9,
    scout.keys.MULTIPLY,
    scout.keys.END,
    scout.keys.HOME,
    scout.keys.RIGHT,
    scout.keys.BACKSPACE,
    scout.keys.LEFT,
    scout.keys.DELETE,
    scout.keys.SPACE
  ]);
};
scout.inherits(scout.InputFieldKeyStrokeContext, scout.KeyStrokeContext);

scout.InputFieldKeyStrokeContext.prototype._applyPropagationFlags = function(event) {
  scout.InputFieldKeyStrokeContext.parent.prototype._applyPropagationFlags.call(this, event);

  if (event.isPropagationStopped()) {
    return;
  }

  var inputField = $(event.target).is('input:text') || $(event.target).is('input:file') || $(event.target).is('textarea');
  if (inputField && (this._isLetterKeyStroke(event) || this._isNumberKeyStroke(event))) {
    event.stopPropagation();
  }
};

scout.InputFieldKeyStrokeContext.prototype._isNumberKeyStroke = function(event) {
  return !event.ctrlKey && event.which >= scout.keys[0] && event.which <= scout.keys[9];
};

scout.InputFieldKeyStrokeContext.prototype._isLetterKeyStroke = function(event) {
  return !event.ctrlKey && event.which >= scout.keys.A && event.which <= scout.keys.Z;
};
