/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AjaxCall, AjaxCallModel, AjaxError, BaseDoEntity, dataObjects, DoEntity, objects, scout} from '../index';
import $ from 'jquery';

/**
 * Utility to perform AJAX requests via HTTP in an easy way. It provides some common functions to call a REST backend using {@link AjaxCall}.
 */
export const ajax = {

  /**
   * Performs an HTTP GET request.
   *
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  get(url: string, options?: AjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    let opts = $.extend({}, {
      url: url,
      method: 'GET'
    }, options);
    return ajax.call(opts, model);
  },

  /**
   * Performs an HTTP POST request.
   *
   * @param data the data to be sent.
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  post(url: string, data?: any, options?: AjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    let opts = $.extend({}, {
      url: url,
      method: 'POST',
      data: data
    }, options);
    return ajax.call(opts, model);
  },

  /**
   * Performs an HTTP PUT request.
   *
   * @param data the data to be sent.
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  put(url: string, data?: any, options?: AjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    let opts = $.extend({}, {
      url: url,
      method: 'PUT',
      data: data
    }, options);
    return ajax.call(opts, model);
  },

  /**
   * Performs an HTTP DELETE request.
   *
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  remove(url: string, options?: AjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    let opts = $.extend({}, {
      url: url,
      method: 'DELETE'
    }, options);
    return ajax.call(opts, model);
  },

  /**
   * Performs an HTTP request.
   *
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  call(options: UrlAjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    return ajax.createCall(options, model).call();
  },

  /**
   * Prepares an {@link AjaxCall}, but does not execute it yet.
   *
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns the prepared {@link AjaxCall}. Execute it with the `call()` function.
   */
  createCall(options: UrlAjaxSettings, model?: AjaxCallModel): AjaxCall {
    const ajaxOptions = $.extend({}, {cache: false}, options);
    const ajaxCallModel = $.extend(true, {}, {ajaxOptions}, model);

    return scout.create(AjaxCall, ajaxCallModel);
  },

  // --------------------------------------

  /**
   * Performs an HTTP GET request using JSON as format for the request and the response.
   *
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  getJson(url: string, options?: AjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    let opts = $.extend({}, {
      url: url,
      method: 'GET'
    }, options);
    return ajax.callJson(opts, model);
  },

  /**
   * Performs an HTTP POST request using JSON as format for the request and the response.
   *
   * @param data the data to be sent. If the data is not a string it will be converted to a string using JSON.stringify().
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  postJson(url: string, data?: any, options?: AjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    if (!objects.isNullOrUndefined(data) && typeof data !== 'string') {
      data = JSON.stringify(data);
    }
    let opts = $.extend({}, {
      url: url,
      method: 'POST',
      data: data
    }, options);
    return ajax.callJson(opts, model);
  },

  /**
   * Performs an HTTP PUT request using JSON as format for the request and the response.
   *
   * @param data the data to be sent. If the data is not a string it will be converted to a string using JSON.stringify().
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  putJson(url: string, data?: any, options?: AjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    if (!objects.isNullOrUndefined(data) && typeof data !== 'string') {
      data = JSON.stringify(data);
    }
    let opts = $.extend({}, {
      url: url,
      method: 'PUT',
      data: data
    }, options);
    return ajax.callJson(opts, model);
  },

  /**
   * Performs an HTTP DELETE request using JSON as format for the request and the response.
   *
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  removeJson(url: string, options?: AjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    let opts = $.extend({}, {
      url: url,
      method: 'DELETE'
    }, options);
    return ajax.callJson(opts, model);
  },

  /**
   * Performs an HTTP request using JSON as format for the request and the response.
   * The default HTTP method is POST.
   *
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  callJson(options?: UrlAjaxSettings, model?: AjaxCallModel): JQuery.Promise<any, AjaxError> {
    return ajax.createCallJson(options, model).call();
  },

  /**
   * Prepares an HTTP call with JSON as format for the request and the response, but does not execute it yet.
   * The default HTTP method is POST.
   *
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall}.
   * @returns the prepared Ajax call object. Execute it with the call() function.
   */
  createCallJson(options?: UrlAjaxSettings, model?: AjaxCallModel): AjaxCall {
    let opts = $.extend({}, {
      method: 'POST',
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8'
    }, options);
    return ajax.createCall(opts, model);
  },

  // --------------------------------------

  /**
   * Performs an HTTP GET request to the given URL and converts the response to a data object.
   *
   * @param url The URL of the request.
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall} which is created to perform the request.
   * @returns A promise which is resolved when the request succeeds. The response is automatically converted to a {@link BaseDoEntity} or `null`.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  getDataObject<TDoOut extends BaseDoEntity>(
    url: string,
    options?: AjaxSettings,
    model?: AjaxCallModel
  ): JQuery.Promise<TDoOut, AjaxError> {
    const opts: UrlAjaxSettings = $.extend({}, {
      url: url,
      method: 'GET'
    }, options);
    return ajax.callDataObject(opts, null, model);
  },

  /**
   * Sends the given data object with an HTTP POST request to the given URL. The result is expected to be a data object as well.
   *
   * @param url The URL of the request.
   * @param dataObject The data object to send or `null`.
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall} which is created to perform the request.
   * @returns A promise which is resolved when the request succeeds. The response is automatically converted to a {@link BaseDoEntity} or `null`.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  postDataObject<TDoIn extends BaseDoEntity | void, TDoOut extends BaseDoEntity | void>(
    url: string,
    dataObject?: TDoIn,
    options?: AjaxSettings,
    model?: AjaxCallModel
  ): JQuery.Promise<TDoOut, AjaxError> {
    const opts: UrlAjaxSettings = $.extend({}, {
      url: url,
      method: 'POST'
    }, options);
    return ajax.callDataObject(opts, dataObject, model);
  },

  /**
   * Sends the given data object with an HTTP PUT request to the given URL. The result is expected to be a data object as well.
   *
   * @param url The URL of the request.
   * @param dataObject The data object to send or `null`.
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall} which is created to perform the request.
   * @returns A promise which is resolved when the request succeeds. The response is automatically converted to a {@link BaseDoEntity} or `null`.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  putDataObject<TDoIn extends BaseDoEntity | void, TDoOut extends BaseDoEntity | void>(
    url: string,
    dataObject?: TDoIn,
    options?: AjaxSettings,
    model?: AjaxCallModel
  ): JQuery.Promise<TDoOut, AjaxError> {
    const opts: UrlAjaxSettings = $.extend({}, {
      url: url,
      method: 'PUT'
    }, options);
    return ajax.callDataObject(opts, dataObject, model);
  },

  /**
   * Sends the given data object with an HTTP DELETE request to the given URL. The result is expected to be a data object as well.
   *
   * @param url The URL of the request.
   * @param dataObject The data object to send or `null`.
   * @param options additional settings for the request.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param model additional properties for the {@link AjaxCall} which is created to perform the request.
   * @returns A promise which is resolved when the request succeeds. The response is automatically converted to a {@link BaseDoEntity} or `null`.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  removeDataObject<TDoIn extends BaseDoEntity | void, TDoOut extends BaseDoEntity | void>(
    url: string,
    dataObject?: TDoIn,
    options?: AjaxSettings,
    model?: AjaxCallModel
  ): JQuery.Promise<TDoOut, AjaxError> {
    const opts: UrlAjaxSettings = $.extend({}, {
      url: url,
      method: 'DELETE'
    }, options);
    return ajax.callDataObject(opts, dataObject, model);
  },

  /**
   * Sends the given data object with an HTTP request. The result is expected to be a data object as well.
   *
   * @param options Settings for the request. Must contain the properties `url` and `method`.
   *        Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param dataObject The data object to send or `null`.
   * @param model additional properties for the {@link AjaxCall} which is created to perform the request.
   * @returns A promise which is resolved when the request succeeds. The response is automatically converted to a {@link BaseDoEntity} or `null`.
   *          In case of an error the promise is rejected with an {@link AjaxError}.
   */
  callDataObject<TDoIn extends BaseDoEntity | void, TDoOut extends BaseDoEntity | void>(
    options: UrlAjaxSettings,
    dataObject?: TDoIn,
    model?: AjaxCallModel
  ): JQuery.Promise<TDoOut, AjaxError> {
    return ajax.createCallDataObject(options, dataObject, model).call();
  },

  /**
   * Creates an {@link AjaxCall} which sends the given data object as json.
   *
   * @param options Settings for the request. Must contain the property 'url' and 'method'.
   *         Since jQuery is used to perform the request, all {@link https://api.jquery.com/jQuery.ajax/ jQuery.ajax} settings are accepted.
   * @param dataObject The data object to send or `null`.
   * @param model Properties for the {@link AjaxCall} which is created.
   * @returns an {@link AjaxCall} which sends the given data object, pre-configured using the given options and model. The result of the call is converted to a data object automatically.
   */
  createCallDataObject(options: UrlAjaxSettings, dataObject?: DoEntity | void, model?: AjaxCallModel): AjaxCall {
    const json = dataObject && dataObjects.stringify(dataObject);
    const opts: AjaxSettings = $.extend({}, {
      converters: {
        'text json': data => dataObjects.parse(data)
      },
      data: json || undefined
    }, options);
    return this.createCallJson(opts, model);
  }
};

/**
 * Like JQuery.AjaxSettings without 'type' property. Use 'method' instead.
 */
export type AjaxSettings = Omit<JQuery.AjaxSettings, 'type'>;
/**
 * Like JQuery.UrlAjaxSettings without 'type' property. Use 'method' instead.
 */
export type UrlAjaxSettings = AjaxSettings & { url: string };
