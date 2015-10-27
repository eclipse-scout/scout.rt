scout.CellEditorTabKeyStroke = function(popup) {
  scout.CellEditorTabKeyStroke.parent.call(this);
  this.field = popup;
  this.which = [scout.keys.TAB];
  this.shift = undefined; // to tab forward and backward
};
scout.inherits(scout.CellEditorTabKeyStroke, scout.KeyStroke);

scout.CellEditorTabKeyStroke.prototype._accept = function(event) {
  var accepted = scout.CellEditorTabKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && !this.field.completeEditRequested; // Make sure events (complete, prepare) don't get sent twice since it will lead to exceptions. This may happen if user presses and holds the tab key.
};

scout.CellEditorTabKeyStroke.prototype.handle = function(event) {
  var pos,
    backwards = event.shiftKey,
    table = this.field.table,
    column = this.field.column,
    row = this.field.row;

  this.field.completeEdit();
  this.field.completeEditRequested = true;

  pos = table.nextEditableCellPos(column, row, backwards);
  if (pos) {
    table.prepareCellEdit(pos.row.id, pos.column.id);
  }
};
