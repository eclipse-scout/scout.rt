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
 * Modifier like shift, ctrl or alt used in keystrokes.
 *
 * A keystroke can consist of multiple modifierBitMask, which can be represented by bitwise OR'ing them together.
 */
scout.keyStrokeModifier = {
  NONE: 0, // without any modifier
  CTRL: 1 << 0, // with the ctrl modifier
  CTRL_UNDEFINED: 1 << 1, // with or without the ctrl modifier
  SHIFT: 1 << 2, // with the shift modifier
  SHIFT_UNDEFINED: 1 << 3, // with or without the shift modifier
  ALT: 1 << 4, // with the alt modifier
  ALT_UNDEFINED: 1 << 5, // with or without the alt modifier

  isCtrl: function(modifierBitMask) {
    return this._eval(modifierBitMask, this.CTRL, this.CTRL_UNDEFINED);
  },

  isShift: function(modifierBitMask) {
    return this._eval(modifierBitMask, this.SHIFT, this.SHIFT_UNDEFINED);
  },

  isAlt: function(modifierBitMask) {
    return this._eval(modifierBitMask, this.ALT, this.ALT_UNDEFINED);
  },

  _eval: function(testee, modifier, modifierUndefined) {
    if ((modifierUndefined & testee) > 0) {
      return undefined;
    } else {
      return (modifier & testee) > 0;
    }
  },

  toModifierBitMask: function(event) {
    var modifierBitMask = 0;
    modifierBitMask |= event.ctrlKey ? scout.keyStrokeModifier.CTRL : 0;
    modifierBitMask |= event.altKey ? scout.keyStrokeModifier.ALT : 0;
    modifierBitMask |= event.shiftKey ? scout.keyStrokeModifier.SHIFT : 0;
    return modifierBitMask;
  }
};
