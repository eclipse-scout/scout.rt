scout.CheckBoxColumn = function() {
  scout.CheckBoxColumn.parent.call(this);
};
scout.inherits(scout.CheckBoxColumn, scout.Column);

scout.CheckBoxColumn.prototype.buildCell = function(row) {
  var cell, style, content, tooltipText, tooltip, cssClass, checked;
  var enabled = this.table.enabled && row.enabled;
  if (this.isCheckableColumn()) {
    checked = row.checked;
  } else {
    cell = this.table.cell(this.index, row);
    checked = cell.value;
    enabled = enabled & cell.editable;
  }
  style = this.table.cellStyle(this, row);
  cssClass = this._cssClass(row, cell);
  tooltipText = this.table.cellTooltipText(this, row);
  tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');

  content = '<input type="checkbox"';
  if (checked) {
    content += ' checked="checked" ';
  }
  if (!enabled) {
    content += ' disabled="disabled" ';
  }
  content += '/><label>&nbsp;</label>';

  return '<div class="' + cssClass + '" style="' + style + '"' + tooltip + scout.device.unselectableAttribute + '>' + content + '</div>';
};

scout.CheckBoxColumn.prototype.$checkBox = function($row) {
  var $cell = this.table.$cell(this, $row);
  return $cell.children('input');
};

scout.CheckBoxColumn.prototype._cssClass = function(row) {
  var cssClass = scout.CheckBoxColumn.parent.prototype._cssClass.call(this, row);
  cssClass = cssClass.replace(' editable', '');
  cssClass += ' checkable';
  return cssClass;
};

scout.CheckBoxColumn.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row'),
    $target = $(event.target);

  if (this.isCheckableColumn()) {
    if ($target.is('label') && $target.parent().hasClass('checkable')) {
      this.table.checkRow(row, !row.checked);
    }
  } else {
    // editable column behaviour -> server will handle the click, see AbstractTable#interceptRowClickSingleObserver
    // don't call super, no need to send a prepareEdit
  }
};

/**
 *
 * @returns true if it is the table's checkable column, false if it is just a boolean column.
 * Difference: the table's checkable column represents the row.checked state, other boolean columns represent their own value.
 */
scout.CheckBoxColumn.prototype.isCheckableColumn = function() {
  return this.table.checkableColumn === this;
};

//scout.CheckBoxColumn.prototype.startCellEdit = function(row, fieldId) {
//FIXME CGU create checkbox celleditor popup, copy table-cell into popup and add space-listener.
//};
