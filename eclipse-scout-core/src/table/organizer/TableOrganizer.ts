/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Column, ObjectWithType, scout, ShowInvisibleColumnsForm, Table} from '../../index';

/**
 * If present on a table, allows adding, removing or modifying columns. If the table is customizable,
 * those functions are delegated to the table customizer. Otherwise, adding or removing a column
 * changes its `visible` flag, while modifying is currently not supported.
 *
 * Use {@link install} to attach the table organizer to a table. This happens automatically when
 * the table organizer is defined on the {@link Table}.
 *
 * @see Table.organizer
 * @see TableHeaderMenu._renderColumnActionsGroup
 * @see ShowInvisibleColumnsForm
 */
export class TableOrganizer implements ObjectWithType {

  objectType: string;
  table: Table;

  constructor() {
    this.table = null; // set with install()
  }

  /**
   * Passes the given table to the organizer.
   * If the organizer is already installed, an error is thrown.
   * In most cases, it is not necessary to call this method manually. Consider using {@link Table.setOrganizer} instead.
   */
  install(table: Table) {
    if (this.table) {
      throw new Error('Already installed');
    }
    this.table = scout.assertInstance(table, Table);
  }

  /**
   * Removes the table from the organizer.
   * If the organizer is not installed, nothing happens.
   * In most cases, it is not necessary to call this method manually. Consider using {@link Table.setOrganizer} instead.
   */
  uninstall() {
    this.table = null;
  }

  // --------------------

  /**
   * Returns a list of all currently invisible but displayable columns that can be added to the list of
   * visible columns by the table organizer.
   */
  getInvisibleColumns(): Column<any>[] {
    if (!this.table) {
      return []; // not installed
    }

    let displayableColumns = this.table.displayableColumns();
    let visibleColumns = this.table.visibleColumns();
    return arrays.diff(displayableColumns, visibleColumns);
  }

  /**
   * Adds the given columns to the list of visible columns. If `insertAfterColumn` is set, the columns are
   * also moved after the specified column. Otherwise, they remain at their current position.
   */
  showColumns<T>(columns: Column<any>[], insertAfterColumn?: Column<any>) {
    if (!this.table) {
      return; // not installed
    }

    columns = arrays.ensure(columns).filter(column => this.table.columns.includes(column));
    if (!columns.length) {
      return; // nothing to do
    }

    // Make the columns visible
    columns.forEach(column => column.setVisible(true, false)); // parameter 'false' skips call of onColumnVisibilityChanged()

    // If a "insertAfterColumn" is provided, move the columns to a position right after that column.
    // Otherwise, the selected columns are only made visible, but not moved.
    if (insertAfterColumn && insertAfterColumn.visible && this.table.columns.includes(insertAfterColumn)) {
      this._moveColumns(columns, insertAfterColumn);
    }

    this.table.onColumnVisibilityChanged(); // do this only once, will also update the aggregate rows
  }

  protected _moveColumns(columns: Column<any>[], insertAfterColumn: Column<any>) {
    for (const column of columns.reverse()) {
      let visibleColumns = this.table.visibleColumns();
      let visibleOldPos = visibleColumns.indexOf(column);
      let visibleNewPos = visibleColumns.indexOf(insertAfterColumn);
      this.table._moveColumn(column, visibleColumns, visibleOldPos, visibleNewPos);
    }
  }

  /**
   * Hides the given columns. If it was grouped, the grouping is removed. If it was part of a filter, the filter is removed.
   */
  hideColumns(columns: Column<any>[]) {
    if (!this.table) {
      return; // not installed
    }
    columns = arrays.ensure(columns).filter(Boolean);
    if (!columns.length) {
      return; // nothing to do
    }

    for (let column of columns) {
      column.setVisible(false, false); // parameter 'false' skips call of onColumnVisibilityChanged()

      if (column.grouped) {
        this.table.groupColumn(column, true, null, true); // multiGroup=true is necessary to not affect potentially other grouped columns
      }
      this.table.removeFilterByKey(column.id);
    }
    this.table.onColumnVisibilityChanged(); // do this only once, will also update the aggregate rows
  }

  /**
   * Moves the columns to their new position according to the indices in the given `visibleColumns` array.
   */
  moveColumns(visibleColumns: Column<any>[]) {
    // Add guiOnly columns to visibleColumns array if they are not already included
    // moveColumn only works with indices based on all visibleColumns including guiOnly columns
    let guiOnlyColumns = this.table.filterColumns(column => column.guiOnly);
    for (const column of guiOnlyColumns.reverse()) {
      if (!visibleColumns.includes(column)) {
        visibleColumns = [column, ...visibleColumns];
      }
    }
    for (let newPos = 0; newPos < visibleColumns.length; newPos++) {
      const column = visibleColumns[newPos];
      this.table.moveColumn(column, newPos);
    }
  }

  /**
   * Returns true if there are addable columns according to {@link getInvisibleColumns}.
   */
  isColumnAddable(): boolean {
    if (!this.table) {
      return false; // not installed
    }
    if (!this.table.columnAddable) {
      return false; // explicitly disabled
    }
    if (this.table.isCustomizable()) {
      return true;
    }
    let invisibleColumns = this.getInvisibleColumns();
    return arrays.hasElements(invisibleColumns);
  }

  addColumn(column?: Column<any>): JQuery.Promise<void> {
    if (this.table.isCustomizable()) {
      // TODO bsh [js-table] Delegate to this.table.tableCustomizer
      return $.resolvedPromise();
    }
    return this._showInvisibleColumnsForm(column);
  }

  /**
   * Returns true if the given column can be removed form the table.
   *
   * @param allowRemovalOfLastColumn true, to allow the removal of the last visible column. Default is false.
   */
  isColumnRemovable(column: Column<any>, allowRemovalOfLastColumn = false): boolean {
    if (!this.table) {
      return false; // not installed
    }
    if (!column.removable) {
      return false; // explicitly disabled
    }
    if (column.fixedPosition) {
      return false;
    }
    if (this.table.isCustomizable()) {
      return true;
    }
    // Prevent removal of last column, because there may not always be a table organizer menu to add it again
    return this.table.visibleColumns(false).length > (allowRemovalOfLastColumn ? 0 : 1);
  }

  removeColumns(columns: Column<any>[]) {
    if (this.table.isCustomizable()) {
      // TODO bsh [js-table] Delegate to this.table.tableCustomizer
    } else {
      this.hideColumns(columns);
    }
  }

  /**
   * Returns true if the given column can be modified.
   */
  isColumnModifiable(column: Column<any>): boolean {
    if (!this.table) {
      return false; // not installed
    }
    if (!column.modifiable) {
      return false; // explicitly disabled
    }
    if (this.table.isCustomizable()) {
      return true;
    }
    return false;
  }

  modifyColumn(column: Column<any>) {
    if (this.table.isCustomizable()) {
      // TODO bsh [js-table] Delegate to this.table.tableCustomizer
    } else {
      // NOP (currently not supported)
    }
  }

  /**
   * @returns true if the column can be moved to the left.
   *          It cannot be moved if the column is invisible, the column is already at the beginning or if left of the column is a fixed position column.
   */
  isColumnMovableToLeft(column: Column<any>): boolean {
    if (!column.visible) {
      return false;
    }
    let visibleOldPos = this.table.visibleColumns().indexOf(column);
    let visibleNewPos = visibleOldPos - 1;
    visibleNewPos = this.table.considerFixedPositionColumns(visibleOldPos, visibleNewPos);
    return visibleNewPos >= 0 && visibleNewPos < visibleOldPos;
  }

  /**
   * @returns true if the column can be moved to the right.
   *          It cannot be moved if the column is invisible, the column is already at the end or if right of the column is a fixed position column.
   */
  isColumnMovableToRight(column: Column<any>): boolean {
    if (!column.visible) {
      return false;
    }
    let visibleColumns = this.table.visibleColumns();
    let visibleOldPos = visibleColumns.indexOf(column);
    let visibleNewPos = visibleOldPos + 1;
    visibleNewPos = this.table.considerFixedPositionColumns(visibleOldPos, visibleNewPos);
    return visibleNewPos < visibleColumns.length && visibleNewPos > visibleOldPos;
  }

  protected _showInvisibleColumnsForm(insertAfterColumn?: Column<any>): JQuery.Promise<void> {
    let form = scout.create(ShowInvisibleColumnsForm, {
      parent: this.table,
      data: {
        columns: this.getInvisibleColumns()
      }
    });
    form.open();
    return form.whenSave().then(() => {
      this.showColumns(form.data.columns, insertAfterColumn);
    });
  }
}
