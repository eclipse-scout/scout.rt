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
  // FIXME AWE: impl. _renderScrollBarsEnabled
};

scout.BrowserField.prototype._renderSandbox = function() {
  if (this.sandbox) {
    if (this.sandbox === 'deny-all') {
      this.$field.attr('sandbox', '');
    } else {
      this.$field.attr('sandbox', this.sandbox);
    }
  } else {
    this.$field.removeAttr('sandbox');
  }
};


