/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import {AjaxSettings, ErrorDo, InitModelOf, ModelOf, objects, scout, strings} from '../index';

export interface AjaxErrorModel {
  jqXHR: JQuery.jqXHR;
  textStatus: JQuery.Ajax.ErrorTextStatus;
  errorThrown: string;
  requestOptions: AjaxSettings;
}

export class AjaxError extends Error implements AjaxErrorModel {
  declare model: AjaxErrorModel;

  jqXHR: JQuery.jqXHR;
  textStatus: JQuery.Ajax.ErrorTextStatus;
  errorDo: ErrorDo;
  errorThrown: string;
  requestOptions: AjaxSettings;

  constructor(model: InitModelOf<AjaxError>) {
    super();
    this.jqXHR = null;
    this.textStatus = null;
    this.errorThrown = null;
    this.requestOptions = null;
    $.extend(this, model);

    const errorDo = this.jqXHR?.responseJSON?.error;
    if (AjaxError.isErrorDo(errorDo)) {
      this.errorDo = scout.create(ErrorDo, errorDo);
    }
  }

  /**
   * @returns the request, e.g. `POST https://server.example/api` or an empty string if the request options are not available.
   */
  formatRequest(): string {
    return AjaxError.formatRequest(this.requestOptions);
  }

  /**
   * @returns the status, e.g. `500 Internal Server Error`. See {@link AjaxError.formatStatus}.
   */
  formatStatus(): string {
    return AjaxError.formatStatus(this.jqXHR, this.errorThrown, this.textStatus);
  }

  /**
   * @returns a message describing the failed request, e.g. `AJAX call "POST https://server.example/api" failed [500 Internal Server Error]`.
   */
  formatMessage(): string {
    return AjaxError.formatMessage(this.requestOptions, this.jqXHR, this.errorThrown, this.textStatus);
  }

  /**
   * @returns e.g. `AjaxError: AJAX call "POST https://server.example/api" failed [500 Internal Server Error]: Message of the ErrorDo`
   */
  override toString(): string {
    return strings.join(': ', 'AjaxError', this.formatMessage(), this.errorDo?.message);
  }

  /**
   * @returns the request, e.g. `POST https://server.example/api` or an empty string if the request options are not available.
   */
  static formatRequest(requestOptions: AjaxSettings): string {
    return requestOptions ? strings.join(' ', requestOptions.method, requestOptions.url) : '';
  }

  /**
   * @param textStatus the text status reported by jQuery. If there is no HTTP status, it is used to distinguish a `timeout` or an `abort` from a connection error.
   * @returns the HTTP status and the error thrown, e.g. `500 Internal Server Error`.
   * If there is no HTTP status, `timeout` or `abort` is returned for timed out or aborted requests, otherwise `Connection error`.
   */
  static formatStatus(jqXHR: JQuery.jqXHR, errorThrown: string, textStatus?: JQuery.Ajax.ErrorTextStatus): string {
    if (jqXHR?.status) {
      return strings.join(' ', jqXHR.status + '', errorThrown);
    }
    if (textStatus === 'timeout' || textStatus === 'abort') {
      return textStatus;
    }
    return 'Connection error';
  }

  /**
   * @returns a message describing the failed request, e.g. `AJAX call "POST https://server.example/api" failed [500 Internal Server Error]`.
   */
  static formatMessage(requestOptions: AjaxSettings, jqXHR: JQuery.jqXHR, errorThrown: string, textStatus?: JQuery.Ajax.ErrorTextStatus): string {
    return 'AJAX call' + strings.box(' "', AjaxError.formatRequest(requestOptions), '"') + ' failed' + strings.box(' [', AjaxError.formatStatus(jqXHR, errorThrown, textStatus), ']');
  }

  static isErrorDo(errorDo: any): errorDo is ModelOf<ErrorDo> {
    return errorDo?._type === 'scout.Error'
      || objects.isNumber(errorDo?.severityAsInt); // if rt.rest.jackson dependency is missing no _type is part of the response. But instead the severityAsInt property.
  }

  static extractErrorDo(error: any): ErrorDo {
    if (error instanceof AjaxError) {
      return error.errorDo;
    }
    return null;
  }
}
