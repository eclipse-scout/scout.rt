/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Column, EventHandler, InitModelOf, ObjectWithType, scout, ShowInvisibleColumnsForm, Table, TableColumnOrganizeActionEvent, TableOrganizerModel} from '../index';

export class TableOrganizer implements TableOrganizerModel, ObjectWithType {
  declare model: TableOrganizerModel;

  objectType: string;
  table: Table;

  protected _columnOrganizeActionHandler: EventHandler<TableColumnOrganizeActionEvent>;

  constructor() {
    this.table = null;
  }

  init(model: InitModelOf<this>) {
    this.table = scout.assertInstance(model.table, Table);
    this._columnOrganizeActionHandler = this._onColumnOrganizeAction.bind(this);
  }

  install() {
    this.table.on('columnOrganizeAction', this._columnOrganizeActionHandler);
  }

  uninstall() {
    this.table.off('columnOrganizeAction', this._columnOrganizeActionHandler);
  }

  isColumnAddable(insertAfterColumn?: Column): boolean {
    if (this.table.isCustomizable()) {
      return true;
    }
    let addableColumns = this.getAddableColumns(insertAfterColumn);
    return arrays.hasElements(addableColumns);
  }

  isColumnRemovable(column: Column): boolean {
    if (column.fixedPosition) {
      return false;
    }
    if (this.table.isCustomizable()) {
      return true;
    }
    return this.table.visibleColumns().length > 1;
  }

  isColumnModifiable(column: Column): boolean {
    if (this.table.isCustomizable()) {
      return true;
    }
    return false;
  }

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
    let form = scout.create(ShowInvisibleColumnsForm, {
      parent: this.table,
      table: this.table,
      insertAfterColumn: event.column
    });
    form.open();
  }

  protected _handleColumnRemoveEvent(event: TableColumnOrganizeActionEvent) {
    let column = event.column;
    column.setVisible(false);

    // FIXME bsh [js-table] Remove sorting needed?
    this.table.sort(column, null, false, true);
    this.table.groupColumn(column, false, null, true);
    this.table.removeFilterByKey(column.id);
  }

  protected _handleColumnModifyEvent(event: TableColumnOrganizeActionEvent) {
  }

  getAddableColumns(insertAfterColumn?: Column): Column[] {
    let displayableColumns = this.table.displayableColumns();
    let visibleColumns = this.table.visibleColumns();
    if (insertAfterColumn) {
      // Only allow adding invisible columns between two columns with fixed position.
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
}
