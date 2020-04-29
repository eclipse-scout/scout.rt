/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DesktopNotification, Status} from '../../index';

export default class OfflineDesktopNotification extends DesktopNotification {

  constructor() {
    super();

    this.connectFailedReset = null;
  }

  _init(model) {
    super._init(model);
    this.closable = false;
    this.duration = DesktopNotification.INFINITE;
    this.status = new Status({
      message: this.session.text('ui.ConnectionInterrupted'),
      severity: Status.Severity.ERROR
    });
  }

  _render() {
    super._render();
    this.$content.addClass('offline-message');
    this.$messageText.addClass('offline-message-text');
    this.$loader.text(this.session.text('ui.Reconnecting_'));
  }

  reconnect() {
    this.setLoading(true);
    if (this.connectFailedReset) {
      clearTimeout(this.connectFailedReset);
    }
    this.$messageText.hide();
  }

  reconnectFailed() {
    /* remove the connecting state with a small delay. otherwise it cannot be read because its only shown very shortly */
    this.connectFailedReset = setTimeout(() => {
      this.connectFailedReset = null;
      this.setLoading(false);
      this.$messageText.show();
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
    this.$messageText.show();
  }
}
