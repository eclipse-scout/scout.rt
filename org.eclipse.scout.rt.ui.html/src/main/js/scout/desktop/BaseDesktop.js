// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.BaseDesktop = function() {
  scout.BaseDesktop.parent.call(this);
  this.offline = false;
};
scout.inherits(scout.BaseDesktop, scout.ModelAdapter);

scout.BaseDesktop.prototype._renderProperties = function() {
  scout.BaseDesktop.parent.prototype._renderProperties.call(this);

  this._renderTitle(this.title);
};

scout.BaseDesktop.prototype._renderTitle = function(title) {
  var $scoutDivs = $('div.scout');
  if ($scoutDivs.length <= 1) { // only set document title in non-portlet case
    document.title = title;
  }
};

scout.BaseDesktop.prototype.addNotification = function($notification) {
  if (!$notification) {
    return;
  }
  if (!this.$notifications) {
    this.$notifications = this.$container.prependDiv('notifications');
  }
  // Fade in notification
  $notification.appendTo(this.$notifications).hide().fadeIn(250);
};

scout.BaseDesktop.prototype.removeNotification = function($notification) {
  if (!$notification) {
    return;
  }
  if (this.$notifications) {
    var that = this;
    // Fade out notification
    $notification.fadeOutAndRemove(750, function() {
      if (!that.$notifications.has('.notification')) {
        that.$notifications.remove();
        that.$notifications = null;
      }
    }.bind(this));
  }
};

scout.BaseDesktop.prototype._goOffline = function() {
  if (this.offline) {
    return;
  }
  this.offline = true;

  this.$offlineNotification = $.makeDiv('notification error');
  this._$offlineMessage = this.$offlineNotification.appendDiv('notification-content offline-message');
  this._$offlineMessage.appendDiv('offline-message-text')
    .text(this.session.text('ui.ConnectionInterrupted'));
  var $reconnect = this._$offlineMessage.appendDiv('reconnect');
  $reconnect
    .text(this.session.text('ui.Reconnecting_'))
    .hide();
  if (scout.device.supportsCssAnimation()) {
    $reconnect.addClass('reconnect-animated');
  }
  this.addNotification(this.$offlineNotification);
};

scout.BaseDesktop.prototype._goOnline = function() {
  if (!this._hideOfflineMessagePending) {
    this.hideOfflineMessage();
  }
};

scout.BaseDesktop.prototype.hideOfflineMessage = function() {
  this._hideOfflineMessagePending = false;
  this.removeNotification(this.$offlineNotification);
  this.$offlineNotification = null;
};

scout.BaseDesktop.prototype.onReconnecting = function() {
  if (!this.offline) {
    return;
  }
  this._$offlineMessage.children('.offline-message-text').hide();
  this._$offlineMessage.children('.reconnect').show();
};

scout.BaseDesktop.prototype.onReconnectingSucceeded = function() {
  if (!this.offline) {
    return;
  }
  this.offline = false;

  this._$offlineMessage.children('.reconnect').hide();
  this._$offlineMessage.text(this.session.text('ui.ConnectionReestablished'));
  this.$offlineNotification.removeClass('error');
  this._hideOfflineMessagePending = true;
  setTimeout(this.hideOfflineMessage.bind(this), 3000);
};

scout.BaseDesktop.prototype.onReconnectingFailed = function() {
  if (!this.offline) {
    return;
  }
  this._$offlineMessage.children('.reconnect').hide();
  this._$offlineMessage.children('.offline-message-text').show();
};
