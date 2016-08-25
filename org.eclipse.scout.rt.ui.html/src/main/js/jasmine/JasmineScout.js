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
/* exported mostRecentJsonRequest  */
/* global LocaleSpecHelper */
function mostRecentJsonRequest() {
  var req = jasmine.Ajax.requests.mostRecent();
  if (req) {
    return $.parseJSON(req.params);
  }
}

function sandboxSession(options) {
  var $sandbox = $('#sandbox').addClass('scout');

  options = options || {};
  options.portletPartId = options.portletPartId || '0';
  options.backgroundJobPollingEnabled = false;
  options.suppressErrors = true;
  options.$entryPoint = $sandbox;

  var session = new scout.Session();
  session.init(options);

  // Install non-filtering requestToJson() function. This is required to test
  // the value of the "showBusyIndicator" using toContainEvents(). Usually, this
  // flag is filtered from the request before sending the AJAX call, however in
  // the tests we want to keep it.
  session._requestToJson = function(request) {
    return JSON.stringify(request);
  };

  // Simulate successful session initialization
  session.uiSessionId = '1.1';
  session.modelAdapterRegistry[session.uiSessionId] = session;
  session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
  session.desktop = scout.create('Desktop', {
    parent: session.rootAdapter
  });
  session._renderDesktop();

  return session;
}

function sandboxDesktop() {
  var $sandbox = window.sandbox();
  $sandbox.addClass('scout desktop');
  return $sandbox;
}

function createSimpleModel(objectType, session, id) {
  if (id === undefined) {
    id = scout.objectFactory.createUniqueId();
  }
  return {
    id: id,
    objectType: objectType,
    parent: new scout.NullWidget(),
    session: session
  };
}

/**
 * Sends the queued requests and simulates a response as well.
 * @param response if not set an empty success response will be generated
 */
function sendQueuedAjaxCalls(response, time) {
  time = time || 0;
  jasmine.clock().tick(time);

  receiveResponseForAjaxCall('', response);
}

function receiveResponseForAjaxCall(request, response) {
  if (!response) {
    response = {
      status: 200,
      responseText: '{"events":[]}'
    };
  }
  if (!request) {
    request = jasmine.Ajax.requests.mostRecent();
  }
  if (request && request.onload) {
    request.response(response);
  }
}

/**
 * Uninstalls 'beforeunload' and 'unload' events from window that were previously installed by session.start()
 */
function uninstallUnloadHandlers(session) {
  $(window)
    .off('beforeunload.' + session.uiSessionId)
    .off('unload.' + session.uiSessionId);
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
  adapter = session.getOrCreateModelAdapter(model.id, model.parent);
  expect(session.getModelAdapter(adapter.id)).toBe(adapter);
  return adapter;
}

function stripCommentsFromJson(input) {
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
    if (currentCharacter === '\r') {
      if (nextCharacter === '\n') {
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
        var result = {},
          i;

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
   * Checks if given request contains all the expected events in the given order
   * @actual json request, may be obtained by mostRecentJsonRequest
   */
  toContainEventsExactly: function(util, customEqualityTesters) {
    return {
      compare: function(actual, expected) {
        if (expected === undefined) {
          expected = [];
        }
        if (!Array.isArray(expected)) {
          expected = [expected];
        }
        var result = {},
          i;

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
        }

        result.pass = util.equals(actualEvents, expected, customEqualityTesters);

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
  },

  /**
   * Checks if all given jQuery objects (array of jQuery objects) have a specific class (list).
   */
  allToHaveClass: function(util, customEqualityTesters) {
    return {
      compare: function(actual, expected) {
        if (expected === undefined) {
          expected = [];
        }
        if (!Array.isArray(expected)) {
          expected = [expected];
        }
        if (!Array.isArray(actual)) {
          actual = [actual];
        }
        var result = {
          pass: actual.every(function($elem) {
            return $elem.hasClass(expected);
          })
        };

        if (!result.pass) {
          result.message = 'Expected ' + actual + ' all to have ' + expected + ' as classes.';
        }
        return result;
      }
    };
  },

  /**
   * Checks if any given jQuery object (array of jQuery objects) has a specific class (list).
   */
  anyToHaveClass: function(util, customEqualityTesters) {
    return {
      compare: function(actual, expected) {
        if (expected === undefined) {
          expected = [];
        }
        if (!Array.isArray(expected)) {
          expected = [expected];
        }
        if (!Array.isArray(actual)) {
          actual = [actual];
        }
        var result = {
          pass: actual.some(function($elem) {
            return $elem.hasClass(expected);
          })
        };

        if (!result.pass) {
          result.message = 'Expected any ' + actual + ' to have ' + expected + ' as classes.';
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
$.fn.triggerBlur = function() {
  var event = new jQuery.Event("blur");
  event.originalEvent = {}; // create dummy object
  this.trigger(event);
};

$.fn.triggerRightClick = function() {
  this.trigger({
    type: 'mousedown',
    which: 3
  });
  this.trigger({
    type: 'mouseup',
    which: 3
  });
  return this;
};

$.fn.triggerKeyUp = function(key, modifier) {
  var event = new jQuery.Event("keyup");
  event.originalEvent = {}; // create dummy object
  event.which = key;
  extendEventWithModifier(event, modifier);
  this.trigger(event);
};

$.fn.triggerKeyDown = function(key, modifier) {
  var event = new jQuery.Event("keydown");
  event.originalEvent = {}; // create dummy object
  event.which = key;
  extendEventWithModifier(event, modifier);
  this.trigger(event);
};

function extendEventWithModifier(event, modifier) {
  event.altKey = modifier === 'alt';
  event.ctrlKey = modifier === 'ctrl';
  event.shiftKey = modifier === 'shift';
  event.metaKey = modifier === 'meta';
}

$.fn.triggerMouseEnter = function(opts) {
  return this.triggerMouseAction('mouseenter', opts);
};

$.fn.triggerMouseLeave = function(opts) {
  return this.triggerMouseAction('mouseleave', opts);
};

$.fn.triggerMouseDown = function(opts) {
  return this.triggerMouseAction('mousedown', opts);
};

$.fn.triggerMouseUp = function(opts) {
  return this.triggerMouseAction('mouseup', opts);
};

$.fn.triggerMouseMove = function(position) {
  return this.triggerWithPosition('mousemove', {
    position: position
  });
};

$.fn.triggerMouseAction = function(eventType, opts) {
  var event;
  opts = opts || {};

  if (!opts.position) {
    opts.position = this.offset();
  }
  if (!opts.clicks) {
    opts.clicks = 1;
  }
  if (!opts.which) {
    opts.which = 1;
  }
  event = {
    type: eventType,
    which: opts.which,
    originalEvent: {
      detail: opts.clicks
    },
    pageX: opts.position.left,
    pageY: opts.position.top
  };
  if (opts.modifier) {
    extendEventWithModifier(event, opts.modifier);
  }

  this.trigger(event);
  return this;
};

$.fn.triggerWithPosition = function(event, position) {
  if (!position) {
    position = this.offset();
  }

  this.trigger({
    type: event,
    pageX: position.left,
    pageY: position.right
  });
  return this;
};

$.fn.triggerContextMenu = function() {
  var opts = {
    position: this.offset(),
    which: 3
  };

  this.triggerMouseDown(opts);
  this.triggerMouseUp(opts);
  this.trigger({
    type: 'contextmenu',
    pageX: opts.position.left,
    pageY: opts.position.top
  });
  return this;
};

/**
 * Triggers mouse down, mouse up and click events. <br>
 * Also sets the detail property of the originalEvent which contains the numbers of clicks.
 * @param clicks the number of clicks. If not set 1 is used.
 */
$.fn.triggerClick = function(opts) {
  opts = opts || {};
  if (!opts.clicks) {
    opts.clicks = 1;
  }

  this.triggerMouseDown(opts);
  this.triggerMouseUp(opts);
  this.triggerMouseAction('click', opts);

  return this;
};

$.fn.triggerDoubleClick = function() {
  var clicks = 2;

  this.triggerClick();
  this.triggerClick({
    clicks: 2
  });
  this.trigger({
    type: 'dblclick',
    originalEvent: {
      detail: clicks
    }
  });
  return this;
};
