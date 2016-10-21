scout.DesktopNotification = function() {
  scout.DesktopNotification.parent.call(this);
  this.closable = true;
  this.status = scout.Status.info();
  this.duration;
  this._removeTimeout;
  this._removing = false;
  this.loading = false;
};
scout.inherits(scout.DesktopNotification, scout.Widget);

/**
 * When duration is set to INFINITE, the notification is not removed automatically.
 */
scout.DesktopNotification.INFINITE = -1;

scout.DesktopNotification.prototype._init = function(model) {
  scout.DesktopNotification.parent.prototype._init.call(this, model);
  this.desktop = model.desktop || this.session.desktop;
};

scout.DesktopNotification.prototype._render = function($parent) {
  this.$container = $parent.prependDiv('notification');
  this.$content = this.$container.appendDiv('notification-content');
  this.$messageText = this.$content.appendDiv('notification-message');
  this.$loader = this.$content.appendDiv('notification-loader');

  if (scout.device.supportsCssAnimation()) {
    this.$loader.addClass('animated');
  }
};

scout.DesktopNotification.prototype._remove = function() {
  scout.DesktopNotification.parent.prototype._remove.call(this);
  this._removeCloser();
};

scout.DesktopNotification.prototype._renderProperties = function() {
  scout.DesktopNotification.parent.prototype._renderProperties.call(this);
  this._renderStatus();
  this._renderClosable();
  this._renderLoading();
};

scout.DesktopNotification.prototype.setStatus = function(status) {
  this.setProperty('status', status);
};

scout.DesktopNotification.prototype._syncStatus = function(status) {
  if (this.rendered) {
    this._removeStatus();
  }
  this._setProperty('status', status);
};

scout.DesktopNotification.prototype._removeStatus = function() {
  this.$container.removeClass(scout.DesktopNotification.cssClassForSeverity(this.status));
};

scout.DesktopNotification.prototype._renderStatus = function() {
  this.$container.addClass(scout.DesktopNotification.cssClassForSeverity(this.status));
  this._renderMessage();
};

scout.DesktopNotification.prototype._renderMessage = function() {
  var message = scout.nvl(scout.strings.nl2br(this.status.message), '');
  this.$messageText.html(message);
};

scout.DesktopNotification.prototype.setLoading = function(loading) {
  this.setProperty('loading', loading);
};

scout.DesktopNotification.prototype._renderLoading = function() {
  this.$container.toggleClass('loading', this.loading);
  this.$loader.setVisible(this.loading);
};

scout.DesktopNotification.prototype.setClosable = function(closable) {
  this.setProperty('closable', closable);
};

scout.DesktopNotification.prototype._renderClosable = function() {
  this.$content.toggleClass('closable', this.closable);
  if (!this.closable) {
    this._removeCloser();
  } else {
    this._renderCloser();
  }
};

scout.DesktopNotification.prototype._removeCloser = function() {
  if (!this.$closer) {
    return;
  }
  this.$closer.remove();
  this.$closer = null;
};

scout.DesktopNotification.prototype._renderCloser = function() {
  if (this.$closer) {
    return;
  }
  this.$closer = this.$content
    .appendDiv('closer')
    .on('click', this._onCloseIconClick.bind(this));
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

scout.DesktopNotification.cssClassForSeverity = function(status) {
  var cssSeverity,
    severity = scout.Status.Severity;

  switch (status.severity) {
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
  return cssSeverity;
};
