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
  this.format;
  this.groupFormat;
  this.groupFormatFormatter;
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
scout.DateColumn.prototype._formatValue = function(value) {
  return scout.dates.format(value, this.session.locale, this._createDatePattern());
};

/**
 * @override Columns.js
 */
scout.DateColumn.prototype._parseValue = function(text) {
  return scout.dates.ensure(text);
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

scout.DateColumn.prototype.cellTextForGrouping = function(row) {
  if (this.groupFormat === undefined || this.groupFormat === this.format || !this.groupFormatFormatter) {
    // fallback/shortcut, if no groupFormat defined or groupFormat equals format use cellText
    return scout.DateColumn.parent.prototype.cellTextForGrouping.call(this, row);
  }

  var val = this.table.cellValue(this, row);
  return this.groupFormatFormatter.format(val);
};
