scout.CheckBoxKeyStrokeAdapter = function(field) {
  scout.CheckBoxKeyStrokeAdapter.parent.call(this, field);
  this.registerKeyStroke(new scout.CheckBoxEnterKeyStroke(field));
  this.registerKeyStroke(new scout.CheckBoxSpaceKeyStroke(field));
};
scout.inherits(scout.CheckBoxKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

scout.CheckBoxEnterKeyStroke = function(field) {
  scout.CheckBoxEnterKeyStroke.parent.call(this);
  this.keyStroke = 'ENTER';
  this.drawHint = false;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.CheckBoxEnterKeyStroke, scout.KeyStroke);

scout.CheckBoxEnterKeyStroke.prototype.handle = function() {
  this._field._toggleChecked();
};

scout.CheckBoxSpaceKeyStroke = function(field) {
  scout.CheckBoxSpaceKeyStroke.parent.call(this, field);
  this.keyStroke = 'SPACE';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.CheckBoxSpaceKeyStroke, scout.CheckBoxEnterKeyStroke);
