scout.ContextMenuKeyStrokeAdapter = function(popup) {
  scout.ContextMenuKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.MenuControlKeyStrokes(popup, 'menu-item'));
  this.keyStrokes.push(new scout.MenuPopupCloseKeyStrokes(popup));
};
scout.inherits(scout.ContextMenuKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
