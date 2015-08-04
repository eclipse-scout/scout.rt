scout.DesktopTableKeyStrokeAdapter = function(field) {
  scout.DesktopTableKeyStrokeAdapter.parent.call(this, field);
};
scout.inherits(scout.DesktopTableKeyStrokeAdapter, scout.TableKeyStrokeAdapter);

scout.DesktopTableKeyStrokeAdapter.prototype.accept = function(event) {

  //accept events if focus is on scout div or if focus is in filter input. Filter input prevents bubble up on all default input keystrokes.
  var activeElement = document.activeElement;
  if ($('glasspane').length === 0 &&
     event.target === activeElement || activeElement.className === 'control-filter' ||
    (this._field.$container && (this._field.$container[0] === activeElement || this._field.$container[0].contains(activeElement)))) {
    return true;
  }
  return false;
};
