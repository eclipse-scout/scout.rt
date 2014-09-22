describe("scout.Dimension", function() {

  describe("Ctor", function() {

    it("accepts two numbers as width and height arguments", function() {
      var dim = new scout.Dimension(6, 7);
      expect(dim.width).toBe(6);
      expect(dim.height).toBe(7);
    });

    it("accepts a single scout.Dimension argument", function() {
      var dim1 = new scout.Dimension(6, 7);
      var dim2 = new scout.Dimension(dim1);
      expect(dim2.width).toBe(6);
      expect(dim2.height).toBe(7);
      expect(dim1).toEqual(dim2);
    });

  });

});
