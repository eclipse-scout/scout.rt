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
/**
 * May be an ordinary boolean column or the table's checkable column (table.checkableColumn)
 * Difference: the table's checkable column represents the row.checked state, other boolean columns represent their own value.
 */
scout.BooleanColumn = function() {
  scout.BooleanColumn.parent.call(this);
  this.minWidth = scout.Column.NARROW_MIN_WIDTH;
  this.filterType = 'ColumnUserFilter';
  this.comparator = scout.comparators.NUMERIC;
};
scout.inherits(scout.BooleanColumn, scout.Column);

/**
 * @override
 */
scout.BooleanColumn.prototype.buildCell = function(cell, row) {
  var style, content, tooltipText, tooltip, cssClass, checked, checkBoxCssClass;
  var enabled = row.enabled;
  if (cell.empty) {
    // if cell wants to be really empty (e.g. no checkbox icon, use logic of base class)
    return scout.BooleanColumn.parent.prototype.buildCell.call(this, cell, row);
  }

  checked = cell.value;
  enabled = enabled && cell.editable;
  cssClass = this._cellCssClass(cell);
  style = this._cellStyle(cell);
  if (!enabled) {
    cssClass += ' disabled';
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

  return '<div class="' + cssClass + '" style="' + style + '"' + tooltip + scout.device.unselectableAttribute.string + '>' + content + '</div>';
};

scout.BooleanColumn.prototype.$checkBox = function($row) {
  var $cell = this.table.$cell(this, $row);
  return $cell.children('.check-box');
};

scout.BooleanColumn.prototype._cellCssClass = function(cell) {
  var cssClass = scout.BooleanColumn.parent.prototype._cellCssClass.call(this, cell);
  cssClass = cssClass.replace(' editable', '');
  cssClass += ' checkable';
  return cssClass;
};

scout.BooleanColumn.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row');

  if (this.table.checkableColumn === this) {
    this.table.checkRow(row, !row.checked);
  } else {
    // editable column behaviour -> server will handle the click, see AbstractTable#interceptRowClickSingleObserver
    // don't call super, no need to send a prepareEdit
  }
};
