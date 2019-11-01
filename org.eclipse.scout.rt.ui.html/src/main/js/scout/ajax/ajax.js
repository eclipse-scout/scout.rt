import {AjaxError} from '../index';
import {AjaxCall} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';

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
  var opts = $.extend({}, {
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
  var opts = $.extend({}, {
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
  var opts = $.extend({}, {
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
  var opts = $.extend({}, {
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
  var opts = $.extend({}, {
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
  var opts = $.extend({}, {
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
  var opts = $.extend({}, {
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
  var opts = $.extend({}, {
    url: url,
    type: 'DELETE'
  }, options);
  return callJson(opts);
}

/**
 * Performs an Ajax request using JSON as format for the request and the response.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function callJson(options) {
  var opts = $.extend({}, {
    dataType: 'json',
    contentType: 'application/json; charset=UTF-8'
  }, options);
  return call(opts);
}

/**
 * Performs an Ajax request.
 * @param [options] additional settings for the request.
 *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
 * @returns {Promise} a promise which is resolved when the request succeeds.
 *          In case of an error the promise is rejected with an {@link AjaxError} as argument.
 */
export function call(options) {
  var opts = $.extend({}, {
    cache: false
  }, options);

  var ajaxCall = scout.create('AjaxCall', {
    ajaxOptions: opts
  }, {
    ensureUniqueId: false
  });
  return ajaxCall.call()
    .catch(function(jqXHR, textStatus, errorThrown) {
      // Wrap arguments in an object to make rethrowing the error easier.
      return $.rejectedPromise(new AjaxError({
        jqXHR: jqXHR,
        textStatus: textStatus,
        errorThrown: errorThrown,
        requestOptions: opts
      }));
    }.bind(this));
}

export default {
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
