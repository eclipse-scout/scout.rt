// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Reconnector = function(session) {
  this.session = session;
  this.interval = 3000;
};

scout.Reconnector.prototype.start = function() {
  this._schedulePing();
};

scout.Reconnector.prototype._schedulePing = function() {
  if (this.pingScheduled) {
    return;
  }

  this.pingScheduled = true;
  setTimeout(function() {
    this.ping();
    this.pingScheduled = false;
  }.bind(this), this.interval);
};

scout.Reconnector.prototype.ping = function() {
  this.session.onReconnecting();

  var request = {
    ping: true
  };

  var that = this;

  $.ajax({
    async: true,
    type: 'POST',
    dataType: 'json',
    contentType: 'application/json',
    cache: false,
    url: this.session.url,
    data: JSON.stringify(request),
    context: request
  })
  .done(function(data) {
    that._onSuccess(data);
  })
  .fail(function(jqXHR, textStatus, errorThrown) {
    var request = this;
    that._onFailure(request, jqXHR, textStatus, errorThrown);
  });
};

scout.Reconnector.prototype._onSuccess = function() {
  this.session.onReconnectingSucceeded();
};

scout.Reconnector.prototype._onFailure = function() {
  this.session.onReconnectingFailed();
  this._schedulePing();
};
