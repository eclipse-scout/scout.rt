/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AjaxError, Call, URL} from '../index';
import $ from 'jquery';

export default class AjaxCall extends Call {

  constructor() {
    super();
    this.type = 'ajax';
    this.ajaxOptions = null;
  }

  init(model) {
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

  _callImpl() {
    // Mark retries by adding an URL parameter
    if (this.callCounter !== 1) {
      this.ajaxOptions.url = new URL(this.ajaxOptions.url).setParameter('retry', this.callCounter - 1).toString({
        alwaysLast: ['retry']
      });
    }
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + (this.callCounter === 1 ? '--- ' : '') + this.ajaxOptions.type + ' "' + this.ajaxOptions.url + '"' + (this.callCounter === 1 ? ' ---' : ''));

    return $.ajax(this.ajaxOptions);
  }

  _setResultFail(jqXHR, textStatus, errorThrown) {
    // Store result as single object to make rethrowing the error easier for callers of AjaxCall
    this._setResult(new AjaxError({
      jqXHR: jqXHR,
      textStatus: textStatus,
      errorThrown: errorThrown,
      requestOptions: this.ajaxOptions
    }));
  }

  _onCallDone(data, textStatus, jqXHR) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX success');
    super._onCallDone(data, textStatus, jqXHR);
  }

  _onCallFail(jqXHR, textStatus, errorThrown) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX fail: type=' + textStatus + ', httpStatus=' + jqXHR.status + (errorThrown ? ' "' + errorThrown + '"' : ''));
    super._onCallFail(jqXHR, textStatus, errorThrown);
  }

  _nextRetryImpl(jqXHR, textStatus, errorThrown) {
    let offlineError = AjaxCall.isOfflineError(jqXHR, textStatus, errorThrown, this.request);
    if (!offlineError) {
      $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Unexpected HTTP error');
      return false;
    }
    return super._nextRetryImpl();
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static isOfflineError(jqXHR, textStatus, errorThrown, request) {
    // noinspection UnnecessaryLocalVariableJS
    let offline = (
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
    return offline;
  }
}
