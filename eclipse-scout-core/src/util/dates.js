/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DateFormat, scout, strings} from '../index';

export function shift(date, years, months, days) {
  let newDate = new Date(date.getTime());
  if (years) {
    newDate.setFullYear(date.getFullYear() + years);
    if (compareMonths(newDate, date) !== years * 12) {
      // Set to last day of the previous month
      // The reason: 2016-02-29 + 1 year -> 2017-03-01 instead of 2017-02-28
      newDate.setDate(0);
    }
  }
  if (months) {
    newDate.setMonth(date.getMonth() + months);
    if (compareMonths(newDate, date) !== months + years * 12) {
      // Set to last day of the previous month
      // The reason: 2010-10-31 + 1 month -> 2010-12-01 instead of 2010-11-30
      newDate.setDate(0);
    }
  }
  if (days) {
    newDate.setDate(date.getDate() + days);
  }
  return newDate;
}

export function shiftTime(date, hours, minutes, seconds, milliseconds) {
  let newDate = new Date(date.getTime());
  if (hours) {
    newDate.setHours(date.getHours() + hours);
  }
  if (minutes) {
    newDate.setMinutes(date.getMinutes() + minutes);
  }
  if (seconds) {
    newDate.setSeconds(date.getSeconds() + seconds);
  }
  if (milliseconds) {
    newDate.setMilliseconds(date.getMilliseconds() + milliseconds);
  }
  return newDate;
}

export function shiftToNextDayOfType(date, day) {
  let diff = day - date.getDay();

  if (diff <= 0) {
    diff += 7;
  }
  return shift(date, 0, 0, diff);
}

/**
 * Finds the next date (based on the given date) that matches the given day in week and date.
 *
 * @param {Date} date Start date
 * @param {number} dayInWeek 0-6
 * @param {number} dayInMonth 1-31
 * @returns {Date}
 */
export function shiftToNextDayAndDate(date, dayInWeek, dayInMonth) {
  let tmpDate = new Date(date.getTime());
  if (dayInMonth < tmpDate.getDate()) {
    tmpDate.setMonth(tmpDate.getMonth() + 1);
  }
  tmpDate.setDate(dayInMonth);
  while (tmpDate.getDay() !== dayInWeek || tmpDate.getDate() !== dayInMonth) {
    tmpDate = shift(tmpDate, 0, 1, 0);
    tmpDate.setDate(dayInMonth);
  }
  return tmpDate;
}

export function shiftToPreviousDayOfType(date, day) {
  let diff = day - date.getDay();

  if (diff >= 0) {
    diff -= 7;
  }
  return shift(date, 0, 0, diff);
}

export function shiftToNextOrPrevDayOfType(date, day, direction) {
  if (direction > 0) {
    return shiftToNextDayOfType(date, day);
  }
  return shiftToPreviousDayOfType(date, day);
}

export function shiftToNextOrPrevMonday(date, direction) {
  return shiftToNextOrPrevDayOfType(date, 1, direction);
}

/**
 * Ensures that the given date is really a date.
 * <p>
 * If it already is a date, the date will be returned.
 * Otherwise parseJsonDate is used to create a Date.
 *
 * @param {Date|string} date may be of type date or string.
 */
export function ensure(date) {
  if (!date) {
    return date;
  }
  if (date instanceof Date) {
    return date;
  }
  return parseJsonDate(date);
}

export function ensureMonday(date, direction) {
  if (date.getDay() === 1) {
    return date;
  }
  return shiftToNextOrPrevMonday(date, direction);
}

export function isSameTime(date, date2) {
  if (!date || !date2) {
    return false;
  }
  return date.getHours() === date2.getHours() &&
    date.getMinutes() === date2.getMinutes() &&
    date.getSeconds() === date2.getSeconds();
}

export function isSameDay(date, date2) {
  if (!date || !date2) {
    return false;
  }
  return date.getFullYear() === date2.getFullYear() &&
    date.getMonth() === date2.getMonth() &&
    date.getDate() === date2.getDate();
}

export function isSameMonth(date, date2) {
  if (!date || !date2) {
    return false;
  }
  return compareMonths(date, date2) === 0;
}

/**
 * Returns the difference of the two dates in number of months.
 * @returns {number}
 */
export function compareMonths(date1, date2) {
  let d1Month = date1.getMonth(),
    d2Month = date2.getMonth(),
    d1Year = date1.getFullYear(),
    d2Year = date2.getFullYear(),
    monthDiff = d1Month - d2Month;
  if (d1Year === d2Year) {
    return monthDiff;
  }
  return (d1Year - d2Year) * 12 + monthDiff;
}

/**
 * Returns the difference of the two dates in number of days.
 * @returns {number}
 */
export function compareDays(date1, date2) {
  return (trunc(date1) - trunc(date2) - (date1.getTimezoneOffset() - date2.getTimezoneOffset()) * 60000) / (3600000 * 24);
}

/**
 * Returns the time (with milliseconds) for the given date as a string in the format
 * [year#4][month#2][day#2][hour#2][minute#2][second#2][#millisecond#3]. All characters
 * are guaranteed to be digits. If the date argument is omitted, the current date is
 * used. The returned string in in UTC if the argument 'utc' is true, otherwise the
 * result is in local time (default).
 *
 * @deprecated this function will be deleted in release 23.1. Use DateFormat.js instead
 */
export function timestamp(date, utc) {
  // (note: month is 0-indexed)
  let d = date || new Date();
  if (utc) {
    return strings.padZeroLeft(d.getUTCFullYear(), 4) +
      strings.padZeroLeft(d.getUTCMonth() + 1, 2) +
      strings.padZeroLeft(d.getUTCDate(), 2) +
      strings.padZeroLeft(d.getUTCHours(), 2) +
      strings.padZeroLeft(d.getUTCMinutes(), 2) +
      strings.padZeroLeft(d.getUTCSeconds(), 2) +
      strings.padZeroLeft(d.getUTCMilliseconds(), 3);
  }
  return strings.padZeroLeft(d.getFullYear(), 4) +
    strings.padZeroLeft(d.getMonth() + 1, 2) +
    strings.padZeroLeft(d.getDate(), 2) +
    strings.padZeroLeft(d.getHours(), 2) +
    strings.padZeroLeft(d.getMinutes(), 2) +
    strings.padZeroLeft(d.getSeconds(), 2) +
    strings.padZeroLeft(d.getMilliseconds(), 3);
}

export function orderWeekdays(weekdays, firstDayOfWeekArg) {
  let weekdaysOrdered = [];
  for (let i = 0; i < 7; i++) {
    weekdaysOrdered[i] = weekdays[(i + firstDayOfWeekArg) % 7];
  }
  return weekdaysOrdered;
}

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
export function weekInYear(date, option) {
  if (!date) {
    return undefined;
  }
  let firstDayOfWeekArg = 1;
  if (typeof option === 'object') {
    // DateFormat
    if (option.symbols !== undefined && option.symbols.firstDayOfWeek !== undefined) {
      firstDayOfWeekArg = option.symbols.firstDayOfWeek;
    } else if (option.decimalFormatSymbols !== undefined && option.decimalFormatSymbols.firstDayOfWeek !== undefined) {
      // Locale
      firstDayOfWeekArg = option.decimalFormatSymbols.firstDayOfWeek;
    }
  } else if (typeof option === 'number') {
    firstDayOfWeekArg = option;
  }

  // Thursday of current week decides the year
  let thursday = _thursdayOfWeek(date, firstDayOfWeekArg);

  // In ISO format, the week with January 4th is the first week
  let jan4 = new Date(thursday.getFullYear(), 0, 4);

  // If the date is before the beginning of the year, it belongs to the year before
  let startJan4 = firstDayOfWeek(jan4, firstDayOfWeekArg);
  if (date.getTime() < startJan4.getTime()) {
    jan4 = new Date(thursday.getFullYear() - 1, 0, 4);
  }

  // Get the Thursday of the first week, to be able to compare it to 'thursday'
  let thursdayFirstWeek = _thursdayOfWeek(jan4, firstDayOfWeekArg);

  let diffInDays = (thursday.getTime() - thursdayFirstWeek.getTime()) / 86400000;

  return 1 + Math.round(diffInDays / 7);
}

export function _thursdayOfWeek(date, firstDayOfWeekArg) {
  if (!date || typeof firstDayOfWeekArg !== 'number') {
    return undefined;
  }

  let thursday = new Date(date.valueOf());
  if (thursday.getDay() !== 4) { // 0 = Sun, 1 = Mon, 2 = Thu, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat
    if (thursday.getDay() < firstDayOfWeekArg) {
      // go 1 week backward
      thursday.setDate(thursday.getDate() - 7);
    }
    thursday.setDate(thursday.getDate() - thursday.getDay() + 4); // go to start of week, then add 4 to go to Thursday
  }
  return thursday;
}

export function firstDayOfWeek(date, firstDayOfWeekArg) {
  if (!date || typeof firstDayOfWeekArg !== 'number') {
    return undefined;
  }
  let firstDay = new Date(date.valueOf());
  if (firstDay.getDay() !== firstDayOfWeekArg) {
    firstDay.setDate(firstDay.getDate() - (firstDay.getDay() + 7 - firstDayOfWeekArg) % 7);
  }
  return firstDay;
}

/**
 * Parses a string that corresponds to one of the canonical JSON transfer formats
 * and returns it as a JavaScript 'Date' object.
 *
 * @see JsonDate.java
 */
export function parseJsonDate(jsonDate) {
  if (!jsonDate) {
    return null;
  }

  let year = '1970',
    month = '01',
    day = '01',
    hours = '00',
    minutes = '00',
    seconds = '00',
    milliseconds = '000',
    utc = false;

  // Date + Time
  let matches = /^\+?(\d{4,5})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})\.(\d{3})(Z?)$/.exec(jsonDate);
  if (matches !== null) {
    year = matches[1];
    month = matches[2];
    day = matches[3];
    hours = matches[4];
    minutes = matches[5];
    seconds = matches[6];
    milliseconds = matches[7];
    utc = matches[8] === 'Z';
  } else {
    // Date only
    matches = /^\+?(\d{4,5})-(\d{2})-(\d{2})(Z?)$/.exec(jsonDate);
    if (matches !== null) {
      year = matches[1];
      month = matches[2];
      day = matches[3];
      utc = matches[4] === 'Z';
    } else {
      // Time only
      matches = /^(\d{2}):(\d{2}):(\d{2})\.(\d{3})(Z?)$/.exec(jsonDate);
      if (matches !== null) {
        hours = matches[1];
        minutes = matches[2];
        seconds = matches[3];
        milliseconds = matches[4];
        utc = matches[5] === 'Z';
      } else {
        throw new Error('Unparsable date: ' + jsonDate);
      }
    }
  }

  let result;
  if (utc) {
    // UTC date
    result = new Date(Date.UTC(year, month - 1, day, hours, minutes, seconds, milliseconds));
    if (year < 100) { // fix "two-digit years between 1900 and 1999" logic
      result.setUTCFullYear(year);
    }
  } else {
    // local date
    result = new Date(year, month - 1, day, hours, minutes, seconds, milliseconds);
    if (year < 100) { // fix "two-digit years between 1900 and 1999" logic
      result.setFullYear(year);
    }
  }
  return result;
}

/**
 * Converts the given date object to a JSON string. By default, the local time zone
 * is used to built the result, time zone information itself is not part of the
 * result. If the argument 'utc' is set to true, the result is built using the
 * UTC values of the date. Such a result string is marked with a trailing 'Z' character.
 *
 * @see JsonDate.java
 */
export function toJsonDate(date, utc, includeDate, includeTime) {
  if (!date) {
    return null;
  }
  if (includeDate === undefined) {
    includeDate = true;
  }
  if (includeTime === undefined) {
    includeTime = true;
  }
  let datePart, timePart, utcPart;
  if (utc) {
    // (note: month is 0-indexed)
    datePart = getYearPart(date) + '-' +
      strings.padZeroLeft(date.getUTCMonth() + 1, 2) + '-' +
      strings.padZeroLeft(date.getUTCDate(), 2);
    timePart = strings.padZeroLeft(date.getUTCHours(), 2) + ':' +
      strings.padZeroLeft(date.getUTCMinutes(), 2) + ':' +
      strings.padZeroLeft(date.getUTCSeconds(), 2) + '.' +
      strings.padZeroLeft(date.getUTCMilliseconds(), 3);
    utcPart = 'Z';
  } else {
    // (note: month is 0-indexed)
    datePart = getYearPart(date) + '-' +
      strings.padZeroLeft(date.getMonth() + 1, 2) + '-' +
      strings.padZeroLeft(date.getDate(), 2);
    timePart = strings.padZeroLeft(date.getHours(), 2) + ':' +
      strings.padZeroLeft(date.getMinutes(), 2) + ':' +
      strings.padZeroLeft(date.getSeconds(), 2) + '.' +
      strings.padZeroLeft(date.getMilliseconds(), 3);
    utcPart = '';
  }
  let result = '';
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

  function getYearPart(date) {
    let year = date.getFullYear();
    if (year > 9999) {
      return '+' + year;
    }
    return strings.padZeroLeft(year, 4);
  }
}

export function toJsonDateRange(range) {
  return {
    from: toJsonDate(range.from),
    to: toJsonDate(range.to)
  };
}

/**
 * Creates a new JavaScript Date object by parsing the given string. This method is not intended to be
 * used in application code, but provides a quick way to create dates in unit tests.
 *
 * The format is as follows:
 *
 * [Year#4|5]-[Month#2]-[Day#2] [Hours#2]:[Minutes#2]:[Seconds#2].[Milliseconds#3][Z]
 *
 * The year component is mandatory, but all others are optional (starting from the beginning).
 * The date is constructed using the local time zone. If the last character is 'Z', then
 * the values are interpreted as UTC date.
 */
export function create(dateString) {
  if (dateString) {
    let matches = /^(\d{4,5})(?:-(\d{2})(?:-(\d{2})(?: (\d{2})(?::(\d{2})(?::(\d{2})(?:\.(\d{3}))?(Z?))?)?)?)?)?/.exec(dateString);
    if (matches === null) {
      throw new Error('Unparsable date: ' + dateString);
    }
    let date;
    if (matches[8] === 'Z') {
      date = new Date(Date.UTC(
        matches[1], // fullYear
        (matches[2] || 1) - 1, // month (0-indexed)
        matches[3] || 1, // day of month
        matches[4] || 0, // hours
        matches[5] || 0, // minutes
        matches[6] || 0, // seconds
        matches[7] || 0 // milliseconds
      ));
    } else {
      date = new Date(
        matches[1], // fullYear
        (matches[2] || 1) - 1, // month (0-indexed)
        matches[3] || 1, // day of month
        matches[4] || 0, // hours
        matches[5] || 0, // minutes
        matches[6] || 0, // seconds
        matches[7] || 0 // milliseconds
      );
    }
    return date;
  }
  return undefined;
}

/**
 * Returns a new Date. Use this function in place of <code>new Date();</code> in your productive code
 * when you want to provide a fixed date instead of the system time/date for unit tests. In your unit test
 * you can replace this function with a function that provides a fixed date. Don't forget to restore the
 * original function when you cleanup/tear-down the test.
 *
 * @returns {Date}
 */
export function newDate() {
  return new Date();
}

export function format(date, locale, pattern) {
  let dateFormat = new DateFormat(locale, pattern);
  return dateFormat.format(date);
}

/**
 * Uses the default date and time format patterns from the locale to format the given date.
 */
export function formatDateTime(date, locale) {
  let dateFormat = new DateFormat(locale, locale.dateFormatPatternDefault + ' ' + locale.timeFormatPatternDefault);
  return dateFormat.format(date);
}

export function compare(a, b) {
  if (!a && !b) {
    return 0;
  }
  if (!a) {
    return -1;
  }
  if (!b) {
    return 1;
  }
  let diff = a.getTime() - b.getTime();
  if (diff < -1) {
    return -1;
  }
  if (diff > 1) {
    return 1;
  }
  return diff;
}

export function equals(a, b) {
  return compare(a, b) === 0;
}

/**
 * This combines a date and time, passed as date objects to one object with the date part of param date and the time part of param time.
 * <p>
 * If time is omitted, 00:00:00 is used as time part.<br>
 * If date is omitted, 1970-01-01 is used as date part independent of the time zone, means it is 1970-01-01 in every time zone.
 */
export function combineDateTime(date, time) {
  let newDate = new Date();
  newDate.setHours(0, 0, 0, 0); // set time part to zero in local time!
  newDate.setFullYear(1970, 0, 1); // make sure local time has no effect on date (if date is omitted it has to be 1970-01-01)
  if (date) {
    newDate.setFullYear(date.getFullYear(), date.getMonth(), date.getDate());
  }
  if (time) {
    newDate.setHours(scout.nvl(time.getHours(), 0));
    newDate.setMinutes(scout.nvl(time.getMinutes(), 0));
    newDate.setSeconds(scout.nvl(time.getSeconds(), 0));
    newDate.setMilliseconds(scout.nvl(time.getMilliseconds(), 0));
  }
  return newDate;
}

/**
 * Returns <code>true</code> if the given year is a leap year, i.e if february 29 exists in that year.
 */
export function isLeapYear(year) {
  if (year === undefined || year === null) {
    return false;
  }
  let date = new Date(year, 1, 29);
  return date.getDate() === 29;
}

/**
 * Returns the given date with time set to midnight (hours, minutes, seconds, milliseconds = 0).
 *
 * @param {Date} date (required)
 *          The date to truncate.
 * @param {boolean} [createCopy] (optional)
 *          If this flag is true, a copy of the given date is returned (the input date is not
 *          altered). If the flag is false, the given object is changed and then returned.
 *          The default value for this flag is "true".
 */
export function trunc(date, createCopy) {
  if (date) {
    if (scout.nvl(createCopy, true)) {
      date = new Date(date.getTime());
    }
    date.setHours(0, 0, 0, 0); // clear time
  }
  return date;
}

/**
 * Returns the given date with time set to midnight (hours, minutes, seconds, milliseconds = 0).
 *
 * @param {Date} date
 *          The date to truncate.
 * @param {number} [minutesResolution] default is 30
 *          The amount of minutes added to every full hour XX:00 until > XX+1:00. The given date will ceiled to the next valid time.
 *          e.g. time:15:05, resolution 40  -> 15:40
 *               time: 15:41 resolution 40 -> 16:00
 * @param {boolean} [createCopy]
 *          If this flag is true, a copy of the given date is returned (the input date is not
 *          altered). If the flag is false, the given object is changed and then returned.
 *          The default value for this flag is "true".
 */
export function ceil(date, minutesResolution, createCopy) {
  let h,
    m,
    mResulution = scout.nvl(minutesResolution, 30);
  if (date) {
    if (scout.nvl(createCopy, true)) {
      date = new Date(date.getTime());
    }

    date.setSeconds(0, 0); // clear seconds and millis

    m = parseInt((date.getMinutes() + mResulution) / mResulution) * mResulution;
    h = date.getHours();
    if (m >= 60) {
      h++;
      m = 0;
    }
    if (h > 23) {
      h = 0;
      date.setDate(date.getDate() + 1);
    }
    date.setHours(h, m);
  }
  return date;
}

export default {
  ceil,
  combineDateTime,
  compare,
  compareDays,
  compareMonths,
  create,
  ensure,
  ensureMonday,
  equals,
  firstDayOfWeek,
  format,
  formatDateTime,
  isLeapYear,
  isSameDay,
  isSameMonth,
  isSameTime,
  newDate,
  orderWeekdays,
  parseJsonDate,
  shift,
  shiftTime,
  shiftToNextDayOfType,
  shiftToNextDayAndDate,
  shiftToNextOrPrevDayOfType,
  shiftToNextOrPrevMonday,
  shiftToPreviousDayOfType,
  timestamp,
  toJsonDate,
  toJsonDateRange,
  trunc,
  weekInYear
};
