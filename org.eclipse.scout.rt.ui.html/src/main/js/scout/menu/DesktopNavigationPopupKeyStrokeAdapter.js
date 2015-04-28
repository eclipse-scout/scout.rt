scout.DesktopNavigationPopupKeyStrokeAdapter = function(popup) {
  scout.DesktopNavigationPopupKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.MenuControlKeyStrokes(popup, 'outline-menu-item'));
};
scout.inherits(scout.DesktopNavigationPopupKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
