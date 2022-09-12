/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import Device from '../util/Device';

const keys = {
  BACKSPACE: 8,
  TAB: 9,
  ENTER: 13,
  SHIFT: 16,
  CTRL: 17,
  ALT: 18,
  PAUSE: 19,
  CAPS_LOCK: 20,
  ESC: 27,
  /**
   * The duplicate ESC / ESCAPE constant is required for Scout classic
   * where keys defined in IKeyStroke are mapped to keys defined in keys.js
   */
  ESCAPE: 27,
  PAGE_UP: 33,
  PAGE_DOWN: 34,
  END: 35,
  HOME: 36,
  LEFT: 37,
  UP: 38,
  RIGHT: 39,
  DOWN: 40,
  SPACE: 32,
  PRINT_SCREEN: 44,
  INSERT: 45,
  DELETE: 46,
  0: 48,
  1: 49,
  2: 50,
  3: 51,
  4: 52,
  5: 53,
  6: 54,
  7: 55,
  8: 56,
  9: 57,
  A: 65,
  B: 66,
  C: 67,
  D: 68,
  E: 69,
  F: 70,
  G: 71,
  H: 72,
  I: 73,
  J: 74,
  K: 75,
  L: 76,
  M: 77,
  N: 78,
  O: 79,
  P: 80,
  Q: 81,
  R: 82,
  S: 83,
  T: 84,
  U: 85,
  V: 86,
  W: 87,
  X: 88,
  Y: 89,
  Z: 90,
  F1: 112,
  F2: 113,
  F3: 114,
  F4: 115,
  F5: 116,
  F6: 117,
  F7: 118,
  F8: 119,
  F9: 120,
  F10: 121,
  F11: 122,
  F12: 123,
  WIN_LEFT: 91, // Left windows key / left command key
  WIN_RIGHT: 92, // Right windows key / right command key
  SELECT: 93, // Menu key
  NUMPAD_0: 96,
  NUMPAD_1: 97,
  NUMPAD_2: 98,
  NUMPAD_3: 99,
  NUMPAD_4: 100,
  NUMPAD_5: 101,
  NUMPAD_6: 102,
  NUMPAD_7: 103,
  NUMPAD_8: 104,
  NUMPAD_9: 105,
  MULTIPLY: 106,
  ADD: 107,
  SUBTRACT: 109,
  DECIMAL_POINT: 110,
  DIVIDE: 111,
  NUM_LOCK: 144,
  SCROLL_LOCK: 145,
  SEMICOLON: 186, // ü
  DASH: 189,
  COMMA: 188,
  POINT: 190,
  FORWARD_SLASH: 191, // §
  OPEN_BRACKET: 219, // '
  BACK_SLASH: 220, // ä
  CLOSE_BRACKET: 221, // ^
  SINGLE_QUOTE: 222, // ö,
  ANGULAR_BRACKET: 226,

  codesToKeys: {
    8: 'Backspace',
    9: 'Tab',
    13: 'Enter',
    16: 'SHIFT',
    17: 'CTRL',
    18: 'ALT',
    19: 'PAUSE',
    20: 'Caps lock',
    27: 'Esc',
    33: 'Page up',
    34: 'Page down',
    35: 'End',
    36: 'Home',
    37: 'Left',
    38: 'UP',
    39: 'Right',
    40: 'Down',
    32: 'Space',
    44: 'Print screen',
    45: 'Insert',
    46: 'Delete',
    48: '0',
    49: '1',
    50: '2',
    51: '3',
    52: '4',
    53: '5',
    54: '6',
    55: '7',
    56: '8',
    57: '9',
    65: 'A',
    66: 'B',
    67: 'C',
    68: 'D',
    69: 'E',
    70: 'F',
    71: 'G',
    72: 'H',
    73: 'I',
    74: 'J',
    75: 'K',
    76: 'L',
    77: 'M',
    78: 'N',
    79: 'O',
    80: 'P',
    81: 'Q',
    82: 'R',
    83: 'S',
    84: 'T',
    85: 'U',
    86: 'V',
    87: 'W',
    88: 'X',
    89: 'Y',
    90: 'Z',
    91: 'WIN_LEFT',
    92: 'WIN_RIGHT',
    93: 'SELECT',
    112: 'F1',
    113: 'F2',
    114: 'F3',
    115: 'F4',
    116: 'F5',
    117: 'F6',
    118: 'F7',
    119: 'F8',
    120: 'F9',
    121: 'F10',
    122: 'F11',
    123: 'F12',
    96: 'NUMPAD_0',
    97: 'NUMPAD_1',
    98: 'NUMPAD_2',
    99: 'NUMPAD_3',
    100: 'NUMPAD_4',
    101: 'NUMPAD_5',
    102: 'NUMPAD_6',
    103: 'NUMPAD_7',
    104: 'NUMPAD_8',
    105: 'NUMPAD_9',
    106: 'MULTIPLY',
    107: '+',
    109: 'SUBTRACT',
    110: 'DECIMAL_POINT',
    111: '/',
    144: 'Num lock',
    145: 'Scroll lock',
    186: 'ü', // ü
    189: '-',
    188: ',',
    190: '.',
    191: '§',
    219: '\'',
    220: 'ä',
    221: '^',
    222: 'ö',
    226: '<'
  },

  /**
   * This map defines key-codes which are not the same in various browsers. Use the forBrowser function to access it.
   */
  browserMap: {
    [Device.Browser.FIREFOX]: {
      226: 60
    },
    [Device.Browser.SAFARI]: {
      226: 188,
      ctrl: {
        226: 192
      }
    }
  },

  browserMapReverse: {},

  /**
   * If a browser has a non-standard key-code for one of the keys defined in this file this function returns the correct key code for that browser.
   *
   * @param {number} keyCode
   * @param {string} [modifier] some key codes change when a modifier is pressed
   * @returns {number}
   */
  forBrowser: (keyCode, modifier) => keys.mapKey(keys.browserMap, keyCode, modifier),

  /**
   * If a browser has a non-standard key-code for one of the keys defined in this file this function returns the original key for that browser.
   *
   * @param {number} keyCode
   * @returns {number}
   */
  fromBrowser: keyCode => keys.mapKey(keys.browserMapReverse, keyCode),

  mapKey: (map, keyCode, modifier) => {
    let browserMap = map[Device.get().browser];
    if (browserMap && modifier) {
      browserMap = browserMap[modifier];
    }
    if (browserMap && browserMap.hasOwnProperty(keyCode)) {
      // A mapping is defined for this browser and key-code
      return browserMap[keyCode];
    }
    // No mapping is defined, use standard
    return keyCode;
  }
};

// Create a map with reverse key mappings for browser specific keys
for (let browser in keys.browserMap) {
  let reverseMap = {};
  let mappedKeysForBrowser = keys.browserMap[browser];
  for (let origKey in mappedKeysForBrowser) {
    let browserKey = mappedKeysForBrowser[origKey];
    reverseMap[browserKey] = parseInt(origKey, 10);
  }
  keys.browserMapReverse[browser] = reverseMap;
}

export default keys;
