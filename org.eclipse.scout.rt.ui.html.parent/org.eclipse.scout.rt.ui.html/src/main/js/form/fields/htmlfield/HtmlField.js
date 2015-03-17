scout.HtmlField = function() {
  scout.HtmlField.parent.call(this);
};
scout.inherits(scout.HtmlField, scout.ValueField);

scout.HtmlField.prototype._render = function($parent) {
  this.addContainer($parent, 'html-field');
  this.addLabel();
  this.addField($.makeDiv());
  this.addStatus();
};

scout.HtmlField.prototype._renderProperties = function() {
  scout.HtmlField.parent.prototype._renderProperties.call(this);

  this._renderScrollBarsEnabled(this.scrollBarsEnabled);
  this._renderScrollToPosition(this.scrollToPosition);
};


/**
 * @override
 */
scout.HtmlField.prototype._renderDisplayText = function(displayText) {
  this.$field.html(displayText);
};

scout.HtmlField.prototype._renderScrollBarsEnabled = function(scrollBarsEnabled) {
  // XXX
};

// Not called in _renderProperties() because this is not really property (more like an event)
scout.HtmlField.prototype._renderScrollToEnd = function() {
  // XXX
};

scout.HtmlField.prototype._renderScrollToPosition = function(scrollToPosition) {
  // XXX
};
