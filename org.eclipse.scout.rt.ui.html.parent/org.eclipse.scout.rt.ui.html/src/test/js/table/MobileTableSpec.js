/* global TableSpecHelper */
describe("MobileTable", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("render", function() {

    //FIXME CGU temporarily disabled until mobile works again
//    it("does not create an addional div for scrolling", function() {
//      var model = helper.createModelFixture(2);
//      var table = helper.createMobileTable(model);
//      table.render(session.$entryPoint);
//      expect(table.$data).toBe(table._$viewport);
//    });

    it("does not display context menus", function() {
      var model = helper.createModelFixture(2,2);
      var table = helper.createMobileTable(model);
      table.render(session.$entryPoint);

      model.menus = [helper.createMenuModel('1','menu')];
      var $row0 = table._$viewport.children().eq(0);
      $row0.triggerContextMenu();

      var $menu = helper.getDisplayingContextMenu(table);
      expect($menu.length).toBeFalsy();
    });
  });


});
