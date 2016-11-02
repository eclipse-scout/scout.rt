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
scout.NumberColumn = function() {
  scout.NumberColumn.parent.call(this);
  this.uiSortPossible = true;
  this.filterType = 'NumberColumnUserFilter';
  this.comparator = scout.comparators.NUMERIC;
};
scout.inherits(scout.NumberColumn, scout.Column);

/**
 * @override Column.js
 */
scout.NumberColumn.prototype.init = function(model) {
  scout.NumberColumn.parent.prototype.init.call(this, model);
  if (!(this.decimalFormat instanceof scout.DecimalFormat)) {
    this.decimalFormat = new scout.DecimalFormat(this.session.locale, this.decimalFormat);
  }
};

/**
 * @override Column.js
 */
scout.NumberColumn.prototype._initCell = function(cell) {
  scout.NumberColumn.parent.prototype._initCell.call(this, cell);
  // server sends cell.value only if it differs from text -> make sure cell.value is set and has the right type
  // Cell.value may be undefined for other column types -> use table.cellValue to access the value.
  // The only reason is to save some memory (may get obsolete in the future)
  if (cell.value === undefined && cell.text) { // Number('') would generate 0 -> don't set in that case
    cell.value = Number(cell.text);
  }
};

/**
 * Override this method to create a cell model object based on the given scalar value.
 */
scout.NumberColumn.prototype._createCellModel = function(text) {
  var formattedNumber = this.decimalFormat.format(text);
  return {
    text: formattedNumber,
    value: text
  };
};

scout.NumberColumn.prototype.createAggrValueCell = function(value) {
  var formattedValue = this.decimalFormat.format(value);
  return this.initCell({
    text: formattedValue,
    iconId: (formattedValue ? this.aggrSymbol : null),
    horizontalAlignment: this.horizontalAlignment,
    cssClass: 'table-aggregate-cell'
  });
};

/**
 * @override Column.js
 */
scout.NumberColumn.prototype.cellValueForGrouping = function(row) {
  var cell = this.table.cell(this, row);
  return this._preprocessValueForGrouping(cell.value);
};

/**
 * @override Column.js
 */
scout.NumberColumn.prototype._preprocessValueForGrouping = function(value) {
  return this.decimalFormat.round(value);
};
