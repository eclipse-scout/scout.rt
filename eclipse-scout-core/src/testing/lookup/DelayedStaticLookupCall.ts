/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Deferred, LookupResult, StaticLookupCall} from '../../index';

export class DelayedStaticLookupCall<TKey> extends StaticLookupCall<TKey> {
  protected _delayDeferred: Deferred<any>;

  override abort() {
    this._delayDeferred?.reject({
      abort: true
    });
    super.abort();
  }

  protected override _getByKey(key: TKey): Promise<LookupResult<TKey>> {
    this._delayDeferred = new Deferred();
    return this._delayDeferred.promise().then(() => super._getByKey(key));
  }

  protected override _getAll(): Promise<LookupResult<TKey>> {
    this._delayDeferred = new Deferred();
    return this._delayDeferred.promise().then(() => super._getAll());
  }

  protected override _getByText(text: string): Promise<LookupResult<TKey>> {
    this._delayDeferred = new Deferred();
    return this._delayDeferred.promise().then(() => super._getByText(text));
  }

  protected override _getByRec(rec: TKey): Promise<LookupResult<TKey>> {
    this._delayDeferred = new Deferred();
    return this._delayDeferred.promise().then(() => super._getByRec(rec));
  }

  resolve() {
    this._delayDeferred.resolve();
  }
}
