scout.CellEditorCancelEditKeyStroke = function(popup) {
  scout.CellEditorCancelEditKeyStroke.parent.call(this);
  this.field = popup;
  this.which = [scout.keys.ESC];
  this.stopPropagation = true;
};
scout.inherits(scout.CellEditorCancelEditKeyStroke, scout.KeyStroke);

scout.CellEditorCancelEditKeyStroke.prototype.handle = function(event) {
  this.field.cancelEdit();
};
