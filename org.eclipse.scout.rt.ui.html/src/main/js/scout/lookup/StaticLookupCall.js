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
 * Base class for lookup calls with static or local data. Implement the _data() and _dataToLookupRow()
 * functions to provide data for lookup calls. Results are resolved as a Promise, the _delay
 * property controls how long it takes until the promise is resolved. By default that value is 0.
 * You can set it to a higher value for testing purposes.
 *
 * By default we assume that the data array uses the following indizes:
 * 0: key
 * 1: text
 * 2: parentKey (optional)
 */
scout.StaticLookupCall = function() {
  scout.StaticLookupCall.parent.call(this);

  this._delay = 0; // delay in [ms]
};
scout.inherits(scout.StaticLookupCall, scout.LookupCall);

scout.StaticLookupCall.MAX_ROW_COUNT = 100;

scout.StaticLookupCall.prototype.getAll = function() {
  var deferred = $.Deferred();
  setTimeout(this._queryAll.bind(this, deferred), this._delay);
  return deferred.promise();
};

scout.StaticLookupCall.prototype._queryAll = function(deferred) {
  var datas = this._data().slice(0, scout.StaticLookupCall.MAX_ROW_COUNT + 1);
  deferred.resolve({
    lookupRows: datas.map(this._dataToLookupRow)
  });
};

scout.StaticLookupCall.prototype.getByText = function(text) {
  var deferred = $.Deferred();
  setTimeout(this._queryByText.bind(this, deferred, text), this._delay);
  return deferred.promise();
};

scout.StaticLookupCall.prototype._queryByText = function(deferred, text) {
  var datas = this._data().filter(function(data) {
    return scout.strings.startsWith(data[1].toLowerCase(), text.toLowerCase());
  });
  var lookupRows = datas.map(this._dataToLookupRow);

  // resolve non-hierarchical results immediately
  if (!this.hierarchical) {
    deferred.resolve({
      searchText: text,
      lookupRows: lookupRows
    });
  }

  // if loadIncremental=false we must also load children
  var promise, builder = new scout.HierarchicalLookupResultBuilder(this);
  if (this.loadIncremental) {
    promise = $.resolvedPromise(lookupRows);
  } else {
    promise = builder.addChildLookupRows(lookupRows);
  }

  // hierarchical lookups must first load their parent nodes
  // before we can resolve the results
  promise
    .then(function(lookupRows) {
      return builder.addParentLookupRows(lookupRows);
    })
    .done(function(lookupRows) {
      deferred.resolve({
        searchText: text,
        lookupRows: lookupRows
      });
    }.bind(this))
    .fail(function(error) {
      throw error;
    });
};

scout.StaticLookupCall.prototype.getByKey = function(key) {
  var deferred = $.Deferred();
  setTimeout(this._queryByKey.bind(this, deferred, key), this._delay);
  return deferred.promise();
};

scout.StaticLookupCall.prototype._queryByKey = function(deferred, key) {
  var data = scout.arrays.find(this._data(), function(data) {
    return data[0] === key;
  });
  if (data) {
    deferred.resolve(this._dataToLookupRow(data));
  } else {
    deferred.reject();
  }
};

scout.StaticLookupCall.prototype.getByRec = function(rec) {
  var deferred = $.Deferred();
  setTimeout(this._queryByRec.bind(this, deferred, rec), this._delay);
  return deferred.promise();
};

scout.StaticLookupCall.prototype._queryByRec = function(deferred, rec) {
  var lookupRows = this._data().reduce(function(aggr, data) {
    if (data[2] === rec) {
      aggr.push(this._dataToLookupRow(data));
    }
    return aggr;
  }.bind(this), []);
  deferred.resolve({
    rec: rec,
    lookupRows: lookupRows
  });
};

scout.StaticLookupCall.prototype.setDelay = function(delay) {
  this._delay = delay;
};

/**
 * Implement this function to convert a single data array into an instance of scout.LookupRow.
 */
scout.StaticLookupCall.prototype._dataToLookupRow = function(data) {
  return new scout.LookupRow(data[0], data[1]);
};

/**
 * Implement this function to provide static data. The data should be an array of arrays,
 * where the inner array contains the values required to create a scout.LookupRow. By
 * default the first two elements of the array must be:
 *
 *   0: Text
 *   1: Key
 *
 * When your data contains more elements you must also implement the _dataToLookupRow() function.
 */
scout.StaticLookupCall.prototype._data = function() {
  throw new Error('_data not implemented');
};

