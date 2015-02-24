scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.prototype._render = function($parent) {
  this.addContainer($parent, 'string-field');
  this.addLabel();
  this.addMandatoryIndicator();

  var $field;
  if (this.multilineText) {
    $field = $('<textarea>');
  }
  else {
    $field = scout.fields.new$TextField();
  }
  $field.on('blur', this._onFieldBlur.bind(this));
  this.addField($field);

  this.addStatus();
};

scout.StringField.prototype._renderProperties = function() {
  scout.StringField.parent.prototype._renderProperties.call(this);
  this._renderValidateOnAnyKey(this.validateOnAnyKey);
  this._renderInputMasked(this.inputMasked);
};

scout.StringField.prototype._renderInputMasked = function(inputMasked){
  if (this.multilineText) {
    return;
  }
  this.$field.attr('type', (inputMasked ? 'password' : 'text'));
};


scout.StringField.prototype._registerKeyStrokeAdapter = function(){
  this.keystrokeAdapter = new scout.StringfieldKeystrokeAdapter(this);
};
