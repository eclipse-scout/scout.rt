/**
 * May be an ordinary boolean column or the table's checkable column (table.checkableColumn)
 * Difference: the table's checkable column represents the row.checked state, other boolean columns represent their own value.
 */
scout.CheckBoxColumn = function() {
  scout.CheckBoxColumn.parent.call(this);
};
scout.inherits(scout.CheckBoxColumn, scout.Column);

scout.CheckBoxColumn.prototype.buildCell = function(row) {
  var cell, style, content, tooltipText, tooltip, cssClass, checked, checkBoxCssClass;
  var enabled = row.enabled;
  cell = this.table.cell(this, row);
  checked = cell.value;
  enabled = enabled && cell.editable;
  style = this.table.cellStyle(this, cell);
  cssClass = this._cssClass(row, cell);
  if (!enabled) {
    cssClass +=' disabled';
  }
  tooltipText = this.table.cellTooltipText(this, row);
  tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');

  checkBoxCssClass = 'check-box';
  if (checked) {
    checkBoxCssClass += ' checked';
  }
  if (!enabled) {
    checkBoxCssClass += ' disabled';
  }
  content = '<div class="' + checkBoxCssClass + '"/>';

  return '<div class="' + cssClass + '" style="' + style + '"' + tooltip + scout.device.unselectableAttribute + '>' + content + '</div>';
};

scout.CheckBoxColumn.prototype.$checkBox = function($row) {
  var $cell = this.table.$cell(this, $row);
  return $cell.children('.check-box');
};

scout.CheckBoxColumn.prototype._cssClass = function(row, cell) {
  var cssClass = scout.CheckBoxColumn.parent.prototype._cssClass.call(this, row, cell);
  cssClass = cssClass.replace(' editable', '');
  cssClass += ' checkable';
  return cssClass;
};

scout.CheckBoxColumn.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row'),
    $target = $(event.target);

  if (this.table.checkableColumn === this) {
    this.table.checkRow(row, !row.checked);
  } else {
    // editable column behaviour -> server will handle the click, see AbstractTable#interceptRowClickSingleObserver
    // don't call super, no need to send a prepareEdit
  }
};
