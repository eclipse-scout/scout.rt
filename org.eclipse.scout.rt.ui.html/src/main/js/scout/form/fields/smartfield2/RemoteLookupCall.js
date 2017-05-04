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
  this._newDeferred();
  this.adapter.lookupAll();
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype.getByText = function(text) {
  this._newDeferred();
  this.adapter.lookupByText(text);
  return this.deferred.promise();
};

scout.RemoteLookupCall.prototype.resolveLookup = function(lookupResult) {
  this.deferred.resolve(lookupResult);
};

/**
 * Creates a new deferred and rejects the previous one.
 */
scout.RemoteLookupCall.prototype._newDeferred = function() {
  if (this.deferred) {
    this.deferred.reject({
      canceled: true
    });
  }
  this.deferred = $.Deferred();
};

