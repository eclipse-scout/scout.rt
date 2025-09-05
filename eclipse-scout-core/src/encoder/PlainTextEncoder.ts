/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CachedElement, scout, strings} from '../index';

/**
 * Replaces character HTML entities (e.g. &amp;nbsp;, &amp;gt;, etc.).
 */
export class PlainTextEncoder {
  protected _cachedElement: CachedElement<HTMLTextAreaElement>;

  constructor() {
    this._cachedElement = new CachedElement<HTMLTextAreaElement>('textarea');
  }

  encode(text: string, options?: PlainTextEncoderOptions): string {
    options = options || {};
    if (!text) {
      return text;
    }
    text = strings.asString(text);

    // Regexp is used to replace the tags.
    // It is not possible to use jquery's text() function or to create a html element and use textContent/innerText, because it does not handle line breaks as desired.

    // Remove comments
    text = text.replace(/<!--.*?--!?>/gs, '');

    // Remove font icons (needs to be executed before removing attribute values)
    if (options.removeFontIcons) {
      text = text.replace(/<span\s[^>]*class="[^"]*font-icon[^"]*"[^>]*>[^<]*<\/span>/gmi, '');
    }

    // Remove attribute values since they could contain special characters like >
    text = this.removeAttributeValues(text);

    // Convert native newlines to whitespace
    text = text.replace(/\r/g, '');
    text = text.replace(/\n/g, ' ');

    // Separate td with ' '
    text = text.replace(/<\/td>|<\/th>/gi, ' ');

    // Create newlines for certain end tags
    text = text.replace(/<br\/?><\/div>|<\/div>|<br\/?>|<\/p>|<p\/>|<\/tr>|<\/h[1-6]>|<\/dt>|<\/dd>|<\/dl>|<\/li>|<\/head>/gi, '\n');

    // Remove script and style contents
    text = text.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
    text = text.replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '');

    // Replace remaining tags
    text = text.replace(/<[^\s>][^>]*>/gi, '');

    // Remove multiple spaces
    text = text.replace(/[ ]+/g, ' ');

    // Remove spaces at the beginning and end of each line
    text = text.replace(/^[ ]+/gm, '');
    text = text.replace(/[ ]+$/gm, '');

    if (options.compact) {
      // Compact consecutive empty lines. One is enough
      text = text.replace(/\n{3,}/gm, '\n\n');
    }
    if (scout.nvl(options.trim, true)) {
      text = text.trim();
    }

    // Decore character references
    let textarea = this._cachedElement.get();
    textarea.innerHTML = text;
    text = textarea.value;

    // Convert non-breaking spaces to normal spaces
    text = text.replace(/\u00A0/g, ' ');

    return text;
  }

  removeAttributeValues(text: string): string {
    // Keep in sync with HtmlHelper.removeAttributeValues

    let lastAttributeQuote: string = null;
    let insideTag = false;
    let result = '';

    for (let i = 0; i < text.length; i++) {
      let c = text[i];
      if (lastAttributeQuote) {
        // inside quoted attribute value
        if (c === lastAttributeQuote) {
          // end of quoted attribute value
          lastAttributeQuote = null;
        } else {
          // ignore all characters beside closing attribute value quote
          continue;
        }
      } else if (insideTag && (c === '\'' || c === '"')) {
        // start of quoted attribute value
        lastAttributeQuote = c;
      } else if (c === '<' && text.length > i + 1 && !/\s/.test(text[i + 1])) {
        // start of tag
        insideTag = true;
      } else if (c === '>') {
        // end of tag
        insideTag = false;
      }
      result += c;
    }
    return result;
  }
}

export interface PlainTextEncoderOptions {
  /**
   * If true, multiple consecutive empty lines are reduced to a single empty line. Default is false.
   */
  compact?: boolean;
  /**
   * If true, empty lines at the beginning and the end of the text are removed. Default is true.
   *
   * Spaces at the beginning and at the end of *every* line are *always* removed.
   */
  trim?: boolean;
  /**
   * Removes font icons. Default is false.
   */
  removeFontIcons?: boolean;
}
