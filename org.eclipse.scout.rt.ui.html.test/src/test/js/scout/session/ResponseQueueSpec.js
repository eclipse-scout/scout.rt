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
describe('ResponseQueue', function() {

  beforeEach(function() {
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  function createSession() {
    setFixtures(sandbox());
    return sandboxSession();
  }

  describe('add', function() {

    it('adds elements to the queue in the correct order', function() {
      var session = createSession();
      var rq = session.responseQueue;

      expect(rq.nextExpectedSequenceNo).toBe(1);

      rq.add({id: 1});
      rq.add({id: 3});
      rq.add({id: 2});
      rq.add({'#': 100, id: 4});
      rq.add({id: 7});
      rq.add({'#': 300, id: 5});
      rq.add({'#': 200, id: 6});
      rq.add({id: 8});
      rq.add({'#': 150, id: 9});

      expect(rq.queue.length).toBe(9);
      expect(rq.queue[0].id).toBe(1);
      expect(rq.queue[1].id).toBe(3);
      expect(rq.queue[2].id).toBe(2);
      expect(rq.queue[3].id).toBe(4);
      expect(rq.queue[4].id).toBe(7);
      expect(rq.queue[5].id).toBe(9); // <--
      expect(rq.queue[6].id).toBe(6); // <--
      expect(rq.queue[7].id).toBe(5); // <--
      expect(rq.queue[8].id).toBe(8);

      expect(rq.nextExpectedSequenceNo).toBe(1);
    });

    it('removes elements that are superseded by combined response', function() {
      var session = createSession();
      var rq = session.responseQueue;

      expect(rq.nextExpectedSequenceNo).toBe(1);

      rq.add({'#': 1, id: 1});
      rq.add({'#': 2, id: 2});
      rq.add({'#': 4, id: 4});
      rq.add({'#': 8, combined: true, id: 8});
      rq.add({'#': 3, id: 3});
      rq.add({'#': 6, combined: true, id: 6});
      rq.add({'#': 9, id: 9});

      expect(rq.queue.length).toBe(2);
      expect(rq.queue[0].id).toBe(8);
      expect(rq.queue[1].id).toBe(9);

      // assume "processed" until combined message
      expect(rq.nextExpectedSequenceNo).toBe(8);
      expect(rq.lastProcessedSequenceNo).toBe(7);
    });

  });

  describe('process', function() {

    it('processes elements in the correct order', function() {
      var session = createSession();
      var rq = session.responseQueue;
      spyOn(session, 'processJsonResponseInternal').and.callFake(function(data) {
        if (data && data.id === 9) {
          return true;
        }
        return false;
      });

      expect(rq.nextExpectedSequenceNo).toBe(1);

      rq.add({id: 1});
      rq.add({id: 3});
      rq.add({id: 2});
      rq.add({'#': 1, id: 4});
      rq.add({id: 7});
      rq.add({'#': 3, id: 5});
      rq.add({'#': 2, id: 6});
      rq.add({id: 8});

      var success = rq.process({'#': 4, id: 9});

      expect(rq.queue.length).toBe(0);
      expect(rq.nextExpectedSequenceNo).toBe(5);
      expect(session.processJsonResponseInternal.calls.count()).toBe(9);
      expect(success).toBe(true);
    });

    it('does not process elements in the wrong order', function() {
      var session = createSession();
      var rq = session.responseQueue;
      spyOn(session, 'processJsonResponseInternal').and.callFake(function(data) {
        if (data && data.id === 9) {
          return true;
        }
        return false;
      });

      expect(rq.nextExpectedSequenceNo).toBe(1);

      rq.add({'#': 2, id: 1});
      var success = rq.process({'#': 3, id: 2});
      expect(success).toBe(true);
      expect(rq.queue.length).toBe(2); // not processed!

      // wait 5s
      jasmine.clock().tick(5000);

      success = rq.process({'#': 4, id: 3});
      expect(success).toBe(true);
      expect(rq.queue.length).toBe(3); // still not processed
      expect(rq.nextExpectedSequenceNo).toBe(1);
      expect(rq.forceTimeoutId).not.toBe(null);

      // wait 6s
      jasmine.clock().tick(6000);

      expect(rq.queue.length).toBe(0); // should have been forced after 10s
      expect(session.processJsonResponseInternal.calls.count()).toBe(3);
      expect(rq.forceTimeoutId).toBe(null);
      expect(rq.nextExpectedSequenceNo).toBe(5);
    });

    it('does not process same response twice', function() {
      var session = createSession();
      var rq = session.responseQueue;
      spyOn(session, 'processJsonResponseInternal').and.callFake(function(data) {
        if (data && data.id === 9) {
          return true;
        }
        return false;
      });

      expect(rq.nextExpectedSequenceNo).toBe(1);

      rq.add({'#': 1, id: 4});
      rq.add({'#': 3, id: 5});
      rq.add({'#': 2, id: 6});
      var success = rq.process({'#': 4, id: 9});

      expect(rq.queue.length).toBe(0);
      expect(rq.lastProcessedSequenceNo).toBe(4);
      expect(rq.nextExpectedSequenceNo).toBe(5);
      expect(session.processJsonResponseInternal.calls.count()).toBe(4);
      expect(success).toBe(true);

      // add an older element than current seqNo -> ignore
      success = rq.process({'#': 1, id: 4});
      expect(success).toBe(true);
      expect(rq.queue.length).toBe(0);
      expect(rq.lastProcessedSequenceNo).toBe(4);
      expect(rq.nextExpectedSequenceNo).toBe(5);
      expect(session.processJsonResponseInternal.calls.count()).toBe(4);
    });

  });

});
