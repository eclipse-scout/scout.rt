/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, objects, scout, Status} from '../index';
import $ from 'jquery';
import {assertValue} from '../scout';

export default class MaxLengthHandler {

  constructor(options) {
    options = options || {};
    assertValue(options.target, 'target is mandatory');

    this.$textInputField = null;
    this.onInputFieldPaste = this._onInputFieldPaste.bind(this);
    $.extend(this, options);
  }

  install($textInputField) {
    this.uninstall();
    if (!$textInputField || (!$textInputField.is('input:text') && !$textInputField.is('textarea'))) {
      return;
    }
    if ($textInputField) {
      this.$textInputField = $textInputField;
      this.$textInputField.on('paste', this.onInputFieldPaste);
    }
  }

  uninstall() {
    if (this.$textInputField) {
      this.$textInputField.off('paste', this.onInputFieldPaste);
    }
  }

  render() {
    if (!this.$textInputField || objects.isNullOrUndefined(this.target.maxLength)) {
      return;
    }
    this.$textInputField.attr('maxlength', this.target.maxLength);

    // Make sure current text does not exceed max length
    let text = this.$textInputField.val();
    if (text.length > this.target.maxLength) {
      this.$textInputField.val(text.slice(0, this.target.maxLength));
    }
    if (!this.target.rendering) {
      this.target.parseAndSetValue(this.target._readDisplayText());
    }
  }

  _onInputFieldPaste(event) {
    if (!this.$textInputField || objects.isNullOrUndefined(this.target.maxLength)) {
      return;
    }
    // must read out the text and selection size now because when the callback is executed, the clipboard content has already been applied to the input field
    let textSize = this.$textInputField.val().length - this._getSelectionSize();

    this._getClipboardData(event, pastedText => {
      if (!pastedText) {
        return;
      }
      if ((textSize + pastedText.length) > this.target.maxLength) {
        this._showNotification('ui.PastedTextTooLong');
      }
    });
  }

  _getSelectionSize() {
    let start = scout.nvl(this.$textInputField[0].selectionStart, null);
    let end = scout.nvl(this.$textInputField[0].selectionEnd, null);
    if (start === null || end === null) {
      return 0;
    }
    return end - start;
  }

  /**
   * Get clipboard data, different strategies for browsers.
   * Must use a callback because this is required by Chrome's clipboard API.
   */
  _getClipboardData(event, doneHandler) {
    let data = event.originalEvent.clipboardData || this.target.$container.window(true).clipboardData;
    if (data) {
      // Chrome, Firefox
      if (data.items && data.items.length) {
        let item = arrays.find(data.items, item => {
          return item.type === 'text/plain';
        });
        if (item) {
          item.getAsString(doneHandler);
        }
        return;
      }

      // IE, Safari
      if (data.getData) {
        doneHandler(data.getData('Text'));
      }
    }

    // Can't access clipboard -> don't call done handler
  }

  _showNotification(textKey) {
    scout.create('DesktopNotification', {
      parent: this.target,
      severity: Status.Severity.WARNING,
      message: this.target.session.text(textKey)
    }).show();
  }
}
