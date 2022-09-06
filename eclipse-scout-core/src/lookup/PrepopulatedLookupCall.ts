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
import {arrays, LookupCall, QueryBy, scout, strings} from '../index';
import $ from 'jquery';

export default class PrepopulatedLookupCall extends LookupCall {

  constructor() {
    super();
    this.lookupRows = [];
  }

  setLookupRows(lookupRows) {
    this.lookupRows = arrays.ensure(lookupRows);
  }

  _filterActiveLookupRow(lookupRow) {
    return !!scout.nvl(lookupRow.active, true);
  }

  // --- ALL ---

  _getAll() {
    let deferred = $.Deferred();
    setTimeout(this._queryByAll.bind(this, deferred));
    return deferred.promise();
  }

  _queryByAll(deferred) {
    deferred.resolve({
      queryBy: QueryBy.ALL,
      lookupRows: this._lookupRowsByAll()
    });
  }

  _lookupRowsByAll() {
    return this.lookupRows
      .filter(this._filterActiveLookupRow, this)
      .slice(0, this.maxRowCount);
  }

  // --- TEXT ---

  _getByText(text) {
    let deferred = $.Deferred();
    setTimeout(this._queryByText.bind(this, deferred, text));
    return deferred.promise();
  }

  _queryByText(deferred, text) {
    deferred.resolve({
      queryBy: QueryBy.TEXT,
      text: text,
      lookupRows: this._lookupRowsByText(text)
    });
  }

  _lookupRowsByText(text) {
    let filterText = String(scout.nvl(text, '')).trim().toLowerCase();
    return this.lookupRows
      .filter(lookupRow => {
        return strings.startsWith(scout.nvl(lookupRow.text, '').toLowerCase(), filterText);
      })
      .filter(this._filterActiveLookupRow, this)
      .slice(0, this.maxRowCount);
  }

  // --- KEY ---

  _getByKey(key) {
    let deferred = $.Deferred();
    setTimeout(this._queryByKey.bind(this, deferred, key));
    return deferred.promise();
  }

  _queryByKey(deferred, key) {
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

  _lookupRowByKey(key) {
    return arrays.find(this.lookupRows, lookupRow => {
      return lookupRow.key === key;
    });
  }

  // --- REC ---

  _getByRec(rec) {
    let deferred = $.Deferred();
    setTimeout(this._queryByRec.bind(this, deferred, rec));
    return deferred.promise();
  }

  _queryByRec(deferred, rec) {
    deferred.resolve({
      queryBy: QueryBy.REC,
      rec: rec,
      lookupRows: this._lookupRowsByRec(rec)
    });
  }

  _lookupRowsByRec(rec) {
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
