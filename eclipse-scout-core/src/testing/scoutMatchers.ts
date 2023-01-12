/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// noinspection DuplicatedCode

import {RemoteEvent, Widget} from '../index';
import MatchersUtil = jasmine.MatchersUtil;
import CustomEqualityTester = jasmine.CustomEqualityTester;
import CustomMatcher = jasmine.CustomMatcher;
import CustomMatcherResult = jasmine.CustomMatcherResult;

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace jasmine {
    interface Matchers<T> {
      /**
       * Checks if the given remote request contains expected events, order does not matter.
       * Actual json request, may be obtained by {@link mostRecentJsonRequest}.
       */
      toContainEvents(events: RemoteEvent | RemoteEvent[]): void;

      /**
       * Checks if the given remote request contains all the expected events in the given order
       * Actual json request, may be obtained by {@link mostRecentJsonRequest}.
       */
      toContainEventsExactly(events: RemoteEvent | RemoteEvent[]): void;

      /**
       * Checks if the given remote request contains events with the expected event types in the given order
       * Actual json request, may be obtained by {@link mostRecentJsonRequest}.
       */
      toContainEventTypesExactly(events: string | string[]): void;

      /**
       * Checks if all given jQuery objects (array of jQuery objects) have a specific class.
       */
      allToHaveClass(cssClass: string): void;

      /**
       * Checks if any given jQuery object (array of jQuery objects) has a specific class.
       */
      anyToHaveClass(cssClass: string): void;

      /**
       * Checks if the given widget property was correctly cloned.
       */
      toHaveClonedWidgetProperty(original: Widget, property: string): void;
    }
  }
}

export const jasmineScoutMatchers = {

  toContainEvents: (util: MatchersUtil): CustomMatcher => ({
    compare: (actual, expected) => {
      if (expected === undefined) {
        expected = [];
      }
      if (!Array.isArray(expected)) {
        expected = [expected];
      }
      let result = {} as CustomMatcherResult;

      let actualEvents = [];
      if (actual) {
        for (let i = 0; i < actual.events.length; i++) {
          actualEvents.push(actual.events[i]);
        }
      }

      result.pass = true;
      for (let i = 0; i < expected.length; i++) {
        // Prototype may be Event. If that's the case we need to convert, otherwise equals will fail
        if (Object.getPrototypeOf(expected[i]) !== Object.prototype) {
          expected[i] = $.parseJSON(JSON.stringify(expected[i]));
        }

        result.pass = result.pass && util.contains(actualEvents, expected[i]);
      }

      if (!result.pass) {
        result.message = 'Expected actual events ' + actualEvents + ' to be equal to ' + expected;
      }
      return result;
    }
  }),

  toContainEventsExactly: (util: MatchersUtil): CustomMatcher => ({
    compare: (actual, expected) => {
      if (expected === undefined) {
        expected = [];
      }
      if (!Array.isArray(expected)) {
        expected = [expected];
      }
      let result = {} as CustomMatcherResult;

      let actualEvents = [];
      if (actual) {
        for (let i = 0; i < actual.events.length; i++) {
          actualEvents.push(actual.events[i]);
        }
      }

      result.pass = true;
      for (let i = 0; i < expected.length; i++) {
        // Prototype may be Event. If that's the case we need to convert, otherwise equals will fail
        if (Object.getPrototypeOf(expected[i]) !== Object.prototype) {
          expected[i] = $.parseJSON(JSON.stringify(expected[i]));
        }
      }

      result.pass = util.equals(actualEvents, expected);

      if (!result.pass) {
        result.message = 'Expected actual events ' + actualEvents + ' to be equal to ' + expected;
      }
      return result;
    }
  }),

  toContainEventTypesExactly: (util: MatchersUtil): CustomMatcher => ({
    compare: (actual, expected) => {
      if (expected === undefined) {
        expected = [];
      }
      if (!Array.isArray(expected)) {
        expected = [expected];
      }
      let result = {} as CustomMatcherResult;

      let actualEventTypes = [];
      if (actual) {
        for (let i = 0; i < actual.events.length; i++) {
          actualEventTypes.push(actual.events[i].type);
        }
      }

      result.pass = util.equals(actualEventTypes, expected);

      if (!result.pass) {
        result.message = 'Expected actual event types ' + actualEventTypes + ' to be equal to ' + expected;
      }
      return result;
    }
  }),

  allToHaveClass: (util: MatchersUtil, customEqualityTesters?: ReadonlyArray<CustomEqualityTester>): CustomMatcher => ({
    compare: (actual, expected) => {
      if (expected === undefined) {
        expected = [];
      }
      if (!Array.isArray(expected)) {
        expected = [expected];
      }
      if (!Array.isArray(actual)) {
        actual = [actual];
      }
      let result = {
        pass: actual.every($elem => {
          return $elem.hasClass(expected);
        })
      } as CustomMatcherResult;

      if (!result.pass) {
        result.message = 'Expected ' + actual + ' all to have ' + expected + ' as classes.';
      }
      return result;
    }
  }),

  anyToHaveClass: (util: MatchersUtil, customEqualityTesters?: ReadonlyArray<CustomEqualityTester>): CustomMatcher => ({
    compare: (actual, expected) => {
      if (expected === undefined) {
        expected = [];
      }
      if (!Array.isArray(expected)) {
        expected = [expected];
      }
      if (!Array.isArray(actual)) {
        actual = [actual];
      }
      let result = {
        pass: actual.some($elem => {
          return $elem.hasClass(expected);
        })
      } as CustomMatcherResult;

      if (!result.pass) {
        result.message = 'Expected any ' + actual + ' to have ' + expected + ' as classes.';
      }
      return result;
    }
  }),

  toHaveClonedWidgetProperty: (util: MatchersUtil, customEqualityTesters?: ReadonlyArray<CustomEqualityTester>): CustomMatcher => ({
    compare: (clone, original, property) => {
      let compareWidget = (originalWidget, clonedWidget, propertyName) => {
        if (originalWidget === clonedWidget) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' is same on [original: \'' + original[property] + '\', clone: \'' + clone[property] + '\']. It should be a deep copy.'
          };
        }
        if (originalWidget.objectType !== clonedWidget.objectType) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' has not same object type of clone and original. [original.objectType: \'' + originalWidget.objectType + '\', clonedWidget.objectType: \'' + clonedWidget.objectType + '\'].'
          };
        }
        if (clonedWidget.parent !== clone) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' has a wrong parent in clone (widget parent and clone should be same). [clone: \'' + clone + '\', widget.parent: \'' + clonedWidget.parent + '\'].'
          };
        }
        if (originalWidget !== clonedWidget.cloneOf) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' cloneOf of clone is not set correctly. [original: \'' + originalWidget + '\', clone.cloneOf: \'' + clonedWidget.cloneOf + '\'].'
          };
        }
        return {
          pass: true
        };
      };

      if (original[property] === clone[property]) {
        return {
          pass: false,
          message: 'widgetProperty \'' + property + '\' is same on [original: \'' + original[property] + '\', clone: \'' + clone[property] + '\']. It should be a deep copy.'
        };
      }
      if (Array.isArray(original[property])) {
        if (!Array.isArray(clone[property])) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' is not an array [original: \'' + original[property] + '\', clone: \'' + clone[property] + '\']. It should be a deep copy.'
          };
        }
        for (let i = 0; i < original[property].length; i++) {
          let result = compareWidget(original[property][i], clone[property][i], property);
          if (!result.pass) {
            return result;
          }
        }
      }
      return {
        pass: true
      };
    }
  })
};
