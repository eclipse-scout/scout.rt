scout.MessageBoxKeyStrokeAdapter = function(messageBox) {
  scout.MessageBoxKeyStrokeAdapter.parent.call(this, messageBox);
  this.registerKeyStroke(new scout.MessageBoxControlKeyStrokes(messageBox));
};
scout.inherits(scout.MessageBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
