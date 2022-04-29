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

export default class AbortableMicrotaskStaticLookupCall extends StaticLookupCall {
  constructor() {
    super();
    this._deferred = null;
  }

  abort() {
    this._deferred.reject({
      canceled: true
    });
    super.abort();
  }

  _getByKey(key) {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByKey.bind(this, this._deferred, key));
    return this._deferred.promise();
  }

  _getAll() {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByAll.bind(this, this._deferred));
    return this._deferred.promise();
  }

  _getByText(text) {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByText.bind(this, this._deferred, text));
    return this._deferred.promise();
  }

  _getByRec(rec) {
    this._deferred = $.Deferred();
    queueMicrotask(this._queryByRec.bind(this, this._deferred, rec));
    return this._deferred.promise();
  }
}
