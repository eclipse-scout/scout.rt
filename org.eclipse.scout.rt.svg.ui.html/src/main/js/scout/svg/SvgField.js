scout.SvgField = function() {
  scout.SvgField.parent.call(this);
};
scout.inherits(scout.SvgField, scout.ValueField);

scout.SvgField.prototype._render = function($parent) {
  this.addContainer($parent, 'svg-field');
  this.addLabel();
  this.addField($('<div>'));
  this.addMandatoryIndicator();
  this.addStatus();
  this.loadingSupport = new scout.DefaultFieldLoadingSupport(this);
};

scout.SvgField.prototype._renderProperties = function() {
  scout.SvgField.parent.prototype._renderProperties.call(this);
  this._renderSvgDocument();
};

/**
 * @override FormField.js
 */
scout.SvgField.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.SvgField.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);
  keyStrokeContext.registerKeyStroke(new scout.AppLinkKeyStroke(this, this._onAppLinkAction));
}

scout.SvgField.prototype._renderSvgDocument = function() {
  if (!this.svgDocument) {
    this.$field.empty();
    return;
  }
  this.$field.html(this.svgDocument);
  this.$field.find('.app-link')
    .on('click', this._onAppLinkAction.bind(this))
    .attr('tabindex', '0')
    .unfocusable();
};

scout.SvgField.prototype._onAppLinkAction = function(event) {
  var $target = $(event.delegateTarget);
  var ref = $target.data('ref');
  this._sendAppLinkAction(ref);
  event.preventDefault();
};

scout.SvgField.prototype._sendAppLinkAction = function(ref) {
  this._send('appLinkAction', {
    ref: ref
  });
};
