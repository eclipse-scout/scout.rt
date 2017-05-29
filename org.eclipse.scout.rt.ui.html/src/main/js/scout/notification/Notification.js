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
scout.Notification = function() {
  scout.Notification.parent.call(this);
  this.status = scout.Status.info();
};
scout.inherits(scout.Notification, scout.Widget);

scout.Notification.prototype._init = function(model) {
  scout.Notification.parent.prototype._init.call(this, model);

  // this allows to set the properties severity and message directly on the model object
  // without having a status object. because it's more convenient when you must create
  // a notification programmatically.
  if (model.severity || model.message) {
    this.status = new scout.Status({
      severity: scout.nvl(model.severity, this.status.severity),
      message: scout.nvl(model.message, this.status.message)
    });
  }
  scout.texts.resolveTextProperty(this.status, 'message', this.session);
  this._setStatus(this.status);
};

scout.Notification.prototype._render = function() {
  this.$container = this.$parent.appendDiv('notification');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.Notification.prototype._renderProperties = function() {
  scout.Notification.parent.prototype._renderProperties.call(this);

  this._renderStatus();
};

scout.Notification.prototype.setStatus = function(status) {
  this.setProperty('status', status);
};

scout.Notification.prototype._setStatus = function(status) {
  if (this.rendered) {
    this._removeStatus();
  }
  status = scout.Status.ensure(status);
  this._setProperty('status', status);
};

scout.Notification.prototype._removeStatus = function() {
  if (this.status) {
    this.$container.removeClass(this.status.cssClass());
  }
};

scout.Notification.prototype._renderStatus = function() {
  if (this.status) {
    this.$container.addClass(this.status.cssClass());
  }
  this._renderMessage();
};

scout.Notification.prototype._renderMessage = function() {
  var message = scout.nvl(scout.strings.nl2br(this.status.message), '');
  this.$container.html(message);
  this.invalidateLayoutTree();
};

/**
 * @override
 */
scout.Notification.prototype._renderVisible = function() {
  scout.Notification.parent.prototype._renderVisible.call(this);
  this.invalidateLayoutTree();
};
