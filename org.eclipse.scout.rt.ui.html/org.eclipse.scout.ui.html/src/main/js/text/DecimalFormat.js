/**
 * Converts numbers to strings using java format pattern.
 * <p>
 * Compared to the java DecimalFormat the following pattern characters are not considered:
 * <ul>
 *   <li>prefix and suffix</li>
 *   <li>E</li>
 *   <li>%</li>
 * </ul>
 */
Scout.DecimalFormat = function(scout, pattern) {
  this._symbols = scout.locale.decimalFormatSymbols;

  //FIXME HALF_UP is the default, other rounding modes need to be considered as well.
  //this._roundingMode ='HALF_UP';

  // format function will use these (defaults)
  this.negPrefix = this._symbols.minusSign;
  this.negSuffix = '';
  this.groupChar = this._symbols.groupingSeparator;
  this.groupLength = 0;
  this.pointChar = this._symbols.decimalSeparator;
  this.zeroBefore = 1;
  this.zeroAfter = 0;
  this.allAfter = 0;

  //  find prefix and suffix for negative numbers
  var split = pattern.split(this._symbols.patternSeparator);
  if (split.length > 1) {
    this.negPrefix = split[1].slice(0, _find(split[1], this._symbols.zeroDigit + this._symbols.digit, 1));
    this.negSuffix = split[1].slice(_find(split[1], this._symbols.zeroDigit + this._symbols.digit, -1) + 1);
    pattern = split[0];
  }

  // find group length
  var start = _find(pattern, this._symbols.groupingSeparator, -1),
    end = _find(pattern, this._symbols.decimalSeparator, 1) || pattern.length;
  if (start && end) this.groupLength = end - start - 1;
  pattern = pattern.replace(this._symbols.groupingSeparator, '');

  // split on decimal point
  split = pattern.split(this._symbols.decimalSeparator);

  // find digits before and after decimal point
  this.zeroBefore = _count(split[0], this._symbols.zeroDigit);
  this.zeroAfter = _count(split[1], this._symbols.zeroDigit);
  this.allAfter = this.zeroAfter + _count(split[1], this._symbols.digit);

  // helper function
  function _find(string, chars, dir) {
    for (var i = ((dir == 1) ? 0 : string.length - 1); i < string.length && i > -1; i += dir) {
      if (chars.indexOf(string[i]) > -1) return i;
    }
    return null;
  }

  function _count(str, separator) {
    return str.split(separator).length - 1;
  }
};

Scout.DecimalFormat.prototype.format = function format(number) {
  if (number < 0) return this.negPrefix + this.format(-number) + this.negSuffix;

  // before decimal point
  var before = Math.floor(number);
  before = (before === 0) ? '' : String(before);
  before = (before.length >= this.zeroBefore) ? before : Array(this.zeroBefore - before.length + 1).join('0') + before;

  // group digits
  if (this.groupLength) {
    for (var i = before.length - this.groupLength; i > 0; i -= this.groupLength) {
      before = before.substr(0, i) + this.groupChar + before.substr(i);
    }
  }

  // after decimal point
  var after = number.toFixed(this.allAfter);
  after = after.slice(after.indexOf('.') + 1);
  for (var j = after.length - 1; j > this.zeroAfter - 1; j--) {
    if (after[j] != '0') break;
    after = after.slice(0, -1);
  }

  // put together and return
  return before + (after ? this.pointChar + after : '');
};
