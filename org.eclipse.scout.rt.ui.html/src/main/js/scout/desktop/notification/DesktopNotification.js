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
scout.DesktopNotification = function() {
  scout.DesktopNotification.parent.call(this);
  this.closable = true;
  this.duration = 5000;
  this.removeTimeout;
  this._removing = false;
};
scout.inherits(scout.DesktopNotification, scout.Notification);

/**
 * When duration is set to INFINITE, the notification is not removed automatically.
 */
scout.DesktopNotification.INFINITE = -1;

scout.DesktopNotification.prototype._init = function(model) {
  scout.DesktopNotification.parent.prototype._init.call(this, model);
};

scout.DesktopNotification.prototype._render = function() {
  this.$container = this.$parent.prependDiv('desktop-notification');
  this.$content = this.$container.appendDiv('desktop-notification-content');
  this.$messageText = this.$content.appendDiv('desktop-notification-message');
  this.$loader = this.$content.appendDiv('desktop-notification-loader');

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
  this._renderClosable();
};

scout.DesktopNotification.prototype._renderMessage = function() {
  var message = scout.nvl(scout.strings.nl2br(this.status.message), '');
  this.$messageText.html(message);
};

/**
 * @override
 */
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
  this.session.desktop.addNotification(this);
};

scout.DesktopNotification.prototype.hide = function() {
  if (this._removing) {
    return;
  }
  this.trigger('close');
  this.session.desktop.removeNotification(this);
};

scout.DesktopNotification.prototype.fadeIn = function($parent) {
  this.render($parent);
  if (!scout.device.supportsCssAnimation()) {
    return;
  }
  this.$container.addClassForAnimation('desktop-notification-slide-in');
};

scout.DesktopNotification.prototype.fadeOut = function() {
  if (!scout.device.supportsCssAnimation()) {
    this.destroy();
    return;
  }
  // prevent fadeOut from running more than once (for instance from the click of a user).
  if (this._removing) {
    return;
  }
  this._removing = true;
  this.$container.addClass('desktop-notification-fade-out');
  this.$container.oneAnimationEnd(function() {
    this.destroy();
  }.bind(this));
};

/**
 * @override
 */
scout.DesktopNotification.prototype.invalidateLayoutTree = function() {
  // called by notification.js. Since desktop notification has no htmlComp, no need to invalidate
};
