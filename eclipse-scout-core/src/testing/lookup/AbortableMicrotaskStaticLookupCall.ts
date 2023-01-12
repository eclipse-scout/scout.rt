/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupResult, StaticLookupCall} from '../../index';
import $ from 'jquery';

export class AbortableMicrotaskStaticLookupCall<TKey> extends StaticLookupCall<TKey> {
  protected _deferred: JQuery.Deferred<LookupResult<TKey>>;

  constructor() {
    super();
    this._deferred = null;
  }

  override abort() {
    this._deferred.reject({
      canceled: true
    });
    super.abort();
  }

  protected override _getByKey(key: TKey): JQuery.Promise<LookupResult<TKey>> {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByKey.bind(this, this._deferred, key));
    return this._deferred.promise();
  }

  protected override _getAll(): JQuery.Promise<LookupResult<TKey>> {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByAll.bind(this, this._deferred));
    return this._deferred.promise();
  }

  protected override _getByText(text: string): JQuery.Promise<LookupResult<TKey>> {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByText.bind(this, this._deferred, text));
    return this._deferred.promise();
  }

  protected override _getByRec(rec: TKey): JQuery.Promise<LookupResult<TKey>> {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByRec.bind(this, this._deferred, rec));
    return this._deferred.promise();
  }
}
