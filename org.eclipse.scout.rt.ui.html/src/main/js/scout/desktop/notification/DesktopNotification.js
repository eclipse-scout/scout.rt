scout.DesktopNotification = function() {
  scout.DesktopNotification.parent.call(this);
  this.closable = true;
  this._removeTimeout;
  this._removing = false;
};
scout.inherits(scout.DesktopNotification, scout.Widget);

/**
 * When duration is set to INFINITE, the notification is not removed automatically.
 */
scout.DesktopNotification.INFINITE = -1;

scout.DesktopNotification.prototype._init = function(model) {
  scout.DesktopNotification.parent.prototype._init.call(this, model);
  this.id = model.id;
  this.duration = model.duration;
  this.status = model.status;
  this.closable = scout.nvl(model.closable, true);
  this.desktop = model.desktop || this.parent;
};

/**
 * @override Widget.hs
 */
scout.DesktopNotification.prototype._renderProperties = function() {
  scout.DesktopNotification.parent.prototype._renderProperties.call(this);
  this._renderMessage();
  this._renderClosable();
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
  this.$container = $parent
    .prependDiv('notification')
    .addClass(cssSeverity);
  this.$content = this.$container
    .appendDiv('notification-content');
};

scout.DesktopNotification.prototype._renderMessage = function() {
  this.$content.html(scout.strings.nl2br(this.status.message));
};

scout.DesktopNotification.prototype._renderClosable = function() {
  if (this.closable) {
    this.$content
      .addClass('closable')
      .appendDiv('closer')
      .on('click', this._onCloseIconClick.bind(this));
  }
};

scout.DesktopNotification.prototype._onCloseIconClick = function() {
  this.hide();
};

scout.DesktopNotification.prototype.show = function() {
  var desktop = this.desktop;
  desktop.addNotification(this);
  if (this.duration > 0) {
    this._removeTimeout = setTimeout(desktop.removeNotification.bind(desktop, this), this.duration);
  }
};

scout.DesktopNotification.prototype.hide = function() {
  clearTimeout(this._removeTimeout);
  this.desktop.removeNotification(this);
};

scout.DesktopNotification.prototype.fadeIn = function($parent) {
  this.render($parent);
  var $container = this.$container,
    animationCssClass = 'notification-slide-in';
  $container.addClass(animationCssClass);
  // The timeout used here is in-sync with the animation duration used in DesktopNotification.css
  setTimeout(function() {
    $container.removeClass(animationCssClass);
  }, 300);
};

scout.DesktopNotification.prototype.fadeOut = function(callback) {
  // prevent fadeOut from running more than once (for instance from setTimeout
  // in show and from the click of a user).
  if (this._removing) {
    return;
  }
  this._removing = true;
  var $container = this.$container;
  $container.addClass('notification-fade-out');
  // The timeout used here is in-sync with the animation duration used in DesktopNotification.css
  setTimeout(function() {
    $container.remove();
    if (callback) {
      callback();
    }
  }, 300);
};
