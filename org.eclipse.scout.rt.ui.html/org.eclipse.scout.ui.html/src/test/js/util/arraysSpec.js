describe("arrays", function() {

  describe("equalsIgnoreOrder", function() {

    it("checks whether two arrays contain the same elements without considering the order", function() {
      var arr1 = ['a','b','c'];
      var arr2 = ['b','a','b'];
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();

      arr1 = ['a','b','c'];
      arr2 = ['a','b'];
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = ['a','b'];
      arr2 = ['a','b', 'c'];
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = ['a'];
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = ['b'];
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();
    });

    it("considers emtpy and same arrays", function() {
      var arr1 = [];
      var arr2 = ['b','a','b'];
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = ['a'];
      arr2 = [];
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeFalsy();

      arr1 = [];
      arr2 = [];
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();

      arr1 = ['a'];
      arr2 = arr1;
      expect(arrays.equalsIgnoreOrder(arr1,arr2)).toBeTruthy();
    });

  });

});
