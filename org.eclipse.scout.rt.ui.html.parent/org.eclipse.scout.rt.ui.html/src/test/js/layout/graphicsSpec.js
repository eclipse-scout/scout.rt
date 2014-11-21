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

  describe("getBounds", function() {

    var $div = $('<div>').
      css('left', '6px').
      css('top', '7px').
      css('width', '8px').
      css('height', '9px').
      css('margin', '10px');

    it("returns JQuery.outerWidth/Height(true)", function() {
      var rect = scout.graphics.getBounds($div);
      expect(rect.x).toBe(6);
      expect(rect.y).toBe(7);
      expect(rect.width).toBe(28);
      expect(rect.height).toBe(29);
    });

    it("returns 0 when left/right is set to auto", function() {
      $div.
        css('left', 'auto').
        css('top', 'auto');
      var rect = scout.graphics.getBounds($div);
      expect(rect.x).toBe(0);
      expect(rect.y).toBe(0);
    });

  });

});
