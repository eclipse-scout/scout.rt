scout.GroupBoxKeyStrokeAdapter = function(groupBox) {
  scout.GroupBoxKeyStrokeAdapter.parent.call(this, groupBox);
};
scout.inherits(scout.GroupBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.GroupBoxKeyStrokeAdapter.prototype.registerKeyStroke = function(keyStroke) {
  if (this._srcElement.mainBox) {
    scout.GroupBoxKeyStrokeAdapter.parent.prototype.registerKeyStroke.call(this, keyStroke);
  } else {
    this._srcElement.registerRootKeyStroke(keyStroke);
  }
};

scout.GroupBoxKeyStrokeAdapter.prototype.unregisterKeyStroke = function(keyStroke) {
  if (this._srcElement.mainBox) {
    scout.GroupBoxKeyStrokeAdapter.parent.prototype.unregisterKeyStroke.call(this, keyStroke);
  } else {
    this._srcElement.unregisterRootKeyStroke(keyStroke);
  }
};
