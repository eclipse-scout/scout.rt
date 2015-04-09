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
