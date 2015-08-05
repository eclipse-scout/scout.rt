scout.PopupKeyStrokeAdapter = function(popup) {
  scout.PopupKeyStrokeAdapter.parent.call(this, popup);

  this.registerKeyStroke(new scout.PopupCloseKeyStroke(popup));
};
scout.inherits(scout.PopupKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
