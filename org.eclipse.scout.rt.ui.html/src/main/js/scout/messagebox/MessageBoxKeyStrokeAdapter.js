scout.MessageBoxKeyStrokeAdapter = function(field) {
  scout.MessageBoxKeyStrokeAdapter.parent.call(this, field);
  this.uiSessionId(field._session.uiSessionId);
  this.keyStrokes.push(new scout.MessageBoxControlKeyStrokes(field));
  this.anchorKeyStrokeAdapter = true;
};
scout.inherits(scout.MessageBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
