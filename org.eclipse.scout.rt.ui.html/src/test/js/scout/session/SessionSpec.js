describe('Session', function() {

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
    return sandboxSession({'userAgent':userAgent});
  }

  describe('send', function() {

    it('sends multiple async events in one call', function() {
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

    it('sends multiple async events in one call over multiple user interactions if sending was delayed', function() {
      var session = createSession();

      // send first event delayed (in 500 ms)
      session.send(1, 'nodeClicked', '', 500);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 100 ms
      sendQueuedAjaxCalls('', 100);

      // since 500 ms are not passed yet, the request has not been sent and following events should be added
      session.send(1, 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      session.send(1, 'nodeExpanded');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls('', 1000);

      // after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodeSelected', 'nodeExpanded']);
    });

    it('does not await the full delay if a susequent send call has a smaller delay', function() {
      var session = createSession();

      // send first event delayed (in 500 ms)
      session.send(1, 'nodeClicked', '', 500);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 100 ms
      sendQueuedAjaxCalls('', 100);

      // since 500 ms are not passed yet, the request has not been sent and following events should be added
      session.send(1, 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      session.send(1, 'nodeExpanded');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls('', 0);

      // after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodeSelected', 'nodeExpanded']);
    });

    it('coalesces events if event provides a coalesce function', function() {
      var session = createSession();

      var coalesce = function(previous) {
        return this.target === previous.target && this.type === previous.type && this.column === previous.column;
      };

      var event0 = new scout.Event(1, 'columnResized', {column: 'a'});
      event0.coalesce = coalesce;
      session.sendEvent(event0);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      var event1 = new scout.Event(1, 'rowSelected');
      event1.coalesce = coalesce;
      session.sendEvent(event1);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      var event2 = new scout.Event(1, 'columnResized', {column: 'a'});
      event2.coalesce = coalesce;
      session.sendEvent(event2);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      var event3 = new scout.Event(1, 'columnResized', {column: 'z'});
      event3.coalesce = coalesce;
      session.sendEvent(event3);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // event for another target
      var event4 = new scout.Event(2, 'columnResized', {column: 'a'});
      event4.coalesce = coalesce;
      session.sendEvent(event4);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      var event5 = new scout.Event(1, 'columnResized', {column: 'a'});
      event5.coalesce = coalesce;
      session.sendEvent(event5);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls();

      // after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check whether the first and second resize events were correctly removed
      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEvents([event1, event3, event4, event5]);
    });

  });

  describe('init', function() {

    it('sends startup parameter', function() {
      var session = createSession();

      session.init();
      uninstallUnloadHandlers(session);
      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      expect(requestData.startup).toBe(true);

      //don't send it on subsequent requests
      session.send(1, 'nodeClicked');
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.startup).toBeUndefined();
    });

    it('sends user agent on startup if not desktop', function() {
      var session = createSession(new scout.UserAgent(scout.UserAgent.DEVICE_TYPE_MOBILE));

      session.init();
      uninstallUnloadHandlers(session);
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
      uninstallUnloadHandlers(session);
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.userAgent).toBeUndefined();
    });

  });

  // Tests whether delegation to scout.Texts works as expected
  describe('texts', function() {

    var session;

    beforeEach(function() {
      session = createSession();
      session._texts = new scout.Texts({
        NoOptions: 'Keine Übereinstimmung',
        NumOptions: '{0} Optionen',
        Greeting: 'Hello {0}, my name is {2}, {1}.',
        Empty: '',
        Null: null
      });
    });

    it('check if correct text is returned', function() {
      expect(session.text('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it('check if empty text is returned', function() {
      expect(session.text('Empty')).toBe('');
    });

    it('check if null text is returned', function() {
      expect(session.text('Null')).toBe(null);
    });

    it('check if arguments are replaced in text', function() {
      expect(session.text('NumOptions', 3)).toBe('3 Optionen');
    });

    it('check if multiple arguments are replaced in text', function() {
      expect(session.text('Greeting', 'Computer', 'nice to meet you', 'User')).toBe('Hello Computer, my name is User, nice to meet you.');
    });

    it('check if undefined texts return an error message', function() {
      expect(session.text('DoesNotExist')).toBe('[undefined text: DoesNotExist]');
    });

    it('optText returns undefined if key is not found', function() {
      expect(session.optText('DoesNotExist')).toBe(undefined);
    });

    it('optText returns default value if key is not found', function() {
      expect(session.optText('DoesNotExist', '#Default', 'Any argument')).toBe('#Default');
    });

    it('optText returns text if key found', function() {
      expect(session.optText('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it('optText returns text if key found, with arguments', function() {
      expect(session.optText('NumOptions', '#Default', 7)).toBe('7 Optionen');
    });

  });

});
