scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.prototype._renderProperties = function() {
  scout.StringField.parent.prototype._renderProperties.call(this);
  this._renderValidateOnAnyKey(this.validateOnAnyKey);
  this._renderInputMasked(this.inputMasked);
};

scout.StringField.prototype._render = function($parent) {
  this.addContainer($parent, 'string-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(
    this.multilineText ? $('<textarea>') :
      this.inputMasked ?
          $('<input>').attr('type', 'password').blur(this._onFieldBlur.bind(this)) :
          scout.fields.new$TextField().blur(this._onFieldBlur.bind(this)));
  this.addStatus();
};

scout.StringField.prototype._renderInputMasked = function(inputMasked){
  if (this.$field) {
    if(inputMasked){
      this.$field.attr('type', 'password');
    }
    else{
      this.$field.attr('type', 'text');
    }
  }
};
