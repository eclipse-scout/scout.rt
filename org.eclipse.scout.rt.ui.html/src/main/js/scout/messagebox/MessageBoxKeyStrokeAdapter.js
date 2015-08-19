scout.MessageBoxKeyStrokeAdapter = function(messageBox) {
  scout.MessageBoxKeyStrokeAdapter.parent.call(this, messageBox);
  this.registerKeyStroke(new scout.MessageBoxControlKeyStrokes(messageBox));
  this.registerKeyStroke(new scout.MessageBoxExecuteButtonKeyStroke('enter'));
  this.registerKeyStroke(new scout.MessageBoxExecuteButtonKeyStroke('space'));
};
scout.inherits(scout.MessageBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);


scout.MessageBoxExecuteButtonKeyStroke = function(keyStroke) {
  scout.MessageBoxControlKeyStrokes.parent.call(this);
  this.keyStroke = keyStroke;
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.MessageBoxExecuteButtonKeyStroke, scout.KeyStroke);

scout.MessageBoxExecuteButtonKeyStroke.prototype.handle = function(event) {
  var activeElement = document.activeElement;
  if (activeElement) {
    $(activeElement).trigger({ type: 'click', which: 1 });
  }
};
