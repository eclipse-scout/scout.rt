scout.DesktopTableKeyStrokeAdapter = function(field) {
  scout.DesktopTableKeyStrokeAdapter.parent.call(this, field);
  this.alt = true;
};
scout.inherits(scout.DesktopTableKeyStrokeAdapter, scout.TableKeyStrokeAdapter);


scout.DesktopTableKeyStrokeAdapter.prototype.accept = function(event) {

  //accept events if focus is on scout div or if focus is in filter input. Filter input prevents bubble up on all default input keystrokes.
  var activeElement = document.activeElement;
  if( $('.glasspane').length===0 &&
      this.$target[0] === activeElement || activeElement.className ==='control-filter'){
    return true;
  }
  return false;
};
