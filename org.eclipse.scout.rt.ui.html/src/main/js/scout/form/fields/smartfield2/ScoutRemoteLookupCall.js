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
scout.ScoutRemoteLookupCall = function(adapter) {
  scout.ScoutRemoteLookupCall.parent.call(this);
  this.adapter = adapter;
  this.deferred = null;
};
scout.inherits(scout.ScoutRemoteLookupCall, scout.LookupCall);

/**
 * To be implemented by the subclass.
 *
 * @returns {Promise} which returns {scout.LookupRow}s
 */
scout.ScoutRemoteLookupCall.prototype.getAll = function() {
  this.deferred = $.Deferred();
  this.adapter.lookup();
  return this.deferred.promise();
};

scout.ScoutRemoteLookupCall.prototype.resolveLookup = function(lookupResult) {
  this.deferred.resolve(lookupResult);
};
