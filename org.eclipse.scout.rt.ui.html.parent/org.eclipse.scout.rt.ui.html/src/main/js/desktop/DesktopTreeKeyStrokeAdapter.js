scout.DesktopTreeKeyStrokeAdapter = function(field) {
  scout.DesktopTreeKeyStrokeAdapter.parent.call(this, field);

  var keyStroke = new scout.TreeControlKeyStrokes(field);
  keyStroke.ctrl=true;
  keyStroke.shift = true;
  this.keyStrokes.push(keyStroke);
};
scout.inherits(scout.DesktopTreeKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);


scout.DesktopTreeKeyStrokeAdapter.prototype.accept = function(event) {
  if($('.glasspane').length>0){
    return false;
  }
  return true;
};
