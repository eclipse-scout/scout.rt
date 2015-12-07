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
      expect(scout.numbers.round(0)).toBe('0');
      expect(scout.numbers.round(1.1)).toBe('1');
      expect(scout.numbers.round(1.5)).toBe('2');
      expect(scout.numbers.round(-1.5)).toBe('-2');
      expect(scout.numbers.round(-1.1)).toBe('-1');
      expect(scout.numbers.round(-0)).toBe('0');
    });

    it("tests rounding mode 'UP'", function() {
      var roundingMode = scout.numbers.RoundingMode.UP;
      expect(scout.numbers.round(5.51, roundingMode)).toBe('6');
      expect(scout.numbers.round(5.5, roundingMode)).toBe('6');
      expect(scout.numbers.round(2.5, roundingMode)).toBe('3');
      expect(scout.numbers.round(1.6, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.1, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.0, roundingMode)).toBe('1');
      expect(scout.numbers.round(-1.0, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.1, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-1.6, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-2.5, roundingMode)).toBe('-3');
      expect(scout.numbers.round(-2.5, roundingMode)).toBe('-3');
      expect(scout.numbers.round(-5.5, roundingMode)).toBe('-6');
      expect(scout.numbers.round(-5.51, roundingMode)).toBe('-6');

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe('-12.35');
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe('-12.35');
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe('12.35');
    });

    it("tests rounding mode 'DOWN'", function() {
      var roundingMode = scout.numbers.RoundingMode.DOWN;
      expect(scout.numbers.round(5.51, roundingMode)).toBe('5');
      expect(scout.numbers.round(5.5, roundingMode)).toBe('5');
      expect(scout.numbers.round(2.5, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.6, roundingMode)).toBe('1');
      expect(scout.numbers.round(1.1, roundingMode)).toBe('1');
      expect(scout.numbers.round(1.0, roundingMode)).toBe('1');
      expect(scout.numbers.round(-1.0, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.1, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.6, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-2.5, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-5.5, roundingMode)).toBe('-5');
      expect(scout.numbers.round(-5.51, roundingMode)).toBe('-5');

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe('12.34');
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe('-12.34');
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe('12.34');
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe('-12.34');
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe('12.34');
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe('12.34');
    });

    it("tests rounding mode 'CEILING'", function() {
      var roundingMode = scout.numbers.RoundingMode.CEILING;
      expect(scout.numbers.round(5.51, roundingMode)).toBe('6');
      expect(scout.numbers.round(5.5, roundingMode)).toBe('6');
      expect(scout.numbers.round(2.5, roundingMode)).toBe('3');
      expect(scout.numbers.round(1.6, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.1, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.0, roundingMode)).toBe('1');
      expect(scout.numbers.round(-1.0, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.1, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.6, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-2.5, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-5.5, roundingMode)).toBe('-5');
      expect(scout.numbers.round(-5.51, roundingMode)).toBe('-5');

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe('-12.34');
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe('-12.34');
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe('12.35');
    });

    it("tests rounding mode 'FLOOR'", function() {
      var roundingMode = scout.numbers.RoundingMode.FLOOR;
      expect(scout.numbers.round(5.51, roundingMode)).toBe('5');
      expect(scout.numbers.round(5.5, roundingMode)).toBe('5');
      expect(scout.numbers.round(2.5, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.6, roundingMode)).toBe('1');
      expect(scout.numbers.round(1.1, roundingMode)).toBe('1');
      expect(scout.numbers.round(1.0, roundingMode)).toBe('1');
      expect(scout.numbers.round(-1.0, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.1, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-1.6, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-2.5, roundingMode)).toBe('-3');
      expect(scout.numbers.round(-5.5, roundingMode)).toBe('-6');
      expect(scout.numbers.round(-5.51, roundingMode)).toBe('-6');

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe('12.34');
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe('-12.35');
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe('12.34');
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe('-12.35');
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe('12.34');
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe('12.34');
    });

    it("tests rounding mode 'HALF_UP'", function() {
      var roundingMode = scout.numbers.RoundingMode.HALF_UP;
      expect(scout.numbers.round(5.51, roundingMode)).toBe('6');
      expect(scout.numbers.round(5.5, roundingMode)).toBe('6');
      expect(scout.numbers.round(2.5, roundingMode)).toBe('3');
      expect(scout.numbers.round(1.6, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.1, roundingMode)).toBe('1');
      expect(scout.numbers.round(1.0, roundingMode)).toBe('1');
      expect(scout.numbers.round(-1.0, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.1, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.6, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-2.5, roundingMode)).toBe('-3');
      expect(scout.numbers.round(-5.5, roundingMode)).toBe('-6');
      expect(scout.numbers.round(-5.51, roundingMode)).toBe('-6');

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe('-12.35');
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe('-12.35');
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe('12.35');
    });

    it("tests rounding mode 'HALF_DOWN'", function() {
      var roundingMode = scout.numbers.RoundingMode.HALF_DOWN;

      expect(scout.numbers.round(5.51, roundingMode)).toBe('6');
      expect(scout.numbers.round(5.5, roundingMode)).toBe('5');
      expect(scout.numbers.round(2.5, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.6, roundingMode)).toBe('2');
      expect(scout.numbers.round(1.1, roundingMode)).toBe('1');
      expect(scout.numbers.round(1.0, roundingMode)).toBe('1');
      expect(scout.numbers.round(-1.0, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.1, roundingMode)).toBe('-1');
      expect(scout.numbers.round(-1.6, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-2.5, roundingMode)).toBe('-2');
      expect(scout.numbers.round(-5.5, roundingMode)).toBe('-5');
      expect(scout.numbers.round(-5.51, roundingMode)).toBe('-6');

      expect(scout.numbers.round(12.3456, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(-12.3456, roundingMode, 2)).toBe('-12.35');
      expect(scout.numbers.round(12.345, roundingMode, 2)).toBe('12.34');
      expect(scout.numbers.round(-12.345, roundingMode, 2)).toBe('-12.34');
      expect(scout.numbers.round(12.34500000000001, roundingMode, 2)).toBe('12.35');
      expect(scout.numbers.round(12.34500000000000, roundingMode, 2)).toBe('12.34');
    });
  });

});
