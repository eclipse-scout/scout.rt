/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, QueryBy, scout} from '../index';
import $ from 'jquery';

/**
 * Base class for lookup calls. A concrete implementation of LookupCall.js which uses resources over a network
 * must deal with I/O errors and set, in case of an error, the 'exception' property on the returned lookup result.
 * The lookup call must _always_ return a result, otherwise the SmartField cannot work properly.
 */
export default class LookupCall {
  constructor() {
    this.session = null;
    this.hierarchical = false;
    this.loadIncremental = false;

    this.queryBy = null;
    this.searchText = null; // used on QueryBy.TEXT
    this.key = null; // used on QueryBy.KEY
    this.parentKey = null; // used on QueryBy.REC
    this.active = null;
  }

  init(model) {
    scout.assertParameter('session', model.session);
    this._init(model);
  }

  _init(model) {
    $.extend(this, model);
  }

  setLoadIncremental(loadIncremental) {
    this.loadIncremental = loadIncremental;
  }

  setHierarchical(hierarchical) {
    this.hierarchical = hierarchical;
  }

  /**
   * This method may be called directly on any LookupCall. For the key lookup an internal clone is created automatically.
   *
   * You should not override this function. Instead override <code>_textByKey</code>.
   *
   * @returns {Promise} which returns a text of the lookup row resolved by #getByKey
   */
  textByKey(key) {
    if (objects.isNullOrUndefined(key)) {
      return $.resolvedPromise('');
    }
    return this._textByKey(key);
  }

  /**
   * Override this function to provide your own textByKey implementation.
   *
   * @returns {Promise} which returns a text of the lookup row resolved by #getByKey
   */
  _textByKey(key) {
    return this
      .cloneForKey(key)
      .execute()
      .then(function(result) {
        var lookupRow = LookupCall.firstLookupRow(result);
        return lookupRow ? lookupRow.text : '';
      });
  }

  /**
   * Only call this function if this LookupCall is not used again. Otherwise use <code>.cloneForAll().execute()</code> or <code>.clone().getAll()</code>.
   *
   * You should not override this function. Instead override <code>_getAll</code>.
   *
   * @return {Promise} resolves to a result object with an array of {LookupRow}s
   */
  getAll() {
    this.queryBy = QueryBy.ALL;
    return this._getAll();
  }

  /**
   * Override this method to implement.
   */
  _getAll() {
    throw new Error('getAll() not implemented');
  }

  /**
   * Only call this function if this LookupCall is not used again. Otherwise use <code>.cloneForText(text).execute()</code> or <code>.clone().getByText(text)</code>.
   *
   * You should not override this function. Instead override <code>_getByText</code>.
   *
   * @return {Promise} resolves to a result object with an array of {LookupRow}s
   */
  getByText(text) {
    this.queryBy = QueryBy.TEXT;
    this.searchText = text;
    return this._getByText(text);
  }

  /**
   * Override this method to implement.
   */
  _getByText(text) {
    throw new Error('getByText() not implemented');
  }

  /**
   * Only call this function if this LookupCall is not used again. Otherwise use <code>.cloneForKey(key).execute()</code> or <code>.clone().getByKey(parentKey)</code>.
   *
   * You should not override this function. Instead override <code>_getByKey</code>.
   *
   * @return {Promise} resolves to a result object with a single {LookupRow}
   */
  getByKey(key) {
    this.queryBy = QueryBy.KEY;
    this.key = key;
    return this._getByKey(key);
  }

  /**
   * Override this method to implement.
   */
  _getByKey(key) {
    throw new Error('getByKey() not implemented');
  }

  /**
   * Only call this function if this LookupCall is not used again. Otherwise use <code>.cloneForRec(parentKey).execute()</code> or <code>.clone().getByRec(parentKey)</code>.
   *
   * You should not override this function. Instead override <code>_getByRec</code>.
   *
   * Returns a result with lookup rows for the given parent key. This is used for incremental lookups.
   *
   * @return {Promise} resolves to a result object with an array of {LookupRow}s
   * @param {object} parentKey references the parent key
   */
  getByRec(parentKey) {
    this.queryBy = QueryBy.REC;
    this.parentKey = parentKey;
    return this._getByRec(parentKey);
  }

  /**
   * Override this method to implement.
   */
  _getByRec(rec) {
    throw new Error('getByRec() not implemented');
  }

  /**
   * Executes this LookupCall. For this method to work this LookupCall must be a clone created with one of the following methods:
   * <code>cloneForAll()</code>, <code>cloneForText(text)</code>, <code>cloneForKey(key)</code>, <code>cloneForRec(parentKey)</code>
   */
  execute() {
    if (QueryBy.KEY === this.queryBy) {
      return this._getByKey(this.key);
    }
    if (QueryBy.ALL === this.queryBy) {
      return this._getAll();
    }
    if (QueryBy.TEXT === this.queryBy) {
      return this._getByText(this.searchText);
    }
    if (QueryBy.REC === this.queryBy) {
      return this._getByRec(this.parentKey);
    }
    throw new Error('cannot execute a non-clone LookupCall. Use one of the cloneFor*-methods before executing.');
  }

  clone(properties) {
    // Warning: This is _not_ a deep clone! (Because otherwise the entire session would be duplicated.)
    // Non-primitive properties must _only_ be added to the resulting clone during the 'prepareLookupCall' event!
    return scout.cloneShallow(this, properties, true);
  }

  cloneForAll() {
    return this.clone({
      queryBy: QueryBy.ALL
    });
  }

  cloneForText(text) {
    return this.clone({
      queryBy: QueryBy.TEXT,
      searchText: text
    });
  }

  cloneForKey(key) {
    return this.clone({
      queryBy: QueryBy.KEY,
      key: key
    });
  }

  cloneForRec(parentKey) {
    return this.clone({
      queryBy: QueryBy.REC,
      parentKey: parentKey
    });
  }

  abort() {
    // NOP. Implement in subclasses if necessary.
  }

  // ---- static helpers ----

  static ensure(lookupCall, session) {
    if (lookupCall instanceof LookupCall) {
      // NOP - required to distinct instance from plain object (=model)
    } else if (objects.isPlainObject(lookupCall)) {
      lookupCall.session = session;
      lookupCall = scout.create(lookupCall);
    } else if (typeof lookupCall === 'string') {
      lookupCall = scout.create(lookupCall, {
        session: session
      });
    }
    return lookupCall;
  }

  static firstLookupRow(result) {
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
