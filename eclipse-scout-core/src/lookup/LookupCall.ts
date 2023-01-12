/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, ChildModelOf, FullModelOf, InitModelOf, LookupCallModel, LookupResult, LookupRow, objects, ObjectType, QueryBy, scout, Session, SomeRequired} from '../index';
import $ from 'jquery';

/**
 * Base class for lookup calls. A concrete implementation of LookupCall.js which uses resources over a network
 * must deal with I/O errors and set, in case of an error, the 'exception' property on the returned lookup result.
 * The lookup call must _always_ return a result, otherwise the SmartField cannot work properly.
 */
export class LookupCall<TKey> implements LookupCallModel<TKey> {
  declare model: LookupCallModel<TKey>;
  declare initModel: SomeRequired<this['model'], 'session'>;

  id: string;
  objectType: ObjectType<LookupCall<TKey>>;
  session: Session;
  hierarchical: boolean;
  loadIncremental: boolean;
  batch: boolean;
  queryBy: QueryBy;
  searchText: string;
  key: TKey;
  keys: TKey[];
  parentKey: TKey;
  active: boolean;
  maxRowCount: number;

  constructor() {
    this.session = null;
    this.hierarchical = false;
    this.loadIncremental = false;
    this.batch = false;
    this.queryBy = null;
    this.searchText = null;
    this.key = null;
    this.keys = null;
    this.parentKey = null;
    this.active = null;
    this.maxRowCount = 100;
  }

  init(model: InitModelOf<this>) {
    scout.assertParameter('session', model.session);
    this._init(model);
  }

  protected _init(model: InitModelOf<this>) {
    $.extend(this, model);
  }

  setLoadIncremental(loadIncremental: boolean) {
    this.loadIncremental = loadIncremental;
  }

  setHierarchical(hierarchical: boolean) {
    this.hierarchical = hierarchical;
  }

  setBatch(batch: boolean) {
    this.batch = batch;
  }

  /**
   * @param maxRowCount - a positive number, _not_ null or undefined!
   */
  setMaxRowCount(maxRowCount: number) {
    this.maxRowCount = maxRowCount;
  }

  /**
   * This method may be called directly on any {@link LookupCall}. For the key lookup an internal clone is created automatically.
   *
   * You should not override this function. Instead override {@link _textByKey}.
   *
   * @returns a promise which returns a text of the lookup row resolved by {@link getByKey}.
   */
  textByKey(key: TKey): JQuery.Promise<string> {
    if (objects.isNullOrUndefined(key)) {
      return $.resolvedPromise('');
    }
    return this._textByKey(key);
  }

  /**
   * Override this function to provide your own textByKey implementation.
   *
   * @returns a promise which returns a text of the lookup row resolved by {@link getByKey}.
   */
  protected _textByKey(key: TKey): JQuery.Promise<string> {
    return this
      .cloneForKey(key)
      .execute()
      .then(result => {
        let lookupRow = LookupCall.firstLookupRow(result);
        return lookupRow ? lookupRow.text : '';
      });
  }

  /**
   * This method may be called directly on any {@link LookupCall}. For the keys lookup an internal clone is created automatically.
   *
   * You should not override this function. Instead override {@link _textsByKeys}.
   *
   * @returns A promise which returns an object that maps every {@link LookupRow} key to the text of the resolved {@link LookupRow}.
   */
  textsByKeys(keys: TKey[]): JQuery.Promise<Record<string, string>> {
    if (arrays.empty(keys)) {
      return $.resolvedPromise({});
    }
    return this._textsByKeys(keys);
  }

  /**
   * Override this function to provide your own textsByKeys implementation.
   *
   * * @returns A promise which returns an object that maps every {@link LookupRow} key to the text of the resolved {@link LookupRow}.
   */
  protected _textsByKeys(keys: TKey[]): JQuery.Promise<Record<string, string>> {
    return this
      .cloneForKeys(keys)
      .execute()
      .then(result => {
        if (!result || !objects.isArray(result.lookupRows)) {
          return {};
        }

        const textMap = {};
        result.lookupRows.forEach(row => {
          textMap[objects.ensureValidKey(row.key)] = row.text;
        });

        return textMap;
      });
  }

  /**
   * Only call this function if this LookupCall is not used again. Otherwise, use {@link cloneForAll().execute()} or {@link clone().getAll()}.
   *
   * You should not override this function. Instead override {@link _getAll}.
   */
  getAll(): JQuery.Promise<LookupResult<TKey>> {
    this.queryBy = QueryBy.ALL;
    return this._getAll();
  }

  /**
   * Override this method to implement.
   */
  protected _getAll(): JQuery.Promise<LookupResult<TKey>> {
    throw new Error('_getAll() not implemented');
  }

  /**
   * Only call this function if this {@link LookupCall} is not used again. Otherwise, use {@link cloneForText(text).execute()} or {@link clone().getByText(text)}.
   *
   * You should not override this function. Instead override {@link _getByText}.
   */
  getByText(text: string): JQuery.Promise<LookupResult<TKey>> {
    this.queryBy = QueryBy.TEXT;
    this.searchText = text;
    return this._getByText(text);
  }

  /**
   * Override this method to implement.
   */
  protected _getByText(text: string): JQuery.Promise<LookupResult<TKey>> {
    throw new Error('_getByText() not implemented');
  }

  /**
   * Only call this function if this {@link LookupCall} is not used again. Otherwise, use {@link cloneForKey(key).execute()} or {@link clone().getByKey(parentKey)}.
   *
   * You should not override this function. Instead override {@link _getByKey}.
   */
  getByKey(key: TKey): JQuery.Promise<LookupResult<TKey>> {
    this.queryBy = QueryBy.KEY;
    this.key = key;
    return this._getByKey(key);
  }

  /**
   * Override this method to implement.
   */
  protected _getByKey(key: TKey): JQuery.Promise<LookupResult<TKey>> {
    throw new Error('getByKey() not implemented');
  }

  /**
   * Only call this function if this {@link LookupCall} is not used again. Otherwise, use {@link cloneForKeys(keys).execute()} or {@link clone().getByKeys(keys)}.
   *
   * You should not override this function. Instead override {@link _getByKeys}.
   */
  getByKeys(keys: TKey[]): JQuery.Promise<LookupResult<TKey>> {
    this.queryBy = QueryBy.KEYS;
    this.keys = keys;
    return this._getByKeys(keys);
  }

  /**
   * Override this method to implement.
   */
  protected _getByKeys(keys: TKey[]): JQuery.Promise<LookupResult<TKey>> {
    throw new Error('_getByKeys() not implemented');
  }

  /**
   * Only call this function if this {@link LookupCall} is not used again. Otherwise, use {@link cloneForRec(parentKey).execute()} or {@link clone().getByRec(parentKey)}.
   *
   * You should not override this function. Instead override {@link _getByRec}.
   *
   * Returns a result with lookup rows for the given parent key. This is used for incremental lookups.
   *
   * @param parentKey references the parent key
   */
  getByRec(parentKey: TKey): JQuery.Promise<LookupResult<TKey>> {
    this.queryBy = QueryBy.REC;
    this.parentKey = parentKey;
    if (objects.isNullOrUndefined(parentKey)) {
      // Lookup rows with key = null cannot act as parent since lookup rows with parentKey = null are always top level
      return this._emptyRecResult(parentKey);
    }
    return this._getByRec(parentKey);
  }

  protected _emptyRecResult(rec: TKey): JQuery.Promise<LookupResult<TKey>> {
    return $.resolvedPromise({
      queryBy: QueryBy.REC,
      rec: rec,
      lookupRows: []
    });
  }

  /**
   * Override this method to implement.
   */
  protected _getByRec(rec: TKey): JQuery.Promise<LookupResult<TKey>> {
    throw new Error('_getByRec() not implemented');
  }

  /**
   * Executes this LookupCall. For this method to work this LookupCall must be a clone created with one of the following methods:
   * {@link cloneForAll()}, {@link cloneForText(text)}, {@link cloneForKey(key)}, {@link cloneForRec(parentKey)}
   */
  execute(): JQuery.Promise<LookupResult<TKey>> {
    if (QueryBy.KEY === this.queryBy) {
      return this._getByKey(this.key);
    }
    if (QueryBy.ALL === this.queryBy) {
      return this._getAll();
    }
    if (QueryBy.KEYS === this.queryBy) {
      return this._getByKeys(this.keys);
    }
    if (QueryBy.TEXT === this.queryBy) {
      return this._getByText(this.searchText);
    }
    if (QueryBy.REC === this.queryBy) {
      if (objects.isNullOrUndefined(this.parentKey)) {
        // Lookup rows with key = null cannot act as parent since lookup rows with parentKey = null are always top level
        return this._emptyRecResult(this.parentKey);
      }
      return this._getByRec(this.parentKey);
    }
    throw new Error('cannot execute a non-clone LookupCall. Use one of the cloneFor*-methods before executing.');
  }

  /**
   * @param properties Properties to add to the resulting clone instance.
   */
  clone(properties?: object): this {
    // Warning: This is _not_ a deep clone! (Because otherwise the entire session would be duplicated.)
    // Non-primitive properties must _only_ be added to the resulting clone during the 'prepareLookupCall' event!
    return scout.cloneShallow(this, properties, true) as this;
  }

  cloneForAll(): this {
    return this.clone({
      queryBy: QueryBy.ALL
    });
  }

  cloneForText(text: string): this {
    return this.clone({
      queryBy: QueryBy.TEXT,
      searchText: text
    });
  }

  cloneForKey(key: TKey): this {
    return this.clone({
      queryBy: QueryBy.KEY,
      key: key
    });
  }

  cloneForKeys(keys: TKey[]): this {
    return this.clone({
      queryBy: QueryBy.KEYS,
      keys: keys
    });
  }

  cloneForRec(parentKey: TKey): this {
    return this.clone({
      queryBy: QueryBy.REC,
      parentKey: parentKey
    });
  }

  abort() {
    // NOP. Implement in subclasses if necessary.
  }

  // ---- static helpers ----

  static ensure<TKey, TLookupCall extends LookupCall<TKey>>(lookupCall: LookupCallOrModel<TKey, TLookupCall>, session: Session): TLookupCall {
    if (!lookupCall) {
      return lookupCall as TLookupCall;
    }
    if (lookupCall instanceof LookupCall) {
      return lookupCall;
    }
    if (typeof lookupCall === 'string' || typeof lookupCall === 'function') {
      return scout.create(lookupCall, {
        session: session
      } as InitModelOf<TLookupCall>);
    }
    lookupCall.session = session;
    return scout.create(lookupCall as FullModelOf<TLookupCall>);
  }

  static firstLookupRow<K>(result: LookupResult<K>): LookupRow<K> {
    if (!result) {
      return null;
    }
    if (!objects.isArray(result.lookupRows)) {
      return null;
    }
    if (result.lookupRows.length === 0) {
      return null;
    }
    return result.lookupRows[0];
  }
}

/**
 * A LookupCall, a LookupCallModel or a string with a LookupCall class name.
 */
export type LookupCallOrModel<TKey, TLookupCall extends LookupCall<TKey> = LookupCall<TKey>> = TLookupCall | ChildModelOf<TLookupCall> | ObjectType<TLookupCall>;
