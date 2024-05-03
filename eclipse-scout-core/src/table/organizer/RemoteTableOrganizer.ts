/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, scout, Table, TableOrganizer} from '../../index';

/**
 * Special table organizer for classic tables that are controlled by the Java model on the UI server. Unlike
 * the default {@link TableOrganizer}, it does not handle the `columnOrganizeAction` event itself, but lets
 * it be sent to the server via {@link TableAdapter}. The availability of the column organize actions depends
 * on the flags {@link Table#columnAddable}, {@link Column#removable} and {@link Column#modifiable} reported
 * by the server.
 */
export class RemoteTableOrganizer extends TableOrganizer {

  override install(table: Table) {
    if (this.table) {
      throw new Error('Already installed');
    }
    this.table = scout.assertInstance(table, Table);
  }

  override uninstall() {
    this.table = null;
  }

  override isColumnAddable(insertAfterColumn?: Column): boolean {
    if (!this.table) {
      return false; // not installed
    }
    return this.table.columnAddable;
  }

  override isColumnRemovable(column: Column): boolean {
    if (!this.table) {
      return false; // not installed
    }
    return column.removable;
  }

  override isColumnModifiable(column: Column): boolean {
    if (!this.table) {
      return false; // not installed
    }
    return column.modifiable;
  }
}
