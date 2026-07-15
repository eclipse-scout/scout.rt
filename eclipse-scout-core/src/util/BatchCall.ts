/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, dataObjects, scout} from '../index';
import $ from 'jquery';

/**
 * Helper for loading values for multiple keys in a single batch operation.
 * The actual {@link BatchCallHandler} has to be provided when constructing the instance.
 *
 * Keys can be added to the next scheduled batch by calling {@link addKey}.
 * If no batch is scheduled, one will be created automatically.
 * The promise returned by {@link promise} resolves once the batch call completes.
 * The resolved value is a map with the result for the entire batch call, indexed by key (as returned by the {@link BatchCallHandler}).
 *
 * The same {@link BatchCall} instance can be reused.
 * After each batch completes, the promise returned by {@link promise} is automatically reset.
 * Callers should therefore store the promise immediately after adding keys.
 *
 * @example
 * ```ts
 * protected _batchCall = new BatchCall(this._doBatchCall.bind(this));
 *
 * protected _doBatchCall(keys: number[]): JQuery.Promise<BatchCallResult<number, string>> {
 *   return ...
 * }
 *
 * // Usage
 * keysToResolve.forEach(key => this._batchCall.addKey(key));
 * this._batchCall.promise().then(result => keysToResolve.map(key => result.get(key)).join(', '));
 * ```
 *
 * @template TKey type of the keys used in the call
 * @template TValue type of the values used in the call
 */
export class BatchCall<TKey, TValue> {

  protected _keySet: Set<TKey> = null;
  protected _deferred: JQuery.Deferred<BatchCallResult<TKey, TValue>> = null;
  protected _promise: JQuery.Promise<BatchCallResult<TKey, TValue>> = null;

  protected _batchCall: BatchCallHandler<TKey, TValue>;
  protected _coalesceKeys: boolean;

  /**
   * @param batchCall The callback that handles all the keys added to a batch.
   * @param coalesceKeys If `true` (default), different key instances which result in the same serialized representation (according to {@link dataObjects.stringify}) are combined and only
   * one of them will be passed to the given {@link BatchCallHandler}. This might be handy if a REST call is executed as part of the handler which requires a serialized represenation anyway but would like to ommit duplicates.
   * If `false`, different key instances are directly passed to the handler. Only same keys (according to
   * {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Equality_comparisons_and_sameness#same-value-zero_equality SameValueZero} which almost matches `===`) are combined.
   */
  constructor(batchCall: BatchCallHandler<TKey, TValue>, coalesceKeys = true) {
    this._batchCall = scout.assertValue(batchCall, 'Missing batch call handler.');
    this._coalesceKeys = coalesceKeys;
  }

  /**
   * Adds the key to the current batch. If there is no batch yet, a new one is created.
   *
   * If the key has already been added (according to {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Equality_comparisons_and_sameness#same-value-zero_equality SameValueZero} which almost matches `===`) this method does nothing.
   */
  addKey(key: TKey) {
    this._ensureBatchCallReady();
    this._keySet.add(key);
  }

  /**
   * @returns the {@link JQuery.Promise} which resolves when the {@link BatchCallHandler} has been completed.
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
      promise = this._doBatchCall(uniqueKeys).then(response => deferred.resolve(this._createBatchCallResultCoalesced(response, serializedIndex)));
    } else {
      promise = this._doBatchCall([...keySet]).then(response => deferred.resolve(response));
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

  protected _doBatchCall(keys: TKey[]): JQuery.Promise<BatchCallResult<TKey, TValue>> {
    if (arrays.empty(keys)) {
      return $.resolvedPromise();
    }

    try {
      return this._batchCall(keys) ?? $.resolvedPromise();
    } catch (error) {
      return $.rejectedPromise(error);
    }
  }

  protected _createBatchCallResultCoalesced(response: BatchCallResult<TKey, TValue>, serializedIndex: Map<string, TKey[]>): BatchCallResult<TKey, TValue> {
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
 * The result of the batch call operation, indexed by key.
 */
export type BatchCallResult<TKey, TValue> = Map<TKey, TValue>;
/**
 * The function to load the values for all keys of a batch.
 *
 * @param keys All the unique keys that have been added to the batch.
 * @returns A {@link JQuery.promise} holding the resolved value for each key as {@link Map}.
 */
export type BatchCallHandler<TKey, TValue> = (keys: TKey[]) => JQuery.Promise<BatchCallResult<TKey, TValue>>;
