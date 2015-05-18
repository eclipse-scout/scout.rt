scout.DesktopNavigationKeyStrokeAdapter = function(desktopNavigation) {
  scout.DesktopNavigationKeyStrokeAdapter.parent.call(this, desktopNavigation);

  this.keyStrokes.push(new scout.DesktopNavigationKeyStroke(desktopNavigation));
};
scout.inherits(scout.DesktopNavigationKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.DesktopNavigationKeyStrokeAdapter.prototype.accept = function(event) {
  //accept events if focus is on scout div or if focus is in filter input. Filter input prevents bubble up on all default input keystrokes.
  if ($('div.glasspane').length === 0) {
    return true;
  }
  return false;
};
