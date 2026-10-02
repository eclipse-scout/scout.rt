/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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
  declare initModel: SomeRequired<this['model'], 'ajaxOptions'>;

  ajaxOptions: UrlAjaxSettings;
  /**
   * The {@link JQuery.jqXHR} of the current or most recent request (successful or not), e.g. to access the HTTP status code
   * once the promise returned by {@link call} has settled. It is also used to abort a pending request.
   *
   * Only accessible on {@link AjaxCall} instances created directly, e.g. using {@link ajax.createCall}/{@link ajax.createCallJson}:
   * the shorthand functions like {@link ajax.get}/{@link ajax.getJson} only ever return the resolved response body,
   * since a native promise cannot carry the jqXHR alongside it the way a jQuery done/fail callback could.
   */
  xhr: JQuery.jqXHR;

  constructor() {
    super();
    this.type = 'ajax';
    this.ajaxOptions = null;
    this.xhr = null;
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

  protected override _callImpl(): Promise<any> {
    // Mark retries by adding a URL parameter
    if (this.callCounter !== 1) {
      this.ajaxOptions.url = new URL(this.ajaxOptions.url).setParameter('retry', (this.callCounter - 1) + '').toString({
        alwaysLast: ['retry']
      });
    }
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + (this.callCounter === 1 ? '--- ' : '') + this.ajaxOptions.method + ' "' + this.ajaxOptions.url + '"' + (this.callCounter === 1 ? ' ---' : ''));

    let jqXHR = $.ajax(this.ajaxOptions);
    // pendingCall (inherited from Call) is the native promise wrapping the jqXHR and has no abort() method -> keep the jqXHR to abort it
    this.xhr = jqXHR;
    // Capture the extra arguments of jQuery's done/fail callbacks and create a native promise.
    // Note: use the two-argument form of then() (rather than .then(fn).catch(fn)) so success and failure each
    // incur exactly one jQuery.Deferred#then() scheduling hop (jQuery defers then() reactions via setTimeout,
    // even for an already-settled Deferred). Chaining .then(fn).catch(fn) instead would need two such hops for
    // a rejection (one to pass it through the first then(), one for catch() to actually handle it), which can
    // let an unrelated, later-settling success elsewhere resolve before an earlier rejection is fully processed.
    return new Promise((resolve, reject) => {
      jqXHR.then(
        (data, textStatus) => {
          resolve(data);
        },
        (xhr, textStatus, errorThrown) => {
          reject(new AjaxError({
            jqXHR,
            textStatus,
            errorThrown,
            requestOptions: this.ajaxOptions
          }));
        }
      );
    });
  }

  protected override _onCallDone(data?: any) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX success');
    super._onCallDone(data);
  }

  protected override _onCallFail(error: AjaxError) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX fail: type=' + error.textStatus + ', httpStatus=' + error.jqXHR?.status + (error.errorThrown ? ' "' + error.errorThrown + '"' : ''));
    super._onCallFail(error);
  }

  protected override _nextRetryImpl(error: AjaxError): number | boolean {
    let offlineError = AjaxCall.isOfflineError(error);
    if (!offlineError) {
      $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Unexpected HTTP error');
      return false;
    }
    return super._nextRetryImpl(error);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static isOfflineError(error: AjaxError): boolean {
    const jqXHR = error.jqXHR;
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
    // aborting an already completed request has no effect
    this.xhr?.abort();
  }
}

export interface AjaxCallModel extends CallModel {
  /**
   * Options for the jquery ajax call. At least the {@link JQuery.UrlAjaxSettings.url} is required.
   */
  ajaxOptions?: UrlAjaxSettings;
}
