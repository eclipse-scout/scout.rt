scout.StringFieldKeyStrokeAdapter = function(field) {
  scout.StringFieldKeyStrokeAdapter.parent.call(this, field);

  // Prevent enter to bubble up and execute form or groupbox enter key.
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
  this.keyStrokes.push(new scout.StringFieldEnterKeyStroke());
  this.keyStrokes.push(new scout.StringFieldBackspaceKeyStroke());
};
scout.inherits(scout.StringFieldKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

scout.StringFieldEnterKeyStroke = function() {
  scout.StringFieldEnterKeyStroke.parent.call(this);
  this.keyStroke = 'ENTER';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.StringFieldEnterKeyStroke, scout.KeyStroke);

scout.StringFieldEnterKeyStroke.prototype.handle = function(event) {};

scout.StringFieldEnterKeyStroke.prototype.accept = function(event) {
  var acceptKey = scout.StringFieldEnterKeyStroke.parent.prototype.accept.call(this, event);
  var elementType = document.activeElement.tagName.toLowerCase();

  if (acceptKey && elementType === 'textarea') {
    return true;
  }
  return false;
};

scout.StringFieldEnterKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  //add swallowed keys to drawed keys;
  var i = 0;
  for (i = 0; i < this.preventBubbleUpKeys.length; i++) {
    scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, false, false, false, this.preventBubbleUpKeys[i]);
  }
  if (drawedKeys[this.keyStrokeName()]) {
    return;
  }
  var elementType = document.activeElement.tagName.toLowerCase();
  if (this.drawHint) {
    this._drawKeyBox($container);
  }
  if (elementType === 'textarea') {
    drawedKeys[this.keyStrokeName()] = true;
  }
};

scout.StringFieldBackspaceKeyStroke = function() {
  scout.StringFieldBackspaceKeyStroke.parent.call(this);
  this.keyStroke = 'Backspace';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.StringFieldBackspaceKeyStroke, scout.KeyStroke);

scout.StringFieldBackspaceKeyStroke.prototype.handle = function(event) {};

scout.StringFieldKeyStrokeAdapter.prototype.preventBubbleUp = function(event) {
  if (this.preventBubbleUpKeys.indexOf(event.which) > -1) {
    return true;
  }
  return false;
};
