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
describe("scout.numbers", function() {

  /**
   * Test cases copied & extended from {@link java.math.RoundingMode}
   */
  describe("round", function() {

    it ("tests special cases", function() {
      expect(scout.numbers.round(undefined, 0)).toBe(undefined);
      expect(scout.numbers.round(undefined)).toBe(undefined);
      expect(scout.numbers.round(undefined, 0, 0)).toBe(undefined);
      expect(scout.numbers.round(undefined, undefined, 0)).toBe(undefined);

      // HALF_UP applied by default
      expect(scout.numbers.round(0)).toBe(0);
      expect(scout.numbers.round(1.1)).toBe(1);
      expect(scout.numbers.round(1.5)).toBe(2);
      expect(scout.numbers.round(-1.5)).toBe(-2);
      expect(scout.numbers.round(-1.1)).toBe(-1);
      expect(scout.numbers.round(-0)).toBe(0);
    });

    it("tests rounding mode 'UP'", function() {
      var roundingMode = scout.numbers.RoundingMode.UP;
      expect(scout.numbers.round(5.51, roundingMode)).toBe(6);
      expect(scout.numbers.round(5.5, roundingMode)).toBe(6);
      expect(scout.numbers.round(2.5, roundingMode)).toBe(3);
      expect(scout.numbers.round(1.6, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.1, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.0, roundingMode)).toBe(1);
      expect(scout.numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.1, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-1.6, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-2.5, roundingMode)).toBe(-3);
      expect(scout.numbers.round(-2.5, roundingMode)).toBe(-3);
      expect(scout.numbers.round(-5.5, roundingMode)).toBe(-6);
      expect(scout.numbers.round(-5.51, roundingMode)).toBe(-6);

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe(-12.35);
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe(-12.35);
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.35);
    });

    it("tests rounding mode 'DOWN'", function() {
      var roundingMode = scout.numbers.RoundingMode.DOWN;
      expect(scout.numbers.round(5.51, roundingMode)).toBe(5);
      expect(scout.numbers.round(5.5, roundingMode)).toBe(5);
      expect(scout.numbers.round(2.5, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.6, roundingMode)).toBe(1);
      expect(scout.numbers.round(1.1, roundingMode)).toBe(1);
      expect(scout.numbers.round(1.0, roundingMode)).toBe(1);
      expect(scout.numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.1, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.6, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-2.5, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-5.5, roundingMode)).toBe(-5);
      expect(scout.numbers.round(-5.51, roundingMode)).toBe(-5);

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe(12.34);
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe(-12.34);
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe(12.34);
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe(-12.34);
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.34);
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.34);
    });

    it("tests rounding mode 'CEILING'", function() {
      var roundingMode = scout.numbers.RoundingMode.CEILING;
      expect(scout.numbers.round(5.51, roundingMode)).toBe(6);
      expect(scout.numbers.round(5.5, roundingMode)).toBe(6);
      expect(scout.numbers.round(2.5, roundingMode)).toBe(3);
      expect(scout.numbers.round(1.6, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.1, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.0, roundingMode)).toBe(1);
      expect(scout.numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.1, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.6, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-2.5, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-5.5, roundingMode)).toBe(-5);
      expect(scout.numbers.round(-5.51, roundingMode)).toBe(-5);

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe(-12.34);
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe(-12.34);
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.35);
    });

    it("tests rounding mode 'FLOOR'", function() {
      var roundingMode = scout.numbers.RoundingMode.FLOOR;
      expect(scout.numbers.round(5.51, roundingMode)).toBe(5);
      expect(scout.numbers.round(5.5, roundingMode)).toBe(5);
      expect(scout.numbers.round(2.5, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.6, roundingMode)).toBe(1);
      expect(scout.numbers.round(1.1, roundingMode)).toBe(1);
      expect(scout.numbers.round(1.0, roundingMode)).toBe(1);
      expect(scout.numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.1, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-1.6, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-2.5, roundingMode)).toBe(-3);
      expect(scout.numbers.round(-5.5, roundingMode)).toBe(-6);
      expect(scout.numbers.round(-5.51, roundingMode)).toBe(-6);

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe(12.34);
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe(-12.35);
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe(12.34);
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe(-12.35);
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.34);
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.34);
    });

    it("tests rounding mode 'HALF_UP'", function() {
      var roundingMode = scout.numbers.RoundingMode.HALF_UP;
      expect(scout.numbers.round(5.51, roundingMode)).toBe(6);
      expect(scout.numbers.round(5.5, roundingMode)).toBe(6);
      expect(scout.numbers.round(2.5, roundingMode)).toBe(3);
      expect(scout.numbers.round(1.6, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.1, roundingMode)).toBe(1);
      expect(scout.numbers.round(1.0, roundingMode)).toBe(1);
      expect(scout.numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.1, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.6, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-2.5, roundingMode)).toBe(-3);
      expect(scout.numbers.round(-5.5, roundingMode)).toBe(-6);
      expect(scout.numbers.round(-5.51, roundingMode)).toBe(-6);

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe(-12.35);
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe(-12.35);
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.35);
    });

    it("tests rounding mode 'HALF_DOWN'", function() {
      var roundingMode = scout.numbers.RoundingMode.HALF_DOWN;

      expect(scout.numbers.round(5.51, roundingMode)).toBe(6);
      expect(scout.numbers.round(5.5, roundingMode)).toBe(5);
      expect(scout.numbers.round(2.5, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.6, roundingMode)).toBe(2);
      expect(scout.numbers.round(1.1, roundingMode)).toBe(1);
      expect(scout.numbers.round(1.0, roundingMode)).toBe(1);
      expect(scout.numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.1, roundingMode)).toBe(-1);
      expect(scout.numbers.round(-1.6, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-2.5, roundingMode)).toBe(-2);
      expect(scout.numbers.round(-5.5, roundingMode)).toBe(-5);
      expect(scout.numbers.round(-5.51, roundingMode)).toBe(-6);

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe(-12.35);
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe(12.34);
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe(-12.34);
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.35);
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.34);
    });

  });

  describe("shiftDecimalPoint", function() {

    it ("can shift decimal point to left and right", function() {
      expect(scout.numbers.shiftDecimalPoint()).toBe(undefined);
      expect(scout.numbers.shiftDecimalPoint(null)).toBe(null);
      expect(scout.numbers.shiftDecimalPoint('')).toBe('');
      expect(scout.numbers.shiftDecimalPoint(1234500)).toBe(1234500);
      expect(scout.numbers.shiftDecimalPoint(1234500, 0)).toBe(1234500);
      expect(scout.numbers.shiftDecimalPoint(0.1234500, 0)).toBe(0.1234500);

      // to left
      expect(scout.numbers.shiftDecimalPoint(1234500, -1)).toBe(123450.0);
      expect(scout.numbers.shiftDecimalPoint(1234500, -2)).toBe(12345.00);
      expect(scout.numbers.shiftDecimalPoint(1234500, -10)).toBe(0.0001234500);
      expect(scout.numbers.shiftDecimalPoint(25, -1)).toBe(2.5);
      expect(scout.numbers.shiftDecimalPoint(25, -2)).toBe(0.25);
      expect(scout.numbers.shiftDecimalPoint(25, -3)).toBe(0.025);
      expect(scout.numbers.shiftDecimalPoint(25, -4)).toBe(0.0025);
      expect(scout.numbers.shiftDecimalPoint(25, -5)).toBe(0.00025);
      expect(scout.numbers.shiftDecimalPoint(0, -1)).toBe(0);
      expect(scout.numbers.shiftDecimalPoint(0, -99)).toBe(0);
      expect(scout.numbers.shiftDecimalPoint(-1234500, -1)).toBe(-123450.0);
      expect(scout.numbers.shiftDecimalPoint(-25, -5)).toBe(-0.00025);
      expect(scout.numbers.shiftDecimalPoint(0.001, -1)).toBe(0.0001);
      expect(scout.numbers.shiftDecimalPoint(0.1, -4)).toBe(0.00001);
      expect(scout.numbers.shiftDecimalPoint(-0.555e3, -3)).toBe(-0.555);
      expect(scout.numbers.shiftDecimalPoint(0.555e-3, -3)).toBe(0.000000555);

      // to right
      expect(scout.numbers.shiftDecimalPoint(1234500, 1)).toBe(12345000);
      expect(scout.numbers.shiftDecimalPoint(1234500, 2)).toBe(123450000);
      expect(scout.numbers.shiftDecimalPoint(1234500, 10)).toBe(12345000000000000);
      expect(scout.numbers.shiftDecimalPoint(0.0025, 1)).toBe(0.025);
      expect(scout.numbers.shiftDecimalPoint(0.0025, 2)).toBe(0.25);
      expect(scout.numbers.shiftDecimalPoint(0.0025, 3)).toBe(2.5);
      expect(scout.numbers.shiftDecimalPoint(0.0025, 4)).toBe(25);
      expect(scout.numbers.shiftDecimalPoint(0.0025, 5)).toBe(250);
      expect(scout.numbers.shiftDecimalPoint(0, 1)).toBe(0);
      expect(scout.numbers.shiftDecimalPoint(0, 99)).toBe(0);
      expect(scout.numbers.shiftDecimalPoint(-1234500, 1)).toBe(-12345000);
      expect(scout.numbers.shiftDecimalPoint(-25, 5)).toBe(-2500000);
      expect(scout.numbers.shiftDecimalPoint(-0.555e3, 3)).toBe(-555000);
      expect(scout.numbers.shiftDecimalPoint(0.555e-3, 3)).toBe(0.555);

      // corner cases (lost precision!)
      expect(scout.numbers.shiftDecimalPoint(0.0999999999999999, 1)).toBe(0.999999999999999);
      expect(scout.numbers.shiftDecimalPoint(1.0999999999999999, 1)).toBe(10.999999999999998);
      expect(scout.numbers.shiftDecimalPoint(2.0999999999999999, 1)).toBe(21);
    });

  });

  describe("randomId", function() {

    it ("can generate random IDs", function() {
      expect(typeof scout.numbers.randomId()).toBe('string');
      expect(scout.numbers.randomId().length).toBe(8);
      expect(scout.numbers.randomId(0).length).toBe(8);
      expect(scout.numbers.randomId(1).length).toBe(1);
      expect(scout.numbers.randomId(27).length).toBe(27);
      expect(scout.numbers.randomId()).not.toBe(scout.numbers.randomId());
    });

  });

  describe("correlationId", function() {

    it ("can generate random correlation IDs", function() {
      scout.numbers._correlationCounter = 4865;
      expect(typeof scout.numbers.correlationId()).toBe('string');
      expect(scout.numbers.correlationId().length).toBe(11 + 5);
      expect(scout.numbers.correlationId(0).length).toBe(11 + 5);
      expect(scout.numbers.correlationId(1).length).toBe(1 + 5);
      expect(scout.numbers.correlationId(27).length).toBe(27 + 5);
      expect(scout.numbers.correlationId()).toMatch(/^([a-zA-Z]{2}[0-9]){3}[a-zA-Z]{2}\/4870$/);
    });

  });

});
