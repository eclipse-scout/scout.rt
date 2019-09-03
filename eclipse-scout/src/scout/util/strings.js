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
import * as scout from '../scout';


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

/**
 * Joins a list of strings to a single string using the given separator. Elements that are
 * not defined or have zero length are ignored. The default return value is the empty string.
 *
 * @param {string} separator String to use as separator
 * @param {arguments|array} varargs List of strings to join
 */
export function join(separator, vararg) {
  var stringsToJoin;
  if (vararg && Array.isArray(vararg)) {
    stringsToJoin = vararg;
  } else {
    stringsToJoin = scout.argumentsToArray(arguments).slice(1);
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
 * If the given 'string' has text, its first letter is returned in upper case,
 * the remainder is unchanged. Otherwise, the empty string is returned.
 */
export function uppercaseFirstLetter(string) {
  return _changeFirstLetter(string, 'toUpperCase');
}

// not exported!
function _changeFirstLetter(string, funcName) {
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
  // see 'escapeRegExp()' from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#Using_special_characters
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

