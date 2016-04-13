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
scout.arrays = {

  /**
   * Ensures the given parameter is an array
   * @memberOf scout.arrays
   */
  ensure: function(array) {
    if (array === undefined || array === null) {
      return [];
    }
    if (!Array.isArray(array)) {
      return [array];
    }
    return array;
  },

  /**
   * Creates an array with the given length and initializes each value with the given initValue.
   */
  init: function(length, initValue) {
    var array = [];
    for (var i = 0; i < length; i++) {
      array[i] = initValue;
    }
    return array;
  },

  /**
   * Removes the first occurrence of the specified element from the array,
   * if it is present (optional operation).  If the array does not contain
   * the element, it is unchanged.
   *
   * @return true if the array contained the specified element
   */
  remove: function(arr, element) {
    if (arr) {
      var index = arr.indexOf(element);
      if (index !== -1) {
        arr.splice(index, 1);
        return true;
      }
    }
    return false;
  },

  /**
   * Removes every given element from the array
   *
   * @return true if the array contained at least one of the specified elements
   */
  removeAll: function(arr, elements) {
    var modified = false;
    if (!elements || elements.length === 0) {
      return false;
    }
    for (var i = arr.length - 1; i >= 0; i--) {
      if (elements.indexOf(arr[i]) > -1) {
        arr.splice(i, 1);
        modified = true;
      }
    }
    return modified;
  },

  /**
   * return index of replaced element
   */
  replace: function(arr, element, replacement) {
    var index = arr.indexOf(element);
    if (index !== -1) {
      arr[index] = replacement;
    }
    return index;
  },

  insert: function(arr, element, index) {
    arr.splice(index, 0, element);
  },

  containsAny: function(haystack, needles) {
    haystack = this.ensure(haystack);
    needles = this.ensure(needles);
    return needles.some(function contains(element) {
      return haystack.indexOf(element) >= 0;
    });
  },

  containsAll: function(haystack, needles) {
    haystack = this.ensure(haystack);
    needles = this.ensure(needles);
    return needles.every(function contains(element) {
      return haystack.indexOf(element) >= 0;
    });
  },

  first: function(arr) {
    if (Array.isArray(arr)) {
      return arr[0];
    }
    return arr;
  },

  last: function(arr) {
    if (Array.isArray(arr)) {
      return arr[arr.length - 1];
    }
    return arr;
  },

  pushAll: function(arr, arr2) {
    arr.push.apply(arr, arr2);
  },

  /**
   * Merges the two given arrays and removes duplicate entries in O(n).
   * If the arrays contain objects instead of primitives, it uses their id to check for equality.
   */
  union: function(array1, array2) {
    var result = [],
      map = {};

    array1 = this.ensure(array1);
    array2 = this.ensure(array2);

    array1.forEach(function(entry) {
      var key = entry;
      if (typeof entry === 'object') {
        key = entry.id;
      }
      map[key] = entry;
      result.push(entry);
    });

    array2.forEach(function(entry) {
      var key = entry;
      if (typeof entry === 'object') {
        key = entry.id;
      }
      if (!(key in map)) {
        result.push(entry);
      }
    });

    return result;
  },

  equalsIgnoreOrder: function(arr, arr2) {
    if (arr === arr2) {
      return true;
    } else if ((!arr || arr.length === 0) && (!arr2 || arr2.length === 0)) {
      return true;
    } else if (!arr || !arr2) {
      return false;
    } else if (arr.length !== arr2.length) {
      return false;
    }
    return scout.arrays.containsAll(arr, arr2);
  },

  equals: function(arr, arr2) {
    if (arr === arr2) {
      return true;
    } else if ((!arr || arr.length === 0) && (!arr2 || arr2.length === 0)) {
      return true;
    } else if (!arr || !arr2) {
      return false;
    } else if (arr.length !== arr2.length) {
      return false;
    }

    for (var i = 0; i < arr.length; i++) {
      if (arr[i] !== arr2[i]) {
        return false;
      }
    }
    return true;
  },

  greater: function(arr, arr2) {
    var arrLength = 0,
      arr2Length = 0;
    if (arr) {
      arrLength = arr.length;
    }
    if (arr2) {
      arr2Length = arr2.length;
    }
    return arrLength > arr2Length;
  },

  eachSibling: function(arr, element, func) {
    if (!arr || !func) {
      return;
    }
    for (var i = 0; i < arr.length; i++) {
      var elementAtI = arr[i];
      if (elementAtI !== element) {
        func(elementAtI, i);
      }
    }
  },

  find: function(arr, predicate) {
    if (!arr || !predicate) {
      return null;
    }
    for (var i = 0; i < arr.length; i++) {
      var element = arr[i];
      if (predicate(element, i)) {
        return element;
      }
    }
  },

  findFrom: function(arr, startIndex, predicate, backwards) {
    if (backwards) {
      return scout.arrays.findFromPrev(arr, startIndex, predicate);
    } else {
      return scout.arrays.findFromNext(arr, startIndex, predicate);
    }
  },

  findFromNext: function(arr, startIndex, predicate) {
    if (!arr || !predicate) {
      return null;
    }
    for (var i = startIndex; i < arr.length; i++) {
      var element = arr[i];
      if (predicate(element, i)) {
        return element;
      }
    }
  },

  findFromPrev: function(arr, startIndex, predicate) {
    if (!arr || !predicate) {
      return null;
    }
    for (var i = startIndex; i >= 0; i--) {
      var element = arr[i];
      if (predicate(element, i)) {
        return element;
      }
    }
  },

  /**
   * @param encoded defaults to false
   */
  format: function(arr, delimiter, encodeHtml) {
    if (!arr || arr.length === 0) {
      return '';
    }

    var output = '';
    for (var i = 0; i < arr.length; i++) {
      var element = arr[i];
      if (delimiter && i > 0 && i < arr.length) {
        output += delimiter;
      }
      if (encodeHtml) {
        element = scout.strings.encode(element);
      }
      output += element;
    }
    return output;
  },

  formatEncoded: function(arr, delimiter) {
    return scout.arrays.format(arr, delimiter, true);
  },

  max: function(arr) {
    return Math.max.apply(Math, arr);
  },

  min: function(arr) {
    return Math.min.apply(Math, arr);
  },

  //
  // Use these methods if you have an array of jquery objects.
  // Reason $elem1 === $elem2 does often not work because new jquery objects are created for the same html node.
  // -> Html nodes need to be compared.
  //

  $indexOf: function(arr, $element) {
    for (var i = 0; i < arr.length; i++) {
      if (arr[i][0] === $element[0]) {
        return i;
      }
    }
  },

  $remove: function(arr, $element) {
    var index = scout.arrays.$indexOf(arr, $element);
    if (index >= 0) {
      arr.splice(index, 1);
    }
  },

  /**
   * Converts an JS array of jQuery elements to a jQuery collection.
   */
  $: function(arrayOfJQueryElements) {
    return $(arrayOfJQueryElements).map(function() {
      return this[0];
    });
  }
};
