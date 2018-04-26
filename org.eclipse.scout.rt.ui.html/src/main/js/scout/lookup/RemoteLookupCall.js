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
scout.RemoteLookupCall.prototype._getAll = function() {
  this._newDeferred(new scout.RemoteLookupRequest(scout.QueryBy.ALL));
  this.adapter.sendLookup(scout.QueryBy.ALL);
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype._getByText = function(text) {
  this._newDeferred(new scout.RemoteLookupRequest(scout.QueryBy.TEXT, text));
  this.adapter.sendLookup(scout.QueryBy.TEXT, text);
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype._getByKey = function(key) {
  this._newDeferred(new scout.RemoteLookupRequest(scout.QueryBy.KEY, key));
  this.adapter.sendLookup(scout.QueryBy.KEY, key);
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype._getByRec = function(rec) {
  this._newDeferred(new scout.RemoteLookupRequest(scout.QueryBy.REC, rec));
  this.adapter.sendLookup(scout.QueryBy.REC, rec);
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype.resolveLookup = function(lookupResult) {
  if (!this._belongsToLatestRequest(lookupResult)) {
    $.log.isTraceEnabled() && $.log.trace('(RemoteLookupCall#resolveLookup) ignore lookupResult. Does not belong to latest request', this.deferred.requestParameter);
    return;
  }

  var lookupRows = scout.arrays.ensure(lookupResult.lookupRows).map(function(lookupRowObject) {
    return scout.create('LookupRow', lookupRowObject);
  });
  lookupResult.lookupRows = lookupRows;
  this.deferred.resolve(lookupResult);
};

scout.RemoteLookupCall.prototype._belongsToLatestRequest = function(lookupResult) {
  // This case may happen when a lookup is initialized by the UI server (not the browser)
  // Note: currently we simply ignore that case because it can only occur when the UI server
  // calls doSearch in unexpected conditions. However, we could support this case in a similar
  // way than we support requestInput().
  if (!this.deferred) {
    return false;
  }

  var propertyName = lookupResult.queryBy.toLowerCase(),
    requestData = lookupResult[propertyName],
    resultParameter = new scout.RemoteLookupRequest(lookupResult.queryBy, requestData);
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

