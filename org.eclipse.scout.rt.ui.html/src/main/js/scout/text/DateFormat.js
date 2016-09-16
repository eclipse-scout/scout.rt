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
 * Custom JavaScript Date Format
 *
 * Support for formatting and parsing dates based on a pattern string and some locale
 * information from the server model. A subset of the standard Java pattern strings
 * (see SimpleDateFormat) with the most commonly used patterns is supported.
 *
 * This object only operates on the local time zone.
 * <p>
 * locale.dateFormatSymbols contains:
 * <ul>
 * <li>weekdays start with Sunday (starts at 0 and not 1 as it does in java)</li>
 * <li>weekdaysShort start with Sunday (starts at 0 and not 1 as it does in java)</li>
 * <li>months start with January</li>
 * <li>monthsShort start with January<7li>
 * <li>am</li>
 * <li>pm</li>
 *</ul>
 *
 * @see http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
 */
scout.DateFormatPatternType = {
  YEAR: 'year',
  MONTH: 'month',
  WEEK_IN_YEAR: 'week_in_year',
  DAY_IN_MONTH: 'day_in_month',
  WEEKDAY: 'weekday',
  HOUR_24: 'hour_24',
  HOUR_12: 'hour_12',
  AM_PM: 'am_pm',
  MINUTE: 'minute',
  SECOND: 'second',
  MILLISECOND: 'millisecond'
};

scout.DateFormat = function(locale, pattern) { // NOSONAR
  /*jshint sub:true*/
  this.locale = locale;
  if (this.locale === undefined || this.locale === null) {
    throw new Error('Missing locale');
  }
  this.pattern = pattern || locale.dateFormatPatternDefault;
  if (this.pattern === undefined || this.pattern === null) {
    throw new Error('Missing pattern');
  }

  this.symbols = locale.dateFormatSymbols;
  this.symbols.firstDayOfWeek = 1; // monday //FIXME cgu: deliver from server
  this.symbols.weekdaysOrdered = scout.dates.orderWeekdays(this.symbols.weekdays, this.symbols.firstDayOfWeek);
  this.symbols.weekdaysShortOrdered = scout.dates.orderWeekdays(this.symbols.weekdaysShort, this.symbols.firstDayOfWeek);
  this.symbols.monthsToNumber;
  this.symbols.monthsShortToNumber;

  // List of terms, e.g. split up parts of this.pattern. The length of this array is equal
  // to the length of this._formatFunctions, this._parseFunctions and this._analyzeFunctions.
  this._terms = [];

  // List of format function to be called _in that exact order_ to convert this.pattern
  // to a formatted date string (by sequentially replacing all terms with real values).
  this._formatFunctions = [];

  // List of parse functions to be called _in that exact order_ to convert an input
  // string to a valid JavaScript Date object. This order matches the recognized terms
  // in the pattern. Unrecognized terms are represented by a "constant" function that
  // matches the string itself (e.g. separator characters or spaces).
  this._parseFunctions = [];

  // Array of arrays, same order as _parseFunctions, but term functions are a list of term functions (to support lenient parsing)
  this._analyzeFunctions = [];

  var _dateFormat = this;

  // Local helper (constructor) functions
  /**
   * Definition of a date format pattern.
   *
   * A definition provides the following properties (most of which can be set with the 'options' argument):
   *
   * type:
   *   The "group" where the pattern definition belongs to. E.g. "dd" and "d" belong to the same group "day".
   *   This is used during analysis to find other definitions for the same type. Use one of the constants
   *   defined in scout.DateFormatPatternType.
   *
   * terms:
   *   An array consisting of all pattern terms that this particular definition can handle. Multiple
   *   terms with the same meaning may be accepted (e.g. "yyy", "yy" and "y" can be all used for a
   *   2-digit year formatting, but different parsing rules may apply).
   *
   * acceptedTerm:
   *   Indicates which of the "terms" exactly was accepted for the date pattern.
   *
   * dateFormat:
   *   Reference to the corresponding dateFormat object.
   *
   * formatFunction:
   *   An optional function that is used to format this particular term.
   *   @param formatContext
   *            See documentation at _createFormatContext().
   *   @return
   *            The function may return a string as result. If a string is returned, it is appended
   *            to the formatContext.formattedString automatically. If the formatFunction already did
   *            this, it should return "undefined".
   *
   * parseRegExp:
   *   A optional JavaScript RegExp object that is applied to the input string to extract this
   *   definition's term. The expression _must_ use exactly two capturing groups:
   *   [1] = matched part of the input
   *   [2] = remaining input (will be parsed later by other definitions)
   *   Example: /^(\d{4})(.*)$/
   *
   * applyMatchFunction:
   *   If 'parseRegExp' is set and found a match, and this function is defined, it is called
   *   to apply the matched part to the parseContext.
   *   @param parseContext
   *            See documentation at _createParseContext().
   *   @param match
   *            The first match from the reg exp.
   *   @return
   *            No return value.
   *
   * parseFunction:
   *   If parsing is not possible with a regular expression, this function may be defined to execute
   *   more complex parse logic.
   *   @param parseContext
   *            See documentation at _createParseContext().
   *   @return
   *            A string with the matched part of the input, or null if it did not match.
   */
  var DateFormatPatternDefinition = function(options) { // NOSONAR
    options = options || {};
    this.type = options.type;
    // List of termins meaning the same thing
    if (Array.isArray(options.terms)) {
      this.terms = options.terms;
    } else {
      this.terms = [options.terms];
    }
    this.acceptedTerm = null; // set by accept()
    this.dateFormat = _dateFormat;
    this.formatFunction = options.formatFunction && options.formatFunction.bind(this);
    this.parseRegExp = options.parseRegExp;
    this.applyMatchFunction = options.applyMatchFunction && options.applyMatchFunction.bind(this);
    this.parseFunction = options.parseFunction && options.parseFunction.bind(this);
  };

  DateFormatPatternDefinition.prototype.createFormatFunction = function() {
    return function(formatContext) {
      if (this.formatFunction) {
        var result = this.formatFunction(formatContext);
        if (result !== undefined) { // convenience
          formatContext.formattedString += result;
        }
      }
    }.bind(this);
  };

  DateFormatPatternDefinition.prototype.createParseFunction = function() {
    return function(parseContext) {
      var m, parsedTerm, match;

      var success = false;
      if (this.parseRegExp) {
        // RegEx handling (default)
        m = this.parseRegExp.exec(parseContext.inputString);
        if (m) { // match found
          if (this.applyMatchFunction) {
            this.applyMatchFunction(parseContext, m[1]);
          }
          match = m[1];
          // update remaining string
          parseContext.inputString = m[2];
          success = true;
        }
      }
      if (!success && this.parseFunction) {
        // Custom function
        match = this.parseFunction(parseContext);
        if (match !== null) {
          success = true;
        }
      }

      if (success) {
        // If patternDefinition accepts more than one term, try to choose
        // the form that matches the length of the match.
        parsedTerm = this.terms[0];
        if (this.terms.length > 1) {
          this.terms.some(function(term) {
            if (term.length === match.length) {
              parsedTerm = term;
              return true; // found
            }
            return false; // look further
          });
        }
        parseContext.parsedPattern += parsedTerm;
      }
      return success;
    }.bind(this);
  };

  DateFormatPatternDefinition.prototype.accept = function(term) {
    if (term) {
      // Check if one of the terms matches
      for (var i = 0; i < this.terms.length; i++) {
        if (term === this.terms[i]) {
          this.acceptedTerm = this.terms[i];
          return true;
        }
      }
    }
    this.acceptedTerm = null;
    return false;
  };

  // Build a list of all pattern definitions. This list is then used to build the list of
  // format, parse and analyze functions according to this.pattern.
  //
  // !!! PLEASE NOTE !!!
  // The order of these definitions is important! For each term in the pattern, the list
  // is scanned from the beginning until a definition accepts the term. If the wrong
  // definition was picked, results would be unpredictable.
  //
  // Following the following rules ensures that the algorithm can pick the best matching
  // pattern format definition for each term in the pattern:
  // - Sort definitions by time span, from large (year) to small (milliseconds).
  // - Two definitions of the same type should be sorted by term length, from long
  //   (e.g. MMMM) to short (e.g. M).
  this._patternDefinitions = [
    // --- Year ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.YEAR,
      terms: 'yyyy',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft(formatContext.inputDate.getFullYear(), 4).slice(-4);
      },
      parseRegExp: /^(\d{4})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.matchInfo.year = match;
        parseContext.dateInfo.year = Number(match);
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.YEAR,
      terms: ['yyy', 'yy', 'y'],
      formatFunction: function(formatContext) {
        var year = String(formatContext.inputDate.getFullYear());
        var length = (formatContext.exactLength ? this.acceptedTerm.length : 2);
        if (this.acceptedTerm.length === 1) {
          // Return max. 2 digits, no leading zero
          return year.slice(-length);
        }
        // Return max. 2 digits with zero padding
        return scout.strings.padZeroLeft(year, length).slice(-length);
      },
      parseRegExp: /^(\d{1,3})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        if (match.length === 3) {
          parseContext.dateInfo.year = Number(match);
          parseContext.matchInfo.year = match;
          return;
        }
        var shortInputYear = Number(match);
        var startDate = parseContext.startDate || new Date();
        var shortStartYear = Number(String(startDate.getFullYear()).slice(-2));
        var rangeMin = (shortStartYear - 50 < 0 ? shortStartYear + 50 : shortStartYear - 50);
        var yearPrefix = String(scout.strings.padZeroLeft(startDate.getFullYear(), 4)).substr(0, 2);
        if (shortInputYear > rangeMin) {
          yearPrefix = Number(yearPrefix) - 1;
        }
        var year = Number(String(yearPrefix) + scout.strings.padZeroLeft(shortInputYear, 2));
        parseContext.dateInfo.year = year;
        parseContext.matchInfo.year = match;
      }
    }),
    // --- Month ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.MONTH,
      terms: 'MMMM',
      formatFunction: function(formatContext) {
        return this.dateFormat.symbols.months[formatContext.inputDate.getMonth()];
      },
      parseFunction: function(parseContext) {
        var i, symbol, re, m;
        for (i = 0; i < this.dateFormat.symbols.months.length; i++) {
          symbol = this.dateFormat.symbols.months[i];
          if (!symbol) {
            continue; // Ignore empty symbols (otherwise, pattern would match everything)
          }
          re = new RegExp('^(' + scout.strings.quote(symbol) + ')(.*)$', 'i');
          m = re.exec(parseContext.inputString);
          if (m) { // match found
            parseContext.dateInfo.month = i;
            parseContext.matchInfo.month = m[1];
            parseContext.inputString = m[2];
            return m[1];
          }
        }
        // No match found so far. In analyze mode, check prefixes.
        if (parseContext.analyze) {
          for (i = 0; i < this.dateFormat.symbols.months.length; i++) {
            symbol = this.dateFormat.symbols.months[i];
            re = new RegExp('^(' + scout.strings.quote(parseContext.inputString) + ')(.*)$', 'i');
            m = re.exec(symbol);
            if (m) { // match found
              parseContext.dateInfo.month = i;
              parseContext.matchInfo.month = symbol;
              parseContext.inputString = '';
              return m[1];
            }
          }
        }
        return null; // no match found
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.MONTH,
      terms: 'MMM',
      formatFunction: function(formatContext) {
        return this.dateFormat.symbols.monthsShort[formatContext.inputDate.getMonth()];
      },
      parseFunction: function(parseContext) {
        var i, symbol, re, m;
        for (i = 0; i < this.dateFormat.symbols.monthsShort.length; i++) {
          symbol = this.dateFormat.symbols.monthsShort[i];
          if (!symbol) {
            continue; // Ignore empty symbols (otherwise, pattern would match everything)
          }
          re = new RegExp('^(' + scout.strings.quote(symbol) + ')(.*)$', 'i');
          m = re.exec(parseContext.inputString);
          if (m) { // match found
            parseContext.dateInfo.month = i;
            parseContext.matchInfo.month = m[1];
            parseContext.inputString = m[2];
            return m[1];
          }
        }
        // No match found so far. In analyze mode, check prefixes.
        if (parseContext.analyze) {
          for (i = 0; i < this.dateFormat.symbols.monthsShort.length; i++) {
            symbol = this.dateFormat.symbols.monthsShort[i];
            re = new RegExp('^(' + scout.strings.quote(parseContext.inputString) + ')(.*)$', 'i');
            m = re.exec(symbol);
            if (m) { // match found
              parseContext.dateInfo.month = i;
              parseContext.matchInfo.month = symbol;
              parseContext.inputString = '';
              return m[1];
            }
          }
        }
        return null; // no match found
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.MONTH,
      terms: 'MM',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft(formatContext.inputDate.getMonth() + 1, 2);
      },
      parseRegExp: /^(\d{2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        var month = Number(match);
        parseContext.dateInfo.month = month - 1;
        parseContext.matchInfo.month = match;
      },
      parseFunction: function(parseContext) {
        // Special case! When regexp did not match, check if input is '0'. In this case (and only
        // if we are in analyze mode), predict '01' as input.
        if (parseContext.analyze) {
          if (parseContext.inputString === '0') {
            // Use current dateInfo to create a date
            var date = this.dateFormat._dateInfoToDate(parseContext.dateInfo);
            if (!date) {
              return null; // parsing failed (dateInfo does not seem to contain a valid string)
            }
            var month = date.getMonth();
            if (month >= 9) {
              month = 0;
              if (parseContext.dateInfo.year === undefined) {
                parseContext.dateInfo.year = Number(date.getFullYear()) + 1;
              } else {
                parseContext.dateInfo.year = parseContext.dateInfo.year + 1;
              }
            }
            parseContext.dateInfo.month = month;
            parseContext.matchInfo.month = scout.strings.padZeroLeft(String(month + 1), 2);
            parseContext.inputString = '';
            return '0';
          }
        }
        return null; // no match found
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.MONTH,
      terms: 'M',
      formatFunction: function(formatContext) {
        return String(formatContext.inputDate.getMonth() + 1);
      },
      parseRegExp: /^(\d{1,2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        var month = Number(match);
        parseContext.dateInfo.month = month - 1;
        parseContext.matchInfo.month = match;
      }
    }),
    // --- Week in year ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.WEEK_IN_YEAR,
      terms: 'ww',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft(scout.dates.weekInYear(formatContext.inputDate), 2);
      },
      parseRegExp: /^(\d{2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.matchInfo.week = match;
        parseContext.hints.weekInYear = Number(match);
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.WEEK_IN_YEAR,
      terms: 'w',
      formatFunction: function(formatContext) {
        return String(scout.dates.weekInYear(formatContext.inputDate));
      },
      parseRegExp: /^(\d{1,2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.matchInfo.week = match;
        parseContext.hints.weekInYear = Number(match);
      }
    }),
    // --- Day in month ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.DAY_IN_MONTH,
      terms: 'dd',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft(formatContext.inputDate.getDate(), 2);
      },
      parseRegExp: /^(\d{2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.day = Number(match);
        parseContext.matchInfo.day = match;
      },
      parseFunction: function(parseContext) {
        // Special case! When regexp did not match, check if input is '0'. In this case (and only
        // if we are in analyze mode), predict '01' as input.
        if (parseContext.analyze) {
          if (parseContext.inputString === '0') {
            parseContext.dateInfo.day = 1;
            parseContext.matchInfo.day = '01';
            parseContext.inputString = '';
            return '0';
          }
        }
        return null; // no match found
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.DAY_IN_MONTH,
      terms: 'd',
      formatFunction: function(formatContext) {
        return String(formatContext.inputDate.getDate());
      },
      parseRegExp: /^(\d{1,2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.day = Number(match);
        parseContext.matchInfo.day = match;
      }
    }),
    // --- Weekday ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.WEEKDAY,
      terms: 'EEEE',
      formatFunction: function(formatContext) {
        return this.dateFormat.symbols.weekdays[formatContext.inputDate.getDay()];
      },
      parseFunction: function(parseContext) {
        var i, symbol, re, m;
        for (i = 0; i < this.dateFormat.symbols.weekdays.length; i++) {
          symbol = this.dateFormat.symbols.weekdays[i];
          if (!symbol) {
            continue; // Ignore empty symbols (otherwise, pattern would match everything)
          }
          re = new RegExp('^(' + scout.strings.quote(symbol) + ')(.*)$', 'i');
          m = re.exec(parseContext.inputString);
          if (m) { // match found
            parseContext.matchInfo.weekday = m[1];
            parseContext.hints.weekday = i;
            parseContext.inputString = m[2];
            return m[1];
          }
        }
        // No match found so far. In analyze mode, check prefixes.
        if (parseContext.analyze) {
          for (i = 0; i < this.dateFormat.symbols.weekdays.length; i++) {
            symbol = this.dateFormat.symbols.weekdays[i];
            re = new RegExp('^(' + scout.strings.quote(parseContext.inputString) + ')(.*)$', 'i');
            m = re.exec(symbol);
            if (m) { // match found
              parseContext.matchInfo.weekday = symbol;
              parseContext.hints.weekday = i;
              parseContext.inputString = '';
              return m[1];
            }
          }
        }
        return null; // no match found
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.WEEKDAY,
      terms: ['EEE', 'EE', 'E'],
      formatFunction: function(formatContext) {
        return this.dateFormat.symbols.weekdaysShort[formatContext.inputDate.getDay()];
      },
      parseFunction: function(parseContext) {
        var i, symbol, re, m;
        for (i = 0; i < this.dateFormat.symbols.weekdaysShort.length; i++) {
          symbol = this.dateFormat.symbols.weekdaysShort[i];
          if (!symbol) {
            continue; // Ignore empty symbols (otherwise, pattern would match everything)
          }
          re = new RegExp('^(' + scout.strings.quote(symbol) + ')(.*)$', 'i');
          m = re.exec(parseContext.inputString);
          if (m) { // match found
            parseContext.matchInfo.weekday = m[1];
            parseContext.hints.weekday = i;
            parseContext.inputString = m[2];
            return m[1];
          }
        }
        // No match found so far. In analyze mode, check prefixes.
        if (parseContext.analyze) {
          for (i = 0; i < this.dateFormat.symbols.weekdaysShort.length; i++) {
            symbol = this.dateFormat.symbols.weekdaysShort[i];
            re = new RegExp('^(' + scout.strings.quote(parseContext.inputString) + ')(.*)$', 'i');
            m = re.exec(symbol);
            if (m) { // match found
              parseContext.matchInfo.weekday = symbol;
              parseContext.hints.weekday = i;
              parseContext.inputString = '';
              return m[1];
            }
          }
        }
        return null; // no match found
      }
    }),
    // --- Hour (24h) ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.HOUR_24,
      terms: 'HH',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft(formatContext.inputDate.getHours(), 2);
      },
      parseRegExp: /^(\d{2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.hours = Number(match);
        parseContext.matchInfo.hours = match;
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.HOUR_24,
      terms: ['H', 'h'],
      formatFunction: function(formatContext) {
        return String(formatContext.inputDate.getHours());
      },
      parseRegExp: /^(\d{1,2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.hours = Number(match);
        parseContext.matchInfo.hours = match;
      }
    }),
    // --- Hour (12h) ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.HOUR_12,
      terms: 'KK',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft((formatContext.inputDate.getHours() + 11) % 12 + 1, 2);
      },
      parseRegExp: /^(\d{2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.hours = Number(match);
        parseContext.matchInfo.hours = match;
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.HOUR_12,
      terms: 'K',
      formatFunction: function(formatContext) {
        return String((formatContext.inputDate) % 12 + 1);
      },
      parseRegExp: /^(\d{1,2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.hours = Number(match);
        parseContext.matchInfo.hours = match;
      }
    }),
    // --- AM/PM marker ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.AM_PM,
      terms: 'a',
      formatFunction: function(formatContext) {
        if (formatContext.inputDate.getHours() < 12) {
          return this.dateFormat.symbols.am;
        }
        return this.dateFormat.symbols.pm;
      },
      parseFunction: function(parseContext) {
        var am = false,
          pm = false,
          re, m;

        re = new RegExp('^(' + scout.strings.quote(this.dateFormat.symbols.am) + ')(.*)$', 'i');
        m = re.exec(parseContext.inputString);
        if (m) { // match found
          am = true;
          parseContext.matchInfo.ampm = m[1];
          parseContext.inputString = m[2];
        } else {
          re = new RegExp('^(' + scout.strings.quote(this.dateFormat.symbols.pm) + ')(.*)$', 'i');
          m = re.exec(parseContext.inputString);
          if (m) { // match found
            pm = true;
            parseContext.matchInfo.ampm = m[1];
            parseContext.inputString = m[2];
          }
        }

        if (!am && !pm) {
          return null; // no match found
        }
        parseContext.hints.am = am;
        parseContext.hints.pm = pm;
        return m[1];
      }
    }),
    // --- Minute ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.MINUTE,
      terms: 'mm',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft(formatContext.inputDate.getMinutes(), 2);
      },
      parseRegExp: /^(\d{2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.minutes = Number(match);
        parseContext.matchInfo.minutes = match;
      },
      parseFunction: function(parseContext) {
        // Special case! When regexp did not match, check if input + '0' would make a
        // valid minutes value. If yes, predict this value.
        if (parseContext.analyze) {
          if (scout.isOneOf(parseContext.inputString, '0', '1', '2', '3', '4', '5')) {
            var tenMinutes = parseContext.inputString + '0';
            parseContext.dateInfo.minutes = Number(tenMinutes);
            parseContext.matchInfo.minutes = tenMinutes;
            parseContext.inputString = '';
            return parseContext.inputString;
          }
        }
        return null; // no match found
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.MINUTE,
      terms: 'm',
      formatFunction: function(formatContext) {
        return String(formatContext.inputDate.getMinutes());
      },
      parseRegExp: /^(\d{1,2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.minutes = Number(match);
        parseContext.matchInfo.minutes = match;
      }
    }),
    // --- Second ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.SECOND,
      terms: 'ss',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft(formatContext.inputDate.getSeconds(), 2);
      },
      parseRegExp: /^(\d{2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.seconds = Number(match);
        parseContext.matchInfo.seconds = match;
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.SECOND,
      terms: 's',
      formatFunction: function(formatContext) {
        return String(formatContext.inputDate.getSeconds());
      },
      parseRegExp: /^(\d{1,2})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.seconds = Number(match);
        parseContext.matchInfo.seconds = match;
      }
    }),
    // --- Millisecond ---
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.MILLISECOND,
      terms: 'SSS',
      formatFunction: function(formatContext) {
        return scout.strings.padZeroLeft(formatContext.inputDate.getMilliseconds(), 3);
      },
      parseRegExp: /^(\d{3})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.milliseconds = Number(match);
        parseContext.matchInfo.milliseconds = match;
      }
    }),
    new DateFormatPatternDefinition({
      type: scout.DateFormatPatternType.MILLISECOND,
      terms: 'S',
      formatFunction: function(formatContext) {
        return String(formatContext.inputDate.getMilliseconds());
      },
      parseRegExp: /^(\d{1,3})(.*)$/,
      applyMatchFunction: function(parseContext, match) {
        parseContext.dateInfo.milliseconds = Number(match);
        parseContext.matchInfo.milliseconds = match;
      }
    })
  ];

  // Build a map of pattern definitions by pattern type
  this._patternLibrary = {};
  for (var i = 0; i < this._patternDefinitions.length; i++) {
    var patternDefinition = this._patternDefinitions[i];
    var type = patternDefinition.type;
    if (type) {
      if (!this._patternLibrary[type]) {
        this._patternLibrary[type] = [];
      }
      this._patternLibrary[type].push(patternDefinition);
    }
  }

  this._compile();
};

scout.DateFormat.prototype._compile = function() {
  var i, j, patternDefinitions, patternDefinition, re, m, term, termAccepted, analyseFunctions;

  // Build format, parse and analyze functions for all terms in the DateFormat's pattern.
  // A term is a continuous sequence of the same character.
  re = /(.)\1*/g;
  while ((m = re.exec(this.pattern))) {
    term = m[0];
    this._terms.push(term);

    termAccepted = false;
    for (i = 0; i < this._patternDefinitions.length; i++) {
      patternDefinition = this._patternDefinitions[i];
      if (patternDefinition.accept(term)) {
        // 1. Create and install format function
        this._formatFunctions.push(patternDefinition.createFormatFunction());

        // 2. Create and install parse function
        this._parseFunctions.push(patternDefinition.createParseFunction());

        // 3. Create and install analyze functions (= all parse functions for the same type)
        patternDefinitions = this._patternLibrary[patternDefinition.type];
        analyseFunctions = [];
        for (j = 0; j < patternDefinitions.length; j++) {
          analyseFunctions.push(patternDefinitions[j].createParseFunction());
        }
        this._analyzeFunctions.push(analyseFunctions);

        // Term was processed, continue with next term
        termAccepted = true;
        break;
      }
    }

    // In case term was not accepted by any pattern definition, assume it is a constant string
    if (!termAccepted) {
      // 1. Create and install constant format function
      this._formatFunctions.push(this._createConstantStringFormatFunction(term));
      // 2./3. Create and install parse and analyse functions
      var constantStringParseFunction = this._createConstantStringParseFunction(term);
      this._parseFunctions.push(constantStringParseFunction);
      this._analyzeFunctions.push([constantStringParseFunction]);
    }
  }
};

/**
 * Returns a format function for constant terms (e.g. all parts of a pattern that don't
 * have a DateFormatPatternDefinition).
 */
scout.DateFormat.prototype._createConstantStringFormatFunction = function(term) {
  return function(formatContext) {
    formatContext.formattedString += term;
  };
};

/**
 * Returns a parse function for constant terms (e.g. all parts of a pattern that don't
 * have a DateFormatPatternDefinition).
 */
scout.DateFormat.prototype._createConstantStringParseFunction = function(term) {
  return function(parseContext) {
    if (scout.strings.startsWith(parseContext.inputString, term)) {
      parseContext.inputString = parseContext.inputString.substr(term.length);
      parseContext.parsedPattern += term;
      return true;
    }
    // In analyze mode, constant terms are optional (this supports "020318" --> "02.03.2018")
    if (parseContext.analyze) {
      return true;
    }
    return false;
  };
};

/**
 * Formats the given date according to the date pattern. If the date is missing, the
 * empty string is returned.
 *
 * @param exactLength
 *          May be set to true to force the patterns to use the exact length. For example,
 *          the year pattern 'yyy' would normally format the year using 2 digits. If
 *          the parameter is true, 3 are used. This is mainly useful, when an "analyzed"
 *          date should be formatted again using the "parsedPattern".
 */
scout.DateFormat.prototype.format = function(date, exactLength) {
  if (!date) {
    return '';
  }

  var formatContext = this._createFormatContext(date);
  formatContext.exactLength = scout.nvl(exactLength, false);
  // Apply all formatter functions for this DateFormat to the pattern to replace the
  // different terms with the corresponding value from the given date.
  for (var i = 0; i < this._formatFunctions.length; i++) {
    var formatFunction = this._formatFunctions[i];
    formatFunction(formatContext);
  }
  return formatContext.formattedString;
};

/**
 * Analyzes the given string and returns an information object with all recognized information
 * for the current date format.
 *
 * The result object contains the following properties:
 *
 * inputString:
 *   The original input for the analysis.
 *
 * dateInfo:
 *   An object with all numeric date parts that could be parsed from the input string. Unrecognized
 *   parts are undefined, all others are converted to numbers. Those values may be directly
 *   used in the JavaScript Date() type (month is zero-based!).
 *   Valid properties:
 *   - year, month, day, hours, minutes, seconds, milliseconds
 *
 * matchInfo:
 *   Similar to dateInfo, but the parts are defined as strings as they were parsed from the input.
 *   While dateInfo may contain the year 1995, the matchInfo may contain "95". Also note that
 *   the month is "one-based", as opposed to dateInfo.month!
 *   Valid properties:
 *   - year, month, week, day, weekday, hours, ampm, minutes, seconds, milliseconds
 *
 * hints:
 *   An object that contains further recognized date parts that are not needed to define the exact time.
 *   Valid properties:
 *   - am [true / false]
 *   - pm [true / false]
 *   - weekday [number 0-6; 0=sun, 1=mon, etc.]
 *   - weekInYear [number 1-53]
 *
 * parsedPattern:
 *   The pattern that was used to parse the input. This may differ from the date format's pattern.
 *   Example: dateFormat="dd.MM.YYYY", inputString="5.7.2015" --> parsedPattern="d.M.yyyy"
 *
 * predictedDate:
 *   The date that could be predicted from the recognized inputs. If the second method argument
 *   'startDate' is set, this date is used as basis for this predicted date. Otherwise, 'today' is used.
 *
 * error:
 *   Boolean that indicates if analyzing the input was successful (e.g. if the pattern could be parsed
 *   and a date could be predicted).
 */
scout.DateFormat.prototype.analyze = function(text, startDate) {
  var analyzeInfo = this._createAnalyzeInfo(text);
  if (!text) {
    return analyzeInfo;
  }

  var parseContext = this._createParseContext(text);
  parseContext.analyze = true; // Mark context as "analyze mode"
  parseContext.startDate = startDate;
  for (var i = 0; i < this._terms.length; i++) {
    if (parseContext.inputString.length > 0) {
      var parseFunctions = this._analyzeFunctions[i];
      var parsed = false;
      for (var j = 0; j < parseFunctions.length; j++) {
        var parseFunction = parseFunctions[j];
        if (parseFunction(parseContext)) {
          parsed = true;
          break;
        }
      }
      if (!parsed) {
        // Parsing failed
        analyzeInfo.error = true;
        return analyzeInfo;
      }
    } else {
      // Input is fully consumed, now just add the remaining terms from the pattern
      parseContext.parsedPattern += this._terms[i];
    }
  }

  if (parseContext.inputString.length > 0) {
    // There is still input, but the pattern has no more terms --> parsing failed
    analyzeInfo.error = true;
    return analyzeInfo;
  }

  // Try to generate a valid predicted date with the information retrieved so far
  startDate = this._prepareStartDate(startDate);
  if (parseContext.hints.weekday !== undefined) {
    startDate = scout.dates.shiftToNextDayOfType(startDate, parseContext.hints.weekday);
  }
  var predictedDate = this._dateInfoToDate(parseContext.dateInfo, startDate);

  // Update analyzeInfo
  analyzeInfo.dateInfo = parseContext.dateInfo;
  analyzeInfo.matchInfo = parseContext.matchInfo;
  analyzeInfo.hints = parseContext.hints;
  analyzeInfo.parsedPattern = parseContext.parsedPattern;
  analyzeInfo.predictedDate = predictedDate;
  analyzeInfo.error = (!predictedDate);
  return analyzeInfo;
};

/**
 * Parses the given text with the current date format. If the text does not match exactly
 * with the pattern, "null" is returned. Otherwise, the parsed date is returned.
 *
 * The argument 'startDate' is optional. It may set the date where parsed information should
 * be applied to (e.g. relevant for 2-digit years).
 */
scout.DateFormat.prototype.parse = function(text, startDate) {
  if (!text) {
    return null;
  }

  var parseContext = this._createParseContext(text);
  parseContext.startDate = startDate;
  for (var i = 0; i < this._parseFunctions.length; i++) {
    var parseFunction = this._parseFunctions[i];
    if (!parseFunction(parseContext)) {
      return null; // Parsing failed
    }
    if (parseContext.inputString.length === 0) {
      break; // Everything parsed!
    }
  }
  if (parseContext.inputString.length > 0) {
    // Input remaining but no more parse functions available -> parsing failed
    return null;
  }

  // Build date from dateInfo
  var date = this._dateInfoToDate(parseContext.dateInfo, startDate);
  if (!date) {
    return null; // dateInfo could not be converted to a valid date -> parsing failed
  }

  // Handle hints
  if ((parseContext.hints.am || parseContext.hints.pm)) {
    var hours = date.getHours();
    if (hours === 12) {
      hours = 0;
    }
    if (parseContext.hints.pm) {
      hours += 12;
    }
    date.setHours(hours);
  }
  if (parseContext.hints.weekday !== undefined) {
    if (date.getDay() !== parseContext.hints.weekday) {
      return null; // Date and weekday don't match -> parsing failed
    }
  }
  // TODO [5.2] bsh: Handle week of year

  // Return valid date
  return date;
};

scout.DateFormat.prototype._dateInfoToDate = function(dateInfo, startDate) {
  if (!dateInfo) {
    return null;
  }

  // Default date
  startDate = this._prepareStartDate(startDate);

  // Apply date info (Start with "zero date", otherwise the date may become invalid
  // due to JavaScript's automatic date correction, e.g. dateInfo = { day: 11, month: 1 }
  // and startDate = 2015-07-29 would result in invalid date 2015-03-11, because February
  // 2015 does not have 29 days and is "corrected" to March.)
  var result = new Date(1970, 0, 1);

  var validMonth = scout.nvl(dateInfo.month, startDate.getMonth());
  var validYear = scout.nvl(dateInfo.year, startDate.getFullYear());
  // When user entered the day but not (yet) the month, adjust month if possible to propose a valid date
  if (dateInfo.day && !dateInfo.month) {
    // If day "31" does not exist in the proposed month, use the next month
    if (dateInfo.day === 31) {
      var monthsWithThirthyOneDays = [0, 2, 4, 6, 7, 9, 11];
      if (!scout.isOneOf(validMonth, monthsWithThirthyOneDays)) {
        validMonth = validMonth + 1;
      }
    }
    // If day is "29" or "30" and month is february, use next month (except day is "29" and the year is a leap year)
    else if (dateInfo.day >= 29 && validMonth === 1) {
      if (dateInfo.day > 29 || !scout.dates.isLeapYear(validYear)) {
        validMonth = validMonth + 1;
      }
    }
  }
  result.setFullYear(
      validYear,
      validMonth,
      scout.nvl(dateInfo.day, startDate.getDate())
  );

  result.setHours(
      scout.nvl(dateInfo.hours, startDate.getHours()),
      scout.nvl(dateInfo.minutes, startDate.getMinutes()),
      scout.nvl(dateInfo.seconds, startDate.getSeconds()),
      scout.nvl(dateInfo.milliseconds, startDate.getMilliseconds())
  );

  // Validate. A date is considered valid if the value from the dateInfo did
  // not change (JS date automatically converts illegal values, e.g. day 32 is
  // converted to first day of next month).
  if (!isValid(result.getFullYear(), dateInfo.year)) {
    return null;
  }
  if (!isValid(result.getMonth(), dateInfo.month)) {
    return null;
  }
  if (!isValid(result.getDate(), dateInfo.day)) {
    return null;
  }
  if (!isValid(result.getHours(), dateInfo.hours)) {
    return null;
  }
  if (!isValid(result.getMinutes(), dateInfo.minutes)) {
    return null;
  }
  if (!isValid(result.getSeconds(), dateInfo.seconds)) {
    return null;
  }
  if (!isValid(result.getMilliseconds(), dateInfo.milliseconds)) {
    return null;
  }

  return result;

  // ----- Helper functions -----

  function isValid(value, expectedValue) {
    return (expectedValue === undefined || expectedValue === value);
  }
};

/**
 * Returns the date where parsed information should be applied to. The given
 * startDate is used when specified, otherwise a new date is created (today).
 */
scout.DateFormat.prototype._prepareStartDate = function(startDate) {
  if (startDate) {
    // It is important that we don't alter the argument 'startDate', but create an independent copy!
    return new Date(startDate.getTime());
  }
  return scout.dates.trunc(new Date()); // clear time
};

/**
 * Returns the "format context", an object that is initially filled with the input date and is then
 * passed through the various formatting functions. As the formatting progresses, the format context object
 * is updated accordingly. At the end of the process, the object contains the result.
 *
 * The parse context contains the following properties:
 *
 * inputString:
 *   The original input for the parsing. This string will be consumed during the parse process,
 *   and will be empty at the end.
 *
 * dateInfo:
 *   An object with all numeric date parts that could be parsed from the input string. Unrecognized
 *   parts are undefined, all others are converted to numbers.
 *   Valid properties:
 *   - year, month, day, hours, minutes, seconds, milliseconds
 *
 * matchInfo:
 *   Similar to dateInfo, but the parts are defined as strings as they were parsed from the input.
 *   While dateInfo may contain the year 1995, the matchInfo may contain "95".
 *   Valid properties:
 *   - year, month, week, day, weekday, hours, ampm, minutes, seconds, milliseconds
 *
 * hints:
 *   An object that contains further recognized date parts that are not needed to define the exact time.
 *   Valid properties:
 *   - am [true / false]
 *   - pm [true / false]
 *   - weekday [number 0-6; 0=sun, 1=mon, etc.]
 *   - weekInYear [number 1-53]
 *
 * analyze:
 *   A flag that indicates if the "analyze mode" is on. This is true when analyze() was called, and
 *   false when parse() was called. It may alter the behavior of the parse functions, i.e. they will
 *   not fail in analyze mode when the pattern does not match exactly.
 *
 * startDate:
 *   A date to be used as reference for date calculations. Is used for example when mapping a 2-digit
 *   year to a 4-digit year.
 */

scout.DateFormat.prototype._createFormatContext = function(inputDate) {
  return {
    inputDate: inputDate,
    formattedString: '',
    exactLength: false
  };
};

/**
 * Returns the "parse context", an object that is initially filled with the input string and is then
 * passed through the various parsing functions. As the parsing progresses, the parse context object
 * is updated accordingly. At the end of the process, the object contains the result.
 *
 * The parse context contains the following properties:
 *
 * inputString:
 *   The original input for the parsing. This string will be consumed during the parse process,
 *   and will be empty at the end.
 *
 * dateInfo:
 *   An object with all numeric date parts that could be parsed from the input string. Unrecognized
 *   parts are undefined, all others are converted to numbers. Those values may be directly
 *   used in the JavaScript Date() type (month is zero-based!).
 *   Valid properties:
 *   - year, month, day, hours, minutes, seconds, milliseconds
 *
 * matchInfo:
 *   Similar to dateInfo, but the parts are defined as strings as they were parsed from the input.
 *   While dateInfo may contain the year 1995, the matchInfo may contain "95". Also note that
 *   the month is "one-based", as opposed to dateInfo.month!
 *   Valid properties:
 *   - year, month, week, day, weekday, hours, ampm, minutes, seconds, milliseconds
 *
 * hints:
 *   An object that contains further recognized date parts that are not needed to define the exact time.
 *   Valid properties:
 *   - am [true / false]
 *   - pm [true / false]
 *   - weekday [number 0-6; 0=sun, 1=mon, etc.]
 *   - weekInYear [number 1-53]
 *
 * analyze:
 *   A flag that indicates if the "analyze mode" is on. This is true when analyze() was called, and
 *   false when parse() was called. It may alter the behavior of the parse functions, i.e. they will
 *   not fail in analyze mode when the pattern does not match exactly.
 *
 * startDate:
 *   A date to be used as reference for date calculations. Is used for example when mapping a 2-digit
 *   year to a 4-digit year.
 */
scout.DateFormat.prototype._createParseContext = function(inputText) {
  return {
    inputString: inputText,
    dateInfo: {},
    matchInfo: {},
    hints: {},
    parsedPattern: '',
    analyze: false,
    startDate: null
  };
};

/**
 * @see analyze()
 */
scout.DateFormat.prototype._createAnalyzeInfo = function(inputText) {
  return {
    inputString: inputText,
    dateInfo: {},
    matchInfo: {},
    hints: {},
    parsedPattern: '',
    predictedDate: null,
    error: false
  };
};
