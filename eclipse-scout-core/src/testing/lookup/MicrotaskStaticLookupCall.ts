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

export class MicrotaskStaticLookupCall<TKey> extends StaticLookupCall<TKey> {

  protected override _getByKey(key: TKey): Promise<LookupResult<TKey>> {
    this._deferred = new Deferred();
    queueMicrotask(this._queryByKey.bind(this, key));
    return this._deferred.promise();
  }

  protected override _getAll(): Promise<LookupResult<TKey>> {
    this._deferred = new Deferred();
    queueMicrotask(this._queryByAll.bind(this));
    return this._deferred.promise();
  }

  protected override _getByText(text: string): Promise<LookupResult<TKey>> {
    this._deferred = new Deferred();
    queueMicrotask(this._queryByText.bind(this, text));
    return this._deferred.promise();
  }

  protected override _getByRec(rec: TKey): Promise<LookupResult<TKey>> {
    this._deferred = new Deferred();
    queueMicrotask(this._queryByRec.bind(this, rec));
    return this._deferred.promise();
  }
}
