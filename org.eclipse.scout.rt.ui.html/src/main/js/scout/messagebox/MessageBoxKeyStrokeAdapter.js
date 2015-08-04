scout.MessageBoxKeyStrokeAdapter = function(messageBox) {
  scout.MessageBoxKeyStrokeAdapter.parent.call(this, messageBox);
  this.registerKeyStroke(new scout.MessageBoxControlKeyStrokes(messageBox));
  this.anchorKeyStrokeAdapter = true;
};
scout.inherits(scout.MessageBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
