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
describe("EventSupport", function() {

  describe("on / trigger / off", function() {

    var count, events = new scout.EventSupport();

    function fooListener() {
      count++;
    }

    beforeEach(function() {
      count = 0;
    });

    it("single event", function() {
      events.on('foo', fooListener);
      events.trigger('foo');
      expect(count).toBe(1);
      // after 'off' listener shall not be triggered anymore
      events.off('foo', fooListener);
      events.trigger('foo');
      expect(count).toBe(1);
    });

    it("multiple events", function() {
      events.on('foo bar', fooListener);
      events.trigger('foo');
      events.trigger('bar');
      expect(count).toBe(2);
    });

  });

});
