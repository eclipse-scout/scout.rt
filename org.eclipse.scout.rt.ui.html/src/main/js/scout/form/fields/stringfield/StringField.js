scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.prototype._createKeyStrokeAdapter = function(){
  return new scout.StringFieldKeyStrokeAdapter(this);
};

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

  this._renderUpdateDisplayTextOnModify();
  this._renderInputMasked(this.inputMasked);
  this._renderWrapText(this.wrapText);
};

scout.StringField.prototype._renderInputMasked = function(inputMasked){
  if (this.multilineText) {
    return;
  }
  this.$field.attr('type', (inputMasked ? 'password' : 'text'));
};

//Not called in _renderProperties() because this is not really a property (more like an event)
scout.StringField.prototype._renderInsertText = function() {
  var s = this.insertText;
  if (s && this.$field.length > 0) {
    var elem = this.$field[0];
    var a = 0;
    var b = 0;
    if (elem.selectionStart !== undefined && elem.selectionEnd !== undefined) {
      a = elem.selectionStart;
      b = elem.selectionEnd;
    }
    var text = elem.value;
    text = text.slice(0, a) + s + text.slice(b);
    elem.value = text;
  }
};

scout.StringField.prototype._renderWrapText = function() {
  this.$field.toggleClass('white-space-nowrap', !this.wrapText);
};

