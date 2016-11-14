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
/**
 * Provides formatting of numbers using java format pattern.
 * <p>
 * Compared to the java DecimalFormat the following pattern characters are not considered:
 * <ul>
 *   <li>prefix and suffix</li>
 *   <li>E</li>
 *   <li>%</li>
 * </ul>
 */
scout.DecimalFormat = function(locale, decimalFormatConfiguration) {
  // format function will use these (defaults)
  this.positivePrefix;
  this.positiveSuffix;
  this.negativePrefix;
  this.negativeSuffix;
  this.minusSign = locale.decimalFormatSymbols.minusSign;
  this.groupingChar = locale.decimalFormatSymbols.groupingSeparator;
  this.groupLength = 0;
  this.decimalSeparatorChar = locale.decimalFormatSymbols.decimalSeparator;
  this.zeroBefore = 1;
  this.zeroAfter = 0;
  this.allAfter = 0;

  decimalFormatConfiguration = decimalFormatConfiguration || {};
  this.pattern = decimalFormatConfiguration.pattern || locale.decimalFormatPatternDefault;
  this.multiplier = decimalFormatConfiguration.multiplier || 1;
  this.roundingMode = decimalFormatConfiguration.roundingMode || scout.numbers.RoundingMode.HALF_UP;

  var SYMBOLS = scout.DecimalFormat.PATTERN_SYMBOLS;
  // Check if there are separate subpatterns for positive and negative numbers ("PositivePattern;NegativePattern")
  var split = this.pattern.split(SYMBOLS.patternSeparator);
  // Use the first subpattern as positive prefix/suffix
  var positivePatternParts = scout.DecimalFormat.findPatternParts(split[0]);
  this.positivePrefix = positivePatternParts.prefix;
  this.positiveSuffix = positivePatternParts.suffix;
  this.numberPattern = positivePatternParts.number;
  if (split.length > 1) {
    // Yes, there is a negative subpattern
    var negativePatternParts = scout.DecimalFormat.findPatternParts(split[1]);
    this.negativePrefix = negativePatternParts.prefix;
    this.negativeSuffix = negativePatternParts.suffix;
    if (negativePatternParts.prefixMinusMask.indexOf(SYMBOLS.minusSign) !== -1) {
      this.minusSignInNegativePrefix = true;
    }
    if (negativePatternParts.suffixMinusMask.indexOf(SYMBOLS.minusSign) !== -1) {
      this.minusSignInNegativeSuffix = true;
    }
    // "number" part is ignored, positive and negative number pattern are the same
  } else {
    // No, there is no negative subpattern, so the positive prefix/suffix are used for both positive and negative numbers.
    this.negativePrefix = this.positivePrefix;
    this.negativeSuffix = this.positiveSuffix;
    // Check if there is a minus sign in the prefix/suffix.
    var prefixMinusSignIndex = positivePatternParts.prefixMinusMask.indexOf(SYMBOLS.minusSign);
    if (prefixMinusSignIndex !== -1) {
      // Yes, there is a minus sign in the prefix. Use this a negativePrefix and remove the minus sign from the posistivePrefix.
      while (prefixMinusSignIndex !== -1) {
        this.positivePrefix = replaceCharAt(this.positivePrefix, prefixMinusSignIndex, '');
        this.negativePrefix = replaceCharAt(this.negativePrefix, prefixMinusSignIndex, this.minusSign);
        prefixMinusSignIndex = positivePatternParts.prefixMinusMask.indexOf(SYMBOLS.minusSign, prefixMinusSignIndex + 1);
      }
      this.minusSignInNegativePrefix = true;
    }
    var suffixMinusSignIndex = positivePatternParts.suffixMinusMask.indexOf(SYMBOLS.minusSign);
    if (suffixMinusSignIndex !== -1) {
      // Yes, there is a minus sign in the suffix. Use this a negativeSuffix and remove the minus sign from the posistiveSuffix.
      while (suffixMinusSignIndex !== -1) {
        this.positiveSuffix = replaceCharAt(this.positiveSuffix, suffixMinusSignIndex, '');
        this.negativeSuffix = replaceCharAt(this.negativePrefix, suffixMinusSignIndex, this.minusSign);
        suffixMinusSignIndex = positivePatternParts.suffixMinusMask.indexOf(SYMBOLS.minusSign, suffixMinusSignIndex + 1);
      }
      this.minusSignInNegativeSuffix = true;
    }
    if (!this.minusSignInNegativePrefix && !this.minusSignInNegativeSuffix) {
      // No, there is no minus sign in the prefix/suffix. Therefore, automatically prepend the minus sign to the prefix.
      this.negativePrefix = this.minusSign + this.positivePrefix;
      this.minusSignInNegativePrefix = true;
    }
  }

  // find group length
  var posDecimalSeparator = this.numberPattern.indexOf(SYMBOLS.decimalSeparator);
  if (posDecimalSeparator === -1) {
    posDecimalSeparator = this.numberPattern.length; // assume decimal separator at end
  }
  var posGroupingSeparator = this.numberPattern.lastIndexOf(SYMBOLS.groupingSeparator, posDecimalSeparator); // only search before decimal separator
  if (posGroupingSeparator > 0) {
    this.groupLength = posDecimalSeparator - posGroupingSeparator - 1;
  }
  this.numberPattern = this.numberPattern.replace(new RegExp('[' + SYMBOLS.groupingSeparator + ']', 'g'), '');

  // split on decimal point
  split = this.numberPattern.split(SYMBOLS.decimalSeparator);

  // find digits before and after decimal point
  this.zeroBefore = scout.strings.count(split[0], SYMBOLS.zeroDigit);
  if (split.length > 1) { // has decimal point?
    this.zeroAfter = scout.strings.count(split[1], SYMBOLS.zeroDigit);
    this.allAfter = this.zeroAfter + scout.strings.count(split[1], SYMBOLS.digit);
  }

  // ----- Helper functions -----

  function replaceCharAt(s, pos, replacement) {
    return s.substring(0, pos) + replacement + s.substring(pos + 1);
  }
};

/**
 * Returns a number for the given numberString, if the string can be converted into a number.
 * Throws an Error otherwise
 */
scout.DecimalFormat.prototype.parse = function(numberString) {
  if (scout.strings.empty(numberString)) {
    return null;
  }
  var pureNumber = this.normalizeString(numberString);
  var number = Number(pureNumber);
  if (isNaN(number)) {
    throw new Error(numberString + ' is not a number (NaN)');
  }
  return number;
};

scout.DecimalFormat.prototype.normalizeString = function(numberString) {
  if (scout.strings.empty(numberString)) {
    return '';
  }
  var negativePrefixRegEx = new RegExp('^' + scout.strings.quote(this.negativePrefix));
  var negativeSuffixRegEx = new RegExp(scout.strings.quote(this.negativeSuffix) + '$');
  var minus = '';
  if ((this.minusSignInNegativePrefix && negativePrefixRegEx.test(numberString)) ||
      (this.minusSignInNegativeSuffix && negativeSuffixRegEx.test(numberString))) {
    minus = '-';
  }
  return minus + numberString
    .replace(new RegExp('^' + scout.strings.quote(this.positivePrefix)), '')
    .replace(new RegExp(scout.strings.quote(this.positiveSuffix) + '$'), '')
    .replace(negativePrefixRegEx, '')
    .replace(negativeSuffixRegEx, '')
    .replace(new RegExp('[' + this.groupingChar + ']', 'g'), '')
    .replace(new RegExp('[' + this.decimalSeparatorChar + ']', 'g'), '.')
    .replace(/\s/g, '');
};

scout.DecimalFormat.prototype.format = function(number, applyMultiplier) {
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
  before = scout.strings.padZeroLeft(before, this.zeroBefore);

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
scout.DecimalFormat.prototype.round = function(number) {
  return scout.numbers.round(number, this.roundingMode, this.allAfter);
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * Literal (not localized!) pattern symbols as defined in http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html
 */
scout.DecimalFormat.PATTERN_SYMBOLS = {
  digit: '#',
  zeroDigit: '0',
  decimalSeparator: '.',
  groupingSeparator: ',',
  minusSign: '-',
  patternSeparator: ';'
};

scout.DecimalFormat.ensure = function(locale, format) {
  if (!format) {
    return format;
  }
  if (format instanceof scout.DecimalFormat) {
    return format;
  }
  return new scout.DecimalFormat(locale, format);
};

/**
 * Returns an object with the properties 'prefix', 'number' and 'suffix'. Number contains
 * the part of the pattern that consists of 'digit-like' characters. Prefix and suffix
 * contain the literal part before and after the number part, respectively. Single quotes
 * that can be used to escape characters are removed from the result.
 */
scout.DecimalFormat.findPatternParts = function(pattern) {
  var result = {
    prefix: '',
    prefixMinusMask: '',
    number: '',
    suffix: '',
    suffixMinusMask: ''
  };
  var SYMBOLS = scout.DecimalFormat.PATTERN_SYMBOLS;
  // Pattern that matches digit-like characters
  var r = new RegExp('[' + SYMBOLS.digit + SYMBOLS.zeroDigit + SYMBOLS.decimalSeparator + SYMBOLS.groupingSeparator + ']');
  var escape = false;
  var scope = 'PREFIX';
  for (var i = 0; i < pattern.length; i++) {
    var ch = pattern.charAt(i);
    if (scope === 'PREFIX') {
      // prefix
      if (ch === '\'') { // toggle escape
        if (escape && pattern.charAt(i - 1) === '\'') { // two consecutive ' are equal to one literal '
          result.prefix += '\'';
          result.prefixMinusMask += ' ';
        }
        escape = !escape;
        continue;
      } else if (!escape && r.test(ch)) { // digit-like character, belongs to 'number' part
        scope = 'NUMBER';
      } else { // part of prefix
        result.prefix += ch;
        result.prefixMinusMask += (ch === SYMBOLS.minusSign && !escape ? '-' : ' ');
        continue;
      }
    }
    if (scope === 'NUMBER') {
      // number
      if (r.test(ch)) { // digit-like character
        result.number += ch;
        continue;
      } else { // number is finished, belongs to suffix
        scope = 'SUFFIX';
      }
    }
    if (scope === 'SUFFIX') {
      // suffix
      if (ch === '\'') { // toggle escape
        if (escape && pattern.charAt(i - 1) === '\'') { // two consecutive ' are equal to one literal '
          result.suffix += '\'';
          result.suffixMinusMask += ' ';
        }
        escape = !escape;
        continue;
      } else { // part of suffix
        result.suffix += ch;
        result.suffixMinusMask += (ch === SYMBOLS.minusSign && !escape ? '-' : ' ');
      }
    }
  }
  return result;
};
