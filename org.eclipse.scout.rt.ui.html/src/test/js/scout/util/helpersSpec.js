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

  describe("nvl", function() {

    it("can return alternative value", function() {
      expect(scout.helpers.nvl()).toBe(undefined);
      expect(scout.helpers.nvl('X')).toBe('X');
      expect(scout.helpers.nvl('X', 'Y')).toBe('X');
      expect(scout.helpers.nvl(undefined)).toBe(undefined);
      expect(scout.helpers.nvl(undefined, undefined)).toBe(undefined);
      expect(scout.helpers.nvl(undefined, null)).toBe(null);
      expect(scout.helpers.nvl(undefined, '')).toBe('');
      expect(scout.helpers.nvl(undefined, 'X')).toBe('X');
      expect(scout.helpers.nvl(null, 'X')).toBe('X');
      expect(scout.helpers.nvl(null, '')).toBe('');
      expect(scout.helpers.nvl(null, undefined)).toBe(undefined);
      expect(scout.helpers.nvl(null, null)).toBe(null);
      expect(scout.helpers.nvl(null)).toBe(undefined);
      expect(scout.helpers.nvl(0, '123')).toBe(0);
      expect(scout.helpers.nvl(1, '123')).toBe(1);
      expect(scout.helpers.nvl(undefined, '123')).toBe('123');
      expect(scout.helpers.nvl(undefined, 123)).toBe(123);
      expect(scout.helpers.nvl(0.000000000000000000000001, -1)).toBe(0.000000000000000000000001);
      expect(scout.helpers.nvl({}, {x: 2})).toEqual({});
      expect(scout.helpers.nvl({y: undefined}, {x: 2})).toEqual({y: undefined});
      expect(scout.helpers.nvl(null, {x: 2})).toEqual({x: 2});
    });

  });

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
