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

export class DelayedStaticLookupCall<TKey> extends StaticLookupCall<TKey> {
  protected _delayDeferred: JQuery.Deferred<any>;

  override abort() {
    this._delayDeferred?.reject({
      abort: true
    });
    super.abort();
  }

  protected override _getByKey(key: TKey): JQuery.Promise<LookupResult<TKey>> {
    this._delayDeferred = $.Deferred();
    return this._delayDeferred.promise().then(() => super._getByKey(key));
  }

  protected override _getAll(): JQuery.Promise<LookupResult<TKey>> {
    this._delayDeferred = $.Deferred();
    return this._delayDeferred.promise().then(() => super._getAll());
  }

  protected override _getByText(text: string): JQuery.Promise<LookupResult<TKey>> {
    this._delayDeferred = $.Deferred();
    return this._delayDeferred.promise().then(() => super._getByText(text));
  }

  protected override _getByRec(rec: TKey): JQuery.Promise<LookupResult<TKey>> {
    this._delayDeferred = $.Deferred();
    return this._delayDeferred.promise().then(() => super._getByRec(rec));
  }

  resolve() {
    this._delayDeferred.resolve();
  }
}
