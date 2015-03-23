scout.PopupKeyStrokeAdapter = function(popup) {
  scout.PopupKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.MenuControlKeyStrokes(popup));
  this.keyStrokes.push(new scout.MenuCloseKeyStrokes(popup));
};
scout.inherits(scout.PopupKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
