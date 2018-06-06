/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.OfflineDesktopNotification = function() {
  scout.OfflineDesktopNotification.parent.call(this);

  this.connectFailedReset = null;
};
scout.inherits(scout.OfflineDesktopNotification, scout.DesktopNotification);

scout.OfflineDesktopNotification.prototype._init = function(model) {
  scout.OfflineDesktopNotification.parent.prototype._init.call(this, model);
  this.closable = false;
  this.duration = scout.DesktopNotification.INFINITE;
  this.status = new scout.Status({
    message: this.session.text('ui.ConnectionInterrupted'),
    severity: scout.Status.Severity.ERROR
  });
};

scout.OfflineDesktopNotification.prototype._render = function() {
  scout.OfflineDesktopNotification.parent.prototype._render.call(this);
  this.$content.addClass('offline-message');
  this.$messageText.addClass('offline-message-text');
  this.$loader.text(this.session.text('ui.Reconnecting_'));
};

scout.OfflineDesktopNotification.prototype.reconnect = function() {
  this.setLoading(true);
  if (this.connectFailedReset) {
    clearTimeout(this.connectFailedReset);
  }
  this.$messageText.hide();
};

scout.OfflineDesktopNotification.prototype.reconnectFailed = function() {
  /* remove the connecting state with a small delay. otherwise it cannot be read because its only shown very shortly */
  this.connectFailedReset = setTimeout(function() {
    this.connectFailedReset = null;
    this.setLoading(false);
    this.$messageText.show();
  }.bind(this), 1100 /* this delay must be < Reconnector.interval */ );
};

scout.OfflineDesktopNotification.prototype.reconnectSucceeded = function() {
  this.setLoading(false);
  if (this.connectFailedReset) {
    clearTimeout(this.connectFailedReset);
  }
  this.setStatus({
    message: this.session.text('ui.ConnectionReestablished'),
    severity: scout.Status.Severity.OK
  });
  this.$messageText.show();
};
