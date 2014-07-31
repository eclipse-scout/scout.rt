scout.NullAdapter = function() {
  scout.NullAdapter.parent.call(this);
};

scout.inherits(scout.NullAdapter, scout.ModelAdapter);

scout.NullAdapter.prototype._setSelected = function(selected) {
  // nop
};
