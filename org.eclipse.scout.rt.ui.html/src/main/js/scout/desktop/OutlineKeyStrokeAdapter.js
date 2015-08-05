scout.OutlineKeyStrokeAdapter = function(outline) {
  scout.OutlineKeyStrokeAdapter.parent.call(this, outline);

  var keyStroke = new scout.TreeControlKeyStrokes(outline);
  keyStroke.ctrl = true;
  keyStroke.shift = true;
  this.registerKeyStroke(keyStroke);
};
scout.inherits(scout.OutlineKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
