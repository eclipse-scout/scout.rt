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
/**
 * Input is expected to be encoded. Output (toString()) is also encoded.
 * If no URL is passed, 'window.location.href' is used as input.
 */
scout.URL = function(url) {
  if (url === undefined) {
    url = window.location.href;
  }
  var urlParts = /^([^?#]*)(?:\?([^#]*))?(?:#(.*))?$/.exec(url || '');
  // encoded
  this.baseUrlRaw = urlParts[1];
  this.queryPartRaw = urlParts[2];
  this.hashPartRaw = urlParts[3];
  // un-encoded (!)
  this.parameterMap = scout.URL._parse(this.queryPartRaw);
};

scout.URL.prototype.getParameter = function(param) {
  if (typeof param !== 'string') {
    throw new Error('Illegal argument type: ' + param);
  }
  var value = this.parameterMap[param];
  if (Array.isArray(value)) {
    return value.sort(scout.URL._sorter);
  }
  return value;
};

scout.URL.prototype.removeParameter = function(param) {
  if (typeof param !== 'string') {
    throw new Error('Illegal argument type: ' + param);
  }
  delete this.parameterMap[param];
  return this;
};

scout.URL.prototype.setParameter = function(param, value) {
  if (typeof param !== 'string') {
    throw new Error('Illegal argument type: ' + param);
  }
  if (param === '') { // ignore empty keys
    return;
  }
  this.parameterMap[param] = value;
  return this;
};

scout.URL.prototype.addParameter = function(param, value) {
  if (typeof param !== 'string') {
    throw new Error('Illegal argument type: ' + param);
  }
  if (param === '') { // ignore empty keys
    return;
  }
  scout.URL._addToMap(this.parameterMap, param, value);
  return this;
};

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
scout.URL.prototype.toString = function(options) {
  var result = this.baseUrlRaw;

  if (Object.keys(this.parameterMap).length) {
    options = options || {};
    var sorter = options.sorter || scout.URL._sorter;
    if (options.alwaysFirst || options.alwaysLast) {
      options.alwaysFirst = scout.arrays.ensure(options.alwaysFirst);
      options.alwaysLast = scout.arrays.ensure(options.alwaysLast);
      var origSorter = sorter;
      sorter = function(a, b) {
        var firstA = options.alwaysFirst.indexOf(a);
        var firstB = options.alwaysFirst.indexOf(b);
        var lastA = options.alwaysLast.indexOf(a);
        var lastB = options.alwaysLast.indexOf(b);
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
    var reconstructedQueryPart = Object.keys(this.parameterMap).sort(sorter).map(function(key) {
      var value = this.getParameter(key);
      // For multiple values, generate a parameter string for each value
      if (Array.isArray(value)) {
        return value.map(
          function(innerKey, innerIndex) {
            return scout.URL._formatQueryParam(key, value[innerIndex]);
          }
        ).join('&');
      }
      return scout.URL._formatQueryParam(key, value);
    }.bind(this)).join('&');
    result += '?' + reconstructedQueryPart;
  }

  if (this.hashPartRaw) {
    result += '#' + this.hashPartRaw;
  }

  return result;
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * Helper function to sort arrays alphabetically, nulls in front
 *
 * @memberOf scout.URL
 */
scout.URL._sorter = function(a, b) {
  return a === null ? -1 : b === null ? 1 : a.toString().localeCompare(b);
};

/**
 * Helper function to build a query parameter with value
 *
 * @memberOf scout.URL
 */
//
scout.URL._formatQueryParam = function(key, value) {
  var s = encodeURIComponent(key);
  if (value !== undefined && value !== null) {
    s += '=' + encodeURIComponent(value);
  }
  return s;
};

/**
 * Helper function to add an key-value pair to a map. If the key is added multiple
 * times, the value is converted to an array.
 *
 * @memberOf scout.URL
 */
scout.URL._addToMap = function(map, key, value) {
  if (map === undefined) {
    throw new Error("Argument 'map' must not be null");
  }
  if (key === undefined) {
    throw new Error("Argument 'key' must not be null");
  }
  if (key in map) {
    var oldValue = map[key];
    if (Array.isArray(oldValue)) {
      oldValue.push(value);
    } else {
      map[key] = [oldValue, value];
    }
  } else {
    map[key] = value;
  }
};

/**
 * Helper function to parse the given (encoded) query string and return
 * it as (un-encoded) map of key-value pairs.
 *
 * @memberOf scout.URL
 */
scout.URL._parse = function(queryPart) {
  var queryString = (queryPart || '').replace(/\+/g, ' '),
    pattern = /([^&=]+)(=?)([^&]*)/g,
    map = {},
    m, key, value;

  while ((m = pattern.exec(queryString))) {
    key = decodeURIComponent(m[1]);
    value = decodeURIComponent(m[3]);
    if (value === '' && m[2] !== '=') {
      value = null;
    }
    scout.URL._addToMap(map, key, value);
  }
  return map;
};
