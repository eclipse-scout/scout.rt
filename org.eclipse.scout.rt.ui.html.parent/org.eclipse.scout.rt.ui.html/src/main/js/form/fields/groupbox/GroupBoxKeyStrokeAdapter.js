scout.GroupBoxKeyStrokeAdapter = function(field) {
  scout.GroupBoxKeyStrokeAdapter.parent.call(this, field);
};

scout.inherits(scout.GroupBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.GroupBoxKeyStrokeAdapter.prototype.registerKeyStroke = function(keyStroke) {
  if (this._field.mainBox) {
    scout.GroupBoxKeyStrokeAdapter.parent.prototype.registerKeyStroke.call(this, keyStroke);
  } else {
    this._field.getForm().rootGroupBox.keyStrokeAdapter.registerKeyStroke(keyStroke);
  }
};

scout.GroupBoxKeyStrokeAdapter.prototype.unregisterKeyStroke = function(keyStroke) {
  if (this._field.mainBox) {
    scout.GroupBoxKeyStrokeAdapter.parent.prototype.unregisterKeyStroke.call(this, keyStroke);
  } else {
    this._field.getForm().rootGroupBox.keyStrokeAdapter.unregisterKeyStroke(keyStroke);
  }
};
