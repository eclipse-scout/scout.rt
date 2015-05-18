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
};

scout.SvgField.prototype._renderProperties = function() {
  scout.SvgField.parent.prototype._renderProperties.call(this);
  this._renderSvgDocument();
};

scout.SvgField.prototype._renderSvgDocument = function() {
  if (!this.svgDocument) {
    this.$field.empty();
    return;
  }
  this.$field.html(this.svgDocument);
  this.$field.find('.app-link').on('click', this._onAppLinkAction.bind(this))
  //prevent focusvalidation on other field
  .on('mousedown', function(event){event.preventDefault(); event.stopPropagation();});
};

scout.SvgField.prototype._onAppLinkAction = function(event) {
  var $target = $(event.target);
  var ref = $target.data('ref');
  this._sendAppLinkAction(ref);
  event.preventDefault();
};

scout.SvgField.prototype._sendAppLinkAction = function(ref) {
  this.session.send(this.id, 'appLinkAction', {
    ref: ref
  });
};
