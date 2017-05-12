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
 */
scout.StaticLookupCall = function() {
  scout.StaticLookupCall.parent.call(this);

  this._delay = 0; // delay in [ms]
};
scout.inherits(scout.StaticLookupCall, scout.LookupCall);

scout.StaticLookupCall.MAX_ROW_COUNT = 100;

scout.StaticLookupCall.prototype.getAll = function() {
  this._newDeferred();
  setTimeout(function() {
    var datas = this._data().slice(0, scout.StaticLookupCall.MAX_ROW_COUNT + 1);
    this.resolveLookup({
      lookupRows: datas.map(this._dataToLookupRow)
    });
  }.bind(this), this._delay);
  return this.deferred.promise();
};

scout.StaticLookupCall.prototype.getByText = function(text) {
  this._newDeferred();
  setTimeout(function() {
    var datas = this._data().filter(function(data) {
      return scout.strings.startsWith(data[0].toLowerCase(), text.toLowerCase());
    });
    this.resolveLookup({
      lookupRows: datas.map(this._dataToLookupRow)
    });
  }.bind(this), this._delay);
  return this.deferred.promise();
};

scout.StaticLookupCall.prototype.getByKey = function(key) {
  this._newDeferred();
  setTimeout(function() {
    var data = scout.arrays.find(this._data(), function(data) {
      return data[1] === key;
    });
    if (data) {
      this.resolveLookup(this._dataToLookupRow(data));
    } else {
      this.deferred.reject();
    }
  }.bind(this), this._delay);
  return this.deferred.promise();
};


scout.StaticLookupCall.prototype.resolveLookup = function(lookupResult) {
  this.deferred.resolve(lookupResult);
};

scout.StaticLookupCall.prototype._newDeferred = function() {
  if (this.deferred) {
    this.deferred.reject({
      canceled: true
    });
  }
  this.deferred = $.Deferred();
};

scout.StaticLookupCall.prototype.setDelay = function(delay) {
  this._delay = delay;
};

/**
 * Implement this function to convert a single data array into an instance of scout.LookupRow.
 */
scout.StaticLookupCall.prototype._dataToLookupRow = function(data) {
  return new scout.LookupRow(data[1], data[0]);
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

