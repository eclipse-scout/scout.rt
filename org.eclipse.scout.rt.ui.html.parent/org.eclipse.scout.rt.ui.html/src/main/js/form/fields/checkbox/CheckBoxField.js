scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this._$checkBox;
};
scout.inherits(scout.CheckBoxField, scout.FormField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.attr('id', 'CheckBoxField-' + this.id);

  this.$label = $('<label>' + this.label + '</label>');
  this.$container.append(this.$label);

  this.$status = $('<span class="status"></span>');
  this.$container.append(this.$status);

  // a wrapper span element is required in order to align the checkbox within
  // the form-field. If we'd apply the width to the checkbox element itself, the
  // checkbox is always in the center.
  var $field = $('<span class="field"></span>');
  this._$checkBox = $('<input type="checkbox" />');
  $field.append(this._$checkBox);
  this.$container.append($field);

  this._$checkBox.on('click', function() {
    this.session.send('click', this.id);
  }.bind(this));
};

scout.CheckBoxField.prototype._setEnabled = function(enabled) {
  if (enabled) {
    this._$checkBox.removeAttr('disabled');
  } else {
    this._$checkBox.attr('disabled', 'disabled');
  }
};

scout.CheckBoxField.prototype._setValue = function(value) {
  if (value) {
    this._$checkBox.attr('checked', 'checked');
  } else {
    this._$checkBox.removeAttr('checked');
  }
};

