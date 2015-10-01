describe("CheckBoxField", function() {

  describe("inheritance", function() {

    var session;
    var checkBox;
    var model;

    beforeEach(function() {
      setFixtures(sandbox());
      session = sandboxSession();
      model = createSimpleModel('CheckBoxField', session);
      checkBox = new scout.CheckBoxField();
      checkBox.init(model);
    });

    it("inherits from ValueField", function() {
      expect(scout.ValueField.prototype.isPrototypeOf(checkBox)).toBe(true);
    });

    it("_renderValue sets checked property", function() {
      var $div = $('<div>');
      checkBox._render($div);
      checkBox._renderValue(true);
      expect(checkBox.$checkBox.hasClass('checked')).toBe(true);
      checkBox._renderValue(false);
      expect(checkBox.$checkBox.hasClass('checked')).toBe(false);
    });

    it("_renderValue sets enabled property", function() {
      var $div = $('<div>');
      checkBox._render($div);
      checkBox.enabled=false;
      checkBox._renderEnabled();
      expect(checkBox.$field.hasClass('disabled')).toBe(true);
      expect(checkBox.$checkBox.hasClass('disabled')).toBe(true);
      checkBox.enabled=true;
      checkBox._renderEnabled();
      expect(checkBox.$field.hasClass('disabled')).toBe(false);
      expect(checkBox.$checkBox.hasClass('disabled')).toBe(false);
    });

  });

});
