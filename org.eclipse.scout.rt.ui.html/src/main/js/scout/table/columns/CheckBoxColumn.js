/**
 * May be an ordinary boolean column or the table's checkable column (table.checkableColumn)
 * Difference: the table's checkable column represents the row.checked state, other boolean columns represent their own value.
 */
scout.CheckBoxColumn = function() {
  scout.CheckBoxColumn.parent.call(this);
  this.minWidth = scout.Column.NARROW_MIN_WIDTH;
};
scout.inherits(scout.CheckBoxColumn, scout.Column);

/**
 * @override
 */
scout.CheckBoxColumn.prototype.buildCell = function(cell, row) {
  var style, content, tooltipText, tooltip, cssClass, checked, checkBoxCssClass;
  var enabled = row.enabled;
  if (cell.empty) {
    // if cell wants to be really empty (e.g. no checkbox icon, use logic of base class)
    return scout.CheckBoxColumn.parent.prototype.buildCell.call(this, cell, row);
  }

  checked = cell.value;
  enabled = enabled && cell.editable;
  cssClass = this._cellCssClass(cell);
  style = this._cellStyle(cell);
  if (!enabled) {
    cssClass +=' disabled';
  }
  tooltipText = cell.tooltipText;
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

scout.CheckBoxColumn.prototype._cellCssClass = function(cell) {
  var cssClass = scout.CheckBoxColumn.parent.prototype._cellCssClass.call(this, cell);
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
