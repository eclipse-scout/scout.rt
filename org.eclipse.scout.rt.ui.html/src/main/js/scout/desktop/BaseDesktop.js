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
scout.BaseDesktop = function() {
  scout.BaseDesktop.parent.call(this);
  this.offline = false;
  this.notifications = [];
};
scout.inherits(scout.BaseDesktop, scout.ModelAdapter);

scout.BaseDesktop.prototype._renderProperties = function() {
  scout.BaseDesktop.parent.prototype._renderProperties.call(this);
  this._renderTitle(this.title);
};

scout.BaseDesktop.prototype._renderTitle = function(title) {
  if (title === undefined || title === null) {
    return;
  }
  var $scoutDivs = $('div.scout');
  if ($scoutDivs.length <= 1) { // only set document title in non-portlet case
    $scoutDivs.document(true).title = title;
  }
};

scout.BaseDesktop.prototype.addNotification = function(notification) {
  if (!notification) {
    return;
  }
  this.notifications.push(notification);
  if (this.$notifications) {
    // Bring to front
    this.$notifications.appendTo(this.$container);
  } else {
    this.$notifications = this.$container.appendDiv('notifications');
  }
  notification.fadeIn(this.$notifications);
};

scout.BaseDesktop.prototype.removeNotification = function(notification) {
  if (!notification) {
    return;
  }
  if (this.$notifications) {
    notification.fadeOut(this._onNotificationRemoved.bind(this, notification));
  } else {
    scout.arrays.remove(this.notifications, notification);
  }
};

scout.BaseDesktop.prototype._onNotificationRemoved = function(notification) {
  scout.arrays.remove(this.notifications, notification);
  if (this.notifications.length === 0) {
    this.$notifications.remove();
    this.$notifications = null;
  }
};

scout.BaseDesktop.prototype._goOffline = function() {
  if (this.offline) {
    return;
  }
  this.offline = true;
  this._offlineNotification = scout.create('DesktopNotification.Offline', {
    parent: this,
    duration: scout.DesktopNotification.INFINITE,
    status: {
      message: this.session.text('ui.ConnectionInterrupted'),
      severity: scout.Status.Severity.ERROR
    }
  });
  this._offlineNotification.show();
};

scout.BaseDesktop.prototype._goOnline = function() {
  if (!this._hideOfflineMessagePending) {
    this.hideOfflineMessage();
  }
};

scout.BaseDesktop.prototype.hideOfflineMessage = function() {
  this._hideOfflineMessagePending = false;
  this.removeNotification(this._offlineNotification);
  this._offlineNotification = null;
};

scout.BaseDesktop.prototype.onReconnecting = function() {
  if (!this.offline) {
    return;
  }
  this._offlineNotification.reconnect();
};

scout.BaseDesktop.prototype.onReconnectingSucceeded = function() {
  if (!this.offline) {
    return;
  }
  this.offline = false;
  this._offlineNotification.reconnectSucceeded();
  this._hideOfflineMessagePending = true;
  setTimeout(this.hideOfflineMessage.bind(this), 3000);
};

scout.BaseDesktop.prototype.onReconnectingFailed = function() {
  if (!this.offline) {
    return;
  }
  this._offlineNotification.reconnectFailed();
};
