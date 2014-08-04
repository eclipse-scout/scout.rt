describe("FormField", function() {

  var session;
  var formField;
  var model = {id:'2'};

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
      // Note: normally an event for ID 123 would never be applied
      // to an adpater with ID 2! We only do this here in order to
      // check whether or not the onModelPropertyChange method applies
      // the ID of the event by error (should not happen).
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
      expect(formField.id).toBe('2');
      expect(formField.hasOwnProperty('type')).toBe(false);
      expect(formField.hasOwnProperty('properties')).toBe(false);
    });

  });

});
