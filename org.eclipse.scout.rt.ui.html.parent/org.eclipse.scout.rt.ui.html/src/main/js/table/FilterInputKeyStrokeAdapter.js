scout.FilterInputKeyStrokeAdapter = function(field) {
  scout.FilterInputKeyStrokeAdapter.parent.call(this, field);
  this.ctrlPreventBubbleUpKeys = [scout.keys.A, scout.keys.C, scout.keys.Y, scout.keys.V, scout.keys.Z,
    scout.keys.RIGHT,
    scout.keys.BACKSPACE,
    scout.keys.LEFT,
    scout.keys.HOME,
    scout.keys.END,
    scout.keys.NUMPAD_4,
    scout.keys.NUMPAD_6
  ];
  this.ctrlShiftPreventBubbleUpKeys = [scout.keys.RIGHT,
    scout.keys.BACKSPACE,
    scout.keys.LEFT,
    scout.keys.HOME,
    scout.keys.END,
    scout.keys.NUMPAD_4,
    scout.keys.NUMPAD_6
  ];
  this.preventBubbleUpKeys = [scout.keys.SEMICOLON,
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
  ];
};
scout.inherits(scout.FilterInputKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.FilterInputKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeys) {
  //add swallowed keys to drawed keys;
  var i = 0;
  for (i = 0; i < this.preventBubbleUpKeys.length; i++) {
    scout.KeyStrokeUtil.keyStrokeAlreadyDrawnAndDraw(drawedKeys, false, false, false, this.preventBubbleUpKeys[i]);
  }
  for (i = 0; i < this.ctrlShiftPreventBubbleUpKeys.length; i++) {
    scout.KeyStrokeUtil.keyStrokeAlreadyDrawnAndDraw(drawedKeys, true, false, true, this.ctrlShiftPreventBubbleUpKeys[i]);
  }
  for (i = 0; i < this.ctrlPreventBubbleUpKeys.length; i++) {
    scout.KeyStrokeUtil.keyStrokeAlreadyDrawnAndDraw(drawedKeys, true, false, false, this.ctrlPreventBubbleUpKeys[i]);
  }
};

scout.FilterInputKeyStrokeAdapter.prototype.removeKeyBox = function() {
  //nope
};

scout.FilterInputKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  //nope
};

scout.FilterInputKeyStrokeAdapter.prototype.preventBubbleUp = function(event) {
  if (event.altKey || (event.altKey && event.shiftKey)) {
    return false;
  }
  if (event.ctrlKey && event.shiftKey && this.ctrlShiftPreventBubbleUpKeys.indexOf(event.which) > -1) {
    return true;
  }
  if (event.ctrlKey && this.ctrlPreventBubbleUpKeys.indexOf(event.which) > -1) {
    //copy, paste, mark all, etc.
    return true;
  }
  if (this.preventBubbleUpKeys.indexOf(event.which) > -1 || (event.which >= scout.keys.A && event.which <= scout.keys.Z) || (event.which >= scout.keys[0] && event.which <= scout.keys[9])) {
    //all alphabetical chars, numbers, etc. which are captured by input.
    return true;
  }
  return false;
};
