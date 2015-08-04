scout.TreeKeyStrokeAdapter = function(tree) {
  scout.TreeKeyStrokeAdapter.parent.call(this, tree);
  this.registerKeyStroke(new scout.TreeControlKeyStrokes(tree));
};
scout.inherits(scout.TreeKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.TreeKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  scout.TreeKeyStrokeAdapter.parent.prototype.installModelKeystrokes.call(this);
  this.keyStrokes = this.keyStrokes.concat(this._srcElement.menus);
};
