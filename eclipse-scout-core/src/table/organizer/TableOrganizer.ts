/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Column, EventHandler, ObjectWithType, scout, ShowInvisibleColumnsForm, Table, TableColumnOrganizeActionEvent} from '../../index';

/**
 * If present on a table, allows adding, removing or modifying columns. If the table is customizable,
 * those functions are delegated to the table customizer. Otherwise, adding or removing a column
 * changes its `visible` flag, while modifying is currently not supported.
 *
 * Use {@link #install} to attach the table organizer to a table. This happens automatically when
 * the table organizer is defined on the {@link Table}.
 *
 * @see Table#organizer
 * @see TableHeaderMenu#_renderColumnActionsGroup
 * @see ShowInvisibleColumnsForm
 */
export class TableOrganizer implements ObjectWithType {

  objectType: string;
  table: Table;

  protected _columnOrganizeActionHandler: EventHandler<TableColumnOrganizeActionEvent>;

  constructor() {
    this.table = null; // set with install()
    this._columnOrganizeActionHandler = this._onColumnOrganizeAction.bind(this);
  }

  /**
   * Installs a listener for the `columnOrganizeAction` event on the given table. If the organizer is already
   * installed, an error is thrown. In most cases, it is not necessary to call this method manually. Consider
   * using {@link Table#setOrganizer} instead.
   */
  install(table: Table) {
    if (this.table) {
      throw new Error('Already installed');
    }
    this.table = scout.assertInstance(table, Table);
    this.table.on('columnOrganizeAction', this._columnOrganizeActionHandler);
  }

  /**
   * Uninstalls the listener for the `columnOrganizeAction` event on the given table. If the organizer is not
   * installed, nothing happens. In most cases, it is not necessary to call this method manually. Consider
   * using {@link Table#setOrganizer} instead.
   */
  uninstall() {
    if (this.table) {
      this.table.off('columnOrganizeAction', this._columnOrganizeActionHandler);
      this.table = null;
    }
  }

  // --------------------

  /**
   * Returns a list of all currently invisible but displayable columns that can be added to the list of
   * visible columns by the table organizer.
   *
   * If a column is specified after which the selected columns are to be moved, the result will only include
   * columns that can be moved there without overtaking existing columns with `fixedPosition=true`.
   */
  getInvisibleColumns(insertAfterColumn?: Column): Column[] {
    if (!this.table) {
      return []; // not installed
    }

    let displayableColumns = this.table.displayableColumns();
    let visibleColumns = this.table.visibleColumns();

    // Only consider the "insertAfterColumn" if it is visible. ShowInvisibleColumnsForm#exportData will
    // then keep the current column position and only update the visibility.
    if (insertAfterColumn && visibleColumns.includes(insertAfterColumn)) {
      // Only allow adding invisible columns between two visible columns with fixed position (if present).
      // Otherwise, it would be possible to hide them and add them again on the other side.
      let insertAfterIndex = visibleColumns.indexOf(insertAfterColumn);
      let prevFixedColumn = arrays.findFromReverse(visibleColumns, insertAfterIndex, col => col.fixedPosition);
      let nextFixedColumn = arrays.findFromForward(visibleColumns, insertAfterIndex + 1, col => col.fixedPosition);
      if (prevFixedColumn || nextFixedColumn) {
        displayableColumns = displayableColumns.slice(
          prevFixedColumn ? displayableColumns.indexOf(prevFixedColumn) : 0,
          nextFixedColumn ? displayableColumns.indexOf(nextFixedColumn) : undefined
        );
      }
    }
    return arrays.diff(displayableColumns, visibleColumns);
  }

  /**
   * Adds the given columns to the list of visible columns. If `insertAfterColumn` is set, the columns are
   * also moved after the specified column. Otherwise, they remain at their current position.
   */
  showColumns<T>(columns: Column[], insertAfterColumn?: Column) {
    if (!this.table) {
      return; // not installed
    }

    columns = arrays.ensure(columns).filter(column => this.table.columns.includes(column));
    if (!columns.length) {
      return; // nothing to do
    }

    if (insertAfterColumn && insertAfterColumn.visible && this.table.columns.includes(insertAfterColumn)) {
      // If a "insertAfterColumn" is provided, move the (still hidden) columns to a position right after that column.
      // We do this _before_ making them visible to prevent the animation. If no valid "insertAfterColumn" is provided,
      // the selected columns are only made visible, but not moved.
      arrays.removeAll(this.table.columns, columns);
      arrays.insertAll(this.table.columns, columns, this.table.columns.indexOf(insertAfterColumn) + 1);
    }

    // Make the columns visible (at their new location)
    columns.forEach(column => column.setVisible(true, false)); // parameter 'false' skips call of onColumnVisibilityChanged()
    this.table.onColumnVisibilityChanged(); // do this only once, will also update the aggregate rows
  }

  /**
   * Hides the given column. If it was grouped, the grouping is removed. If it was part of a filter, the filter is removed.
   */
  hideColumn(column: Column) {
    if (!this.table) {
      return; // not installed
    }
    if (!column) {
      return; // nothing to do
    }

    column.setVisible(false);

    if (column.grouped) {
      this.table.groupColumn(column, true, null, true); // multiGroup=true is necessary to not affect potentially other grouped columns
    }
    this.table.removeFilterByKey(column.id);
  }

  /**
   * Returns true if there are addable columns according to {@link getInvisibleColumns}.
   */
  isColumnAddable(insertAfterColumn?: Column): boolean {
    if (!this.table) {
      return false; // not installed
    }
    if (!this.table.columnAddable) {
      return false; // explicitly disabled
    }
    if (this.table.isCustomizable()) {
      return true;
    }
    let invisibleColumns = this.getInvisibleColumns(insertAfterColumn);
    return arrays.hasElements(invisibleColumns);
  }

  /**
   * Returns true if the given column can be removed form the table.
   */
  isColumnRemovable(column: Column): boolean {
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
    // Prevent removal of last column, because there is no separate organize menu in Scout JS
    return this.table.visibleColumns().length > 1;
  }

  /**
   * Returns true if the given column can be modified.
   */
  isColumnModifiable(column: Column): boolean {
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

  // --------------------

  protected _onColumnOrganizeAction(event: TableColumnOrganizeActionEvent) {
    switch (event.action) {
      case 'add':
        return this._handleColumnAddEvent(event);
      case 'remove':
        return this._handleColumnRemoveEvent(event);
      case 'modify':
        return this._handleColumnModifyEvent(event);
    }
  }

  protected _handleColumnAddEvent(event: TableColumnOrganizeActionEvent) {
    if (this.table.isCustomizable()) {
      // TODO bsh [js-table] Delegate to this.table.tableCustomizer
    } else {
      this._showInvisibleColumnsForm(event.column);
    }
  }

  protected _showInvisibleColumnsForm(insertAfterColumn?: Column) {
    let form = scout.create(ShowInvisibleColumnsForm, {
      parent: this.table,
      data: {
        columns: this.getInvisibleColumns(insertAfterColumn)
      }
    });
    form.open();
    form.whenSave().then(() => {
      this.showColumns(form.data.columns, insertAfterColumn);
    });
  }

  protected _handleColumnRemoveEvent(event: TableColumnOrganizeActionEvent) {
    if (this.table.isCustomizable()) {
      // TODO bsh [js-table] Delegate to this.table.tableCustomizer
    } else {
      this.hideColumn(event.column);
    }
  }

  protected _handleColumnModifyEvent(event: TableColumnOrganizeActionEvent) {
    if (this.table.isCustomizable()) {
      // TODO bsh [js-table] Delegate to this.table.tableCustomizer
    } else {
      // NOP (currently not supported)
    }
  }
}
