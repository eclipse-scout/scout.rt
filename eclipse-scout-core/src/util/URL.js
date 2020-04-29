/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays} from '../index';

/**
 * Input is expected to be encoded. Output (toString()) is also encoded.
 * If no URL is passed, 'window.location.href' is used as input.
 */
export default class URL {

  constructor(url) {
    if (url === undefined) {
      url = window.location.href;
    }
    let urlParts = /^([^?#]*)(?:\?([^#]*))?(?:#(.*))?$/.exec(url || '');
    // encoded
    this.baseUrlRaw = urlParts[1];
    this.queryPartRaw = urlParts[2];
    this.hashPartRaw = urlParts[3];
    // un-encoded (!)
    this.parameterMap = URL._parse(this.queryPartRaw);
  }

  /**
   * Checks if the given parameter exists, even if value is null or an empty string.
   *
   * @param param
   * @return {boolean}
   */
  hasParameter(param) {
    return this.parameterMap.hasOwnProperty(param);
  }

  getParameter(param) {
    if (typeof param !== 'string') {
      throw new Error('Illegal argument type: ' + param);
    }
    let value = this.parameterMap[param];
    if (Array.isArray(value)) {
      return value.sort(URL._sorter);
    }
    return value;
  }

  removeParameter(param) {
    if (typeof param !== 'string') {
      throw new Error('Illegal argument type: ' + param);
    }
    delete this.parameterMap[param];
    return this;
  }

  setParameter(param, value) {
    if (typeof param !== 'string') {
      throw new Error('Illegal argument type: ' + param);
    }
    if (param === '') { // ignore empty keys
      return;
    }
    this.parameterMap[param] = value;
    return this;
  }

  addParameter(param, value) {
    if (typeof param !== 'string') {
      throw new Error('Illegal argument type: ' + param);
    }
    if (param === '') { // ignore empty keys
      return;
    }
    URL._addToMap(this.parameterMap, param, value);
    return this;
  }

  /**
   * Options:
   *
   *   sorter:
   *     a function to be used instead of the default lexical ordering
   *     based function
   *
   *   alwaysFirst:
   *     an array of parameter names that should always be first in the
   *     resulting string. Among those parameters, the order in the passed
   *     array is respected.
   *
   *   alwaysLast:
   *     similar to alwaysFirst, but puts the parameters at the end of
   *     the resulting string.
   */
  toString(options) {
    let result = this.baseUrlRaw;

    if (Object.keys(this.parameterMap).length) {
      options = options || {};
      let sorter = options.sorter || URL._sorter;
      if (options.alwaysFirst || options.alwaysLast) {
        options.alwaysFirst = arrays.ensure(options.alwaysFirst);
        options.alwaysLast = arrays.ensure(options.alwaysLast);
        let origSorter = sorter;
        sorter = (a, b) => {
          let firstA = options.alwaysFirst.indexOf(a);
          let firstB = options.alwaysFirst.indexOf(b);
          let lastA = options.alwaysLast.indexOf(a);
          let lastB = options.alwaysLast.indexOf(b);
          // If A is marked as "alwaysFirst", sort them A-B. If B is also marked as "alwaysFirst", sort them
          // by their position in the array. If only B is marked as "alwaysFirst", sort them B-A.
          if (firstA !== -1) {
            return (firstB === -1 ? -1 : firstA - firstB);
          } else if (firstB !== -1) {
            return 1;
          }
          // If A is marked as "alwaysLast", sort them B-A. If B is also marked as "alwaysLast", sort them
          // by their position in the array. If only B is marked as "alwaysLast", sort them A-B.
          if (lastA !== -1) {
            return (lastB === -1 ? 1 : lastA - lastB);
          } else if (lastB !== -1) {
            return -1;
          }
          // Default order
          return origSorter(a, b);
        };
      }
      // Built a sorted string of all formatted parameterMap entries
      let reconstructedQueryPart = Object.keys(this.parameterMap).sort(sorter).map(key => {
        let value = this.getParameter(key);
        // For multiple values, generate a parameter string for each value
        if (Array.isArray(value)) {
          return value.map(
            (innerKey, innerIndex) => {
              return URL._formatQueryParam(key, value[innerIndex]);
            }
          ).join('&');
        }
        return URL._formatQueryParam(key, value);
      }).join('&');
      result += '?' + reconstructedQueryPart;
    }

    if (this.hashPartRaw) {
      result += '#' + this.hashPartRaw;
    }

    return result;
  }

  clone() {
    return new URL(this.toString());
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * Helper function to sort arrays alphabetically, nulls in front
   *
   * @memberOf URL
   */
  static _sorter(a, b) {
    return a === null ? -1 : b === null ? 1 : a.toString().localeCompare(b);
  }

  /**
   * Helper function to build a query parameter with value
   *
   * @memberOf URL
   */
  //
  static _formatQueryParam(key, value) {
    let s = encodeURIComponent(key);
    if (value !== undefined && value !== null) {
      s += '=' + encodeURIComponent(value);
    }
    return s;
  }

  /**
   * Helper function to add an key-value pair to a map. If the key is added multiple
   * times, the value is converted to an array.
   *
   * @memberOf URL
   */
  static _addToMap(map, key, value) {
    if (map === undefined) {
      throw new Error('Argument \'map\' must not be null');
    }
    if (key === undefined) {
      throw new Error('Argument \'key\' must not be null');
    }
    if (key in map) {
      let oldValue = map[key];
      if (Array.isArray(oldValue)) {
        oldValue.push(value);
      } else {
        map[key] = [oldValue, value];
      }
    } else {
      map[key] = value;
    }
  }

  /**
   * Helper function to parse the given (encoded) query string and return
   * it as (un-encoded) map of key-value pairs.
   *
   * @memberOf URL
   */
  static _parse(queryPart) {
    let queryString = (queryPart || '').replace(/\+/g, ' '),
      pattern = /([^&=]+)(=?)([^&]*)/g,
      map = {},
      m, key, value;

    while ((m = pattern.exec(queryString))) {
      key = decodeURIComponent(m[1]);
      value = decodeURIComponent(m[3]);
      if (value === '' && m[2] !== '=') {
        value = null;
      }
      URL._addToMap(map, key, value);
    }
    return map;
  }
}
