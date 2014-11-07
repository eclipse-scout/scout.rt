/* global FormFieldSpecHelper */
describe("StringField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    helper = new FormFieldSpecHelper(session);
  });

  function createField(model) {
    var field = new scout.StringField();
    field.init(model, session);
    return field;
  }

  function createModel(id) {
    return helper.createModel();
  }

  describe("property label position", function() {
    var field;

    beforeEach(function() {
      field = createField(createModel());
    });

    describe("position on_field", function() {

      beforeEach(function() {
        field.label = 'labelName';
        field.labelPosition = scout.FormField.LABEL_POSITION_ON_FIELD;
      });

      it("sets the label as placeholder", function() {
        field.render(session.$entryPoint);
        expect(field.$label.html()).toBeFalsy();
        expect(field.$field.attr('placeholder')).toBe(field.label);
      });

      it("does not call field._renderLabelPosition initially", function() {
        field.render(session.$entryPoint);
        expect(field.$label.html()).toBeFalsy();
        expect(field.$field.attr('placeholder')).toBe(field.label);

        spyOn(field, '_renderLabelPosition');
        expect(field._renderLabelPosition).not.toHaveBeenCalled();
      });

    });

  });

});
