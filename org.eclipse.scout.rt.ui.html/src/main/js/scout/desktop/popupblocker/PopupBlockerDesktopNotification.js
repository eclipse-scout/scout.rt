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
scout.PopupBlockerDesktopNotification = function() {
  scout.PopupBlockerDesktopNotification.parent.call(this);
  this.duration = scout.DesktopNotification.INFINITE;
  this.linkUrl;
};
scout.inherits(scout.PopupBlockerDesktopNotification, scout.DesktopNotification);

scout.PopupBlockerDesktopNotification.prototype._init = function(model) {
  scout.PopupBlockerDesktopNotification.parent.prototype._init.call(this, model);
  this.linkText = scout.nvl(this.linkText, this.session.text('ui.OpenManually'));
  this._setStatus({
    message: this.session.text('ui.PopupBlockerDetected'),
    severity: scout.Status.Severity.WARNING
  });
};

scout.PopupBlockerDesktopNotification.prototype._render = function() {
  scout.PopupBlockerDesktopNotification.parent.prototype._render.call(this);

  this.$messageText.addClass('popup-blocked-title');
  this.$link = this.$content.appendElement('<a>', 'popup-blocked-link')
    .text(this.linkText)
    .on('click', this._onLinkClick.bind(this));
};

scout.PopupBlockerDesktopNotification.prototype._renderProperties = function() {
  scout.PopupBlockerDesktopNotification.parent.prototype._renderProperties.call(this);
  this._renderLinkUrl();
};

scout.PopupBlockerDesktopNotification.prototype._renderLinkUrl = function() {
  if (this.linkUrl) {
    this.$link.attr('href', this.linkUrl)
      .attr('target', '_blank');
  } else {
    this.$link.removeAttr('href')
      .removeAttr('target');
  }
};

scout.PopupBlockerDesktopNotification.prototype._onLinkClick = function() {
  this.trigger('linkClick');
  this.hide();
};
