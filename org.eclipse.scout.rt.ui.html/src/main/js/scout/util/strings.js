/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects} from '../index';
import {scout} from '../index';



/**
 * @param text
 * @param encodeHtml defaults to true
 * @memberOf scout.strings
 */
export function nl2br(text, encodeHtml) {
  if (!text) {
    return text;
  }
  text = asString(text);
  encodeHtml = scout.nvl(encodeHtml, true);
  if (encodeHtml) {
    text = encode(text);
  }
  return text.replace(/\n/g, '<br>').replace(/\r/g, '');
}

export function insertAt(text, insertText, position) {
  if (!text) {
    return text;
  }
  text = asString(text);
  insertText = asString(insertText);
  if (insertText && (typeof position === 'number' || position instanceof Number) && position >= 0) {
    return text.substr(0, position) + insertText + text.substr(position);
  }
  return text;
}

/**
 * @returns true if the given string contains any non-space characters
 */
export function hasText(text) {
  if (text === undefined || text === null) {
    return false;
  }
  text = asString(text);
  if (typeof text !== 'string' || text.length === 0) {
    return false;
  }
  return !/^\s*$/.test(text);
}

/**
 * Inverse operation of hasText(string). Used because empty(s) is more readable than !hasText(s).
 * @returns true if the given string is not set or contains only white-space characters.
 */
export function empty(text) {
  return !hasText(text);
}

export function repeat(pattern, count) {
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
}

export function padZeroLeft(string, padding) {
  string = asString(string);
  if (string === undefined || string === null || typeof padding !== 'number' || padding < 1 || (string + '').length >= padding) {
    return string;
  }
  var z = repeat('0', padding) + string;
  return z.slice(-padding);
}

export function contains(string, searchFor) {
  if (!string) {
    return false;
  }
  return string.indexOf(searchFor) > -1;
}

export function startsWith(fullString, startString) {
  if (fullString === undefined || fullString === null || startString === undefined || startString === null) {
    return false;
  }
  fullString = asString(fullString);
  startString = asString(startString);
  if (startString.length === 0) {
    return true; // every string starts with the empty string
  }
  if (fullString.length === 0) {
    return false; // empty string cannot start with non-empty string
  }
  return (fullString.substr(0, startString.length) === startString);
}

export function endsWith(fullString, endString) {
  if (fullString === undefined || fullString === null || endString === undefined || endString === null) {
    return false;
  }
  fullString = asString(fullString);
  endString = asString(endString);
  if (endString.length === 0) {
    return true; // every string ends with the empty string
  }
  if (fullString.length === 0) {
    return false; // empty string cannot end with non-empty string
  }
  return (fullString.substr(-endString.length) === endString);
}

/**
 * Returns the number of occurrences of 'separator' in 'string'
 */
export function count(string, separator) {
  if (!string || separator === undefined || separator === null) {
    return 0;
  }
  string = asString(string);
  separator = asString(separator);
  return string.split(separator).length - 1;
}

// Cache used by encode(). Also referenced in stringsSpec.js.
let _encodeElement = null;

/**
 * Encodes the html of the given string.
 */
export function encode(string) {
  if (!string) {
    return string;
  }
  var elem = _encodeElement;
  if (!elem) {
    elem = window.document.createElement('div');
    // cache it to prevent creating an element every time
    _encodeElement = elem;
  }
  elem.textContent = string;
  return elem.innerHTML;
}

let plainTextElement = null;

/**
 * Returns the plain text of the given html string using simple tag replacement.<p>
 * Tries to preserve the new lines. Since it does not consider the style, it won't be right in any cases.
 * A div for example always generates a new line, even if display style is not set to block.
 *
 * Options:
 * - compact: Multiple consecutive empty lines are reduced to a single empty line
 * - trim: Calls string.trim(). White space at the beginning and the end of the text gets removed.
 */
export function plainText(text, options) {
  options = options || {};
  if (!text) {
    return text;
  }
  text = asString(text);

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

  if (options.compact) {
    // Compact consecutive empty lines. One is enough
    text = text.replace(/\n{3,}/gm, '\n\n');
  }
  if (options.trim) {
    text = text.trim();
  }

  // Replace character html entities (e.g. &nbsp;, &gt;, ...)
  var textarea = plainTextElement;
  if (!textarea) {
    textarea = document.createElement('textarea');
    // cache it to prevent creating an element every time
    plainTextElement = textarea;
  }
  textarea.innerHTML = text;
  return textarea.value;
}

/**
 * Joins a list of strings to a single string using the given separator. Elements that are
 * not defined or have zero length are ignored. The default return value is the empty string.
 *
 * @param {string} separator String to use as separator
 * @param {arguments|array} varargs List of strings to join
 */
export function join(separator, vararg) {
  var stringsToJoin;
  if (vararg && objects.isArray(vararg)) {
    stringsToJoin = vararg;
  } else {
    stringsToJoin = objects.argumentsToArray(arguments).slice(1);
  }
  separator = asString(separator);
  var s = '';
  for (var i = 0; i < stringsToJoin.length; i++) {
    var arg = asString(stringsToJoin[i]);
    if (arg) {
      if (s && separator) {
        s += separator;
      }
      s += arg;
    }
  }
  return s;
}

/**
 * If the given 'string' has text, it is returned with the 'prefix' and 'suffix'
 * prepended and appended, respectively. Otherwise, the empty string is returned.
 */
export function box(prefix, string, suffix) {
  prefix = asString(prefix);
  string = asString(string);
  suffix = asString(suffix);
  var s = '';
  if (hasText(string)) {
    if (prefix) {
      s += prefix;
    }
    s += string;
    if (suffix) {
      s += suffix;
    }
  }
  return s;
}

/**
 * If the given 'string' has text, its first letter is returned in lower case,
 * the remainder is unchanged. Otherwise, the empty string is returned.
 */
export function lowercaseFirstLetter(string) {
  return _changeFirstLetter(string, 'toLowerCase');
}

/**
 * If the given 'string' has text, its first letter is returned in upper case,
 * the remainder is unchanged. Otherwise, the empty string is returned.
 */
export function uppercaseFirstLetter(string) {
  return _changeFirstLetter(string, 'toUpperCase');
}

//private
 export function _changeFirstLetter(string, funcName) {
  if (string === undefined || string === null) {
    return string;
  }
  string = asString(string);
  var s = '';
  if (hasText(string)) {
    s = string.charAt(0)[funcName]() + string.slice(1);
  }
  return s;
}

/**
 * Quotes a string for use in a regular expression, i.e. escapes all characters with special meaning.
 */
export function quote(string) {
  if (string === undefined || string === null) {
    return string;
  }
  string = asString(string);
  // see "escapeRegExp()" from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#Using_special_characters
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& = last match
}

/**
 * If the given input is not of type string, it is converted to a string (using the standard
 * JavaScript "String()" function). Inputs 'null' and 'undefined' are returned as they are.
 */
export function asString(input) {
  if (input === undefined || input === null) {
    return input;
  }
  if (typeof input === 'string' || input instanceof String) {
    return input;
  }
  return String(input);
}

/**
 * Returns an empty string '', when given string is null or undefined.
 * This is a shortcut for <code>scout.nvl(string, '')</code>.
 */
export function nvl(string) {
  return scout.nvl(string, '');
}

export function toUpperCaseFirstLetter(string) {
  return string.substring(0, 1).toUpperCase() + string.substring(1);
}

/**
 * Returns the number of unicode characters in the given string.
 * As opposed to the string.length property, astral symbols are
 * counted as one single character.
 * Example: <code>'\uD83D\uDC4D'.length</code> returns 2, whereas
 * <code>countCharpoints('\uD83D\uDC4D')</code> returns 1.
 * (\uD83D\uDC4D is Unicode Character 'THUMBS UP SIGN' (U+1F44D))
 */
export function countCodePoints(string) {
  return string
  // Replace every surrogate pair with a BMP symbol.
    .replace(/[\uD800-\uDBFF][\uDC00-\uDFFF]/g, '_')
    // and then get the length.
    .length;
}

/**
 * Splits the given 'string' at 'separator' while returning at most 'limit' elements.
 * Unlike String.prototype.split(), this function does not discard elements if more than
 * 'limit' elements are found. Instead, the surplus elements are joined with the last element.
 *
 * Example:
 *   'a-b-c'.split('-', 2)                     ==>   ['a', 'b']
 *   splitMax('a-b-c', '-', 2)   ==>   ['a', 'b-c']
 */
export function splitMax(string, separator, limit) {
  if (string === null || string === undefined) {
    return [];
  }
  string = asString(string);
  separator = asString(separator);
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

export function nullIfEmpty(string) {
  return empty(string) ? null : string;
}

/**
 * Null safe case sensitive comparison of two strings.
 *
 * @param [ignoreCase] optional flag to perform case insensitive comparison
 */
export function equals(a, b, ignoreCase) {
  a = nullIfEmpty(a);
  b = nullIfEmpty(b);
  if (!a && !b) {
    return true;
  }
  if (!a || !b) {
    return false;
  }
  if (ignoreCase) {
    return a.toLowerCase() === b.toLowerCase();
  } else {
    return a === b;
  }
}

export function equalsIgnoreCase(a, b) {
  return equals(a, b, true);
}

export function removePrefix(string, prefix) {
  var s = string;
  if (startsWith(string, prefix)) {
    s = string.substring(prefix.length);
  }
  return s;
}

export function removeSuffix(string, suffix) {
  var s = string;
  if (endsWith(string, suffix)) {
    s = string.substring(0, string.length - suffix.length);
  }
  return s;
}

//private
 export function _setPlainTextElement(el) {
  plainTextElement = el;
}

//private
 export function _getPlainTextElement() {
  return plainTextElement;
}

//private
 export function _setEncodeElement(el) {
  _encodeElement = el;
}

//private
 export function _getEncodeElement() {
  return _encodeElement;
}


export default {
  asString,
  box,
  contains,
  count,
  countCodePoints,
  empty,
  encode,
  endsWith,
  equals,
  equalsIgnoreCase,
  hasText,
  insertAt,
  join,
  lowercaseFirstLetter,
  nl2br,
  nullIfEmpty,
  nvl,
  padZeroLeft,
  plainText,
  plainTextElement,
  quote,
  removePrefix,
  removeSuffix,
  repeat,
  splitMax,
  startsWith,
  toUpperCaseFirstLetter,
  uppercaseFirstLetter
};
