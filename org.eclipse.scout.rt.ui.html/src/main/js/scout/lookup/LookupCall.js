/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Base class for lookup calls.
 */
scout.LookupCall = function() {
  this.session = null;
  this.hierarchical = false;
  this.loadIncremental = false;

  this.queryBy = null;
  this.searchText = null; // used on QueryBy.TEXT
  this.key = null; // used on QueryBy.KEY
  this.parentKey = null; // used on QueryBy.REC
  this.active = null;
};

scout.LookupCall.prototype.init = function(model) {
  scout.assertParameter('session', model.session);
  this._init(model);
};

scout.LookupCall.prototype._init = function(model) {
  $.extend(this, model);
};

scout.LookupCall.prototype.setLoadIncremental = function(loadIncremental) {
  this.loadIncremental = loadIncremental;
};

scout.LookupCall.prototype.setHierarchical = function(hierarchical) {
  this.hierarchical = hierarchical;
};

/**
 * This method may be called directly on any LookupCall. For the key lookup an internal clone is created automatically.
 *
 * You should not override this function. Instead override <code>_textByKey</code>.
 *
 * @returns {Promise} which returns a text of the lookup row resolved by #getByKey
 */
scout.LookupCall.prototype.textByKey = function(key) {
  if (scout.objects.isNullOrUndefined(key)) {
    return $.resolvedPromise('');
  }
  return this._textByKey(key);
};

/**
 * Override this function to provide your own textByKey implementation.
 *
 * @returns {Promise} which returns a text of the lookup row resolved by #getByKey
 */
scout.LookupCall.prototype._textByKey = function(key) {
  return this
    .cloneForKey(key)
    .execute()
    .then(function(result) {
      var lookupRow = scout.LookupCall.firstLookupRow(result);
      return lookupRow ? lookupRow.text : '';
    });
};

/**
 * Only call this function if this LookupCall is not used again. Otherwise use <code>.cloneForAll().execute()</code> or <code>.clone().getAll()</code>.
 *
 * You should not override this function. Instead override <code>_getAll</code>.
 *
 * @return {Promise} resolves to a result object with an array of {scout.LookupRow}s
 */
scout.LookupCall.prototype.getAll = function() {
  this.queryBy = scout.QueryBy.ALL;
  return this._getAll();
};

/**
 * Override this method to implement.
 */
scout.LookupCall.prototype._getAll = function() {
  throw new Error('getAll() not implemented');
};

/**
 * Only call this function if this LookupCall is not used again. Otherwise use <code>.cloneForText(text).execute()</code> or <code>.clone().getByText(text)</code>.
 *
 * You should not override this function. Instead override <code>_getByText</code>.
 *
 * @return {Promise} resolves to a result object with an array of {scout.LookupRow}s
 */
scout.LookupCall.prototype.getByText = function(text) {
  this.queryBy = scout.QueryBy.TEXT;
  this.searchText = text;
  return this._getByText(text);
};

/**
 * Override this method to implement.
 */
scout.LookupCall.prototype._getByText = function(text) {
  throw new Error('getByText() not implemented');
};

/**
 * Only call this function if this LookupCall is not used again. Otherwise use <code>.cloneForKey(key).execute()</code> or <code>.clone().getByKey(parentKey)</code>.
 *
 * You should not override this function. Instead override <code>_getByKey</code>.
 *
 * @return {Promise} resolves to a result object with a single {scout.LookupRow}
 */
scout.LookupCall.prototype.getByKey = function(key) {
  this.queryBy = scout.QueryBy.KEY;
  this.key = key;
  return this._getByKey(key);
};

/**
 * Override this method to implement.
 */
scout.LookupCall.prototype._getByKey = function(key) {
  throw new Error('getByKey() not implemented');
};

/**
 * Only call this function if this LookupCall is not used again. Otherwise use <code>.cloneForRec(parentKey).execute()</code> or <code>.clone().getByRec(parentKey)</code>.
 *
 * You should not override this function. Instead override <code>_getByRec</code>.
 *
 * Returns a result with lookup rows for the given parent key. This is used for incremental lookups.
 *
 * @return {Promise} resolves to a result object with an array of {scout.LookupRow}s
 * @param {object} parentKey references the parent key
 */
scout.LookupCall.prototype.getByRec = function(parentKey) {
  this.queryBy = scout.QueryBy.REC;
  this.parentKey = parentKey;
  return this._getByRec(parentKey);
};

/**
 * Override this method to implement.
 */
scout.LookupCall.prototype._getByRec = function(rec) {
  throw new Error('getByRec() not implemented');
};

/**
 * Executes this LookupCall. For this method to work this LookupCall must be a clone created with one of the following methods:
 * <code>cloneForAll()</code>, <code>cloneForText(text)</code>, <code>cloneForKey(key)</code>, <code>cloneForRec(parentKey)</code>
 */
scout.LookupCall.prototype.execute = function() {
  if (scout.QueryBy.KEY === this.queryBy) {
    return this._getByKey(this.key);
  }
  if (scout.QueryBy.ALL === this.queryBy) {
    return this._getAll();
  }
  if (scout.QueryBy.TEXT === this.queryBy) {
    return this._getByText(this.searchText);
  }
  if (scout.QueryBy.REC === this.queryBy) {
    return this._getByRec(this.parentKey);
  }
  throw new Error('cannot execute a non-clone LookupCall. Use one of the cloneFor*-methods before executing.');
};

scout.LookupCall.prototype.clone = function(properties) {
  // Warning: This is _not_ a deep clone! (Because otherwise the entire session would be duplicated.)
  // Non-primitive properties must _only_ be added to the resulting clone during the 'prepareLookupCall' event!
  return $.extend({}, this, properties, {
    id: scout.objectFactory.createUniqueId()
  });
};

scout.LookupCall.prototype.cloneForAll = function() {
  return this.clone({
    queryBy: scout.QueryBy.ALL
  });
};

scout.LookupCall.prototype.cloneForText = function(text) {
  return this.clone({
    queryBy: scout.QueryBy.TEXT,
    searchText: text
  });
};

scout.LookupCall.prototype.cloneForKey = function(key) {
  return this.clone({
    queryBy: scout.QueryBy.KEY,
    key: key
  });
};

scout.LookupCall.prototype.cloneForRec = function(parentKey) {
  return this.clone({
    queryBy: scout.QueryBy.REC,
    parentKey: parentKey
  });
};

scout.LookupCall.prototype.abort = function() {
  // NOP. Implement in subclasses if necessary.
};

// ---- static helpers ----

scout.LookupCall.ensure = function(lookupCall, session) {
  if (lookupCall instanceof scout.LookupCall) {
    // NOP - required to distinct instance from plain object (=model)
  } else if (scout.objects.isPlainObject(lookupCall)) {
    lookupCall.session = session;
    lookupCall = scout.create(lookupCall);
  } else if (typeof lookupCall === 'string') {
    lookupCall = scout.create(lookupCall, {
      session: session
    });
  }
  return lookupCall;
};

scout.LookupCall.firstLookupRow = function(result) {
  if (!result) {
    return null;
  }
  if (!scout.objects.isArray(result.lookupRows)) {
    return null;
  }
  if (result.lookupRows.length === 0) {
    return null;
  }
  return result.lookupRows[0];
};
