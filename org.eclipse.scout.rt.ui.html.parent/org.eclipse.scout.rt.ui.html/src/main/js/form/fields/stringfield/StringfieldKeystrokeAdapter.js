scout.StringfieldKeystrokeAdapter = function(field) {
  scout.StringfieldKeystrokeAdapter.parent.call(this, field);
  var that = this;
  //prevent enter to bubble up and execute form or groupbox enter key.
  var enterKeystroke = new scout.StringfieldEnterKeystroke();
  enterKeystroke.initKeystrokeParts();
  this.keystrokes.push(enterKeystroke);
};

scout.inherits(scout.StringfieldKeystrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.StringfieldEnterKeystroke = function() {
  scout.StringfieldEnterKeystroke.parent.call(this);
  this.keystroke = 'ENTER';
  this.drawHint = false;
};
scout.inherits(scout.StringfieldEnterKeystroke, scout.Keystroke);

scout.StringfieldEnterKeystroke.prototype.handle = function(event) {};

scout.StringfieldEnterKeystroke.prototype.accept = function(event) {
  var acceptKey = scout.Keystroke.prototype.accept.call(this, event);
  var elementType = document.activeElement.tagName.toLowerCase();

  if (acceptKey && elementType === 'textarea') {
    return true;
  }
  return false;
};
