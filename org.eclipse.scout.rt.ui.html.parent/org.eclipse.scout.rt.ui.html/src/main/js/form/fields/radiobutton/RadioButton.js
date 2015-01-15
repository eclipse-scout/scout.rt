scout.RadioButton = function() {
  scout.RadioButton.parent.call(this);
  this._$radioButton;
  this._$radioButtonLabel;
};
scout.inherits(scout.RadioButton, scout.Button);

scout.RadioButton.prototype._render = function($parent) {
  this.addContainer($parent, 'radio-button');
  this.addLabel();

  // a wrapper span element is required in order to align the checkbox within
  // the form-field. If we'd apply the width to the checkbox element itself, the
  // checkbox is always in the center.
  this.addField($('<span>'));

  var forRefId = 'RefId-' + this.id;
  this._$radioButton = $('<input>').
    attr('id', forRefId).
    attr('type', 'radio').
    attr('value', this.radioValue).
    appendTo(this.$field);

  this._$radioButtonLabel = $('<label>').
    attr('for', forRefId).
    attr('title', this.label).
    appendTo(this.$field);

  this._$radioButton.on('click', this._onClick.bind(this));
  this.addStatus();
};

scout.RadioButton.prototype._onClick = function() {
  var uiChecked = this._$radioButton[0].checked;
  this.session.send('selected', this.id, {checked: uiChecked});
};

/**
 * @override
 */
scout.RadioButton.prototype._renderEnabled = function(enabled) {
  this._$radioButton.setEnabled(enabled);
};

/**
 * @override
 */
scout.RadioButton.prototype._renderLabel = function(label) {
  if (!label) {
    label = '';
  }
  if (this._$radioButtonLabel) {
    this._$radioButtonLabel.html(label);
  }
};

scout.RadioButton.prototype._renderRadioValue = function(radioValue){
  this._$radioButton.attr('value', radioValue);
};
