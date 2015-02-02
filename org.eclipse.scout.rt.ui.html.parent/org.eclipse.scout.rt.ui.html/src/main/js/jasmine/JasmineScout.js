/* exported mostRecentJsonRequest */
function mostRecentJsonRequest() {
  var req = jasmine.Ajax.requests.mostRecent();
  if (req) {
    return $.parseJSON(req.params);
  }
}

var adapterSeq = 1;

function createUniqueAdapterId() {
  return "" + adapterSeq++;
}

function createSimpleModel(objectType, id) {
  if (id === undefined) {
    id = createUniqueAdapterId();
  }

  return {
    id: id,
    objectType: objectType
  };
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

function createPropertyChangeEvent(model, properties) {
  return {
    target: model.id,
    properties: properties,
    type: 'property'
  };
}

function createAdapterData(adapterDataArray) {
  var i,
    adapterData = {};
  adapterDataArray = scout.arrays.ensure(adapterDataArray);

  for (i = 0; i < adapterDataArray.length; i++) {
    adapterData[adapterDataArray[i].id] = adapterDataArray[i];
  }
  return adapterData;
}

function createAdapter(model, session, adapterDataArray) {
  var adapterData, adapter;
  adapterDataArray = scout.arrays.ensure(adapterDataArray);
  adapterDataArray.push(model);

  adapterData = createAdapterData(adapterDataArray);
  session._copyAdapterData(adapterData);
  adapter = session.getOrCreateModelAdapter(model.id);
  expect(session.getModelAdapter(adapter.id)).toBe(adapter);
  return adapter;
}

function removeCommentsFromJson(input) {
  if (!input || typeof input !== 'string') {
    return input;
  }
  var result = '';
  var whitespaceBuffer = '';
  for (var i = 0; i < input.length; i++) {
    var previousCharacter = input.charAt(i - 1);
    var currentCharacter = input.charAt(i);
    var nextCharacter = input.charAt(i + 1);

    // Add whitespace to a buffer (because me might want to ignore it at the end of a line)
    if (currentCharacter === ' ' || currentCharacter === '\t') {
      whitespaceBuffer += currentCharacter;
      continue;
    }
    // Handle end of line
    if (currentCharacter == '\r') {
      if (nextCharacter == '\n') {
        // Handle \r\n as \n
        continue;
      }
      // Handle \r as \n
      currentCharacter = '\n';
    }
    if (currentCharacter === '\n') {
      whitespaceBuffer = ''; // discard whitespace
      // Add line break (but not at the begin and not after another line break)
      if (result.charAt(result.length - 1) !== '\n') {
        result += currentCharacter;
      }
      continue;
    }

    // Handle strings
    if (currentCharacter === '"' && previousCharacter !== '\\') {
      // Flush whitespace to result
      result += whitespaceBuffer;
      whitespaceBuffer = '';
      result += currentCharacter;
      for (i++; i < input.length; i++) {
        previousCharacter = input.charAt(i - 1);
        currentCharacter = input.charAt(i);
        nextCharacter = input.charAt(i + 1);
        result += currentCharacter;
        if (currentCharacter === '"' && previousCharacter !== '\\') {
          break; // end of string
        }
      }
    }
    // Handle multi-line comments
    else if (currentCharacter === '/' && nextCharacter === '*') {
      for (i++; i < input.length; i++) {
        previousCharacter = input.charAt(i - 1);
        currentCharacter = input.charAt(i);
        nextCharacter = input.charAt(i + 1);
        if (currentCharacter === '/' && previousCharacter === '*') {
          break; // end of multi-line comment
        }
      }
    }
    // Handle single-line comment
    else if (currentCharacter === '/' && nextCharacter === '/') {
      for (i++; i < input.length; i++) {
        previousCharacter = input.charAt(i - 1);
        currentCharacter = input.charAt(i);
        nextCharacter = input.charAt(i + 1);
        if (nextCharacter === '\n' || nextCharacter === '\r') {
          break; // end of single-line comment
        }
      }
    }
    // regular character
    else {
      // Flush whitespace to result
      result += whitespaceBuffer;
      whitespaceBuffer = '';
      result += currentCharacter;
    }
  }
  return result;
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
            actualEventTypes.push(actual.events[i].type);
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

$.fn.triggerKeyDown = function(key, modifier) {
  var event = jQuery.Event("keydown");
  event.which = key;
  if (modifier === 'ctrl') {
    event.ctrlKey = true;
  }
  if (modifier === 'shift') {
    event.shiftKey = true;
  }
  (this).trigger(event);
};

$.fn.triggerMouseDown = function(clicks) {
  return $(this).triggerWithDetail('mousedown', clicks);
};

$.fn.triggerMouseUp = function(clicks) {
  return $(this).triggerWithDetail('mouseup', clicks);
};

$.fn.triggerMouseMove = function(position) {
  return $(this).triggerWithPosition('mousemove', position);
};

$.fn.triggerWithDetail = function(event, clicks) {
  var $this = $(this),
    pos = $this.position();

  if (!clicks) {
    clicks = 1;
  }

  $this.trigger({
    type: event,
    originalEvent: {
      detail: clicks
    },
    pageX: pos.left,
    pageY: pos.right
  });
  return $this;
};

$.fn.triggerWithPosition = function(event, position) {
  var $this = $(this);

  if (!position) {
    position = $this.position();
  }

  $this.trigger({
    type: event,
    pageX: position.left,
    pageY: position.right
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

/**
 * Triggers mouse down, mouse up and click events. <br>
 * Also sets the detail property of the originalEvent which contains the numbers of clicks.
 * @param clicks the number of clicks. If not set 1 is used.
 */
$.fn.triggerClick = function(clicks) {
  var $this = $(this);

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

$.fn.triggerDoubleClick = function() {
  var $this = $(this);
  var clicks = 2;

  $this.triggerClick();
  $this.triggerClick(2);
  $this.trigger({
    type: 'dblclick',
    originalEvent: {
      detail: clicks
    }
  });
  return $this;
};
