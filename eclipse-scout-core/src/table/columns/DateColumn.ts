/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Column, comparators, DateColumnEventMap, DateColumnModel, DateColumnTableHeaderMenu, DateColumnUserFilter, DateField, DateFormat, DateGroupType, dates, InitModelOf, Locale, scout, TableHeader, TableHeaderMenu, TableMatrix, TableRow
} from '../../index';

export class DateColumn extends Column<Date> implements DateColumnModel {
  declare model: DateColumnModel;
  declare eventMap: DateColumnEventMap;
  declare self: DateColumn;

  format: DateFormat;
  groupFormat: DateFormat;
  groupType: DateGroupType;
  hasDate: boolean;
  hasTime: boolean;

  constructor() {
    super();
    this.format = null;
    // @ts-expect-error
    this.groupFormat = 'yyyy';
    this.groupType = null;
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

  override createTableHeaderMenu(tableHeader: TableHeader): TableHeaderMenu {
    return scout.create(DateColumnTableHeaderMenu, {
      parent: tableHeader,
      column: this,
      tableHeader: tableHeader,
      $anchor: this.$header
    });
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

  setGroupType(groupType: DateGroupType) {
    let changed = this.setProperty('groupType', groupType);
    if (!changed) {
      return;
    }
    this.table.trigger('columnDateGroupTypeChanged', {column: this});
  }

  protected override _formatValue(value: Date, row?: TableRow): string {
    return this.format.format(value);
  }

  protected override _ensureValue(text: Date | string): Date {
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
    let val = this.table.cellValue(this, row);
    if (!val) {
      return '';
    }
    let group = TableMatrix.resolveDateGroup(this.groupType);
    if (group) {
      let matrix = new TableMatrix(this.table);
      let axis = matrix.addAxis(this, group);
      return axis.format(axis.norm(val));
    }
    return this.groupFormat.format(val);
  }

  protected override _createEditor(row: TableRow): DateField {
    return scout.create(DateField, {
      parent: this.table,
      hasDate: this.hasDate,
      hasTime: this.hasTime
    });
  }

  override compare(row1: TableRow, row2: TableRow): number {
    // ---------------------------------------------------------------------------
    // Keep implementation in sync with AbstractDateColumn.java#compareTableRows
    // ---------------------------------------------------------------------------

    let value1 = this.cellValue(row1);
    let value2 = this.cellValue(row2);
    if (!value1 && !value2) {
      return 0;
    }
    if (!value1) {
      return -1;
    }
    if (!value2) {
      return 1;
    }

    if (this.grouped && this.groupType) {
      let group = TableMatrix.resolveDateGroup(this.groupType);
      if (group) {
        let matrix = new TableMatrix(this.table);
        let axis = matrix.addAxis(this, group);
        let c = axis.norm(value1) - axis.norm(value2);
        if (c) {
          return c;
        }
        // If we are here, the grouped values are the same. Only return 0 if this is _not_ the last sort column,
        // or else the additional columns could not have an effect on the row order. However, if this _is_ the
        // last sort column, sort the values normally (-> super call).
        if (this.table.columns.some(c => c !== this && c.sortActive && (c.sortIndex > this.sortIndex || c.initialAlwaysIncludeSortAtEnd))) {
          return 0;
        }
      }
    }
    return super.compare(row1, row2);
  }
}
