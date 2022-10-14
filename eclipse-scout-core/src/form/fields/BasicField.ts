/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ValueField} from '../../index';

/**
 * Common base class for ValueFields having an HTML input field.
 */
export default class BasicField extends ValueField {

  constructor() {
    super();
    this.disabledCopyOverlay = true;
    this._displayTextModifiedTimeoutId = null;
    this.updateDisplayTextOnModify = false;
    this.updateDisplayTextOnModifyDelay = 250; // in milliseconds
  }

  addField($field) {
    super.addField($field);
    if ($field) {
      $field.on('input', this._onFieldInput.bind(this));
    }
  }

  setUpdateDisplayTextOnModify(updateDisplayTextOnModify) {
    // Execute pending "accept input while typing" function _before_ updating the "updateDisplayTextOnModify" property
    if (this._displayTextModifiedTimeoutId !== null) {
      // Cancel pending "acceptInput(true)" call (see _onDisplayTextModified) and execute it now
      clearTimeout(this._displayTextModifiedTimeoutId);
      this._acceptInputWhileTyping();
    }

    this.setProperty('updateDisplayTextOnModify', updateDisplayTextOnModify);
  }

  setUpdateDisplayTextOnModifyDelay(delay) {
    this.setProperty('updateDisplayTextOnModifyDelay', delay);
  }

  _clear() {
    if (this.$field) {
      this.$field.val('');
    }
  }

  _onFieldInput() {
    this._updateHasText();
    if (this.updateDisplayTextOnModify) {
      this._onDisplayTextModified();
    }
  }

  /**
   * Called when the property 'updateDisplayTextOnModified' is TRUE and the display text (field's input
   * value) has been modified by a user action, e.g. a key or paste event. If the property is FALSE, this
   * method is _never_ called. Uses the debounce pattern.
   */
  _onDisplayTextModified() {
    clearTimeout(this._displayTextModifiedTimeoutId);
    if (this.updateDisplayTextOnModifyDelay) {
      this._displayTextModifiedTimeoutId = setTimeout(this._acceptInputWhileTyping.bind(this), this.updateDisplayTextOnModifyDelay);
    } else {
      this._acceptInputWhileTyping();
    }
  }

  _acceptInputWhileTyping() {
    this._displayTextModifiedTimeoutId = null;
    if (this.rendered) { // Check needed because field may have been removed in the meantime
      this.acceptInput(true);
    }
  }

  acceptInput(whileTyping) {
    if (this._displayTextModifiedTimeoutId !== null) {
      // Cancel pending "acceptInput(true)" call (see _onDisplayTextModified) and execute it now
      clearTimeout(this._displayTextModifiedTimeoutId);
      this._displayTextModifiedTimeoutId = null;
    }
    super.acceptInput(whileTyping);
  }

  _renderDisplayText() {
    this.$field.val(this.displayText);
    super._renderDisplayText();
  }

  _readDisplayText() {
    return this.$field ? this.$field.val() : '';
  }

  /**
   * @override ValueField.js
   */
  _checkDisplayTextChanged(displayText, whileTyping) {
    let displayTextChanged = super._checkDisplayTextChanged(displayText, whileTyping);

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
  }
}
