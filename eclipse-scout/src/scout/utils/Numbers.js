export default class Numbers {

  static round(number, roundingMode, decimalPlaces) {
    if (number === null || number === undefined) {
      return number;
    }
    decimalPlaces = decimalPlaces || 0;

    // Do _not_ multiply with powers of 10 here, because that might cause rounding errors!
    // Example: 1.005 with 2 decimal places would result in 100.49999999999999
    number = Numbers.shiftDecimalPoint(number, decimalPlaces);

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

    return Numbers.shiftDecimalPoint(number, -decimalPlaces);
  }

  static shiftDecimalPoint(number, move) {
    if (number === null || number === undefined || !move) {
      return number;
    }

    var sign = (number ? (number < 0 ? -1 : 1) : 0);
    var distance = Math.abs(move);

    number = Math.abs(number);
    var s = scout.strings.asString(number);
    var a;
    if (move < 0) {
      // move to left
      s = scout.strings.repeat('0', distance) + s;
      a = s.split('.', 2);
      if (a.length === 1) {
        s = s.substr(0, s.length - distance) + '.' + s.substr(s.length - distance);
      } else {
        s = a[0].substr(0, a[0].length - distance) + '.' + a[0].substr(a[0].length - distance) + a[1];
      }
    } else if (move > 0) {
      // move to right
      s += scout.strings.repeat('0', distance);
      a = s.split('.', 2);
      if (a.length === 2) {
        s = a[0] + a[1].substr(0, distance) + '.' + a[1].substr(distance);
      }
    }
    // Remove multiple leading zeros to prevent interpretation as octal number
    s = s.replace(/^0*(\d)/g, '$1');
    return Number(s) * sign;
  }
}

export const RoundingMode = Object.freeze({
  UP: 'UP',
  DOWN: 'DOWN',
  CEILING: 'CEILING',
  FLOOR: 'FLOOR',
  HALF_UP: 'HALF_UP',
  HALF_DOWN: 'HALF_DOWN',
  HALF_EVEN: 'HALF_EVEN',
  UNNECESSARY: 'UNNECESSARY'
});
