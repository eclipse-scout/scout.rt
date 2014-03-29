/*global jasmineScoutMatchers:false */
describe("JasmineScout", function() {

  describe("toContainEventTypesExactly", function() {

    function createRequestDataFixture() {
      var requestData = {};
      requestData.events = [];
      requestData.events.push(new Scout.Event('event1', 1));
      requestData.events.push(new Scout.Event('event2', 1));
      requestData.events.push(new Scout.Event('event3', 1));

      return requestData;
    }

    it("checks whether event types in correct order", function() {
      var matcher = jasmineScoutMatchers.toContainEventTypesExactly(jasmine.matchersUtil),
        result;

      var requestData = createRequestDataFixture();

      result = matcher.compare(requestData, ['event1', 'event2', 'event3']);
      expect(result.pass).toBeTruthy();

      result = matcher.compare(requestData, ['event3', 'event2', 'event2']);
      expect(result.pass).toBeFalsy();

      result = matcher.compare(requestData, ['event1', 'event3', 'event2']);
      expect(result.pass).toBeFalsy();
    });

    it("checks whether event types are complete", function() {
      var matcher = jasmineScoutMatchers.toContainEventTypesExactly(jasmine.matchersUtil),
        result;

      var requestData = createRequestDataFixture();

      result = matcher.compare(requestData, ['event1', 'event2']);
      expect(result.pass).toBeFalsy();

      result = matcher.compare(requestData, ['abc', 'event2', 'event2']);
      expect(result.pass).toBeFalsy();
    });

    it("considers empty values", function() {
      var matcher = jasmineScoutMatchers.toContainEventTypesExactly(jasmine.matchersUtil),
        result;

      var requestData = createRequestDataFixture();

      result = matcher.compare(requestData, []);
      expect(result.pass).toBeFalsy();

      requestData = {};
      requestData.events = [];

      result = matcher.compare(requestData, []);
      expect(result.pass).toBeTruthy();
    });
  });

});
