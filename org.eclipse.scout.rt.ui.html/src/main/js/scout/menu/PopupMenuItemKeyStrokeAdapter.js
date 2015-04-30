scout.PopupMenuItemKeyStrokeAdapter = function(popup) {
  scout.PopupMenuItemKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.MenuControlKeyStrokes(popup, 'menu-item'));
  this.keyStrokes.push(new scout.MenuPopupCloseKeyStrokes(popup));
};
scout.inherits(scout.PopupMenuItemKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
