scout.StringFieldKeyStrokeAdapter = function(field) {
  scout.StringFieldKeyStrokeAdapter.parent.call(this, field);
  this.keyStrokes.push(new scout.StringFieldEnterKeyStroke());
};
scout.inherits(scout.StringFieldKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

scout.StringFieldKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeys) {
  scout.StringFieldKeyStrokeAdapter.parent.prototype.drawKeyBox.call(this, drawedKeys);
};

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

scout.StringFieldKeyStrokeAdapter.prototype.preventBubbleUp = function(event) {
  if (this.preventBubbleUpKeys.indexOf(event.which) > -1) {
    return true;
  }
  return false;
};
