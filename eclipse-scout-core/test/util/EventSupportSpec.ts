/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, EventListener, EventSupport} from '../../src/index';

describe('EventSupport', () => {

  let count: number, events: SpecEventSupport;

  class SpecEventSupport extends EventSupport {
    declare _eventListeners: EventListener[];
  }

  function fooListener() {
    count++;
  }

  describe('on / trigger / off', () => {

    beforeEach(() => {
      events = new SpecEventSupport();
      count = 0;
    });

    it('single event func only triggered until off() is called', () => {
      events.on('foo', fooListener);
      events.trigger('foo');
      expect(count).toBe(1);
      // after 'off' listener shall not be triggered anymore
      events.off('foo', fooListener);
      events.trigger('foo');
      expect(count).toBe(1);
    });

    it('multiple events', () => {
      events.on('foo bar', fooListener);
      events.trigger('foo');
      events.trigger('bar');
      expect(count).toBe(2);
    });

  });

  describe('one', () => {

    beforeEach(() => {
      events = new SpecEventSupport();
      count = 0;
    });

    it('single event func only triggered once when registered with one()', () => {
      events.one('foo', fooListener);
      events.trigger('foo');
      events.trigger('foo');
      expect(count).toBe(1);
      expect(events._eventListeners.length).toBe(0);
    });

    it('event parameter passed to registered func', () => {
      let receivedEvent = null;
      events.one('foo', event => {
        receivedEvent = event;
      });
      events.trigger('foo', new Event({
        theProp: 'bar'
      }));
      // expect the event has been passed to the registered func
      expect(receivedEvent.theProp).toBe('bar');
      // expect the type property is automatically set by EventSupport
      expect(receivedEvent.type).toBe('foo');
    });

    it('de-register function registered with one()', () => {
      events.one('foo', fooListener);
      expect(events._eventListeners.length).toBe(1);
      events.off('foo', fooListener);
      expect(events._eventListeners.length).toBe(0);
    });

  });

  describe('off', () => {

    function fooListener1() {
      count++;
    }

    function fooListener2() {
      count++;
    }

    function fooListener3() {
      count++;
    }

    function fooListener4() {
      count++;
    }

    function fooListener5() {
      count++;
    }

    function fooListener6() {
      count++;
    }

    function fooListener7() {
      count++;
    }

    beforeEach(() => {
      events = new SpecEventSupport();
      count = 0;
    });

    it('remove all with same type', () => {
      events.one('foo', fooListener);
      events.one('foo', fooListener1);
      events.one('asdf', fooListener2);
      events.one('asdf', fooListener3);
      events.one('asdf', fooListener4);
      events.one('asdf', fooListener5);
      events.one('asdf', fooListener6);
      events.one('asdf', fooListener7);
      events.off('foo');
      expect(events._eventListeners.length).toBe(6);
    });

    it('remove specific listener', () => {
      events.one('foo', fooListener);
      events.one('foo', fooListener1);
      events.one('asdf', fooListener2);
      events.one('asdf', fooListener3);
      events.one('asdf', fooListener4);
      events.one('asdf', fooListener5);
      events.one('asdf', fooListener6);
      events.one('asdf', fooListener7);
      events.off('foo', fooListener);
      expect(events._eventListeners.length).toBe(7);
    });
  });
});
