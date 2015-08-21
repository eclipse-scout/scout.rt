scout.CellEditorCompleteEditKeyStroke = function(popup) {
  scout.CellEditorCompleteEditKeyStroke.parent.call(this);
  this.field = popup;
  this.which = [scout.keys.ENTER];
  this.stopPropagation = true;
};
scout.inherits(scout.CellEditorCompleteEditKeyStroke, scout.KeyStroke);

scout.CellEditorCompleteEditKeyStroke.prototype.handle = function(event) {
  this.field.completeEdit();
};
