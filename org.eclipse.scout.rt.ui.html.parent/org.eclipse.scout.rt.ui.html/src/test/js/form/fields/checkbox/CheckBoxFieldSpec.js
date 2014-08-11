describe("CheckBoxField", function() {

  describe("inheritance", function() {

    var session;
    var checkBox;
    var model = {id:'2'};

    beforeEach(function() {
      setFixtures(sandbox());
      session = new scout.Session($('#sandbox'), '1.1');
      checkBox = new scout.CheckBoxField();
      checkBox.init(model, session);
    });

    it("inherits from ValueField", function() {
      expect(scout.ValueField.prototype.isPrototypeOf(checkBox)).toBe(true);
    });

  });

});
