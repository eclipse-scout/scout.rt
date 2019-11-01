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
import {objects} from '../index';
import {LookupCall} from '../index';
import {LookupRow} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';

export default class HierarchicalLookupResultBuilder {

constructor(lookupCall) {
  scout.assertParameter('lookupCall', lookupCall);
  this.lookupCall = lookupCall;

  this._lookupRowMap = {};
}

/**
 * Load all parent nodes of the given lookup rows up to the root.
 *
 * @returns {Promise} a promise resolved to an array of {LookupRow}s
 */
addParentLookupRows(lookupRows) {
  this._fillMap(lookupRows);

  var promises = lookupRows.map(this._addParent.bind(this));
  return $.promiseAll(promises)
    .then(function() {
      return objects.values(this._lookupRowMap);
    }.bind(this));
}

/**
 * @returns {Promise}
 */
_addParent(lookupRow) {
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
  return this.lookupCall
    .cloneForKey(key)
    .execute()
    .then(function(result) {
      var lookupRow = LookupCall.firstLookupRow(result);
      this._lookupRowMap[lookupRow.key] = lookupRow;
      return this._addParent(lookupRow);
    }.bind(this));
}

_fillMap(lookupRows) {
  lookupRows.forEach(function(lookupRow) {
    this._lookupRowMap[lookupRow.key] = lookupRow;
  }.bind(this));
}

/**
 * Load all parent child of the given lookup rows.
 *
 * @returns {Promise} a promise resolved to an array of {LookupRow}s
 */
addChildLookupRows(lookupRows) {
  this._fillMap(lookupRows);

  var promises = lookupRows.map(this._addChildren.bind(this));
  return $.promiseAll(promises)
    .then(function() {
      return objects.values(this._lookupRowMap);
    }.bind(this));
}

/**
 * @returns {Promise}
 */
_addChildren(lookupRow) {
  return this.lookupCall
    .cloneForRec(lookupRow.key)
    .execute()
    .then(function(result) {
      if (result.lookupRows.length) {
        return this.addChildLookupRows(result.lookupRows);
      }
    }.bind(this));
}
}
