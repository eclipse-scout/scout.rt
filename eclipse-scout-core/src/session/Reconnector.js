/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import $ from 'jquery';

export default class Reconnector {

  constructor(session) {
    this.session = session;
    this.started = false;

    // Delay before first ping
    this.initialDelay = 1000; // ms
    // Interval to be used between pings (indefinite retries).
    this.interval = 3000; // ms
    // Minimal assumed ping duration (to prevent flickering of the reconnect notification when AJAX call fails very fast)
    this.minPingDuration = 1000; // ms
  }

  start() {
    if (this.started) {
      return;
    }

    $.log.isTraceEnabled() && $.log.trace('[ajax reconnector] start');
    this.started = true;
    this._schedulePing(this.initialDelay);
  }

  stop() {
    this.started = false;
  }

  _schedulePing(delay) {
    $.log.isTraceEnabled() && $.log.trace('[ajax reconnector] schedule ping() in ' + delay + ' ms');
    setTimeout(this._ping.bind(this), delay);
  }

  //
  //   [START]
  //      |
  //      v
  // +---------+          .--------.  (yes)
  // | _ping() | ------> < success? > ------> [END]
  // +---------+          '--------'
  //      ^                   |(no)
  //      |                   |
  //      +-------------------+
  //
  _ping() {
    this.session.onReconnecting();

    let pingAjaxOptions = this.session.defaultAjaxOptions({
      ping: true
    });

    $.log.isTraceEnabled() && $.log.trace('[ajax reconnector] ' + pingAjaxOptions.type + ' "' + pingAjaxOptions.url + '"');
    this.pingStartTimestamp = Date.now();
    $.ajax(pingAjaxOptions)
      .done(this._onPingDone.bind(this))
      .fail(this._onPingFail.bind(this));
  }

  _onPingDone(data, textStatus, jqXHR) {
    $.log.isTraceEnabled() && $.log.trace('[ajax reconnector] ping success -> connection re-established!');
    this.session.onReconnectingSucceeded();
    this.stop();
  }

  _onPingFail(jqXHR, textStatus, errorThrown) {
    let handleFailedPing = function handleFailedPing() {
      $.log.isTraceEnabled() && $.log.trace('[ajax reconnector] ping failed');
      this.session.onReconnectingFailed();
      this._schedulePing(this.interval);
    }.bind(this);

    let pingDuration = Date.now() - this.pingStartTimestamp;
    if (pingDuration < this.minPingDuration) {
      // Wait at least a certain time before informing about connection failure (to prevent flickering of the reconnecting notification)
      setTimeout(handleFailedPing, this.minPingDuration - pingDuration);
    } else {
      handleFailedPing();
    }
  }
}
