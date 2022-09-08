/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlEncoder, objects, PlainTextEncoder, scout} from '../index';
import {PlainTextEncoderOptions} from '../encoder/PlainTextEncoder';

let htmlEncoder: HtmlEncoder = null;
let plainTextEncoder: PlainTextEncoder = null;
let lineFeedRegex = /\n/g;
let carriageReturnRegex = /\r/g;
let whitespaceRegex = /^\s*$/;

/**
 * @param [encodeHtml] defaults to true
 */
export function nl2br(text: string, encodeHtml?: boolean): string {
  if (!text) {
    return text;
  }
  text = asString(text);
  encodeHtml = scout.nvl(encodeHtml, true);
  if (encodeHtml) {
    text = encode(text);
  }
  return text.replace(lineFeedRegex, '<br>').replace(carriageReturnRegex, '');
}

export function insertAt(text: string, insertText: string, position: number): string {
  if (!text) {
    return text;
  }
  text = asString(text);
  insertText = asString(insertText);
  // @ts-ignore
  if (insertText && (typeof position === 'number' || position instanceof Number) && position >= 0) {
    return text.substr(0, position) + insertText + text.substr(position);
  }
  return text;
}

/**
 * @returns true if the given string contains any non-space characters
 */
export function hasText(text: string): boolean {
  if (text === undefined || text === null) {
    return false;
  }
  text = asString(text);
  if (typeof text !== 'string' || text.length === 0) {
    return false;
  }
  return !whitespaceRegex.test(text);
}

/**
 * Inverse operation of hasText(string). Used because empty(s) is more readable than !hasText(s).
 * @returns true if the given string is not set or contains only white-space characters.
 */
export function empty(text: string): boolean {
  return !hasText(text);
}

export function repeat(pattern: string, count: number): string {
  if (pattern === undefined || pattern === null) {
    return pattern;
  }
  if (typeof count !== 'number' || count < 1) {
    return '';
  }
  let result = '';
  for (let i = 0; i < count; i++) {
    result += pattern;
  }
  return result;
}

export function padZeroLeft(string: string | number, padding: number): string {
  string = asString(string);
  if (string === undefined || string === null || typeof padding !== 'number' || padding < 1 || (string + '').length >= padding) {
    return string;
  }
  let z = repeat('0', padding) + string;
  return z.slice(-padding);
}

export function contains(string: string, searchFor: string) {
  if (!string) {
    return false;
  }
  return string.indexOf(searchFor) > -1;
}

export function startsWith(fullString: string, startString: string): boolean {
  // noinspection DuplicatedCode
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

export function endsWith(fullString: string, endString: string): boolean {
  // noinspection DuplicatedCode
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
export function count(string: string, separator: string): number {
  if (!string || separator === undefined || separator === null) {
    return 0;
  }
  string = asString(string);
  separator = asString(separator);
  return string.split(separator).length - 1;
}

/**
 * Returns the HTML encoded text. Example: 'Foo<br>Bar' returns 'Foo&amp;lt;br&amp;gt;Bar'.
 * If the argument is or undefined, the same value is returned.
 * @param text plain text to encode
 * @return HTML encoded text
 */
export function encode(text: string): string {
  if (!htmlEncoder) { // lazy instantiation to avoid cyclic dependency errors during webpack bootstrap
    htmlEncoder = new HtmlEncoder();
  }
  return htmlEncoder.encode(text);
}

/**
 * Returns the plain text of the given html string using simple tag replacement.<p>
 * Tries to preserve the new lines. Since it does not consider the style, it won't be right in any cases.
 * A div for example always generates a new line, even if display style is not set to block.
 */
export function plainText(text: string, options?: PlainTextEncoderOptions) {
  if (!plainTextEncoder) { // lazy instantiation to avoid cyclic dependency errors during webpack bootstrap
    plainTextEncoder = new PlainTextEncoder();
  }
  return plainTextEncoder.encode(text, options);
}

/**
 * Joins a list of strings to a single string using the given separator. Elements that are
 * not defined or have zero length are ignored. The default return value is the empty string.
 *
 * @param separator String to use as separator
 * @param args list of strings to join. Can be an array or individual arguments
 */
export function join(separator: string, ...args: string[]): string {
  let stringsToJoin = args;
  if (args[0] && objects.isArray(args[0])) {
    stringsToJoin = (args[0] as unknown) as string[];
  }
  separator = asString(separator);
  let s = '';
  for (let i = 0; i < stringsToJoin.length; i++) {
    let arg = asString(stringsToJoin[i]);
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
export function box(prefix: string, string: string, suffix?: string): string {
  prefix = asString(prefix);
  string = asString(string);
  suffix = asString(suffix);
  let s = '';
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
 * Quotes a string for use in a regular expression, i.e. escapes all characters with special meaning.
 */
export function quote(string: string): string {
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
export function asString(input: any): string {
  if (input === undefined || input === null) {
    return input;
  }
  if (typeof input === 'string' || input instanceof String) {
    return input as string;
  }
  return String(input);
}

/**
 * This is a shortcut for <code>scout.nvl(string, '')</code>.
 * @param string String to check
 * @returns Empty string '' when given string is null or undefined.
 */
export function nvl(string: string): string {
  if (arguments.length > 1) {
    throw new Error('strings.nvl only accepts one argument. Use scout.nvl if you need to handle multiple arguments');
  }
  return scout.nvl(string, '');
}

/**
 * Null-safe version of <code>String.prototype.length</code>.
 * If the argument is null or undefined, 0 will be returned.
 * A non-string argument will be converted to a string.
 */
export function length(string: string): number {
  string = asString(string);
  return (string ? string.length : 0);
}

/**
 * Null-safe version of <code>String.prototype.trim</code>.
 * If the argument is null or undefined, the same value will be returned.
 * A non-string argument will be converted to a string.
 */
export function trim(string: string): string {
  string = asString(string);
  return (string ? string.trim() : string);
}

/**
 * Null-safe version of <code>String.prototype.toUpperCase</code>.
 * If the argument is null or undefined, the same value will be returned.
 * A non-string argument will be converted to a string.
 */
export function toUpperCase(string: string): string {
  string = asString(string);
  return (string ? string.toUpperCase() : string);
}

/**
 * Null-safe version of <code>String.prototype.toLowerCase</code>.
 * If the argument is null or undefined, the same value will be returned.
 * A non-string argument will be converted to a string.
 */
export function toLowerCase(string: string): string {
  string = asString(string);
  return (string ? string.toLowerCase() : string);
}

/**
 * Returns the given string, with the first character converted to upper case and the remainder unchanged.
 * If the argument is null or undefined, the same value will be returned.
 * A non-string argument will be converted to a string.
 */
export function toUpperCaseFirstLetter(string: string): string {
  string = asString(string);
  if (!string) {
    return string;
  }
  return string.substring(0, 1).toUpperCase() + string.substring(1);
}

/**
 * Returns the given string, with the first character converted to lower case and the remainder unchanged.
 * If the argument is null or undefined, the same value will be returned.
 * A non-string argument will be converted to a string.
 */
export function toLowerCaseFirstLetter(string: string): string {
  string = asString(string);
  if (!string) {
    return string;
  }
  return string.substring(0, 1).toLowerCase() + string.substring(1);
}

/**
 * Returns the number of unicode characters in the given string.
 * As opposed to the string.length property, astral symbols are
 * counted as one single character.
 *
 * Example: <code>'\uD83D\uDC4D'.length</code> returns 2, whereas
 * <code>countCodePoints('\uD83D\uDC4D')</code> returns 1.
 *
 * (\uD83D\uDC4D = unicode character U+1F44D 'THUMBS UP SIGN')
 */
export function countCodePoints(string: string): number {
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
 * <ul>
 * <li>'a-b-c'.split('-', 2)       ==>   ['a', 'b']
 * <li>splitMax('a-b-c', '-', 2)   ==>   ['a', 'b-c']
 * </ul>
 */
export function splitMax(string: string, separator: string, limit: number): string[] {
  if (string === null || string === undefined) {
    return [];
  }
  string = asString(string);
  separator = asString(separator);
  limit = Number(limit);

  let array = string.split(separator);
  if (isNaN(limit) || limit <= 0 || limit >= array.length) {
    return array;
  }

  let arrayShort = array.slice(0, limit - 1);
  let last = array.slice(limit - 1).join(separator); // combine the rest
  arrayShort.push(last);
  return arrayShort;
}

export function nullIfEmpty(string: string): string {
  return empty(string) ? null : string;
}

/**
 * Null safe case sensitive comparison of two strings.
 *
 * @param [ignoreCase] optional flag to perform case insensitive comparison
 */
export function equals(a: string, b: string, ignoreCase?: boolean): boolean {
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
  }
  return a === b;
}

export function equalsIgnoreCase(a: string, b: string): boolean {
  return equals(a, b, true);
}

export function removePrefix(string: string, prefix: string): string {
  let s = string;
  if (startsWith(string, prefix)) {
    s = string.substring(prefix.length);
  }
  return s;
}

export function removeSuffix(string: string, suffix: string): string {
  let s = string;
  if (endsWith(string, suffix)) {
    s = string.substring(0, string.length - suffix.length);
  }
  return s;
}

/**
 * Truncates the given text and appends '...' so it fits into the given horizontal space.
 * @param text the text to be truncated
 * @param horizontalSpace the horizontal space the text needs to fit into
 * @param measureText a function that measures the span of a text, it needs to return an object containing a 'width' property.
 * @return the truncated text
 */
export function truncateText(text: string, horizontalSpace: number, measureText: (text: string) => { width: number }): string {
  if (text && horizontalSpace && measureText && horizontalSpace > 0 && measureText(text).width > horizontalSpace) {
    text = text.trim();
    if (measureText(text).width <= horizontalSpace) {
      return text;
    }
    let upperBound = text.length, // exclusive
      lowerBound = 0; // inclusive
    while (lowerBound + 1 < upperBound) {
      let textLength = Math.round((upperBound + lowerBound) / 2);
      if (measureText(text.slice(0, textLength) + '...').width > horizontalSpace) {
        upperBound = textLength;
      } else {
        lowerBound = textLength;
      }
    }
    return text.slice(0, lowerBound).trim() + '...';
  }
  return text;
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
  length,
  nl2br,
  nullIfEmpty,
  nvl,
  padZeroLeft,
  plainText,
  quote,
  removePrefix,
  removeSuffix,
  repeat,
  splitMax,
  startsWith,
  toLowerCase,
  toLowerCaseFirstLetter,
  toUpperCase,
  toUpperCaseFirstLetter,
  trim,
  truncateText
};
