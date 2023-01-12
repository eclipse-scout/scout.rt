/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, LookupCall, LookupResult, LookupRow, QueryBy, scout, strings} from '../index';
import $ from 'jquery';
import Deferred = JQuery.Deferred;

export class PrepopulatedLookupCall<TKey> extends LookupCall<TKey> {

  lookupRows: LookupRow<TKey>[];

  constructor() {
    super();
    this.lookupRows = [];
  }

  setLookupRows(lookupRows: LookupRow<TKey>[] | LookupRow<TKey>) {
    this.lookupRows = arrays.ensure(lookupRows);
  }

  protected _filterActiveLookupRow(lookupRow: LookupRow<TKey>): boolean {
    return !!scout.nvl(lookupRow.active, true);
  }

  // --- ALL ---

  protected override _getAll(): JQuery.Promise<LookupResult<TKey>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByAll.bind(this, deferred));
    return deferred.promise();
  }

  protected _queryByAll(deferred: Deferred<LookupResult<TKey>>) {
    deferred.resolve({
      queryBy: QueryBy.ALL,
      lookupRows: this._lookupRowsByAll()
    });
  }

  protected _lookupRowsByAll(): LookupRow<TKey>[] {
    return this.lookupRows
      .filter(this._filterActiveLookupRow, this)
      .slice(0, this.maxRowCount);
  }

  // --- TEXT ---

  protected override _getByText(text: string): JQuery.Promise<LookupResult<TKey>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByText.bind(this, deferred, text));
    return deferred.promise();
  }

  protected _queryByText(deferred: Deferred<LookupResult<TKey>>, text: string) {
    deferred.resolve({
      queryBy: QueryBy.TEXT,
      text: text,
      lookupRows: this._lookupRowsByText(text)
    });
  }

  protected _lookupRowsByText(text: string): LookupRow<TKey>[] {
    let filterText = String(scout.nvl(text, '')).trim().toLowerCase();
    return this.lookupRows
      .filter(lookupRow => strings.startsWith(scout.nvl(lookupRow.text, '').toLowerCase(), filterText))
      .filter(this._filterActiveLookupRow, this)
      .slice(0, this.maxRowCount);
  }

  // --- KEY ---

  protected override _getByKey(key: TKey): JQuery.Promise<LookupResult<TKey>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByKey.bind(this, deferred, key));
    return deferred.promise();
  }

  protected _queryByKey(deferred: Deferred<LookupResult<TKey>>, key: TKey) {
    let lookupRow = this._lookupRowByKey(key);
    if (lookupRow) {
      deferred.resolve({
        queryBy: QueryBy.KEY,
        lookupRows: [lookupRow]
      });
    } else {
      deferred.reject();
    }
  }

  protected _lookupRowByKey(key: TKey): LookupRow<TKey> {
    return arrays.find(this.lookupRows, lookupRow => lookupRow.key === key);
  }

  // --- REC ---

  protected override _getByRec(rec: TKey): JQuery.Promise<LookupResult<TKey>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByRec.bind(this, deferred, rec));
    return deferred.promise();
  }

  protected _queryByRec(deferred: Deferred<LookupResult<TKey>>, rec: TKey) {
    deferred.resolve({
      queryBy: QueryBy.REC,
      rec: rec,
      lookupRows: this._lookupRowsByRec(rec)
    });
  }

  protected _lookupRowsByRec(rec: TKey): LookupRow<TKey>[] {
    return this.lookupRows
      .reduce((aggr, lookupRow) => {
        if (lookupRow.parentKey === rec) {
          aggr.push(lookupRow);
        }
        return aggr;
      }, [])
      .filter(this._filterActiveLookupRow, this)
      .slice(0, this.maxRowCount);
  }
}
