function mostRecentJsonRequest() {
  return $.parseJSON(mostRecentAjaxRequest().params);
}

var jasmineScoutMatchers = {
  toContainEventTypesExactly: function(util, customEqualityTesters) {
    return {
      compare: function(actual, expected) {
        if (expected === undefined) {
          expected = [];
        }
        var result = {};

        var actualEventTypes = [];
        for (i = 0; i < actual.events.length; i++) {
          actualEventTypes.push(actual.events[i].type_);
        }

        result.pass = util.equals(actualEventTypes, expected, customEqualityTesters);

        if (!result.pass) {
          result.message = "Expected actual event types " + actualEventTypes + " to be equal to " + expected;
        }
        return result;
      }
    };
  }
};

beforeEach(function() {
  jasmine.addMatchers(jasmineScoutMatchers);
});
