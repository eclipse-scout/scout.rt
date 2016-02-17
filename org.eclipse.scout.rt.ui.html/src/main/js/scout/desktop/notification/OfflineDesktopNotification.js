scout.OfflineDesktopNotification = function() {
  scout.OfflineDesktopNotification.parent.call(this);
  this.$reconnect;
};
scout.inherits(scout.OfflineDesktopNotification, scout.DesktopNotification);

scout.OfflineDesktopNotification.prototype._render = function($parent) {
  scout.OfflineDesktopNotification.parent.prototype._render.call(this, $parent);
  this.$messageText = this.$content
    .addClass('offline-message')
    .appendDiv('offline-message-text');

  this.$reconnect = this.$content
    .appendDiv('reconnect')
    .text(this.session.text('ui.Reconnecting_'))
    .hide();

  if (scout.device.supportsCssAnimation()) {
    this.$reconnect.addClass('reconnect-animated');
  }
};

/**
 * @override DesktopNotification.js
 */
scout.OfflineDesktopNotification.prototype._renderMessage = function() {
  this.$messageText.text(scout.strings.hasText(this.status.message) ?
      this.status.message : '');
};

scout.OfflineDesktopNotification.prototype.reconnect = function() {
  this.$reconnect.show();
  this.$messageText.hide();
};

scout.OfflineDesktopNotification.prototype.reconnectFailed = function() {
  this.$reconnect.hide();
  this.$messageText.show();
};

scout.OfflineDesktopNotification.prototype.reconnectSucceeded = function() {
  this.$reconnect.hide();
  this.$container.removeClass('error').addClass('ok');
  this.$content.text(this.session.text('ui.ConnectionReestablished'));
};


