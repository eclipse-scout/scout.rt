import * as scout from '../scout';

let _encodeElement = null;

function encode(string) {
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

function asString(input) {
  if (input === undefined || input === null) {
    return input;
  }
  if (typeof input === 'string' || input instanceof String) {
    return input;
  }
  return String(input);
}

function hasText(text) {
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
function empty(text) {
  return !hasText(text);
}

function box(prefix, string, suffix) {
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

function startsWith(fullString, startString) {
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

function quote(string) {
  if (string === undefined || string === null) {
    return string;
  }
  string = asString(string);
  // see 'escapeRegExp()' from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#Using_special_characters
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& = last match
}

function count(string, separator) {
  if (!string || separator === undefined || separator === null) {
    return 0;
  }
  string = asString(string);
  separator = asString(separator);
  return string.split(separator).length - 1;
}

function repeat(pattern, count) {
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

function padZeroLeft(string, padding) {
  string = asString(string);
  if (string === undefined || string === null || typeof padding !== 'number' || padding < 1 || (string + '').length >= padding) {
    return string;
  }
  var z = repeat('0', padding) + string;
  return z.slice(-padding);
}

function join(separator, vararg) {
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

function contains(string, searchFor) {
  if (!string) {
    return false;
  }
  return string.indexOf(searchFor) > -1;
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

function uppercaseFirstLetter(string) {
  return _changeFirstLetter(string, 'toUpperCase');
}

export {
  asString,
  box,
  contains,
  count,
  empty,
  encode,
  hasText,
  join,
  padZeroLeft,
  quote,
  repeat,
  startsWith,
  uppercaseFirstLetter
};
