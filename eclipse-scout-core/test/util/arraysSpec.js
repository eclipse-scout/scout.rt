/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays} from '../../src/index';

describe('scout.arrays', function() {

  describe('init', function() {

    it('checks whether array has correct length and initial values', function() {
      var i, arr = arrays.init(5, 'foo');
      expect(arr.length).toBe(5);
      for (i = 0; i < arr.length; i++) {
        expect(arr[i]).toBe('foo');
      }
    });

  });

  describe('ensure', function() {

    it('creates an array if the param is not an array', function() {
      expect(arrays.ensure()).toEqual([]);
      expect(arrays.ensure(undefined)).toEqual([]);
      expect(arrays.ensure(null)).toEqual([]);
      expect(arrays.ensure('')).toEqual(['']);
      expect(arrays.ensure(true)).toEqual([true]);
      expect(arrays.ensure(false)).toEqual([false]);
    });

    it('returns the param if the param already is an array', function() {
      var arr = [];
      expect(arrays.ensure(arr)).toBe(arr);
      arr = [0, 1];
      expect(arrays.ensure(arr)).toBe(arr);
    });
  });

  describe('remove', function() {

    it('removes elements', function() {
      // Ensure empty arguments are supported
      expect(arrays.remove()).toBe(false);
      expect(arrays.remove([])).toBe(false);
      expect(arrays.remove([], 'x')).toBe(false);

      var arr = ['a', 'b', 'c', 'a', 'd']; // 'a' is two times in the list

      expect(arrays.remove(arr, 'y')).toBe(false);
      expect(arr).toEqual(['a', 'b', 'c', 'a', 'd']);
      expect(arrays.remove(arr, 'b')).toBe(true);
      expect(arr).toEqual(['a', 'c', 'a', 'd']);
      expect(arrays.remove(arr, 'a')).toBe(true);
      expect(arr).toEqual(['c', 'a', 'd']);
      expect(arrays.remove(arr, 'a')).toBe(true);
      expect(arr).toEqual(['c', 'd']);
      expect(arrays.remove(arr, 'a')).toBe(false);
      expect(arr).toEqual(['c', 'd']);

      arr = ['a', 'b', undefined, 'c', undefined, 'd'];
      expect(arrays.remove(arr, 'a')).toBe(true);
      expect(arr).toEqual(['b', undefined, 'c', undefined, 'd']);
      expect(arrays.remove(arr)).toBe(true);
      expect(arr).toEqual(['b', 'c', undefined, 'd']);
    });

  });

  describe('removeAll', function() {

    it('removes all given elements', function() {
      var arr = ['a', 'b', 'c', 'a', 'd']; // 'a' is two times in the list

      expect(arrays.removeAll(arr, ['y'])).toBe(false);
      expect(arr).toEqual(['a', 'b', 'c', 'a', 'd']);
      expect(arrays.removeAll(arr, ['b'])).toBe(true);
      expect(arr).toEqual(['a', 'c', 'a', 'd']);
      expect(arrays.removeAll(arr, ['a'])).toBe(true);
      expect(arr).toEqual(['c', 'd']);

      arr = ['a', 'b', 'c', 'a', 'd'];
      expect(arrays.removeAll(arr, ['a', 'd'])).toBe(true);
      expect(arr).toEqual(['b', 'c']);

      arr = ['a', 'b', 'c', 'a', 'd'];
      expect(arrays.removeAll(arr, ['a', 'b', 'c', 'd'])).toBe(true);
      expect(arr).toEqual([]);
    });

    it('considers emtpy args', function() {
      expect(arrays.removeAll()).toBe(false);
      expect(arrays.removeAll([])).toBe(false);
      expect(arrays.removeAll([], [])).toBe(false);
      expect(arrays.removeAll([], ['x'])).toBe(false);
    });

  });

  describe('replace', function() {

    it('replaces elements', function() {
      var arr = ['a', 'b', 'c', 'd'];

      arrays.replace(arr, 'c', 'x');
      expect(arr).toEqual(['a', 'b', 'x', 'd']);
      arrays.replace(arr, 'e', 'y');
      expect(arr).toEqual(['a', 'b', 'x', 'd']);
    });

  });

  describe('insert', function() {

    it('insert element at index', function() {
      var arr = ['a', 'b', 'c', 'd'];
      arrays.insert(arr, 'e', 0);
      expect(arr).toEqual(['e', 'a', 'b', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      arrays.insert(arr, 'e', 1);
      expect(arr).toEqual(['a', 'e', 'b', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      arrays.insert(arr, 'e', 10);
      expect(arr).toEqual(['a', 'b', 'c', 'd', 'e']);
    });

  });

  describe('insertAll', function() {

    it('insert element array at index', function() {
      var arr = ['a', 'b', 'c', 'd'];
      arrays.insertAll(arr, ['e', 'f', 'g'], 0);
      expect(arr).toEqual(['e', 'f', 'g', 'a', 'b', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      arrays.insertAll(arr, ['e', 'f', 'g'], 1);
      expect(arr).toEqual(['a', 'e', 'f', 'g', 'b', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      arrays.insertAll(arr, ['e', 'f', 'g'], 10);
      expect(arr).toEqual(['a', 'b', 'c', 'd', 'e', 'f', 'g']);
    });

  });

  describe('insertSorted', function() {

    it('inserts element at correct position', function() {
      var compareOrdered = function(a, b) {
        return a.order < b.order ? -1 : (a.order > b.order ? 1 : 0);
      };
      var flat = function(arr) {
        return arr.map(function(el) {
          return el.text;
        }).join(', ');
      };

      // Without duplicates
      var arr = [];
      arrays.insertSorted(arr, {order: 10, text: 'A'}, compareOrdered);
      arrays.insertSorted(arr, {order: 20, text: 'B'}, compareOrdered);
      arrays.insertSorted(arr, {order: 5, text: 'C'}, compareOrdered);
      expect(flat(arr)).toBe('C, A, B');

      // With duplicates
      arrays.insertSorted(arr, {order: 10, text: 'D'}, compareOrdered);
      expect(flat(arr)).toBe('C, A, D, B');
      arrays.insertSorted(arr, {order: 10, text: 'E'}, compareOrdered);
      expect(flat(arr)).toBe('C, A, D, E, B');
      arrays.insertSorted(arr, {order: 5, text: 'F'}, compareOrdered);
      expect(flat(arr)).toBe('C, F, A, D, E, B');
      arrays.insertSorted(arr, {order: 5, text: 'G'}, compareOrdered);
      expect(flat(arr)).toBe('C, F, G, A, D, E, B');
      arrays.insertSorted(arr, {order: 5, text: 'H'}, compareOrdered);
      expect(flat(arr)).toBe('C, F, G, H, A, D, E, B');
      arrays.insertSorted(arr, {order: 5, text: 'I'}, compareOrdered);
      expect(flat(arr)).toBe('C, F, G, H, I, A, D, E, B');

      // Only duplicates
      arr = [];
      arrays.insertSorted(arr, {order: 11, text: 'X'}, compareOrdered);
      arrays.insertSorted(arr, {order: 11, text: 'Y'}, compareOrdered);
      arrays.insertSorted(arr, {order: 11, text: 'Z'}, compareOrdered);
      expect(flat(arr)).toBe('X, Y, Z');
    });

  });

  describe('max', function() {
    it('returns 0 iff input contains 0', function() {
      expect(arrays.max([null, 5])).toBe(5);
      expect(arrays.max([null, 0])).toBe(0);
      expect(arrays.max([null, -13])).toBe(-13);
    });

    it('behaves like Math.max on null and undefined', function() {
      var doTest = function(arr) {
        expect(arrays.max(arr)).toBe(Math.max.apply(Math, arr));
      };

      doTest(null);
      doTest(undefined);
    });

    it('ignores non-number elements', function() {
      expect(arrays.max([null, 5, 6.7, false, undefined, NaN, {}, 'batman'])).toBe(6.7);
    });
  });

  describe('min', function() {
    it('returns 0 iff input contains 0', function() {
      expect(arrays.min([null, 5])).toBe(5);
      expect(arrays.min([null, 0])).toBe(0);
      expect(arrays.min([null, -13])).toBe(-13);
    });

    it('behaves like Math.min on null and undefined', function() {
      var doTest = function(arr) {
        expect(arrays.min(arr)).toBe(Math.min.apply(Math, arr));
      };

      doTest(null);
      doTest(undefined);
    });

    it('ignores non-number elements', function() {
      expect(arrays.min([null, 5, 6.7, false, undefined, NaN, {}, 'batman'])).toBe(5);
    });
  });

  describe('move', function() {

    it('replaces elements', function() {
      var arr = ['a', 'b', 'c', 'd'];
      arrays.move(arr, 0, 1);
      expect(arr).toEqual(['b', 'a', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      arrays.move(arr, 1, 0);
      expect(arr).toEqual(['b', 'a', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      arrays.move(arr, 0, 100);
      expect(arr).toEqual(['b', 'c', 'd', 'a']);

      arr = ['a', 'b', 'c', 'd'];
      arrays.move(arr, 10, 0);
      expect(arr).toEqual([undefined, 'a', 'b', 'c', 'd']);
    });

  });

  describe('union', function() {

    it('merges two arrays', function() {
      var arr1 = ['a', 'b', 'c'];
      var arr2 = ['d', 'e', 'f'];

      expect(arrays.union(arr1, arr2)).toEqual(['a', 'b', 'c', 'd', 'e', 'f']);
    });

    it('merges two arrays and removes duplicates', function() {
      var arr1 = ['a', 'b', 'c'];
      var arr2 = ['d', 'a', 'e'];

      expect(arrays.union(arr1, arr2)).toEqual(['a', 'b', 'c', 'd', 'e']);
    });

    it('also works with floats', function() {
      var arr1 = [1.5, 2];
      var arr2 = [1.5, 30];

      expect(arrays.union(arr1, arr2)).toEqual([1.5, 2, 30]);
    });

    it('if the arrays contain objects instead of primitives, it uses their id to check for equality', function() {
      var obj1 = {
        id: '1'
      };
      var obj2 = {
        id: '2'
      };
      var obj3 = {
        id: '3'
      };
      var arr1 = [obj1, obj2];
      var arr2 = [obj2, obj3, obj1];

      expect(arrays.union(arr1, arr2)).toEqual([obj1, obj2, obj3]);
    });

    it('does not fail if arr1 or arr2 are not defined', function() {
      expect(arrays.union(null, ['d', 'e', 'f'])).toEqual(['d', 'e', 'f']);
      expect(arrays.union(['d', 'e', 'f'], null)).toEqual(['d', 'e', 'f']);
      expect(arrays.union(null, null)).toEqual([]);
    });
  });

  describe('equals', function() {

    it('checks whether two arrays contain the same elements in the same order', function() {
      var arr1 = ['a', 'b', 'c'];
      var arr2 = ['a', 'b', 'c'];
      expect(arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = ['a', 'b', 'c'];
      arr2 = ['b', 'a', 'b'];
      expect(arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = ['a', 'b', 'c'];
      arr2 = ['a', 'b'];
      expect(arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = ['a', 'b'];
      arr2 = ['a', 'b', 'c'];
      expect(arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = ['a'];
      expect(arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = ['b'];
      expect(arrays.equals(arr1, arr2)).toBeFalsy();
    });

    it('considers emtpy and same arrays', function() {
      var arr1 = [];
      var arr2 = ['b', 'a', 'b'];
      expect(arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = [];
      expect(arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = [];
      arr2 = [];
      expect(arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = arr1;
      expect(arrays.equals(arr1, arr2)).toBeTruthy();
    });

    it('returns true if one array is undefined/null and the other empty', function() {
      var arr1 = [];
      var arr2;
      expect(arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = undefined;
      expect(arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = null;
      arr2 = [];
      expect(arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = null;
      arr2 = null;
      expect(arrays.equals(arr1, arr2)).toBeTruthy();
    });

  });

  describe('equalsIgnoreOrder', function() {

    it('checks whether two arrays contain the same elements without considering the order', function() {
      var arr1 = ['a', 'b', 'c'];
      var arr2 = ['b', 'a', 'b'];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = ['a', 'b', 'c'];
      arr2 = ['a', 'b'];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = ['a', 'b'];
      arr2 = ['a', 'b', 'c'];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = ['a'];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = ['b'];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();
    });

    it('considers emtpy and same arrays', function() {
      var arr1 = [];
      var arr2 = ['b', 'a', 'b'];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = [];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = [];
      arr2 = [];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = arr1;
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();
    });

    it('returns true if one array is undefined/null and the other empty', function() {
      var arr1 = [];
      var arr2;
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = undefined;
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = null;
      arr2 = [];
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = null;
      arr2 = null;
      expect(arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();
    });

  });

  describe('findIndex', function() {

    it('returns the index of the element for which the given predicate returns true', function() {
      var arr = ['a', 'b', 'c', 'd'];

      expect(arrays.findIndex()).toBe(-1);
      expect(arrays.findIndex()).toBe(-1);

      var index = arrays.findIndex(arr, function(element) {
        return element === 'c';
      });
      expect(index).toBe(2);

      index = arrays.findIndex(arr, function(element) {
        return element === 'z';
      });
      expect(index).toBe(-1);
    });

  });

  describe('find', function() {

    it('returns the element for which the given predicate returns true', function() {
      var arr = ['a', 'b', 'c', 'd'];

      expect(arrays.find()).toBe(null);
      expect(arrays.find(arr)).toBe(null);

      var element = arrays.find(arr, function(element) {
        return element === 'c';
      });
      expect(element).toBe('c');

      element = arrays.find(arr, function(element) {
        return element === 'z';
      });
      expect(element).toBeFalsy();
    });

  });

  describe('findFrom', function() {

    it('returns the element for which the given predicate returns true, starting from a given index', function() {
      var arr = ['a', 'b', 'c', 'd'];

      expect(arrays.find()).toBe(null);
      expect(arrays.find(arr)).toBe(null);

      var element = arrays.findFrom(arr, 1, function(element) {
        return element === 'c';
      });
      expect(element).toBe('c');

      element = arrays.findFrom(arr, 1, function(element) {
        return element === 'z';
      });
      expect(element).toBeFalsy();
    });

    it('does not return the element for which the given predicate returns true, if it is on the left side of the start index', function() {
      var arr = ['a', 'b', 'c', 'd'];

      var element = arrays.findFrom(arr, 1, function(element) {
        return element === 'a';
      });
      expect(element).toBeFalsy();
    });

    it('also checks the element at start index ', function() {
      var arr = ['a', 'b', 'c', 'd'];

      var element = arrays.findFrom(arr, 1, function(element) {
        return element === 'b';
      });
      expect(element).toBeTruthy();

      // edge cases
      element = arrays.findFrom(arr, 0, function(element) {
        return element === 'a';
      });
      expect(element).toBeTruthy();

      element = arrays.findFrom(arr, 3, function(element) {
        return element === 'd';
      });
      expect(element).toBeTruthy();

      element = arrays.findFrom(arr, 3, function(element) {
        return element === 'z';
      });
      expect(element).toBeFalsy();
    });

    it('searches from right to left if backwards is true', function() {
      var arr = ['a', 'b', 'c', 'd'];

      var element = arrays.findFrom(arr, 2, function(element) {
        return element === 'a';
      }, true);
      expect(element).toBeTruthy();

      element = arrays.findFrom(arr, 2, function(element) {
        return element === 'd';
      }, true);
      expect(element).toBeFalsy();
    });

  });

  describe('format', function() {

    it('formats an array by concatenating each entry', function() {
      var arr = ['a', 'b', 'c', 'd'];
      expect(arrays.format(arr)).toBe('abcd');
      expect(arrays.format(arr, '_')).toBe('a_b_c_d');

      arr = ['abc', 'def'];
      expect(arrays.format(arr)).toBe('abcdef');
      expect(arrays.format(arr, '_')).toBe('abc_def');

      arr = ['abc'];
      expect(arrays.format(arr)).toBe('abc');
      expect(arrays.format(arr, '_')).toBe('abc');
    });

    it('returns \'\' for empty array or if no array was provided', function() {
      var arr = [];
      expect(arrays.format(arr)).toBe('');
      expect(arrays.format()).toBe('');
    });

  });

  describe('formatEncoded', function() {

    it('encodes the html of each array element', function() {
      var arr = ['<span>a</span>', 'b', 'c<p>', '<script>d</script>'];
      expect(arrays.formatEncoded(arr, '<br>')).toBe('&lt;span&gt;a&lt;/span&gt;<br>b<br>c&lt;p&gt;<br>&lt;script&gt;d&lt;/script&gt;');

      arr = ['abc', '<script>def'];
      expect(arrays.formatEncoded(arr)).toBe('abc&lt;script&gt;def');
      expect(arrays.formatEncoded(arr, '<br>')).toBe('abc<br>&lt;script&gt;def');

      arr = ['<p>abc'];
      expect(arrays.formatEncoded(arr)).toBe('&lt;p&gt;abc');
      expect(arrays.formatEncoded(arr, '<br>')).toBe('&lt;p&gt;abc');
    });

  });

  describe('first', function() {

    it('finds first array element', function() {
      expect(arrays.first()).toBe(undefined);
      expect(arrays.first('test')).toBe('test');
      expect(arrays.first({
        x: 'y'
      })).toEqual({
        x: 'y'
      });
      expect(arrays.first([])).toBe(undefined);
      expect(arrays.first([undefined])).toBe(undefined);
      expect(arrays.first(['a', 'b', 'c'])).toBe('a');
      expect(arrays.first([null, undefined, 123])).toBe(null);
      expect(arrays.first(['', 'b', 'c'])).toBe('');
    });

  });

  describe('last', function() {

    it('finds last array element', function() {
      expect(arrays.last()).toBe(undefined);
      expect(arrays.last('test')).toBe('test');
      expect(arrays.last({
        x: 'y'
      })).toEqual({
        x: 'y'
      });
      expect(arrays.last([])).toBe(undefined);
      expect(arrays.last([undefined])).toBe(undefined);
      expect(arrays.last(['a', 'b', 'c'])).toBe('c');
      expect(arrays.last([123, undefined, null])).toBe(null);
      expect(arrays.last(['a', 'b', ''])).toBe('');
    });

  });

  describe('pushIfDefined', function() {

    it('pushes element only if it is defined', function() {
      var arr = null;

      // expect no errors:
      arrays.pushIfDefined();
      arrays.pushIfDefined(arr);
      arrays.pushIfDefined(arr, 'element');

      arr = [];
      arrays.pushIfDefined(arr);
      expect(arr).toEqual([]);
      arrays.pushIfDefined(arr, undefined);
      expect(arr).toEqual([]);
      arrays.pushIfDefined(arr, null);
      expect(arr).toEqual([]);
      arrays.pushIfDefined(arr, 'element');
      expect(arr).toEqual(['element']);
      arrays.pushIfDefined(arr, 0);
      expect(arr).toEqual(['element', 0]);
      arrays.pushIfDefined(arr, false);
      arrays.pushIfDefined(arr, true);
      expect(arr).toEqual(['element', 0, false, true]);

      arr = [];
      arrays.pushIfDefined(arr, null, 1, undefined, 2, 3, null, null, '');
      expect(arr).toEqual([1, 2, 3, '']);

      arr = [];
      arrays.pushIfDefined(arr, []);
      expect(arr).toEqual([[]]);
      arrays.pushIfDefined(arr, [1, 2]);
      expect(arr).toEqual([[], [1, 2]]);
    });

  });

  describe('pushSet', function() {

    it('only pushes the element if it is truthy and does not already exist in the array', function() {
      var arr = [1, 2, 3];
      arrays.pushSet(arr, 1);
      expect(arr).toEqual([1, 2, 3]);

      arr = [1, 2, 3];
      arrays.pushSet(arr, null);
      expect(arr).toEqual([1, 2, 3]);

      arr = [1, 2, 3];
      arrays.pushSet(arr, 4);
      expect(arr).toEqual([1, 2, 3, 4]);
    });

  });

  describe('diff', function() {

    it('returns all elements of the first array which are not in the second array', function() {
      var arr1 = ['a', 'b', 'b1', 'c'];
      var arr2 = ['b', 'c', 'c1', 'd'];

      expect(arrays.diff(arr1, arr2)).toEqual(['a', 'b1']);
      expect(arrays.diff(arr2, arr1)).toEqual(['c1', 'd']);
    });

  });

  describe('flatMap', function() {

    it('returns flat list of all merged array elements', function() {
      var arr = [
        'a b c',
        '1 2',
        'x'
      ];
      var result = arrays.flatMap(arr, function(text) {
        return text.split(' ');
      });
      expect(['a', 'b', 'c', '1', '2', 'x']).toEqual(result);
    });

  });

});
