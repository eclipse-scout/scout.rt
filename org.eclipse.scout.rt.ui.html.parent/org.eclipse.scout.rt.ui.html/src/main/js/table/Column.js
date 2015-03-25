scout.Column = function() {
};

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
  var style, text, tooltipText, tooltip;
  style = this.table.cellStyle(this, row);
  text = this.table.cellText(this, row);
  if (this.table.multilineText) {
    text = scout.strings.nl2br(text);
  }
  tooltipText = this.table.cellTooltipText(this, row);
  tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');

  return '<div class="table-cell" style="' + style + '"' + tooltip + scout.device.unselectableAttribute + '>' + text + '</div>';
};

scout.Column.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row'),
    cell = this.table.cell(this, row);

  if (cell.editable) {
    this.table.sendPrepareCellEdit(row.id, this.id);
  }
};

scout.Column.prototype.startCellEdit = function(row, fieldId) {
  var popup,
    cell = this.table.cell(this, row),
    $row = row.$row,
    $cell = this.table.$cell(this.index, $row);

  cell.field = this.session.getOrCreateModelAdapter(fieldId, this.table);
  popup = new scout.CellEditorPopup(this, row, cell);
  popup.$origin = this.$cell;
  popup.render();
};
