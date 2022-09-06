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
import {LookupCall, objects, scout} from '../index';
import $ from 'jquery';

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

    let promises = lookupRows.map(this._addParent.bind(this));
    return $.promiseAll(promises)
      .then(() => {
        return objects.values(this._lookupRowMap);
      });
  }

  /**
   * @returns {Promise}
   */
  _addParent(lookupRow) {
    let key = lookupRow.parentKey;

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
      .then(result => {
        let lookupRow = LookupCall.firstLookupRow(result);
        this._lookupRowMap[lookupRow.key] = lookupRow;
        return this._addParent(lookupRow);
      });
  }

  _fillMap(lookupRows) {
    lookupRows.forEach(lookupRow => {
      this._lookupRowMap[lookupRow.key] = lookupRow;
    });
  }

  /**
   * Load all parent child of the given lookup rows.
   *
   * @returns {Promise} a promise resolved to an array of {LookupRow}s
   */
  addChildLookupRows(lookupRows) {
    this._fillMap(lookupRows);

    let promises = lookupRows.map(this._addChildren.bind(this));
    return $.promiseAll(promises)
      .then(() => {
        return objects.values(this._lookupRowMap);
      });
  }

  /**
   * @returns {Promise}
   */
  _addChildren(lookupRow) {
    return this.lookupCall
      .cloneForRec(lookupRow.key)
      .execute()
      .then(result => {
        if (result.lookupRows.length) {
          return this.addChildLookupRows(result.lookupRows);
        }
      });
  }
}
