scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this._$checkBox;
};
scout.inherits(scout.CheckBoxField, scout.ValueField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.addContainer($parent, 'check-box-field');
  this.addLabel();
  this.addMandatoryIndicator();

  // a wrapper span element is required in order to align the checkbox within
  // the form-field. If we'd apply the width to the checkbox element itself, the
  // checkbox is always in the center.

  this.addFieldContainer($('<span>'));
  this._$checkBox = $('<div>')
  .appendTo(this.$fieldContainer);

  if(this.enabled){
    this._$checkBox.attr('tabindex', '0');
  }

  this.addField(this._$checkBox);
  this._$checkBox.appendTo(this.$fieldContainer);
  this._$checkBox.on('mousedown', this._onMouseDown.bind(this));
  this.addStatus();
};

scout.CheckBoxField.prototype._createKeyStrokeAdapter = function(){
  return new scout.CheckBoxKeyStrokeAdapter(this);
};

scout.CheckBoxField.prototype._renderDisplayText = function(displayText) {
  //nop;
};

scout.CheckBoxField.prototype._onMouseDown = function() {
  this._toggleChecked();
};

scout.CheckBoxField.prototype._toggleChecked = function(){

  if(!this.enabled){
    return;
  }
  this._$checkBox.toggleClass('checked');
  var uiChecked = this._$checkBox.hasClass('checked');
  this.session.send(this.id, 'clicked', {
    checked: uiChecked
  });
};

scout.CheckBoxField.prototype._renderEnabled=function(){
  scout.CheckBoxField.parent.prototype._renderEnabled .call(this);
  if (this._$checkBox) {
    if(this.enabled){
      this._$checkBox.attr('tabindex', '0');
    }
    else {
      this._$checkBox.removeAttr('tabindex');
    }
    this._$checkBox.toggleClass('disabled', !this.enabled);
  }
};

scout.CheckBoxField.prototype._renderProperties = function() {
  scout.CheckBoxField.parent.prototype._renderProperties.call(this);
  this._renderValue(this.value);
};

scout.CheckBoxField.prototype._renderValue = function(value) {
  this._$checkBox.toggleClass('checked', value);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderEnabled = function(enabled) {
  this._$checkBox.setEnabled(enabled);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderLabel = function(label) {
  if (!label) {
    label = '';
  }
  if (this._$checkBox) {
    this._$checkBox.text(label);
  }
  // Make sure an empty label is as height as the other labels, especially important for top labels
  this.$label.html('&nbsp;').addClass('empty');
};
