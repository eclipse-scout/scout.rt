/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
   * Calls string.trim(). White space at the beginning and the end of the text gets removed.. Default is false.
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

    // Preserve new lines
    text = text.replace(/<br>|<br\/>|<\/p>|<p\/>|<\/div>|<\/li>|<\/tr>/gi, '\n');

    // Separate td with ' '
    text = text.replace(/<\/td>/gi, ' ');

    if (options.removeFontIcons) {
      text = text.replace(/<span\s+class="[^"]*font-icon[^"]*">[^<]*<\/span>/gmi, '');
    }

    // Replace remaining tags
    text = text.replace(/<[^>]+>/gi, '');

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
}
