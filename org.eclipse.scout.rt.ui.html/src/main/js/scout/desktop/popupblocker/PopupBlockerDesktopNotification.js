scout.PopupBlockerDesktopNotification = function() {
  scout.PopupBlockerDesktopNotification.parent.call(this);
  this.linkUrl;
};
scout.inherits(scout.PopupBlockerDesktopNotification, scout.DesktopNotification);

scout.PopupBlockerDesktopNotification.prototype._init = function(model) {
  scout.PopupBlockerDesktopNotification.parent.prototype._init.call(this, model);
  this.linkText = this.session.text('ui.OpenManually');
  this.closable = true;
  this.duration = scout.DesktopNotification.INFINITE;
  this.status = {
    message: this.session.text('ui.PopupBlockerDetected'),
    severity: scout.Status.Severity.WARN
  };
};

scout.PopupBlockerDesktopNotification.prototype._render = function($parent) {
  scout.PopupBlockerDesktopNotification.parent.prototype._render.call(this, $parent);

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
