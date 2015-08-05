/**
 * Keystroke adapter for the 'ContextMenuPopup' to close the popup (inherited), and to allow menu-item navigation by keyboard.
 */
scout.ContextMenuKeyStrokeAdapter = function(contextMenuPopup) {
  scout.ContextMenuKeyStrokeAdapter.parent.call(this, contextMenuPopup);

  this.registerKeyStroke(new scout.MenuControlKeyStrokes(contextMenuPopup, 'menu-item'));
};
scout.inherits(scout.ContextMenuKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
