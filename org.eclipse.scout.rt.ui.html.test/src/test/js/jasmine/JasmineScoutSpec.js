/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/*global jasmineScoutMatchers:false */
describe("JasmineScout", function() {

  describe("toContainEventTypesExactly", function() {

    function createRequestDataFixture() {
      var requestData = {};
      requestData.events = [];
      requestData.events.push(new scout.RemoteEvent(1, 'event1'));
      requestData.events.push(new scout.RemoteEvent(1, 'event2'));
      requestData.events.push(new scout.RemoteEvent(1, 'event3'));

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
