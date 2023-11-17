/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {objects, strings} from '../index';

export const arrays = {
  /**
   * Ensures the given parameter is an array.
   */
  ensure<T>(array: T[] | T): T[] {
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
  init<T>(length: number, initValue: T): T[] {
    let array = [];
    for (let i = 0; i < length; i++) {
      array[i] = initValue;
    }
    return array;
  },

  /**
   * Removes the first occurrence of the specified element from the array.
   * If the array does not contain the element, it stays unchanged.
   *
   * @returns true if an element was removed
   */
  remove<T>(arr: T[], element: T): boolean {
    if (arr) {
      let index = arr.indexOf(element);
      if (index !== -1) {
        arr.splice(index, 1);
        return true;
      }
    }
    return false;
  },

  /**
   * Removes the first array element that matches the given predicate.
   * If no element matches the given predicate, the array stays unchanged.
   *
   * @returns true if an element was removed
   */
  removeByPredicate<T>(arr: T[], predicate: (elem: T, index: number, arr: T[]) => boolean, thisArg?: any): boolean {
    let index = arrays.findIndex(arr, predicate, thisArg);
    if (index !== -1) {
      arr.splice(index, 1);
      return true;
    }
    return false;
  },

  /**
   * Removes every given element from the array
   *
   * @returns true if the array contained at least one of the specified elements
   */
  removeAll<T>(arr: T[], elements: T[]): boolean {
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
  },

  /**
   * @returns the index of the replaced element
   */
  replace<T>(arr: T[], element: T, replacement: T): number {
    let index = arr.indexOf(element);
    if (index !== -1) {
      arr[index] = replacement;
    }
    return index;
  },

  /**
   * Inserts the given element at the specified index.
   * <p>
   * This function uses {@link insertAll} which relies on Array.prototype.splice(). Check its js-doc for details.
   */
  insert<T>(arr: T[], element: T, index: number) {
    arrays.insertAll(arr, [element], index);
  },

  /**
   * Inserts all elements of the given array at the specified index.
   *
   * This function is based on Array.prototype.splice().
   * Thus, if the 'index' is greater than the length of the array, 'elements' will be added to the end of the array 'arr'.
   * This may cause unexpected behavior on accessing arr[index] after insertion.
   *
   * The caller must ensure the size of the array.
   */
  insertAll<T>(arr: T[], elements: T | T[], index: number) {
    let elementsArr = arrays.ensure(elements);
    arr.splice(index, 0, ...elementsArr);
  },

  /**
   * Inserts the given element into the array according to the sort order indicated by the given comparison function.
   *
   * All arguments are mandatory.
   */
  insertSorted<T>(arr: T[], element: T, compareFunc: (a: T, b: T) => number) {
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
  },

  /**
   * Inserts to given element into the array directly BEFORE the first array element that matches the given predicate.
   * If no such element can be found, the new element is inserted at the BEGINNING of the array.
   *
   * @param thisArg optional "this" binding for predicate function
   */
  insertBefore<T>(arr: T[], elementToInsert: T, predicate: (elem: T, index: number, arr: T[]) => boolean, thisArg?: any) {
    let index = arrays.findIndex(arr, predicate, thisArg);
    if (index === -1) {
      arr.unshift(elementToInsert);
    } else {
      arrays.insert(arr, elementToInsert, index);
    }
  },

  /**
   * Inserts to given element into the array directly AFTER the first array element that matches the given predicate.
   * If no such element can be found, the new element is inserted at the END of the array.
   */
  insertAfter<T>(arr: T[], elementToInsert: T, predicate: (elem: T, index: number, arr: T[]) => boolean) {
    let index = arrays.findIndex(arr, predicate);
    if (index === -1) {
      arr.push(elementToInsert);
    } else {
      arrays.insert(arr, elementToInsert, index + 1);
    }
  },

  /**
   * This function uses {@link insert} which relies on Array.prototype.splice(). Check its js-doc for details.
   */
  move<T>(arr: T[], fromIndex: number, toIndex: number) {
    let element = arr.splice(fromIndex, 1)[0];
    arrays.insert(arr, element, toIndex);
  },

  contains<T>(haystack: T[], needle: T): boolean {
    haystack = arrays.ensure(haystack);
    return haystack.indexOf(needle) !== -1;
  },

  containsAny<T>(haystack: T[] | T, needles: T[] | T): boolean {
    let haystackArr = arrays.ensure(haystack);
    let needlesArr = arrays.ensure(needles);
    return needlesArr.some(element => {
      return haystackArr.indexOf(element) >= 0;
    });
  },

  containsAll<T>(haystack: T[] | T, needles: T[] | T): boolean {
    let haystackArr = arrays.ensure(haystack);
    let needlesArr = arrays.ensure(needles);
    return needlesArr.every(element => haystackArr.indexOf(element) >= 0);
  },

  first<T>(arr: T[]): T {
    if (Array.isArray(arr)) {
      return arr[0];
    }
    return arr;
  },

  last<T>(arr: T[]): T {
    if (Array.isArray(arr)) {
      return arr[arr.length - 1];
    }
    return arr;
  },

  /**
   * @returns true if the given argument is an array and has a length > 0, false in any other case.
   */
  hasElements<T>(arr: T[] | T): boolean {
    return !arrays.empty(arr);
  },

  /**
   * @returns true if the given argument is not an array or the length of the array is 0, false in any other case.
   */
  empty<T>(arr: T[] | T): boolean {
    if (Array.isArray(arr)) {
      return arr.length === 0;
    }
    return true;
  },

  /**
   * @returns the size of the array, or 0 if the argument is not an array
   */
  length<T>(arr: T[] | T): number {
    if (Array.isArray(arr)) {
      return arr.length;
    }
    return 0;
  },

  pushAll<T>(arr: T[], arr2: T | T[]) {
    let a = arrays.ensure(arr2);
    arr.push(...a);
  },

  /**
   * Merges the two given arrays and removes duplicate entries in O(n).
   * If the arrays contain objects instead of primitives, it uses their id to check for equality.
   */
  union<T extends number | string | { id: string }>(array1: T[], array2: T[]): T[] {
    let result = [];
    let map = {};

    array1 = arrays.ensure(array1);
    array2 = arrays.ensure(array2);

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
  },

  equalsIgnoreOrder(arr: any[], arr2: any[]): boolean {
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
    return arrays.containsAll(arr, arr2);
  },

  equals(arr: ArrayLike<any>, arr2: ArrayLike<any>): boolean {
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
  },

  greater(arr: [], arr2: []): boolean {
    let arrLength = 0,
      arr2Length = 0;
    if (arr) {
      arrLength = arr.length;
    }
    if (arr2) {
      arr2Length = arr2.length;
    }
    return arrLength > arr2Length;
  },

  eachSibling<T>(arr: ArrayLike<T>, element: T, func: (elem: T, index: number) => void) {
    if (!arr || !func) {
      return;
    }
    for (let i = 0; i < arr.length; i++) {
      let elementAtI = arr[i];
      if (elementAtI !== element) {
        func(elementAtI, i);
      }
    }
  },

  /**
   * Alternative implementation of Array.findIndex(callback [, thisArg]), which is supported by most browsers.
   * See Array.findIndex for a detailed description.
   *
   * @param optional "this" binding for predicate function
   */
  findIndex<T>(arr: ArrayLike<T>, predicate: (elem: T, index: number, arr: T[]) => boolean, thisArg?: any): number {
    if (!arr || !predicate) {
      return -1;
    }
    for (let i = 0; i < arr.length; i++) {
      if (predicate.call(thisArg, arr[i], i, arr)) {
        return i;
      }
    }
    return -1;
  },

  /**
   *
   * @param thisArg optional "this" binding for predicate function
   */
  find<T>(arr: ArrayLike<T>, predicate: (elem: T, index: number, arr: T[]) => boolean, thisArg?: any): T {
    let index = arrays.findIndex(arr, predicate, thisArg);
    if (index === -1) {
      return null;
    }
    return arr[index];
  },

  findFrom<T>(arr: ArrayLike<T>, startIndex: number, predicate: (elem: T, index: number) => boolean, reverse?: boolean): T {
    if (reverse) {
      return arrays.findFromReverse(arr, startIndex, predicate);
    }
    return arrays.findFromForward(arr, startIndex, predicate);
  },

  findIndexFrom<T>(arr: ArrayLike<T>, startIndex: number, predicate: (elem: T, index: number) => boolean, reverse?: boolean): number {
    if (reverse) {
      return arrays.findIndexFromReverse(arr, startIndex, predicate);
    }
    return arrays.findIndexFromForward(arr, startIndex, predicate);
  },

  findFromForward<T>(arr: ArrayLike<T>, startIndex: number, predicate: (elem: T, index: number) => boolean): T {
    let index = arrays.findIndexFromForward(arr, startIndex, predicate);
    if (index === -1) {
      return null;
    }
    return arr[index];
  },

  findIndexFromForward<T>(arr: ArrayLike<T>, startIndex: number, predicate: (elem: T, index: number) => boolean): number {
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
  },

  findFromReverse<T>(arr: ArrayLike<T>, startIndex: number, predicate: (elem: T, index: number) => boolean): T {
    let index = arrays.findIndexFromReverse(arr, startIndex, predicate);
    if (index === -1) {
      return null;
    }
    return arr[index];
  },

  findIndexFromReverse<T>(arr: ArrayLike<T>, startIndex: number, predicate: (elem: T, index: number) => boolean): number {
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
  },

  /**
   * Pushes all elements to the given array that are not null or undefined.
   */
  pushIfDefined<T>(arr: T[], ...elements: T[]) {
    if (arr) {
      arrays.pushAll(arr, elements.filter(element => element !== null && element !== undefined));
    }
  },

  /**
   * Pushes the given element if it does not already exist in the array and the element is truthy. Thus, the array is like a Set where every element
   * can only be added once to the collection. Note: the comparison is done with the === operator.
   */
  pushSet<T>(arr: T[], element: T) {
    if (element && arr.indexOf(element) === -1) {
      arr.push(element);
    }
  },

  /**
   * Creates a string containing all elements in the array separated by the given delimiter.
   * @param encodeHtml true to encode the elements, false if not. Default is false
   */
  format(arr: ArrayLike<string>, delimiter?: string, encodeHtml?: boolean): string {
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
  },

  formatEncoded(arr: ArrayLike<string>, delimiter?: string): string {
    return arrays.format(arr, delimiter, true);
  },

  max(arr: number[]): number {
    if (arr === null || arr === undefined) {
      return Math.max(arr as null | undefined);
    }

    // Math.max() returns 0 (not null!) if arr contains only null and negative elements.
    let filtered = arr.filter(objects.isNumber);
    return Math.max(...filtered);
  },

  min(arr: number[]): number {
    if (arr === null || arr === undefined) {
      return Math.min(arr as null | undefined);
    }

    // Math.min() returns 0 (not null!) if arr contains only null and non-negative elements.
    let filtered = arr.filter(objects.isNumber);
    return Math.min(...filtered);
  },

  /**
   * @returns all elements of the first array which are not in the second array
   */
  diff<T>(arr1: T[], arr2: T[]): T[] {
    let diff = arr1.slice();
    arrays.removeAll(diff, arr2);
    return diff;
  },

  flatMap<T, R>(arr: T[] | T, func: (T) => R | R[] = (x => x)): R[] {
    let result = [];
    arrays.ensure(arr).forEach(element => arrays.pushAll(result, func(element)));
    return result;
  },

  /**
   * Returns a flat array of all elements and their recursive child elements.
   *
   * @param arr The top-level list of all elements
   * @param childrenAccessor Function than extracts a list of child elements from a given element. Used to traverse the object structure.
   */
  flattenRec<T>(arr: T[], childrenAccessor: (T) => T[]): T[] {
    return arrays.ensure(arr).reduce((acc, cur) => {
      acc.push(cur);
      if (cur && childrenAccessor) {
        acc = acc.concat(arrays.flattenRec(childrenAccessor(cur), childrenAccessor));
      }
      return acc;
    }, []);
  },

  /**
   * Replacement for indexOf() that works for arrays of jQuery objects (compares DOM nodes).
   */
  $indexOf(arr: JQuery[], $element: JQuery): number {
    for (let i = 0; i < arr.length; i++) {
      if (arr[i][0] === $element[0]) {
        return i;
      }
    }
  },

  /**
   * Replacement for remove() that works for arrays of jQuery objects (compares DOM nodes).
   */
  $remove(arr: JQuery[], $element: JQuery) {
    let index = arrays.$indexOf(arr, $element);
    if (index >= 0) {
      arr.splice(index, 1);
    }
  },

  randomElement<T>(array: T[]): T {
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
  },

  /**
   * Converts the given array to a map. For each element, key and value is determined by the given functions.
   * If no function is provided, the element itself is used.
   *
   * @param array array of elements
   * @param keyMapper function that maps each element to the target key
   * @param valueExtractor function that maps each element to the target value
   */
  toMap<T>(array: T[], keyMapper: (el: T) => PropertyKey = (el => el + ''), valueMapper: (el: T) => any = (el => el)): any {
    return objects.createMap(arrays.ensure(array).reduce((map, element) => {
      map[keyMapper(element)] = valueMapper(element);
      return map;
    }, {}));
  },

  /**
   * If the argument is an empty array, null is returned. Otherwise, the argument is returned unchanged.
   */
  nullIfEmpty<T>(array: T[]): T[] {
    return arrays.empty(array) ? null : array;
  },

  /**
   * Clears the content of an array <i>in-place</i>. All elements are removed from the array and the
   * length will be set to 0. If the given argument is not an array, nothing happens.
   *
   * This is a more readable version of `array.splice(0, a.length)`.
   *
   * The return value is an array of all deleted elements (never null).
   */
  clear<T>(array: T[]): T[] {
    if (Array.isArray(array)) {
      return array.splice(0, array.length);
    }
    return [];
  },

  swap<T>(array: T[], element1: T, element2: T) {
    let index1 = array.indexOf(element1);
    let index2 = array.indexOf(element2);
    array[index1] = element2;
    array[index2] = element1;
  }
};
