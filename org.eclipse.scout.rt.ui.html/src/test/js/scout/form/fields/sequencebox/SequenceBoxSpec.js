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
    var field = new scout.SequenceBox();
    field.init(model, session);
    return field;
  }

  function createModel() {
    return helper.createFieldModel('SequenceBox');
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

});
