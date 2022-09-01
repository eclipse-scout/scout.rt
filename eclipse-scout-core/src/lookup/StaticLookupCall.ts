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
import {arrays, HierarchicalLookupResultBuilder, LookupCall, LookupRow, objects, QueryBy, scout, strings} from '../index';
import $ from 'jquery';
import LookupCallModel from './LookupCallModel';
import LookupResult from './LookupResult';
import Deferred = JQuery.Deferred;

/**
 * Base class for lookup calls with static or local data. Implement the _data() and _dataToLookupRow()
 * functions to provide data for lookup calls. Results are resolved as a Promise, the delay
 * property controls how long it takes until the promise is resolved. You can set it to a higher value for testing purposes.
 */
export default class StaticLookupCall<Key> extends LookupCall<Key> {

  /** delay in [ms]. By default that value is 0. */
  delay: number;
  data: any[];

  constructor() {
    super();

    this.delay = 0;
    this.data = null;
    this.active = true;
  }

  protected override _init(model: LookupCallModel<Key> & { data?: any[] }) {
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

  protected override _getAll(): JQuery.Promise<LookupResult<Key>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByAll.bind(this, deferred), this.delay);
    return deferred.promise();
  }

  protected _queryByAll(deferred: Deferred<LookupResult<Key>>) {
    deferred.resolve({
      queryBy: QueryBy.ALL,
      lookupRows: this._lookupRowsByAll()
    });
  }

  protected _lookupRowsByAll(): LookupRow<Key>[] {
    let datas = this.data.slice(0, this.maxRowCount);
    return datas
      .map(this._dataToLookupRow, this)
      .filter(this._filterActiveLookupRow, this);
  }

  protected _filterActiveLookupRow(dataRow: LookupRow<Key>): boolean {
    if (objects.isNullOrUndefined(this.active)) {
      return true;
    }
    return this.active === scout.nvl(dataRow.active, true);
  }

  protected override _getByText(text: string): JQuery.Promise<LookupResult<Key>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByText.bind(this, deferred, text), this.delay);
    return deferred.promise();
  }

  protected _queryByText(deferred: Deferred<LookupResult<Key>>, text: string) {
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

  protected _lookupRowsByText(text: string): LookupRow<Key>[] {
    let regex = this._createSearchPattern(text);
    let datas = this.data.filter(data => regex.test(data[1].toLowerCase()));
    return datas
      .map(this._dataToLookupRow, this)
      .filter(this._filterActiveLookupRow, this);
  }

  _createSearchPattern(text: string): RegExp {
    // Implementation copied from LocalLookupRow.java

    const WILDCARD = '*';
    const WILDCARD_PLACEHOLDER = '@wildcard@';

    text = strings.nvl(text);
    text = text.toLowerCase();
    text = text.replace(new RegExp(strings.quote(WILDCARD), 'g'), WILDCARD_PLACEHOLDER);
    text = strings.quote(text);

    // replace repeating wildcards to prevent regex DoS
    let duplicateWildcards = WILDCARD_PLACEHOLDER + WILDCARD_PLACEHOLDER;
    while (strings.contains(text, duplicateWildcards)) {
      text = text.replace(duplicateWildcards, WILDCARD_PLACEHOLDER);
    }

    if (!strings.endsWith(WILDCARD_PLACEHOLDER)) {
      text += WILDCARD_PLACEHOLDER;
    }

    text = text.replace(new RegExp(strings.quote(WILDCARD_PLACEHOLDER), 'g'), '.*');

    return new RegExp('^' + text + '$', 's'); // s = DOT_ALL
  }

  protected override _getByKey(key: Key): JQuery.Promise<LookupResult<Key>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByKey.bind(this, deferred, key), this.delay);
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
    let data = arrays.find(this.data, data => data[0] === key);
    if (!data) {
      return null;
    }
    return this._dataToLookupRow(data);
  }

  protected override _getByRec(rec: Key): JQuery.Promise<LookupResult<Key>> {
    let deferred = $.Deferred();
    setTimeout(this._queryByRec.bind(this, deferred, rec), this.delay);
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
  protected _dataToLookupRow(data: any): LookupRow<Key> {
    return scout.create(LookupRow, {
      key: data[0],
      text: data[1],
      parentKey: data[2]
    }) as LookupRow<Key>;
  }

  /**
   * Implement this function to provide static data.
   */
  protected _data(): any[] {
    return [];
  }
}
