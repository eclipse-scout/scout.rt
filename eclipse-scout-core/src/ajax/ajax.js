/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AjaxCall, AjaxError, scout} from '../index';
import $ from 'jquery';

/**
 * Utility to perform Ajax requests in an easy way.<br>
 * It basically uses the class {@link AjaxCall} and provides some common functions to call a REST backend.
 */

/**
 * Performs a HTTP GET request.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function get(url, options) {
  let opts = $.extend({}, {
    url: url,
    type: 'GET'
  }, options);
  return call(opts);
}

/**
 * Performs a HTTP POST request.
 * @param data the data to be sent.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function post(url, data, options) {
  let opts = $.extend({}, {
    url: url,
    type: 'POST',
    data: data
  }, options);
  return call(opts);
}

/**
 * Performs a HTTP PUT request.
 * @param data the data to be sent.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function put(url, data, options) {
  let opts = $.extend({}, {
    url: url,
    type: 'PUT',
    data: data
  }, options);
  return call(opts);
}

/**
 * Performs a HTTP DELETE request.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function remove(url, options) {
  let opts = $.extend({}, {
    url: url,
    type: 'DELETE'
  }, options);
  return call(opts);
}

/**
 * Performs a HTTP GET request using JSON as format for the request and the response.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function getJson(url, options) {
  let opts = $.extend({}, {
    url: url,
    type: 'GET'
  }, options);
  return callJson(opts);
}

/**
 * Performs a HTTP POST request using JSON as format for the request and the response.
 * @param data {(object|string)} the data to be sent. If the data is not a string it will be converted to a string using JSON.stringify().
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function postJson(url, data, options) {
  if (data && typeof data !== 'string') {
    data = JSON.stringify(data);
  }
  let opts = $.extend({}, {
    url: url,
    type: 'POST',
    data: data
  }, options);
  return callJson(opts);
}

/**
 * Performs a HTTP PUT request using JSON as format for the request and the response.
 * @param data {(object|string)} the data to be sent. If the data is not a string it will be converted to a string using JSON.stringify().
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function putJson(url, data, options) {
  if (data && typeof data !== 'string') {
    data = JSON.stringify(data);
  }
  let opts = $.extend({}, {
    url: url,
    type: 'PUT',
    data: data
  }, options);
  return callJson(opts);
}

/**
 * Performs a HTTP DELETE request using JSON as format for the request and the response.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function removeJson(url, options) {
  let opts = $.extend({}, {
    url: url,
    type: 'DELETE'
  }, options);
  return callJson(opts);
}

/**
 * Performs an Ajax request using JSON as format for the request and the response.
 * The default HTTP method is POST.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function callJson(options) {
  return createCallJson(options).call();
}

/**
 * Performs an Ajax request.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function call(options) {
  return createCall(options).call();
}

/**
 * Prepares an Ajax call with JSON as format for the request and the response,
 * but does not execute it yet. The default HTTP method is POST.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {AjaxCall} the prepared Ajax call object. Execute it with the call() function.
 */
export function createCallJson(options) {
  let opts = $.extend({}, {
    type: 'POST',
    dataType: 'json',
    contentType: 'application/json; charset=UTF-8'
  }, options);
  return createCall(opts);
}

/**
 * Prepares an Ajax call, but does not execute it yet.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {AjaxCall} the prepared Ajax call object. Execute it with the call() function.
 */
export function createCall(options) {
  let opts = $.extend({}, {
    cache: false
  }, options);

  return scout.create('AjaxCall', {
    ajaxOptions: opts
  }, {
    ensureUniqueId: false
  });
}

export default {
  createCall,
  createCallJson,
  call,
  callJson,
  get,
  getJson,
  post,
  postJson,
  put,
  putJson,
  remove,
  removeJson
};
