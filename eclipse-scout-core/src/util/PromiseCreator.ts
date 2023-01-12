/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * The PromiseCreator is used to work with code that creates a lot of promises.
 * In some situations (e.g. file system access) only a few of the created promises can actually do work
 * all other promises must "wait" until, the browser finally executes the promise. All these promises
 * create some overhead. This class is used to prevent that, by delaying the creation of each promise
 * until the next() function is called. Typically the next function is not called until the previous
 * (one or more) promises have been executed.
 */
export class PromiseCreator {
  results: any[];
  error: any;
  items: (() => JQuery.Promise<any>)[];
  currentItem: number;
  aborted: boolean;

  constructor(items: (() => JQuery.Promise<any>)[]) {
    this.results = [];
    this.error = null;

    this.items = items;
    this.currentItem = 0;
    this.aborted = false;
  }

  hasNext(): boolean {
    if (this.error || this.aborted) {
      return false;
    }
    return this.currentItem < this.items.length;
  }

  next(): JQuery.Promise<any> {
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

  createPromise(): JQuery.Promise<any> {
    if (this.currentItem >= this.items.length) {
      throw new Error('items out of bounds');
    }

    let promise = this._createPromise();
    this.currentItem++;
    return promise;
  }

  protected _createPromise(): JQuery.Promise<any> {
    return this.items[this.currentItem]();
  }

  protected _addResults(index: number, result: any[]) {
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
