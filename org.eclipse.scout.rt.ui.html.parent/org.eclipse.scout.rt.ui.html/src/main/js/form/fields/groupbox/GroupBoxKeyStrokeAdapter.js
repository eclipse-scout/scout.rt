scout.GroupBoxKeyStrokeAdapter = function(field) {
  scout.GroupBoxKeyStrokeAdapter.parent.call(this, field);
};

scout.inherits(scout.GroupBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.GroupBoxKeyStrokeAdapter.prototype.registerKeyStroke = function(keyStroke) {
  this.keyStrokes.push(keyStroke);
};
