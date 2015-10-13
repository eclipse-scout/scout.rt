scout.RadioButton = function() {
  scout.RadioButton.parent.call(this);
};
scout.inherits(scout.RadioButton, scout.Button);

scout.RadioButton.prototype._render = function($parent) {
  this.addContainer($parent, 'radio-button', new scout.ButtonLayout(this));

  this.addField($('<div>')
    .attr('value', this.radioValue)
    .on('mousedown', this._mouseDown.bind(this)));

  this.addStatus();
};

scout.RadioButton.prototype._mouseDown = function() {
  this._toggleChecked();
};

scout.RadioButton.prototype._toggleChecked = function() {
  if (!this.enabled) {
    return;
  }
  if (this.parent instanceof scout.RadioButtonGroup) {
    this.parent.setNewSelection(this);
  } else {
    this.selected = true;
    this.$field.toggleClass('checked', true);
    this._send('selected');
    this.$field.focus();
  }
};

/**
 * @override
 */
scout.RadioButton.prototype._renderProperties = function() {
  scout.RadioButton.parent.prototype._renderProperties.call(this);
  this._renderSelected(this.selected);
};

/**
 * @override
 */
scout.RadioButton.prototype._renderLabel = function(label) {
  if (!label) {
    label = '';
  }
  if (this.$field) {
    this.$field.text(label ? scout.strings.removeAmpersand(label) : '');
  }

};

scout.RadioButton.prototype._renderRadioValue = function(radioValue) {
  this.$field.attr('value', radioValue);
};

scout.RadioButton.prototype._renderTabbable = function(tabbable) {
  if (tabbable) {
    this.$field.attr('tabindex', '0');
  } else {
    this.$field.removeAttr('tabindex');
  }
};

scout.RadioButton.prototype._handleTabIndex = function() {
  if (this.parent instanceof scout.RadioButtonGroup) {
    return;
  }
  this._renderTabbable(this.enabled);
};

scout.RadioButton.prototype._renderSelected = function(selected) {
  this.$field.toggleClass('checked', selected);
};
