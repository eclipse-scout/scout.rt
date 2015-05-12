scout.Column = function() {};

scout.Column.prototype.init = function(model, session) {
  this.session = session;

  // Copy all properties from model to this
  $.extend(this, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);

  // InitialWidth is only sent if it differs from width
  if (this.initialWidth === undefined) {
    this.initialWidth = this.width;
  }
};

scout.Column.prototype.buildCell = function(row) {
  var style, text, tooltipText, tooltip, cssClass, cell;
  cell = this.table.cell(this.index, row);
  style = this.table.cellStyle(this, row);
  text = this.table.cellText(this, row);
  if (!cell.htmlEnabled) {
    text = scout.strings.encode(text);
  }
  if (this.table.multilineText) {
    text = scout.strings.nl2br(text, false);
  }
  cssClass = this._cssClass(row, cell);
  tooltipText = this.table.cellTooltipText(this, row);
  tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');
  if (cell.errorStatus) {
    row.hasError = true;
  }

  return '<div class="' + cssClass + '" style="' + style + '"' + tooltip + scout.device.unselectableAttribute + '>' + text + '</div>';
};

scout.Column.prototype._cssClass = function(row, cell) {
  var cssClass = 'table-cell';
  if (this.mandatory) {
    cssClass += ' mandatory';
  }
  if (!this.table.multilineText || !this.textWrap) {
    cssClass += ' white-space-nowrap';
  }
  if (!cell) {
    // gui only columns don't have cells
    return cssClass;
  }
  if (cell.editable) {
    cssClass += ' editable';
  }
  if (cell.errorStatus) {
    cssClass += ' has-error';
  }

  //TODO CGU cssClass is actually only sent for cells, should we change this in model? discuss with jgu
  if (cell.cssClass) {
    cssClass += ' ' + cell.cssClass;
  } else if (this.cssClass) {
    cssClass += ' ' + this.cssClass;
  }
  return cssClass;
};

scout.Column.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row'),
    cell = this.table.cell(this.index, row);

  if (this.table.enabled && row.enabled && cell.editable && !event.ctrlKey && !event.shiftKey) {
    this.table.sendPrepareCellEdit(row.id, this.id);
  }
};

scout.Column.prototype.startCellEdit = function(row, fieldId) {
  var popup,
    $row = row.$row,
    cell = this.table.cell(this.index, row),
    $cell = this.table.$cell(this, $row);

  cell.field = this.session.getOrCreateModelAdapter(fieldId, this.table);
  popup = new scout.CellEditorPopup(this, row, cell, this.session);
  popup.$anchor = $cell;
  popup.render(this.table.$data);
  return popup;
};
