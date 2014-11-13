scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.prototype._renderProperties = function() {
  scout.StringField.parent.prototype._renderProperties.call(this);

  this._renderValidateOnAnyKey(this.validateOnAnyKey);
};

scout.StringField.prototype._render = function($parent) {
  this.addContainer($parent, 'string-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(
    this.multilineText ? $('<textarea>') : scout.fields.new$TextField().
      blur(this._onFieldBlur.bind(this)));
  this.addStatus();
};

