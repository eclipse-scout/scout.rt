/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Common base class for ValueFields having an HTML input field.
 */
scout.BasicField = function() {
  scout.BasicField.parent.call(this);
  this.disabledCopyOverlay = true;
  this._displayTextModifiedTimeoutId = null;
  this.updateDisplayTextOnModify = false;
};
scout.inherits(scout.BasicField, scout.ValueField);

scout.BasicField.prototype.addContainer = function($parent, cssClass, layout) {
  scout.BasicField.parent.prototype.addContainer.call(this, $parent, cssClass, layout);
  this.$container.addClass('basic-field');
};

scout.BasicField.prototype.addField = function($field) {
  scout.BasicField.parent.prototype.addField.call(this, $field);
  if ($field) {
    $field.on('blur', this._onFieldBlur.bind(this))
      .on('focus', this._onFieldFocus.bind(this))
      .on('input', this._onFieldInput.bind(this));
  }
};

scout.BasicField.prototype.setUpdateDisplayTextOnModify = function(updateDisplayTextOnModify) {
  // Execute pending "accept input while typing" function _before_ updating the "updateDisplayTextOnModify" property
  if (this._displayTextModifiedTimeoutId !== null) {
    // Cancel pending "acceptInput(true)" call (see _onDisplayTextModified) and execute it now
    clearTimeout(this._displayTextModifiedTimeoutId);
    this._acceptInputWhileTyping();
  }

  this.setProperty('updateDisplayTextOnModify', updateDisplayTextOnModify);
};

scout.BasicField.prototype._clear = function() {
  this.$field.val('');
};

scout.BasicField.prototype._onFieldInput = function() {
  scout.BasicField.parent.prototype._onFieldInput.call(this);
  if (this.updateDisplayTextOnModify) {
    this._onDisplayTextModified();
  }
};

/**
 * Called when the property 'updateDisplayTextOnModified' is TRUE and the display text (field's input
 * value) has been modified by a user action, e.g. a key or paste event. If the property is FALSE, this
 * method is _never_ called. Uses the debounce pattern.
 */
scout.BasicField.prototype._onDisplayTextModified = function() {
  clearTimeout(this._displayTextModifiedTimeoutId);
  this._displayTextModifiedTimeoutId = setTimeout(this._acceptInputWhileTyping.bind(this), 250);
};

scout.BasicField.prototype._acceptInputWhileTyping = function() {
  this._displayTextModifiedTimeoutId = null;
  if (this.rendered) { // Check needed because field may have been removed in the meantime
    this.acceptInput(true);
  }
};

scout.BasicField.prototype.acceptInput = function(whileTyping) {
  if (this._displayTextModifiedTimeoutId !== null) {
    // Cancel pending "acceptInput(true)" call (see _onDisplayTextModified) and execute it now
    clearTimeout(this._displayTextModifiedTimeoutId);
    this._displayTextModifiedTimeoutId = null;
  }
  scout.BasicField.parent.prototype.acceptInput.call(this, whileTyping);
};

scout.BasicField.prototype._renderDisplayText = function() {
  this.$field.val(this.displayText);
  scout.BasicField.parent.prototype._renderDisplayText.call(this);
};

scout.BasicField.prototype._readDisplayText = function() {
  return this.$field.val();
};

/**
 * @override ValueField.js
 */
scout.BasicField.prototype._checkDisplayTextChanged = function(displayText, whileTyping) {
  var displayTextChanged = scout.BasicField.parent.prototype._checkDisplayTextChanged.call(this, displayText, whileTyping);

  if (whileTyping) {
    if (this.updateDisplayTextOnModify && displayTextChanged) {
      // Remember that we sent some events to the server with "whileTyping=true"
      this._displayTextChangedWhileTyping = true;
    }
  } else {
    // In 'updateDisplayTextOnModify' mode, each change of text is sent to the server with whileTyping=true,
    // see _onDisplayTextModified (facade: "setDisplayTextFromUI"). On field blur, the text must be sent again
    // with whileTyping=false to update the model's value as well (facade: "parseAndSetValueFromUI").
    // Usually, the displayText is only sent if it has changed (to prevent too many server requests). But in
    // the case 'updateDisplayTextOnModify AND whileTyping=false', it has to be sent if the displayText
    // was previously sent with whileTyping=true. To do so, we make the acceptInput() method think that the
    // text has changed (even if it has not).
    if (this._displayTextChangedWhileTyping) {
      displayTextChanged = true;
    }
    this._displayTextChangedWhileTyping = false;
  }

  return displayTextChanged;
};
