/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.RemoteLookupCall = function(adapter) {
  scout.RemoteLookupCall.parent.call(this);
  this.adapter = adapter;
  this.deferred = null;
};
scout.inherits(scout.RemoteLookupCall, scout.LookupCall);

/**
 * To be implemented by the subclass.
 *
 * @returns {Promise} which returns {scout.LookupRow}s
 */
scout.RemoteLookupCall.prototype.getAll = function() {
  this._newDeferred(scout.RemoteLookupRequest.byText());
  this.adapter.lookupAll();
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype.getByText = function(text) {
  this._newDeferred(scout.RemoteLookupRequest.byText(text));
  this.adapter.lookupByText(text);
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype.getByKey = function(key) {
  this._newDeferred(scout.RemoteLookupRequest.byKey(key));
  this.adapter.lookupByKey(key);
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype.getByRec = function(rec) {
  this._newDeferred(scout.RemoteLookupRequest.byRec(rec));
  this.adapter.lookupByRec(rec);
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype.resolveLookup = function(lookupResult) {
  if (!this._belongsToLatestRequest(lookupResult)) {
    $.log.trace('(RemoteLookupCall#resolveLookup) ignore lookupResult. Does not belong to latest request', this.deferred.requestParameter);
    return;
  }

  var lookupRows = scout.arrays.ensure(lookupResult.lookupRows).map(function(lookupRowObject) {
    return scout.create('LookupRow', lookupRowObject);
  });
  lookupResult.lookupRows = lookupRows;
  this.deferred.resolve(lookupResult);
};

scout.RemoteLookupCall.prototype._belongsToLatestRequest = function(lookupResult) {
  var resultParameter;
  if (lookupResult.hasOwnProperty('key')) {
    resultParameter = scout.RemoteLookupRequest.byKey(lookupResult.key);
  } else if (lookupResult.hasOwnProperty('rec')) {
    resultParameter = scout.RemoteLookupRequest.byRec(lookupResult.rec);
  } else {
    resultParameter = scout.RemoteLookupRequest.byText(lookupResult.searchText);
  }
  return this.deferred.requestParameter.equals(resultParameter);
};

/**
 * Creates a new deferred and rejects the previous one.
 */
scout.RemoteLookupCall.prototype._newDeferred = function(requestParameter) {
  if (this.deferred) {
    this.deferred.reject({
      canceled: true
    });
  }
  this.deferred = $.Deferred();
  this.deferred.requestParameter = requestParameter;
};

