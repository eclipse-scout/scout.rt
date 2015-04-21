/**
 * ValueField assumes $field has a .val() method which returns the value of that field.
 * @abstract
 */
scout.ValueField = function() {
  scout.ValueField.parent.call(this);
  this._keyUpListener;
  this._addAdapterProperties('menus');
};
scout.inherits(scout.ValueField, scout.FormField);

/**
 * @override
 */
scout.ValueField.prototype._createKeyStrokeAdapter = function() {
  return new scout.ValueFieldKeyStrokeAdapter(this);
};

scout.ValueField.prototype._renderProperties = function() {
  scout.ValueField.parent.prototype._renderProperties.call(this);
  this._renderDisplayText(this.displayText);
  if (this.menus) {
    for (var j = 0; j < this.menus.length; j++) {
      this.keyStrokeAdapter.registerKeyStroke(this.menus[j]);
    }
  }
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

scout.ValueField.prototype._onFieldBlur = function() {
  this.displayTextChanged();
};

// FIXME AWE: (naming) in JavaStyleGuide ergänzen:
// - wenn als event handler registriert $field.on('click', this._onClick.bind(this));
// - Wenn event vom server kommt, z.B. selection _onSelection(event)
// - Wenn Wert an Server gesendet werden soll displayTextChanged();
//   wird typischerweise auch im _onChange oder _onKeyUp aufgerufen.
//   ruft typischerweise auch sendDisplayText(displayText) auf

scout.ValueField.prototype.displayTextChanged = function() {
  var displayText = scout.strings.nvl(this._readDisplayText(), ''),
    oldDisplayText = scout.strings.nvl(this.displayText, '');
  if (displayText === oldDisplayText) {
    return;
  }
  this.displayText = displayText;
  this._sendDisplayTextChanged(displayText, false);
};

scout.ValueField.prototype._onFieldKeyUp = function() {
  var displayText = scout.strings.nvl(this._readDisplayText(), '');
  this._sendDisplayTextChanged(displayText, true);
};

scout.ValueField.prototype._sendDisplayTextChanged = function(displayText, whileTyping) {
  this.session.send(this.id, 'displayTextChanged', {
    displayText: displayText,
    whileTyping: whileTyping});
};

