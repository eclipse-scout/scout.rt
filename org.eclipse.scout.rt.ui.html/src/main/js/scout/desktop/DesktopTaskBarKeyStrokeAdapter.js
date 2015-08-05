/**
 * Keystroke adapter for keystrokes referring to the task bar.
 * Such keystrokes are enabled if the desktop task bar is not covert by a glass pane.
 */
scout.DesktopTaskBarKeyStrokeAdapter = function(desktop) {
  scout.DesktopTaskBarKeyStrokeAdapter.parent.call(this, {
    'keyStrokes': desktop.actions,
    '$container': desktop._$taskBar
  });

  // Composite keystroke to switch between the view tabs.
  this.registerKeyStroke(new scout.ViewTabCompositeKeyStroke(desktop));
};
scout.inherits(scout.DesktopTaskBarKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
