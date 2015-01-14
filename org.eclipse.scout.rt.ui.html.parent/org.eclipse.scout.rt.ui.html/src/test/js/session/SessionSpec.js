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

      session.send(1, 'nodeClicked');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      session.send(1, 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      session.send(1, 'nodeExpanded');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls();

      // after executing setTimeout there must be exactly one ajax request
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
      session.send(1, 'nodeClicked');
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
      session.send(1, 'nodeClicked');
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

  describe("texts", function() {

    var session = createSession();
    // In production mode these texts are sent by the server in the initialize event
    session._textMap = {
      NoOptions: 'Keine Übereinstimmung',
      NumOptions: '{0} Optionen',
      Greeting: 'Hello {0}, my name is {2}, {1}.',
      Empty: '',
      Null: null
    };

    it("check if correct text is returned", function() {
      expect(session.text('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it("check if empty text is returned", function() {
      expect(session.text('Empty')).toBe('');
    });

    it("check if null text is returned", function() {
      expect(session.text('Null')).toBe(null);
    });

    it("check if arguments are replaced in text", function() {
      expect(session.text('NumOptions', 3)).toBe('3 Optionen');
    });

    it("check if multiple arguments are replaced in text", function() {
      expect(session.text('Greeting', 'Computer', 'nice to meet you', 'User')).toBe('Hello Computer, my name is User, nice to meet you.');
    });

    it("check if undefined texts return an error message", function() {
      expect(session.text('DoesNotExist')).toBe('[undefined text: DoesNotExist]');
    });

    it("optText returns undefined if key is not found", function() {
      expect(session.optText('DoesNotExist')).toBe(undefined);
    });

    it("optText returns default value if key is not found", function() {
      expect(session.optText('DoesNotExist', '#Default', 'Any argument')).toBe('#Default');
    });

    it("optText returns text if key found", function() {
      expect(session.optText('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it("optText returns text if key found, with arguments", function() {
      expect(session.optText('NumOptions', '#Default', 7)).toBe('7 Optionen');
    });

  });

});
