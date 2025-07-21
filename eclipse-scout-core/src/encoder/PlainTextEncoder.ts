/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CachedElement, strings} from '../index';

export interface PlainTextEncoderOptions {
  /**
   * Multiple consecutive empty lines are reduced to a single empty line. Default is false.
   */
  compact?: boolean;

  /**
   * Calls string.trim(). Empty lines at the beginning and the end of the text get removed. Default is false.
   * Spaces are always removed.
   */
  trim?: boolean;

  /**
   * Removes font icons. Default is false.
   */
  removeFontIcons?: boolean;
}

/**
 * Replaces character HTML entities (e.g. &amp;nbsp;, &amp;gt;, etc.).
 */
export class PlainTextEncoder {
  cache: CachedElement;

  constructor() {
    this.cache = new CachedElement('textarea');
  }

  encode(text: string, options?: PlainTextEncoderOptions): string {
    options = options || {};
    if (!text) {
      return text;
    }
    text = strings.asString(text);

    // Regexp is used to replace the tags.
    // It is not possible to use jquery's text() function or to create a html element and use textContent, because the new lines get omitted.
    // Node.innerText would preserve the new lines but it is not supported by firefox

    // Remove comments
    text = text.replace(/<!--.*?--!?>/gs, '');

    // Remove font icons (needs to be executed before removing attribute values)
    if (options.removeFontIcons) {
      text = text.replace(/<span\s[^>]*class="[^"]*font-icon[^"]*"[^>]*>[^<]*<\/span>/gmi, '');
    }

    // Remove attribute values since they could contain special characters like >
    text = this.removeAttributeValues(text);

    // Preserve new lines
    text = text.replace(/<br>|<br\/>|<\/p>|<p\/>|<\/div>|<\/li>|<\/tr>/gi, '\n');

    // Separate td with ' '
    text = text.replace(/<\/td>/gi, ' ');

    // Remove script and style contents
    text = text.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
    text = text.replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '');

    // Replace remaining tags
    text = text.replace(/<[^\s>][^>]*>/gi, '');

    // Convert decimal nrc to unicode
    text = text.replace('&zwj;', String.fromCharCode(0x200D)); // zero width joiner for combined chars
    text = text.replace(/&#(\\d+);/gi, (match, group1) => String.fromCharCode(group1));

    // Remove spaces at the beginning and end of each line
    text = text.replace(/^[ ]+/gm, '');
    text = text.replace(/[ ]+$/gm, '');

    if (options.compact) {
      // Compact consecutive empty lines. One is enough
      text = text.replace(/\n{3,}/gm, '\n\n');
    }
    if (options.trim) {
      text = text.trim();
    }

    let textarea = this.cache.get() as HTMLTextAreaElement;
    textarea.innerHTML = text;
    return textarea.value;
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
