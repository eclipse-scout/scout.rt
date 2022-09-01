/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, LookupCall, LookupFieldAdapter, LookupResult, LookupRow, objects, QueryBy, RemoteLookupRequest, scout} from '../index';
import $ from 'jquery';
import Deferred = JQuery.Deferred;

export default class RemoteLookupCall<Key> extends LookupCall<Key> {
  adapter: LookupFieldAdapter;
  deferred: Deferred<LookupResult<Key>, { canceled: boolean }> & { requestParameter?: RemoteLookupRequest<string | Key | void> };

  constructor(adapter: LookupFieldAdapter) {
    super();
    this.adapter = adapter;
    this.deferred = null;
  }

  protected override _getAll(): JQuery.Promise<LookupResult<Key>> {
    this._newDeferred(new RemoteLookupRequest(QueryBy.ALL));
    this.adapter.sendLookup(QueryBy.ALL);
    return this.deferred.promise();
  }

  protected override _getByText(text: string): JQuery.Promise<LookupResult<Key>> {
    this._newDeferred(new RemoteLookupRequest(QueryBy.TEXT, text));
    this.adapter.sendLookup(QueryBy.TEXT, text);
    return this.deferred.promise();
  }

  protected override _getByKey(key: Key): JQuery.Promise<LookupResult<Key>> {
    this._newDeferred(new RemoteLookupRequest(QueryBy.KEY, key));
    this.adapter.sendLookup(QueryBy.KEY, key);
    return this.deferred.promise();
  }

  protected override _getByRec(rec: Key): JQuery.Promise<LookupResult<Key>> {
    this._newDeferred(new RemoteLookupRequest(QueryBy.REC, rec));
    this.adapter.sendLookup(QueryBy.REC, rec);
    return this.deferred.promise();
  }

  resolveLookup(lookupResult: LookupResult<Key>) {
    if (!this._belongsToLatestRequest(lookupResult)) {
      $.log.isTraceEnabled() && $.log.trace('(RemoteLookupCall#resolveLookup) ignore lookupResult. Does not belong to latest request',
        objects.optProperty(this.deferred, 'requestParameter'));
      return;
    }

    lookupResult.lookupRows = arrays.ensure(lookupResult.lookupRows)
      .map(lookupRowObject => scout.create(LookupRow, lookupRowObject) as LookupRow<Key>);
    this.deferred.resolve(lookupResult);
  }

  protected _belongsToLatestRequest(lookupResult: LookupResult<Key>): boolean {
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
  protected _newDeferred(requestParameter: RemoteLookupRequest<string | Key | void>) {
    if (this.deferred) {
      this.deferred.reject({
        canceled: true
      });
    }
    this.deferred = $.Deferred();
    this.deferred.requestParameter = requestParameter;
  }
}
