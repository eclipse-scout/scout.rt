scout.CellEditorPopup = function(column, row, cell) {
  scout.CellEditorPopup.parent.call(this);
  this.table = column.table;
  this.column = column;
  this.row = row;
  this.cell = cell;
  this.keyStrokeAdapter = new scout.CellEditorPopupKeyStrokeAdapter(this);
};
scout.inherits(scout.CellEditorPopup, scout.Popup);

scout.CellEditorPopup.prototype.render = function($parent) {
  scout.CellEditorPopup.parent.prototype.render.call(this, $parent);
  var offsetBounds,
    field = this.cell.field,
    $cell = this.table.$cell(this.column, this.row.$row);

  field.render(this.$container);
  offsetBounds = scout.graphics.offsetBounds($cell);
  this.setLocation(new scout.Point(offsetBounds.x, offsetBounds.y));
  scout.graphics.setSize(this.$container, offsetBounds.width, offsetBounds.height);
  scout.graphics.setSize(field.$container, offsetBounds.width, offsetBounds.height);
  scout.HtmlComponent.get(field.$container).layout();

  scout.keyStrokeManager.installAdapter(this.$container, this.keyStrokeAdapter);
  setTimeout(function() {
    this.$container.installFocusContext('auto');
  }.bind(this), 0);
};

scout.CellEditorPopup.prototype._remove = function($parent) {
  scout.CellEditorPopup.parent.prototype._remove($parent);
  scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
};

scout.CellEditorPopup.prototype._attachCloseHandler = function() {
  //FIXME CGU merge with popup.js
  if (this.$origin) {
    scout.scrollbars.attachScrollHandlers(this.$origin, this.remove.bind(this));
  }
};

scout.CellEditorPopup.prototype.completeEdit = function() {
  var field = this.cell.field;
  this.table.sendCompleteCellEdit(field.id);
  //FIXME CGU what if there is a validation error?
  this.cell.field.destroy();
  this.remove();
};

scout.CellEditorPopup.prototype.cancelEdit = function() {
  this.table.sendCancelCellEdit(this.cell.field.id);
  this.cell.field.destroy();
  this.remove();
};
