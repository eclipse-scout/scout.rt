/* global MenuSpecHelper */
describe("Menubar", function() {
  var helper, session;

  beforeEach(function() {
    session = new scout.Session($('#sandbox'), '1.1');
    helper = new MenuSpecHelper(session);
  });

  describe("updateItems", function() {

    it("prefers menu type for the left location if menu types for multiple locations are specified", function() {
      var menus, menu1;
      var menubar = new scout.Menubar(session.$entryPoint);
      menubar.menuTypesForLeft1 = ['Table.EmptySpace'];
      menubar.menuTypesForLeft2 = ['Table.SingleSelection', 'Table.MultiSelection'];
      menubar.menuTypesForRight = ['Table.Header'];

      menu1 = helper.createMenu(helper.createModel(1));
      menu1.menuTypes = ['Table.EmptySpace', 'Table.SingleSelection'];
      menus = [menu1];

      menubar.updateItems(menus);
      expect(menubar.menus.length).toBe(2);
      expect(menubar.menus[0]).toBe(menu1);
      //menus[1] is separator
      expect(menubar.menus[1]).not.toBe(menu1);
    });

  });

});
