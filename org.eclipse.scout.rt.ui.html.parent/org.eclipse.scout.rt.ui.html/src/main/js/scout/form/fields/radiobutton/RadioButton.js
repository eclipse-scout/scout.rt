scout.RadioButton = function() {
  scout.RadioButton.parent.call(this);
};
scout.inherits(scout.RadioButton, scout.Button);

scout.RadioButton.prototype._render = function($parent) {
  this.addContainer($parent, 'radio-button', new scout.RadioButtonLayout(this));

  this.addField($('<input>')
    .attr('id', this.refFieldId)
    .attr('type', 'radio')
    .attr('value', this.radioValue).on('click', this._onClick.bind(this)));
  this.addLabel();
  this.$label.attr('for', this.refFieldId);
  this.addStatus();
};

scout.RadioButton.prototype._onClick = function() {
  this.session.send(this.id, 'selected');
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
  if (this.$label) {
    this.$label.html(label ? scout.strings.removeAmpersand(label) : '');
  }
};

scout.RadioButton.prototype._renderRadioValue = function(radioValue) {
  this.$field.attr('value', radioValue);
};

scout.RadioButton.prototype._renderSelected = function(selected) {
  if (selected) {
    this.$field.attr('checked', 'checked');
  }
};
