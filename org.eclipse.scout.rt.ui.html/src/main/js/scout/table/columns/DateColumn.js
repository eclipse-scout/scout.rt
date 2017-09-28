/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
  this.groupFormat = 'yyyy';
  this.hasDate = true;
  this.hasTime = false;
  this.filterType = 'DateColumnUserFilter';
  this.comparator = scout.comparators.NUMERIC;
  this.textBased = false;
};
scout.inherits(scout.DateColumn, scout.Column);

scout.DateColumn.prototype._init = function(model) {
  scout.DateColumn.parent.prototype._init.call(this, model);

  this._setFormat(this.format);
  this._setGroupFormat(this.groupFormat);
};

scout.DateColumn.prototype._setFormat = function(format) {
  if (!format) {
    format = this._getDefaultFormat(this.session.locale);
  }
  format = scout.DateFormat.ensure(this.session.locale, format);
  this.format = format;
};

scout.DateColumn.prototype._setGroupFormat = function(format) {
  if (!format) {
    format = this._getDefaultFormat(this.session.locale);
  }
  format = scout.DateFormat.ensure(this.session.locale, format);
  this.groupFormat = format;
};

/**
 * @override Columns.js
 */
scout.DateColumn.prototype._formatValue = function(value) {
  return this.format.format(value);
};

/**
 * @override Columns.js
 */
scout.DateColumn.prototype._parseValue = function(text) {
  return scout.dates.ensure(text);
};

scout.DateColumn.prototype._getDefaultFormat = function(locale) {
  if (this.hasDate && this.hasTime) {
    return locale.dateFormatPatternDefault + ' ' + locale.timeFormatPatternDefault;
  }
  if (this.hasDate) {
    return locale.dateFormatPatternDefault;
  }
  return locale.timeFormatPatternDefault;
};

scout.DateColumn.prototype.cellTextForGrouping = function(row) {
  var val = this.table.cellValue(this, row);
  return this.groupFormat.format(val);
};
