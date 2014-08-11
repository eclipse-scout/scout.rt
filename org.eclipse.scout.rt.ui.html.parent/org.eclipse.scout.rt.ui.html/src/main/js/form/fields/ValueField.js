scout.ValueField = function() {
  scout.FormField.parent.call(this);
};
scout.inherits(scout.ValueField, scout.FormField);

scout.ValueField.prototype._callSetters = function() {
  scout.ValueField.parent.prototype._callSetters.call(this);

  this._setDisplayText(this.displayText);
};

scout.ValueField.prototype._setDisplayText = function(displayText) {
  if (!this.$field) {
    return;
  }

  this.$field.attr('value', displayText);
};

scout.ValueField.prototype._setValue = function(value) {
  // NOP
};
