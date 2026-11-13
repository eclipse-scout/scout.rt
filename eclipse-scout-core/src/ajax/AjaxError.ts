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
  textStatus?: JQuery.Ajax.ErrorTextStatus;
  errorThrown?: string;
  requestOptions?: AjaxSettings;
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
    $.extend(this, model);

    const errorDo = this.jqXHR?.responseJSON?.error;
    if (errorDo instanceof ErrorDo) {
      this.errorDo = errorDo;
    } else if (AjaxError.isErrorDo(errorDo)) {
      this.errorDo = scout.create(ErrorDo, errorDo);
    }

    // Name and message are used by the default Error.toString() implementation
    // They are also used when the browser is printing errors on the console (which does _not_ use toString())
    this.name = 'AjaxError';
  }

  override get message(): string {
    // A getter is used to format the message only when needed and (e.g. not on aborted ajax calls)
    // The message also stays current if jqXHR or requestOptions change later
    return this.formatMessage();
  }

  /**
   * @returns the request, e.g. `POST https://server.example/api` or an empty string if the request options are not available.
   */
  formatRequest(): string {
    if (!this.requestOptions) {
      return '';
    }
    return strings.join(' ', this.requestOptions.method, this.requestOptions.url);
  }

  /**
   * @param textStatus the text status reported by jQuery. If there is no HTTP status, it is used to distinguish a `timeout` or an `abort` from a connection error.
   * @returns the HTTP status and the error thrown, e.g. `500 Internal Server Error`.
   * If there is no HTTP status, `timeout` or `abort` is returned for timed out or aborted requests, otherwise `Connection error`.
   */
  formatStatus(): string {
    if (this.jqXHR?.status) {
      return strings.join(' ', this.jqXHR.status + '', this.errorThrown);
    }
    if (this.textStatus === 'timeout' || this.textStatus === 'abort') {
      return this.textStatus;
    }
    return 'Connection error';
  }

  /**
   * Creates a message describing the failed request, e.g. `AJAX call "POST https://server.example/api" failed [500 Internal Server Error]`.
   *
   * If `includeErrorDo` is true, the message of the errorDo is prepended on a separate line, e.g.
   * ```
   * Message of the ErrorDo
   * AJAX call "POST https://server.example/api" failed [500 Internal Server Error]
   * ```
   * @param includeErrorDo whether to include the message of the errorDo. Default is true.
   */
  formatMessage(includeErrorDo = true): string {
    let msg = 'AJAX call' + strings.box(' "', this.formatRequest(), '"') + ' failed' + strings.box(' [', this.formatStatus(), ']');
    if (includeErrorDo) {
      msg = strings.join('\n', this.errorDo?.message, msg);
    }
    return msg;
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
