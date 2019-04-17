import * as scout from '../scout'; // FIXME [awe] IntelliJ: how to use/define imports? relative, absolute, $basePath variable? Check what IDE prefers/supports.
import * as numbers from '../utils/numbers';
import * as strings from '../utils/strings';

/**
 * Literal (not localized!) pattern symbols as defined in http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html
 */
const PATTERN_SYMBOLS = Object.freeze({
  digit: '#',
  zeroDigit: '0',
  decimalSeparator: '.',
  groupingSeparator: ',',
  minusSign: '-',
  patternSeparator: ';'
});

export default class DecimalFormat {

  constructor(locale, options) {
    // format function will use these (defaults)
    this.positivePrefix = '';
    this.positiveSuffix = '';
    this.negativePrefix = locale.decimalFormatSymbols.minusSign;
    this.negativeSuffix = '';
    this.groupingChar = locale.decimalFormatSymbols.groupingSeparator;
    this.groupLength = 0;
    this.decimalSeparatorChar = locale.decimalFormatSymbols.decimalSeparator;
    this.zeroBefore = 1;
    this.zeroAfter = 0;
    this.allAfter = 0;

    if (typeof options === 'string') {
      this.pattern = options;
    }
    options = options || {};
    this.pattern = this.pattern || options.pattern || locale.decimalFormatPatternDefault;
    this.multiplier = options.multiplier || 1;
    this.roundingMode = options.roundingMode || numbers.RoundingMode.HALF_UP;

    // Check if there are separate subpatterns for positive and negative numbers ('PositivePattern;NegativePattern')
    var split = this.pattern.split(PATTERN_SYMBOLS.patternSeparator);
    // Use the first subpattern as positive prefix/suffix
    var positivePrefixAndSuffix = findPrefixAndSuffix(split[0]);
    this.positivePrefix = positivePrefixAndSuffix.prefix;
    this.positiveSuffix = positivePrefixAndSuffix.suffix;
    if (split.length > 1) {
      // Yes, there is a negative subpattern
      var negativePrefixAndSuffix = findPrefixAndSuffix(split[1]);
      this.negativePrefix = negativePrefixAndSuffix.prefix;
      this.negativeSuffix = negativePrefixAndSuffix.suffix;
      // from now on, only look at the positive subpattern
      this.pattern = split[0];
    } else {
      // No, there is no negative subpattern, so the positive prefix/suffix are used for both positive and negative numbers.
      // Check if there is a minus sign in the prefix/suffix.
      if (this.positivePrefix.indexOf(PATTERN_SYMBOLS.minusSign) !== -1 || this.positiveSuffix.indexOf(PATTERN_SYMBOLS.minusSign) !== -1) {
        // Yes, there is a minus sign in the prefix/suffix. Use this a negativePrefix/Suffix and remove the minus sign from the posistivePrefix/Suffix.
        this.negativePrefix = this.positivePrefix.replace(PATTERN_SYMBOLS.minusSign, locale.decimalFormatSymbols.minusSign);
        this.negativeSuffix = this.positiveSuffix.replace(PATTERN_SYMBOLS.minusSign, locale.decimalFormatSymbols.minusSign);
        this.positivePrefix = this.positivePrefix.replace(PATTERN_SYMBOLS.minusSign, '');
        this.positiveSuffix = this.positiveSuffix.replace(PATTERN_SYMBOLS.minusSign, '');
      } else {
        // No, there is no minus sign in the prefix/suffix. Therefore, use the default negativePrefix/Suffix, but append the positivePrefix/Suffix
        this.negativePrefix = this.positivePrefix + this.negativePrefix;
        this.negativeSuffix = this.negativeSuffix + this.positiveSuffix;
      }
    }

    // find group length
    var posDecimalSeparator = this.pattern.indexOf(PATTERN_SYMBOLS.decimalSeparator);
    if (posDecimalSeparator === -1) {
      posDecimalSeparator = this.pattern.length; // assume decimal separator at end
    }
    var posGroupingSeparator = this.pattern.lastIndexOf(PATTERN_SYMBOLS.groupingSeparator, posDecimalSeparator); // only search before decimal separator
    if (posGroupingSeparator > 0) {
      this.groupLength = posDecimalSeparator - posGroupingSeparator - 1;
    }
    this.pattern = this.pattern.replace(new RegExp('[' + PATTERN_SYMBOLS.groupingSeparator + ']', 'g'), '');

    // split on decimal point
    split = this.pattern.split(PATTERN_SYMBOLS.decimalSeparator);

    // find digits before and after decimal point
    this.zeroBefore = strings.count(split[0], PATTERN_SYMBOLS.zeroDigit);
    if (split.length > 1) { // has decimal point?
      this.zeroAfter = strings.count(split[1], PATTERN_SYMBOLS.zeroDigit);
      this.allAfter = this.zeroAfter + strings.count(split[1], PATTERN_SYMBOLS.digit);
    }

    // Returns an object with the properties 'prefix' and 'suffix', which contain all characters
    // before or after any 'digit-like' character in the given pattern string.
    function findPrefixAndSuffix(pattern) {
      var result = {
        prefix: '',
        suffix: ''
      };
      // Find prefix (anything before the first 'digit-like' character)
      var digitLikeCharacters = PATTERN_SYMBOLS.digit + PATTERN_SYMBOLS.zeroDigit + PATTERN_SYMBOLS.decimalSeparator + PATTERN_SYMBOLS.groupingSeparator;
      var r = new RegExp('^(.*?)[' + digitLikeCharacters + '].*$');
      var matches = r.exec(pattern);
      if (matches !== null) {
        // Ignore single quotes (for special, quoted characters - e.g. Java quotes percentage sign like '%')
        result.prefix = matches[1].replace(new RegExp('\'([^\']+)\'', 'g'), '$1');
      }
      // Find suffix (anything before the first 'digit-like' character)
      r = new RegExp('^.*[' + digitLikeCharacters + '](.*?)$');
      matches = r.exec(pattern);
      if (matches !== null) {
        // Ignore single quotes (for special, quoted characters - e.g. Java quotes percentage sign like '%')
        result.suffix = matches[1].replace(new RegExp('\'([^\']+)\'', 'g'), '$1');
      }
      return result;
    }
  }

  parse(numberString) {
    if (strings.empty(numberString)) {
      return null;
    }
    var pureNumber = this.normalize(numberString);
    var number = Number(pureNumber);
    if (isNaN(number)) {
      throw new Error(numberString + ' is not a number (NaN)');
    }
    return number;
  };

  format(number, applyMultiplier) {
    applyMultiplier = scout.nvl(applyMultiplier, true);
    if (number === null || number === undefined) {
      return null;
    }

    var prefix = this.positivePrefix;
    var suffix = this.positiveSuffix;

    // apply multiplier
    if (applyMultiplier && this.multiplier !== 1) {
      number *= this.multiplier;
    }

    // round
    number = this.round(number);

    // after decimal point
    var after = '';
    if (this.allAfter) {
      after = number.toFixed(this.allAfter).split('.')[1];
      for (var j = after.length - 1; j > this.zeroAfter - 1; j--) {
        if (after[j] !== '0') {
          break;
        }
        after = after.slice(0, -1);
      }
      if (after) { // did we find any non-zero characters?
        after = this.decimalSeparatorChar + after;
      }
    }

    // absolute value
    if (number < 0) {
      prefix = this.negativePrefix;
      suffix = this.negativeSuffix;
      number = -number;
    }

    // before decimal point
    var before = Math.floor(number);
    before = (before === 0) ? '' : String(before);
    before = strings.padZeroLeft(before, this.zeroBefore);

    // group digits
    if (this.groupLength) {
      for (var i = before.length - this.groupLength; i > 0; i -= this.groupLength) {
        before = before.substr(0, i) + this.groupingChar + before.substr(i);
      }
    }

    // put together and return
    return prefix + before + after + suffix;
  };

  /**
   * Rounds a number according to the properties of the DecimalFormat.
   */
  round(number, applyMultiplier) {
    applyMultiplier = scout.nvl(applyMultiplier, true);
    if (number === null || number === undefined) {
      return null;
    }

    // apply multiplier
    if (applyMultiplier && this.multiplier !== 1) {
      number *= this.multiplier;
    }
    // round
    number = numbers.round(number, this.roundingMode, this.allAfter);
    // un-apply multiplier
    if (applyMultiplier && this.multiplier !== 1) {
      number /= this.multiplier;
    }
    return number;
  };

  /**
   * Convert to JS number format (remove groupingChar, replace decimalSeparatorChar with '.')
   */
  normalize(numberString) {
    if (!numberString) {
      return numberString;
    }
    return numberString
      .replace(new RegExp('[' + this.groupingChar + ']', 'g'), '')
      .replace(new RegExp('[' + this.decimalSeparatorChar + ']', 'g'), '.')
      .replace(/\s/g, '');
  };

  static ensure(locale, format) {
    if (!format) {
      return format;
    }
    if (format instanceof DecimalFormat) {
      return format;
    }
    return new DecimalFormat(locale, format);
  };
}
