describe("scout.objects", function() {

  describe("copyProperties", function() {

    it("check if all properties are copied", function() {
      var dest = {}, source = {
        foo: 6,
        bar: 7
      };
      scout.objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
    });

  });

});
