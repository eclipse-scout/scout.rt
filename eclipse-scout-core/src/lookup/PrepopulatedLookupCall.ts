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

export class PrepopulatedLookupCall<TKey> extends LookupCall<TKey> {
  lookupRows: LookupRow<TKey>[];
  protected _deferred: JQuery.Deferred<LookupResult<TKey>>;

  constructor() {
    super();
    this.lookupRows = [];
    this._deferred = null;
  }

  override abort() {
    this._deferred?.reject({
      abort: true
    });
    super.abort();
  }

  setLookupRows(lookupRows: LookupRow<TKey>[] | LookupRow<TKey>) {
    this.lookupRows = arrays.ensure(lookupRows);
  }

  protected _filterActiveLookupRow(lookupRow: LookupRow<TKey>): boolean {
    return !!scout.nvl(lookupRow.active, true);
  }

  // --- ALL ---

  protected override _getAll(): JQuery.Promise<LookupResult<TKey>> {
    this._deferred = $.Deferred();
    setTimeout(this._queryByAll.bind(this));
    return this._deferred.promise();
  }

  protected _queryByAll() {
    this._deferred.resolve({
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
    this._deferred = $.Deferred();
    setTimeout(this._queryByText.bind(this, text));
    return this._deferred.promise();
  }

  protected _queryByText(text: string) {
    this._deferred.resolve({
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
    this._deferred = $.Deferred();
    setTimeout(this._queryByKey.bind(this, key));
    return this._deferred.promise();
  }

  protected _queryByKey(key: TKey) {
    let lookupRow = this._lookupRowByKey(key);
    if (lookupRow) {
      this._deferred.resolve({
        queryBy: QueryBy.KEY,
        lookupRows: [lookupRow]
      });
    } else {
      this._deferred.reject();
    }
  }

  protected _lookupRowByKey(key: TKey): LookupRow<TKey> {
    return arrays.find(this.lookupRows, lookupRow => lookupRow.key === key);
  }

  // --- REC ---

  protected override _getByRec(rec: TKey): JQuery.Promise<LookupResult<TKey>> {
    this._deferred = $.Deferred();
    setTimeout(this._queryByRec.bind(this, rec));
    return this._deferred.promise();
  }

  protected _queryByRec(rec: TKey) {
    this._deferred.resolve({
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
