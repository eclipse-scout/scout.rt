scout.DesktopNotification = function() {
  scout.DesktopNotification.parent.call(this);
};
scout.inherits(scout.DesktopNotification, scout.Widget);

/**
 * When duration is set to INFINITE, the notification is not removed automatically.
 */
scout.DesktopNotification.INFINITE = -1;

scout.DesktopNotification.prototype._init = function(model) {
  scout.DesktopNotification.parent.prototype._init.call(this, model);
  this.duration = model.duration;
  this.status = model.status;
  this.desktop = this.parent;
};

/**
 * @override Widget.hs
 */
scout.DesktopNotification.prototype._renderProperties = function() {
  this._renderMessage();
};

scout.DesktopNotification.prototype._render = function($parent) {
  var cssSeverity,
    severity = scout.Status.Severity;
  switch (this.status.severity) {
    case severity.OK:
      cssSeverity = 'ok';
      break;
    case severity.INFO:
      cssSeverity = 'info';
      break;
    case severity.WARNING:
      cssSeverity = 'warning';
      break;
    case severity.ERROR:
      cssSeverity = 'error';
      break;
  }
  this.$container = $parent.appendDiv('notification').addClass(cssSeverity);
  this.$content = this.$container.appendDiv('notification-content');
};

scout.DesktopNotification.prototype._renderMessage = function() {
  this.$content.text(scout.strings.hasText(this.status.message) ?
      this.status.message : '');
};

scout.DesktopNotification.prototype.show = function() {
  var desktop = this.desktop;
  desktop.addNotification(this);
  if (this.duration > 0) {
    setTimeout(desktop.removeNotification.bind(desktop, this), this.duration);
  }
};

scout.DesktopNotification.prototype.fadeIn = function($parent) {
  this.render($parent);
  this.$container
    .hide()
    .fadeIn(250);
};

scout.DesktopNotification.prototype.fadeOut = function(callback) {
  this.$container
    .fadeOutAndRemove(750, callback);
};
