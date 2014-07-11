/* exported mostRecentJsonRequest */
function mostRecentJsonRequest() {
  var req = jasmine.Ajax.requests.mostRecent();
  if (req) {
    return $.parseJSON(req.params);
  }
}

var m_adapterSeq = 0;

function createUniqueAdapterId() {
  return m_adapterSeq++;
}

/**
 * Sends the queued requests and simulates a response as well.
 * @param response if not set an empty success response will be generated
 */
function sendQueuedAjaxCalls(response) {
  jasmine.clock().tick(0);

  if (!response) {
    response = {
      status: 200,
      responseText: '{"events":[]}'
    };
  }

  var request = jasmine.Ajax.requests.mostRecent();
  if (request) {
    request.response(response);
  }
}

var jasmineScoutMatchers = {
  /**
   * Checks if given request contains expected events, order does not matter.
   * @actual json request, may be obtained by mostRecentJsonRequest
   */
  toContainEvents: function(util, customEqualityTesters) {
    return {
      compare: function(actual, expected) {
        if (expected === undefined) {
          expected = [];
        }
        if (!Array.isArray(expected)) {
          expected = [expected];
        }
        var result = {}, i;

        var actualEvents = [];
        if (actual) {
          for (i = 0; i < actual.events.length; i++) {
            actualEvents.push(actual.events[i]);
          }
        }

        result.pass = true;
        for (i = 0; i < expected.length; i++) {
          //Prototype may be Event. If that's the case we need to convert, otherwise equals will fail
          if (Object.getPrototypeOf(expected[i]) !== Object.prototype) {
            expected[i] = $.parseJSON(JSON.stringify(expected[i]));
          }

          result.pass = result.pass && util.contains(actualEvents, expected[i], customEqualityTesters);
        }

        if (!result.pass) {
          result.message = 'Expected actual events ' + actualEvents + ' to be equal to ' + expected;
        }
        return result;
      }
    };
  },
  /**
   * Checks if given request contains events with the expected event types in the given order
   * @actual json request, may be obtained by mostRecentJsonRequest
   */
  toContainEventTypesExactly: function(util, customEqualityTesters) {
    return {
      compare: function(actual, expected) {
        if (expected === undefined) {
          expected = [];
        }
        if (!Array.isArray(expected)) {
          expected = [expected];
        }
        var result = {};

        var actualEventTypes = [];
        if (actual) {
          for (var i = 0; i < actual.events.length; i++) {
            actualEventTypes.push(actual.events[i].type_);
          }
        }

        result.pass = util.equals(actualEventTypes, expected, customEqualityTesters);

        if (!result.pass) {
          result.message = 'Expected actual event types ' + actualEventTypes + ' to be equal to ' + expected;
        }
        return result;
      }
    };
  }
};

beforeEach(function() {
  jasmine.addMatchers(jasmineScoutMatchers);
});

//JQuery extensions for testing purpose
$.fn.triggerRightClick = function() {
  $(this).trigger({
    type: 'mousedown',
    which: 3
  });
  $(this).trigger({
    type: 'mouseup',
    which: 3
  });
  return $(this);
};

$.fn.triggerMouseDown = function(clicks) {
  return $(this).triggerEventWithDetail('mousedown', clicks);
};

$.fn.triggerMouseUp = function(clicks) {
  return $(this).triggerEventWithDetail('mouseup', clicks);
};

$.fn.triggerEventWithDetail = function(event, clicks) {
  var $this = $(this);

  if (!clicks) {
    clicks = 1;
  }

  $this.trigger({
    type: event,
    originalEvent: {
      detail: clicks
    }
  });
  return $this;
};

$.fn.triggerContextMenu = function() {
  var $this = $(this),
    pos = $this.position(),
    clicks = 1;

  $this.triggerMouseDown(clicks);
  $this.triggerMouseUp(clicks);
  $this.trigger({
    type: 'contextmenu',
    pageX: pos.left,
    pageY: pos.top
  });
  return $this;
};

$.fn.triggerClick = function(clicks) {
  var $this = $(this),
    pos = $this.position();

  if (!clicks) {
    clicks = 1;
  }

  $this.triggerMouseDown(clicks);
  $this.triggerMouseUp(clicks);
  $this.trigger({
    type: 'click',
    originalEvent: {
      detail: clicks
    }
  });

  return $this;
};

$.fn.triggerDoubleClick = function(clicks) {
  var $this = $(this);
  $this.triggerClick();
  $this.triggerClick(2);
  $this.trigger({
    type: 'dblclick'
  });
  return $this;
};
