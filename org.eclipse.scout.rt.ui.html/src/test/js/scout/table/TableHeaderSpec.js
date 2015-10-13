/* global TableSpecHelper*/
describe("TableHeaderSpec", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
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

    it("attaches listener to the table but only once", function() {
      var model = helper.createModelFixture(2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);
      expect(table.header).not.toBeUndefined();
      var listenerCount = table.events._eventListeners.length;

      var event = createPropertyChangeEvent(table, {
        "headerVisible": false
      });
      table.onModelPropertyChange(event);

      event = createPropertyChangeEvent(table, {
        "headerVisible": true
      });
      table.onModelPropertyChange(event);

      // Still same amount of listeners expected after header visibility changed
      expect(table.events._eventListeners.length).toBe(listenerCount);
    });

  });
});
