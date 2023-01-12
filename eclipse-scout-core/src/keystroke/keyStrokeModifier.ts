/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {ScoutKeyboardEvent} from '../index';

export const keyStrokeModifier = {
  /**
   * Modifier like shift, ctrl or alt used in keystrokes.
   *
   * A keystroke can consist of multiple modifierBitMask, which can be represented by bitwise OR them together.
   */
  NONE: 0, // without any modifier
  CTRL: 1 << 0, // with the ctrl modifier (NOSONAR)
  CTRL_UNDEFINED: 1 << 1, // with or without the ctrl modifier (NOSONAR)
  SHIFT: 1 << 2, // with the shift modifier (NOSONAR)
  SHIFT_UNDEFINED: 1 << 3, // with or without the shift modifier (NOSONAR)
  ALT: 1 << 4, // with the alt modifier (NOSONAR)
  ALT_UNDEFINED: 1 << 5, // with or without the alt modifier (NOSONAR)

  isCtrl(modifierBitMask: number): boolean {
    return keyStrokeModifier._eval(modifierBitMask, keyStrokeModifier.CTRL, keyStrokeModifier.CTRL_UNDEFINED);
  },

  isShift(modifierBitMask: number): boolean {
    return keyStrokeModifier._eval(modifierBitMask, keyStrokeModifier.SHIFT, keyStrokeModifier.SHIFT_UNDEFINED);
  },

  isAlt(modifierBitMask: number): boolean {
    return keyStrokeModifier._eval(modifierBitMask, keyStrokeModifier.ALT, keyStrokeModifier.ALT_UNDEFINED);
  },

  /** @internal */
  _eval(testee: number, modifier: number, modifierUndefined: number): boolean {
    if ((modifierUndefined & testee) > 0) { // NOSONAR
      return undefined;
    }
    return (modifier & testee) > 0; // NOSONAR
  },

  toModifierBitMask(event: ScoutKeyboardEvent): number {
    let modifierBitMask = 0;
    modifierBitMask |= event.ctrlKey ? keyStrokeModifier.CTRL : 0; // NOSONAR
    modifierBitMask |= event.altKey ? keyStrokeModifier.ALT : 0; // NOSONAR
    modifierBitMask |= event.shiftKey ? keyStrokeModifier.SHIFT : 0; // NOSONAR
    return modifierBitMask;
  }
};
