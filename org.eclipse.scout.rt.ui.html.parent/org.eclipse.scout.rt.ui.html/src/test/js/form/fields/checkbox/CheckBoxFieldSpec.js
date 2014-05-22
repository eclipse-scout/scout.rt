describe("CheckBoxField", function() {

  describe("inheritance", function() {

    var session;
    var checkBox;
    var model = {};

    beforeEach(function() {
      setFixtures(sandbox());
      session = new scout.Session($('#sandbox'), '1.1');
      checkBox = new scout.CheckBoxField(model, session);
    });

    it("inherits from FormField", function() {
      expect(scout.FormField.prototype.isPrototypeOf(checkBox)).toBe(true);
    });

  });

});
