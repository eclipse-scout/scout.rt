/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, strings} from '../../index';

export class CompactLineBlock {
  text: string;
  icon: string;
  encodeHtmlEnabled: boolean;
  nlToBrEnabled: boolean;
  htmlToPlainTextEnabled: boolean;
  protected _processedText: string;

  constructor(text?: string, icon?: string) {
    this.text = '';
    this.icon = '';
    this._processedText = null;
    this.encodeHtmlEnabled = true;
    this.nlToBrEnabled = false;
    this.htmlToPlainTextEnabled = false;
    this.setText(text);
    this.setIcon(icon);
  }

  setText(text: string) {
    this.text = scout.nvl(text, '');
  }

  processedText(): string {
    if (this._processedText == null) {
      this._processedText = this.processText();
    }
    return this._processedText;
  }

  setIcon(icon: string) {
    this.icon = scout.nvl(icon, '');
  }

  /**
   * Has no effect if {@link htmlToPlainTextEnabled} is true.
   */
  setEncodeHtmlEnabled(encodeHtmlEnabled: boolean) {
    if (this.encodeHtmlEnabled !== encodeHtmlEnabled) {
      this._processedText = null;
    }
    this.encodeHtmlEnabled = encodeHtmlEnabled;
  }

  setNlToBrEnabled(nlToBrEnabled: boolean) {
    if (this.nlToBrEnabled !== nlToBrEnabled) {
      this._processedText = null;
    }
    this.nlToBrEnabled = nlToBrEnabled;
  }

  /**
   * Wins over encodeHtmlEnabled
   */
  setHtmlToPlainTextEnabled(htmlToPlainTextEnabled: boolean) {
    if (this.htmlToPlainTextEnabled !== htmlToPlainTextEnabled) {
      this._processedText = null;
    }
    this.htmlToPlainTextEnabled = htmlToPlainTextEnabled;
  }

  processText(): string {
    let text = this.text;
    if (this.htmlToPlainTextEnabled) {
      if (this.nlToBrEnabled) {
        // Preserve new lines, toPlainText would replace \n with " "
        text = strings.nl2br(text, false);
      }
      text = strings.plainText(text);
    } else if (this.encodeHtmlEnabled) {
      text = strings.encode(text);
    }
    if (this.nlToBrEnabled) {
      text = strings.nl2br(text, false);
    }
    return text;
  }

  build(): string {
    if (this.icon) {
      return $('<div>').appendIcon(this.icon).addClass(this.processedText() ? ' with-text' : '').parent()[0].innerHTML + this.processedText();
    }
    return this.processedText();
  }
}
