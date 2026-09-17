/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AjaxError, Call, CallModel, InitModelOf, SomeRequired, URL, UrlAjaxSettings} from '../index';
import $ from 'jquery';

export class AjaxCall extends Call implements AjaxCallModel {
  declare model: AjaxCallModel;
  declare pendingCall: JQuery.jqXHR;
  declare initModel: SomeRequired<this['model'], 'ajaxOptions'>;

  ajaxOptions: UrlAjaxSettings;
  /**
   * textStatus/errorThrown captured from the raw jQuery ajax callbacks (see {@link _callImpl}), since a native
   * promise only ever carries a single value and cannot preserve jQuery's multi-argument done/fail signature.
   */
  protected _lastTextStatus: JQuery.Ajax.SuccessTextStatus | JQuery.Ajax.ErrorTextStatus;
  protected _lastErrorThrown: string;

  constructor() {
    super();
    this.type = 'ajax';
    this.ajaxOptions = null;
  }

  override init(model: InitModelOf<this>) {
    if (!model) {
      throw new Error('Missing argument "model"');
    }
    if (!model.ajaxOptions) {
      throw new Error('Missing model property "ajaxOptions"');
    }
    if (!model.name) {
      model.name = model.ajaxOptions.url;
    }
    super.init(model);
  }

  // ==================================================================================

  protected override _callImpl(): JQuery.jqXHR {
    // Mark retries by adding a URL parameter
    if (this.callCounter !== 1) {
      this.ajaxOptions.url = new URL(this.ajaxOptions.url).setParameter('retry', (this.callCounter - 1) + '').toString({
        alwaysLast: ['retry']
      });
    }
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + (this.callCounter === 1 ? '--- ' : '') + this.ajaxOptions.method + ' "' + this.ajaxOptions.url + '"' + (this.callCounter === 1 ? ' ---' : ''));

    let jqXHR = $.ajax(this.ajaxOptions);
    // Capture the extra arguments of jQuery's done/fail callbacks here (using jQuery's own multi-argument API,
    // since the underlying $.ajax() is out of scope for the native-promise migration), so that Call's generic
    // (single-value) then/catch handling further down the chain can still access them via these fields.
    jqXHR
      .done((data, textStatus) => {
        this._lastTextStatus = textStatus;
      })
      .fail((xhr, textStatus, errorThrown) => {
        this._lastTextStatus = textStatus;
        this._lastErrorThrown = errorThrown;
      });
    return jqXHR;
  }

  protected override _setResultFail(jqXHR?: JQuery.jqXHR) {
    // Store result as single object to make rethrowing the error easier for callers of AjaxCall
    this._setResult(new AjaxError({
      jqXHR: jqXHR,
      textStatus: this._lastTextStatus as JQuery.Ajax.ErrorTextStatus,
      errorThrown: this._lastErrorThrown,
      requestOptions: this.ajaxOptions
    }));
  }

  protected override _onCallDone(data?: any) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX success');
    super._onCallDone(data);
  }

  protected override _onCallFail(jqXHR?: JQuery.jqXHR) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX fail: type=' + this._lastTextStatus + ', httpStatus=' + jqXHR?.status + (this._lastErrorThrown ? ' "' + this._lastErrorThrown + '"' : ''));
    super._onCallFail(jqXHR);
  }

  protected override _nextRetryImpl(jqXHR?: JQuery.jqXHR): number | boolean {
    let offlineError = AjaxCall.isOfflineError(jqXHR, this._lastTextStatus as JQuery.Ajax.ErrorTextStatus, this._lastErrorThrown);
    if (!offlineError) {
      $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Unexpected HTTP error');
      return false;
    }
    return super._nextRetryImpl();
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static isOfflineError(jqXHR: JQuery.jqXHR, textStatus: JQuery.Ajax.ErrorTextStatus, errorThrown: string): boolean {
    return (
      // Status code = 0 -> no connection
      !jqXHR.status ||
      // Workaround for IE 9: Apparently, Windows network error codes (http://msdn.microsoft.com/en-us/library/aa383770%28VS.85%29.aspx)
      // are passed to JS as HTTP 'status' in some cases (e.g. when server goes offline).
      jqXHR.status >= 12000 ||
      // Status code 502 = Bad Gateway
      // Status code 503 = Service Unavailable
      // Status code 504 = Gateway Timeout
      // Those codes usually happen when some network component between browser and UI server (e.g. a load balancer)
      // has a short outage, most likely only temporarily. Therefore, we treat them like a lost connection.
      // Otherwise, the polling loop would break, eventually causing the HTTP session to be invalidated on the
      // server due to inactivity. Going offline starts the reconnector which regularly emits ping requests.
      // This allows us to reconnect to the server as soon as the connection is fixed, hopefully saving the
      // HTTP session from inactivation.
      jqXHR.status === 502 ||
      jqXHR.status === 503 ||
      jqXHR.status === 504
    );
  }

  protected override _abortImpl() {
    if (this.pendingCall && typeof this.pendingCall.abort === 'function') {
      this.pendingCall.abort();
    }
  }
}

export interface AjaxCallModel extends CallModel {
  /**
   * Options for the jquery ajax call. At least the {@link JQuery.UrlAjaxSettings.url} is required.
   */
  ajaxOptions?: UrlAjaxSettings;
}
