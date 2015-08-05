/**
 * Keystroke adapter for the 'ViewMenuPopup' to close the popup (inherited), and to allow menu-item navigation by keyboard.
 */
scout.ViewMenuKeyStrokeAdapter = function(viewMenuPopup) {
  scout.ViewMenuKeyStrokeAdapter.parent.call(this, viewMenuPopup);

  this.registerKeyStroke(new scout.MenuControlKeyStrokes(viewMenuPopup, 'view-button-menu'));
};
scout.inherits(scout.ViewMenuKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
