describe("Scout", function() {
  var a;

  describe("$.numberToString", function() {
    it("formats 10 to '10'", function() {
      var str = $.numberToString(10);
      expect(str).toBe('10');
    });

    it("formats 10.49 to '10' with round 0", function() {
      var str = $.numberToString(10.49, 0);
      expect(str).toBe('10');
    });

    it("formats 10.5 to '11' with round 0", function() {
      var str = $.numberToString(10.5, 0);
      expect(str).toBe('11');
    });

  });
});
