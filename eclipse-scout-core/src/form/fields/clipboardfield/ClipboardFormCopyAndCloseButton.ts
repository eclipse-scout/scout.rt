/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Button, clipboard, ClipboardField} from '../../../index';

/**
 * Finds a field of type {@link ClipboardField} on the surrounding form and copies its content to the clipboard.
 * Intended to be used as a model variant on the ClipboardForm.
 */
export class ClipboardFormCopyAndCloseButton extends Button {

  protected override _doAction() {
    this._copyTextToClipboard();
    super._doAction();
  }

  protected _copyTextToClipboard() {
    let clipboardField = this.getForm()?.findChild(ClipboardField);
    let textToCopy = clipboardField?.$field?.selectedText() || clipboardField?.displayText;
    if (textToCopy) {
      clipboard.copyText({
        parent: this,
        text: textToCopy
      });
    }
  }
}
