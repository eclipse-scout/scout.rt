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
  // FIXME CGU move code for aggrFun and backgroundEffect from column.js to numberColumn.js?
  this.aggregationFunction = 'sum';
  this.backgroundEffect;
  this.decimalFormat;
  this.filterType = 'NumberColumnUserFilter';
  this.comparator = scout.comparators.NUMERIC;
};
scout.inherits(scout.NumberColumn, scout.Column);

/**
 * @override Column.js
 */
scout.NumberColumn.prototype._init = function(model) {
  scout.NumberColumn.parent.prototype._init.call(this, model);
  this._setDecimalFormat(this.decimalFormat);
};

scout.NumberColumn.prototype._setDecimalFormat = function(format) {
  if (!format) {
    format = this._getDefaultFormat(this.session.locale);
  }
  format = scout.DecimalFormat.ensure(this.session.locale, format);
  this.decimalFormat = format;
};

scout.NumberColumn.prototype._getDefaultFormat = function(locale) {
  return locale.decimalFormatPatternDefault;
};

/**
 * @override Columns.js
 */
scout.NumberColumn.prototype._formatValue = function(value) {
  return this.decimalFormat.format(value);
};

/**
 * @override Column.js
 */
scout.NumberColumn.prototype._parseValue = function(value) {
  // server sends cell.value only if it differs from text -> make sure cell.value is set and has the right type
  return scout.numbers.ensure(value);
};

scout.NumberColumn.prototype.createAggrValueCell = function(value) {
  var formattedValue = this._formatValue(value);
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
