/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import {Session} from '../index';

export class Reconnector {
  session: Session;
  started: boolean;

  /**
   * Delay before first ping in ms. Default is 1000.
   */
  initialDelay: number;

  /**
   * Interval to be used between pings (indefinite retries) in ms. Default is 3000.
   */
  interval: number;

  /**
   * Minimal assumed ping duration (to prevent flickering of the reconnect notification when AJAX call fails very fast) in ms. Default is 1000.
   */
  minPingDuration: number;

  pingStartTimestamp: number;

  constructor(session: Session) {
    this.session = session;
    this.started = false;
    this.initialDelay = 1000;
    this.interval = 3000;
    this.minPingDuration = 1000;
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

  protected _schedulePing(delay: number) {
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
  protected _ping() {
    this.session.onReconnecting();

    let pingAjaxOptions = this.session.defaultAjaxOptions(this.session._newRequest({
      ping: true
    }));

    $.log.isTraceEnabled() && $.log.trace('[ajax reconnector] ' + pingAjaxOptions.type + ' "' + pingAjaxOptions.url + '"');
    this.pingStartTimestamp = Date.now();
    $.ajax(pingAjaxOptions)
      .done(this._onPingDone.bind(this))
      .fail(this._onPingFail.bind(this));
  }

  protected _onPingDone(data: any, textStatus: JQuery.Ajax.SuccessTextStatus, jqXHR: JQuery.jqXHR) {
    $.log.isTraceEnabled() && $.log.trace('[ajax reconnector] ping success -> connection re-established!');
    this.session.onReconnectingSucceeded();
    this.stop();
  }

  protected _onPingFail(jqXHR: JQuery.jqXHR, textStatus: JQuery.Ajax.ErrorTextStatus, errorThrown: string) {
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
