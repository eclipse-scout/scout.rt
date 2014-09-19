scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this._$checkBox;
  this._$checkBoxLabel;
};
scout.inherits(scout.CheckBoxField, scout.ValueField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.addContainer($parent, 'CheckBoxField');
  this.addLabel();
  this.addStatus();

  // a wrapper span element is required in order to align the checkbox within
  // the form-field. If we'd apply the width to the checkbox element itself, the
  // checkbox is always in the center.
  var $field = $('<span>').
    appendTo(this.$container).
    addClass('field');

  var forRefId = 'RefId-' + this.id;
  this._$checkBox = $('<input>').
    attr('id', forRefId).
    attr('type', 'checkbox').
    appendTo($field);

  this._$checkBoxLabel = $('<label>').
    attr('for', forRefId).
    attr('title', this.label).
    appendTo($field);

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

// FIXME AWE/CGU label visible logic in scout for checkboxes is strange.
// LabelVisible removes not the label but instead the empty space before the checkbox
// A.WE: removed overridden _setLabelVisible method, default impl. is Ok in my opinion.

/**
 * @override
 */
scout.CheckBoxField.prototype._setLabel = function(label) {
  if (!label) {
    label = '';
  }
  if (this._$checkBoxLabel) {
    this._$checkBoxLabel.html(label);
  }
};
