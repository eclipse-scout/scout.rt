scout.TreeKeyStrokeAdapter = function(field) {
  scout.TreeKeyStrokeAdapter.parent.call(this, field);

  this.keyStrokes.push(new scout.TreeControlKeyStrokes(field));
};
scout.inherits(scout.TreeKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
