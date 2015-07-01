scout.TreeKeyStrokeAdapter = function(field) {
  scout.TreeKeyStrokeAdapter.parent.call(this, field);
  this.keyStrokes.push(new scout.TreeControlKeyStrokes(field));
};
scout.inherits(scout.TreeKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.TreeKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  scout.TreeKeyStrokeAdapter.parent.prototype.installModelKeystrokes.call(this);
  this.keyStrokes = this.keyStrokes.concat(this._field.menus);
};
