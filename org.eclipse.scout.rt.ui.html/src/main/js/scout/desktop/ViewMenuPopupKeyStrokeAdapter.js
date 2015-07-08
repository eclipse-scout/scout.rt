scout.ViewMenuPopupKeyStrokeAdapter = function(popup) {
  scout.ViewMenuPopupKeyStrokeAdapter.parent.call(this, popup);

  this.registerKeyStroke(new scout.MenuControlKeyStrokes(popup, 'view-button-menu'));
  var closeKeyStroke = new scout.PopupCloseKeyStroke(popup);
  closeKeyStroke.keyStroke = 'F2';
  closeKeyStroke.initKeyStrokeParts();
  this.registerKeyStroke(closeKeyStroke);
};
scout.inherits(scout.ViewMenuPopupKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
