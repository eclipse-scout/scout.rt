describe("StringField", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
  });

  function createField(model) {
    var field = new scout.StringField();
    field.init(model, session);
    return field;
  }

  function createModel(id) {
    if (id === undefined) {
      id = createUniqueAdapterId();
    }
    var model =  {
      "id": id,
      "enabled": true,
      "visible": true
    };
    return model;
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
