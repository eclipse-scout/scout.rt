scout.MessageBoxKeyStrokeAdapter = function(field) {
  scout.MessageBoxKeyStrokeAdapter.parent.call(this, field);
  this.uiSessionId(field.session.uiSessionId);
  this.registerKeyStroke(new scout.MessageBoxControlKeyStrokes(field));
  this.anchorKeyStrokeAdapter = true;
};
scout.inherits(scout.MessageBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
