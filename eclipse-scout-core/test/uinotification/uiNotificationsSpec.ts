/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BackgroundJobPollingStatus, dataObjects, dates, DoEntity, JsonObject, scout, Session, systems, UiNotificationDo, UiNotificationPoller, UiNotificationResponse, uiNotifications} from '../../src';

describe('uiNotifications', () => {

  beforeEach(() => {
    jasmine.Ajax.install();
  });

  afterEach(() => {
    uiNotifications.tearDown();
    jasmine.Ajax.uninstall();
  });

  function mostRecentRequestData() {
    return jasmine.Ajax.requests.mostRecent().data();
  }

  function pollers(): Map<string, UiNotificationPoller> {
    return new Map<string, UiNotificationPoller>(Array.from(uiNotifications.systems.entries())
      .filter(([, system]) => !!system.poller)
      .map(([name, system]) => [name, system.poller]));
  }

  describe('subscribe', () => {
    it('starts the poller if the first topic is subscribed', () => {
      expect(pollers().size).toBe(0);

      uiNotifications.subscribe('aaa', () => undefined);
      expect(pollers().size).toBe(1);

      let poller = pollers().get('main');
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);
    });

    it('registers the topics in the poller to send them as data', () => {
      uiNotifications.subscribe('aaa', () => undefined);

      let poller = pollers().get('main');
      expect(Array.from(poller.topics)).toEqual(['aaa']);
      expect(mostRecentRequestData()['topics']).toEqual([{_type: 'scout.Topic', name: 'aaa'}]);

      uiNotifications.subscribe('aaa', () => undefined);
      expect(Array.from(poller.topics)).toEqual(['aaa']); // unchanged

      uiNotifications.subscribe('bbb', () => undefined);
      expect(Array.from(poller.topics)).toEqual(['aaa', 'bbb']);
      expect(mostRecentRequestData()['topics']).toEqual([{_type: 'scout.Topic', name: 'aaa'}, {_type: 'scout.Topic', name: 'bbb'}]);
    });

    it('restarts the poller if a new topic is subscribed', () => {
      uiNotifications.subscribe('aaa', () => undefined);
      let poller = pollers().get('main');
      expect(mostRecentRequestData()['topics']).toEqual([{_type: 'scout.Topic', name: 'aaa'}]);

      poller.one('propertyChange:status', event => event.newValue === BackgroundJobPollingStatus.STOPPED);
      uiNotifications.subscribe('bbb', () => undefined);
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(mostRecentRequestData()['topics']).toEqual([{_type: 'scout.Topic', name: 'aaa'}, {_type: 'scout.Topic', name: 'bbb'}]);
    });

    it('executes the handlers for the received notifications', async () => {
      let receivedMsgAaa: DoEntity;
      uiNotifications.subscribe('aaa', event => {
        receivedMsgAaa = event.message;
      });
      let receivedMsgBbb: DoEntity;
      uiNotifications.subscribe('bbb', event => {
        receivedMsgBbb = event.message;
      });
      let receivedMsgCcc: DoEntity;
      uiNotifications.subscribe('ccc', event => {
        receivedMsgCcc = event.message;
      });

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '1',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            a: 'aaa'
          } as JsonObject
        }, {
          id: '2',
          objectType: UiNotificationDo,
          topic: 'bbb',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            b: 'bbb'
          } as JsonObject
        }, {
          id: '3',
          objectType: UiNotificationDo,
          topic: 'zzz',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            z: 'zzz'
          } as JsonObject
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);

      expect(receivedMsgAaa['a']).toBe('aaa');
      expect(receivedMsgBbb['b']).toBe('bbb');
      expect(receivedMsgCcc).toBeUndefined();
    });

    it('does not execute the handler if the notification is already known', async () => {
      let receivedMessages = [];
      uiNotifications.subscribe('aaa', event => {
        receivedMessages.push(event.message);
      });
      let poller = pollers().get('main');

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            id: '1'
          } as JsonObject
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      expect(poller.notifications.get('aaa').get('node1').length).toBe(1);
      expect(receivedMessages.length).toBe(1);

      response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            id: '1'
          } as JsonObject
        }, {
          id: '101',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            id: '2'
          } as JsonObject
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      expect(poller.notifications.get('aaa').get('node1').length).toBe(2);
      expect(receivedMessages.length).toBe(2);
      expect(receivedMessages[0].id).toBe('1');
      expect(receivedMessages[1].id).toBe('2');
    });

    it('does not execute the handlers for the start subscription notification', async () => {
      let receivedMsg: DoEntity;
      uiNotifications.subscribe('aaa', event => {
        receivedMsg = event.message;
      });

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:13.000'),
          subscriptionStart: true
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      // New request started with lastNotifications set
      expect(mostRecentRequestData()['topics']).toEqual([{
        _type: 'scout.Topic',
        name: 'aaa', lastNotifications: [
          {_type: 'scout.UiNotification', id: '100', creationTime: '2023-09-16 21:44:13.000', nodeId: 'node1'}
        ]
      }]);

      expect(receivedMsg).toBeUndefined();
    });

    it('can handle subscription and regular notifications in same response', async () => {
      let receivedMessages = [];
      uiNotifications.subscribe('aaa', event => {
        receivedMessages.push(event.message);
      });

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          subscriptionStart: true
        }, {
          id: '101',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            a: '1'
          } as JsonObject
        }, {
          id: '102',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            a: '2'
          } as JsonObject
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);

      expect(receivedMessages.length).toBe(2);
      expect(receivedMessages[0]['a']).toBe('1');
      expect(receivedMessages[1]['a']).toBe('2');
    });

    it('resolves the return value as soon as the subscriptionStart notification arrives', done => {
      uiNotifications.subscribe('aaa', () => undefined).then(topic => {
        expect(topic).toEqual('aaa');
        done();
      });

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:13.000'),
          subscriptionStart: true
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });
    });

    it('does not resolve the return value if the subscriptionStart for another topic arrives', async () => {
      let called = false;

      uiNotifications.subscribe('aaa', () => undefined).then(topic => {
        called = false;
      });

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'bbb',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:13.000'),
          subscriptionStart: true
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(50);
      expect(called).toBe(false); // Still false
    });

    it('is resolved if there are already notifications in the history', async () => {
      uiNotifications.subscribe('aaa', () => undefined);

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:13.000'),
          subscriptionStart: true
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      const topic = await uiNotifications.subscribe('aaa', () => undefined);
      expect(topic).toEqual('aaa');
    });

    it('starts a poller per system', () => {
      systems.getOrCreate('sys2');
      expect(pollers().size).toBe(0);

      uiNotifications.subscribe('aaa', () => undefined);
      expect(pollers().size).toBe(1);

      let poller = pollers().get('main');
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);

      uiNotifications.subscribe('bbb', () => undefined, 'sys2');
      expect(pollers().size).toBe(2);

      let poller2 = pollers().get('sys2');
      expect(poller2.status).toBe(BackgroundJobPollingStatus.RUNNING);
      expect(poller2).not.toBe(poller);

      uiNotifications.subscribe('ccc', () => undefined, 'sys2');
      expect(pollers().size).toBe(2); // Still 2
    });

    it('automatically registers system on subscribe', () => {
      expect(pollers().size).toBe(0);
      expect(() => uiNotifications.subscribe('aaa', () => undefined, 'unknown_system')).not.toBeFalsy();
    });

    it('sends the last received notifications per topic and node', async () => {
      uiNotifications.subscribe('aaa', () => undefined);
      expect(mostRecentRequestData()['topics']).toEqual([{_type: 'scout.Topic', name: 'aaa'}]);

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '1',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:13.000'),
          message: {}
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      expect(mostRecentRequestData()['topics']).toEqual([{
        _type: 'scout.Topic',
        name: 'aaa',
        lastNotifications: [
          {_type: 'scout.UiNotification', id: '1', creationTime: '2023-09-16 21:44:13.000', nodeId: 'node1'}
        ]
      }]);

      uiNotifications.subscribe('bbb', () => undefined);
      expect(mostRecentRequestData()['topics']).toEqual([{
        _type: 'scout.Topic',
        name: 'aaa',
        lastNotifications: [
          {_type: 'scout.UiNotification', id: '1', creationTime: '2023-09-16 21:44:13.000', nodeId: 'node1'}
        ]
      }, {
        _type: 'scout.Topic',
        name: 'bbb'
      }]);

      let response2 = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '4',
          objectType: UiNotificationDo,
          topic: 'bbb',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:13.000'),
          message: {}
        }, {
          id: '5',
          objectType: UiNotificationDo,
          topic: 'bbb',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:50.000'),
          message: {}
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response2)
      });

      await sleep(1);
      expect(mostRecentRequestData()['topics']).toEqual([{
        _type: 'scout.Topic',
        name: 'aaa', lastNotifications: [{
          _type: 'scout.UiNotification', id: '1', creationTime: '2023-09-16 21:44:13.000', nodeId: 'node1'
        }]
      }, {
        _type: 'scout.Topic',
        name: 'bbb',
        lastNotifications: [{
          _type: 'scout.UiNotification', id: '5', creationTime: '2023-09-16 21:44:50.000', nodeId: 'node1'
        }]
      }]);

      let response3 = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '7',
          objectType: UiNotificationDo,
          topic: 'bbb',
          nodeId: 'node2',
          creationTime: dates.parseJsonDate('2023-09-16 21:45:02.000'),
          message: {}
        }, {
          id: '8',
          objectType: UiNotificationDo,
          topic: 'bbb',
          nodeId: 'node2',
          creationTime: dates.parseJsonDate('2023-09-16 21:46:10.000'),
          message: {}
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response3)
      });

      await sleep(1);
      expect(mostRecentRequestData()['topics']).toEqual([{
        _type: 'scout.Topic',
        name: 'aaa', lastNotifications: [{
          _type: 'scout.UiNotification', id: '1', creationTime: '2023-09-16 21:44:13.000', nodeId: 'node1'
        }]
      }, {
        _type: 'scout.Topic',
        name: 'bbb',
        lastNotifications: [{
          _type: 'scout.UiNotification', id: '5', creationTime: '2023-09-16 21:44:50.000', nodeId: 'node1'
        }, {
          _type: 'scout.UiNotification', id: '8', creationTime: '2023-09-16 21:46:10.000', nodeId: 'node2'
        }]
      }]);
    });

    it('stores the notifications in a history sorted by creation time and grouped by node', async () => {
      jasmine.clock().uninstall();
      uiNotifications.subscribe('aaa', () => undefined);
      let poller = pollers().get('main');
      expect(poller.notifications.get('aaa').get('node1')).toBeUndefined();

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '1',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:50.000'),
          message: {}
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      expect(poller.notifications.get('aaa').get('node1').length).toBe(1);
      expect(poller.notifications.get('aaa').get('node1')[0].id).toBe('1');

      let response2 = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '2',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:43:50.000'),
          message: {}
        }, {
          id: '3',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node2',
          creationTime: dates.parseJsonDate('2023-09-16 22:10:00.000'),
          message: {}
        }, {
          id: '4',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node2',
          creationTime: dates.parseJsonDate('2023-09-16 22:09:00.000'),
          message: {}
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response2)
      });

      await sleep(1);
      expect(poller.notifications.get('aaa').get('node1').length).toBe(2);
      expect(poller.notifications.get('aaa').get('node1')[0].id).toBe('2');
      expect(poller.notifications.get('aaa').get('node1')[1].id).toBe('1'); // 1 is newer
      expect(poller.notifications.get('aaa').get('node2').length).toBe(2);
      expect(poller.notifications.get('aaa').get('node2')[0].id).toBe('4');
      expect(poller.notifications.get('aaa').get('node2')[1].id).toBe('3'); // 3 is newer
    });

    it('restrains the size of the notification history', async () => {
      jasmine.clock().uninstall();
      uiNotifications.subscribe('aaa', () => undefined);
      let poller = pollers().get('main');
      expect(poller.notifications.get('aaa').get('node1')).toBeUndefined();

      let response = scout.create(UiNotificationResponse, {
        notifications: (createNotifications(1, UiNotificationPoller.HISTORY_COUNT + 5))
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      expect(poller.notifications.get('aaa').get('node1').length).toBe(UiNotificationPoller.HISTORY_COUNT);
      expect(poller.notifications.get('aaa').get('node1')[0].id).toBe('6');
      expect(poller.notifications.get('aaa').get('node1')[9].id).toBe('15');

      response = scout.create(UiNotificationResponse, {
        notifications: (createNotifications(UiNotificationPoller.HISTORY_COUNT + 6, 3))
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      expect(poller.notifications.get('aaa').get('node1').length).toBe(UiNotificationPoller.HISTORY_COUNT);
      expect(poller.notifications.get('aaa').get('node1')[0].id).toBe('9');
      expect(poller.notifications.get('aaa').get('node1')[9].id).toBe('18');
    });

    function createNotifications(start: number, count: number): UiNotificationDo[] {
      let notifications = [];
      for (let i = start; i < start + count; i++) {
        notifications.push(scout.create(UiNotificationDo, {
          id: i + '',
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            id: i
          } as JsonObject
        }));
      }
      return notifications;
    }
  });

  describe('unsubscribe', () => {
    it('stops the poller if the last topic is unsubscribed', () => {
      expect(pollers().size).toBe(0);

      let aaaHandler = () => undefined;
      uiNotifications.subscribe('aaa', aaaHandler);
      expect(pollers().size).toBe(1);
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let poller = pollers().get('main');
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);

      let bbbHandler = () => undefined;
      uiNotifications.subscribe('bbb', bbbHandler);
      expect(pollers().size).toBe(1); // still 1
      expect(jasmine.Ajax.requests.count()).toBe(2); // Existing request is canceled and a new one started

      uiNotifications.unsubscribe('aaa', aaaHandler);
      expect(pollers().size).toBe(1); // still 1
      expect(jasmine.Ajax.requests.count()).toBe(3); // Existing request is canceled and a new one started

      uiNotifications.unsubscribe('bbb', bbbHandler);
      expect(pollers().size).toBe(0);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);
      expect(jasmine.Ajax.requests.count()).toBe(3); // No new poll request must be started

      uiNotifications.unsubscribe('bbb', bbbHandler); // should not fail if already unsubscribed
    });

    it('stops the poller even if "one" was used to subscribe', () => {
      expect(pollers().size).toBe(0);

      let aaaHandler = () => undefined;
      uiNotifications.subscribeOne('aaa', aaaHandler);
      let poller = pollers().values().next().value;
      expect(pollers().size).toBe(1);
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);

      uiNotifications.unsubscribe('aaa', aaaHandler);
      expect(pollers().size).toBe(0);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);
      expect(jasmine.Ajax.requests.count()).toBe(1); // No new poll request must be started
    });

    it('stops the poller even if unsubscribe was called right after subscriptionStart', async () => {
      expect(pollers().size).toBe(0);

      let aaaHandler = () => undefined;
      uiNotifications.subscribe('aaa', aaaHandler);
      let poller = pollers().values().next().value;
      expect(pollers().size).toBe(1);
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:13.000'),
          subscriptionStart: true
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      // Execute UiNotificationPoller._onSuccess handler which schedules a new poll request but don't execute scheduled poll yet (setTimeout / sleep would do that)
      await Promise.resolve().then();
      uiNotifications.unsubscribe('aaa', aaaHandler);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);
      expect(pollers().size).toBe(0);
      expect(jasmine.Ajax.requests.count()).toBe(1); // No new poll request must be started
    });

    it('unregisters the topics in the poller to not send them as data anymore', () => {
      let aaaHandler = () => undefined;
      uiNotifications.subscribe('aaa', aaaHandler);

      let poller = pollers().get('main');
      expect(Array.from(poller.topics)).toEqual(['aaa']);
      expect(mostRecentRequestData()['topics']).toEqual([{_type: 'scout.Topic', name: 'aaa'}]);

      let bbbHandler = () => undefined;
      uiNotifications.subscribe('bbb', bbbHandler);
      expect(Array.from(poller.topics)).toEqual(['aaa', 'bbb']);
      expect(mostRecentRequestData()['topics']).toEqual([{_type: 'scout.Topic', name: 'aaa'}, {_type: 'scout.Topic', name: 'bbb'}]);

      uiNotifications.unsubscribe('bbb', bbbHandler);
      expect(Array.from(poller.topics)).toEqual(['aaa']);
      expect(mostRecentRequestData()['topics']).toEqual([{_type: 'scout.Topic', name: 'aaa'}]);

      uiNotifications.unsubscribe('aaa', aaaHandler);
      expect(Array.from(poller.topics)).toEqual([]);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);
    });

    it('removes the notifications from the history', async () => {
      let aaaHandler = () => undefined;
      uiNotifications.subscribe('aaa', aaaHandler);
      let bbbHandler = () => undefined;
      uiNotifications.subscribe('bbb', bbbHandler);

      let poller = pollers().get('main');
      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '1',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 20:41:50.000'),
          message: {}
        }, {
          id: '2',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node2',
          creationTime: dates.parseJsonDate('2023-09-16 20:42:00.000'),
          message: {}
        }, {
          id: '3',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node2',
          creationTime: dates.parseJsonDate('2023-09-16 22:00:00.000'),
          message: {}
        }, {
          id: '4',
          objectType: UiNotificationDo,
          topic: 'bbb',
          nodeId: 'node2',
          creationTime: dates.parseJsonDate('2023-09-16 22:00:00.000'),
          message: {}
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      expect(poller.notifications.get('aaa').get('node1').length).toBe(1);
      expect(poller.notifications.get('aaa').get('node1')[0].id).toBe('1');
      expect(poller.notifications.get('aaa').get('node2').length).toBe(2);
      expect(poller.notifications.get('aaa').get('node2')[0].id).toBe('2');
      expect(poller.notifications.get('aaa').get('node2')[1].id).toBe('3');
      expect(poller.notifications.get('bbb').get('node2').length).toBe(1);
      expect(poller.notifications.get('bbb').get('node2')[0].id).toBe('4');

      uiNotifications.unsubscribe('aaa', aaaHandler);
      expect(poller.notifications.get('aaa')).toBeUndefined();
      expect(poller.notifications.get('bbb').get('node2').length).toBe(1);

      uiNotifications.unsubscribe('bbb', bbbHandler);
      expect(poller.notifications.get('bbb')).toBeUndefined();
      expect(poller.notifications.size).toBe(0);
    });
  });

  describe('one', () => {
    it('subscribes for one notification and automatically unsubscribes once it arrives', async () => {
      let receivedMsg: DoEntity;
      uiNotifications.subscribeOne('aaa', event => {
        receivedMsg = event.message;
      });
      expect(pollers().size).toBe(1);
      const poller = pollers().values().next().value;
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '1',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: new Date(),
          message: {
            a: 'aaa'
          } as JsonObject
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      await sleep(1);
      expect(receivedMsg['a']).toBe('aaa');
      expect(pollers().size).toBe(0);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);
    });
  });

  describe('poller', () => {
    beforeEach(() => {
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('automatically restarts on error', () => {
      uiNotifications.subscribe('aaa', () => undefined);
      let poller = pollers().get('main');
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 500
      });
      jasmine.clock().tick(1);
      expect(poller.status).toBe(BackgroundJobPollingStatus.FAILURE);

      jasmine.clock().tick(UiNotificationPoller.RETRY_INTERVAL + 1000);
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);
    });

    it('does not restart if operation is not allowed', () => {
      uiNotifications.subscribe('aaa', () => undefined);
      let poller = pollers().get('main');
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 403
      });
      jasmine.clock().tick(1);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);

      jasmine.clock().tick(UiNotificationPoller.RETRY_INTERVAL + 1000);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED); // still stopped
    });

    it('does not restart on session timeout', () => {
      uiNotifications.subscribe('aaa', () => undefined);
      let poller = pollers().get('main');
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: JSON.stringify({error: {code: Session.JsonResponseError.SESSION_TIMEOUT}})
      });
      jasmine.clock().tick(1);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);

      jasmine.clock().tick(UiNotificationPoller.RETRY_INTERVAL + 1000);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED); // still stopped
    });

    it('does not process any notification if stopped right before notification arrives', () => {
      let receivedEvent;
      uiNotifications.subscribe('aaa', event => {
        receivedEvent = event;
      });
      let poller = pollers().get('main');

      let response = scout.create(UiNotificationResponse, {
        notifications: [{
          id: '100',
          objectType: UiNotificationDo,
          topic: 'aaa',
          nodeId: 'node1',
          creationTime: dates.parseJsonDate('2023-09-16 21:44:13.000')
        }]
      });
      jasmine.Ajax.requests.mostRecent().respondWith({
        status: 200,
        responseText: dataObjects.stringify(response)
      });

      poller.stop();
      jasmine.clock().tick(1);
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);
      expect(receivedEvent).toBeUndefined();
    });
  });

  describe('tearDown', () => {
    it('stops every poller', () => {
      uiNotifications.subscribe('aaa', () => undefined);
      let poller = pollers().get('main');
      expect(poller.status).toBe(BackgroundJobPollingStatus.RUNNING);

      uiNotifications.subscribe('bbb', () => undefined, 'sys2');
      let poller2 = pollers().get('sys2');
      expect(poller2.status).toBe(BackgroundJobPollingStatus.RUNNING);

      uiNotifications.tearDown();
      expect(poller.status).toBe(BackgroundJobPollingStatus.STOPPED);
      expect(poller2.status).toBe(BackgroundJobPollingStatus.STOPPED);
    });
  });
});
