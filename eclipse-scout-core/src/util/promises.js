/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {PromiseCreator} from '../index';
import $ from 'jquery';

/**
 * Use a promise creator to create a promise and wait until each promise has been done before the next
 * promise is created and executed.
 *
 * @param {PromiseCreator} promiseCreator this function
 * @returns {Promise}
 */
export function oneByOne(promiseCreator) {
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
}

/**
 * Use a promise creator to create a group of promises and wait until the whole group has been executed
 * before creating and executing promises for the next group.
 *
 * @param groupSize
 * @param promiseCreator
 * @returns {Promise}
 */
export function groupwise(groupSize, promiseCreator) {
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
}

/**
 * Use a promise creator to try to keep a fixed size pool of promises of working. As soon as one
 * promise is finished, the next promise will be created and executed (as a long as there are more
 * promises available).
 *
 * @param maxPoolSize defines how many promises should be created and executed at most in parallel.
 * @param promiseCreator
 * @param timeout specifies a timeout to wait for until the next promise will be started.
 * @returns {Promise}
 */
export function parallel(maxPoolSize, promiseCreator, timeout) {
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
}

export default {
  groupwise,
  oneByOne,
  parallel
};

