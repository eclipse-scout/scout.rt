scout.StringFieldKeyStrokeAdapter = function(field) {
  scout.StringFieldKeyStrokeAdapter.parent.call(this, field);

  // Prevent enter to bubble up and execute form or groupbox enter key.
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
