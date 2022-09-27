/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, TableFilter} from '../index';

export default class KeyTableFilter extends TableFilter {

  /**
   * @param {function} [keySupplier] - An optional function that extracts the key from
   *          a table row. The default expects the row object to have a "lookupRow"
   *          property and returns the "key" property.
   */
  constructor(keySupplier) {
    super();
    this.acceptedKeys = [];
    this.keySupplier = keySupplier || (row => row.lookupRow && row.lookupRow.key);
    this.active = true;
    this.alwaysAcceptedRowIds = [];
  }

  accept(row) {
    if (!this.active || arrays.empty(this.acceptedKeys) || this.alwaysAcceptedRowIds.indexOf(row.id) !== -1) {
      return true;
    }
    let key = this.keySupplier(row);
    return this.acceptedKeys.indexOf(key) !== -1;
  }

  setAcceptedKeys(...acceptedKeys) {
    this.acceptedKeys = arrays.ensure(acceptedKeys);
  }

  setActive(active) {
    this.active = active;
  }

  setAlwaysAcceptedRowIds(...rowIds) {
    this.alwaysAcceptedRowIds = rowIds;
  }
}
