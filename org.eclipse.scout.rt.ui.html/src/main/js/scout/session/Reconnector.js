/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Reconnector = function(session) {
  this.session = session;

  // Additional delays are inserted when an AJAX call fails faster than this value.
  this.minAjaxDurationOnline = 500; // ms
  this.minAjaxDurationOffline = 1000; // ms
  // Intervals to be used for retries when a request fails while the session is still online.
  this.retryIntervals = [100, 500, 500, 500]; // ms
  // Interval to be used between pings while the session is offline (indefinite retries).
  this.reconnectInterval = 3000; // ms

  this._reset();
};

scout.Reconnector.prototype._reset = function() {
  var oldDeferred = this.deferred;
  this.started = false;
  this.deferred = $.Deferred();
  // When this array becomes empty, the session will be switched to "offline" mode
  this.remainingRetryIntervals = this.retryIntervals.slice();
  return oldDeferred;
};

scout.Reconnector.prototype.promise = function() {
  return this.deferred.promise();
};

scout.Reconnector.prototype.start = function() {
  if (this.started) { // Reconnection already in progress
    return;
  }
  this.started = true;
  if (this.session.offline) {
    $.log.trace('[ajax reconnector] start: ping while offline');
    setTimeout(this._pingWhileOffline.bind(this), 1000);
  } else {
    $.log.trace('[ajax reconnector] start: ping');
    this._ping();
  }
};

scout.Reconnector.prototype._markAjaxStart = function() {
  this.ajaxStartTimestamp = Date.now();
};

scout.Reconnector.prototype._getAjaxDuration = function() {
  return Date.now() - this.ajaxStartTimestamp;
};

// --------------------------------------------------------------

// +---------+                                +---------+
// | _ping() | --------> (success?) -Y------> | _sync() | --------> (success?) -Y------> [RESOLVE]
// +---------+               |                +---------+               |
//      .                    N                                          N
//     /|\                  \|/                                        \|/
//      |                    '                                          '
//      +--------------Y- (retry?) -N----+       +----------------Y- (retry?) -N----+
//      |                                |       |                                  |
//      +--------------------------------|-------+                                  |
//                                       |                                          |
//                                       +------------------------------------------+--> [REJECT]

scout.Reconnector.prototype._ping = function() {
  var pingAjaxOptions = this.session.defaultAjaxOptions({
    ping: true
  });

  $.log.trace('[ajax reconnector] > ' + pingAjaxOptions.url);
  this._markAjaxStart();
  this.session.registerAjaxRequest($.ajax(pingAjaxOptions)
    .done(this._onPingDone.bind(this))
    .fail(this._onPingFail.bind(this)));
};

scout.Reconnector.prototype._onPingDone = function(data, textStatus, jqXHR) {
  this.session.unregisterAjaxRequest(jqXHR);

  $.log.trace('[ajax reconnector] ping successful -> sync responses');
  this._sync();
};

scout.Reconnector.prototype._onPingFail = function(jqXHR, textStatus, errorThrown) {
  this.session.unregisterAjaxRequest(jqXHR);

  if (this.remainingRetryIntervals.length) {
    var delay = Math.max(this.minAjaxDurationOnline - this._getAjaxDuration(), 0);
    var retryInterval = delay + this.remainingRetryIntervals.shift();
    $.log.trace('[ajax reconnector] ping failed (' + (this.remainingRetryIntervals.length + 1) + ' retries remaining), ping again in ' + retryInterval + ' ms');
    setTimeout(this._ping.bind(this), retryInterval);
  } else {
    $.log.trace('[ajax reconnector] ping failed (no more retries) -> REJECT');
    this._reset().reject(jqXHR, textStatus, errorThrown);
  }
};

scout.Reconnector.prototype._sync = function() {
  var request = this.session._newRequest({
    syncResponseQueue: true
  });
  this.session.responseQueue.prepareRequest(request);
  var syncAjaxOptions = this.session.defaultAjaxOptions(request);

  $.log.trace('[ajax reconnector] > ' + syncAjaxOptions.url);
  this._markAjaxStart();
  this.session.registerAjaxRequest($.ajax(syncAjaxOptions)
      .done(this._onSyncDone.bind(this))
      .fail(this._onSyncFail.bind(this)));
};

scout.Reconnector.prototype._onSyncDone = function(data, textStatus, jqXHR) {
  this.session.unregisterAjaxRequest(jqXHR);

  $.log.trace('[ajax reconnector] sync successful -> RESOLVE');
  if (this.session.areRequestsPending()) {
    // Add response to queue, handle later by _performUserAjaxRequest()
    this.session.responseQueue.add(data);
  } else {
    // No user request pending, handle immediately
    this.session.responseQueue.process(data);
  }

  this._reset().resolve(data, textStatus, jqXHR);
};

scout.Reconnector.prototype._onSyncFail = function(jqXHR, textStatus, errorThrown) {
  this.session.unregisterAjaxRequest(jqXHR);

  if (this.remainingRetryIntervals.length) {
    var delay = Math.max(this.minAjaxDurationOnline - this._getAjaxDuration(), 0);
    var retryInterval = delay + this.remainingRetryIntervals.shift();
    $.log.trace('[ajax reconnector] sync failed (' + (this.remainingRetryIntervals.length + 1) + ' retries remaining), ping again in ' + retryInterval + ' ms');
    setTimeout(this._ping.bind(this), retryInterval);
  } else {
    $.log.trace('[ajax reconnector] sync failed, no more retries -> REJECT');
    this._reset().reject(jqXHR, textStatus, errorThrown);
  }
};

// --------------------------------------------------------------
//
// +---------------------+                       Y
// | _pingWhileOffline() | --------> (success?) --------> [RESOLVE]
// +---------------------+
//         .                             | N
//        /|\                            |
//         |          setTimeout         |
//         +-------------(X)-------------+
//

scout.Reconnector.prototype._pingWhileOffline = function() {
  this.session.onReconnecting();

  var pingAjaxOptions = this.session.defaultAjaxOptions({
    ping: true
  });

  $.log.trace('[ajax reconnector] > ' + pingAjaxOptions.url);
  this._markAjaxStart();
  this.session.registerAjaxRequest($.ajax(pingAjaxOptions)
    .done(this._onPingDoneWhileOffline.bind(this))
    .fail(this._onPingFailWhileOffline.bind(this)));
};

scout.Reconnector.prototype._onPingDoneWhileOffline = function(data, textStatus, jqXHR) {
  this.session.unregisterAjaxRequest(jqXHR);

  this.session.onReconnectingSucceeded();
  $.log.trace('[ajax reconnector] ping (while offline) successful -> RESOLVE');
  this._reset().resolve(data, textStatus, jqXHR);
};

scout.Reconnector.prototype._onPingFailWhileOffline = function(jqXHR, textStatus, errorThrown) {
  this.session.unregisterAjaxRequest(jqXHR);

  var ajaxDuration = this._getAjaxDuration();

  var handleFailedPing = function handleFailedPing() {
    $.log.trace('[ajax reconnector] ping (while offline) failed, ping again in ' + this.reconnectInterval + ' ms');
    this.session.onReconnectingFailed();
    setTimeout(this._pingWhileOffline.bind(this), this.reconnectInterval);
  }.bind(this);

  if (ajaxDuration < this.minAjaxDurationOffline) {
    // Wait at least a certain time before informing about connection failure (to prevent flickering of the reconnecting notification)
    setTimeout(handleFailedPing, this.minAjaxDurationOffline - ajaxDuration);
  } else {
    handleFailedPing();
  }
};
