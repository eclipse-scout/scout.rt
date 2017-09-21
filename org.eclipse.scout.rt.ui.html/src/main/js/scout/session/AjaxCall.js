scout.AjaxCall = function() {
  scout.AjaxCall.parent.call(this);
  this.type = 'ajax';
};
scout.inherits(scout.AjaxCall, scout.Call);

scout.AjaxCall.prototype.init = function(model) {
  if (!model) {
    throw new Error('Missing argument "model"');
  }
  if (!model.ajaxOptions) {
    throw new Error('Missing model property "ajaxOptions"');
  }
  if (!model.name) {
    model.name = this.ajaxOptions.url;
  }
  scout.AjaxCall.parent.prototype.init.call(this, model);
};

// ==================================================================================

scout.AjaxCall.prototype._callImpl = function() {
  // Mark retries by adding an URL parameter
  if (this.callCounter !== 1) {
    this.ajaxOptions.url = new scout.URL(this.ajaxOptions.url).setParameter('retry', this.callCounter - 1).toString({
      alwaysLast: ['retry']
    });
  }
  $.log.isTraceEnabled() && $.log.trace(this.logPrefix + (this.callCounter === 1 ? '--- ' : '') + this.ajaxOptions.type + ' "' + this.ajaxOptions.url + '"' + (this.callCounter === 1 ? ' ---' : ''));

  return $.ajax(this.ajaxOptions);
};

scout.AjaxCall.prototype._onCallDone = function(data, textStatus, jqXHR) {
  $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX success');
  scout.AjaxCall.parent.prototype._onCallDone.call(this, data, textStatus, jqXHR);
};

scout.AjaxCall.prototype._onCallFail = function(jqXHR, textStatus, errorThrown) {
  $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'AJAX fail: type=' + textStatus + ', httpStatus=' + jqXHR.status + (errorThrown ? ' "' + errorThrown + '"' : ''));
  scout.AjaxCall.parent.prototype._onCallFail.call(this, jqXHR, textStatus, errorThrown);
};

scout.AjaxCall.prototype._nextRetryImpl = function(jqXHR, textStatus, errorThrown) {
  var offlineError = scout.AjaxCall.isOfflineError(jqXHR, textStatus, errorThrown, this.request);
  if (!offlineError) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Unexpected HTTP error');
    return false;
  }
  return scout.AjaxCall.parent.prototype._nextRetryImpl.call(this);
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

scout.AjaxCall.isOfflineError = function(jqXHR, textStatus, errorThrown, request) {
  var offline = (
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
  return offline;
};
