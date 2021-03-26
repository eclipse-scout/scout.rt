/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, HierarchicalLookupResultBuilder, LookupCall, LookupRow, objects, QueryBy, scout, strings} from '../index';
import $ from 'jquery';

/**
 * Base class for lookup calls with static or local data. Implement the _data() and _dataToLookupRow()
 * functions to provide data for lookup calls. Results are resolved as a Promise, the delay
 * property controls how long it takes until the promise is resolved. By default that value is 0.
 * You can set it to a higher value for testing purposes.
 *
 * By default we assume that the data array uses the following indices:
 * 0: key
 * 1: text
 * 2: parentKey (optional)
 */
export default class StaticLookupCall extends LookupCall {

  constructor() {
    super();

    this.delay = 0; // delay in [ms]
    this.data = null;
    this.active = true;
  }

  static MAX_ROW_COUNT = 100;

  _init(model) {
    super._init(model);
    if (!this.data) {
      // data may either be provided by the model or by implementing the _data function
      this.data = this._data();
    }
  }

  refreshData(data) {
    if (data === undefined) {
      this.data = this._data();
    } else {
      this.data = data;
    }
  }

  _getAll() {
    let deferred = $.Deferred();
    setTimeout(this._queryByAll.bind(this, deferred), this.delay);
    return deferred.promise();
  }

  _queryByAll(deferred) {
    deferred.resolve({
      queryBy: QueryBy.ALL,
      lookupRows: this._lookupRowsByAll()
    });
  }

  _lookupRowsByAll() {
    let datas = this.data.slice(0, StaticLookupCall.MAX_ROW_COUNT + 1);
    return datas
      .map(this._dataToLookupRow, this)
      .filter(this._filterActiveLookupRow, this);
  }

  _filterActiveLookupRow(dataRow) {
    if (objects.isNullOrUndefined(this.active)) {
      return true;
    }
    return this.active === scout.nvl(dataRow.active, true);
  }

  _getByText(text) {
    let deferred = $.Deferred();
    setTimeout(this._queryByText.bind(this, deferred, text), this.delay);
    return deferred.promise();
  }

  _queryByText(deferred, text) {
    let lookupRows = this._lookupRowsByText(text);

    // resolve non-hierarchical results immediately
    if (!this.hierarchical) {
      deferred.resolve({
        queryBy: QueryBy.TEXT,
        text: text,
        lookupRows: lookupRows
      });
    }

    // if loadIncremental=false we must also load children
    let promise, builder = new HierarchicalLookupResultBuilder(this);
    if (this.loadIncremental) {
      promise = $.resolvedPromise(lookupRows);
    } else {
      promise = builder.addChildLookupRows(lookupRows);
    }

    // hierarchical lookups must first load their parent nodes
    // before we can resolve the results
    promise
      .then(lookupRows => {
        return builder.addParentLookupRows(lookupRows);
      })
      .done(lookupRows => {
        deferred.resolve({
          queryBy: QueryBy.TEXT,
          text: text,
          lookupRows: lookupRows
        });
      })
      .fail(error => {
        throw error;
      });
  }

  _lookupRowsByText(text) {
    let datas = this.data.filter(data => {
      return strings.startsWith(data[1].toLowerCase(), text.toLowerCase());
    });
    return datas
      .map(this._dataToLookupRow, this)
      .filter(this._filterActiveLookupRow, this);
  }

  _getByKey(key) {
    let deferred = $.Deferred();
    setTimeout(this._queryByKey.bind(this, deferred, key), this.delay);
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
    let data = arrays.find(this.data, data => {
      return data[0] === key;
    });
    if (!data) {
      return null;
    }
    return this._dataToLookupRow(data);
  }

  _getByRec(rec) {
    let deferred = $.Deferred();
    setTimeout(this._queryByRec.bind(this, deferred, rec), this.delay);
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
    return this.data.reduce((aggr, data) => {
      if (data[2] === rec) {
        aggr.push(this._dataToLookupRow(data));
      }
      return aggr;
    }, [])
      .filter(this._filterActiveLookupRow, this);
  }

  setDelay(delay) {
    this.delay = delay;
  }

  /**
   * Implement this function to convert a single data array into an instance of LookupRow.
   */
  _dataToLookupRow(data) {
    return scout.create('LookupRow', {
      key: data[0],
      text: data[1],
      parentKey: data[2]
    });
  }

  /**
   * Implement this function to provide static data. The data should be an array of arrays,
   * where the inner array contains the values required to create a LookupRow. By
   * default the first two elements of the array are mandatory, the others are optional:
   *
   *   0: Key
   *   1: Text
   *   2: ParentKey (optional)
   *
   * When your data contains more elements you must also implement the _dataToLookupRow() function.
   */
  _data() {
    return [];
  }
}
