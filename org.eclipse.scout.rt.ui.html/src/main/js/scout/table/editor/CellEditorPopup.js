scout.CellEditorPopup = function(column, row, cell, session) {
  scout.CellEditorPopup.parent.call(this, session);
  this.table = column.table;
  this.column = column;
  this.row = row;
  this.cell = cell;
};
scout.inherits(scout.CellEditorPopup, scout.Popup);

scout.CellEditorPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.CellEditorPopupKeyStrokeAdapter(this);
};

scout.CellEditorPopup.prototype._render = function($parent) {
  scout.CellEditorPopup.parent.prototype._render.call(this, $parent);
  var field = this.cell.field,
    firstCell = this.table.columns.indexOf(this.column) === 0;
  this.$container.addClass('cell-editor-popup');
  this.$container.data('popup', this);
  if (firstCell) {
    this.$container.addClass('first');
  }
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.CellEditorPopupLayout(this));

  // Sets a hint on the field that that field was opened as cell editor.
  // Additionally, sets the property openFieldPopupOnCellEdit to indicate that the a popup should be opened immediately after it has been rendered.
  // The field should use setTimeout() to open the popup, because the editor-field
  // itself is in the middle of rendering and thus the popup of the editor-field
  // cannot position itself correctly
  field.cellEditor = {
    openFieldPopupOnCellEdit: this.table.openFieldPopupOnCellEdit
  };
  field.render(this.$container);
  field.prepareForCellEdit({
    firstCell: firstCell,
    cellHorizontalAlignment: scout.Table.parseHorizontalAlignment(this.cell.horizontalAlignment)
  });
  this.addChild(field);

  // Make sure cell content is not visible while the editor is open (especially necessary for transparent editors like checkboxes)
  this.$anchor.css('visibility', 'hidden');

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
  // table may have been removed in the meantime
  if (this.table.rendered) {
    this.table.$container.removeClass('focused');
  }
  this.$anchor.css('visibility', '');
};

scout.CellEditorPopup.prototype.alignTo = function() {
  var cellBounds, rowBounds,
    $tableData = this.table.$data,
    $row = this.row.$row,
    $cell = this.$anchor;

  cellBounds = scout.graphics.bounds($cell, false, true);
  rowBounds = scout.graphics.bounds($row, false, true);
  this.setLocation(new scout.Point(cellBounds.x, $tableData.scrollTop() + rowBounds.y));
};

scout.CellEditorPopup.prototype.completeEdit = function() {
  var field = this.cell.field;

  // There is no blur event when the popup gets closed -> trigger blur so that the field may react (accept display text, close popups etc.)
  field.displayTextChanged();

  this.table.sendCompleteCellEdit(field.id);
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
