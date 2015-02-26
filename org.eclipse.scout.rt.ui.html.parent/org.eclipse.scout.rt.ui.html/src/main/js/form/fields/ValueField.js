/**
 * ValueField assumes $field has a .val() method which returns the value of that field.
 */
scout.ValueField = function() {
  scout.ValueField.parent.call(this);
  this._keyUpListener;
};
scout.inherits(scout.ValueField, scout.FormField);

scout.ValueField.prototype._renderProperties = function() {
  scout.ValueField.parent.prototype._renderProperties.call(this);
  this._renderDisplayText(this.displayText);
};

scout.ValueField.prototype._renderDisplayText = function(displayText) {
  this.$field.val(displayText);
};

scout.ValueField.prototype._readDisplayText = function() {
  return this.$field.val();
};

/**
 * "Update display-text on modify" does not really belong to ValueField, but is available here
 * as a convenience for all subclasses that want to support it.
 */
scout.ValueField.prototype._renderUpdateDisplayTextOnModify = function() {
  if (this.updateDisplayTextOnModify) {
    this._keyUpListener = this._onFieldKeyUp.bind(this);
    this.$field.on('keyup', this._keyUpListener);
  } else {
    this.$field.off('keyup', this._keyUpListener);
  }
};

scout.ValueField.prototype._onFieldKeyUp = function() {
  var displayText = this._readDisplayText();
  this._updateDisplayText(displayText, true);
};

scout.ValueField.prototype._onFieldBlur = function() {
  this._updateDisplayText(this._readDisplayText(), false);
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

  // Don't send when set to false (save some bytes)
  // TODO ASA: remove whileTyping argument when validateOnAnyKey is removed
  if (whileTyping) {
    data.whileTyping = true;
  }

  this.displayText = displayText;
  this.session.send(this.id, 'displayTextChanged', data);
};
