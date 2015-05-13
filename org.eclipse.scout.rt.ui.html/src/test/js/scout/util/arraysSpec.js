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
      expect(arr).toEqual(['b', undefined, 'c', undefined, 'd' ]);
      expect(scout.arrays.remove(arr)).toBe(true);
      expect(arr).toEqual(['b', 'c', undefined, 'd' ]);
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
      expect(scout.arrays.first({x: 'y'})).toEqual({x: 'y'});
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
      expect(scout.arrays.last({x: 'y'})).toEqual({x: 'y'});
      expect(scout.arrays.last([])).toBe(undefined);
      expect(scout.arrays.last([undefined])).toBe(undefined);
      expect(scout.arrays.last(['a', 'b', 'c'])).toBe('c');
      expect(scout.arrays.last([123, undefined, null])).toBe(null);
      expect(scout.arrays.last(['a', 'b', ''])).toBe('');
    });

  });

});
