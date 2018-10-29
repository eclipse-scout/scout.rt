#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
scout.ajax = {

  post: function(url, data, opts) {
    var options = ${symbol_dollar}.extend({}, {
      url: url,
      data: data,
      type: 'POST'
    }, opts);
    return this.call(options);
  },

  get: function(url, opts) {
    var options = ${symbol_dollar}.extend({}, {
      url: url,
      type: 'GET'
    }, opts);
    return this.call(options);
  },

  remove: function(url, opts) {
    var options = ${symbol_dollar}.extend({}, {
      url: url,
      type: 'DELETE'
    }, opts);
    return this.call(options);
  },

  put: function(url, opts) {
    var options = ${symbol_dollar}.extend({}, {
      url: url,
      type: 'PUT'
    }, opts);
    return this.call(options);
  },

  putJson: function(url, data, opts) {
    var options = ${symbol_dollar}.extend({}, {
      url: url,
      type: 'PUT',
      data: data
    }, opts);
    return this.callJson(options);
  },

  removeJson: function(url, opts) {
    var options = ${symbol_dollar}.extend({}, {
      type: 'DELETE',
      url: url
    }, opts);
    return this.callJson(options);
  },

  getJson: function(url, opts) {
    var options = ${symbol_dollar}.extend({}, {
      url: url,
      type: 'GET'
    }, opts);
    return this.callJson(options);
  },

  postJson: function(url, data, opts) {
    var options = ${symbol_dollar}.extend({}, {
      type: 'POST',
      url: url,
      data: data
    }, opts);
    return this.callJson(options);
  },

  callJson: function(opts) {
    var options = ${symbol_dollar}.extend({}, {
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8',
    }, opts);
    return this.call(options);
  },

  call: function(opts) {
    var options = ${symbol_dollar}.extend({}, {
      cache: false
    }, opts);

    var ajaxCall = scout.create('AjaxCall', {
      ajaxOptions: options,
      maxRetries: 0
    }, {
      ensureUniqueId: false
    });
    return ajaxCall.call();
  }

};
