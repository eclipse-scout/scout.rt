scout.BrowserField = function() {
  scout.BrowserField.parent.call(this);
  this._postMessageListener;
};
scout.inherits(scout.BrowserField, scout.ValueField);

scout.BrowserField.prototype._render = function($parent) {
  this.addContainer($parent, 'browser-field');
  this.addLabel();
  this.addField($('<iframe>'));
  this.addStatus();

  this._postMessageListener = this._onPostMessage.bind(this);
  window.addEventListener('message', this._postMessageListener);
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
    this.$field.attr('sandbox', scout.helpers.nvl(this.sandboxPermissions, ''));
    if (scout.device.supportsIframeSecurityAttribute()) {
      this.$field.attr('security', 'restricted');
    }
  } else {
    this.$field.removeAttr('sandbox');
    this.$field.removeAttr('security');
  }
};

scout.BrowserField.prototype._onPostMessage = function(event) {
  $.log.debug('received post-message data=' + event.data + ' origin=' + event.origin);
  this.remoteHandler(this.id, 'postMessage',  {
    data: event.data,
    origin: event.origin});
};

/**
 * @override FormField.js
 */
scout.BrowserField.prototype._remove = function() {
  scout.BrowserField.parent.prototype._remove.call(this);
  window.removeEventListener('message', this._postMessageListener);
  this._postMessageListener = null;
};
