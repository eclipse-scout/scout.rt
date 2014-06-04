describe("FormField", function() {

  var session;
  var formField;
  var model = {};

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    formField = new scout.FormField();
    formField.init(model, session);
  });

  describe("inheritance", function() {

    it("inherits from ModelAdapter", function() {
      expect(scout.ModelAdapter.prototype.isPrototypeOf(formField)).toBe(true);
    });

  });

  describe("onModelPropertyChange", function() {

    it("event should update model", function() {
      var event = {
        id:'123',
        type_:'property',
        errorStatus:'foo'
      };
      // required
      formField._$statusLabel = $('<div></div>');
      formField.onModelPropertyChange(event);
      expect(model.errorStatus).toBe('foo');
      // never apply id, type_ on model
      expect(model.hasOwnProperty('id')).toBe(false);
      expect(model.hasOwnProperty('type_')).toBe(false);
    });

  });

});
