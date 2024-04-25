/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BackgroundJobPollingStatus, Device, RemoteEvent, Session, TextMap, TextMapType, texts, UserAgent} from '../../src/index';
import {LocaleSpecHelper} from '../../src/testing';

describe('Session', () => {

  beforeEach(() => {
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createSession(userAgent?: UserAgent): SandboxSession {
    setFixtures(sandbox());
    let session = sandboxSession({
      userAgent: userAgent
    });
    // test request only, don't test response (would require valid session, desktop etc.)
    session._processStartupResponse = () => {
      // nop
    };
    return session;
  }

  function send(session: Session, target: string, type: string, data?: object, delay?: number) {
    session.sendEvent(new RemoteEvent(target, type, data), delay);
  }

  describe('send', () => {

    it('sends multiple async events in one call', () => {
      let session = createSession();
      // initially there should be no request at all
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, '1', 'nodeClick');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, '1', 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, '1', 'nodeExpanded');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls();

      // after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      let requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClick', 'nodeSelected', 'nodeExpanded']);
    });

    it('sends multiple async events in one call over multiple user interactions if sending was delayed', () => {
      let session = createSession();

      // send first event delayed (in 500 ms)
      send(session, '1', 'nodeClick', {}, 500);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 100 ms
      sendQueuedAjaxCalls(null, 100);

      // since 500 ms are not passed yet, the request has not been sent and following events should be added
      send(session, '1', 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, '1', 'nodeExpanded');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls(null, 1000);

      // after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      let requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClick', 'nodeSelected', 'nodeExpanded']);
    });

    it('does not await the full delay if a subsequent send call has a smaller delay', () => {
      let session = createSession();

      // send first event delayed (in 500 ms)
      send(session, '1', 'nodeClick', {}, 500);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 100 ms
      sendQueuedAjaxCalls(null, 100);

      // since 500 ms are not passed yet, the request has not been sent and following events should be added
      send(session, '1', 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      send(session, '1', 'nodeExpanded');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls(null, 0);

      // after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      let requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClick', 'nodeSelected', 'nodeExpanded']);
    });

    it('does not await the full delay if a previous send call has a smaller delay', () => {
      let session = createSession();

      // send first event with 300ms delay
      send(session, '1', 'nodeClick', {}, 300);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // send second event with 500ms delay
      send(session, '1', 'nodeSelected', {}, 500);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 100 ms
      sendQueuedAjaxCalls(null, 100);

      // since 300 ms are not passed yet, the request has not been sent
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // tick 250 ms --> both events should now have been sent
      sendQueuedAjaxCalls(null, 250);

      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check that content is complete and in correct order
      let requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClick', 'nodeSelected']);
    });

    it('coalesces events if event provides a coalesce function', () => {
      let session = createSession();

      let coalesce = function(previous) {
        return this.target === previous.target && this.type === previous.type && this.column === previous.column;
      };

      let event0 = new RemoteEvent('1', 'columnResized', {
        column: 'a'
      });
      event0.coalesce = coalesce;
      session.sendEvent(event0);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event1 = new RemoteEvent('1', 'rowSelected');
      event1.coalesce = coalesce;
      session.sendEvent(event1);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event2 = new RemoteEvent('1', 'columnResized', {
        column: 'a'
      });
      event2.coalesce = coalesce;
      session.sendEvent(event2);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event3 = new RemoteEvent('1', 'columnResized', {
        column: 'z'
      });
      event3.coalesce = coalesce;
      session.sendEvent(event3);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // event for another target
      let event4 = new RemoteEvent('2', 'columnResized', {
        column: 'a'
      });
      event4.coalesce = coalesce;
      session.sendEvent(event4);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event5 = new RemoteEvent('1', 'columnResized', {
        column: 'a'
      });
      event5.coalesce = coalesce;
      session.sendEvent(event5);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      sendQueuedAjaxCalls();

      // after executing setTimeout there must be exactly one ajax request
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // check whether the first and second resize events were correctly removed
      let requestData = mostRecentJsonRequest();
      expect(requestData).toContainEvents([event1, event3, event4, event5]);
    });

    it('sends requests consecutively', () => {
      let session = createSession();

      // send first request
      send(session, '1', 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // trigger sending (response not received yet)
      jasmine.clock().tick(0);

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);

      send(session, '1', 'nodeClick');

      // trigger sending of second request
      jasmine.clock().tick(0);

      // second request must not be sent because first is still pending
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(true);
      expect(session.asyncEvents[0].type).toBe('nodeClick');

      // receive response for nodeSelected -> request for nodeClick gets sent
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0));
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(2);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areRequestsPending()).toBe(true);

      // receive response for nodeClick
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(1));
      jasmine.clock().tick(0);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areRequestsPending()).toBe(false);
    });

    it('sends requests consecutively and respects delay', () => {
      let session = createSession();

      // send first request
      send(session, '1', 'nodeSelected');
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // trigger sending (response not received yet)
      jasmine.clock().tick(0);

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);

      // send second request delayed
      send(session, '1', 'nodeClick', {}, 300);

      // trigger sending of second request
      jasmine.clock().tick(0);

      // second request must not be sent because first is still pending
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(true);
      expect(session.asyncEvents[0].type).toBe('nodeClick');

      // receive response for nodeSelected -> request for nodeClick does not get sent because it should be sent delayed
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0));
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(true);
      expect(session.asyncEvents[0].type).toBe('nodeClick');

      // trigger sending of second request
      jasmine.clock().tick(300);

      // now second request is sent because the time elapsed
      expect(jasmine.Ajax.requests.count()).toBe(2);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areRequestsPending()).toBe(true);

      // receive response for nodeClick
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(1));
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areRequestsPending()).toBe(false);
    });

    it('splits events into separate requests if an event requires a new request', () => {
      let session = createSession();

      let event0 = new RemoteEvent('1', 'eventType0');
      session.sendEvent(event0);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event1 = new RemoteEvent('1', 'eventType1', {
        newRequest: true
      });
      session.sendEvent(event1);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event2 = new RemoteEvent('1', 'eventType2');
      session.sendEvent(event2);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event3 = new RemoteEvent('1', 'eventType3', {
        newRequest: true
      });
      session.sendEvent(event3);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event4 = new RemoteEvent('1', 'eventType4');
      session.sendEvent(event4);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // Send first request (other requests must not be sent yet)
      jasmine.clock().tick(0);
      let request = jasmine.Ajax.requests.at(0);
      expect(JSON.parse(request.params)).toContainEvents([event0]);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(true);

      // Send second request
      receiveResponseForAjaxCall(request);
      request = jasmine.Ajax.requests.at(1);
      expect(JSON.parse(request.params)).toContainEvents([event1, event2]);
      expect(jasmine.Ajax.requests.count()).toBe(2);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(true);

      // Send last request
      receiveResponseForAjaxCall(request);
      request = jasmine.Ajax.requests.at(2);
      expect(JSON.parse(request.params)).toContainEvents([event3, event4]);
      expect(jasmine.Ajax.requests.count()).toBe(3);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(false);
      receiveResponseForAjaxCall(request);
    });

    it('does not split events into separate requests if only first request requires a new request', () => {
      let session = createSession();

      let event0 = new RemoteEvent('1', 'eventType0', {
        newRequest: true
      });
      session.sendEvent(event0);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event1 = new RemoteEvent('1', 'eventType1');
      session.sendEvent(event1);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      let event2 = new RemoteEvent('1', 'eventType2');
      session.sendEvent(event2);
      expect(jasmine.Ajax.requests.count()).toBe(0);

      // Send request
      jasmine.clock().tick(0);
      let request = jasmine.Ajax.requests.at(0);
      expect(JSON.parse(request.params)).toContainEvents([event0, event1, event2]);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(false);
    });

    it('queues ?poll results when user requests are pending', () => {
      let session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Start user request
      send(session, '1', 'nodeSelected');
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(2);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(true); // <--
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send response for ?poll request (response must be queued)
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0));
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(true);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(true); // <--
      expect(session._processSuccessResponse).not.toHaveBeenCalled();

      // Send response for user request (must be executed, including the queued response)
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(1));
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false); // <--
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false); // <--
      expect(session._processSuccessResponse).toHaveBeenCalled();
    });

    it('resumes polling after successful responses', () => {
      let session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send response for ?poll request
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: '{"events": []}'
      });
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);
      expect(session._processSuccessResponse).toHaveBeenCalled(); // <--
      expect(session._processErrorJsonResponse).not.toHaveBeenCalled();
      expect(session._processErrorResponse).not.toHaveBeenCalled();
    });

    it('does not resume polling after JS errors', () => {
      let session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send response for ?poll request
      expect(() => {
        receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
          status: 200,
          responseText: '{"events": [ { "target": "invalidTarget" } ]}' // <-- causes a JS error
        });
      }).toThrow();
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.FAILURE); // <--
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(true); // still in queue
      expect(session._processSuccessResponse).toHaveBeenCalled(); // <--
      expect(session._processErrorJsonResponse).not.toHaveBeenCalled();
      expect(session._processErrorResponse).not.toHaveBeenCalled();
    });

    it('does not resume polling after UI server errors', () => {
      let session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send "UI server" response for ?poll request
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: '{"error": true}'
      });
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.FAILURE); // <--
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);
      expect(session._processSuccessResponse).not.toHaveBeenCalled();
      expect(session._processErrorJsonResponse).toHaveBeenCalled(); // <--
      expect(session._processErrorResponse).not.toHaveBeenCalled();
    });

    it('does not resume polling after HTTP errors', () => {
      let session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send "UI server" response for ?poll request
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 404,
        responseText: 'Not found'
      });
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.FAILURE); // <--
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);
      expect(session._processSuccessResponse).not.toHaveBeenCalled();
      expect(session._processErrorJsonResponse).not.toHaveBeenCalled();
      expect(session._processErrorResponse).toHaveBeenCalled(); // <--
    });

    it('does not resume polling after session terminated', () => {
      let session = createSession();
      session.backgroundJobPollingSupport.enabled = true;
      spyOn(session, '_processSuccessResponse').and.callThrough();
      spyOn(session, '_processErrorJsonResponse').and.callThrough();
      spyOn(session, '_processErrorResponse').and.callThrough();

      // Start ?poll request
      session._resumeBackgroundJobPolling();
      jasmine.clock().tick(0);
      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);

      // Send "UI server" response for ?poll request
      receiveResponseForAjaxCall(jasmine.Ajax.requests.at(0), {
        status: 200,
        responseText: '{"sessionTerminated": true}'
      });
      jasmine.clock().tick(0);
      expect(session.backgroundJobPollingSupport.status).toBe(BackgroundJobPollingStatus.STOPPED); // <--
      expect(session.areRequestsPending()).toBe(false);
      expect(session.areEventsQueued()).toBe(false);
      expect(session.areResponsesQueued()).toBe(false);
      expect(session._processSuccessResponse).not.toHaveBeenCalled(); // <--
      expect(session._processErrorJsonResponse).not.toHaveBeenCalled();
      expect(session._processErrorResponse).not.toHaveBeenCalled();
    });
  });

  describe('init', () => {

    it('sends startup parameter', () => {
      let session = createSession();
      session.start();

      uninstallUnloadHandlers(session);
      sendQueuedAjaxCalls();

      let requestData = mostRecentJsonRequest();
      expect(requestData.startup).toBe(true);

      // don't send it on subsequent requests
      send(session, '1', 'nodeClick');
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.startup).toBeUndefined();
    });

    it('sends user agent on startup', () => {
      let session = createSession(new UserAgent({
        deviceType: Device.Type.MOBILE
      }));
      session.start();

      uninstallUnloadHandlers(session);
      sendQueuedAjaxCalls();

      let requestData = mostRecentJsonRequest();
      expect(requestData.userAgent.deviceType).toBe('MOBILE');

      // don't send it on subsequent requests
      send(session, '1', 'nodeClick');
      sendQueuedAjaxCalls();

      requestData = mostRecentJsonRequest();
      expect(requestData.userAgent).toBeUndefined();
    });

  });

  // Tests whether delegation to TextMap works as expected
  describe('texts', () => {

    let session: SandboxSession;
    let oldTextsByLocale: TextMapType;

    beforeEach(() => {
      oldTextsByLocale = texts.textsByLocale;
      session = createSession();
      let textMap = new TextMap({
        NoOptions: 'Keine Übereinstimmung',
        NumOptions: '{0} Optionen',
        Greeting: 'Hello {0}, my name is {2}, {1}.',
        Empty: '',
        Null: null
      });
      texts._setTextsByLocale({
        'de-CH': textMap
      });
      session.textMap = textMap;
    });

    afterEach(() => {
      texts._setTextsByLocale(oldTextsByLocale);
    });

    it('check if correct text is returned', () => {
      expect(session.text('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it('check if empty text is returned', () => {
      expect(session.text('Empty')).toBe('');
    });

    it('check if null text is returned', () => {
      expect(session.text('Null')).toBe(null);
    });

    it('check if arguments are replaced in text', () => {
      expect(session.text('NumOptions', 3)).toBe('3 Optionen');
    });

    it('check if multiple arguments are replaced in text', () => {
      expect(session.text('Greeting', 'Computer', 'nice to meet you', 'User')).toBe('Hello Computer, my name is User, nice to meet you.');
    });

    it('check if undefined texts return an error message', () => {
      expect(session.text('DoesNotExist')).toBe('[undefined text: DoesNotExist]');
    });

    it('optText returns undefined if key is not found', () => {
      expect(session.optText('DoesNotExist')).toBe(undefined);
    });

    it('optText returns default value if key is not found', () => {
      expect(session.optText('DoesNotExist', '#Default', 'Any argument')).toBe('#Default');
    });

    it('optText returns text if key found', () => {
      expect(session.optText('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it('optText returns text if key found, with arguments', () => {
      expect(session.optText('NumOptions', '#Default', '7')).toBe('7 Optionen');
    });

    it('switchLocale extends existing texts with new ones', () => {
      let newLocale = new LocaleSpecHelper().createLocale('de-CH');
      session.switchLocale(newLocale, new TextMap({
        NumOptions: '{0} Optionen nach Switch', // overwrites an existing one
        NewEntry: 'Neuer Eintrag nach Switch' // adds a new text
      }));

      // tests that overwritten texts use the new one
      expect(session.optText('NumOptions', '#Default', '8')).toBe('8 Optionen nach Switch');

      // tests that existing texts are preserved
      expect(session.text('NoOptions')).toBe('Keine Übereinstimmung');

      // tests that new texts are available
      expect(session.optText('NewEntry')).toBe('Neuer Eintrag nach Switch');
    });
  });
});
