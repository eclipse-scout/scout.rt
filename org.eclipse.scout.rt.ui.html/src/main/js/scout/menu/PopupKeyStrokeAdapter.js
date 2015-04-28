scout.PopupKeyStrokeAdapter = function(popup) {
  scout.PopupKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.PopupCloseKeyStrokes(popup));
};
scout.inherits(scout.PopupKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
