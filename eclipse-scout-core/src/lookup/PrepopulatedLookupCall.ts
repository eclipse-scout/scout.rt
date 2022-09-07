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
import {arrays, LookupCall, LookupRow, QueryBy, scout, strings} from '../index';
import $ from 'jquery';
import LookupResult from './LookupResult';
import Deferred = JQuery.Deferred;

export default class PrepopulatedLookupCall<Key> extends LookupCall<Key> {

  lookupRows: LookupRow<Key>[];

  constructor() {
    super();
    this.lookupRows = [];
  }

  setLookupRows(lookupRows: LookupRow<Key>[] | LookupRow<Key>) {
    this.lookupRows = arrays.ensure(lookupRows);
  }

  protected _filterActiveLookupRow(lookupRow: LookupRow<Key>): boolean {
    return !!scout.nvl(lookupRow.active, true);
  }

  // --- ALL ---

  override _getAll(): JQuery.Promise<LookupResult<Key>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByAll.bind(this, deferred));
    return deferred.promise();
  }

  protected _queryByAll(deferred: Deferred<LookupResult<Key>>) {
    deferred.resolve({
      queryBy: QueryBy.ALL,
      lookupRows: this._lookupRowsByAll()
    });
  }

  protected _lookupRowsByAll() {
    return this.lookupRows
      .filter(this._filterActiveLookupRow, this)
      .slice(0, this.maxRowCount);
  }

  // --- TEXT ---

  override _getByText(text: string): JQuery.Promise<LookupResult<Key>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByText.bind(this, deferred, text));
    return deferred.promise();
  }

  protected _queryByText(deferred: Deferred<LookupResult<Key>>, text: string) {
    deferred.resolve({
      queryBy: QueryBy.TEXT,
      text: text,
      lookupRows: this._lookupRowsByText(text)
    });
  }

  protected _lookupRowsByText(text: string): LookupRow<Key>[] {
    let filterText = String(scout.nvl(text, '')).trim().toLowerCase();
    return this.lookupRows
      .filter(lookupRow => strings.startsWith(scout.nvl(lookupRow.text, '').toLowerCase(), filterText))
      .filter(this._filterActiveLookupRow, this)
      .slice(0, this.maxRowCount);
  }

  // --- KEY ---

  override _getByKey(key: Key): JQuery.Promise<LookupResult<Key>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByKey.bind(this, deferred, key));
    return deferred.promise();
  }

  protected _queryByKey(deferred: Deferred<LookupResult<Key>>, key: Key) {
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

  protected _lookupRowByKey(key: Key): LookupRow<Key> {
    return arrays.find(this.lookupRows, lookupRow => lookupRow.key === key);
  }

  // --- REC ---

  override _getByRec(rec: Key): JQuery.Promise<LookupResult<Key>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByRec.bind(this, deferred, rec));
    return deferred.promise();
  }

  protected _queryByRec(deferred: Deferred<LookupResult<Key>>, rec: Key) {
    deferred.resolve({
      queryBy: QueryBy.REC,
      rec: rec,
      lookupRows: this._lookupRowsByRec(rec)
    });
  }

  protected _lookupRowsByRec(rec: Key): LookupRow<Key>[] {
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
