scout.Label = function() {
  scout.Label.parent.call(this);
  this.value = null;
  this.htmlEnabled = false;
};
scout.inherits(scout.Label, scout.Widget);

/**
 * @override
 */
scout.Label.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.Label.prototype._initKeyStrokeContext = function() {
  scout.Label.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.AppLinkKeyStroke(this, this._onAppLinkAction));
};

scout.Label.prototype._init = function(model) {
  scout.Label.parent.prototype._init.call(this, model);
  this.resolveTextKeys(['value']);
};

scout.Label.prototype._render = function() {
  this.$container = this.$parent.appendDiv();
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.Label.prototype._renderProperties = function() {
  scout.Label.parent.prototype._renderProperties.call(this);
  this._renderValue();
};

scout.Label.prototype.setValue = function(value) {
  this.setProperty('value', value);
};

scout.Label.prototype._renderValue = function() {
  var value = this.value || '';
  if (this.htmlEnabled) {
    this.$container.html(value);

    // Find app links and add handlers
    this.$container.find('.app-link').on('click', this._onAppLinkAction.bind(this));

    // Add handler to images to update the layout when the images are loaded
    this.$container.find('img')
      .on('load', this._onImageLoad.bind(this))
      .on('error', this._onImageError.bind(this));
  } else {
    this.$container.html(scout.strings.nl2br(value));
  }
  this.invalidateLayoutTree();
};

scout.Label.prototype.setHtmlEnabled = function(htmlEnabled) {
  this.setProperty('htmlEnabled', htmlEnabled);
};

scout.Label.prototype._renderHtmlEnabled = function() {
  // Render the value again when html enabled changes dynamically
  this._renderValue();
};

scout.Label.prototype._onAppLinkAction = function(event) {
  var $target = $(event.delegateTarget);
  var ref = $target.data('ref');
  this.triggerAppLinkAction(ref);
};

scout.Label.prototype.triggerAppLinkAction = function(ref) {
  this.trigger('appLinkAction', {
    ref: ref
  });
};

scout.Label.prototype._onImageLoad = function(event) {
  this.invalidateLayoutTree();
};

scout.Label.prototype._onImageError = function(event) {
  this.invalidateLayoutTree();
};
