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
        type:'property',
        properties: {
          errorStatus:'foo'
        }
      };
      // required
      formField._$statusLabel = $('<div></div>');
      formField.onModelPropertyChange(event);
      expect(formField.errorStatus).toBe('foo');
      // never apply id, type, properties on model
      expect(formField.hasOwnProperty('id')).toBe(false);
      expect(formField.hasOwnProperty('type')).toBe(false);
      expect(formField.hasOwnProperty('properties')).toBe(false);
    });

  });

});
