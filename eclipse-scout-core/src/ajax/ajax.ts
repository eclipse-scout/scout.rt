/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AjaxCall, scout} from '../index';
import $ from 'jquery';

/**
 * Utility to perform Ajax requests in an easy way.
 * It basically uses the class {@link AjaxCall} and provides some common functions to call a REST backend.
 */
export const ajax = {
  /**
   * Performs an HTTP GET request.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  get(url: string, options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    let opts = $.extend({}, {
      url: url,
      type: 'GET'
    }, options);
    return ajax.call(opts);
  },

  /**
   * Performs an HTTP POST request.
   * @param data the data to be sent.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  post(url: string, data?: any, options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    let opts = $.extend({}, {
      url: url,
      type: 'POST',
      data: data
    }, options);
    return ajax.call(opts);
  },

  /**
   * Performs an HTTP PUT request.
   * @param data the data to be sent.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  put(url: string, data: any, options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    let opts = $.extend({}, {
      url: url,
      type: 'PUT',
      data: data
    }, options);
    return ajax.call(opts);
  },

  /**
   * Performs an HTTP DELETE request.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  remove(url: string, options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    let opts = $.extend({}, {
      url: url,
      type: 'DELETE'
    }, options);
    return ajax.call(opts);
  },

  /**
   * Performs an HTTP GET request using JSON as format for the request and the response.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  getJson(url: string, options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    let opts = $.extend({}, {
      url: url,
      type: 'GET'
    }, options);
    return ajax.callJson(opts);
  },

  /**
   * Performs an HTTP POST request using JSON as format for the request and the response.
   * @param data the data to be sent. If the data is not a string it will be converted to a string using JSON.stringify().
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  postJson(url: string, data: any, options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    if (data && typeof data !== 'string') {
      data = JSON.stringify(data);
    }
    let opts = $.extend({}, {
      url: url,
      type: 'POST',
      data: data
    }, options);
    return ajax.callJson(opts);
  },

  /**
   * Performs an HTTP PUT request using JSON as format for the request and the response.
   * @param data the data to be sent. If the data is not a string it will be converted to a string using JSON.stringify().
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  putJson(url: string, data: any, options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    if (data && typeof data !== 'string') {
      data = JSON.stringify(data);
    }
    let opts = $.extend({}, {
      url: url,
      type: 'PUT',
      data: data
    }, options);
    return ajax.callJson(opts);
  },

  /**
   * Performs an HTTP DELETE request using JSON as format for the request and the response.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  removeJson(url: string, options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    let opts = $.extend({}, {
      url: url,
      type: 'DELETE'
    }, options);
    return ajax.callJson(opts);
  },

  /**
   * Performs an Ajax request using JSON as format for the request and the response.
   * The default HTTP method is POST.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  callJson(options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    return ajax.createCallJson(options).call();
  },

  /**
   * Performs an Ajax request.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
   */
  call(options?: JQuery.AjaxSettings): JQuery.Promise<any> {
    return ajax.createCall(options).call();
  },

  /**
   * Prepares an Ajax call with JSON as format for the request and the response,
   * but does not execute it yet. The default HTTP method is POST.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns the prepared Ajax call object. Execute it with the call() function.
   */
  createCallJson(options?: JQuery.AjaxSettings): AjaxCall {
    let opts = $.extend({}, {
      type: 'POST',
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8'
    }, options);
    return ajax.createCall(opts);
  },

  /**
   * Prepares an Ajax call, but does not execute it yet.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns the prepared Ajax call object. Execute it with the call() function.
   */
  createCall(options?: JQuery.AjaxSettings): AjaxCall {
    let opts = $.extend({}, {
      cache: false
    }, options);

    return scout.create(AjaxCall, {
      ajaxOptions: opts
    }, {
      ensureUniqueId: false
    });
  }
};
