scout.CellEditorPopup = function(column, row, cell, session) {
  scout.CellEditorPopup.parent.call(this, session);
  this.table = column.table;
  this.column = column;
  this.row = row;
  this.cell = cell;
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
};
scout.inherits(scout.CellEditorPopup, scout.Popup);

scout.CellEditorPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.CellEditorPopupKeyStrokeAdapter(this);
};

scout.CellEditorPopup.prototype._render = function($parent) {
  scout.CellEditorPopup.parent.prototype._render.call(this, $parent);
  this.$container.addClass('cell-editor-popup');
  this.$container.data('popup', this);
  this.$body.addClass('cell-editor-popup-body');
  var offsetBounds, rowOffsetBounds,
    field = this.cell.field,
    $row = this.row.$row,
    $cell = this.table.$cell(this.column, $row);

  field.render(this.$container);
  field.$container.addClass('cell-editor');

  // remove mandatory and status indicators (popup should 'fill' the whole cell)
  if (field.$mandatory) {
    field.$mandatory.remove();
    field.$mandatory = null;
  }
  if (field.$status) {
    field.$status.remove();
    field.$status = null;
  }

  offsetBounds = scout.graphics.offsetBounds($cell);
  rowOffsetBounds = scout.graphics.offsetBounds($row);
  this.setLocation(new scout.Point(offsetBounds.x, rowOffsetBounds.y));
  scout.graphics.setSize(this.$container, offsetBounds.width, rowOffsetBounds.height);
  scout.graphics.setSize(field.$container, offsetBounds.width, rowOffsetBounds.height);
  scout.HtmlComponent.get(field.$container).layout();
};

scout.CellEditorPopup.prototype._remove = function() {
  this.cell.field.remove();
};

scout.CellEditorPopup.prototype._onMouseDownOutside = function(event) {
  this.completeEdit();
};

scout.CellEditorPopup.prototype.completeEdit = function() {
  var field = this.cell.field;

  // There is no blur event when the popup gets closed -> trigger blur so that the field may react (accept display text, close popups etc.)
  var $activeElement = $(document.activeElement);
  $activeElement.blur();

  this.table.sendCompleteCellEdit(field.id);
  this.remove();
};

scout.CellEditorPopup.prototype.cancelEdit = function() {
  this.table.sendCancelCellEdit(this.cell.field.id);
  this.remove();
};
