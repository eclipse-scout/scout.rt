scout.RadioButton = function() {
  scout.RadioButton.parent.call(this);
};
scout.inherits(scout.RadioButton, scout.Button);

scout.RadioButton.prototype._render = function($parent) {

  var htmlContainer = this.addContainer($parent, 'radio-button',new scout.RadioButtonLayout(this));

  var forRefId = 'RefId-' + this.id;
  this.addField( $('<input>')
    .attr('id', forRefId)
    .attr('type', 'radio')
    .attr('value', this.radioValue).on('click', this._onClick.bind(this)));
  this.addLabel();

  this.$label.attr('for', forRefId);

  this.addStatus();
};

scout.RadioButton.prototype._onClick = function() {
  var uiChecked = this.$field[0].checked;
  this.session.send(this.id, 'selected', {
    checked: uiChecked
  });
};

/**
 * @override
 */
scout.RadioButton.prototype._renderEnabled = function(enabled) {
  this.$field.setEnabled(enabled);
};

/**
 * @override
 */
scout.RadioButton.prototype._renderLabel = function(label) {
  if (this.$label) {
    this.$label.html(label);
  }
};

scout.RadioButton.prototype._renderRadioValue = function(radioValue) {
  this.$field.attr('value', radioValue);
};
