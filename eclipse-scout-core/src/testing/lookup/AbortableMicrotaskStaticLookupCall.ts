/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {StaticLookupCall} from '../../index';
import $ from 'jquery';
import LookupResult from '../../lookup/LookupResult';

export default class AbortableMicrotaskStaticLookupCall<TKey> extends StaticLookupCall<TKey> {
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

  protected override _getByKey(key: TKey) {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByKey.bind(this, this._deferred, key));
    return this._deferred.promise();
  }

  protected override _getAll() {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByAll.bind(this, this._deferred));
    return this._deferred.promise();
  }

  protected override _getByText(text: string) {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByText.bind(this, this._deferred, text));
    return this._deferred.promise();
  }

  protected override _getByRec(rec: TKey) {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByRec.bind(this, this._deferred, rec));
    return this._deferred.promise();
  }
}
