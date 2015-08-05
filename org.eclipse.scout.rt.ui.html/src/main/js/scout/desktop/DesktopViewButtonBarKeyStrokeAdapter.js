/**
 * Keystroke adapter for keystrokes referring to the view button area (top-left corner).
 * Such keystrokes are enabled if the view button area is not covert by a glass pane.
 */
scout.DesktopViewButtonBarKeyStrokeAdapter = function(desktop) {
  scout.DesktopViewButtonBarKeyStrokeAdapter.parent.call(this, {
    'keyStrokes': desktop.viewButtons,
    '$container': desktop.navigation.$viewButtons
  });

  // Keystroke to open view-button popup to change outline (F2)
  this.registerKeyStroke(new scout.ViewMenuOpenKeyStroke(desktop.navigation));
};
scout.inherits(scout.DesktopViewButtonBarKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
