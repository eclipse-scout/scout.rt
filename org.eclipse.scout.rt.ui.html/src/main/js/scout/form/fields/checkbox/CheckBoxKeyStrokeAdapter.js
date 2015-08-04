scout.CheckBoxKeyStrokeAdapter = function(checkBoxField) {
  scout.CheckBoxKeyStrokeAdapter.parent.call(this, checkBoxField);
  this.registerKeyStroke(new scout.CheckBoxEnterKeyStroke(checkBoxField));
  this.registerKeyStroke(new scout.CheckBoxSpaceKeyStroke(checkBoxField));
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

scout.CheckBoxSpaceKeyStroke = function(checkbox) {
  scout.CheckBoxSpaceKeyStroke.parent.call(this, checkbox);
  this.keyStroke = 'SPACE';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.CheckBoxSpaceKeyStroke, scout.CheckBoxEnterKeyStroke);
