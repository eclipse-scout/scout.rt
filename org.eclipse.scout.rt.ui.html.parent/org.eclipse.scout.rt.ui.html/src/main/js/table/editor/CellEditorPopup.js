scout.CellEditorPopup = function(column, row, cell) {
  scout.CellEditorPopup.parent.call(this);
  this._table = column.table;
  this._column = column;
  this._row = row;
  this._cell = cell;
  this.keyStrokeAdapter = new scout.CellEditorPopupKeyStrokeAdapter(this);
};
scout.inherits(scout.CellEditorPopup, scout.Popup);

scout.CellEditorPopup.prototype.render = function($parent) {
  scout.CellEditorPopup.parent.prototype.render.call(this, $parent);
  var offsetBounds,
    field = this._cell.field,
    $cell = this._table.$cell(this._column.index, this._row.$row);

  field.render(this.$container);
  offsetBounds = scout.graphics.offsetBounds($cell);
  this.setLocation(new scout.Point(offsetBounds.x, offsetBounds.y));
  scout.graphics.setSize(this.$container, offsetBounds.width, offsetBounds.height);
  scout.graphics.setSize(field.$container, offsetBounds.width, offsetBounds.height);
  scout.HtmlComponent.get(field.$container).layout();

  scout.keyStrokeManager.installAdapter(this.$container, this.keyStrokeAdapter);
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
  var field = this._cell.field;
  if (field instanceof scout.ValueField) {
    // FIXME CGU maybe make sure field calls this when pressing enter.
    field.acceptDisplayText();
  }
  this._table.sendCompleteCellEdit(field.id);
  //FIXME CGU what if there is a validation error?
  this._cell.field.destroy();
  this.remove();
};

scout.CellEditorPopup.prototype.cancelEdit = function() {
  this._table.sendCancelCellEdit(this._cell.field.id);
  this._cell.field.destroy();
  this.remove();
};
