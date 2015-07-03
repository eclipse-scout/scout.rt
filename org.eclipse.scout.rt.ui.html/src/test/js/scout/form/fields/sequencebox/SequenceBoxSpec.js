/* global FormSpecHelper */
describe("SequenceBox", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createField(model) {
    return helper.createCompositeField(session, model);
  }

  function createModel() {
    var model = helper.createFieldModel('SequenceBox');
    model.fields = [helper.createFieldModel('StringField'), helper.createFieldModel('DateField')];
    return model;
  }

  describe("mandatory indicator", function() {

    // Must not contain an indicator to prevent a double indicator if the first field is mandatory too
    it("does not exist", function() {
      var model = createModel();
      model.mandatory = true;
      var field = createField(model);
      field.render(session.$entryPoint);

      expect(field.$mandatory).toBeUndefined();
    });

  });

  describe("label width", function() {

    it("is 0 if it is empty", function() {
      var model = createModel();
      var field = createField(model);
      field.render(session.$entryPoint);
      // css is not applied, therefore we need to adjust display style here
      field.fields[0].$label.css('display', 'inline-block');
      field.validateLayout();

      expect(field.fields[0].$label.outerWidth(true)).toBe(0);
    });

  });

});
