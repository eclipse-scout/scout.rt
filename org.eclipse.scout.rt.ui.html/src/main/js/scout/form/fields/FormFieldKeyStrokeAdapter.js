scout.FormFieldKeyStrokeAdapter = function(formField) {
  scout.FormFieldKeyStrokeAdapter.parent.call(this, formField);

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
scout.inherits(scout.FormFieldKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.FormFieldKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeys) {
  if (this._srcElement && this._srcElement.$field && this._srcElement.$field.is('input:text')) {
    //add swallowed keys to drawed keys;
    var i = 0;
    for (i = 0; i < this.preventBubbleUpKeys.length; i++) {
      scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, false, false, false, this.preventBubbleUpKeys[i]);
    }
    for (i = 0; i < this.ctrlShiftPreventBubbleUpKeys.length; i++) {
      scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, true, false, true, this.ctrlShiftPreventBubbleUpKeys[i]);
    }
    for (i = 0; i < this.ctrlPreventBubbleUpKeys.length; i++) {
      scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, true, false, false, this.ctrlPreventBubbleUpKeys[i]);
    }
  }
  scout.FormFieldKeyStrokeAdapter.parent.prototype.drawKeyBox.call(this, drawedKeys);
};

scout.FormFieldKeyStrokeAdapter.prototype.preventBubbleUp = function(event) {
  if (this._srcElement.$field && this._srcElement.$field.is('input:text')) {
    return this._isInputKeyStroke(event) ||
      this._isCtrlPrevent(event) ||
      this._isCtrlShiftPrevent(event);
  } else {
    return false;
  }
};

scout.FormFieldKeyStrokeAdapter.prototype._isNumberKeyStroke = function(event) {
  return event.which >= scout.keys[0] && event.which <= scout.keys[9];
};

scout.FormFieldKeyStrokeAdapter.prototype._isLetterKeyStroke = function(event) {
  return event.which >= scout.keys.A && event.which <= scout.keys.Z;
};

scout.FormFieldKeyStrokeAdapter.prototype._isInputKeyStroke = function(event) {
  if (event.ctrlKey || event.altKey) {
    return false; // no printable character if Ctrl or Alt modifier is pressed.
  } else {
    return this._isLetterKeyStroke(event) || this._isNumberKeyStroke(event) || this.preventBubbleUpKeys.indexOf(event.which) > -1 ;
  }
};

scout.FormFieldKeyStrokeAdapter.prototype._isCtrlPrevent = function(event) {
  return event.ctrlKey && this.ctrlPreventBubbleUpKeys.indexOf(event.which) > -1;
};

scout.FormFieldKeyStrokeAdapter.prototype._isCtrlShiftPrevent = function(event) {
  return event.ctrlKey && event.shiftKey && this.ctrlShiftPreventBubbleUpKeys.indexOf(event.which) > -1;
};
