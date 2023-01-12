/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BasicFieldEventMap, BasicFieldModel, ValueField} from '../../index';

/**
 * Common base class for ValueFields having an HTML input field.
 */
export abstract class BasicField<TValue extends TModelValue, TModelValue = TValue> extends ValueField<TValue, TModelValue> implements BasicFieldModel<TValue, TModelValue> {
  declare model: BasicFieldModel<TValue, TModelValue>;
  declare eventMap: BasicFieldEventMap<TValue>;
  declare self: BasicField<any>;

  updateDisplayTextOnModify: boolean;
  updateDisplayTextOnModifyDelay: number;

  protected _displayTextModifiedTimeoutId: number;
  protected _displayTextChangedWhileTyping: boolean;

  constructor() {
    super();
    this.disabledCopyOverlay = true;
    this._displayTextModifiedTimeoutId = null;
    this.updateDisplayTextOnModify = false;
    this.updateDisplayTextOnModifyDelay = 250;
  }

  override addField($field: JQuery) {
    super.addField($field);
    if ($field) {
      $field.on('input', this._onFieldInput.bind(this));
    }
  }

  /** @see BasicFieldModel.updateDisplayTextOnModify */
  setUpdateDisplayTextOnModify(updateDisplayTextOnModify: boolean) {
    // Execute pending "accept input while typing" function _before_ updating the "updateDisplayTextOnModify" property
    if (this._displayTextModifiedTimeoutId !== null) {
      // Cancel pending "acceptInput(true)" call (see _onDisplayTextModified) and execute it now
      clearTimeout(this._displayTextModifiedTimeoutId);
      this._acceptInputWhileTyping();
    }

    this.setProperty('updateDisplayTextOnModify', updateDisplayTextOnModify);
  }

  /** @see BasicFieldModel.updateDisplayTextOnModifyDelay */
  setUpdateDisplayTextOnModifyDelay(delay: number) {
    this.setProperty('updateDisplayTextOnModifyDelay', delay);
  }

  protected override _clear() {
    if (this.$field) {
      this.$field.val('');
    }
  }

  protected _onFieldInput() {
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
  protected _onDisplayTextModified() {
    clearTimeout(this._displayTextModifiedTimeoutId);
    if (this.updateDisplayTextOnModifyDelay) {
      this._displayTextModifiedTimeoutId = setTimeout(this._acceptInputWhileTyping.bind(this), this.updateDisplayTextOnModifyDelay);
    } else {
      this._acceptInputWhileTyping();
    }
  }

  protected _acceptInputWhileTyping() {
    this._displayTextModifiedTimeoutId = null;
    if (this.rendered) { // Check needed because field may have been removed in the meantime
      this.acceptInput(true);
    }
  }

  override acceptInput(whileTyping?: boolean) {
    if (this._displayTextModifiedTimeoutId !== null) {
      // Cancel pending "acceptInput(true)" call (see _onDisplayTextModified) and execute it now
      clearTimeout(this._displayTextModifiedTimeoutId);
      this._displayTextModifiedTimeoutId = null;
    }
    super.acceptInput(whileTyping);
  }

  protected override _renderDisplayText() {
    this.$field.val(this.displayText);
    super._renderDisplayText();
  }

  protected override _readDisplayText(): string {
    return this.$field ? this.$field.val() as string : '';
  }

  protected override _checkDisplayTextChanged(displayText: string, whileTyping?: boolean): boolean {
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
