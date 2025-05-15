/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, Table, TableAdapter, TableOrganizer} from '../../index';

/**
 * Special table organizer for classic tables that are controlled by the Java model on the UI server.
 *
 * Unlike the default {@link TableOrganizer}, it does not handle {@link addColumn}, {@link removeColumns} and {@link modifyColumn}.
 * Instead, it just delegates the calls to the UI server using the {@link TableAdapter}.
 *
 * The availability of the column organize actions depends on the flags
 * {@link Table.columnAddable}, {@link Column.removable} and {@link Column.modifiable} reported by the server.
 */
export class RemoteTableOrganizer extends TableOrganizer {

  override isColumnAddable(insertAfterColumn?: Column): boolean {
    if (!this.table) {
      return false; // not installed
    }
    return this.table.columnAddable;
  }

  override addColumn(column: Column<any>): JQuery.Promise<void> {
    (this.table.modelAdapter as TableAdapter).sendColumnOrganizeAction(column, 'add');
    return $.resolvedPromise();
  }

  override isColumnRemovable(column: Column): boolean {
    if (!this.table) {
      return false; // not installed
    }
    return column.removable;
  }

  override removeColumns(columns: Column<any>[]) {
    for (const column of columns) {
      (this.table.modelAdapter as TableAdapter).sendColumnOrganizeAction(column, 'remove');
    }
  }

  override isColumnModifiable(column: Column): boolean {
    if (!this.table) {
      return false; // not installed
    }
    return column.modifiable;
  }

  override modifyColumn(column: Column<any>) {
    (this.table.modelAdapter as TableAdapter).sendColumnOrganizeAction(column, 'modify');
  }
}
