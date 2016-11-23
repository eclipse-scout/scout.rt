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
scout.DateColumn = function() {
  scout.DateColumn.parent.call(this);
  this.hasDate = true;
  this.hasTime = false;
  this.filterType = 'DateColumnUserFilter';
  this.comparator = scout.comparators.NUMERIC;
};
scout.inherits(scout.DateColumn, scout.Column);

scout.DateColumn.DATE_PATTERN = 'dd.MM.yyyy';
scout.DateColumn.TIME_PATTERN = 'HH:mm';

scout.DateColumn.prototype._init = function(model) {
  this.groupFormatFormatter = new scout.DateFormat(this.session.locale, this.groupFormat);
};

/**
 * @override Columns.js
 */
scout.DateColumn.prototype._createCellModel = function(text) {
  var date = scout.dates.parseJsonDate(text);
  var formattedDate = scout.dates.format(date, this.session.locale, this._createDatePattern());
  return {
    text: formattedDate,
    value: date
  };
};

scout.DateColumn.prototype._createDatePattern = function() {
  if (this.hasDate && this.hasTime) {
    return scout.DateColumn.DATE_PATTERN + ' ' + scout.DateColumn.TIME_PATTERN;
  }
  if (this.hasDate) {
    return scout.DateColumn.DATE_PATTERN;
  }
  return scout.DateColumn.TIME_PATTERN;
};

/**
 * If cell.value is a Date instance or undefined we do nothing. If value is a String, we assume
 * its a JSON date string and convert it to a Date instance.
 * @override Column.js
 */
scout.DateColumn.prototype._initCell = function(cell) {
  scout.DateColumn.parent.prototype._initCell.call(this, cell);
  if (typeof cell.value === 'string') {
    cell.value = scout.dates.parseJsonDate(cell.value);
  }
};

scout.DateColumn.prototype.cellTextForGrouping = function(row) {
  if (this.groupFormat === undefined || this.groupFormat === this.format || !this.groupFormatFormatter) {
    // fallback/shortcut, if no groupFormat defined or groupFormat equals format use cellText
    return scout.DateColumn.parent.prototype.cellTextForGrouping.call(this, row);
  }

  var val = this.table.cellValue(this, row);
  return this.groupFormatFormatter.format(val);
};
