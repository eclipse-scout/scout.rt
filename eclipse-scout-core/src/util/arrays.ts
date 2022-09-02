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
import {objects, Predicate, strings} from '../index';

type ArrayLikeType<T> = {
  [index: number]: T;
  length: number;
};

/**
 * Ensures the given parameter is an array.
 */
export function ensure<T>(array: T[] | T): T[] {
  if (array === undefined || array === null) {
    return [];
  }
  if (!Array.isArray(array)) {
    return [array];
  }
  return array;
}

/**
 * Creates an array with the given length and initializes each value with the given initValue.
 */
export function init<T>(length: number, initValue: T): T[] {
  let array = [];
  for (let i = 0; i < length; i++) {
    array[i] = initValue;
  }
  return array;
}

/**
 * Removes the first occurrence of the specified element from the array,
 * if it is present (optional operation). If the array does not contain
 * the element, it is unchanged.
 *
 * @return true if the array contained the specified element
 */
export function remove<T>(arr: T[], element: T): boolean {
  if (arr) {
    let index = arr.indexOf(element);
    if (index !== -1) {
      arr.splice(index, 1);
      return true;
    }
  }
  return false;
}

/**
 * Removes every given element from the array
 *
 * @return true if the array contained at least one of the specified elements
 */
export function removeAll<T>(arr: T[], elements: T[]): boolean {
  let modified = false;
  if (!elements || elements.length === 0) {
    return false;
  }
  for (let i = arr.length - 1; i >= 0; i--) {
    if (elements.indexOf(arr[i]) > -1) {
      arr.splice(i, 1);
      modified = true;
    }
  }
  return modified;
}

/**
 * @return the index of the replaced element
 */
export function replace<T>(arr: T[], element: T, replacement: T): number {
  let index = arr.indexOf(element);
  if (index !== -1) {
    arr[index] = replacement;
  }
  return index;
}

/**
 * Inserts the given element at the specified index.
 * <p>
 * This function uses {@link insertAll} which relies on Array.prototype.splice(). Check its js-doc for details.
 */
export function insert<T>(arr: T[], element: T, index: number) {
  insertAll(arr, [element], index);
}

/**
 * Inserts all elements of the given array at the specified index.
 * <p>
 * This function is based on Array.prototype.splice().
 * Thus, if the 'index' is greater than the length of the array, 'elements' will be added to the end of the array 'arr'.
 * This may cause unexpected behavior on accessing arr[index] after insertion.
 *
 * The caller must ensure the size of the array.
 */
export function insertAll<T>(arr: T[], elements: T | T[], index: number) {
  elements = ensure(elements);
  // @ts-ignore // FIXME TS dom splice api wrong?
  arr.splice(...[index, 0].concat(elements));
}

/**
 * Inserts the given element into the array according to the sort order indicated by the given comparison function.
 *
 * All arguments are mandatory.
 */
export function insertSorted<T>(arr: T[], element: T, compareFunc: (a: T, b: T) => number) {
  // https://en.wikipedia.org/wiki/Binary_search_algorithm
  let left = 0;
  let right = arr.length - 1;
  while (left <= right) {
    let middle = left + Math.floor((right - left) / 2);
    let c = compareFunc(arr[middle], element);
    if (c < 0) {
      // Search in right half
      left = middle + 1;
    } else if (c > 0) {
      // Search in left half
      right = middle - 1;
    } else {
      // Found an exact match.
      // The insertion point index is equal to the last index starting from "middle" that matches
      // the element. This ensures a stable insertion order (because of the device-and-conquer
      // method, "middle" might be any of the elements with the same value).
      left = middle + 1;
      while (left < arr.length && compareFunc(arr[left], element) === 0) {
        left++;
      }
      break;
    }
  }
  // "left" now contains the index to insert the element
  arr.splice(left, 0, element);
}

/**
 * Inserts to given element into the array directly BEFORE the first array element that matches the given predicate.
 * If no such element can be found, the new element is inserted at the BEGIN of the array.
 *
 * @param thisArg optional "this" binding for predicate function
 */
export function insertBefore<T>(arr: T[], elementToInsert: T, predicate: Predicate<T>, thisArg?: any) {
  let index = findIndex(arr, predicate, thisArg);
  if (index === -1) {
    arr.unshift(elementToInsert);
  } else {
    insert(arr, elementToInsert, index);
  }
}

/**
 * Inserts to given element into the array directly AFTER the first array element that matches the given predicate.
 * If no such element can be found, the new element is inserted at the END of the array.
 */
export function insertAfter<T>(arr: T[], elementToInsert: T, predicate: Predicate<T>) {
  let index = findIndex(arr, predicate);
  if (index === -1) {
    arr.push(elementToInsert);
  } else {
    insert(arr, elementToInsert, index + 1);
  }
}

/**
 * This function uses {@link insert} which relies on Array.prototype.splice(). Check its js-doc for details.
 */
export function move<T>(arr: T[], fromIndex: number, toIndex: number) {
  let element = arr.splice(fromIndex, 1)[0];
  insert(arr, element, toIndex);
}

export function contains<T>(haystack: T[], needle: T): boolean {
  haystack = ensure(haystack);
  return haystack.indexOf(needle) !== -1;
}

export function containsAny<T>(haystack: T[], needles: T[]): boolean {
  haystack = ensure(haystack);
  needles = ensure(needles);
  return needles.some(element => {
    return haystack.indexOf(element) >= 0;
  });
}

export function containsAll<T>(haystack: T[], needles: T[]) {
  haystack = ensure(haystack);
  needles = ensure(needles);
  return needles.every(element => {
    return haystack.indexOf(element) >= 0;
  });
}

export function first<T>(arr: T[]): T {
  if (Array.isArray(arr)) {
    return arr[0];
  }
  return arr;
}

export function last<T>(arr: T[]): T {
  if (Array.isArray(arr)) {
    return arr[arr.length - 1];
  }
  return arr;
}

/**
 * @returns true if the given argument is an array and has a length > 0, false in any other case.
 */
export function hasElements<T>(arr: any): boolean {
  return !empty(arr);
}

/**
 * @returns true if the given argument is not an array or the length of the array is 0, false in any other case.
 */
export function empty<T>(arr: any): boolean {
  if (Array.isArray(arr)) {
    return arr.length === 0;
  }
  return true;
}

/**
 * @returns the size of the array, or 0 if the argument is not an array
 */
export function length<T>(arr: any): number {
  if (Array.isArray(arr)) {
    return arr.length;
  }
  return 0;
}

export function pushAll<T>(arr: T[], arr2: T | T[]) {
  arr2 = ensure(arr2);
  arr.push(...arr2);
}

/**
 * Merges the two given arrays and removes duplicate entries in O(n).
 * If the arrays contain objects instead of primitives, it uses their id to check for equality.
 */
export function union<T extends number | string | { id: string }>(array1: T[], array2: T[]): T[] {
  let result = [];
  let map = {};

  array1 = ensure(array1);
  array2 = ensure(array2);

  array1.forEach(entry => {
    let key = entry as (number | string);
    if (typeof entry === 'object') {
      key = entry.id;
    }
    map[key] = entry;
    result.push(entry);
  });

  array2.forEach(entry => {
    let key = entry as (number | string);
    if (typeof entry === 'object') {
      key = entry.id;
    }
    if (!(key in map)) {
      result.push(entry);
    }
  });

  return result;
}

export function equalsIgnoreOrder(arr: any[], arr2: any[]): boolean {
  // noinspection DuplicatedCode
  if (arr === arr2) {
    return true;
  }
  if ((!arr || arr.length === 0) && (!arr2 || arr2.length === 0)) {
    return true;
  }
  if (!arr || !arr2) {
    return false;
  }
  if (arr.length !== arr2.length) {
    return false;
  }
  return containsAll(arr, arr2);
}

export function equals(arr: ArrayLikeType<any>, arr2: ArrayLikeType<any>): boolean {
  // noinspection DuplicatedCode
  if (arr === arr2) {
    return true;
  }
  if ((!arr || arr.length === 0) && (!arr2 || arr2.length === 0)) {
    return true;
  }
  if (!arr || !arr2) {
    return false;
  }
  if (arr.length !== arr2.length) {
    return false;
  }

  for (let i = 0; i < arr.length; i++) {
    if (arr[i] !== arr2[i]) {
      return false;
    }
  }
  return true;
}

export function greater(arr: [], arr2: []): boolean {
  let arrLength = 0,
    arr2Length = 0;
  if (arr) {
    arrLength = arr.length;
  }
  if (arr2) {
    arr2Length = arr2.length;
  }
  return arrLength > arr2Length;
}

export function eachSibling<T>(arr: ArrayLikeType<T>, element: T, func: (elem: T, index: number) => void) {
  if (!arr || !func) {
    return;
  }
  for (let i = 0; i < arr.length; i++) {
    let elementAtI = arr[i];
    if (elementAtI !== element) {
      func(elementAtI, i);
    }
  }
}

/**
 * Alternative implementation of Array.findIndex(callback [, thisArg]), which is supported by most browsers.
 * See Array.findIndex for a detailed description.
 *
 * @param optional "this" binding for predicate function
 */
export function findIndex<T>(arr: ArrayLikeType<T>, predicate: (arg0: T, index: number, arr: T[]) => boolean, thisArg?: any): number {
  if (!arr || !predicate) {
    return -1;
  }
  for (let i = 0; i < arr.length; i++) {
    if (predicate.call(thisArg, arr[i], i, arr)) {
      return i;
    }
  }
  return -1;
}

/**
 *
 * @param optional "this" binding for predicate function
 */
export function find<T>(arr: ArrayLikeType<T>, predicate: Predicate<T>, thisArg?: any): T {
  let index = findIndex(arr, predicate, thisArg);
  if (index === -1) {
    return null;
  }
  return arr[index];
}

export function findFrom<T>(arr: ArrayLikeType<T>, startIndex: number, predicate: Predicate<T>, reverse?: boolean): T {
  if (reverse) {
    return findFromReverse(arr, startIndex, predicate);
  }
  return findFromForward(arr, startIndex, predicate);
}

export function findIndexFrom<T>(arr: ArrayLikeType<T>, startIndex: number, predicate: Predicate<T>, reverse?: boolean): number {
  if (reverse) {
    return findIndexFromReverse(arr, startIndex, predicate);
  }
  return findIndexFromForward(arr, startIndex, predicate);
}

export function findFromForward<T>(arr: ArrayLikeType<T>, startIndex: number, predicate: Predicate<T>): T {
  let index = findIndexFromForward(arr, startIndex, predicate);
  if (index === -1) {
    return null;
  }
  return arr[index];
}

export function findIndexFromForward<T>(arr: ArrayLikeType<T>, startIndex: number, predicate: (elem: T, index: number) => boolean): number {
  if (!arr || !predicate || startIndex >= arr.length) {
    return -1;
  }
  if (startIndex < 0) {
    startIndex = 0;
  }
  for (let i = startIndex; i < arr.length; i++) {
    let element = arr[i];
    if (predicate(element, i)) {
      return i;
    }
  }
  return -1;
}

export function findFromReverse<T>(arr: ArrayLikeType<T>, startIndex: number, predicate: Predicate<T>): T {
  let index = findIndexFromReverse(arr, startIndex, predicate);
  if (index === -1) {
    return null;
  }
  return arr[index];
}

export function findIndexFromReverse<T>(arr: ArrayLikeType<T>, startIndex: number, predicate: (elem: T, index: number) => boolean): number {
  if (!arr || !predicate || startIndex < 0) {
    return -1;
  }
  if (startIndex >= arr.length) {
    startIndex = arr.length - 1;
  }
  for (let i = startIndex; i >= 0; i--) {
    let element = arr[i];
    if (predicate(element, i)) {
      return i;
    }
  }
  return -1;
}

/**
 * Pushes all elements to the given array that are not null or undefined.
 */
export function pushIfDefined<T>(arr: T[], ...elements: T[]) {
  if (arr) {
    pushAll(arr, elements.filter(element => element !== null && element !== undefined));
  }
}

/**
 * Pushes the given element if it does not already exist in the array and the element is truthy. Thus the array is like a Set where every element
 * can only be added once to the collection. Note: the comparison is done with the === operator.
 */
export function pushSet<T>(arr: T[], element: T) {
  if (element && arr.indexOf(element) === -1) {
    arr.push(element);
  }
}

/**
 * Creates a string containing all elements in the array separated by the given delimiter.
 * @param encodeHtml true to encode the elements, false if not. Default is false
 */
export function format(arr: ArrayLikeType<string>, delimiter?: string, encodeHtml?: boolean) {
  if (!arr || arr.length === 0) {
    return '';
  }

  let output = '';
  for (let i = 0; i < arr.length; i++) {
    let element = arr[i];
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

export function formatEncoded(arr: ArrayLikeType<string>, delimiter?: string) {
  return format(arr, delimiter, true);
}

export function max(arr: number[]): number {
  if (arr === null || arr === undefined) {
    return Math.max(arr as null | undefined);
  }

  // Math.max() returns 0 (not null!) if arr contains only null and negative elements.
  let filtered = arr.filter(objects.isNumber);
  return Math.max(...filtered);
}

export function min(arr: number[]): number {
  if (arr === null || arr === undefined) {
    return Math.min(arr as null | undefined);
  }

  // Math.min() returns 0 (not null!) if arr contains only null and non-negative elements.
  let filtered = arr.filter(objects.isNumber);
  return Math.min(...filtered);
}

/**
 * @returns all elements of the first array which are not in the second array
 */
export function diff<T>(arr1: T[], arr2: T[]): T[] {
  let diff = arr1.slice();
  removeAll(diff, arr2);
  return diff;
}

export function flatMap<T, R>(arr: T[], func: (T) => R = (x => x)): R[] {
  let result = [];
  ensure(arr).forEach(element => {
    pushAll(result, func(element));
  });
  return result;
}

/**
 * Returns a flat array of all elements and their recursive child elements.
 *
 * @param arr The top-level list of all elements
 * @param childrenAccessor Function than extracts a list of child elements from a given element. Used to traverse the object structure.
 */
export function flattenRec<T>(arr: T[], childrenAccessor: (T) => T[]): T[] {
  return ensure(arr).reduce((acc, cur) => {
    acc.push(cur);
    if (cur && childrenAccessor) {
      acc = acc.concat(flattenRec(childrenAccessor(cur), childrenAccessor));
    }
    return acc;
  }, []);
}

/**
 * Replacement for indexOf() that works for arrays of jQuery objects (compares DOM nodes).
 */
export function $indexOf(arr: JQuery[], $element: JQuery): number {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i][0] === $element[0]) {
      return i;
    }
  }
}

/**
 * Replacement for remove() that works for arrays of jQuery objects (compares DOM nodes).
 */
export function $remove(arr: JQuery[], $element: JQuery) {
  let index = $indexOf(arr, $element);
  if (index >= 0) {
    arr.splice(index, 1);
  }
}

export function randomElement<T>(array: T[]): T {
  if (!array) {
    return undefined;
  }
  if (!Array.isArray(array)) {
    return array;
  }
  if (!array.length) {
    return undefined;
  }
  return array[Math.floor(Math.random() * array.length)];
}

/**
 * Converts the given array to a map. For each element, key and value is determined by the given functions.
 * If no function is provided, the element itself is used.
 *
 * @param array array of elements
 * @param keyMapper function that maps each element to the target key
 * @param valueExtractor function that maps each element to the target value
 */
export function toMap<T>(array: T[], keyMapper: (el: T) => string = (el => el + ''), valueMapper: (el: T) => any = (el => el)): any {
  return objects.createMap(ensure(array).reduce((map, element) => {
    map[keyMapper(element)] = valueMapper(element);
    return map;
  }, {}));
}

/**
 * If the argument is an empty array, null is returned. Otherwise, the argument is returned unchanged.
 */
export function nullIfEmpty<T>(array: T[]): T[] {
  return empty(array) ? null : array;
}

/**
 * Clears the content of an array <i>in-place</i>. All elements are removed from the array and the
 * length will be set to 0. If the given argument is not an array, nothing happens.
 *
 * This is a more readable version of `array.splice(0, a.length)`.
 *
 * The return value is an array of all deleted elements (never null).
 */
export function clear<T>(array: T[]): T[] {
  if (Array.isArray(array)) {
    return array.splice(0, array.length);
  }
  return [];
}

export default {
  $indexOf,
  $remove,
  clear,
  contains,
  containsAll,
  containsAny,
  diff,
  eachSibling,
  empty,
  ensure,
  equals,
  equalsIgnoreOrder,
  find,
  findFrom,
  findFromForward,
  findFromReverse,
  findIndex,
  findIndexFrom,
  findIndexFromForward,
  findIndexFromReverse,
  first,
  flatMap,
  flattenRec,
  format,
  formatEncoded,
  greater,
  hasElements,
  init,
  insert,
  insertAll,
  insertSorted,
  insertBefore,
  insertAfter,
  last,
  length,
  max,
  min,
  move,
  nullIfEmpty,
  pushAll,
  pushIfDefined,
  pushSet,
  remove,
  removeAll,
  replace,
  toMap,
  union,
  randomElement
};
