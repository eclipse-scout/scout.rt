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
import {arrays, LookupCall, LookupRow, objects, QueryBy, RemoteLookupRequest, scout} from '../index';
import $ from 'jquery';

export default class RemoteLookupCall extends LookupCall {

  constructor(adapter) {
    super();
    this.adapter = adapter;
    this.deferred = null;
  }

  /**
   * To be implemented by the subclass.
   *
   * @returns {Promise} which returns {LookupRow}s
   */
  _getAll() {
    this._newDeferred(new RemoteLookupRequest(QueryBy.ALL));
    this.adapter.sendLookup(QueryBy.ALL);
    return this.deferred.promise();
  }

  _getByText(text) {
    this._newDeferred(new RemoteLookupRequest(QueryBy.TEXT, text));
    this.adapter.sendLookup(QueryBy.TEXT, text);
    return this.deferred.promise();
  }

  _getByKey(key) {
    this._newDeferred(new RemoteLookupRequest(QueryBy.KEY, key));
    this.adapter.sendLookup(QueryBy.KEY, key);
    return this.deferred.promise();
  }

  _getByRec(rec) {
    this._newDeferred(new RemoteLookupRequest(QueryBy.REC, rec));
    this.adapter.sendLookup(QueryBy.REC, rec);
    return this.deferred.promise();
  }

  resolveLookup(lookupResult) {
    if (!this._belongsToLatestRequest(lookupResult)) {
      $.log.isTraceEnabled() && $.log.trace('(RemoteLookupCall#resolveLookup) ignore lookupResult. Does not belong to latest request',
        objects.optProperty(this.deferred, 'requestParameter'));
      return;
    }

    let lookupRows = arrays.ensure(lookupResult.lookupRows).map(lookupRowObject => {
      return scout.create('LookupRow', lookupRowObject);
    });
    lookupResult.lookupRows = lookupRows;
    this.deferred.resolve(lookupResult);
  }

  _belongsToLatestRequest(lookupResult) {
    // This case may happen when a lookup is initialized by the UI server (not the browser)
    // Note: currently we simply ignore that case because it can only occur when the UI server
    // calls doSearch in unexpected conditions. However, we could support this case in a similar
    // way than we support requestInput().
    if (!this.deferred) {
      return false;
    }

    let propertyName = lookupResult.queryBy.toLowerCase(),
      requestData = lookupResult[propertyName],
      resultParameter = new RemoteLookupRequest(lookupResult.queryBy, requestData);
    return this.deferred.requestParameter.equals(resultParameter);
  }

  /**
   * Creates a new deferred and rejects the previous one.
   */
  _newDeferred(requestParameter) {
    if (this.deferred) {
      this.deferred.reject({
        canceled: true
      });
    }
    this.deferred = $.Deferred();
    this.deferred.requestParameter = requestParameter;
  }
}
