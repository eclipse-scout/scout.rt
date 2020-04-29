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
/**
 * Modifier like shift, ctrl or alt used in keystrokes.
 *
 * A keystroke can consist of multiple modifierBitMask, which can be represented by bitwise OR'ing them together.
 */

const NONE = 0; // without any modifier
const CTRL = 1 << 0; // with the ctrl modifier (NOSONAR)
const CTRL_UNDEFINED = 1 << 1; // with or without the ctrl modifier (NOSONAR)
const SHIFT = 1 << 2; // with the shift modifier (NOSONAR)
const SHIFT_UNDEFINED = 1 << 3; // with or without the shift modifier (NOSONAR)
const ALT = 1 << 4; // with the alt modifier (NOSONAR)
const ALT_UNDEFINED = 1 << 5; // with or without the alt modifier (NOSONAR)

export function isCtrl(modifierBitMask) {
  return _eval(modifierBitMask, CTRL, CTRL_UNDEFINED);
}

export function isShift(modifierBitMask) {
  return _eval(modifierBitMask, SHIFT, SHIFT_UNDEFINED);
}

export function isAlt(modifierBitMask) {
  return _eval(modifierBitMask, ALT, ALT_UNDEFINED);
}

export function _eval(testee, modifier, modifierUndefined) {
  if ((modifierUndefined & testee) > 0) { // NOSONAR
    return undefined;
  }
  return (modifier & testee) > 0; // NOSONAR
}

export function toModifierBitMask(event) {
  let modifierBitMask = 0;
  modifierBitMask |= event.ctrlKey ? CTRL : 0; // NOSONAR
  modifierBitMask |= event.altKey ? ALT : 0; // NOSONAR
  modifierBitMask |= event.shiftKey ? SHIFT : 0; // NOSONAR
  return modifierBitMask;
}

export default {
  ALT,
  ALT_UNDEFINED,
  CTRL,
  CTRL_UNDEFINED,
  NONE,
  SHIFT,
  SHIFT_UNDEFINED,
  isAlt,
  isCtrl,
  isShift,
  toModifierBitMask
};
