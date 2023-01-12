/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupCall, LookupRow, objects, scout} from '../index';
import $ from 'jquery';

export class HierarchicalLookupResultBuilder<TKey> {

  lookupCall: LookupCall<TKey>;
  protected _lookupRowMap: Record<PropertyKey, LookupRow<TKey>>;

  constructor(lookupCall: LookupCall<TKey>) {
    scout.assertParameter('lookupCall', lookupCall);
    this.lookupCall = lookupCall;
    this._lookupRowMap = {};
  }

  /**
   * Load all parent nodes of the given lookup rows up to the root.
   */
  addParentLookupRows(lookupRows: LookupRow<TKey>[]): JQuery.Promise<LookupRow<TKey>[]> {
    this._fillMap(lookupRows);

    let promises = lookupRows.map(this._addParent.bind(this));
    return $.promiseAll(promises)
      .then(() => objects.values(this._lookupRowMap));
  }

  protected _addParent(lookupRow: LookupRow<TKey>): JQuery.Promise<void> {
    let key = lookupRow.parentKey;

    if (!key) {
      return $.resolvedPromise();
    }

    // parent already exists in map
    if (this._lookupRowMap.hasOwnProperty(key + '')) {
      lookupRow = this._lookupRowMap[key + ''];
      return this._addParent(lookupRow);
    }

    // load parent and add it to the map
    return this.lookupCall
      .cloneForKey(key)
      .execute()
      .then(result => {
        let lookupRow = LookupCall.firstLookupRow(result);
        this._lookupRowMap[lookupRow.key + ''] = lookupRow;
        return this._addParent(lookupRow);
      });
  }

  protected _fillMap(lookupRows: LookupRow<TKey>[]) {
    lookupRows.forEach(lookupRow => {
      this._lookupRowMap[lookupRow.key + ''] = lookupRow;
    });
  }

  /**
   * Load all parent child of the given lookup rows.
   */
  addChildLookupRows(lookupRows: LookupRow<TKey>[]): JQuery.Promise<LookupRow<TKey>[]> {
    this._fillMap(lookupRows);

    let promises = lookupRows.map(this._addChildren.bind(this));
    return $.promiseAll(promises)
      .then(() => objects.values(this._lookupRowMap));
  }

  protected _addChildren(lookupRow: LookupRow<TKey>): JQuery.Promise<LookupRow<TKey>[]> {
    return this.lookupCall
      .cloneForRec(lookupRow.key)
      .execute()
      .then(result => {
        if (result.lookupRows.length) {
          return this.addChildLookupRows(result.lookupRows);
        }
      });
  }
}
