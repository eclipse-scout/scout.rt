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
  Column, comparators, DateColumnEventMap, DateColumnModel, DateColumnTableHeaderMenu, DateColumnUserFilter, DateField, DateFormat, DateGroupType, dates, InitModelOf, Locale, scout, TableHeader, TableHeaderMenu, TableMatrix,
  TableMatrixKeyAxis, TableRow
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

  protected _groupTypeAxis: TableMatrixKeyAxis; // set by _updateGroupTypeAxis() when groupType is changed

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
    this._setGroupType(this.groupType);
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

  /**
   * Changes the {@link groupType} to the given value.
   *
   * If the column is grouped, the optional argument `applyGrouping` specifies whether the table rows should
   * be updated automatically. Otherwise, the grouping has to be applied manually ({@link group}). Default is true.
   */
  setGroupType(groupType: DateGroupType, applyGrouping = true) {
    let changed = this.setProperty('groupType', groupType);
    if (changed && applyGrouping && this.grouped) {
      // Adding an already grouped column does not change the index, but will sort the table correctly
      this.table.addGroupColumn(this);
    }
  }

  protected _setGroupType(groupType: DateGroupType) {
    this._setProperty('groupType', groupType);
    this._updateGroupTypeAxis();
    // Trigger event to update ui preferences and sync to java model
    this.table.trigger('columnDateGroupTypeChanged', {column: this});
  }

  protected _updateGroupTypeAxis() {
    let group = TableMatrix.resolveDateGroup(this.groupType);
    if (group) {
      let matrix = new TableMatrix(this.table);
      this._groupTypeAxis = matrix.addAxis(this, group);
    } else {
      this._groupTypeAxis = null;
    }
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
    if (this._groupTypeAxis) {
      return this._groupTypeAxis.format(this._groupTypeAxis.norm(val));
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

    if (this.grouped && this._groupTypeAxis) {
      let c = this._groupTypeAxis.norm(value1) - this._groupTypeAxis.norm(value2);
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
    return super.compare(row1, row2);
  }
}
