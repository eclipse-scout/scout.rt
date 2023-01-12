/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, comparators, DateColumnEventMap, DateColumnModel, DateField, DateFormat, dates, InitModelOf, Locale, scout, TableRow} from '../../index';

export class DateColumn extends Column<Date> implements DateColumnModel {
  declare model: DateColumnModel;
  declare eventMap: DateColumnEventMap;
  declare self: DateColumn;

  format: DateFormat;
  groupFormat: DateFormat;
  hasDate: boolean;
  hasTime: boolean;

  constructor() {
    super();
    this.format = null;
    // @ts-expect-error
    this.groupFormat = 'yyyy';
    this.hasDate = true;
    this.hasTime = false;
    this.filterType = 'DateColumnUserFilter';
    this.comparator = comparators.NUMERIC;
    this.textBased = false;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this._setFormat(this.format);
    this._setGroupFormat(this.groupFormat);
  }

  setFormat(format: DateFormat | string) {
    this.setProperty('format', format);
  }

  protected _setFormat(format: DateFormat | string) {
    if (!format) {
      format = this._getDefaultFormat(this.session.locale);
    }
    let dateFormat = DateFormat.ensure(this.session.locale, format);
    this._setProperty('format', dateFormat);
    if (this.initialized) {
      // if dateFormat changes on the fly, just update the cell text
      this.table.rows.forEach(row => this._updateCellText(row, this.cell(row)));
    }
  }

  setGroupFormat(groupFormat: DateFormat | string) {
    this.setProperty('groupFormat', groupFormat);
  }

  protected _setGroupFormat(format: DateFormat | string) {
    if (!format) {
      format = this._getDefaultFormat(this.session.locale);
    }
    let groupFormat = DateFormat.ensure(this.session.locale, format);
    this._setProperty('groupFormat', groupFormat);
    if (this.initialized) {
      this.table._group();
    }
  }

  protected override _formatValue(value: Date, row?: TableRow): string {
    return this.format.format(value);
  }

  protected override _parseValue(text: Date): Date {
    return dates.ensure(text);
  }

  protected _getDefaultFormat(locale: Locale): string {
    if (this.hasDate && this.hasTime) {
      return locale.dateFormatPatternDefault + ' ' + locale.timeFormatPatternDefault;
    }
    if (this.hasDate) {
      return locale.dateFormatPatternDefault;
    }
    return locale.timeFormatPatternDefault;
  }

  override cellTextForGrouping(row: TableRow): string {
    let val = this.table.cellValue(this, row) as Date;
    return this.groupFormat.format(val);
  }

  protected override _createEditor(row: TableRow): DateField {
    return scout.create(DateField, {
      parent: this.table,
      hasDate: this.hasDate,
      hasTime: this.hasTime
    });
  }
}
