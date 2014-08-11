scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this._$checkBox;
};
scout.inherits(scout.CheckBoxField, scout.ValueField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.attr('id', 'CheckBoxField-' + this.id);

  //FIXME checkbox should be on the left of the label like the other ui's. resp. should consider label position property
  this.$label = $('<label>').text(this.label);
  this.$container.append(this.$label);

  // TODO AWE: überlegen ob wir einen besser helper als makeDiv verwenden
  // oder lieber HTML in den code schreiben / performance testen.
  this.$status = $('<span class="status"></span>');
  this.$container.append(this.$status);

  // a wrapper span element is required in order to align the checkbox within
  // the form-field. If we'd apply the width to the checkbox element itself, the
  // checkbox is always in the center.
  var $field = $('<span>').addClass('field');
  this._$checkBox = $('<input type="checkbox" />');
  $field.append(this._$checkBox);
  this.$container.append($field);

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
  //FIXME label visible logic in scout for checkboxes is strange. LabelVisible removes not the label but instead the empty space before the checkbox
};
