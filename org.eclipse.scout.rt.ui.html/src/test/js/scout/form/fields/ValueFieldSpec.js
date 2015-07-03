/* global FormSpecHelper */
/* global MenuSpecHelper */
describe("ValueField", function() {
  var session, helper, menuHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    menuHelper = new MenuSpecHelper(session);
  });

  describe("property status visible", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.ValueField();
      formField._render = function($parent) {
        this.addContainer($parent, 'form-field');
        this.addField($('<div>'));
        this.addStatus();
      };
      formField.init(model, session);
    });

    it("shows a status even though status visible is false but there are visible menus", function() {
      formField.statusVisible = false;
      var menu0 = menuHelper.createMenu(menuHelper.createModel());
      formField.menus = [menu0];
      formField.menusVisible = true;
      formField.render(session.$entryPoint);
      expect(formField.$status.isVisible()).toBe(true);

      var event = createPropertyChangeEvent(formField, {
        menusVisible: false
      });
      formField.onModelPropertyChange(event);
      expect(formField.$status.isVisible()).toBe(false);
    });

  });

});
