/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.CellEditorPopup = function() {
  scout.CellEditorPopup.parent.call(this);
  this.table;
  this.column;
  this.row;
  this.cell;
};
scout.inherits(scout.CellEditorPopup, scout.Popup);

scout.CellEditorPopup.prototype._init = function(options) {
  options.scrollType = options.scrollType || 'position';
  scout.CellEditorPopup.parent.prototype._init.call(this, options);

  this.table = options.column.table;
  this.column = options.column;
  this.row = options.row;
  this.cell = options.cell;
  this.link(this.cell.field);
};

scout.CellEditorPopup.prototype._createLayout = function() {
  return new scout.CellEditorPopupLayout(this);
};

/**
 * @override
 */
scout.CellEditorPopup.prototype._initKeyStrokeContext = function() {
  scout.CellEditorPopup.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([
    new scout.CellEditorCompleteEditKeyStroke(this),
    new scout.CellEditorCancelEditKeyStroke(this),
    new scout.CellEditorTabKeyStroke(this)
  ]);
};

/**
 * @override
 */
scout.CellEditorPopup.prototype._open = function($parent, event) {
  this.render($parent, event);
  this.position();
  this.pack();
};

scout.CellEditorPopup.prototype._render = function($parent) {
  scout.CellEditorPopup.parent.prototype._render.call(this, $parent);

  var firstCell = this.table.columns.indexOf(this.column) === 0;
  this.$container.addClass('cell-editor-popup');
  this.$container.data('popup', this);
  if (firstCell) {
    this.$container.addClass('first');
  }

  var field = this.cell.field;
  field.mode = scout.FormField.MODE_CELLEDITOR; // hint that this field is used within a cell-editor
  field.render(this.$container);
  field.prepareForCellEdit({
    firstCell: firstCell
  });

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
  if (this.table.enabled) {
    this.table.$container.addClass('focused');
  }
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
    $cell = this.$anchor,
    insetsLeft = $tableData.cssPxValue('padding-left') + $row.cssBorderLeftWidth();

  cellBounds = scout.graphics.bounds($cell, false, true);
  rowBounds = scout.graphics.bounds($row, false, true);
  this.setLocation(new scout.Point(insetsLeft + cellBounds.x, $tableData.scrollTop() + rowBounds.y));
};

scout.CellEditorPopup.prototype.completeEdit = function() {
  var field = this.cell.field;
  if (this.completeCellEditRequested) {
    // Make sure complete cell edit does not get sent twice since it will lead to exceptions. This may happen if user clicks very fast multiple times.
    return;
  }

  // There is no blur event when the popup gets closed -> trigger blur so that the field may react (accept display text, close popups etc.)
  field.acceptInput();

  this.table._triggerCompleteCellEdit(field);
  this.completeCellEditRequested = true;
};

scout.CellEditorPopup.prototype.cancelEdit = function() {
  this.table._triggerCancelCellEdit(this.cell.field);
  this.remove();
};

scout.CellEditorPopup.prototype._onMouseDownOutside = function(event) {
  this.completeEdit();
};
