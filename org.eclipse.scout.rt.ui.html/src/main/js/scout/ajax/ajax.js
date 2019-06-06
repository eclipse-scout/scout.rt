/**
 * Utility to perform Ajax requests in an easy way.<br>
 * It basically uses the class {@link scout.AjaxCall} and provides some common functions to call a REST backend.
 */
scout.ajax = {

  /**
   * Performs a HTTP GET request.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  get: function(url, options) {
    var opts = $.extend({}, {
      url: url,
      type: 'GET'
    }, options);
    return this.call(opts);
  },

  /**
   * Performs a HTTP POST request.
   * @param data the data to be sent.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  post: function(url, data, options) {
    var opts = $.extend({}, {
      url: url,
      type: 'POST',
      data: data
    }, options);
    return this.call(opts);
  },

  /**
   * Performs a HTTP PUT request.
   * @param data the data to be sent.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  put: function(url, data, options) {
    var opts = $.extend({}, {
      url: url,
      type: 'PUT',
      data: data
    }, options);
    return this.call(opts);
  },

  /**
   * Performs a HTTP DELETE request.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  remove: function(url, options) {
    var opts = $.extend({}, {
      url: url,
      type: 'DELETE'
    }, options);
    return this.call(opts);
  },

  /**
   * Performs a HTTP GET request using JSON as format for the request and the response.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  getJson: function(url, options) {
    var opts = $.extend({}, {
      url: url,
      type: 'GET'
    }, options);
    return this.callJson(opts);
  },

  /**
   * Performs a HTTP POST request using JSON as format for the request and the response.
   * @param data {(object|string)} the data to be sent. If the data is not a string it will be converted to a string using JSON.stringify().
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  postJson: function(url, data, options) {
    if (data && typeof data !== 'string') {
      data = JSON.stringify(data);
    }
    var opts = $.extend({}, {
      url: url,
      type: 'POST',
      data: data
    }, options);
    return this.callJson(opts);
  },

  /**
   * Performs a HTTP PUT request using JSON as format for the request and the response.
   * @param data {(object|string)} the data to be sent. If the data is not a string it will be converted to a string using JSON.stringify().
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  putJson: function(url, data, options) {
    if (data && typeof data !== 'string') {
      data = JSON.stringify(data);
    }
    var opts = $.extend({}, {
      url: url,
      type: 'PUT',
      data: data
    }, options);
    return this.callJson(opts);
  },

  /**
   * Performs a HTTP DELETE request using JSON as format for the request and the response.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  removeJson: function(url, options) {
    var opts = $.extend({}, {
      url: url,
      type: 'DELETE'
    }, options);
    return this.callJson(opts);
  },

  /**
   * Performs an Ajax request using JSON as format for the request and the response.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  callJson: function(options) {
    var opts = $.extend({}, {
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8'
    }, options);
    return this.call(opts);
  },

  /**
   * Performs an Ajax request.
   * @param [options] additional settings for the request.
   *        Since jQuery is used to perform the request, all the jQuery Ajax settings are accepted.
   * @returns {Promise} a promise which is resolved when the request succeeds.
   *          In case of an error the promise is rejected with an {@link scout.AjaxError} as argument.
   */
  call: function(options) {
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
        return $.rejectedPromise(new scout.AjaxError({
          jqXHR: jqXHR,
          textStatus: textStatus,
          errorThrown: errorThrown,
          requestOptions: opts
        }));
      }.bind(this));
  }
};
