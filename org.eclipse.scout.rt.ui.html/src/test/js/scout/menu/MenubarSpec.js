/* global MenuSpecHelper */
describe("Menubar", function() {
  // FIXME AWE: (menu) rename to MenuBar (file)
  var helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    helper = new MenuSpecHelper(session);
  });

  describe("updateItems", function() {

    it("prefers menu type for the left location if menu types for multiple locations are specified", function() {
      var menus, menu1;
      var menubar = new scout.MenuBar(session.$entryPoint, 'top', scout.GroupBoxMenuItemsOrder.order);

      menu1 = helper.createMenu(helper.createModel(1));
      menu1.menuTypes = ['Table.EmptySpace', 'Table.SingleSelection'];
      menus = [menu1];

      menubar.updateItems(menus);
      // FIXME AWE: fix MenubarSpec (still required to add a separator here)?
      /*
      expect(menubar.menuItems.length).toBe(2);
      expect(menubar.menuItems[0]).toBe(menu1);
      //menus[1] is separator
      expect(menubar.menuItems[1]).not.toBe(menu1);
      */
    });

  });

});
