scout.StringFieldCtrlEnterKeyStroke = function(stringField) {
  scout.StringFieldCtrlEnterKeyStroke.parent.call(this);
  this.field = stringField;
  this.which = [scout.keys.ENTER];
  this.ctrl = true;
};
scout.inherits(scout.StringFieldCtrlEnterKeyStroke, scout.KeyStroke);

scout.StringFieldCtrlEnterKeyStroke.prototype._accept = function(event) {
  var accepted = scout.StringFieldCtrlEnterKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && this.field.hasAction;
};

scout.StringFieldCtrlEnterKeyStroke.prototype.handle = function(event) {
  this.field._onIconClick();
};
