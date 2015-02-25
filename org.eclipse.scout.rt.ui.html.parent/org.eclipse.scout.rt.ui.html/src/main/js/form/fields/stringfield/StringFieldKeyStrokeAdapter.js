scout.StringFieldKeyStrokeAdapter = function(field) {
  scout.StringFieldKeyStrokeAdapter.parent.call(this, field);
  var that = this;
  //prevent enter to bubble up and execute form or groupbox enter key.
  var enterKeyStroke = new scout.StringFieldEnterKeyStroke();
  enterKeyStroke.initKeyStrokeParts();
  this.keyStrokes.push(enterKeyStroke);
};

scout.inherits(scout.StringFieldKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.StringFieldEnterKeyStroke = function() {
  scout.StringFieldEnterKeyStroke.parent.call(this);
  this.keyStroke = 'ENTER';
  this.drawHint = false;
};
scout.inherits(scout.StringFieldEnterKeyStroke, scout.KeyStroke);

scout.StringFieldEnterKeyStroke.prototype.handle = function(event) {};

scout.StringFieldEnterKeyStroke.prototype.accept = function(event) {
  var acceptKey = scout.KeyStroke.prototype.accept.call(this, event);
  var elementType = document.activeElement.tagName.toLowerCase();

  if (acceptKey && elementType === 'textarea') {
    return true;
  }
  return false;
};
