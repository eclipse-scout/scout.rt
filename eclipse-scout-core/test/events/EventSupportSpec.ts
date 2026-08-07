/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, EventSupport, scout} from '../../src/index';

describe('EventSupport', () => {

  let count: number, events: EventSupport;

  function fooListener() {
    count++;
  }

  describe('on / trigger / off', () => {

    beforeEach(() => {
      events = new EventSupport();
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

  describe('on', () => {

    it('adds handler for given type', () => {
      const eventSupport = new EventSupport();
      const handler = () => {
      };

      expect(eventSupport.count(null, handler)).toBe(0);

      eventSupport.on('foo', handler);
      expect(eventSupport.count(null, handler)).toBe(1);

      eventSupport.on('bar', handler);
      expect(eventSupport.count(null, handler)).toBe(2);

      eventSupport.on('lorem ipsum dolor', handler);
      expect(eventSupport.count(null, handler)).toBe(3);
    });

    it('does not remove handler after event is triggered', () => {
      const eventSupport = new EventSupport();
      const handler = () => {
      };

      eventSupport.on('foo', handler);
      eventSupport.on('bar', handler);
      eventSupport.on('lorem ipsum dolor', handler);
      expect(eventSupport.count(null, handler)).toBe(3);

      eventSupport.trigger('foo');
      eventSupport.trigger('foo');
      eventSupport.trigger('bar');
      eventSupport.trigger('ipsum');
      expect(eventSupport.count(null, handler)).toBe(3);
    });

    it('adds a handler twice', () => {
      const eventSupport = new EventSupport();
      const handler = () => {
      };

      expect(eventSupport.count(null, handler)).toBe(0);

      eventSupport.on('foo', handler);
      expect(eventSupport.count(null, handler)).toBe(1);

      eventSupport.on('foo', handler);
      expect(eventSupport.count(null, handler)).toBe(2);

      eventSupport.on('lorem ipsum dolor', handler);
      expect(eventSupport.count(null, handler)).toBe(3);

      eventSupport.on('lorem ipsum dolor', handler);
      expect(eventSupport.count(null, handler)).toBe(4);

      eventSupport.on('lorem', handler);
      expect(eventSupport.count(null, handler)).toBe(5);
    });
  });

  describe('one', () => {

    beforeEach(() => {
      events = new EventSupport();
      count = 0;
    });

    it('single event func only triggered once when registered with one()', () => {
      events.one('foo', fooListener);
      events.trigger('foo');
      events.trigger('foo');
      expect(count).toBe(1);
      expect(events.count()).toBe(0);
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
      expect(events.count()).toBe(1);
      events.off('foo', fooListener);
      expect(events.count()).toBe(0);
    });

    it('adds handler for given type', () => {
      const eventSupport = new EventSupport();
      const handler = () => {
      };

      expect(eventSupport.count(null, handler)).toBe(0);

      eventSupport.one('foo', handler);
      expect(eventSupport.count(null, handler)).toBe(1);

      eventSupport.one('bar', handler);
      expect(eventSupport.count(null, handler)).toBe(2);

      eventSupport.one('lorem ipsum dolor', handler);
      expect(eventSupport.count(null, handler)).toBe(3);
    });

    it('removes handler after event is triggered', () => {
      const eventSupport = new EventSupport();
      const handler = () => {
      };

      eventSupport.one('foo', handler);
      eventSupport.one('bar', handler);
      eventSupport.one('lorem ipsum dolor', handler);
      expect(eventSupport.count(null, handler)).toBe(3);

      eventSupport.trigger('foo');
      expect(eventSupport.count(null, handler)).toBe(2);

      eventSupport.trigger('foo');
      expect(eventSupport.count(null, handler)).toBe(2);

      eventSupport.trigger('bar');
      expect(eventSupport.count(null, handler)).toBe(1);

      // removes all events that were registered together
      eventSupport.trigger('ipsum');
      expect(eventSupport.count(null, handler)).toBe(0);
    });

    it('adds a handler twice', () => {
      const eventSupport = new EventSupport();
      const handler = () => {
      };

      expect(eventSupport.count(null, handler)).toBe(0);

      eventSupport.one('foo', handler);
      expect(eventSupport.count(null, handler)).toBe(1);

      eventSupport.one('foo', handler);
      expect(eventSupport.count(null, handler)).toBe(2);

      eventSupport.one('lorem ipsum dolor', handler);
      expect(eventSupport.count(null, handler)).toBe(3);

      eventSupport.one('lorem ipsum dolor', handler);
      expect(eventSupport.count(null, handler)).toBe(4);

      eventSupport.one('lorem', handler);
      expect(eventSupport.count(null, handler)).toBe(5);
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
      events = new EventSupport();
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
      expect(events.count()).toBe(6);
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
      expect(events.count()).toBe(7);
    });

    it('removes handler', () => {
      const eventSupport = new EventSupport();
      const handler1 = () => {
      };
      const handler2 = () => {
      };

      eventSupport.on('foo', handler1);
      eventSupport.on('foo', handler2);
      eventSupport.on('bar', handler1);
      eventSupport.on('bar', handler2);
      eventSupport.on('lorem ipsum dolor', handler1);
      eventSupport.on('lorem', handler1);
      eventSupport.on('lorem', handler1);
      eventSupport.on('lorem', handler1);
      eventSupport.on('dolor', handler1);

      expect(eventSupport.count(null, handler1)).toBe(7);
      expect(eventSupport.count(null, handler2)).toBe(2);
      expect(eventSupport.count('foo', handler1)).toBe(1);
      expect(eventSupport.count('foo', handler2)).toBe(1);
      expect(eventSupport.count('bar', handler1)).toBe(1);
      expect(eventSupport.count('bar', handler2)).toBe(1);
      expect(eventSupport.count('lorem ipsum dolor', handler1)).toBe(1);
      expect(eventSupport.count('lorem', handler1)).toBe(4);
      expect(eventSupport.count('ipsum', handler1)).toBe(1);
      expect(eventSupport.count('dolor', handler1)).toBe(2);

      eventSupport.off('foo', handler1);
      expect(eventSupport.count(null, handler1)).toBe(6);
      expect(eventSupport.count(null, handler2)).toBe(2);
      expect(eventSupport.count('foo', handler1)).toBe(0);
      expect(eventSupport.count('foo', handler2)).toBe(1);

      eventSupport.off('foo', handler1);
      expect(eventSupport.count(null, handler1)).toBe(6);
      expect(eventSupport.count(null, handler2)).toBe(2);

      eventSupport.off('bar');
      expect(eventSupport.count(null, handler1)).toBe(5);
      expect(eventSupport.count(null, handler2)).toBe(1);
      expect(eventSupport.count('bar', handler1)).toBe(0);
      expect(eventSupport.count('bar', handler2)).toBe(0);

      eventSupport.off('ipsum', handler1);
      expect(eventSupport.count(null, handler1)).toBe(5);
      expect(eventSupport.count('lorem ipsum dolor', handler1)).toBe(1);
      expect(eventSupport.count('lorem', handler1)).toBe(4);
      expect(eventSupport.count('ipsum', handler1)).toBe(1);
      expect(eventSupport.count('dolor', handler1)).toBe(2);

      eventSupport.off('ipsum');
      expect(eventSupport.count(null, handler1)).toBe(5);
      expect(eventSupport.count('lorem ipsum dolor', handler1)).toBe(1);
      expect(eventSupport.count('lorem', handler1)).toBe(4);
      expect(eventSupport.count('ipsum', handler1)).toBe(1);
      expect(eventSupport.count('dolor', handler1)).toBe(2);

      // type need to match exactly
      eventSupport.off('ipsum lorem dolor', handler1);
      expect(eventSupport.count(null, handler1)).toBe(5);
      expect(eventSupport.count('lorem ipsum dolor', handler1)).toBe(1);
      expect(eventSupport.count('lorem', handler1)).toBe(4);
      expect(eventSupport.count('ipsum', handler1)).toBe(1);
      expect(eventSupport.count('dolor', handler1)).toBe(2);

      eventSupport.off('lorem ipsum dolor', handler1);
      expect(eventSupport.count(null, handler1)).toBe(4);
      expect(eventSupport.count('lorem ipsum dolor', handler1)).toBe(0);
      expect(eventSupport.count('lorem', handler1)).toBe(3);
      expect(eventSupport.count('ipsum', handler1)).toBe(0);
      expect(eventSupport.count('dolor', handler1)).toBe(1);

      eventSupport.off(null, handler1);
      expect(eventSupport.count(null, handler1)).toBe(0);
      expect(eventSupport.count('lorem', handler1)).toBe(0);
      expect(eventSupport.count('dolor', handler1)).toBe(0);
    });
  });

  describe('when', () => {

    beforeEach(() => {
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('is resolved on the next event', () => {
      const eventSupport = new EventSupport();
      let fooCount = 0;
      let barCount = 0;

      eventSupport.trigger('foo');
      eventSupport.trigger('bar');
      jasmine.clock().tick(10);
      expect(fooCount).toBe(0);
      expect(barCount).toBe(0);

      eventSupport.when('foo').then(() => fooCount++);
      eventSupport.when('bar').then(() => barCount++);

      eventSupport.trigger('foo');
      jasmine.clock().tick(10);
      expect(fooCount).toBe(1);
      expect(barCount).toBe(0);

      eventSupport.trigger('bar');
      jasmine.clock().tick(10);
      expect(fooCount).toBe(1);
      expect(barCount).toBe(1);

      eventSupport.trigger('foo');
      eventSupport.trigger('bar');
      jasmine.clock().tick(10);
      expect(fooCount).toBe(1);
      expect(barCount).toBe(1);

      eventSupport.when('foo').then(() => fooCount++);
      eventSupport.when('bar').then(() => barCount++);

      eventSupport.trigger('foo');
      eventSupport.trigger('bar');
      jasmine.clock().tick(10);
      expect(fooCount).toBe(2);
      expect(barCount).toBe(2);
    });
  });

  describe('addListener', () => {

    it('adds listener if it does not exist', () => {
      const eventSupport = new EventSupport();
      const listener1 = {
        func: () => {
        }
      };
      const listener2 = {
        type: 'foo',
        func: () => {
        }
      };
      const listener3 = {
        type: 'lorem ipsum dolor',
        func: () => {
        }
      };

      expect(eventSupport.count()).toBe(0);

      eventSupport.addListener(listener1);
      expect(eventSupport.count()).toBe(1);

      eventSupport.addListener(listener1);
      expect(eventSupport.count()).toBe(1);

      eventSupport.addListener(listener2);
      expect(eventSupport.count()).toBe(2);

      eventSupport.addListener(listener2);
      expect(eventSupport.count()).toBe(2);

      eventSupport.addListener(listener3);
      expect(eventSupport.count()).toBe(3);

      eventSupport.addListener(listener3);
      expect(eventSupport.count()).toBe(3);

      // new instance
      eventSupport.addListener({...listener3});
      expect(eventSupport.count()).toBe(4);
    });
  });

  describe('removeListener', () => {

    it('removes listener if it does exist', () => {
      const eventSupport = new EventSupport();
      const listener1 = {
        func: () => {
        }
      };
      const listener2 = {
        type: 'foo',
        func: () => {
        }
      };
      const listener3 = {
        type: 'lorem ipsum dolor',
        func: () => {
        }
      };

      eventSupport.addListener(listener1);
      eventSupport.addListener(listener2);
      eventSupport.addListener(listener3);
      eventSupport.addListener({...listener3});

      expect(eventSupport.count()).toBe(4);

      eventSupport.removeListener(listener1);
      expect(eventSupport.count()).toBe(3);

      eventSupport.removeListener(listener1);
      expect(eventSupport.count()).toBe(3);

      eventSupport.removeListener(listener2);
      expect(eventSupport.count()).toBe(2);

      eventSupport.removeListener(listener2);
      expect(eventSupport.count()).toBe(2);

      eventSupport.removeListener(listener3);
      expect(eventSupport.count()).toBe(1);

      eventSupport.removeListener(listener3);
      expect(eventSupport.count()).toBe(1);

      eventSupport.removeListener({...listener3});
      expect(eventSupport.count()).toBe(1);
    });
  });

  describe('count', () => {

    it('gives total count if no argument is provided', () => {
      const eventSupport = new EventSupport();
      const handler1 = () => {
      };
      const handler2 = () => {
      };

      eventSupport.on('foo', handler1);
      eventSupport.one('foo', handler2);

      eventSupport.on('', handler2);
      eventSupport.on(null, handler2);

      eventSupport.on('lorem ipsum dolor', handler1);
      eventSupport.on('ipsum', handler2);

      expect(eventSupport.count()).toBe(6);
    });

    it('filters by type if type argument is provided', () => {
      const eventSupport = new EventSupport();
      const handler1 = () => {
      };
      const handler2 = () => {
      };

      eventSupport.on('foo', handler1);
      eventSupport.one('foo', handler2);

      eventSupport.on('', handler2);
      eventSupport.on(null, handler2);

      eventSupport.on('lorem ipsum dolor', handler1);
      eventSupport.on('ipsum', handler2);

      expect(eventSupport.count('foo')).toBe(2);
      expect(eventSupport.count('bar')).toBe(0);
      expect(eventSupport.count('lorem ipsum dolor')).toBe(1);
      expect(eventSupport.count('lorem')).toBe(1);
      expect(eventSupport.count('ipsum')).toBe(2);
      expect(eventSupport.count('dolor')).toBe(1);
    });

    it('filters by func if func argument is provided', () => {
      const eventSupport = new EventSupport();
      const handler1 = () => {
      };
      const handler2 = () => {
      };

      eventSupport.on('foo', handler1);
      eventSupport.one('foo', handler2);

      eventSupport.on('', handler2);
      eventSupport.on(null, handler2);

      eventSupport.on('lorem ipsum dolor', handler1);
      eventSupport.on('ipsum', handler2);

      expect(eventSupport.count(null, handler1)).toBe(2);
      expect(eventSupport.count(null, handler2)).toBe(4);
    });

    it('filters by type and func if both argument are provided', () => {
      const eventSupport = new EventSupport();
      const handler1 = () => {
      };
      const handler2 = () => {
      };

      eventSupport.on('foo', handler1);
      eventSupport.one('foo', handler2);

      eventSupport.on('', handler2);
      eventSupport.on(null, handler2);

      eventSupport.on('lorem ipsum dolor', handler1);
      eventSupport.on('ipsum', handler2);

      expect(eventSupport.count('foo', handler1)).toBe(1);
      expect(eventSupport.count('foo', handler2)).toBe(1);
      expect(eventSupport.count('bar', handler1)).toBe(0);
      expect(eventSupport.count('bar', handler2)).toBe(0);
      expect(eventSupport.count('lorem', handler1)).toBe(1);
      expect(eventSupport.count('lorem', handler2)).toBe(0);
      expect(eventSupport.count('ipsum', handler1)).toBe(1);
      expect(eventSupport.count('ipsum', handler2)).toBe(1);
      expect(eventSupport.count('dolor', handler1)).toBe(1);
      expect(eventSupport.count('dolor', handler2)).toBe(0);
    });
  });

  describe('types', () => {

    it('returns a distinct list of types', () => {
      const eventSupport = new EventSupport();
      const handler1 = () => {
      };
      const handler2 = () => {
      };

      eventSupport.on('foo', handler1);
      eventSupport.one('foo', handler2);
      expect(eventSupport.types()).toEqual(['foo']);

      eventSupport.on('', handler2);
      expect(eventSupport.types()).toEqual(['foo']);

      eventSupport.on(null, handler2);
      expect(eventSupport.types()).toEqual(['foo']);

      eventSupport.on('lorem ipsum dolor', handler1);
      expect(eventSupport.types()).toEqual(['foo', 'lorem', 'ipsum', 'dolor']);

      eventSupport.on('ipsum', handler2);
      expect(eventSupport.types()).toEqual(['foo', 'lorem', 'ipsum', 'dolor']);
    });
  });

  describe('trigger', () => {

    it('triggers each listener once', () => {
      const eventSupport = new EventSupport();
      let count1 = 0;
      let count2 = 0;
      const handler1 = () => count1++;
      const handler2 = () => count2++;

      eventSupport.on('foo', handler1);
      eventSupport.on('foo', handler2);

      eventSupport.on('bar', handler2);

      eventSupport.on('lorem ipsum dolor', handler1);
      eventSupport.on('lorem', handler1);
      eventSupport.on('ipsum dolor', handler2);

      expect(count1).toBe(0);
      expect(count2).toBe(0);

      eventSupport.trigger('foo');
      expect(count1).toBe(1);
      expect(count2).toBe(1);

      eventSupport.trigger('bar');
      expect(count1).toBe(1);
      expect(count2).toBe(2);

      eventSupport.trigger('lorem');
      expect(count1).toBe(3);
      expect(count2).toBe(2);

      eventSupport.trigger('ipsum');
      expect(count1).toBe(4);
      expect(count2).toBe(3);

      eventSupport.trigger('dolor');
      expect(count1).toBe(5);
      expect(count2).toBe(4);
    });

    it('triggers listeners in order they aer registered', () => {
      const eventSupport = new EventSupport();
      eventSupport.registerSubTypeProvider('fancy', (event: FancyEvent) => event.subType);
      let listenersTriggered = [];

      const handlerA = () => listenersTriggered.push('a');
      const handlerB = () => listenersTriggered.push('b');
      const handlerC = () => listenersTriggered.push('c');
      const handlerD = () => listenersTriggered.push('d');

      // simple case
      eventSupport.on('lorem', handlerA);
      eventSupport.on('lorem', handlerB);
      eventSupport.on('lorem', handlerC);

      eventSupport.trigger('lorem');
      expect(listenersTriggered).toEqual(['a', 'b', 'c']);

      eventSupport.off('lorem');
      listenersTriggered = [];

      // listeners added multiple times
      eventSupport.on('lorem', handlerC);
      eventSupport.on('lorem', handlerB);
      eventSupport.on('lorem', handlerC);
      eventSupport.on('lorem', handlerA);
      eventSupport.on('lorem', handlerB);

      eventSupport.trigger('lorem');
      expect(listenersTriggered).toEqual(['c', 'b', 'c', 'a', 'b']);

      eventSupport.off('lorem');
      listenersTriggered = [];

      // listeners are de- and re-registered
      eventSupport.on('lorem', handlerC);
      eventSupport.on('lorem', handlerB);
      eventSupport.off('lorem', handlerC);
      eventSupport.on('lorem', handlerC);
      eventSupport.on('lorem', handlerA);
      eventSupport.off('lorem', handlerB);
      eventSupport.on('lorem', handlerB);

      eventSupport.trigger('lorem');
      expect(listenersTriggered).toEqual(['c', 'a', 'b']);

      eventSupport.off('lorem');
      listenersTriggered = [];

      // listeners registered for event with subtype
      eventSupport.on('fancy:lorem', handlerA);
      eventSupport.on('fancy:lorem', handlerB);
      eventSupport.on('fancy:lorem', handlerC);

      eventSupport.trigger('fancy', new FancyEvent('lorem'));
      expect(listenersTriggered).toEqual(['a', 'b', 'c']);

      eventSupport.off('fancy:lorem');
      listenersTriggered = [];

      // listeners registered for event without subtype
      eventSupport.on('fancy', handlerC);
      eventSupport.on('fancy', handlerB);
      eventSupport.on('fancy', handlerA);

      eventSupport.trigger('fancy', new FancyEvent('lorem'));
      expect(listenersTriggered).toEqual(['c', 'b', 'a']);

      eventSupport.off('fancy');
      listenersTriggered = [];

      // listeners registered for event with and without subtype
      eventSupport.on('fancy', handlerA);
      eventSupport.on('fancy:lorem', handlerB);
      eventSupport.on('fancy:lorem', handlerC);
      eventSupport.on('fancy', handlerD);

      eventSupport.trigger('fancy', new FancyEvent('lorem'));
      expect(listenersTriggered).toEqual(['a', 'b', 'c', 'd']);
    });
  });

  describe('registerSubTypeProvider', () => {

    it('is used to execute specific listeners', () => {
      const eventSupport = new EventSupport();
      eventSupport.registerSubTypeProvider('fancy', (event: FancyEvent) => event.subType);

      let count = 0;
      let countFoo = 0;
      let countBar = 0;

      eventSupport.on('fancy', e => count++);
      eventSupport.on('fancy:foo', e => countFoo++);
      eventSupport.on('fancy:bar', e => countBar++);

      expect(count).toBe(0);
      expect(countFoo).toBe(0);
      expect(countBar).toBe(0);

      eventSupport.trigger('fancy', new FancyEvent('not-existing'));
      expect(count).toBe(1);
      expect(countFoo).toBe(0);
      expect(countBar).toBe(0);

      eventSupport.trigger('fancy', new FancyEvent('foo'));
      expect(count).toBe(2);
      expect(countFoo).toBe(1);
      expect(countBar).toBe(0);

      eventSupport.trigger('fancy', new FancyEvent('bar'));
      expect(count).toBe(3);
      expect(countFoo).toBe(1);
      expect(countBar).toBe(1);
    });
  });
});

class FancyEvent extends Event {
  subType: string;

  constructor(subType: string) {
    super();
    this.subType = scout.assertValue(subType);
  }
}
