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
   * @memberOf scout.strings
   */
  nl2br: function(text, encodeHtml) {
    if (!text) {
      return text;
    }
    text = this.asString(text);
    encodeHtml = scout.nvl(encodeHtml, true);
    if (encodeHtml) {
      text = scout.strings.encode(text);
    }
    return text.replace(/\n/g, '<br>').replace(/\r/g, '');
  },

  insertAt: function(text, insertText, position) {
    if (!text) {
      return text;
    }
    text = this.asString(text);
    insertText = this.asString(insertText);
    if (insertText && (typeof position === 'number' || position instanceof Number) && position >= 0) {
      return text.substr(0, position) + insertText + text.substr(position);
    }
    return text;
  },

  /**
   * @returns true if the given string contains any non-space characters
   */
  hasText: function(text) {
    if (text === undefined || text === null) {
      return false;
    }
    text = this.asString(text);
    if (typeof text !== 'string' || text.length === 0) {
      return false;
    }
    return !/^\s*$/.test(text);
  },

  /**
   * Inverse operation of hasText(string). Used because empty(s) is more readable than !hasText(s).
   * @returns true if the given string is not set or contains only white-space characters.
   */
  empty: function(text) {
    return !scout.strings.hasText(text);
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

  contains: function(string, searchFor) {
    if (!string) {
      return false;
    }
    return string.indexOf(searchFor) > -1;
  },

  startsWith: function(fullString, startString) {
    if (fullString === undefined || fullString === null || startString === undefined || startString === null) {
      return false;
    }
    fullString = this.asString(fullString);
    startString = this.asString(startString);
    if (startString.length === 0) {
      return true; // every string starts with the empty string
    }
    if (fullString.length === 0) {
      return false; // empty string cannot start with non-empty string
    }
    return (fullString.substr(0, startString.length) === startString);
  },

  endsWith: function(fullString, endString) {
    if (fullString === undefined || fullString === null || endString === undefined || endString === null) {
      return false;
    }
    fullString = this.asString(fullString);
    endString = this.asString(endString);
    if (endString.length === 0) {
      return true; // every string ends with the empty string
    }
    if (fullString.length === 0) {
      return false; // empty string cannot end with non-empty string
    }
    return (fullString.substr(-endString.length) === endString);
  },

  /**
   * Returns the number of occurrences of 'separator' in 'string'
   */
  count: function(string, separator) {
    if (!string || separator === undefined || separator === null) {
      return 0;
    }
    string = this.asString(string);
    separator = this.asString(separator);
    return string.split(separator).length - 1;
  },

  // Cache used by scout.strings.encode(). Also referenced in stringsSpec.js.
  _encodeElement: null,

  /**
   * Encodes the html of the given string.
   */
  encode: function(string) {
    if (!string) {
      return string;
    }
    var elem = scout.strings._encodeElement;
    if (!elem) {
      elem = window.document.createElement('div');
      // cache it to prevent creating an element every time
      scout.strings._encodeElement = elem;
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
    text = this.asString(text);

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
   * @param {string} separator String to use as separator
   * @param {arguments|array} varargs List of strings to join
   */
  join: function(separator, vararg) {
    var stringsToJoin;
    if (vararg && scout.objects.isArray(vararg)) {
      stringsToJoin = vararg;
    } else {
      stringsToJoin = scout.objects.argumentsToArray(arguments).slice(1);
    }
    separator = this.asString(separator);
    var s = '';
    for (var i = 0; i < stringsToJoin.length; i++) {
      var arg = this.asString(stringsToJoin[i]);
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
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& = last match
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
  },

  /**
   * Returns an empty string '', when given string is null or undefined.
   * This is a shortcut for <code>scout.nvl(string, '')</code>.
   */
  nvl: function(string) {
    return scout.nvl(string, '');
  },

  toUpperCaseFirstLetter: function(string) {
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  },

  /**
   * Returns the number of unicode characters in the given string.
   * As opposed to the string.length property, astral symbols are
   * counted as one single character.
   * Example: <code>'\uD83D\uDC4D'.length</code> returns 2, whereas
   * <code>scout.strings.countCharpoints('\uD83D\uDC4D')</code> returns 1.
   * (\uD83D\uDC4D is Unicode Character 'THUMBS UP SIGN' (U+1F44D))
   */
  countCodePoints: function(string) {
    return string
      // Replace every surrogate pair with a BMP symbol.
      .replace(/[\uD800-\uDBFF][\uDC00-\uDFFF]/g, '_')
      // and then get the length.
      .length;
  },

  /**
   * Splits the given 'string' at 'separator' while returning at most 'limit' elements.
   * Unlike String.prototype.split(), this function does not discard elements if more than
   * 'limit' elements are found. Instead, the surplus elements are joined with the last element.
   *
   * Example:
   *   'a-b-c'.split('-', 2)                     ==>   ['a', 'b']
   *   scout.strings.splitMax('a-b-c', '-', 2)   ==>   ['a', 'b-c']
   */
  splitMax: function(string, separator, limit) {
    if (string === null || string === undefined) {
      return [];
    }
    string = this.asString(string);
    separator = this.asString(separator);
    limit = Number(limit);

    var array = string.split(separator);
    if (isNaN(limit) || limit <= 0 || limit >= array.length) {
      return array;
    }

    var arrayShort = array.slice(0, limit - 1);
    var last = array.slice(limit - 1).join(separator); // combine the rest
    arrayShort.push(last);
    return arrayShort;
  }
};
