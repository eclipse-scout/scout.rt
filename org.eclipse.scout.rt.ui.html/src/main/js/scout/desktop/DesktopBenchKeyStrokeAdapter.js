/**
 * Keystroke adapter for keystrokes referring to the desktop bench.
 * Such keystrokes are enabled if the desktop bench is not covert by a glass pane.
 */
scout.DesktopBenchKeyStrokeAdapter = function(desktop) {
  scout.DesktopBenchKeyStrokeAdapter.parent.call(this, {
    'keyStrokes': desktop.keyStrokes,
    '$container': desktop.$bench
  });
};
scout.inherits(scout.DesktopBenchKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
