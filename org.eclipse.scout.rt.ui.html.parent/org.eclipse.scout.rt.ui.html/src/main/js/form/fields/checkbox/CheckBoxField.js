scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this._$checkBox;
};
scout.inherits(scout.CheckBoxField, scout.ValueField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.addContainer($parent, 'CheckBoxField');
  //this.addLabel();
  this.addStatus();

  // a wrapper span element is required in order to align the checkbox within
  // the form-field. If we'd apply the width to the checkbox element itself, the
  // checkbox is always in the center.
  var $field = $('<span>').
    appendTo(this.$container).
    addClass('field');

  this._$checkBox = $('<input>').
    appendTo($field).
    attr('type', 'checkbox');

  // Note we don't call the addLabel method here, instead we add the label right of
  // the check-box.
  this.$label = $('<label>').
    appendTo($field).
    attr('title', this.label);

  this._$checkBox.on('click', function() {
    this.session.send('click', this.id);
  }.bind(this));
};

scout.CheckBoxField.prototype._setEnabled = function(enabled) {
  this._$checkBox.setEnabled(enabled);
};

scout.CheckBoxField.prototype._setValue = function(value) {
  if (value) {
    this._$checkBox.attr('checked', 'checked');
  } else {
    this._$checkBox.removeAttr('checked');
  }
};

scout.CheckBoxField.prototype._setLabelVisible = function(visible) {
  // FIXME label visible logic in scout for checkboxes is strange. LabelVisible removes not the label but instead the empty space before the checkbox
};
