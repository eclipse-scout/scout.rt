scout.ContextMenuKeyStrokeAdapter = function(contextMenuPopup) {
  scout.ContextMenuKeyStrokeAdapter.parent.call(this, contextMenuPopup);

  this.registerKeyStroke(new scout.MenuControlKeyStrokes(contextMenuPopup, 'menu-item'));
  this.registerKeyStroke(new scout.MenuPopupCloseKeyStrokes(contextMenuPopup));
};
scout.inherits(scout.ContextMenuKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
