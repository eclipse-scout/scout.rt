/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.promises = {

  /**
   * Use a promise creator to create a promise and wait until each promise has been done before the next
   * promise is created and executed.
   *
   * @param {scout.PromiseCreator} promiseCreator this function
   * @returns {Promise}
   */
  oneByOne: function(promiseCreator) {
    var deferred = $.Deferred();
    _repeat(promiseCreator);
    return deferred.promise();

    // use set timeout to prevent stack overflow
    function onDone() {
      setTimeout(_repeat.bind(this, promiseCreator));
    }

    function onFail() {
      deferred.reject.apply(deferred, promiseCreator.error);
    }

    function _repeat(promiseCreator) {
      if (promiseCreator.hasNext()) {
        promiseCreator.next()
          .done(onDone)
          .fail(onFail);
      } else {
        deferred.resolve.apply(deferred, promiseCreator.results);
      }
    }
  },

  /**
   * Use a promise creator to create a group of promises and wait until the whole group has been executed
   * before creating and executing promises for the next group.
   *
   * @param groupSize
   * @param promiseCreator
   * @returns {Promise}
   */
  groupwise: function(groupSize, promiseCreator) {
    var deferred = $.Deferred();
    _repeat(promiseCreator);
    return deferred.promise();

    // use set timeout to prevent stack overflow
    function onDone() {
      setTimeout(_repeat.bind(this, promiseCreator));
    }

    function onFail() {
      deferred.reject.apply(deferred, promiseCreator.error);
    }

    function _repeat(promiseCreator) {
      if (promiseCreator.hasNext()) {
        var promises = [];
        while (promises.length < groupSize && promiseCreator.hasNext()) {
          promises.push(promiseCreator.next());
        }
        $.promiseAll(promises, true)
          .done(onDone)
          .fail(onFail);
      } else {
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
   * @param promiseCreator
   * @param timeout specifies a timeout to wait for until the next promise will be started.
   * @returns {Promise}
   */
  parallel: function(maxPoolSize, promiseCreator, timeout) {
    timeout = timeout || 0;
    var deferred = $.Deferred();
    var poolSize = 0;
    _startNext(promiseCreator);
    return deferred.promise();

    // use set timeout to prevent stack overflow
    function onDone() {
      poolSize--;
      setTimeout(_startNext.bind(this, promiseCreator), timeout);
    }

    function onFail() {
      deferred.reject.apply(deferred, promiseCreator.error);
    }

    function _startNext(promiseCreator) {
      if (deferred.state() !== "pending") {
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

};

/**
 * The PromiseCreator is used to work with code that creates a lot of promises.
 * In some situations (e.g. file system access) only a few of the created promises can actually do work
 * all other promises must "wait" until, the browser finally excecutes the promise. All these promises
 * create some overhead. This class is used to prevent that, by delaying the creation of each promise
 * until the next() function is called. Typically the next function is not called until the previous
 * (one or more) promises have been executed.
 *
 * @constructor
 */
scout.PromiseCreator = function(items) {
  this.results = [];
  this.error = null;

  this.items = items;
  this.currentItem = 0;
  this.aborted = false;
};

scout.PromiseCreator.prototype.hasNext = function() {
  if (this.error || this.aborted) {
    return false;
  }
  return this.currentItem < this.items.length;
};

scout.PromiseCreator.prototype.next = function() {
  return this.createPromise()
    .done(function() {
      this._addResults.apply(this, arguments);
    }.bind(this))
    .fail(function() {
      this.error = arguments.length > 0 ? arguments : new Error('Promise execution failed');
    }.bind(this));
};

scout.PromiseCreator.prototype.createPromise = function() {
  if (this.currentItem >= this.items.length) {
    throw new Error('items out of bounds');
  }

  var promise = this._createPromise();
  this.currentItem++;
  return promise;
};

scout.PromiseCreator.prototype._createPromise = function() {
  return this.items[this.currentItem]();
};

scout.PromiseCreator.prototype._addResults = function() {
  var result = scout.objects.argumentsToArray(arguments);
  if (result.length === 0) {
    result = undefined;
  }
  else if (result.length === 1) {
    result = result[0];
  }
  this.results.push(result);
};

scout.PromiseCreator.prototype.abort = function() {
  this.aborted = true;
};
