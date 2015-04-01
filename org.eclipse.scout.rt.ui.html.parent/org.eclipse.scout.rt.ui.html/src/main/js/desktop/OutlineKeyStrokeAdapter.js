scout.OutlineKeyStrokeAdapter = function(field) {
  scout.OutlineKeyStrokeAdapter.parent.call(this, field);

  var keyStroke = new scout.TreeControlKeyStrokes(field);
  keyStroke.ctrl = true;
  keyStroke.shift = true;
  this.keyStrokes.push(keyStroke);
};
scout.inherits(scout.OutlineKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.OutlineKeyStrokeAdapter.prototype.accept = function(event) {
  if ($('.glasspane').length > 0) {
    return false;
  }
  return true;
};
