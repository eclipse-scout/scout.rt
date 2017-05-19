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
 * @returns {Promise} which returns a {scout.LookupCall}
 */
scout.LookupCall.prototype.getById = function(id) {
  // To be implemented by the subclass
  return $.resolvedPromise(scout.create('LookupRow'));
};

/**
 * You should not override this function. Instead override <code>_textById</code>.
 *
 * @returns {Promise} which returns a text of the lookup row resolved by #getById
 */
scout.LookupCall.prototype.textById = function(id) {
  if (scout.objects.isNullOrUndefined(id)) {
    return $.resolvedPromise('');
  }
  return this._textById(id);
};

/**
 * Override this function to provide your own textById implementation.
 *
 * @returns {Promise} which returns a text of the lookup row resolved by #getById
 */
scout.LookupCall.prototype._textById = function(id) {
  return this.getById(id)
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
scout.LookupCall.prototype.getAll = function() { // FIXME [awe] 7.0 - SF2: stimmt der kommentar? returns doch eher LookupROWs? nicht?
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
scout.LookupCall.prototype.getByKey = function(key) { // FIXME [awe] 7.0 - SF2: rename all getById to getByKey? discuss with C.GU
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
