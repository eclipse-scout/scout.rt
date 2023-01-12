/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Filter, TableRow} from '../index';

export class KeyTableFilter implements Filter<TableRow> {
  acceptedKeys: any[];
  keySupplier: (row: TableRow) => any;
  active: boolean;
  alwaysAcceptedRowIds: string[];

  /**
   * @param keySupplier An optional function that extracts the key from a table row. The default expects the row object to have a "lookupRow" property and returns its "key" property.
   */
  constructor(keySupplier?: (row: TableRow) => any) {
    this.acceptedKeys = [];
    this.keySupplier = keySupplier || (row => row.lookupRow && row.lookupRow.key);
    this.active = true;
    this.alwaysAcceptedRowIds = [];
  }

  accept(row: TableRow): boolean {
    if (!this.active || arrays.empty(this.acceptedKeys) || this.alwaysAcceptedRowIds.indexOf(row.id) !== -1) {
      return true;
    }
    let key = this.keySupplier(row);
    return this.acceptedKeys.indexOf(key) !== -1;
  }

  setAcceptedKeys(...acceptedKeys: any[]) {
    this.acceptedKeys = arrays.ensure(acceptedKeys);
  }

  setActive(active: boolean) {
    this.active = active;
  }

  setAlwaysAcceptedRowIds(...rowIds: string[]) {
    this.alwaysAcceptedRowIds = rowIds;
  }
}
