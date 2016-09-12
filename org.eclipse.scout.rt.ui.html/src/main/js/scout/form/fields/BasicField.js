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
  this._onDisplayTextModifiedHandler = this._onDisplayTextModified.bind(this);
  this.disabledCopyOverlay = true;
  this._displayTextModifiedTimeoutId = null;
};
scout.inherits(scout.BasicField, scout.ValueField);

scout.BasicField.prototype._renderProperties = function() {
  scout.BasicField.parent.prototype._renderProperties.call(this);
  this._renderUpdateDisplayTextOnModify();
};

scout.BasicField.prototype.setUpdateDisplayTextOnModify = function(updateDisplayTextOnModify) {
  this.setProperty('updateDisplayTextOnModify', updateDisplayTextOnModify);
};

scout.BasicField.prototype._renderUpdateDisplayTextOnModify = function() {
  if (this.updateDisplayTextOnModify) {
    this.$field.on('input', this._onDisplayTextModifiedHandler);
  } else {
    clearTimeout(this._displayTextModifiedTimeoutId);
    this.$field.off('input', this._onDisplayTextModifiedHandler);
  }
};

/**
 * Called when the property 'updateDisplayTextOnModified' is TRUE and the display text (field's input
 * value) has been modified by a user action, e.g. a key or paste event. If the property is FALSE, this
 * method is _never_ called. Uses the debounce pattern.
 */
scout.BasicField.prototype._onDisplayTextModified = function() {
  clearTimeout(this._displayTextModifiedTimeoutId);
  this._displayTextModifiedTimeoutId = setTimeout(function() {
    if (!this.rendered) {
      // Field may be removed in the meantime -> accepting input is not possible anymore
      this._displayTextModifiedTimeoutId = null;
      return;
    }
    this.acceptInput(true);
  }.bind(this), 250);
};

scout.BasicField.prototype.acceptInput = function(whileTyping) {
  if (!whileTyping && this._displayTextModifiedTimeoutId !== null) {
    clearTimeout(this._displayTextModifiedTimeoutId);
    this.acceptInput(true);
  }
  this._displayTextModifiedTimeoutId = null;
  scout.BasicField.parent.prototype.acceptInput.call(this, whileTyping);
};

/**
 * @override ValueField.js
 */
scout.BasicField.prototype._checkDisplayTextChanged = function(displayText, whileTyping) {
  var displayTextChanged = scout.BasicField.parent.prototype._checkDisplayTextChanged.call(this, displayText, whileTyping);

  // OR if updateDisplayTextOnModify is true
  // 2. check is necessary to make sure the value and not only the display text gets written to the model (IBasicFieldUIFacade.parseAndSetValueFromUI vs setDisplayTextFromUI)
  if (displayTextChanged || (this.updateDisplayTextOnModify || this._displayTextChangedWhileTyping) && !whileTyping) {
    // In 'updateDisplayTextOnModify' mode, each change of text is sent to the server with whileTyping=true.
    // On field blur, the text is sent again with whileTyping=false. The following logic prevents sending
    // to many events to the server. When whileTyping is false, the text has only to be send to the server
    // when there have been any whileTyping=true events. When the field looses the focus without any
    // changes, no request should be sent.
    if (this.updateDisplayTextOnModify) {
      if (whileTyping) {
        // Remember that we sent some events to the server with "whileTyping=true".
        this._displayTextChangedWhileTyping = true;
      } else {
        if (!this._displayTextChangedWhileTyping) {
          // If there were no "whileTyping=true" events, don't send anything to the server.
          return false;
        }
        this._displayTextChangedWhileTyping = false; // Reset
      }
    }
    return true;
  }
  return false;
};

scout.BasicField.prototype._renderDisabledStyle = function() {
  this._renderDisabledStyleInternal(this.$field);
};
