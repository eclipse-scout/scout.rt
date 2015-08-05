/**
 * Keystroke adapter for keystrokes referring to the view button area (top-left corner).
 * Such keystrokes are enabled if the view button area is not covert by a glass pane.
 */
scout.DesktopViewButtonKeyStrokeAdapter = function(desktop) {
  scout.DesktopViewButtonKeyStrokeAdapter.parent.call(this, {
    'keyStrokes': desktop.viewButtons,
    '$container': desktop.navigation.$viewButtons
  });

  // Keystroke to open view-button popup to change outline (F2)
  this.registerKeyStroke(new scout.DesktopViewButtonPopupKeyStroke(desktop.navigation));
};
scout.inherits(scout.DesktopViewButtonKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
