// TODO AWE: gemeinsame basis-klasse für widgets mit text-feld
scout.NumberField = function() {
  scout.NumberField.parent.call(this);
  this._$inputText;
};
scout.inherits(scout.NumberField, scout.FormField);

scout.NumberField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.attr('id', 'NumberField-' + this.id);
  this.$label = $('<label>' + this.label + '</label>');
  this.$container.append(this.$label);

  this.$status = $('<span class="status"></span>');
  this.$container.append(this.$status);

  this._$inputText = $('<input type="text" class="field" />');
  this.$container.append(this._$inputText);
};

scout.NumberField.prototype._setEnabled = function(enabled) {
  if (enabled) {
    this._$inputText.removeAttr('disabled');
  } else {
    this._$inputText.attr('disabled', 'disabled');
  }
};

scout.NumberField.prototype._setValue = function(value) {
  this._$inputText.attr('value', value);
};

