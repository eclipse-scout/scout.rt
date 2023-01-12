/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, DesktopNotification, InitModelOf, MaxLengthHandlerModel, MaxLengthHandlerTarget, objects, ObjectWithType, scout, SomeRequired, Status} from '../index';
import $ from 'jquery';

export class MaxLengthHandler implements MaxLengthHandlerModel, ObjectWithType {
  declare model: MaxLengthHandlerModel;
  declare initModel: SomeRequired<this['model'], 'target'>;
  objectType: string;

  onInputFieldPaste: (event: JQuery.TriggeredEvent<HTMLInputElement, undefined, HTMLInputElement, HTMLInputElement>) => void;
  target: MaxLengthHandlerTarget;
  $textInputField: JQuery<HTMLInputElement>;

  constructor(options: InitModelOf<MaxLengthHandler>) {
    // @ts-expect-error
    options = options || {};
    scout.assertValue(options.target, 'target is mandatory');

    this.$textInputField = null;
    this.onInputFieldPaste = this._onInputFieldPaste.bind(this);
    this.target = null;
    $.extend(this, options);
  }

  install($textInputField: JQuery<HTMLInputElement>) {
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
    let text = this.$textInputField.val() as string;
    if (text.length > this.target.maxLength) {
      this.$textInputField.val(text.slice(0, this.target.maxLength));
    }
    if (!this.target.rendering) {
      this.target.parseAndSetValue(this.target._readDisplayText());
    }
  }

  protected _onInputFieldPaste(event: JQuery.TriggeredEvent<HTMLInputElement, undefined, HTMLInputElement, HTMLInputElement>) {
    if (!this.$textInputField || objects.isNullOrUndefined(this.target.maxLength)) {
      return;
    }
    // must read out the text and selection size now because when the callback is executed, the clipboard content has already been applied to the input field
    let text = this.$textInputField.val() as string;
    let textSize = text.length - this._getSelectionSize();

    this._getClipboardData(event, pastedText => {
      if (!pastedText) {
        return;
      }
      if ((textSize + pastedText.length) > this.target.maxLength) {
        this._showNotification('ui.PastedTextTooLong');
      }
    });
  }

  protected _getSelectionSize(): number {
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
  protected _getClipboardData(event: JQuery.TriggeredEvent<HTMLInputElement, undefined, HTMLInputElement, HTMLInputElement>, doneHandler: (pastedText: string) => void) {
    let data: DataTransfer = event.originalEvent['clipboardData'] || this.target.$container.window(true)['clipboardData'];
    if (data) {
      // Chrome, Firefox
      if (data.items && data.items.length) {
        let item = arrays.find(data.items, (item: DataTransferItem) => {
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

  protected _showNotification(textKey: string) {
    scout.create(DesktopNotification, {
      parent: this.target,
      severity: Status.Severity.WARNING,
      message: this.target.session.text(textKey)
    }).show();
  }
}
