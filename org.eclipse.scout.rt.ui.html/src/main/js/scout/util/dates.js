scout.dates = {

  /**
   * @memberOf scout.dates
   */
  shift: function(date, years, months, days) {
    var newDate = new Date(date.getTime());
    if (years) {
      newDate.setFullYear(date.getFullYear() + years);
      if (scout.dates.compareMonths(newDate, date) !== years * 12) {
        // Set to last day of the previous month
        // The reason: 2016-02-29 + 1 year -> 2017-03-01 instead of 2017-02-28
        newDate.setDate(0);
      }
    }
    if (months) {
      newDate.setMonth(date.getMonth() + months);
      if (scout.dates.compareMonths(newDate, date) !== months + years * 12) {
        // Set to last day of the previous month
        // The reason: 2010-10-31 + 1 month -> 2010-12-01 instead of 2010-11-30
        newDate.setDate(0);
      }
    }
    if (days) {
      newDate.setDate(date.getDate() + days);
    }
    return newDate;
  },

  shiftToNextDayOfType: function(date, day) {
    var diff = day - date.getDay();

    if (diff <= 0) {
      diff += 7;
    }
    return scout.dates.shift(date, 0, 0, diff);
  },

  shiftToPreviousDayOfType: function(date, day) {
    var diff = day - date.getDay();

    if (diff >= 0) {
      diff -= 7;
    }
    return scout.dates.shift(date, 0, 0, diff);
  },

  shiftToNextOrPrevDayOfType: function(date, day, direction) {
    if (direction > 0) {
      return scout.dates.shiftToNextDayOfType(date, day);
    } else {
      return scout.dates.shiftToPreviousDayOfType(date, day);
    }
  },

  shiftToNextOrPrevMonday: function(date, direction) {
    return scout.dates.shiftToNextOrPrevDayOfType(date, 1, direction);
  },

  isSameDay: function(date, date2) {
    return date.getFullYear() === date2.getFullYear() &&
      date.getMonth() === date2.getMonth() &&
      date.getDate() === date2.getDate();
  },

  /**
   * @return the difference of the two dates in number of months.
   */
  compareMonths: function(date1, date2) {
    var d1Month = date1.getMonth(),
      d2Month = date2.getMonth(),
      d1Year = date1.getFullYear(),
      d2Year = date2.getFullYear(),
      monthDiff = d1Month - d2Month;
    if (d1Year === d2Year) {
      return monthDiff;
    }
    return (d1Year - d2Year) * 12 + monthDiff;
  },

  /**
   * Returns the time (with milliseconds) for the given date as a string in the format
   * [year#4][month#2][day#2][hour#2][minute#2][second#2][#millisecond#3]. All characters
   * are guaranteed to be digits. If the date argument is omitted, the current date is
   * used. Note: The date is returned in UTC.
   */
  timestamp: function(date) {
    var d = date || new Date();
    return '' + d.getUTCFullYear() +
      scout.strings.padZeroLeft((d.getUTCMonth() + 1), 2) + // (0-indexed)
    scout.strings.padZeroLeft(d.getUTCDate(), 2) +
      scout.strings.padZeroLeft(d.getUTCHours(), 2) +
      scout.strings.padZeroLeft(d.getUTCMinutes(), 2) +
      scout.strings.padZeroLeft(d.getUTCSeconds(), 2) +
      scout.strings.padZeroLeft(d.getUTCMilliseconds(), 3);
  },

  orderWeekdays: function(weekdays, firstDayOfWeek) {
    var weekdaysOrdered = [];
    for (var i = 0; i < 7; i++) {
      weekdaysOrdered[i] = weekdays[(i + firstDayOfWeek) % 7];
    }
    return weekdaysOrdered;
  },

  /**
   * Returns the week number according to ISO 8601 definition:
   * - All years have 52 or 53 weeks.
   * - The first week is the week with January 4th in it.
   * - The first day of a week is Monday, the last day is Sunday
   *
   * This is the default behavior. By setting the optional second argument 'option',
   * the first day in a week can be changed (e.g. 0 = Sunday). The returned numbers weeks are
   * not ISO 8601 compliant anymore, but can be more appropriate for display in a calendar. The
   * argument can be a number, a 'scout.Locale' or a 'scout.DateFormat' object.
   */
  weekInYear: function(date, option) {
    if (!date) {
      return undefined;
    }
    var firstDayOfWeek = 1;
    if (typeof option === 'object') {
      // scout.DateFormat
      if (option.symbols !== undefined && option.symbols.firstDayOfWeek !== undefined) {
        firstDayOfWeek = option.symbols.firstDayOfWeek;
      }
      // scout.Locale
      else if (option.decimalFormatSymbols !== undefined && option.decimalFormatSymbols.firstDayOfWeek !== undefined) {
        firstDayOfWeek = option.decimalFormatSymbols.firstDayOfWeek;
      }
    } else if (typeof option === 'number') {
      firstDayOfWeek = option;
    }

    // Thursday of current week decides the year
    var thursday = this._thursdayOfWeek(date, firstDayOfWeek);

    // In ISO format, the week with January 4th is the first week
    var jan4 = new Date(thursday.getFullYear(), 0, 4);

    // If the date is before the beginning of the year, it belongs to the year before
    var startJan4 = this._firstDayOfWeek(jan4, firstDayOfWeek);
    if (date.getTime() < startJan4.getTime()) {
      jan4 = new Date(thursday.getFullYear() - 1, 0, 4);
    }

    // Get the Thursday of the first week, to be able to compare it to 'thursday'
    var thursdayFirstWeek = this._thursdayOfWeek(jan4, firstDayOfWeek);

    var diffInDays = (thursday.getTime() - thursdayFirstWeek.getTime()) / 86400000;

    return 1 + Math.round(diffInDays / 7);
  },

  _thursdayOfWeek: function(date, firstDayOfWeek) {
    if (!date || typeof firstDayOfWeek !== 'number') {
      return undefined;
    }

    var thursday = new Date(date.valueOf());
    if (thursday.getDay() !== 4) { // 0 = Sun, 1 = Mon, 2 = Thu, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat
      if (thursday.getDay() < firstDayOfWeek) {
        // go 1 week backward
        thursday.setDate(thursday.getDate() - 7);
      }
      thursday.setDate(thursday.getDate() - thursday.getDay() + 4); // go to start of week, then add 4 to go to Thursday
    }
    return thursday;
  },

  _firstDayOfWeek: function(date, firstDayOfWeek) {
    if (!date || typeof firstDayOfWeek !== 'number') {
      return undefined;
    }
    var firstDay = new Date(date.valueOf());
    if (firstDay.getDay() !== firstDayOfWeek) {
      firstDay.setDate(firstDay.getDate() - ((firstDay.getDay() + 7 - firstDayOfWeek) % 7));
    }
    return firstDay;
  },

  /**
   * Parses a string that corresponds to one of the canonical JSON transfer formats
   * and returns it as a JavaScript 'Date' object.
   *
   * @see JsonDate.java
   */
  parseJsonDate: function(jsonDate) {
    if (!jsonDate) {
      return undefined;
    }

    var year = '1970',
      month = '01',
      day = '01',
      hours = '00',
      minutes = '00',
      seconds = '00',
      milliseconds = '000',
      utc = false;

    // Date + Time
    var matches = /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})\.(\d{3})(Z?)$/.exec(jsonDate);
    if (matches !== null) {
      year = matches[1];
      month = matches[2];
      day = matches[3];
      hours = matches[4];
      minutes = matches[5];
      seconds = matches[6];
      milliseconds = matches[7];
      utc = (matches[8] === 'Z');
    } else {
      // Date only
      matches = /^(\d{4})-(\d{2})-(\d{2})(Z?)$/.exec(jsonDate);
      if (matches !== null) {
        year = matches[1];
        month = matches[2];
        day = matches[3];
        utc = (matches[4] === 'Z');
      } else {
        // Time only
        matches = /^(\d{2}):(\d{2}):(\d{2})\.(\d{3})(Z?)$/.exec(jsonDate);
        if (matches !== null) {
          hours = matches[1];
          minutes = matches[2];
          seconds = matches[3];
          milliseconds = matches[4];
          utc = (matches[5] === 'Z');
        } else {
          throw new Error('Unparsable date: ' + jsonDate);
        }
      }
    }

    if (utc) {
      // UTC date
      return new Date(Date.UTC(year, (month - 1), day, hours, minutes, seconds, milliseconds));
    } else {
      // local date
      return new Date(year, (month - 1), day, hours, minutes, seconds, milliseconds);
    }
  },

  /**
   * Converts the given date object to a JSON string. By default, the local time zone
   * is used to built the result, time zone information itself is not part of the
   * result. If the argument 'utc' is set to true, the result is built using the
   * UTC values of the date. Such a result string is marked with a trailing 'Z' character.
   *
   * @see JsonDate.java
   */
  toJsonDate: function(date, utc, includeDate, includeTime) {
    if (!date) {
      return undefined;
    }
    if (includeDate === undefined) {
      includeDate = true;
    }
    if (includeTime === undefined) {
      includeTime = true;
    }
    var datePart, timePart, utcPart;
    if (utc) {
      datePart = date.getUTCFullYear() + '-' +
        scout.strings.padZeroLeft((date.getUTCMonth() + 1), 2) + '-' + // (0-indexed)
      scout.strings.padZeroLeft(date.getUTCDate(), 2);
      timePart = scout.strings.padZeroLeft(date.getUTCHours(), 2) + ':' +
        scout.strings.padZeroLeft(date.getUTCMinutes(), 2) + ':' +
        scout.strings.padZeroLeft(date.getUTCSeconds(), 2) + '.' +
        scout.strings.padZeroLeft(date.getUTCMilliseconds(), 3);
      utcPart = 'Z';
    } else {
      datePart = date.getFullYear() + '-' +
        scout.strings.padZeroLeft((date.getMonth() + 1), 2) + '-' + // (0-indexed)
      scout.strings.padZeroLeft(date.getDate(), 2);
      timePart = scout.strings.padZeroLeft(date.getHours(), 2) + ':' +
        scout.strings.padZeroLeft(date.getMinutes(), 2) + ':' +
        scout.strings.padZeroLeft(date.getSeconds(), 2) + '.' +
        scout.strings.padZeroLeft(date.getMilliseconds(), 3);
      utcPart = '';
    }
    var result = '';
    if (includeDate) {
      result += datePart;
      if (includeTime) {
        result += ' ';
      }
    }
    if (includeTime) {
      result += timePart;
    }
    result += utcPart;
    return result;
  },

  toJsonDateRange: function(range) {
    return {
      from: scout.dates.toJsonDate(range.from),
      to: scout.dates.toJsonDate(range.to)
    };
  },

  /**
   * Creates a new JavaScript Date object by parsing the given string. The format is as follows:
   *
   * [Year#4]-[Month#2]-[Day#2] [Hours#2]:[Minutes#2]:[Seconds#2].[Milliseconds#3][Z]
   *
   * The year component is mandatory, but all others are optional (starting from the beginning).
   * The date is constructed using the local time zone. If the last character is 'Z', then
   * the values are interpreted as UTC date.
   */
  create: function(dateString) {
    if (dateString) {
      var matches = /^(\d{4})(?:-(\d{2})(?:-(\d{2})(?: (\d{2})(?::(\d{2})(?::(\d{2})(?:\.(\d{3}))?(Z?))?)?)?)?)?/.exec(dateString);
      if (matches === null) {
        throw new Error('Unparsable date: ' + dateString);
      }
      var date;
      if (matches[8] === 'Z') {
        date = new Date(Date.UTC(
          matches[1], // fullYear
          ((matches[2] || 1) - 1), // month (0-indexed)
          (matches[3] || 1), // day of month
          (matches[4] || 0), // hours
          (matches[5] || 0), // minutes
          (matches[6] || 0), // seconds
          (matches[7] || 0) // milliseconds
        ));
      } else {
        date = new Date(
          matches[1], // fullYear
          ((matches[2] || 1) - 1), // month (0-indexed)
          (matches[3] || 1), // day of month
          (matches[4] || 0), // hours
          (matches[5] || 0), // minutes
          (matches[6] || 0), // seconds
          (matches[7] || 0) // milliseconds
        );
      }
      return date;
    }
    return undefined;
  },

  format: function(date, locale, pattern) {
    var dateClone = new Date(date.valueOf()),
      dateFormat = new scout.DateFormat(locale, pattern);
    return dateFormat.format(dateClone);
  },

  compare: function(a, b) {
    var diff = a.valueOf() - b.valueOf();
    if (diff < -1) {
      return -1;
    }
    if (diff > 1) {
      return 1;
    }
    return diff;
  },

  /**
   * TODO NBU Add jasmine test
   * This combines a date and time, passed as date objects to one object with the date part of param date and the time part of param time.
   */
  combineDateTime: function(date, time) {
    var newDate = new Date(0);
    newDate.setHours(0,0,0,0);
    if (date) {
      newDate.setFullYear(date.getFullYear(), date.getMonth(), date.getDay());
    }
    if (time) {
      newDate.setHours(time.getHours() ? time.getHours() : 0);
      newDate.setMinutes(time.getMinutes() ? time.getMinutes() : 0);
      newDate.setSeconds(time.getSeconds() ? time.getSeconds() : 0);
      newDate.setMilliseconds(time.getMilliseconds() ? time.getMilliseconds() : 0);
    }
    return newDate;
  }

};
