/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AjaxError, Call, CallModel, InitModelOf, URL} from '../index';
import $ from 'jquery';

export class AjaxCall extends Call implements AjaxCallModel {
  declare model: AjaxCallModel;
  declare pendingCall: JQuery.jqXHR;

  ajaxOptions: JQuery.AjaxSettings;

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
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + (this.callCounter === 1 ? '--- ' : '') + this.ajaxOptions.type + ' "' + this.ajaxOptions.url + '"' + (this.callCounter === 1 ? ' ---' : ''));

    return $.ajax(this.ajaxOptions);
  }

  protected override _setResultFail(jqXHR: JQuery.jqXHR, textStatus: JQuery.Ajax.ErrorTextStatus, errorThrown: string) {
    // Store result as single object to make rethrowing the error easier for callers of AjaxCall
    this._setResult(new AjaxError({
      jqXHR: jqXHR,
      textStatus: textStatus,
      errorThrown: errorThrown,
      requestOptions: this.ajaxOptions
    }));
  }

  protected override _onCallDone(data: any, textStatus: JQuery.Ajax.SuccessTextStatus, jqXHR: JQuery.jqXHR) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX success');
    super._onCallDone(data, textStatus, jqXHR);
  }

  protected override _onCallFail(jqXHR: JQuery.jqXHR, textStatus: JQuery.Ajax.ErrorTextStatus, errorThrown: string) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX fail: type=' + textStatus + ', httpStatus=' + jqXHR.status + (errorThrown ? ' "' + errorThrown + '"' : ''));
    super._onCallFail(jqXHR, textStatus, errorThrown);
  }

  protected override _nextRetryImpl(jqXHR: JQuery.jqXHR, textStatus: JQuery.Ajax.ErrorTextStatus, errorThrown: string): number | boolean {
    let offlineError = AjaxCall.isOfflineError(jqXHR, textStatus, errorThrown);
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
   * Options for the jquery ajax call. At least the {@link CallModel.url} is required.
   */
  ajaxOptions: JQuery.AjaxSettings;
}
