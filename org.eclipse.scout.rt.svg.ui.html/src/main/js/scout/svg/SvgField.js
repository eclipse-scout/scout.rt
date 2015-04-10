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
  if (this.svgDocument) {
  var that = this;
    this.$field.html(this.svgDocument);
    this.$field.find('.app-link').on('click', function(event) {
      var dataRef = $(this).attr('data-ref');
      that._appLinkClicked(dataRef);
    });
  } else {
    this.$field.empty();
  }
};

scout.SvgField.prototype._appLinkClicked = function(ref) {
  this.session.send(this.id, 'appLink', {ref: ref});
};
