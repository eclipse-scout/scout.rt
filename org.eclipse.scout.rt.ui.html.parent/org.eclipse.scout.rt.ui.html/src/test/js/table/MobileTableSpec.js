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

  describe("attach", function() {

    it("does not create an addional div for scrolling", function() {
      var model = helper.createModelFixture(2);
      var table = helper.createMobileTable(model);
      table.attach(session.$entryPoint);

      expect(table.$data).toBe(table.$dataScroll);
    });

  });


    it("does not display context menus", function() {
      var model = helper.createModelFixture(2,2);
      var table = helper.createMobileTable(model);
      table.attach(session.$entryPoint);

      model.menus = [helper.createMenuModel('1','menu')];
      var $row0 = table.$dataScroll.children().eq(0);
      $row0.triggerRightClick();

      var $menu = helper.getDisplayingRowMenu(table);

      expect($menu.length).toBeFalsy();
    });


});
