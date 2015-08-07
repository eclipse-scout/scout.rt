scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this.$checkBox;
  this.$checkBoxLabel;
};
scout.inherits(scout.CheckBoxField, scout.ValueField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.addContainer($parent, 'check-box-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField($('<div>'));
  this.$checkBox = $.makeDiv('check-box');
  this.$checkBox.appendTo(this.$field);
  this.$checkBox.on('mousedown', this._onMouseDown.bind(this));
  this.$checkBoxLabel = $.makeDiv('label');
  this.$checkBoxLabel.appendTo(this.$field);
  this.$checkBoxLabel.on('mousedown', this._onMouseDown.bind(this));
  this.addStatus();
};

scout.CheckBoxField.prototype._createKeyStrokeAdapter = function() {
  return new scout.CheckBoxKeyStrokeAdapter(this);
};

scout.CheckBoxField.prototype.acceptInput = function(whileTyping, forceSend) {
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
  this.$checkBox.toggleClass('checked');
  uiChecked = this.$checkBox.hasClass('checked');
  this.remoteHandler(this.id, 'clicked', {
    checked: uiChecked
  });
};
/**
 * @override
 */
scout.CheckBoxField.prototype._renderEnabled = function(enabled) {
  scout.CheckBoxField.parent.prototype._renderEnabled.call(this);
  if (this.enabled) {
    this.$checkBox.attr('tabindex', '0');
  } else {
    this.$checkBox.removeAttr('tabindex');
  }
  this.$checkBox.setEnabled(this.enabled);
};

scout.CheckBoxField.prototype._renderProperties = function() {
  scout.CheckBoxField.parent.prototype._renderProperties.call(this);
  this._renderValue(this.value);
};

scout.CheckBoxField.prototype._renderValue = function(value) {
  this.$checkBox.toggleClass('checked', value);
};

/**
 * @override
 */
scout.CheckBoxField.prototype._renderLabel = function(label) {
  if (!label) {
    label = '';
  }
  if (this.$checkBoxLabel) {
    this.$checkBoxLabel.text(label);
  }
};
