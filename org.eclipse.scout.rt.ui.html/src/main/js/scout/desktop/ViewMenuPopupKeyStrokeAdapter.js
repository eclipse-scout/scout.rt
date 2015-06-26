scout.ViewMenuPopupKeyStrokeAdapter = function(popup) {
  scout.ViewMenuPopupKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.MenuControlKeyStrokes(popup, 'view-button-menu'));
  var closeKeyStroke = new scout.PopupCloseKeyStroke(popup);
  closeKeyStroke.keyStroke = 'F2';
  closeKeyStroke.initKeyStrokeParts();
  this.keyStrokes.push(closeKeyStroke);
};
scout.inherits(scout.ViewMenuPopupKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
