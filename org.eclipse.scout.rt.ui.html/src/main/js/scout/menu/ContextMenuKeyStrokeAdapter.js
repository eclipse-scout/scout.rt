scout.ContextMenuKeyStrokeAdapter = function(popup) {
  scout.ContextMenuKeyStrokeAdapter.parent.call(this, popup);

  this.registerKeyStroke(new scout.MenuControlKeyStrokes(popup, 'menu-item'));
  this.registerKeyStroke(new scout.MenuPopupCloseKeyStrokes(popup));
};
scout.inherits(scout.ContextMenuKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
