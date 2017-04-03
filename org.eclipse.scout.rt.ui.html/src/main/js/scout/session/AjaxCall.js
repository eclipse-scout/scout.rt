scout.AjaxCall = function(session) {
  this.session = session;

  this.logPrefix = '[ajax call] ';
  this.deferred = $.Deferred();
};

scout.AjaxCall.prototype.call = function(callOptions) {
  this._prepareCall(callOptions);

  $.log.trace(this.logPrefix + '----- URL: ' + this.ajaxOptions.url + ' -----');
  if (this.session.reconnector.started) {
    // Reconnector is already trying to connect. Wait for the outcome of the process.
    // If connection was reestablished, perform the AJAX call. Otherwise, inform the caller
    // about the failed connection. (In the later case, the passed arguments will be the
    // objects of the last ?ping request, because no actual AJAX call was made yet.)
    $.log.trace(this.logPrefix + 'Waiting for reconnector promise...');
    this._waitForReconnect();
  } else {
    // Connection seems fine, perform AJAX call (reconnection will start when this call fails)
    this._performAjaxCall();
  }

  return this.deferred.promise();
};

scout.AjaxCall.prototype._prepareCall = function(callOptions) {
  if (!callOptions || !callOptions.ajaxOptions) {
    throw new Error('Invalid call options');
  }
  this.ajaxOptions = callOptions.ajaxOptions;
  this.request = callOptions.request;
  this.reconnectorEnabled = scout.nvl(callOptions.reconnectorEnabled, true);

  if (this.request) {
    this.logPrefix = '[ajax call' + scout.strings.box(' #', this.request['#'], '') + '] ';
  }
};

//scout.AjaxCall.prototype.call = function(ajaxOptions) {
//  this.ajaxOptions = ajaxOptions;
//
//  // First try
//  $.log.trace(this.logPrefix + '----- URL: ' + ajaxOptions.url + ' -----');
//  if (this.session.reconnector.started) {
//    // Reconnector is already trying to connect. Wait for the outcome of the process.
//    // If connection was reestablished, perform the AJAX call. Otherwise, inform the caller
//    // about the failed connection. (In the later case, the passed arguments will be the
//    // objects of the last ?ping request, because no actual AJAX call was made yet.)
//    $.log.trace(this.logPrefix + 'Waiting for reconnector promise...');
//    this._waitForReconnect();
//  } else {
//    // Connection seems fine, perform AJAX call (reconnection will start when this call fails)
//    this._performAjaxCall();
//  }
//
//  return this.deferred.promise();
//};

scout.AjaxCall.prototype._performAjaxCall = function() {
  $.log.trace(this.logPrefix + '> ' + this.ajaxOptions.url);
  this.session.registerAjaxRequest($.ajax(this.ajaxOptions)
    .done(onAjaxDone.bind(this))
    .fail(onAjaxFail.bind(this)));

  // ----- Helper methods -----

  function onAjaxDone(data, textStatus, jqXHR) {
    this.session.unregisterAjaxRequest(jqXHR);
    $.log.trace(this.logPrefix + '{OK} AJAX success');
    this._resolve(data, textStatus, jqXHR);
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    this.session.unregisterAjaxRequest(jqXHR);
    $.log.trace(this.logPrefix + '{ERROR} AJAX fail (type=' + textStatus + ', httpStatus=' + jqXHR.status + (errorThrown ? ' "' + errorThrown + '"' : '') + ')');
    this._waitForReconnect();
  }
};

scout.AjaxCall.prototype._resolve = function() {
  this.deferred.resolve.apply(this.deferred, arguments);
};

scout.AjaxCall.prototype._reject = function() {
  this.deferred.reject.apply(this.deferred, arguments);
};

scout.AjaxCall.prototype._waitForReconnect = function() {
  this.session.reconnector.promise()
    .done(this._performAjaxCall.bind(this))
    .fail(this._reject.bind(this));
  this.session.reconnector.start();
};
