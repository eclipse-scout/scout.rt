scout.PopupBlockerDesktopNotification = function() {
  scout.PopupBlockerDesktopNotification.parent.call(this);
  this._addEventSupport();
};
scout.inherits(scout.PopupBlockerDesktopNotification, scout.DesktopNotification);

scout.PopupBlockerDesktopNotification.prototype._init = function(model) {
  scout.PopupBlockerDesktopNotification.parent.prototype._init.call(this, model);
  this.linkUrl = model.linkUrl;
  this.linkText = this.session.text('ui.OpenManually');
  this.closeable = true;
  this.duration = scout.DesktopNotification.INFINITE;
  this.status = {
    message: this.session.text('ui.PopupBlockerDetected'),
    severity: scout.Status.Severity.WARN
  };
};

scout.PopupBlockerDesktopNotification.prototype._render = function($parent) {
  scout.PopupBlockerDesktopNotification.parent.prototype._render.call(this, $parent);

  this.$messageText = this.$content.appendDiv('popup-blocked-title');
  this.$link = this.$content.appendElement('<a>', 'popup-blocked-link')
    .text(this.linkText)
    .on('click', this._onLinkClick.bind(this));
};

scout.PopupBlockerDesktopNotification.prototype._renderProperties = function() {
  scout.PopupBlockerDesktopNotification.parent.prototype._renderProperties.call(this);
  this._renderLinkUrl();
};

scout.PopupBlockerDesktopNotification.prototype._renderMessage = function() {
  this.$messageText.text(scout.strings.hasText(this.status.message) ?
    this.status.message : '');
};

scout.PopupBlockerDesktopNotification.prototype._renderLinkUrl = function() {
  if (this.linkUrl) {
    this.$link.attr('href', scout.strings.encode(this.linkUrl))
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
