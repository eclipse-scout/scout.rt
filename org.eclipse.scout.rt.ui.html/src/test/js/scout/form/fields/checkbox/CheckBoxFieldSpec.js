describe("CheckBoxField", function() {

  describe("inheritance", function() {

    var session;
    var checkBox;
    var model = {id:'2'};

    beforeEach(function() {
      setFixtures(sandbox());
      session = sandboxSession();
      checkBox = new scout.CheckBoxField();
      checkBox.init(model, session);
    });

    it("inherits from ValueField", function() {
      expect(scout.ValueField.prototype.isPrototypeOf(checkBox)).toBe(true);
    });

    it("_renderValue sets checked property", function() {
      var $div = $('<div>');
      checkBox._render($div);
      checkBox._renderValue(true);
      expect(checkBox._$checkBox.hasClass('checked')).toBe(true);
      checkBox._renderValue(false);
      expect(checkBox._$checkBox.hasClass('checked')).toBe(false);
    });

    it("_renderValue sets enabled property", function() {
      var $div = $('<div>');
      checkBox._render($div);
      checkBox.enabled=false;
      checkBox._renderEnabled();
      expect(checkBox._$checkBox.hasClass('disabled')).toBe(true);
    });

  });

});
