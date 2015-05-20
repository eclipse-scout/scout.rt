/* global FormSpecHelper */
describe("FormField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  describe("inheritance", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.FormField();
      formField.init(model, session);
    });

    it("inherits from ModelAdapter", function() {
      expect(scout.ModelAdapter.prototype.isPrototypeOf(formField)).toBe(true);
    });

  });

  describe("property label position", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.StringField();
      formField.init(model, session);
    });

    describe("position on_field", function() {

      beforeEach(function() {
        formField.label = 'labelName';
        formField.labelPosition = scout.FormField.LABEL_POSITION_ON_FIELD;
      });

      it("sets the label as placeholder", function() {
        formField.render(session.$entryPoint);
        expect(formField.$label.html()).toBeFalsy();
        expect(formField.$field.attr('placeholder')).toBe(formField.label);
      });

      it("does not call field._renderLabelPosition initially", function() {
        formField.render(session.$entryPoint);
        expect(formField.$label.html()).toBeFalsy();
        expect(formField.$field.attr('placeholder')).toBe(formField.label);

        spyOn(formField, '_renderLabelPosition');
        expect(formField._renderLabelPosition).not.toHaveBeenCalled();
      });

    });

    describe("position top", function() {

      beforeEach(function() {
        formField.label = 'labelName';
        formField.labelPosition = scout.FormField.LABEL_POSITION_TOP;
      });

      it("guarantees a minimum height if label is empty", function() {
        formField.label = '';
        formField.render(session.$entryPoint);
        expect(formField.$label.html()).toBe('&nbsp;');
        expect(formField.$label).toBeVisible();
      });

    });

  });

  describe("onModelPropertyChange", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.FormField();
      formField.init(model, session);
    });

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
      expect(formField.id).toBe(model.id);
      expect(formField.hasOwnProperty('type')).toBe(false);
      expect(formField.hasOwnProperty('properties')).toBe(false);
    });

    it("considers custom css class", function() {
      formField._render = function($parent) {
        this.addContainer($parent, 'form-field');
      };
      formField.render(session.$entryPoint);

      var event = createPropertyChangeEvent(formField, {cssClass: 'custom-class'});
      formField.onModelPropertyChange(event);
      expect(formField.$container).toHaveClass('custom-class');

      event = createPropertyChangeEvent(formField, {cssClass: ''});
      formField.onModelPropertyChange(event);
      expect(formField.$container).not.toHaveClass('custom-class');
    });

  });

});
