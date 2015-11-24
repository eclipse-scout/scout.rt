/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("scout.helpers", function() {

  describe("isOneOf", function() {

    it("can check if value is one of multiple values", function() {
      expect(scout.helpers.isOneOf()).toBe(false);
      expect(scout.helpers.isOneOf('test')).toBe(false);
      expect(scout.helpers.isOneOf('test', 'bla')).toBe(false);
      expect(scout.helpers.isOneOf('test', {test: 'test'})).toBe(false);
      expect(scout.helpers.isOneOf('test', 'bla', 123, {test: 'test'})).toBe(false);
      expect(scout.helpers.isOneOf('test', 'bla', 123, {test: 'test'}, 'test', true)).toBe(true);
      expect(scout.helpers.isOneOf('test', 'bla', 123, {test: 'test'}, ['test'], true)).toBe(false);
      expect(scout.helpers.isOneOf('test', 'bla', 123, {test: 'test'}, 'Test', true)).toBe(false);
      expect(scout.helpers.isOneOf('test', ['bla', 123, {test: 'test'}, 'test', true])).toBe(true);
      expect(scout.helpers.isOneOf(123, '123', 123.00000000000001, -123)).toBe(false);
      expect(scout.helpers.isOneOf(-123, '123', 123.00000000000001, -123)).toBe(true);
    });

  });

});
