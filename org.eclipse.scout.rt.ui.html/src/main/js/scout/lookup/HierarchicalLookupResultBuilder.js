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

scout.HierarchicalLookupResultBuilder = function(lookupCall) {
  scout.assertParameter('lookupCall', lookupCall);
  this.lookupCall = lookupCall;

  this._lookupRowMap = {};
};

/**
 * Load all parent nodes of the given lookup rows up to the root.
 *
 * @returns {Promise} a promise resolved to an array of {scout.LookupRow}s
 */
scout.HierarchicalLookupResultBuilder.prototype.addParentLookupRows = function(lookupRows) {
  this._fillMap(lookupRows);

  var promises = lookupRows.map(this._addParent.bind(this));
  return $.promiseAll(promises)
    .then(function() {
      return scout.objects.values(this._lookupRowMap);
    }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.HierarchicalLookupResultBuilder.prototype._addParent = function(lookupRow) {
  var key = lookupRow.parentKey;

  if (!key) {
    return $.resolvedPromise();
  }

  // parent already exists in map
  if (this._lookupRowMap.hasOwnProperty(key)) {
    lookupRow = this._lookupRowMap[key];
    return this._addParent(lookupRow);
  }

  // load parent and add it to the map
  return this.lookupCall.getByKey(key)
    .then(function(lookupRow) {
      this._lookupRowMap[lookupRow.key] = lookupRow;
      return this._addParent(lookupRow);
    }.bind(this));
};

scout.HierarchicalLookupResultBuilder.prototype._fillMap = function(lookupRows) {
  lookupRows.forEach(function(lookupRow) {
    this._lookupRowMap[lookupRow.key] = lookupRow;
  }.bind(this));
};

/**
 * Load all parent child of the given lookup rows.
 *
 * @returns {Promise} a promise resolved to an array of {scout.LookupRow}s
 */
scout.HierarchicalLookupResultBuilder.prototype.addChildLookupRows = function(lookupRows) {
  this._fillMap(lookupRows);

  var promises = lookupRows.map(this._addChildren.bind(this));
  return $.promiseAll(promises)
    .then(function() {
      return scout.objects.values(this._lookupRowMap);
    }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.HierarchicalLookupResultBuilder.prototype._addChildren = function(lookupRow) {
  return this.lookupCall.getByRec(lookupRow.key)
    .then(function(result) {
      if (result.lookupRows.length) {
        return this.addChildLookupRows(result.lookupRows);
      }
    }.bind(this));
};
