/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

const jasmineScoutMatchers = {
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
