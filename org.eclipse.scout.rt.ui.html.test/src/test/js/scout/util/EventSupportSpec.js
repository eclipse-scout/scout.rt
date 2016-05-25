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
describe('EventSupport', function() {

  var count, events;

  function fooListener() {
    count++;
  }

  describe('on / trigger / off', function() {

    beforeEach(function() {
      events = new scout.EventSupport();
      count = 0;
    });

    it('single event func only triggered until off() is called', function() {
      events.on('foo', fooListener);
      events.trigger('foo');
      expect(count).toBe(1);
      // after 'off' listener shall not be triggered anymore
      events.off('foo', fooListener);
      events.trigger('foo');
      expect(count).toBe(1);
    });

    it('multiple events', function() {
      events.on('foo bar', fooListener);
      events.trigger('foo');
      events.trigger('bar');
      expect(count).toBe(2);
    });

  });

  describe('one', function() {

    beforeEach(function() {
      events = new scout.EventSupport();
      count = 0;
    });

    it('single event func only triggered once when registered with one()', function() {
      events.one('foo', fooListener);
      events.trigger('foo');
      events.trigger('foo');
      expect(count).toBe(1);
      expect(events._eventListeners.length).toBe(0);
    });

    it('event parameter passed to registered func', function() {
      var receivedEvent = null;
      events.one('foo', function(event) {
        receivedEvent = event;
      });
      events.trigger('foo', {
        theProp: 'bar'
      });
      // expect the event has been passed to the registered func
      expect(receivedEvent.theProp).toBe('bar');
      // expect the type property is automatically set by EventSupport
      expect(receivedEvent.type).toBe('foo');
    });

    it('de-register function registered with one()', function() {
      events.one('foo', fooListener);
      expect(events._eventListeners.length).toBe(1);
      events.off('foo', fooListener);
      expect(events._eventListeners.length).toBe(0);
    });

  });

  describe('off', function() {

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

    beforeEach(function() {
      events = new scout.EventSupport();
      count = 0;
    });

    it('remove all with same type', function() {
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

    it('remove specific listener', function() {
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
