scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
};
scout.inherits(scout.CheckBoxField, scout.ValueField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.addContainer($parent, 'check-box-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField($('<div>'));
  this.$field.on('mousedown', this._onMouseDown.bind(this));
  this.addStatus();
};

scout.CheckBoxField.prototype._createKeyStrokeAdapter = function() {
  return new scout.CheckBoxKeyStrokeAdapter(this);
};

scout.CheckBoxField.prototype.displayTextChanged = function(whileTyping, forceSend) {
  //nop;
};

scout.CheckBoxField.prototype._renderDisplayText = function(displayText) {
  //nop;
};

scout.CheckBoxField.prototype._onMouseDown = function() {
  this._toggleChecked();
};

scout.CheckBoxField.prototype._toggleChecked = function() {
  var uiChecked;
  if (!this.enabled) {
    return;
  }
  this.$field.toggleClass('checked');
  uiChecked = this.$field.hasClass('checked');
  this.session.send(this.id, 'clicked', {
    checked: uiChecked
  });
};
/**
 * @override
 */
scout.CheckBoxField.prototype._renderEnabled = function(enabled) {
  scout.CheckBoxField.parent.prototype._renderEnabled.call(this);
  if (this.enabled) {
    this.$field.attr('tabindex', '0');
  } else {
    this.$field.removeAttr('tabindex');
  }
  this.$field.setEnabled(this.enabled);
};

scout.CheckBoxField.prototype._renderProperties = function() {
  scout.CheckBoxField.parent.prototype._renderProperties.call(this);
  this._renderValue(this.value);
};

scout.CheckBoxField.prototype._renderValue = function(value) {
  this.$field.toggleClass('checked', value);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderLabel = function(label) {
  if (!label) {
    label = '';
  }
  if (this.$field) {
    this.$field.text(label);
  }
};
