/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Column, comparators, DateField, DateFormat, dates, scout} from '../../index';

export default class DateColumn extends Column {

  constructor() {
    super();
    this.format = null;
    this.groupFormat = 'yyyy';
    this.hasDate = true;
    this.hasTime = false;
    this.filterType = 'DateColumnUserFilter';
    this.comparator = comparators.NUMERIC;
    this.textBased = false;
  }

  _init(model) {
    super._init(model);

    this._setFormat(this.format);
    this._setGroupFormat(this.groupFormat);
  }

  setFormat(format) {
    this.setProperty('format', format);
  }

  _setFormat(format) {
    if (!format) {
      format = this._getDefaultFormat(this.session.locale);
    }
    format = DateFormat.ensure(this.session.locale, format);
    this._setProperty('format', format);
    if (this.initialized) {
      // if format changes on the fly, just update the cell text
      this.table.rows.forEach(row => {
        this._updateCellText(row, this.cell(row));
      });
    }
  }

  setGroupFormat(groupFormat) {
    this.setProperty('groupFormat', groupFormat);
  }

  _setGroupFormat(format) {
    if (!format) {
      format = this._getDefaultFormat(this.session.locale);
    }
    format = DateFormat.ensure(this.session.locale, format);
    this._setProperty('groupFormat', format);
    if (this.initialized) {
      this.table._group();
    }
  }

  /**
   * @override Columns.js
   */
  _formatValue(value, row) {
    return this.format.format(value);
  }

  /**
   * @override Columns.js
   */
  _parseValue(text) {
    return dates.ensure(text);
  }

  _getDefaultFormat(locale) {
    if (this.hasDate && this.hasTime) {
      return locale.dateFormatPatternDefault + ' ' + locale.timeFormatPatternDefault;
    }
    if (this.hasDate) {
      return locale.dateFormatPatternDefault;
    }
    return locale.timeFormatPatternDefault;
  }

  cellTextForGrouping(row) {
    let val = this.table.cellValue(this, row);
    return this.groupFormat.format(val);
  }

  /**
   * @override Column.js
   */
  _createEditor(row) {
    return scout.create(DateField, {
      parent: this.table,
      hasDate: this.hasDate,
      hasTime: this.hasTime
    });
  }
}
