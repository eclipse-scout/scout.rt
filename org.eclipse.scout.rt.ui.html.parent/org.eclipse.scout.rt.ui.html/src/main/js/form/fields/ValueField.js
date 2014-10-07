scout.ValueField = function() {
  scout.FormField.parent.call(this);
};
scout.inherits(scout.ValueField, scout.FormField);

scout.ValueField.prototype._renderProperties = function() {
  scout.ValueField.parent.prototype._renderProperties.call(this);

  this._renderDisplayText(this.displayText);
};

scout.ValueField.prototype._renderValue = function(value) {
  // NOP //FIXME CGU may probably be removed
};

scout.ValueField.prototype._renderDisplayText = function(displayText) {
  if (!this.$field) {
    return;
  }

  this.$field.val(displayText);
};

scout.ValueField.prototype._readDisplayText = function() {
  if (!this.$field) {
    return;
  }

  return this.$field.val();
};

scout.ValueField.prototype._renderValidateOnAnyKey = function(validateOnAnyKey) {
  if (!this.$field) {
    return;
  }

  if (validateOnAnyKey) {
    this.$field.on('keyup', this._onFieldKeyUp.bind(this));
  } else {
    this.$field.off('keyup', this._onFieldKeyUp.bind(this));
  }
};

scout.ValueField.prototype._onFieldKeyUp = function() {
  if (!this.$field) {
    return;
  }

  var displayText = this._readDisplayText();
  this._updateDisplayText(displayText, true);
};

scout.ValueField.prototype._onFieldBlur = function() {
  if (!this.$field) {
    return;
  }

  var displayText = this._readDisplayText();
  this._updateDisplayText(displayText, false);
};

scout.ValueField.prototype._updateDisplayText = function(displayText, whileTyping) {
  if (displayText === this.displayText) {
    return;
  }

  if (!displayText) {
    displayText = '';
  }

  var data = {
    displayText: displayText
  };

  //Don't send when set to false (save some bytes)
  if (whileTyping) {
    data.whileTyping = true;
  }

  this.displayText = displayText;
  this.session.send('displayTextChanged', this.id, data);
};
