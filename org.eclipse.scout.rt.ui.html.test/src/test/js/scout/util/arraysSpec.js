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
describe("scout.arrays", function() {

  describe("init", function() {

    it("checks whether array has correct length and initial values", function() {
      var i, arr = scout.arrays.init(5, 'foo');
      expect(arr.length).toBe(5);
      for (i = 0; i < arr.length; i++) {
        expect(arr[i]).toBe('foo');
      }
    });

  });

  describe("ensure", function() {

    it("creates an array if the param is not an array", function() {
      expect(scout.arrays.ensure()).toEqual([]);
      expect(scout.arrays.ensure(undefined)).toEqual([]);
      expect(scout.arrays.ensure(null)).toEqual([]);
      expect(scout.arrays.ensure('')).toEqual(['']);
      expect(scout.arrays.ensure(true)).toEqual([true]);
      expect(scout.arrays.ensure(false)).toEqual([false]);
    });

    it("returns the param if the param already is an array", function() {
      var arr = [];
      expect(scout.arrays.ensure(arr)).toBe(arr);
      arr = [0, 1];
      expect(scout.arrays.ensure(arr)).toBe(arr);
    });
  });

  describe("remove", function() {

    it("removes elements", function() {
      // Ensure empty arguments are supported
      expect(scout.arrays.remove()).toBe(false);
      expect(scout.arrays.remove([])).toBe(false);
      expect(scout.arrays.remove([], 'x')).toBe(false);

      var arr = ['a', 'b', 'c', 'a', 'd']; // 'a' is two times in the list

      expect(scout.arrays.remove(arr, 'y')).toBe(false);
      expect(arr).toEqual(['a', 'b', 'c', 'a', 'd']);
      expect(scout.arrays.remove(arr, 'b')).toBe(true);
      expect(arr).toEqual(['a', 'c', 'a', 'd']);
      expect(scout.arrays.remove(arr, 'a')).toBe(true);
      expect(arr).toEqual(['c', 'a', 'd']);
      expect(scout.arrays.remove(arr, 'a')).toBe(true);
      expect(arr).toEqual(['c', 'd']);
      expect(scout.arrays.remove(arr, 'a')).toBe(false);
      expect(arr).toEqual(['c', 'd']);

      arr = ['a', 'b', undefined, 'c', undefined, 'd'];
      expect(scout.arrays.remove(arr, 'a')).toBe(true);
      expect(arr).toEqual(['b', undefined, 'c', undefined, 'd']);
      expect(scout.arrays.remove(arr)).toBe(true);
      expect(arr).toEqual(['b', 'c', undefined, 'd']);
    });

  });

  describe("removeAll", function() {

    it("removes all given elements", function() {
      var arr = ['a', 'b', 'c', 'a', 'd']; // 'a' is two times in the list

      expect(scout.arrays.removeAll(arr, ['y'])).toBe(false);
      expect(arr).toEqual(['a', 'b', 'c', 'a', 'd']);
      expect(scout.arrays.removeAll(arr, ['b'])).toBe(true);
      expect(arr).toEqual(['a', 'c', 'a', 'd']);
      expect(scout.arrays.removeAll(arr, ['a'])).toBe(true);
      expect(arr).toEqual(['c', 'd']);

      arr = ['a', 'b', 'c', 'a', 'd'];
      expect(scout.arrays.removeAll(arr, ['a', 'd'])).toBe(true);
      expect(arr).toEqual(['b', 'c']);

      arr = ['a', 'b', 'c', 'a', 'd'];
      expect(scout.arrays.removeAll(arr, ['a', 'b', 'c', 'd'])).toBe(true);
      expect(arr).toEqual([]);
    });

    it("considers emtpy args", function() {
      expect(scout.arrays.removeAll()).toBe(false);
      expect(scout.arrays.removeAll([])).toBe(false);
      expect(scout.arrays.removeAll([], [])).toBe(false);
      expect(scout.arrays.removeAll([], ['x'])).toBe(false);
    });

  });

  describe("replace", function() {

    it("replaces elements", function() {
      var arr = ['a', 'b', 'c', 'd'];

      scout.arrays.replace(arr, 'c', 'x');
      expect(arr).toEqual(['a', 'b', 'x', 'd']);
      scout.arrays.replace(arr, 'e', 'y');
      expect(arr).toEqual(['a', 'b', 'x', 'd']);
    });

  });

  describe("insert", function() {

    it("insert element at index", function() {
      var arr = ['a', 'b', 'c', 'd'];
      scout.arrays.insert(arr, 'e', 0);
      expect(arr).toEqual(['e', 'a', 'b', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      scout.arrays.insert(arr, 'e', 1);
      expect(arr).toEqual(['a', 'e', 'b', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      scout.arrays.insert(arr, 'e', 10);
      expect(arr).toEqual(['a', 'b', 'c', 'd', 'e']);
    });

  });

  describe("insertArray", function() {

    it("insert element array at index", function() {
      var arr = ['a', 'b', 'c', 'd'];
      scout.arrays.insertArray(arr, ['e', 'f', 'g'], 0);
      expect(arr).toEqual(['e', 'f', 'g', 'a', 'b', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      scout.arrays.insertArray(arr, ['e', 'f', 'g'], 1);
      expect(arr).toEqual(['a', 'e', 'f', 'g', 'b', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      scout.arrays.insertArray(arr, ['e', 'f', 'g'], 10);
      expect(arr).toEqual(['a', 'b', 'c', 'd', 'e', 'f', 'g']);
    });

  });
  
  describe("max", function() {
    it("returns 0 iff input contains 0", function() {
      expect(scout.arrays.max([null, 5])).toBe(5);
      expect(scout.arrays.max([null, 0])).toBe(0);
      expect(scout.arrays.max([null, -13])).toBe(-13);
    });

    it("behaves like Math.max on null and undefined", function() {
      var doTest = function(arr) {
        expect(scout.arrays.max(arr)).toBe(Math.max.apply(Math, arr));
      };

      doTest(null);
      doTest(undefined);
    });

    it("ignores non-number elements", function() {
      expect(scout.arrays.max([null, 5, 6.7, false, undefined, NaN, {}, "batman"])).toBe(6.7);
    });
  });

  describe("min", function() {
    it("returns 0 iff input contains 0", function() {
      expect(scout.arrays.min([null, 5])).toBe(5);
      expect(scout.arrays.min([null, 0])).toBe(0);
      expect(scout.arrays.min([null, -13])).toBe(-13);
    });

    it("behaves like Math.min on null and undefined", function() {
      var doTest = function(arr) {
        expect(scout.arrays.min(arr)).toBe(Math.min.apply(Math, arr));
      };

      doTest(null);
      doTest(undefined);
    });

    it("ignores non-number elements", function() {
      expect(scout.arrays.min([null, 5, 6.7, false, undefined, NaN, {}, "batman"])).toBe(5);
    });
  });

  describe("move", function() {

    it("replaces elements", function() {
      var arr = ['a', 'b', 'c', 'd'];
      scout.arrays.move(arr, 0, 1);
      expect(arr).toEqual(['b', 'a', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      scout.arrays.move(arr, 1, 0);
      expect(arr).toEqual(['b', 'a', 'c', 'd']);

      arr = ['a', 'b', 'c', 'd'];
      scout.arrays.move(arr, 0, 100);
      expect(arr).toEqual(['b', 'c', 'd', 'a']);

      arr = ['a', 'b', 'c', 'd'];
      scout.arrays.move(arr, 10, 0);
      expect(arr).toEqual([undefined, 'a', 'b', 'c', 'd']);
    });

  });

  describe("union", function() {

    it("merges two arrays", function() {
      var arr1 = ['a', 'b', 'c'];
      var arr2 = ['d', 'e', 'f'];

      expect(scout.arrays.union(arr1, arr2)).toEqual(['a', 'b', 'c', 'd', 'e', 'f']);
    });

    it("merges two arrays and removes duplicates", function() {
      var arr1 = ['a', 'b', 'c'];
      var arr2 = ['d', 'a', 'e'];

      expect(scout.arrays.union(arr1, arr2)).toEqual(['a', 'b', 'c', 'd', 'e']);
    });

    it("also works with floats", function() {
      var arr1 = [1.5, 2];
      var arr2 = [1.5, 30];

      expect(scout.arrays.union(arr1, arr2)).toEqual([1.5, 2, 30]);
    });

    it("if the arrays contain objects instead of primitives, it uses their id to check for equality", function() {
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

      expect(scout.arrays.union(arr1, arr2)).toEqual([obj1, obj2, obj3]);
    });

    it("does not fail if arr1 or arr2 are not defined", function() {
      expect(scout.arrays.union(null, ['d', 'e', 'f'])).toEqual(['d', 'e', 'f']);
      expect(scout.arrays.union(['d', 'e', 'f'], null)).toEqual(['d', 'e', 'f']);
      expect(scout.arrays.union(null, null)).toEqual([]);
    });
  });

  describe("equals", function() {

    it("checks whether two arrays contain the same elements in the same order", function() {
      var arr1 = ['a', 'b', 'c'];
      var arr2 = ['a', 'b', 'c'];
      expect(scout.arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = ['a', 'b', 'c'];
      arr2 = ['b', 'a', 'b'];
      expect(scout.arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = ['a', 'b', 'c'];
      arr2 = ['a', 'b'];
      expect(scout.arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = ['a', 'b'];
      arr2 = ['a', 'b', 'c'];
      expect(scout.arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = ['a'];
      expect(scout.arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = ['b'];
      expect(scout.arrays.equals(arr1, arr2)).toBeFalsy();
    });

    it("considers emtpy and same arrays", function() {
      var arr1 = [];
      var arr2 = ['b', 'a', 'b'];
      expect(scout.arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = [];
      expect(scout.arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = [];
      arr2 = [];
      expect(scout.arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = arr1;
      expect(scout.arrays.equals(arr1, arr2)).toBeTruthy();
    });

    it("returns true if one array is undefined/null and the other empty", function() {
      var arr1 = [];
      var arr2;
      expect(scout.arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = undefined;
      expect(scout.arrays.equals(arr1, arr2)).toBeFalsy();

      arr1 = null;
      arr2 = [];
      expect(scout.arrays.equals(arr1, arr2)).toBeTruthy();

      arr1 = null;
      arr2 = null;
      expect(scout.arrays.equals(arr1, arr2)).toBeTruthy();
    });

  });

  describe("equalsIgnoreOrder", function() {

    it("checks whether two arrays contain the same elements without considering the order", function() {
      var arr1 = ['a', 'b', 'c'];
      var arr2 = ['b', 'a', 'b'];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = ['a', 'b', 'c'];
      arr2 = ['a', 'b'];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = ['a', 'b'];
      arr2 = ['a', 'b', 'c'];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = ['a'];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = ['b'];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();
    });

    it("considers emtpy and same arrays", function() {
      var arr1 = [];
      var arr2 = ['b', 'a', 'b'];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = [];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = [];
      arr2 = [];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = arr1;
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();
    });

    it("returns true if one array is undefined/null and the other empty", function() {
      var arr1 = [];
      var arr2;
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = undefined;
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeFalsy();

      arr1 = null;
      arr2 = [];
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();

      arr1 = null;
      arr2 = null;
      expect(scout.arrays.equalsIgnoreOrder(arr1, arr2)).toBeTruthy();
    });

  });

  describe("findIndex", function() {

    it("returns the index of the element for which the given predicate returns true", function() {
      var arr = ['a', 'b', 'c', 'd'];

      expect(scout.arrays.findIndex()).toBe(-1);
      expect(scout.arrays.findIndex()).toBe(-1);

      var index = scout.arrays.findIndex(arr, function(element) {
        return element === 'c';
      });
      expect(index).toBe(2);

      index = scout.arrays.findIndex(arr, function(element) {
        return element === 'z';
      });
      expect(index).toBe(-1);
    });

  });

  describe("find", function() {

    it("returns the element for which the given predicate returns true", function() {
      var arr = ['a', 'b', 'c', 'd'];

      expect(scout.arrays.find()).toBe(null);
      expect(scout.arrays.find(arr)).toBe(null);

      var element = scout.arrays.find(arr, function(element) {
        return element === 'c';
      });
      expect(element).toBe('c');

      element = scout.arrays.find(arr, function(element) {
        return element === 'z';
      });
      expect(element).toBeFalsy();
    });

  });

  describe("findFrom", function() {

    it("returns the element for which the given predicate returns true, starting from a given index", function() {
      var arr = ['a', 'b', 'c', 'd'];

      expect(scout.arrays.find()).toBe(null);
      expect(scout.arrays.find(arr)).toBe(null);

      var element = scout.arrays.findFrom(arr, 1, function(element) {
        return element === 'c';
      });
      expect(element).toBe('c');

      element = scout.arrays.findFrom(arr, 1, function(element) {
        return element === 'z';
      });
      expect(element).toBeFalsy();
    });

    it("does not return the element for which the given predicate returns true, if it is on the left side of the start index", function() {
      var arr = ['a', 'b', 'c', 'd'];

      var element = scout.arrays.findFrom(arr, 1, function(element) {
        return element === 'a';
      });
      expect(element).toBeFalsy();
    });

    it("also checks the element at start index ", function() {
      var arr = ['a', 'b', 'c', 'd'];

      var element = scout.arrays.findFrom(arr, 1, function(element) {
        return element === 'b';
      });
      expect(element).toBeTruthy();

      // edge cases
      element = scout.arrays.findFrom(arr, 0, function(element) {
        return element === 'a';
      });
      expect(element).toBeTruthy();

      element = scout.arrays.findFrom(arr, 3, function(element) {
        return element === 'd';
      });
      expect(element).toBeTruthy();

      element = scout.arrays.findFrom(arr, 3, function(element) {
        return element === 'z';
      });
      expect(element).toBeFalsy();
    });

    it("searches from right to left if backwards is true", function() {
      var arr = ['a', 'b', 'c', 'd'];

      var element = scout.arrays.findFrom(arr, 2, function(element) {
        return element === 'a';
      }, true);
      expect(element).toBeTruthy();

      element = scout.arrays.findFrom(arr, 2, function(element) {
        return element === 'd';
      }, true);
      expect(element).toBeFalsy();
    });

  });

  describe("format", function() {

    it("formats an array by concatenating each entry", function() {
      var arr = ['a', 'b', 'c', 'd'];
      expect(scout.arrays.format(arr)).toBe('abcd');
      expect(scout.arrays.format(arr, '_')).toBe('a_b_c_d');

      arr = ['abc', 'def'];
      expect(scout.arrays.format(arr)).toBe('abcdef');
      expect(scout.arrays.format(arr, '_')).toBe('abc_def');

      arr = ['abc'];
      expect(scout.arrays.format(arr)).toBe('abc');
      expect(scout.arrays.format(arr, '_')).toBe('abc');
    });

    it("returns '' for empty array or if no array was provided", function() {
      var arr = [];
      expect(scout.arrays.format(arr)).toBe('');
      expect(scout.arrays.format()).toBe('');
    });

  });

  describe("formatEncoded", function() {

    it("encodes the html of each array element", function() {
      var arr = ['<span>a</span>', 'b', 'c<p>', '<script>d</script>'];
      expect(scout.arrays.formatEncoded(arr, '<br>')).toBe('&lt;span&gt;a&lt;/span&gt;<br>b<br>c&lt;p&gt;<br>&lt;script&gt;d&lt;/script&gt;');

      arr = ['abc', '<script>def'];
      expect(scout.arrays.formatEncoded(arr)).toBe('abc&lt;script&gt;def');
      expect(scout.arrays.formatEncoded(arr, '<br>')).toBe('abc<br>&lt;script&gt;def');

      arr = ['<p>abc'];
      expect(scout.arrays.formatEncoded(arr)).toBe('&lt;p&gt;abc');
      expect(scout.arrays.formatEncoded(arr, '<br>')).toBe('&lt;p&gt;abc');
    });

  });

  describe("first", function() {

    it("finds first array element", function() {
      expect(scout.arrays.first()).toBe(undefined);
      expect(scout.arrays.first('test')).toBe('test');
      expect(scout.arrays.first({
        x: 'y'
      })).toEqual({
        x: 'y'
      });
      expect(scout.arrays.first([])).toBe(undefined);
      expect(scout.arrays.first([undefined])).toBe(undefined);
      expect(scout.arrays.first(['a', 'b', 'c'])).toBe('a');
      expect(scout.arrays.first([null, undefined, 123])).toBe(null);
      expect(scout.arrays.first(['', 'b', 'c'])).toBe('');
    });

  });

  describe("last", function() {

    it("finds last array element", function() {
      expect(scout.arrays.last()).toBe(undefined);
      expect(scout.arrays.last('test')).toBe('test');
      expect(scout.arrays.last({
        x: 'y'
      })).toEqual({
        x: 'y'
      });
      expect(scout.arrays.last([])).toBe(undefined);
      expect(scout.arrays.last([undefined])).toBe(undefined);
      expect(scout.arrays.last(['a', 'b', 'c'])).toBe('c');
      expect(scout.arrays.last([123, undefined, null])).toBe(null);
      expect(scout.arrays.last(['a', 'b', ''])).toBe('');
    });

  });

});
