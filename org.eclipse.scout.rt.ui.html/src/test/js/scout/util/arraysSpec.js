describe("scout.arrays", function() {

  describe("init", function() {

    it("checks whether array has correct length and initial values", function() {
      var i, arr = scout.arrays.init(5, 'foo');
      expect(arr.length).toBe(5);
      for (i=0; i<arr.length; i++) {
        expect(arr[i]).toBe('foo');
      }
    });

  });

  describe("replace", function() {

    it("replaces elements", function() {
      var arr = [ 'a', 'b', 'c', 'd' ];

      scout.arrays.replace(arr, 'c', 'x');
      expect(arr).toEqual(['a', 'b', 'x', 'd']);
      scout.arrays.replace(arr, 'e', 'y');
      expect(arr).toEqual(['a', 'b', 'x', 'd']);
    });

  });

  describe("equals", function() {

    it("checks whether two arrays contain the same elements in the same order", function() {
      var arr1 = ['a','b','c'];
      var arr2 = ['a','b','c'];
      expect(scout.arrays.equals(arr1,arr2)).toBeTruthy();

      arr1 = ['a','b','c'];
      arr2 = ['b','a','b'];
      expect(scout.arrays.equals(arr1,arr2)).toBeFalsy();

      arr1 = ['a','b','c'];
      arr2 = ['a','b'];
      expect(scout.arrays.equals(arr1,arr2)).toBeFalsy();

      arr1 = ['a','b'];
      arr2 = ['a','b', 'c'];
      expect(scout.arrays.equals(arr1,arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = ['a'];
      expect(scout.arrays.equals(arr1,arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = ['b'];
      expect(scout.arrays.equals(arr1,arr2)).toBeFalsy();
    });

    it("considers emtpy and same arrays", function() {
      var arr1 = [];
      var arr2 = ['b','a','b'];
      expect(scout.arrays.equals(arr1,arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = [];
      expect(scout.arrays.equals(arr1,arr2)).toBeFalsy();

      arr1 = [];
      arr2 = [];
      expect(scout.arrays.equals(arr1,arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = arr1;
      expect(scout.arrays.equals(arr1,arr2)).toBeTruthy();
    });

    it("returns true if one array is undefined/null and the other empty", function() {
      var arr1 = [];
      var arr2;
      expect(scout.arrays.equals(arr1,arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = undefined;
      expect(scout.arrays.equals(arr1,arr2)).toBeFalsy();

      arr1 = null;
      arr2 = [];
      expect(scout.arrays.equals(arr1,arr2)).toBeTruthy();

      arr1 = null;
      arr2 = null;
      expect(scout.arrays.equals(arr1,arr2)).toBeTruthy();
    });

  });

  describe("equalsIgnoreOrder", function() {

    it("checks whether two arrays contain the same elements without considering the order", function() {
      var arr1 = ['a','b','c'];
      var arr2 = ['b','a','b'];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();

      arr1 = ['a','b','c'];
      arr2 = ['a','b'];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = ['a','b'];
      arr2 = ['a','b', 'c'];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = ['a'];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = ['b'];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();
    });

    it("considers emtpy and same arrays", function() {
      var arr1 = [];
      var arr2 = ['b','a','b'];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = [];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = [];
      arr2 = [];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = arr1;
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();
    });

    it("returns true if one array is undefined/null and the other empty", function() {
      var arr1 = [];
      var arr2;
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = undefined;
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = null;
      arr2 = [];
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();

      arr1 = null;
      arr2 = null;
      expect(scout.arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();
    });

  });
  describe("find", function() {

    it("returns the element for which the given predicate returns true", function() {
      var arr = ['a', 'b', 'c', 'd'];

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

});
