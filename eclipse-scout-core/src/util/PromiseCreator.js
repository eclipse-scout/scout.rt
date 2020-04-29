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
export default class PromiseCreator {

  constructor(items) {
    this.results = [];
    this.error = null;

    this.items = items;
    this.currentItem = 0;
    this.aborted = false;
  }

  hasNext() {
    if (this.error || this.aborted) {
      return false;
    }
    return this.currentItem < this.items.length;
  }

  next() {
    let thisItem = this.currentItem;
    return this.createPromise()
      .done((...args) => {
        this._addResults.apply(this, [thisItem, args]);
      })
      .fail(function() {
        // eslint-disable-next-line prefer-rest-params
        this.error = arguments.length > 0 ? arguments : new Error('Promise execution failed');
      }.bind(this));
  }

  createPromise() {
    if (this.currentItem >= this.items.length) {
      throw new Error('items out of bounds');
    }

    let promise = this._createPromise();
    this.currentItem++;
    return promise;
  }

  _createPromise() {
    return this.items[this.currentItem]();
  }

  _addResults(index, result) {
    if (result.length === 0) {
      result = undefined;
    } else if (result.length === 1) {
      result = result[0];
    }
    this.results[index] = result;
  }

  abort() {
    this.aborted = true;
  }
}
