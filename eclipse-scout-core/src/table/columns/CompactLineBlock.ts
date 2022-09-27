/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, strings} from '../../index';

export default class CompactLineBlock {

  constructor(text, icon) {
    this.text = '';
    this.icon = '';
    this._processedText = null;
    this.encodeHtmlEnabled = true;
    this.nlToBrEnabled = false;
    this.htmlToPlainTextEnabled = false;
    this.setText(text);
    this.setIcon(icon);
  }

  /**
   * @param {string} text
   */
  setText(text) {
    this.text = scout.nvl(text, '');
  }

  processedText() {
    if (this._processedText == null) {
      this._processedText = this.processText();
    }
    return this._processedText;
  }

  /**
   * @param {string} icon
   */
  setIcon(icon) {
    this.icon = scout.nvl(icon, '');
  }

  /**
   * Has no effect if htmlToPlainTextEnabled is true.
   * @param {boolean}
   */
  setEncodeHtmlEnabled(encodeHtmlEnabled) {
    if (this.encodeHtmlEnabled !== encodeHtmlEnabled) {
      this._processedText = null;
    }
    this.encodeHtmlEnabled = encodeHtmlEnabled;
  }

  /**
   * @param {boolean} nlToBrEnabled
   */
  setNlToBrEnabled(nlToBrEnabled) {
    if (this.nlToBrEnabled !== nlToBrEnabled) {
      this._processedText = null;
    }
    this.nlToBrEnabled = nlToBrEnabled;
  }

  /**
   * Wins over encodeHtmlEnabled
   * @param {boolean} htmlToPlainTextEnabled
   */
  setHtmlToPlainTextEnabled(htmlToPlainTextEnabled) {
    if (this.htmlToPlainTextEnabled !== htmlToPlainTextEnabled) {
      this._processedText = null;
    }
    this.htmlToPlainTextEnabled = htmlToPlainTextEnabled;
  }

  processText() {
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

  build() {
    if (this.icon) {
      return $('<div>').appendIcon(this.icon).addClass(this.processedText() ? ' with-text' : '').parent()[0].innerHTML + this.processedText();
    }
    return this.processedText();
  }
}
