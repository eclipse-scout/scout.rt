describe("FormField", function() {

  describe("inheritance", function() {

    var session;
    var formField;
    var model = {};

    beforeEach(function() {
      setFixtures(sandbox());
      session = new scout.Session($('#sandbox'), '1.1');
      formField = new scout.FormField(model, session);
    });

    it("inherits from ModelAdapter", function() {
      expect(scout.ModelAdapter.prototype.isPrototypeOf(formField)).toBe(true);
    });

  });

  // TODO AWE: (form) add more tests to FormFieldSpec


});
