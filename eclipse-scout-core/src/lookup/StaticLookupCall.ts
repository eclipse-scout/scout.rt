/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, HierarchicalLookupResultBuilder, LookupCall, LookupRow, objects, QueryBy, scout, StaticLookupCallModel, strings} from '../index';
import $ from 'jquery';
import LookupResult from './LookupResult';
import Deferred = JQuery.Deferred;

/**
 * Base class for lookup calls with static or local data. Implement the _data() and _dataToLookupRow()
 * functions to provide data for lookup calls. Results are resolved as a Promise, the delay
 * property controls how long it takes until the promise is resolved. You can set it to a higher value for testing purposes.
 */
export default class StaticLookupCall<TKey> extends LookupCall<TKey> implements StaticLookupCallModel<TKey> {
  declare model: StaticLookupCallModel<TKey>;

  delay: number;
  data: any[];

  constructor() {
    super();

    this.delay = 0;
    this.data = null;
    this.active = true;
  }

  protected override _init(model: StaticLookupCallModel<TKey>) {
    super._init(model);
    if (!this.data) {
      // data may either be provided by the model or by implementing the _data function
      this.data = this._data();
    }
  }

  refreshData(data?: any[]) {
    if (data === undefined) {
      this.data = this._data();
    } else {
      this.data = data;
    }
  }

  protected override _getAll(): JQuery.Promise<LookupResult<TKey>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByAll.bind(this, deferred), this.delay);
    return deferred.promise();
  }

  protected _queryByAll(deferred: Deferred<LookupResult<TKey>>) {
    deferred.resolve({
      queryBy: QueryBy.ALL,
      lookupRows: this._lookupRowsByAll()
    });
  }

  protected _lookupRowsByAll(): LookupRow<TKey>[] {
    let datas = this.data.slice(0, this.maxRowCount);
    return datas
      .map(this._dataToLookupRow, this)
      .filter(this._filterActiveLookupRow, this);
  }

  protected _filterActiveLookupRow(dataRow: LookupRow<TKey>): boolean {
    if (objects.isNullOrUndefined(this.active)) {
      return true;
    }
    return this.active === scout.nvl(dataRow.active, true);
  }

  protected override _getByText(text: string): JQuery.Promise<LookupResult<TKey>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByText.bind(this, deferred, text), this.delay);
    return deferred.promise();
  }

  protected _queryByText(deferred: Deferred<LookupResult<TKey>>, text: string) {
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
      .then(lookupRows => builder.addParentLookupRows(lookupRows))
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

  protected _lookupRowsByText(text: string): LookupRow<TKey>[] {
    let datas = this.data.filter(data => strings.startsWith(data[1].toLowerCase(), text.toLowerCase()));
    return datas
      .map(this._dataToLookupRow, this)
      .filter(this._filterActiveLookupRow, this);
  }

  protected override _getByKey(key: TKey): JQuery.Promise<LookupResult<TKey>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByKey.bind(this, deferred, key), this.delay);
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
    let data = arrays.find(this.data, data => data[0] === key);
    if (!data) {
      return null;
    }
    return this._dataToLookupRow(data);
  }

  protected override _getByRec(rec: TKey): JQuery.Promise<LookupResult<TKey>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByRec.bind(this, deferred, rec), this.delay);
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
    return this.data.reduce((aggr, data) => {
      if (data[2] === rec) {
        aggr.push(this._dataToLookupRow(data));
      }
      return aggr;
    }, [])
      .filter(this._filterActiveLookupRow, this);
  }

  setDelay(delay: number) {
    this.delay = delay;
  }

  /**
   * Implement this function to convert a single data array into an instance of LookupRow.
   */
  protected _dataToLookupRow(data: any): LookupRow<TKey> {
    return scout.create(LookupRow, {
      key: data[0],
      text: data[1],
      parentKey: data[2]
    }) as LookupRow<TKey>;
  }

  /**
   * Implement this function to provide static data.
   */
  protected _data(): any[] {
    return [];
  }
}
