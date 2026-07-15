/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout} from '../scout';
import {arrays} from './arrays';
import {dataObjects} from '../dataobject/dataObjects';
import $ from 'jquery';

export class BatchCall<TKey, TValue> {

  protected _keySet: Set<TKey> = null;
  protected _deferred: JQuery.Deferred<BatchCallResult<TKey, TValue>> = null;
  protected _promise: JQuery.Promise<BatchCallResult<TKey, TValue>> = null;

  protected _batchCall: BatchCallHandler<TKey, TValue>;
  protected _coalesceKeys: boolean;

  constructor(batchCall: BatchCallHandler<TKey, TValue>, coalesceKeys = true) {
    this._batchCall = scout.assertValue(batchCall, 'Missing batch call handler.');
    this._coalesceKeys = coalesceKeys;
  }

  addKey(key: TKey) {
    this._ensureBatchCallReady();
    this._keySet.add(key);
  }

  /**
   * Same key might point to the same values! Do not modify (read only!)
   */
  promise(): JQuery.Promise<BatchCallResult<TKey, TValue>> {
    this._ensureBatchCallReady();
    return this._promise;
  }

  protected _ensureBatchCallReady() {
    if (this._keySet) {
      return;
    }
    this._keySet = new Set();
    this._deferred = $.Deferred();
    this._promise = this._deferred.promise();
    setTimeout(this._callBatchAsync.bind(this));
  }

  protected _resetBatchCall() {
    this._keySet = null;
    this._deferred = null;
    this._promise = null;
  }

  protected _callBatchAsync() {
    const keySet = this._keySet;
    const deferred = this._deferred;

    this._resetBatchCall();

    let promise: JQuery.Promise<BatchCallResult<TKey, TValue>>;
    if (this._coalesceKeys) {
      const serializedIndex = this._createSerializedIndex(keySet);
      const uniqueKeys = [...serializedIndex.values()].map(keys => keys[0]);
      promise = this._doBatchCall(uniqueKeys)
        .then(response => deferred.resolve(this._createBatchCallResultCoalesced(response, serializedIndex)));

    } else {
      promise = this._doBatchCall([...keySet])
        .then(response => deferred.resolve(response));
    }
    promise.catch(error => deferred.reject(error));
  }

  /**
   * Index all keys by their serialized form.
   * This is used to convert the resulting key back to the original key instance (js maps always use object identity).
   * Keys that have the same serialized form only have to be resolved once, but the result still has to be returned for each original key separately.
   */
  protected _createSerializedIndex(keySet: Set<TKey>): Map<string, TKey[]> {
    const keys = [...keySet];
    const keyMap = new Map<string, TKey[]>(); // serialized key -> list of original key instances
    keys.map(key => this._serializeKey(key)).forEach((key, index) => {
      let existing = keyMap.get(key);
      if (!existing) {
        existing = [];
        keyMap.set(key, existing);
      }
      existing.push(keys[index]);
    });
    return keyMap;
  }

  protected _doBatchCall(keys: TKey[]): JQuery.Promise<Map<TKey, TValue>> {
    if (arrays.empty(keys)) {
      return $.resolvedPromise();
    }

    try {
      return this._batchCall(keys) ?? $.resolvedPromise();
    } catch (error) {
      return $.rejectedPromise(error);
    }
  }

  protected _createBatchCallResultCoalesced(response: Map<TKey, TValue>, serializedIndex: Map<string, TKey[]>): BatchCallResult<TKey, TValue> {
    if (!response?.size) {
      return new Map();
    }
    const result: BatchCallResult<TKey, TValue> = new Map();
    [...response.entries()].forEach(([key, value]) => {
      const originalKeys = serializedIndex.get(this._serializeKey(key));
      // Uses the same value (=same instance) for all originalKeys!
      // Saves memory but might be an issue if the values are modified (which is currently not allowed)!
      originalKeys?.forEach(originalKey => result.set(originalKey, value));
    });
    return result;
  }

  protected _serializeKey(key: TKey): string {
    return dataObjects.stringify(key);
  }
}

/**
 * The result of the batch lookup operation, indexed by key. Each promise resolved to the same
 * map instance, which contains all loaded entries for the batch. Callers should extract only
 * the entries relevant to their requested keys.
 */
export type BatchCallResult<TKey, TValue> = Map<TKey, TValue>;
export type BatchCallHandler<TKey, TValue> = (keys: TKey[]) => JQuery.Promise<Map<TKey, TValue>>;
