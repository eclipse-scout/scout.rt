scout.BrowserField = function() {
  scout.BrowserField.parent.call(this);
};
scout.inherits(scout.BrowserField, scout.ValueField);

scout.BrowserField.prototype._render = function($parent) {
  this.addContainer($parent, 'browser-field');
  this.addLabel();
  this.addField($('<iframe>'));
  this.addMandatoryIndicator();
  this.addStatus();
};

/**
 * @override ValueField.js
 */
scout.BrowserField.prototype._renderProperties = function() {
  scout.BrowserField.parent.prototype._renderProperties.call(this);
  this._renderLocation();
  this._renderScrollBarsEnabled();
  this._renderSandbox();
};

scout.BrowserField.prototype._renderLocation = function() {
  this.$field.attr('src', this.location);
};

scout.BrowserField.prototype._renderScrollBarsEnabled = function() {
  this.$field.toggleClass('no-scrolling', !this.scrollBarsEnabled);
  // According to http://stackoverflow.com/a/18470016, setting 'overflow: hidden' via
  // CSS should be enough. However, if the inner page sets 'overflow' to another value,
  // scroll bars are shown again. Therefore, we add the legacy 'scrolling=no' attribute,
  // which is deprecated in HTML5, but seems to do the trick.
  if (this.scrollBarsEnabled) {
    this.$field.removeAttr('scrolling');
  }
  else {
    this.$field.attr('scrolling', 'no');
  }
};

scout.BrowserField.prototype._renderSandbox = function() {
  if (this.sandboxEnabled) {
    this.$field.attr('sandbox', scout.helpers.nvl(this.sandboxRestrictions, ''));
    if (scout.device.supportsIframeSecurityAttribute()) {
      this.$field.attr('security', 'restricted');
    }
  } else {
    this.$field.removeAttr('sandbox');
    this.$field.removeAttr('security');
  }
};
