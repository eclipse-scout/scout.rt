describe("Session", function() {

  beforeEach(function() {
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createSession() {
    setFixtures(sandbox());
    return new scout.Session($('#sandbox'), '1.1');
  }

  describe("send", function() {

    it("sends multiple async events in one call", function() {
      var session = createSession();

      session.send('nodeClicked', 1);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      session.send('nodeSelected', 1);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      session.send('nodeExpanded', 1);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      jasmine.clock().tick(0);

      //after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodeSelected', 'nodeExpanded']);
    });
  });

});
