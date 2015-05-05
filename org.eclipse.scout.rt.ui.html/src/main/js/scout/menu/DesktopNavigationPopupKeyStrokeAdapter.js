scout.DesktopNavigationPopupKeyStrokeAdapter = function(popup) {
  scout.DesktopNavigationPopupKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.MenuControlKeyStrokes(popup, 'outline-menu-item'));
  var closeKeyStroke = new scout.PopupCloseKeyStroke(popup);
  closeKeyStroke.keyStroke = 'F2';
  closeKeyStroke.initKeyStrokeParts();
  this.keyStrokes.push(closeKeyStroke);
};
scout.inherits(scout.DesktopNavigationPopupKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
