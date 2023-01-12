/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {objects, RoundingMode, strings} from '../index';

export const numbers = {
  /**
   * Converts the given decimal number to base-62 (i.e. the same value, but represented by [a-zA-Z0-9] instead of only [0-9]).
   */
  toBase62(number: number): string {
    if (number === undefined) {
      return undefined;
    }
    let symbols = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'.split('');
    let base = 62;
    let s = '';
    let n;
    while (number >= 1) {
      n = Math.floor(number / base);
      s = symbols[(number - (base * n))] + s;
      number = n;
    }
    return s;
  },

  /**
   * Returns a random sequence of characters out of the set [a-zA-Z0-9] with the given length.
   * @param length The default length is 8.
   */
  randomId(length?: number): string {
    length = length || 8;
    let alphabet = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
      result += alphabet[Math.floor(Math.random() * alphabet.length)];
    }
    return result;
  },

  /** @internal */
  _correlationCounter: 1,

  /**
   * Generates a random ID suitable for use as correlation ID.
   *
   * Example:
   *
   *   Hq5JY2kz3n/27
   *
   * The ID is generated from two different alphabets: 1. only letter, 2. only digits. By
   * always selecting a random digit after two random characters, accidental "rude words"
   * can be prevented.
   *
   * The characters[01olOL] are not used at all because they are easily confused.
   *
   * For a length of 11 (default), this method can theoretically generate over 200 trillion
   * different IDs:
   *
   *   46^7 * 8^3 = 223'138'640'494'592
   *
   * To further reduce the risk of collisions, a monotonically increasing counter is added
   * at the end of the result string (separated by "/").
   */
  correlationId(length?: number): string {
    length = length || 11;
    let letters = 'abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ';
    let digits = '23456789';
    let result = '';
    for (let i = 0; i < length; i++) {
      if ((i + 1) % 3 === 0) {
        result += digits[Math.floor(Math.random() * digits.length)];
      } else {
        result += letters[Math.floor(Math.random() * letters.length)];
      }
    }
    return result + '/' + (numbers._correlationCounter++);
  },

  /**
   * Rounds a number to the given number of decimal places.
   *
   * Numbers should not be rounded with the built-in Number.toFixed() method, since it
   * behaves differently on different browsers. However, it is safe to apply toFixed()
   * to the result of this method to ensure a fixed number of decimal places (filled up
   * with 0's) because this operation does not involve any rounding anymore.
   * <p>
   * If decimalPlaces is omitted, the number will be rounded to integer by default.
   * Rounding mode {@link RoundingMode.HALF_UP} is used as default.
   */
  round(number: number, roundingMode?: RoundingMode, decimalPlaces?: number): number {
    if (number === null || number === undefined) {
      return number;
    }
    decimalPlaces = decimalPlaces || 0;

    // Do _not_ multiply with powers of 10 here, because that might cause rounding errors!
    // Example: 1.005 with 2 decimal places would result in 100.49999999999999
    number = numbers.shiftDecimalPoint(number, decimalPlaces);

    switch (roundingMode) {
      case RoundingMode.UP:
        if (number < 0) {
          number = -Math.ceil(Math.abs(number));
        } else {
          number = Math.ceil(number);
        }
        break;
      case RoundingMode.DOWN:
        if (number < 0) {
          number = -Math.floor(Math.abs(number));
        } else {
          number = Math.floor(number);
        }
        break;
      case RoundingMode.CEILING:
        number = Math.ceil(number);
        break;
      case RoundingMode.FLOOR:
        number = Math.floor(number);
        break;
      case RoundingMode.HALF_DOWN:
        if (number < 0) {
          number = Math.round(number);
        } else {
          number = -Math.round(-number);
        }
        break;
      // case RoundingMode.HALF_EVEN:
      // case RoundingMode.UNNECESSARY:
      // not implemented, default is used.
      default:
        // RoundingMode.HALF_UP is used as default
        if (number < 0) {
          number = -Math.round(Math.abs(number));
        } else {
          number = Math.round(number);
        }
    }

    return numbers.shiftDecimalPoint(number, -decimalPlaces);
  },

  /**
   * Shifts the decimal point in the given number by a certain distance. While the result is also
   * number, the method uses string operations to move the decimal point. This prevents rounding
   * errors as long as the number does not exceed JavaScript's Number precision.
   *
   * The argument 'move' describes the distance how far the decimal point should be moved:
   *     0 = do no move      (1.57 --> 1.57)
   *   > 0 = move to right   (1.57 --> 15.7)
   *   < 0 = move to left    (1.57 --> 0.157)
   */
  shiftDecimalPoint(number: number, move: number): number {
    if (number === null || number === undefined || !move) {
      return number;
    }

    let sign = (number ? (number < 0 ? -1 : 1) : 0);
    let distance = Math.abs(move);

    number = Math.abs(number);
    let s = strings.asString(number);
    if (s.indexOf('e') !== -1) {
      s = number.toFixed(20);
    }
    let a;
    if (move < 0) {
      // move to left
      s = strings.repeat('0', distance) + s;
      a = s.split('.', 2);
      if (a.length === 1) {
        s = s.substr(0, s.length - distance) + '.' + s.substr(s.length - distance);
      } else {
        s = a[0].substr(0, a[0].length - distance) + '.' + a[0].substr(a[0].length - distance) + a[1];
      }
    } else if (move > 0) {
      // move to right
      s += strings.repeat('0', distance);
      a = s.split('.', 2);
      if (a.length === 2) {
        s = a[0] + a[1].substr(0, distance) + '.' + a[1].substr(distance);
      }
    }
    // Remove multiple leading zeros to prevent interpretation as octal number
    s = s.replace(/^0*(\d)/g, '$1');
    return Number(s) * sign;
  },

  /**
   * Ensures that the given number is really a number.
   * <p>
   * If it already is a number, the number will be returned.
   * Otherwise, a Number is created.
   *
   * @param number may be of type number or string.
   */
  ensure(number: number | string): number {
    if (objects.isNullOrUndefined(number)) {
      return number as number;
    }
    return Number(number);
  },

  /**
   * Returns true if the given number is of type number but not NaN.
   */
  isNumber(number: any): boolean {
    return typeof number === 'number' && !isNaN(number);
  },

  /**
   * Returns true if the given number is an integer.
   */
  isInteger(number: any): boolean {
    return numbers.isNumber(number) && isFinite(number) && Math.floor(number) === number;
  },

  /** @internal */
  _setCorrelationCounter(val: number) {
    numbers._correlationCounter = val;
  }
};
