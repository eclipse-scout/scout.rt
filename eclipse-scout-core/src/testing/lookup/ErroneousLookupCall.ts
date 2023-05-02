/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {StaticLookupCall} from '@eclipse-scout/core';

export class ErroneousLookupCall extends StaticLookupCall<any> {
  protected override _queryByAll() {
    this._resolveWithException('QueryByAll failed');
  }

  protected override _queryByKey(key: string) {
    this._resolveWithException('QueryByKey failed');
  }

  protected override _queryByRec(rec: string) {
    this._resolveWithException('QueryByRec failed');
  }

  protected override _queryByText(text: string) {
    this._resolveWithException('QueryByText failed');
  }

  protected _resolveWithException(exception: string) {
    this._deferred.resolve(this._createLookupResult([], exception));
  }
}
