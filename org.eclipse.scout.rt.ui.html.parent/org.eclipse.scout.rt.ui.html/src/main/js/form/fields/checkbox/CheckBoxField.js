scout.CheckBoxField = function(model, session) {
  scout.CheckBoxField.parent.call(this, model, session);
  this._$checkBox;
};
scout.inherits(scout.CheckBoxField, scout.FormField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field');
  this.$container.data('gridData', this.model.gridData);
  this.$label = this.$container.appendDiv(undefined, 'label', this.model.label);
  this._$checkBox = $('<input type="checkbox" class="field" />');
  this._$checkBox.appendTo(this.$container);

  var that = this;
  this._$checkBox.on('click', function() {
    that.session.send('click', that.model.id);
  });
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

