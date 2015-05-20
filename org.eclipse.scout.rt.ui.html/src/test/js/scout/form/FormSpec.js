/* global FormSpecHelper */
describe("Form", function() {
  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    helper = new FormSpecHelper(session);
    session = sandboxSession();
    session.init();
    uninstallUnloadHandlers(session);
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("destroy", function() {

    it("destroys the adapter and its children", function() {
      var form = helper.createFormWithOneField(session);

      expect(form.rootGroupBox).toBeTruthy();
      expect(session.getModelAdapter(form.rootGroupBox.id)).toBe(form.rootGroupBox);
      expect(form.rootGroupBox.fields[0]).toBeTruthy();
      expect(session.getModelAdapter(form.rootGroupBox.fields[0].id)).toBe(form.rootGroupBox.fields[0]);

      form.destroy();

      expect(session.getModelAdapter(form.rootGroupBox.id)).toBeFalsy();
      expect(session.getModelAdapter(form.rootGroupBox.fields[0].id)).toBeFalsy();
    });

  });

  describe("onModelAction", function() {

    describe("formClose", function() {

      function createFormClosedEvent(model) {
        return {
          target: model.id,
          type: 'formClosed'
        };
      }

      it("destroys the form", function() {
        var form = helper.createFormWithOneField(session);
        spyOn(form, 'destroy');

        var message = {
          events: [createFormClosedEvent(form)]
        };
        session._processSuccessResponse(message);

        expect(form.destroy).toHaveBeenCalled();
      });

    });

  });

});
