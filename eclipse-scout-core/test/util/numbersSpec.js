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
import * as numbers from '../../src/util/numbers';
import RoundingMode from '../../src/util/RoundingMode';

describe('scout.numbers', () => {

  /**
   * Test cases copied & extended from java.math.RoundingMode
   */
  describe('round', () => {

    it('tests special cases', () => {
      expect(numbers.round(undefined, 0)).toBe(undefined);
      expect(numbers.round(undefined)).toBe(undefined);
      expect(numbers.round(undefined, 0, 0)).toBe(undefined);
      expect(numbers.round(undefined, undefined, 0)).toBe(undefined);

      // HALF_UP applied by default
      expect(numbers.round(0)).toBe(0);
      expect(numbers.round(1.1)).toBe(1);
      expect(numbers.round(1.5)).toBe(2);
      expect(numbers.round(-1.5)).toBe(-2);
      expect(numbers.round(-1.1)).toBe(-1);
      expect(numbers.round(-0)).toBe(0);
    });

    it('tests rounding mode \'UP\'', () => {
      let roundingMode = RoundingMode.UP;
      expect(numbers.round(5.51, roundingMode)).toBe(6);
      expect(numbers.round(5.5, roundingMode)).toBe(6);
      expect(numbers.round(2.5, roundingMode)).toBe(3);
      expect(numbers.round(1.6, roundingMode)).toBe(2);
      expect(numbers.round(1.1, roundingMode)).toBe(2);
      expect(numbers.round(1.0, roundingMode)).toBe(1);
      expect(numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(numbers.round(-1.1, roundingMode)).toBe(-2);
      expect(numbers.round(-1.6, roundingMode)).toBe(-2);
      expect(numbers.round(-2.5, roundingMode)).toBe(-3);
      expect(numbers.round(-2.5, roundingMode)).toBe(-3);
      expect(numbers.round(-5.5, roundingMode)).toBe(-6);
      expect(numbers.round(-5.51, roundingMode)).toBe(-6);

      expect(numbers.round(12.3456, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(-12.3456, roundingMode, 2)).toBe(-12.35);
      expect(numbers.round(12.345, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(-12.345, roundingMode, 2)).toBe(-12.35);
      expect(numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.35);
    });

    it('tests rounding mode \'DOWN\'', () => {
      let roundingMode = RoundingMode.DOWN;
      expect(numbers.round(5.51, roundingMode)).toBe(5);
      expect(numbers.round(5.5, roundingMode)).toBe(5);
      expect(numbers.round(2.5, roundingMode)).toBe(2);
      expect(numbers.round(1.6, roundingMode)).toBe(1);
      expect(numbers.round(1.1, roundingMode)).toBe(1);
      expect(numbers.round(1.0, roundingMode)).toBe(1);
      expect(numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(numbers.round(-1.1, roundingMode)).toBe(-1);
      expect(numbers.round(-1.6, roundingMode)).toBe(-1);
      expect(numbers.round(-2.5, roundingMode)).toBe(-2);
      expect(numbers.round(-5.5, roundingMode)).toBe(-5);
      expect(numbers.round(-5.51, roundingMode)).toBe(-5);

      expect(numbers.round(12.3456, roundingMode, 2)).toBe(12.34);
      expect(numbers.round(-12.3456, roundingMode, 2)).toBe(-12.34);
      expect(numbers.round(12.345, roundingMode, 2)).toBe(12.34);
      expect(numbers.round(-12.345, roundingMode, 2)).toBe(-12.34);
      expect(numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.34);
      expect(numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.34);
    });

    it('tests rounding mode \'CEILING\'', () => {
      let roundingMode = RoundingMode.CEILING;
      expect(numbers.round(5.51, roundingMode)).toBe(6);
      expect(numbers.round(5.5, roundingMode)).toBe(6);
      expect(numbers.round(2.5, roundingMode)).toBe(3);
      expect(numbers.round(1.6, roundingMode)).toBe(2);
      expect(numbers.round(1.1, roundingMode)).toBe(2);
      expect(numbers.round(1.0, roundingMode)).toBe(1);
      expect(numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(numbers.round(-1.1, roundingMode)).toBe(-1);
      expect(numbers.round(-1.6, roundingMode)).toBe(-1);
      expect(numbers.round(-2.5, roundingMode)).toBe(-2);
      expect(numbers.round(-5.5, roundingMode)).toBe(-5);
      expect(numbers.round(-5.51, roundingMode)).toBe(-5);

      expect(numbers.round(12.3456, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(-12.3456, roundingMode, 2)).toBe(-12.34);
      expect(numbers.round(12.345, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(-12.345, roundingMode, 2)).toBe(-12.34);
      expect(numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.35);
    });

    it('tests rounding mode \'FLOOR\'', () => {
      let roundingMode = RoundingMode.FLOOR;
      expect(numbers.round(5.51, roundingMode)).toBe(5);
      expect(numbers.round(5.5, roundingMode)).toBe(5);
      expect(numbers.round(2.5, roundingMode)).toBe(2);
      expect(numbers.round(1.6, roundingMode)).toBe(1);
      expect(numbers.round(1.1, roundingMode)).toBe(1);
      expect(numbers.round(1.0, roundingMode)).toBe(1);
      expect(numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(numbers.round(-1.1, roundingMode)).toBe(-2);
      expect(numbers.round(-1.6, roundingMode)).toBe(-2);
      expect(numbers.round(-2.5, roundingMode)).toBe(-3);
      expect(numbers.round(-5.5, roundingMode)).toBe(-6);
      expect(numbers.round(-5.51, roundingMode)).toBe(-6);

      expect(numbers.round(12.3456, roundingMode, 2)).toBe(12.34);
      expect(numbers.round(-12.3456, roundingMode, 2)).toBe(-12.35);
      expect(numbers.round(12.345, roundingMode, 2)).toBe(12.34);
      expect(numbers.round(-12.345, roundingMode, 2)).toBe(-12.35);
      expect(numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.34);
      expect(numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.34);
    });

    it('tests rounding mode \'HALF_UP\'', () => {
      let roundingMode = RoundingMode.HALF_UP;
      expect(numbers.round(5.51, roundingMode)).toBe(6);
      expect(numbers.round(5.5, roundingMode)).toBe(6);
      expect(numbers.round(2.5, roundingMode)).toBe(3);
      expect(numbers.round(1.6, roundingMode)).toBe(2);
      expect(numbers.round(1.1, roundingMode)).toBe(1);
      expect(numbers.round(1.0, roundingMode)).toBe(1);
      expect(numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(numbers.round(-1.1, roundingMode)).toBe(-1);
      expect(numbers.round(-1.6, roundingMode)).toBe(-2);
      expect(numbers.round(-2.5, roundingMode)).toBe(-3);
      expect(numbers.round(-5.5, roundingMode)).toBe(-6);
      expect(numbers.round(-5.51, roundingMode)).toBe(-6);

      expect(numbers.round(12.3456, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(-12.3456, roundingMode, 2)).toBe(-12.35);
      expect(numbers.round(12.345, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(-12.345, roundingMode, 2)).toBe(-12.35);
      expect(numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.35);
    });

    it('tests rounding mode \'HALF_DOWN\'', () => {
      let roundingMode = RoundingMode.HALF_DOWN;

      expect(numbers.round(5.51, roundingMode)).toBe(6);
      expect(numbers.round(5.5, roundingMode)).toBe(5);
      expect(numbers.round(2.5, roundingMode)).toBe(2);
      expect(numbers.round(1.6, roundingMode)).toBe(2);
      expect(numbers.round(1.1, roundingMode)).toBe(1);
      expect(numbers.round(1.0, roundingMode)).toBe(1);
      expect(numbers.round(-1.0, roundingMode)).toBe(-1);
      expect(numbers.round(-1.1, roundingMode)).toBe(-1);
      expect(numbers.round(-1.6, roundingMode)).toBe(-2);
      expect(numbers.round(-2.5, roundingMode)).toBe(-2);
      expect(numbers.round(-5.5, roundingMode)).toBe(-5);
      expect(numbers.round(-5.51, roundingMode)).toBe(-6);

      expect(numbers.round(12.3456, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(-12.3456, roundingMode, 2)).toBe(-12.35);
      expect(numbers.round(12.345, roundingMode, 2)).toBe(12.34);
      expect(numbers.round(-12.345, roundingMode, 2)).toBe(-12.34);
      expect(numbers.round(12.34500000000001, roundingMode, 2)).toBe(12.35);
      expect(numbers.round(12.34500000000000, roundingMode, 2)).toBe(12.34);
    });

  });

  describe('shiftDecimalPoint', () => {

    it('can shift decimal point to left and right', () => {
      expect(numbers.shiftDecimalPoint()).toBe(undefined);
      expect(numbers.shiftDecimalPoint(null)).toBe(null);
      expect(numbers.shiftDecimalPoint('')).toBe('');
      expect(numbers.shiftDecimalPoint(1234500)).toBe(1234500);
      expect(numbers.shiftDecimalPoint(1234500, 0)).toBe(1234500);
      expect(numbers.shiftDecimalPoint(0.1234500, 0)).toBe(0.1234500);

      // to left
      expect(numbers.shiftDecimalPoint(1234500, -1)).toBe(123450.0);
      expect(numbers.shiftDecimalPoint(1234500, -2)).toBe(12345.00);
      expect(numbers.shiftDecimalPoint(1234500, -10)).toBe(0.0001234500);
      expect(numbers.shiftDecimalPoint(1e10, -10)).toBe(1);
      expect(numbers.shiftDecimalPoint(25, -1)).toBe(2.5);
      expect(numbers.shiftDecimalPoint(25, -2)).toBe(0.25);
      expect(numbers.shiftDecimalPoint(25, -3)).toBe(0.025);
      expect(numbers.shiftDecimalPoint(25, -4)).toBe(0.0025);
      expect(numbers.shiftDecimalPoint(25, -5)).toBe(0.00025);
      expect(numbers.shiftDecimalPoint(0, -1)).toBe(0);
      expect(numbers.shiftDecimalPoint(0, -99)).toBe(0);
      expect(numbers.shiftDecimalPoint(-1234500, -1)).toBe(-123450.0);
      expect(numbers.shiftDecimalPoint(-25, -5)).toBe(-0.00025);
      expect(numbers.shiftDecimalPoint(0.001, -1)).toBe(0.0001);
      expect(numbers.shiftDecimalPoint(0.1, -4)).toBe(0.00001);
      expect(numbers.shiftDecimalPoint(-0.555e3, -3)).toBe(-0.555);
      expect(numbers.shiftDecimalPoint(0.555e-3, -3)).toBe(0.000000555);
      expect(numbers.shiftDecimalPoint(1e-8, -3)).toBe(0.00000000001);

      // to right
      expect(numbers.shiftDecimalPoint(1234500, 1)).toBe(12345000);
      expect(numbers.shiftDecimalPoint(1234500, 2)).toBe(123450000);
      expect(numbers.shiftDecimalPoint(1234500, 10)).toBe(12345000000000000);
      expect(numbers.shiftDecimalPoint(0.0025, 1)).toBe(0.025);
      expect(numbers.shiftDecimalPoint(0.0025, 2)).toBe(0.25);
      expect(numbers.shiftDecimalPoint(0.0025, 3)).toBe(2.5);
      expect(numbers.shiftDecimalPoint(0.0025, 4)).toBe(25);
      expect(numbers.shiftDecimalPoint(0.0025, 5)).toBe(250);
      expect(numbers.shiftDecimalPoint(0, 1)).toBe(0);
      expect(numbers.shiftDecimalPoint(0, 99)).toBe(0);
      expect(numbers.shiftDecimalPoint(-1234500, 1)).toBe(-12345000);
      expect(numbers.shiftDecimalPoint(-25, 5)).toBe(-2500000);
      expect(numbers.shiftDecimalPoint(-0.555e3, 3)).toBe(-555000);
      expect(numbers.shiftDecimalPoint(0.555e-3, 3)).toBe(0.555);

      // corner cases (lost precision!)
      expect(numbers.shiftDecimalPoint(0.0999999999999999, 1)).toBe(0.999999999999999);
      expect(numbers.shiftDecimalPoint(1.0999999999999999, 1)).toBe(10.999999999999998);
      // eslint-disable-next-line
      expect(numbers.shiftDecimalPoint(2.0999999999999999, 1)).toBe(21);
    });

  });

  describe('randomId', () => {

    it('can generate random IDs', () => {
      expect(typeof numbers.randomId()).toBe('string');
      expect(numbers.randomId().length).toBe(8);
      expect(numbers.randomId(0).length).toBe(8);
      expect(numbers.randomId(1).length).toBe(1);
      expect(numbers.randomId(27).length).toBe(27);
      expect(numbers.randomId()).not.toBe(numbers.randomId());
    });

  });

  describe('correlationId', () => {

    it('can generate random correlation IDs', () => {
      numbers._setCorrelationCounter(4865);
      expect(typeof numbers.correlationId()).toBe('string');
      expect(numbers.correlationId().length).toBe(11 + 5);
      expect(numbers.correlationId(0).length).toBe(11 + 5);
      expect(numbers.correlationId(1).length).toBe(1 + 5);
      expect(numbers.correlationId(27).length).toBe(27 + 5);
      expect(numbers.correlationId()).toMatch(/^([a-zA-Z]{2}[0-9]){3}[a-zA-Z]{2}\/4870$/);
    });

  });

  describe('isNumber', () => {

    it('returns true if the value is a number', () => {
      expect(numbers.isNumber(3)).toBe(true);
    });

    it('returns false if the value is not a number', () => {
      expect(numbers.isNumber('3')).toBe(false);
    });

    it('returns false if the value is NaN', () => {
      expect(numbers.isNumber(NaN)).toBe(false);
    });

  });

});
