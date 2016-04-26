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
scout.keys = {
  BACKSPACE: 8,
  TAB: 9,
  ENTER: 13,
  SHIFT: 16,
  CTRL: 17,
  ALT: 18,
  PAUSE: 19,
  ESC: 27,
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
  SELECT: 93,
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
  SEMICOLON: 186, //ü
  DASH: 189,
  COMMA: 188,
  POINT: 190,
  FORWARD_SLASH: 191, //§
  OPEN_BRACKET: 219, //'
  BACK_SLASH: 220, //ä
  CLOSE_BRACKET: 221, //^
  SINGLE_QUOTE: 222, //ö,
  ANGULAR_BRACKET: 226
};

scout.codesToKeys = {
  8: 'Backspace',
  9: 'Tab',
  13: 'Enter',
  16: 'SHIFT',
  17: 'CTRL',
  18: 'ALT',
  19: 'PAUSE',
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
  186: 'ü', //ü
  189: '-',
  188: ',',
  190: '.',
  191: '§',
  219: '\'',
  220: 'ä',
  221: '^',
  222: 'ö',
  226: '<'
};

scout.keyStrokeBox = {
  gap: 4,

  drawSingleKeyBoxItem: function(offset, keyBoxText, $container, ctrl, alt, shift, alignementRight) {
    if (!$container) {
      return;
    }

    var $existingKeyBoxes = $('.key-box', $container);

    var align = alignementRight ? 'right' : 'left';
    if ($existingKeyBoxes.length > 0) {
      var $boxLastAdded = $existingKeyBoxes.first();
      if (alignementRight) {
        offset = $container.outerWidth() - $boxLastAdded.position().left + this.gap;
      } else {
        offset = this.gap + $boxLastAdded.position().left + $boxLastAdded.outerWidth();
      }
    }
    if (shift) {
      keyBoxText = 'Shift ' + keyBoxText;
    }
    if (alt) {
      keyBoxText = 'Alt ' + keyBoxText;
    }
    if (ctrl) {
      keyBoxText = 'Ctrl ' + keyBoxText;
    }
    if ($container.css('position') === 'absolute' || $container.css('position') === 'relative' || ($container.css('position') === 'static' && $existingKeyBoxes.length > 0)) {
      return $container.prependDiv('key-box ', keyBoxText).css(align, '' + offset + 'px');
    } else {
      var pos = $container.position();
      if (pos) {
        return $container.prependDiv('key-box ', keyBoxText).css(align, '' + (pos.left + offset) + 'px');
      } else {
        // FIXME nbu: (key-strokes) check why sometimes pos is undefined even though we have a valid $container
        $.log.warn('(keys#drawSingleKeyBoxItem) pos is undefined. $container=' + $container);
      }
    }
  }
};
