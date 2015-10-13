scout.CellEditorPopup = function() {
  scout.CellEditorPopup.parent.call(this);
  this.table;
  this.column;
  this.row;
  this.cell;
};
scout.inherits(scout.CellEditorPopup, scout.Popup);

scout.CellEditorPopup.prototype._init = function(options) {
  scout.CellEditorPopup.parent.prototype._init.call(this, options);

  this.table = options.column.table;
  this.column = options.column;
  this.row = options.row;
  this.cell = options.cell;
};

/**
 * @override Popup.js
 */
scout.CellEditorPopup.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.CellEditorPopup.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke([
    new scout.CellEditorCompleteEditKeyStroke(this),
    new scout.CellEditorCancelEditKeyStroke(this),
    new scout.CellEditorTabKeyStroke(this)
  ]);
};

scout.CellEditorPopup.prototype._render = function($parent) {
  scout.CellEditorPopup.parent.prototype._render.call(this, $parent);

  var firstCell = this.table.columns.indexOf(this.column) === 0;
  this.$container.addClass('cell-editor-popup');
  this.$container.data('popup', this);
  if (firstCell) {
    this.$container.addClass('first');
  }
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.CellEditorPopupLayout(this));

  var field = this.cell.field;
  field.mode = scout.FormField.MODE_CELLEDITOR; // hint that this field is used within a cell-editor
  field.render(this.$container);
  field.prepareForCellEdit({
    firstCell: firstCell,
    cellHorizontalAlignment: scout.Table.parseHorizontalAlignment(this.cell.horizontalAlignment)
  });
  field.setParent(this);

  // Make sure cell content is not visible while the editor is open (especially necessary for transparent editors like checkboxes)
  this.$anchor.css('visibility', 'hidden');

  this._rowOrderChangedFunc = function(event) {
    if (event.animating) {
      // row is only set while animating
      if (event.row === this.row) {
        this.position();
      }
    } else {
      this.position();
    }
  }.bind(this);
  this.table.events.on('rowOrderChanged', this._rowOrderChangedFunc);
  // Set table style to focused, so that it looks as it still has the focus.
  // This prevents flickering if the cell editor gets opened, especially when tabbing to the next cell editor.
  this.table.$container.addClass('focused');
};

scout.CellEditorPopup.prototype._postRender = function() {
  scout.CellEditorPopup.parent.prototype._postRender.call(this); // installs the focus context for this popup

  // If applicable, invoke the field's function 'onCellEditorRendered' to signal the cell-editor to be rendered.
  var field = this.cell.field;
  if (field.onCellEditorRendered) {
    field.onCellEditorRendered({
        openFieldPopup: this.table.openFieldPopupOnCellEdit
    });
  }
};

scout.CellEditorPopup.prototype._remove = function() {
  scout.CellEditorPopup.parent.prototype._remove.call(this); // uninstalls the focus context for this popup

  this.table.events.off('rowOrderChanged', this._rowOrderChangedFunc);
  // table may have been removed in the meantime
  if (this.table.rendered) {
    this.table.$container.removeClass('focused');
  }
  this.$anchor.css('visibility', '');
};

scout.CellEditorPopup.prototype.position = function() {
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
  field.acceptInput();

  this.table._sendCompleteCellEdit(field.id);
};

scout.CellEditorPopup.prototype.cancelEdit = function() {
  this.table._sendCancelCellEdit(this.cell.field.id);
  this.remove();
};

scout.CellEditorPopup.prototype._onMouseDownOutside = function(event) {
  this.completeEdit();
};

scout.CellEditorPopup.prototype._onAnchorScroll = function(event) {
  this.position();
};
