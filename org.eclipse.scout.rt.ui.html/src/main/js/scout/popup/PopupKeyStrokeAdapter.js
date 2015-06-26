scout.PopupKeyStrokeAdapter = function(popup) {
  scout.PopupKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.PopupCloseKeyStroke(popup));
  this.anchorKeyStrokeAdapter = true;
};
scout.inherits(scout.PopupKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
