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
/* global scout.LocaleSpecHelper */
function mostRecentJsonRequest() {
  var req = jasmine.Ajax.requests.mostRecent();
  if (req) {
    return $.parseJSON(req.params);
  }
}

function sandboxSession(options) {
  var $sandbox = $('#sandbox').addClass('scout');
  addDefaultStyles();

  options = options || {};
  options.portletPartId = options.portletPartId || '0';
  options.backgroundJobPollingEnabled = false;
  options.suppressErrors = true;
  options.renderDesktop = scout.nvl(options.renderDesktop, true);
  options.remote = true; // required so adapters will be registered in the adapter registry
  options.$entryPoint = $sandbox;

  var session = scout.create('scout.Session', options, {
    ensureUniqueId: false
  });

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
  session.locale = new scout.LocaleSpecHelper().createLocale(scout.LocaleSpecHelper.DEFAULT_LOCALE);

  var desktop = options.desktop || {};
  desktop.navigationVisible = scout.nvl(desktop.navigationVisible, false);
  desktop.headerVisible = scout.nvl(desktop.headerVisible, false);
  desktop.benchVisible = scout.nvl(desktop.benchVisible, false);
  var rootWidget = new scout.NullWidget();
  rootWidget.session = session;
  desktop.parent = scout.nvl(desktop.parent, rootWidget);
  session.desktop = scout.create('Desktop', desktop);
  if (options.renderDesktop) {
    session._renderDesktop();
  }

  // Prevent exception when test window gets resized
  $sandbox.window().off('resize', session.desktop._resizeHandler);
  return session;
}

function sandboxDesktop() {
  var $sandbox = window.sandbox();
  $sandbox.addClass('scout desktop');
  return $sandbox;
}

function addDefaultStyles() {
  var style = '.hidden {display: none !important;}';
  $('<style>' + style + '</style>').appendTo('<head>');
}

function createSimpleModel(objectType, session, id) {
  if (id === undefined) {
    id = scout.objectFactory.createUniqueId();
  }
  var parent = session.desktop;
  return {
    id: id,
    objectType: objectType,
    parent: parent,
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
    request.respondWith(response);
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

/**
 * Removes all open popups for the given session.
 * May be used to make sure handlers get properly detached
 */
function removePopups(session, cssClass) {
  cssClass = cssClass || '.popup';
  session.$entryPoint.children(cssClass).each(function() {
    var popup = scout.Widget.getWidgetFor($(this));
    popup.animateRemoval = false;
    popup.remove();
  });
}

function createPropertyChangeEvent(model, properties) {
  return {
    target: model.id,
    properties: properties,
    type: 'property'
  };
}

/**
 * Converts the given adapaterDataArray into a map of adapterData where the key
 * is the adapterData.id and the value is the adapterData itself.
 */
function mapAdapterData(adapterDataArray) {
  var adapterDataMap = {};
  adapterDataArray = scout.arrays.ensure(adapterDataArray);
  adapterDataArray.forEach(function(adapterData) {
    adapterDataMap[adapterData.id] = adapterData;
  });
  return adapterDataMap;
}

/**
 * Converts the given adapterDataArray into a map of adapterData and registers the adapterData in the Session.
 * Only use this function when your tests requires to have a remote adapter. In that case create widget and
 * remote adapter with Session#getOrCreateWidget().
 *
 * @param adapterDataArray
 */
function registerAdapterData(adapterDataArray, session) {
  var adapterDataMap = this.mapAdapterData(adapterDataArray);
  session._copyAdapterData(adapterDataMap);
}

/**
 * Returns a new object instance having two properties id, objectType from the given widgetModel.
 * this function is required because the model object passed to the scout.create() function is modified
 * --> model.objectType is changed to whatever string is passed as parameter objectType
 *
 * @param widgetModel
 */
function createAdapterModel(widgetModel) {
  return {
    id: widgetModel.id,
    objectType: widgetModel.objectType,
    session: widgetModel.session
  };
}

/**
 * This function links and existing widget with a new adapter instance. This is useful for tests
 * where you have an existing widget and later create a new adapter instance to that widget.
 */
function linkWidgetAndAdapter(widget, adapterClass) {
  var session = widget.session;
  var adapter = scout.create(adapterClass, {
    id: widget.id,
    session: session
  });
  adapter.widget = widget;
  widget.modelAdapter = adapter;
  adapter._attachWidget();
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

// JQuery extensions for testing purpose
$.fn.triggerBlur = function() {
  var event = jQuery.Event('blur', {
    originalEvent: jQuery.Event('dummy') // create dummy object
  });
  this.trigger(event);
};

$.fn.triggerRightClick = function() {
  this.trigger(jQuery.Event('mousedown', {which: 3}));
  this.trigger(jQuery.Event('mouseup', {which: 3}));
  return this;
};

$.fn.triggerKeyUp = function(key, modifier) {
  var event = jQuery.Event('keyup', {
    originalEvent: jQuery.Event('dummy'), // create dummy object
    which: key
  });
  extendEventWithModifier(event, modifier);
  this.trigger(event);
};

$.fn.triggerKeyDown = function(key, modifier) {
  var event = jQuery.Event('keydown', {
    originalEvent: jQuery.Event('dummy'), // create dummy object
    which: key
  });
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

/**
 * Does not use jQuery to create the event to make sure capture phase listeners are notified as well.
 */
$.fn.triggerMouseDownCapture = function(opts) {
  var event;
  try {
    event = new MouseEvent('mousedown', {
      'view': window,
      'bubbles': true,
      'cancelable': true
    });
  } catch (e) {
    // Phantom JS only supports the old, deprecated API
    event = document.createEvent('MouseEvent');
    event.initEvent('mousedown', true, true);
  }
  this[0].dispatchEvent(event);
  return this;
};

$.fn.triggerKeyDownCapture = function(which) {
  return this.triggerKeyCapture('keydown', which);
};

$.fn.triggerKeyUpCapture = function(which) {
  return this.triggerKeyCapture('keyup', which);
};

$.fn.triggerKeyCapture = function(eventType, which) {
  // Due to a Chrome bug, "new KeyboardEvent" cannot be used,
  // as it doesn't set "which". We have to use this less specific
  // constructor.
  var eventObj;

  try {
    eventObj = new Event(eventType, {
      'bubbles': true,
      'cancelable': true
    });
  }
  catch (e) {
    // Workaround for PhantomJS
    eventObj = document.createEvent('CustomEvent');
    eventObj.initEvent(eventType, true, true);
  }

  eventObj.metaKey = eventObj.altKey = eventObj.shiftKey = eventObj.ctrlKey = false;
  eventObj.keyCode = which;
  eventObj.which = which;

  this[0].dispatchEvent(eventObj);
  return this;
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
  event = jQuery.Event(eventType, {
    which: opts.which,
    originalEvent: jQuery.Event(eventType, {
      detail: opts.clicks
    }),
    pageX: opts.position.left,
    pageY: opts.position.top
  });
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
  this.trigger(jQuery.Event('contextmenu', {
    pageX: opts.position.left,
    pageY: opts.position.top
  }));
  return this;
};

/**
 * Triggers mouse down, mouse up and click events. <br>
 * Also sets the detail property of the originalEvent which contains the numbers of clicks.
 * @param opts options object passed to triggerMouse* functions
 */
$.fn.triggerClick = function(opts) {
  opts = scout.nvl(opts, {});

  if (!opts.click) {
    opts.click = 1;
  }

  this.triggerMouseDown(opts);
  this.triggerMouseUp(opts);
  this.triggerMouseAction('click', opts);

  return this;
};

$.fn.triggerDoubleClick = function() {
  this.triggerClick();
  this.triggerClick({click: 2});
  this.trigger(jQuery.Event('dblclick', {
    originalEvent: jQuery.Event('dummy', {
      detail: 2
    })
  }));
  return this;
};
