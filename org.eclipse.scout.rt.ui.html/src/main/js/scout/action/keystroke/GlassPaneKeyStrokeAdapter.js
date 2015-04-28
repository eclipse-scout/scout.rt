scout.GlassPaneKeyStrokeAdapter = function($glasspane, _uiSessionId) {
  scout.GlassPaneKeyStrokeAdapter.parent.call(this);
  this._uiSessionId = _uiSessionId;
  this.anchorKeyStrokeAdapter=true;
};

scout.inherits(scout.GlassPaneKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.GlassPaneKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeyStrokes) {
  //nop
};

scout.GlassPaneKeyStrokeAdapter.prototype.removeKeyBox = function() {
  //nop
};
scout.AbstractKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  //nop
};

