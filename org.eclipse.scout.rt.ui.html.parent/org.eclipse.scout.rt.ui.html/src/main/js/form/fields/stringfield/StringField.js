scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.prototype._renderProperties = function() {
  scout.StringField.parent.prototype._renderProperties.call(this);

  this._renderValidateOnAnyKey(this.validateOnAnyKey);
};

scout.StringField.prototype._render = function($parent) {
  this.addContainer($parent, 'StringField');
  this.$container.addClass('string-field'); // TODO AWE: refactor addContainer to accept options-object
  this.addLabel();
  this.addMandatoryIndicator();

  if (this.multilineText) {
    this.$field = $('<textarea>');
  } else {
    this.$field = $('<input>').attr('type', 'text');
  }

  this.$field.
    addClass('field').
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container);

  this.addStatus();
};

