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
  var field = this.cell.field;
  this.$container.addClass('cell-editor-popup');
  this.$container.data('popup', this);
  this.$body.addClass('cell-editor-popup-body');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.CellEditorPopupLayout(this));

  field.render(this.$container);
  this.addChild(field);

  // remove mandatory and status indicators (popup should 'fill' the whole cell)
  if (field.$mandatory) {
    field.$mandatory.remove();
    field.$mandatory = null;
  }
  if (field.$status) {
    field.$status.remove();
    field.$status = null;
  }
  if (field.$container) {
    field.$container.addClass('cell-editor-form-field');
  }
  if (field.$fieldContainer) {
    field.$fieldContainer.css('text-align', scout.Table.parseHorizontalAlignment(this.cell.horizontalAlignment));
  }
  if (field.$field) {
    field.$field.addClass('cell-editor-field');
  }

  this._rowOrderChangedFunc = function(event) {
    if (event.animating) {
      // row is only set while animating
      if (event.row === this.row) {
        this.alignTo();
      }
    } else {
      this.alignTo();
    }
  }.bind(this);
  this.table.events.on(scout.Table.GUI_EVENT_ROW_ORDER_CHANGED, this._rowOrderChangedFunc);
  // Set table style to focused, so that it looks as it still has the focus.
  // This prevents flickering if the cell editor gets opened, especially when tabbing to the next cell editor.
  this.table.$container.addClass('focused');
};

scout.CellEditorPopup.prototype._remove = function() {
  scout.CellEditorPopup.parent.prototype._remove.call(this);
  this.table.events.off(scout.Table.GUI_EVENT_ROW_ORDER_CHANGED, this._rowOrderChangedFunc);
  this.table.$container.removeClass('focused');
};

scout.CellEditorPopup.prototype.alignTo = function(event) {
  var cellBounds, rowBounds,
    $tableData = this.table.$data,
    $row = this.row.$row,
    $cell = this.table.$cell(this.column, $row);

  cellBounds = scout.graphics.bounds($cell, false, true);
  rowBounds = scout.graphics.bounds($row, false, true);
  this.setLocation(new scout.Point($tableData.scrollLeft() + cellBounds.x, $tableData.scrollTop() + rowBounds.y));
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

scout.CellEditorPopup.prototype._onMouseDownOutside = function(event) {
  this.completeEdit();
};

scout.CellEditorPopup.prototype._onAnchorScroll = function(event) {
  this.alignTo();
};
