scout.OfflineDesktopNotification = function() {
  scout.OfflineDesktopNotification.parent.call(this);
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
  this.$messageText.hide();
};

scout.OfflineDesktopNotification.prototype.reconnectFailed = function() {
  this.setLoading(false);
  this.$messageText.show();
};

scout.OfflineDesktopNotification.prototype.reconnectSucceeded = function() {
  this.setLoading(false);
  this.setStatus({
    message: this.session.text('ui.ConnectionReestablished'),
    severity: scout.Status.Severity.OK
  });
  this.$messageText.show();
};
