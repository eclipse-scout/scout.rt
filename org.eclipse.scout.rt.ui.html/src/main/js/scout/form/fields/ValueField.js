/**
 * ValueField assumes $field has a .val() method which returns the value of that field.
 * @abstract
 */
scout.ValueField = function() {
  scout.ValueField.parent.call(this);
};
scout.inherits(scout.ValueField, scout.FormField);

scout.ValueField.prototype._renderProperties = function() {
  scout.ValueField.parent.prototype._renderProperties.call(this);
  this._renderDisplayText(this.displayText);
};

scout.ValueField.prototype._renderDisplayText = function(displayText) {
  this.$field.val(displayText);
};

scout.ValueField.prototype._renderCurrentMenuTypes = function() {
  // nop
};

scout.ValueField.prototype._readDisplayText = function() {
  return this.$field.val();
};

scout.ValueField.prototype._onFieldBlur = function() {
  this.acceptInput(false);
};

/**
 * Accepts the current input and writes it to the model.
 * <p>
 * This method is typically called by the _onBlur() function of the field, but may actually be called from anywhere (e.g. button, actions, cell editor, etc).
 * It is also called by the _aboutToBlurByMouseDown() function, which is required because our Ok- and Cancel-buttons are not focusable (thus _onBlur() is
 * never called) but changes in the value-field must be sent to the server anyway when a button is clicked.
 * <p>
 * The default reads the display text using this._readDisplayText() and writes it to the model by calling _sendDisplayTextChanged().
 * If subclasses don't have a display-text or want to write another state to the server, they may override this method.
 *
 */
scout.ValueField.prototype.acceptInput = function(whileTyping) {
  whileTyping = !!whileTyping; // cast to boolean
  var displayText = scout.helpers.nvl(this._readDisplayText(), ''),
    oldDisplayText = scout.helpers.nvl(this.displayText, '');

  // send only if displayText has really changed
  if (this._checkDisplayTextChanged(displayText, whileTyping)) {
    this.displayText = displayText;
    this._sendDisplayTextChanged(displayText, whileTyping);
  }
};

scout.ValueField.prototype._checkDisplayTextChanged = function(displayText, whileTyping) {
  var oldDisplayText = scout.helpers.nvl(this.displayText, '');
  return displayText !== oldDisplayText;
};

/**
 * Method invoked upon a mousedown click with this field as the currently focused control, and is invoked just before the mousedown click will be interpreted.
 * However, the mousedown target must not be this control, but any other control instead.
 *
 * The default implementation checks, whether the click occurred outside this control, and if so invokes 'ValueField.acceptInput'.
 *
 * @param target
 *        the DOM target where the mouse down event occurred.
 */
scout.ValueField.prototype.aboutToBlurByMouseDown = function(target) {
  var eventOnField = this.$field.isOrHas(target);

  if (!eventOnField) {
    this.acceptInput(); // event outside this value field.
  }
};

scout.ValueField.prototype._sendDisplayTextChanged = function(displayText, whileTyping) {
  this._send('displayTextChanged', {
    displayText: displayText,
    whileTyping: whileTyping
  });
};

scout.ValueField.prototype.addField = function($field) {
  scout.ValueField.parent.prototype.addField.call(this, $field);
  this.$field.data('valuefield', this);
};

scout.ValueField.prototype._onStatusMousedown = function(event) {
  if (this.menus && this.menus.length > 0) {
    if (($(document.activeElement).data('valuefield') === this ||
         $(document.activeElement).parent().data('valuefield') === this)) {
      this.acceptInput();
    }
  }

  scout.ValueField.parent.prototype._onStatusMousedown.call(this, event);
};

// ==== static helper methods ==== //

/**
 * Invokes 'ValueField.aboutToBlurByMouseDown' on the currently active value field.
 * This method has no effect if another element is the focus owner.
 */
scout.ValueField.invokeValueFieldAboutToBlurByMouseDown = function(target) {
  var activeValueField = this._getActiveValueField();
  if (activeValueField) {
    activeValueField.aboutToBlurByMouseDown(target);
  }
};

/**
 * Invokes 'ValueField.acceptInput' on the currently active value field.
 * This method has no effect if another element is the focus owner.
 */
scout.ValueField.invokeValueFieldAcceptInput = function() {
  var activeValueField = this._getActiveValueField();
  if (activeValueField) {
    activeValueField.acceptInput();
  }
};

/**
 * Returns the currently active value field, or null if another element is active.
 * Also, if no value field currently owns the focus, its parent is checked to be a value field and is returned accordingly.
 * That is used in DateField.js with multiple input elements.
 */
scout.ValueField._getActiveValueField = function() {
  var valueField = $(document.activeElement).data('valuefield') || $(document.activeElement).parent().data('valuefield');
  return valueField && !(valueField.$field && valueField.$field.hasClass('disabled')) ? valueField : null;
};
