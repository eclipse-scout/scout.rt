scout.StringField = function() {
  scout.StringField.parent.call(this);
  this._$inputText;
};
scout.inherits(scout.StringField, scout.FormField);

scout.StringField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.attr('id', 'StringField-' + this.id);
  this.$label = $('<label>' + this.label + '</label>');
  this.$container.append(this.$label);

  this.$status = $('<span class="status"></span>');
  this.$container.append(this.$status);

  this._$inputText = $('<input type="text" class="field" />');
  this.$container.append(this._$inputText);
};

scout.StringField.prototype._setEnabled = function(enabled) {
  if (!this.isRendered()) {
    return;
  }

  if (enabled) {
    this._$inputText.removeAttr('disabled');
  } else {
    this._$inputText.attr('disabled', 'disabled');
  }
};

scout.StringField.prototype._setValue = function(value) {
  this._$inputText.attr('value', value);
};

//FIXME CGU move to FormField.js
scout.StringField.prototype._setVisible = function(visible) {
  if (visible) {
    this.$container.show();
  } else {
    this.$container.hide();
  }
};

