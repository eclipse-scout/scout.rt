/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DesktopNotification, InitModelOf, Status} from '../../index';

export class OfflineDesktopNotification extends DesktopNotification {
  connectFailedReset: number;
  connectionInterruptedStatus: Status;
  reconnectingStatus: Status;

  constructor() {
    super();
    this.connectFailedReset = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.closable = false;
    this.duration = DesktopNotification.INFINITE;

    this.connectionInterruptedStatus = new Status({
      message: this.session.text('ui.ConnectionInterrupted'),
      severity: Status.Severity.ERROR
    });
    this.reconnectingStatus = new Status({
      message: this.session.text('ui.Reconnecting_'),
      severity: Status.Severity.ERROR
    });

    this._setStatus(this.connectionInterruptedStatus);
  }

  protected override _render() {
    super._render();
    this.$content.addClass('offline-message');
    this.$messageText.addClass('offline-message-text');
  }

  reconnect() {
    this.setLoading(true);
    if (this.connectFailedReset) {
      clearTimeout(this.connectFailedReset);
    }
    this.setStatus(this.reconnectingStatus);
  }

  reconnectFailed() {
    /* remove the connecting state with a small delay. otherwise it cannot be read because its only shown very shortly */
    this.connectFailedReset = setTimeout(() => {
      this.connectFailedReset = null;
      this.setLoading(false);
      this.setStatus(this.connectionInterruptedStatus);
    }, 1100 /* this delay must be < Reconnector.interval */);
  }

  reconnectSucceeded() {
    this.setLoading(false);
    if (this.connectFailedReset) {
      clearTimeout(this.connectFailedReset);
    }
    this.setStatus({
      message: this.session.text('ui.ConnectionReestablished'),
      severity: Status.Severity.OK
    });
  }
}
