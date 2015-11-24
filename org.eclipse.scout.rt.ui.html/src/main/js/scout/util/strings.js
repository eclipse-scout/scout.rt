/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.strings = {

  /**
   * @param text
   * @param encodeHtml defaults to true
   */
  nl2br: function(text, encodeHtml) {
    if (text === undefined || text === null || text === '') {
      return text;
    }
    text = this.asString(text);
    encodeHtml = scout.helpers.nvl(encodeHtml, true);
    if (encodeHtml) {
      text = scout.strings.encode(text);}

    return text.replace(/\n/g, '<br>').replace(/\r/g, '');
  },

  removeAmpersand: function(text) {
    if (text === undefined || text === null || text === '') {
      return text;
    }
    text = this.asString(text);
    // Remove single & that are not surrounded by & or &&
    text = text.replace(/(^|[^&]|&&)&($|[^&]|&&)/g, '$1$2');
    // Replace remaining && by a single &
    text = text.replace(/&&/g, '&');
    return text;
  },

  insertAt: function(text, insertText, position){
    if (text === undefined || text === null || text === '') {
      return text;
    }
    text = this.asString(text);
    insertText = this.asString(insertText);
    if (insertText && (typeof position === 'number' || position instanceof Number) && position >= 0) {
      return text.substr(0, position) + insertText + text.substr(position);
    }
    return text;
  },

  getMnemonic: function(text, resolveKey) {
    if (text === undefined || text === null) {
      return text;
    }
    text = this.asString(text);
    // Remove escaped & (they are not of concern)
    text = text.replace(/&&/g, '');
    var m = text.match(/&(.)/);
    if (m !== null) {
      // Potential mnemonic found
      var mnemonic = m[1];
      if (mnemonic) {
        // Unless disabled explicitly, check if mnemonic matches with a known key
        if (scout.helpers.nvl(resolveKey, true) && !scout.keys[mnemonic.toUpperCase()]) {
          mnemonic = null;
        }
        return mnemonic;
      }
    }
    return null;
  },

  /**
   * @returns true if the given string contains any non-space characters
   */
  hasText: function(text) {
    text = this.asString(text);
    if (typeof text !== 'string' || text.length === 0) {
      return false;
    }
    return !/^\s*$/.test(text);
  },

  repeat: function(pattern, count) {
    if (pattern === undefined || pattern === null) {
      return pattern;
    }
    if (typeof count !== 'number' || count < 1) {
      return '';
    }
    var result = '';
    for (var i = 0; i < count; i++) {
      result += pattern;
    }
    return result;
  },

  padZeroLeft: function(string, padding) {
    string = this.asString(string);
    if (string === undefined || string === null || typeof padding !== 'number' || padding < 1 || (string + '').length >= padding) {
      return string;
    }
    var z = scout.strings.repeat('0', padding) + string;
    return z.slice(-padding);
  },

  startsWith: function(fullString, startString) {
    if (fullString === undefined || fullString === null || startString === undefined || startString === null) {
      return false;
    }
    if (startString.length === 0) {
      return true;
    }
    fullString = this.asString(fullString);
    startString = this.asString(startString);
    return (fullString.substr(0, startString.length) === startString);
  },

  endsWith: function(fullString, endString) {
    if (fullString === undefined || fullString === null || endString === undefined || endString === null) {
      return false;
    }
    if (endString.length === 0) {
      return true;
    }
    fullString = this.asString(fullString);
    endString = this.asString(endString);
    return (fullString.substr(-endString.length) === endString);
  },

  /**
   * Returns the number of occurrences of 'separator' in 'string'
   */
  count: function(string, separator) {
    if (string === undefined || string === null || separator === undefined || separator === null) {
      return 0;
    }
    string = this.asString(string);
    separator = this.asString(separator);
    return string.split(separator).length - 1;
  },

  /**
   * Encodes the html of the given string.
   */
  encode: function(string) {
    if (!string) {
      return string;
    }
    var elem = scout.strings.encodeElement;
    if (!elem) {
      elem = window.document.createElement('div');
      // cache it to prevent creating an element every time
      scout.strings.encodeElement = elem;
    }
    elem.textContent = string;
    return elem.innerHTML;
  },

  /**
   * Returns the plain text of the given html string using simple tag replacement.<p>
   * Tries to preserve the new lines. Since it does not consider the style, it won't be right in any cases.
   * A div for example always generates a new line, even if display style is not set to block.
   */
  plainText: function(text) {
    if (!text) {
      return text;
    }
    // Regexp is used to replace the tags.
    // It is not possible to use jquery's text() function or to create a html element and use textContent, because the new lines get omitted.
    // Node.innerText would preserve the new lines but it is not supported by firefox

    // Preserve new lines
    text = text.replace(/<br>|<br\/>|<\/p>|<p\/>|<\/div>|<\/li>|<\/tr>/gi, '\n');

    // Separate td with ' '
    text = text.replace(/<\/td>/gi, ' ');

    // Replace remaining tags
    text = text.replace(/<[^>]+>/gi, '');

    // Remove spaces at the beginning and end of each line
    text = text.replace(/^[ ]+/gm, '');
    text = text.replace(/[ ]+$/gm, '');

    // Replace character html entities (e.g. &nbsp;, &gt;, ...)
    var textarea = scout.strings.plainTextElement;
    if (!textarea) {
      textarea = document.createElement('textarea');
      // cache it to prevent creating an element every time
      scout.strings.plainTextElement = textarea;
    }
    textarea.innerHTML = text;
    return textarea.value;
  },

  /**
   * Joins a list of strings to a single string using the given separator. Elements that are
   * not defined or have zero length are ignored. The default return value is the empty string.
   *
   * @param separator String to use as separator
   * @param varargs List of strings to join
   */
  join: function(separator) {
    separator = this.asString(separator);
    var s = '';
    for (var i = 1; i < arguments.length; i++) {
      var arg = this.asString(arguments[i]);
      if (arg) {
        if (s && separator) {
          s += separator;
        }
        s += arg;
      }
    }
    return s;
  },

  /**
   * If the given 'string' has text, it is returned with the 'prefix' and 'suffix'
   * prepended and appended, respectively. Otherwise, the empty string is returned.
   */
  box: function(prefix, string, suffix) {
    prefix = this.asString(prefix);
    string = this.asString(string);
    suffix = this.asString(suffix);
    var s = '';
    if (this.hasText(string)) {
      if (prefix) {
        s += prefix;
      }
      s += string;
      if (suffix) {
        s += suffix;
      }
    }
    return s;
  },

  /**
   * If the given 'string' has text, its first letter is returned in lower case,
   * the remainder is unchanged. Otherwise, the empty string is returned.
   */
  lowercaseFirstLetter: function(string) {
    if (string === undefined || string === null) {
      return string;
    }
    string = this.asString(string);
    var s = '';
    if (this.hasText(string)) {
      s = string.charAt(0).toLowerCase() + string.slice(1);
    }
    return s;
  },

  /**
   * Quotes a string for use in a regular expression, i.e. escapes all characters with special meaning.
   */
  quote: function(string) {
    if (string === undefined || string === null) {
      return string;
    }
    string = this.asString(string);
    // see "escapeRegExp()" from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#Using_special_characters
    return string.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"); // $& = last match
  },

  /**
   * If the given input is not of type string, it is converted to a string (using the standard
   * JavaScript "String()" function). Inputs 'null' and 'undefined' are returned as they are.
   */
  asString: function(input) {
    if (input === undefined || input === null) {
      return input;
    }
    if (typeof input === 'string' || input instanceof String) {
      return input;
    }
    return String(input);
  }
};
