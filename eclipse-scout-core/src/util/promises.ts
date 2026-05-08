/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {objects, PromiseCreator} from '../index';
import $ from 'jquery';

export const promises = {

  /**
   * Use a promise creator to create a promise and wait until each promise has been done before the next
   * promise is created and executed.
   *
   * @param {PromiseCreator} promiseCreator this function
   */
  oneByOne(promiseCreator: PromiseCreator): JQuery.Promise<any> {
    let deferred = $.Deferred();
    _repeat(promiseCreator);
    return deferred.promise();

    // use set timeout to prevent stack overflow
    function onDone() {
      setTimeout(_repeat.bind(this, promiseCreator));
    }

    function onFail() {
      // eslint-disable-next-line prefer-spread
      deferred.reject.apply(deferred, promiseCreator.error);
    }

    function _repeat(promiseCreator) {
      if (promiseCreator.hasNext()) {
        promiseCreator.next()
          .done(onDone)
          .fail(onFail);
      } else {
        // eslint-disable-next-line prefer-spread
        deferred.resolve.apply(deferred, promiseCreator.results);
      }
    }
  },

  /**
   * Use a promise creator to create a group of promises and wait until the whole group has been executed
   * before creating and executing promises for the next group.
   */
  groupwise(groupSize: number, promiseCreator: PromiseCreator): JQuery.Promise<any> {
    let deferred = $.Deferred();
    _repeat(promiseCreator);
    return deferred.promise();

    // use set timeout to prevent stack overflow
    function onDone() {
      setTimeout(_repeat.bind(this, promiseCreator));
    }

    function onFail() {
      // eslint-disable-next-line prefer-spread
      deferred.reject.apply(deferred, promiseCreator.error);
    }

    function _repeat(promiseCreator) {
      if (promiseCreator.hasNext()) {
        let promises = [];
        while (promises.length < groupSize && promiseCreator.hasNext()) {
          promises.push(promiseCreator.next());
        }
        $.promiseAll(promises, true)
          .done(onDone)
          .fail(onFail);
      } else {
        // eslint-disable-next-line prefer-spread
        deferred.resolve.apply(deferred, promiseCreator.results);
      }
    }
  },

  /**
   * Use a promise creator to try to keep a fixed size pool of promises of working. As soon as one
   * promise is finished, the next promise will be created and executed (as a long as there are more
   * promises available).
   *
   * @param maxPoolSize defines how many promises should be created and executed at most in parallel.
   * @param timeout specifies a timeout to wait for until the next promise will be started. If not specified, no timeout (=0) is used).
   */
  parallel(maxPoolSize: number, promiseCreator: PromiseCreator, timeout?: number): JQuery.Promise<any> {
    timeout = timeout || 0;
    let deferred = $.Deferred();
    let poolSize = 0;
    _startNext(promiseCreator);
    return deferred.promise();

    // use set timeout to prevent stack overflow
    function onDone() {
      poolSize--;
      setTimeout(_startNext.bind(this, promiseCreator), timeout);
    }

    function onFail() {
      // eslint-disable-next-line prefer-spread
      deferred.reject.apply(deferred, promiseCreator.error);
    }

    function _startNext(promiseCreator) {
      if (deferred.state() !== 'pending') {
        // deferred has already been rejected or resolved, do not start anymore promises or call done handler
        return;
      }
      while (promiseCreator.hasNext() && poolSize < maxPoolSize) {
        poolSize++;
        promiseCreator.next().done(onDone).fail(onFail);
      }
      if (poolSize === 0) {
        deferred.resolve.apply(deferred, [promiseCreator.results]);
      }
    }
  },

  /**
   * If the given value is a promise, the callback function is executed asynchronously once the promise resolves.
   * Otherwise, the callback function is executed immediately.
   *
   * @value a promise or a value that will be passed to the callback function
   * @returns a promise or the result of the callback function
   */
  thenOrNow<TValue, TResult>(value: TValue | JQuery.Promise<TValue>, callback: (value: TValue) => TResult): TResult | JQuery.Promise<TResult> {
    if (objects.isPromise(value)) {
      return value.then(callback);
    }
    return callback(value);
  },

  /**
   * Ensures the given value is a promise.
   *
   * If the value is a promise, that promise is returned.
   * Otherwise, a new resolved promise for the value is returned.
   */
  ensure<T>(value: T | JQuery.Promise<T>): JQuery.Promise<T> {
    if (objects.isPromise(value)) {
      return value;
    }
    return $.resolvedPromise(value);
  }
};

/**
 * A helper class providing a {@link Promise} along with its associated `resolve` and `reject` functions,
 * allowing explicit control over the promise's state. Only the underlying {@link #promise} should be exposed,
 * rather than the {@link Deferred} instance.
 *
 * This is similar to jQuery's `$.Deferred` function, but is purely based on native ES6 promises.
 *
 * @template T The type of the value that the promise resolves to.
 */
export class Deferred<T> {

  protected _resolve: (value: T) => void;
  protected _reject: (reason: any) => void;
  protected _promise = new Promise<T>((resolve, reject) => {
    this._resolve = resolve;
    this._reject = reject;
  });

  resolve(value?: T) {
    this._resolve(value);
  }

  reject(reason?: any) {
    this._reject(reason);
  }

  promise(): Promise<T> {
    return this._promise;
  }
}
