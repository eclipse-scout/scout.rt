/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/*global receiveResponseForAjaxCall */
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
    var session = sandboxSession({
      'userAgent': userAgent
    });
    // test request only, don't test response (would require valid session, desktop etc.)
    session._processStartupResponse = function() {};
    return session;
  }

  function send(session, target, type, data, delay) {
    session.sendEvent(new scout.RemoteEvent(target, type, data), delay);
  }

  describe('send', function() {

    it('sends multiple async events in one call', function() {
      var session = createSession();
      // initially there should be no request at all
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, 1, 'nodeClicked');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, 1, 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, 1, 'nodeExpanded');
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
      send(session, 1, 'nodeClicked', '', 500);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 100 ms
      sendQueuedAjaxCalls('', 100);

      // since 500 ms are not passed yet, the request has not been sent and following events should be added
      send(session, 1, 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, 1, 'nodeExpanded');
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
      send(session, 1, 'nodeClicked', '', 500);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 100 ms
      sendQueuedAjaxCalls('', 100);

      // since 500 ms are not passed yet, the request has not been sent and following events should be added
      send(session, 1, 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, 1, 'nodeExpanded');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls('', 0);

      // after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodeSelected', 'nodeExpanded']);
    });

    it('does not await the full delay if a previous send call has a smaller delay', function() {
      var session = createSession();

      // send first event with 300ms delay
      send(session, 1, 'nodeClicked', '', 300);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // send second event with 500ms delay
      send(session, 1, 'nodeSelected', '', 500);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 100 ms
      sendQueuedAjaxCalls('', 100);

      // since 300 ms are not passed yet, the request has not been sent
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 250 ms --> both events should now have been sent
      sendQueuedAjaxCalls('', 250);

      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodeSelected']);
    });

    it('coalesces events if event provides a coalesce function', function() {
      var session = createSession();

      var coalesce = function(previous) {
        return this.target === previous.target && this.type === previous.type && this.column === previous.column;
      };

      var event0 = new scout.RemoteEvent(1, 'columnResized', {
        column: 'a'
      });
      event0.coalesce = coalesce;
      session.sendEvent(event0);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      var event1 = new scout.RemoteEvent(1, 'rowSelected');
      event1.coalesce = coalesce;
      session.sendEvent(event1);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      var event2 = new scout.RemoteEvent(1, 'columnResized', {
        column: 'a'
      });
      event2.coalesce = coalesce;
      session.sendEvent(event2);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      var event3 = new scout.RemoteEvent(1, 'columnResized', {
        column: 'z'
      });
      event3.coalesce = coalesce;
      session.sendEvent(event3);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // event for another target
      var event4 = new scout.RemoteEvent(2, 'columnResized', {
        column: 'a'
      });
      event4.coalesce = coalesce;
      session.sendEvent(event4);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      var event5 = new scout.RemoteEvent(1, 'columnResized', {
        column: 'a'
      });
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

    it('sends requests consecutively', function() {
      var session = createSession();

      // send first request
      send(session, 1, 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // trigger sending (response not received yet)
      jasmine.clock().tick(0);

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);

      send(session, 1, 'nodeClicked');

      // trigger sending of second request
      jasmine.clock().tick(0);

      // second request must not be sent because first is still pending
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(true);
      expect(session.asyncEvents[0].type).toBe('nodeClicked');

      // receive response for nodeSelected -> request for nodeClicked gets sent
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0));
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(2);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areRequestsPending()).toBe(true);

      // receive response for nodeClicked
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(1));
      jasmine.clock().tick(0);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areRequestsPending()).toBe(false);
    });

    it('sends requests consecutively and respects delay', function() {
      var session = createSession();

      // send first request
      send(session, 1, 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // trigger sending (response not received yet)
      jasmine.clock().tick(0);

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);

      // send second request delayed
      send(session, 1, 'nodeClicked', '', 300);

      // trigger sending of second request
      jasmine.clock().tick(0);

      // second request must not be sent because first is still pending
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(true);
      expect(session.asyncEvents[0].type).toBe('nodeClicked');

      // receive response for nodeSelected -> request for nodeClicked does not get sent because it should be sent delayed
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0));
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(true);
      expect(session.asyncEvents[0].type).toBe('nodeClicked');

      // trigger sending of second request
      jasmine.clock().tick(300);

      // now second request is sent because the time elapsed
      expect(jasmine.Ajax.requests.count()).toBe(2);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areRequestsPending()).toBe(true);

      // receive response for nodeClicked
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(1));
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areRequestsPending()).toBe(false);
    });

    it('queues ?poll results when user requests are pending', function() {
      var session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Start user request
      send(session, 1, 'nodeSelected');
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(2);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(true); // <--
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send response for ?poll request (response must be queued)
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0));
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(true); // <--
      expect(session._processSuccessResponse).not.toHaveBeenCalled();

      // Send response for user request (must be executed, including the queued response)
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(1));
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false); // <--
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false); // <--
      expect(session._processSuccessResponse).toHaveBeenCalled();
    });

    it('resumes polling after successful responses', function() {
      var session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send response for ?poll request
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: '{"events": []}'
      });
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);
      expect(session._processSuccessResponse).toHaveBeenCalled(); // <--
      expect(session._processErrorJsonResponse).not.toHaveBeenCalled();
      expect(session._processErrorResponse).not.toHaveBeenCalled();
    });

    it('does not resume polling after JS errors', function() {
      var session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send response for ?poll request
      expect(function() {
        receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
          status: 200,
          responseText: '{"events": [ { "target": "invalidTarget" } ]}' // <-- causes a JS error
        });
      }).toThrow();
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.FAILURE); // <--
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(true); // still in queue
      expect(session._processSuccessResponse).toHaveBeenCalled(); // <--
      expect(session._processErrorJsonResponse).not.toHaveBeenCalled();
      expect(session._processErrorResponse).not.toHaveBeenCalled();
    });

    it('does not resume polling after UI server errors', function() {
      var session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send "UI server" response for ?poll request
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: '{"error": true}'
      });
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.FAILURE); // <--
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);
      expect(session._processSuccessResponse).not.toHaveBeenCalled();
      expect(session._processErrorJsonResponse).toHaveBeenCalled(); // <--
      expect(session._processErrorResponse).not.toHaveBeenCalled();
    });

    it('does not resume polling after HTTP errors', function() {
      var session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send "UI server" response for ?poll request
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 404,
        responseText: 'Not found'
      });
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.FAILURE); // <--
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);
      expect(session._processSuccessResponse).not.toHaveBeenCalled();
      expect(session._processErrorJsonResponse).not.toHaveBeenCalled();
      expect(session._processErrorResponse).toHaveBeenCalled(); // <--
    });

    it('does not resume polling after session terminated', function() {
      var session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send "UI server" response for ?poll request
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: '{"sessionTerminated": true}'
      });
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(scout.BackgroundJobPollingStatus.STOPPED); // <--
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);
      expect(session._processSuccessResponse).not.toHaveBeenCalled(); // <--
      expect(session._processErrorJsonResponse).not.toHaveBeenCalled();
      expect(session._processErrorResponse).not.toHaveBeenCalled();
    });
  });

  describe('init', function() {

    it('sends startup parameter', function() {
      var session = createSession();
      session.start();

      uninstallUnloadHandlers(session);
      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      expect(requestData.startup).toBe(true);

      //don't send it on subsequent requests
      send(session, 1, 'nodeClicked');
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.startup).toBeUndefined();
    });

    it('sends user agent on startup', function() {
      var session = createSession(new scout.UserAgent({
        deviceType: scout.Device.Type.MOBILE
      }));
      session.start();

      uninstallUnloadHandlers(session);
      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      expect(requestData.userAgent.deviceType).toBe('MOBILE');

      // don't send it on subsequent requests
      send(session, 1, 'nodeClicked');
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.userAgent).toBeUndefined();
    });

  });

  // Tests whether delegation to scout.TextMap works as expected
  describe('texts', function() {

    var session;

    beforeEach(function() {
      session = createSession();
      session.textMap = new scout.TextMap({
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
