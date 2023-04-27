/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DateAdapter, TimeUnit} from 'chart.js';
import {DateFormat, dates, Locale, objects, Session, strings} from '@eclipse-scout/core';

export const chartJsDateAdapter = {
  getAdapter(session: Session): Partial<DateAdapter> {
    return {
      formats: (): { [key: string]: string } => _getFormats(session.locale),

      parse: (value: unknown, format?: TimeUnit): number | null => {
        if (objects.isNullOrUndefined(value)) {
          return null;
        }
        if (value instanceof Date) {
          return value.getTime();
        }
        if (objects.isString(value)) {
          if (objects.isString(format) && strings.hasText(format)) {
            let dateFormat = new DateFormat(session.locale, format);
            value = dateFormat.parse(value);
            return value instanceof Date ? value.getTime() : null;
          }
          value = dates.parseJsonDate(value);
          return value instanceof Date ? value.getTime() : null;
        }
        return null;
      },

      format: (timestamp: number, format: TimeUnit): string => {
        if (format === 'quarter') {
          // Quarters are not supported by DateFormat, but Chart.js anyway never uses this time unit for formatting by default, because it is not very common.
          // See the function "determineUnitForFormatting" in scale.time.js for more details. Therefore, we refrain from supporting this here.
          throw new Error('The time unit "quarter" is not supported for formatting.');
        }
        return dates.format(new Date(timestamp), session.locale, format);
      },

      add: (timestamp: number, amount: number, unit: TimeUnit): number => {
        let date = new Date(timestamp);
        switch (unit) {
          case 'millisecond':
            return dates.shiftTime(date, 0, 0, 0, amount).getTime();
          case 'second':
            return dates.shiftTime(date, 0, 0, amount, 0).getTime();
          case 'minute':
            return dates.shiftTime(date, 0, amount, 0, 0).getTime();
          case 'hour':
            return dates.shiftTime(date, amount, 0, 0, 0).getTime();
          case 'day':
            return dates.shift(date, 0, 0, amount).getTime();
          case 'week':
            return dates.shift(date, 0, 0, amount * 7).getTime();
          case 'month':
            return dates.shift(date, 0, amount, 0).getTime();
          case 'quarter':
            return dates.shift(date, 0, amount * 3, 0).getTime();
          case 'year':
            return dates.shift(date, amount, 0, 0).getTime();
          default:
            return timestamp;
        }
      },

      diff: (a: number, b: number, unit: TimeUnit): number => {
        switch (unit) {
          case 'millisecond':
            return a - b;
          case 'second':
            return (a - b) / 1000;
          case 'minute':
            return (a - b) / 1000 / 60;
          case 'hour':
            return (a - b) / 1000 / 60 / 60;
          case 'day':
            return dates.compareDays(new Date(a), new Date(b));
          case 'week':
            return dates.compareDays(new Date(a), new Date(b)) / 7;
          case 'month':
            return dates.compareMonths(new Date(a), new Date(b));
          case 'quarter':
            return dates.compareMonths(new Date(a), new Date(b)) / 3;
          case 'year':
            return new Date(a).getFullYear() - new Date(b).getFullYear();
          default:
            return 0;
        }
      },

      startOf: (timestamp: number, unit: TimeUnit | 'isoWeek', weekday?: number): number => {
        let date = new Date(timestamp);
        switch (unit) {
          case 'second':
            return date.setMilliseconds(0);
          case 'minute':
            return date.setSeconds(0, 0);
          case 'hour':
            return date.setMinutes(0, 0, 0);
          case 'day':
            return date.setHours(0, 0, 0, 0);
          case 'week':
            return dates.firstDayOfWeek(date, 0).getTime();
          case 'isoWeek':
            return dates.firstDayOfWeek(date, weekday).getTime();
          case 'month':
            return dates.trunc(date).setDate(0);
          case 'quarter':
            return dates.trunc(date).setMonth(date.getMonth() % 4, 1);
          case 'year':
            return dates.trunc(date).setMonth(0, 1);
          default:
            return timestamp;
        }
      },

      endOf: (timestamp: number, unit: TimeUnit | 'isoWeek'): number => {
        let date = new Date(timestamp);
        switch (unit) {
          case 'second':
            return date.setMilliseconds(999);
          case 'minute':
            return date.setSeconds(59, 999);
          case 'hour':
            return date.setMinutes(59, 59, 999);
          case 'day':
            return date.setHours(23, 59, 59, 999);
          case 'week': {
            date.setHours(23, 59, 59, 999);
            let firstDayOfWeek = dates.firstDayOfWeek(date, 0);
            return firstDayOfWeek.setDate(firstDayOfWeek.getDate() + 7);
          }
          case 'month': {
            date.setHours(23, 59, 59, 999);
            return date.setDate(_getNumberOfDays(date.getMonth(), date.getFullYear()));
          }
          case 'quarter': {
            date.setHours(23, 59, 59, 999);
            let endOfQuarterMonth = date.getMonth() % 4 + 2;
            return date.setMonth(endOfQuarterMonth, _getNumberOfDays(endOfQuarterMonth, date.getFullYear()));
          }
          case 'year': {
            date.setHours(23, 59, 59, 999);
            return date.setMonth(11, 31);
          }
          default:
            return timestamp;
        }
      }
    };
  }
};

function _getFormats(locale: Locale) {
  return {
    datetime: locale.dateFormatPatternDefault + ' ' + locale.timeFormatPatternDefault,
    millisecond: 'HH:mm:ss.SSS',
    second: 'HH:mm:ss',
    minute: locale.timeFormatPatternDefault,
    hour: locale.timeFormatPatternDefault,
    day: locale.dateFormatPatternDefault,
    week: 'ww',
    month: 'MMM yyyy',
    quarter: 'qqq - yyyy',
    year: 'yyyy'
  };
}

/**
 * @param month 0 = january, 11 = december
 * @returns the number of days in the given month and year.
 */
function _getNumberOfDays(month: number, year: number): number {
  if (month in [3, 5, 8, 10]) { // april, june, september, november
    return 30;
  } else if (month === 1) { // february
    return dates.isLeapYear(year) ? 29 : 28;
  }
  // january, march, may, july, august, october, december
  return 31;
}
