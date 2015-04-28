scout.PopupMenuItemKeyStrokeAdapter = function(popup) {
  scout.PopupMenuItemKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.MenuControlKeyStrokes(popup, 'menu-item'));
};
scout.inherits(scout.PopupMenuItemKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
