/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import jasmineScoutMatchers from '../../src/testing/scoutMatchers';

describe('JasmineScout', () => {

  describe('toContainEventTypesExactly', () => {

    function createRequestDataFixture() {
      let requestData = {};
      requestData.events = [];
      requestData.events.push({target: 1, type: 'event1'});
      requestData.events.push({target: 1, type: 'event2'});
      requestData.events.push({target: 1, type: 'event3'});
      return requestData;
    }

    it('checks whether event types in correct order', () => {
      let matcher = jasmineScoutMatchers.toContainEventTypesExactly(jasmine.matchersUtil),
        result;

      let requestData = createRequestDataFixture();

      result = matcher.compare(requestData, ['event1', 'event2', 'event3']);
      expect(result.pass).toBeTruthy();

      result = matcher.compare(requestData, ['event3', 'event2', 'event2']);
      expect(result.pass).toBeFalsy();

      result = matcher.compare(requestData, ['event1', 'event3', 'event2']);
      expect(result.pass).toBeFalsy();
    });

    it('checks whether event types are complete', () => {
      let matcher = jasmineScoutMatchers.toContainEventTypesExactly(jasmine.matchersUtil),
        result;

      let requestData = createRequestDataFixture();

      result = matcher.compare(requestData, ['event1', 'event2']);
      expect(result.pass).toBeFalsy();

      result = matcher.compare(requestData, ['abc', 'event2', 'event2']);
      expect(result.pass).toBeFalsy();
    });

    it('considers empty values', () => {
      let matcher = jasmineScoutMatchers.toContainEventTypesExactly(jasmine.matchersUtil),
        result;

      let requestData = createRequestDataFixture();

      result = matcher.compare(requestData, []);
      expect(result.pass).toBeFalsy();

      requestData = {};
      requestData.events = [];

      result = matcher.compare(requestData, []);
      expect(result.pass).toBeTruthy();
    });
  });

});
