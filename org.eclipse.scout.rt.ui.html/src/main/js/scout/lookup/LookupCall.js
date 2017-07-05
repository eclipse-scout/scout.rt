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

/**
 * Base class for lookup calls.
 */
scout.LookupCall = function() {
  this.session = null;

  this.hierarchical = false;
  this.loadIncremental = false;
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
  return this.getByKey(key)
    .then(function(lookupRow) {
      if (!lookupRow) {
        return '';
      }
      return lookupRow.text;
    });
};

/**
 * @return {Promise} resolves to an array of {scout.LookupRow}s
 */
scout.LookupCall.prototype.getAll = function() {
  throw new Error('getAll() not implemented');
};

/**
 * @return {Promise} resolves to an array of {scout.LookupRow}s
 */
scout.LookupCall.prototype.getByText = function(text) {
  throw new Error('getByText() not implemented');
};

/**
 * @return {Promise} resolves to a single {scout.LookupRow}
 */
scout.LookupCall.prototype.getByKey = function(key) {
  throw new Error('getByKey() not implemented');
};

/**
 * Returns a list of lookup rows for the given parent key. This is used for incremental lookups.
 *
 * @return {Promise} resolves to an array of {scout.LookupRow}s.
 * @param {object} rec references the parent key
 */
scout.LookupCall.prototype.getByRec = function(rec) {
  throw new Error('getByRec() not implemented');
};
