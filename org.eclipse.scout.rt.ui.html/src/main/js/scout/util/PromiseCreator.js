/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

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
  var thisItem = this.currentItem;
  return this.createPromise()
    .done(function() {
      this._addResults.apply(this, [thisItem, scout.objects.argumentsToArray(arguments)]);
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

scout.PromiseCreator.prototype._addResults = function(index, result) {
  if (result.length === 0) {
    result = undefined;
  } else if (result.length === 1) {
    result = result[0];
  }
  this.results[index] = result;
};

scout.PromiseCreator.prototype.abort = function() {
  this.aborted = true;
};
