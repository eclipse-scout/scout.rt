describe("Session", function() {

  beforeEach(function() {
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createSession(userAgent) {
    setFixtures(sandbox());
    return new scout.Session($('#sandbox'), '1.1', {'userAgent':userAgent});
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

      sendQueuedAjaxCalls();

      //after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodeSelected', 'nodeExpanded']);
    });

  });

  describe("init", function() {

    it("sends startup parameter", function() {
      var session = createSession();

      session.init();
      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      expect(requestData.startup).toBe(true);

      //don't send it on subsequent requests
      session.send('nodeClicked', 1);
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.startup).toBeUndefined();
    });

    it("sends user agent on startup if not desktop", function() {
      var session = createSession(new scout.UserAgent(scout.UserAgent.DEVICE_TYPE_MOBILE));

      session.init();
      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      expect(requestData.userAgent.deviceType).toBe('MOBILE');

      //don't send it on subsequent requests
      session.send('nodeClicked', 1);
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.userAgent).toBeUndefined();

      //device type desktop is the default, so don't send it
      session = createSession(new scout.UserAgent(scout.UserAgent.DEVICE_TYPE_DESKTOP));

      session.init();
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.userAgent).toBeUndefined();
    });

  });

});
