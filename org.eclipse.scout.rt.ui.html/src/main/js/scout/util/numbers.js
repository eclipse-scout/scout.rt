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
scout.numbers = {

  /**
   * Converts the given decimal number to base-62 (i.e. the same value, but
   * represented by [a-zA-Z0-9] instead of only [0-9].
   */
  toBase62: function(number) {
    if (number === undefined) {
      return undefined;
    }
    var symbols = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'.split('');
    var base = 62;
    var s = '';
    var n;
    while (number >= 1) {
      n = Math.floor(number / base);
      s = symbols[(number - (base * n))] + s;
      number = n;
    }
    return s;
  },

  /**
   * Returns a random sequence of characters out of the set [a-zA-Z0-9] with the
   * given length. The default length is 8.
   */
  randomId: function(length) {
    length = (length !== undefined) ? length : 8;
    var charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    var s = '';
    for (var i = 0; i < length; i++) {
      s += charset[Math.floor(Math.random() * charset.length)];
    }
    return s;
  },

  /**
   * Rounds number to any number of decimal places.
   * <p>
   * If decimalPlaces is omitted, the number will be rounded to integer by default.
   * Rounding mode {@link scout.numbers.RoundingMode.HALF_UP} is used as default.
   */
  round: function(number, roundingMode, decimalPlaces) {
    if (number === null || number === undefined) {
      return number;
    }
    decimalPlaces = decimalPlaces || 0;

    // avoid usage of toFixed on number to round since it behaves differently on different browsers.
    var multiplier = Math.pow(10, decimalPlaces);
    number *= multiplier;
    switch (roundingMode) {
      case scout.numbers.RoundingMode.UP:
        if (number < 0) {
          number = -Math.ceil(Math.abs(number));
        } else {
          number = Math.ceil(number);
        }
        break;
      case scout.numbers.RoundingMode.DOWN:
        if (number < 0) {
          number = -Math.floor(Math.abs(number));
        } else {
          number = Math.floor(number);
        }
        break;
      case scout.numbers.RoundingMode.CEILING:
        number = Math.ceil(number);
        break;
      case scout.numbers.RoundingMode.FLOOR:
        number = Math.floor(number);
        break;
      case scout.numbers.RoundingMode.HALF_DOWN:
        if (number < 0) {
          number = Math.round(number);
        } else {
          number = -Math.round(-number);
        }
        break;
        // case scout.numbers.RoundingMode.HALF_EVEN:
        // case scout.numbers.RoundingMode.UNNECESSARY:
        // not implemented, default is used.
      default:
        // scout.numbers.RoundingMode.HALF_UP is used as default
        if (number < 0) {
          number = -Math.round(Math.abs(number));
        } else {
          number = Math.round(number);
        }
    }
    number /= multiplier;
    // crop to decimal places
    return number.toFixed(decimalPlaces);
  }

};

/**
 * Enum providing rounding-modes for number columns and fields.
 *
 * @see RoundingMode.java
 */
scout.numbers.RoundingMode = {
  UP: 'UP',
  DOWN: 'DOWN',
  CEILING: 'CEILING',
  FLOOR: 'FLOOR',
  HALF_UP: 'HALF_UP',
  HALF_DOWN: 'HALF_DOWN',
  HALF_EVEN: 'HALF_EVEN',
  UNNECESSARY: 'UNNECESSARY'
};
