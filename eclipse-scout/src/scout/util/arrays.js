import * as strings from './strings';

export function ensure(array) {
  if (array === undefined || array === null) {
    return [];
  }
  if (!Array.isArray(array)) {
    return [array];
  }
  return array;
}

export function diff(arr1, arr2) {
  var diff = arr1.slice();
  removeAll(diff, arr2);
  return diff;
}

export function insert(arr, element, index) {
  insertAll(arr, [element], index);
}

export function insertAll(arr, elements, index) {
  elements = ensure(elements);
  Array.prototype.splice.apply(arr, [index, 0].concat(elements));
}

export function pushAll(arr, arr2) {
  arr.push.apply(arr, arr2);
}

export function removeAll(arr, elements) {
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
}

export function find(arr, predicate, thisArg) {
  var index = findIndex(arr, predicate, thisArg);
  if (index === -1) {
    return null;
  }
  return arr[index];
}

export function findIndex(arr, predicate, thisArg) {
  if (!arr || !predicate) {
    return -1;
  }
  for (var i = 0; i < arr.length; i++) {
    if (predicate.call(thisArg, arr[i], i, arr)) {
      return i;
    }
  }
  return -1;
}

export function last(arr) {
  if (Array.isArray(arr)) {
    return arr[arr.length - 1];
  }
  return arr;
}

export function empty(arr) {
  if (Array.isArray(arr)) {
    return arr.length === 0;
  }
  return true;
}

export function remove(arr, element) {
  if (arr) {
    var index = arr.indexOf(element);
    if (index !== -1) {
      arr.splice(index, 1);
      return true;
    }
  }
  return false;
}

export function format(arr, delimiter, encodeHtml) {
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
      element = strings.encode(element);
    }
    output += element;
  }
  return output;
}
