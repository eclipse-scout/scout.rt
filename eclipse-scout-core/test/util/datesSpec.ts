/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {dates} from '../../src/index';
import {LocaleSpecHelper} from '../../src/testing/index';

describe('scout.dates', () => {

  describe('shift', () => {

    // Note: Test dates need explicit time zone setting, otherwise the result of toISOString (which always
    // returns UTC dates) would depend on the browser's time zone. For convenience, we use UTC as well ("+00:00").

    it('shifts year or month or day', () => {
      let date = dates.create('2014-11-21');
      expect(dates.shift(date, 1).toISOString()).toBe(dates.create('2015-11-21 00:00:00.000').toISOString());

      date = dates.create('2014-11-21');
      expect(dates.shift(date, 0, 1).toISOString()).toBe(dates.create('2014-12-21 00:00:00.000').toISOString());

      date = dates.create('2014-11-21');
      expect(dates.shift(date, 0, 0, 1).toISOString()).toBe(dates.create('2014-11-22 00:00:00.000').toISOString());
    });

    it('shifts year and month if both provided', () => {
      let date = dates.create('2014-01-01');
      expect(dates.shift(date, 1, 1).toISOString()).toBe(dates.create('2015-02-01 00:00:00.000').toISOString());
    });

    it('shifts year and month and day if all provided', () => {
      let date = dates.create('2014-01-01');
      expect(dates.shift(date, 1, 1, 1).toISOString()).toBe(dates.create('2015-02-02 00:00:00.000').toISOString());
    });

    describe('shift year', () => {

      it('adds or removes years', () => {
        let date = dates.create('2014-11-21');
        expect(dates.shift(date, 1).toISOString()).toBe(dates.create('2015-11-21 00:00:00.000').toISOString());

        date = dates.create('2014-11-01');
        expect(dates.shift(date, -1).toISOString()).toBe(dates.create('2013-11-01 00:00:00.000').toISOString());
      });

      it('handles edge case leap year', () => {
        let date = dates.create('2016-02-29T00:00:00.000');
        expect(dates.shift(date, 1).toISOString()).toBe(dates.create('2017-02-28 00:00:00.000').toISOString());
      });
    });

    describe('shift month', () => {

      it('adds or removes months', () => {
        let date = dates.create('2014-11-21');
        expect(dates.shift(date, 0, 1).toISOString()).toBe(dates.create('2014-12-21 00:00:00.000').toISOString());

        date = dates.create('2014-12-21');
        expect(dates.shift(date, 0, -1).toISOString()).toBe(dates.create('2014-11-21 00:00:00.000').toISOString());

        // Check if it also works when we cross the "daylight saving time" border (saving->normal)
        date = dates.create('2014-11-21');
        expect(dates.shift(date, 0, -1).toISOString()).toBe(dates.create('2014-10-21 00:00:00.000').toISOString());
        date = dates.create('2014-10-21');
        expect(dates.shift(date, 0, 1).toISOString()).toBe(dates.create('2014-11-21 00:00:00.000').toISOString());

        // Check if it also works when we cross the "daylight saving time" border (normal->saving)
        date = dates.create('2014-04-10');
        expect(dates.shift(date, 0, -1).toISOString()).toBe(dates.create('2014-03-10 00:00:00.000').toISOString());
        date = dates.create('2014-03-10');
        expect(dates.shift(date, 0, 1).toISOString()).toBe(dates.create('2014-04-10 00:00:00.000').toISOString());

        date = dates.create('2014-11-21');
        expect(dates.shift(date, 0, 12).toISOString()).toBe(dates.create('2015-11-21 00:00:00.000').toISOString());

        date = dates.create('2014-11-21');
        expect(dates.shift(date, 0, -12).toISOString()).toBe(dates.create('2013-11-21 00:00:00.000').toISOString());
      });

      it('handles edge case start month', () => {
        let date = dates.create('2014-11-01');
        expect(dates.shift(date, 0, 1).toISOString()).toBe(dates.create('2014-12-01 00:00:00.000').toISOString());

        date = dates.create('2014-11-01');
        expect(dates.shift(date, 0, -1).toISOString()).toBe(dates.create('2014-10-01 00:00:00.000').toISOString());

        date = dates.create('2014-11-01');
        expect(dates.shift(date, 0, 12).toISOString()).toBe(dates.create('2015-11-01 00:00:00.000').toISOString());

        date = dates.create('2014-11-01');
        expect(dates.shift(date, 0, -12).toISOString()).toBe(dates.create('2013-11-01 00:00:00.000').toISOString());

        date = dates.create('2014-11-01');
        expect(dates.shift(date, 0, 13).toISOString()).toBe(dates.create('2015-12-01 00:00:00.000').toISOString());

        date = dates.create('2014-11-01');
        expect(dates.shift(date, 0, -13).toISOString()).toBe(dates.create('2013-10-01 00:00:00.000').toISOString());
      });

      it('handles edge case end month', () => {
        let date = dates.create('2014-10-31');
        expect(dates.shift(date, 0, 1).toISOString()).toBe(dates.create('2014-11-30 00:00:00.000').toISOString());

        date = dates.create('2014-10-31');
        expect(dates.shift(date, 0, -1).toISOString()).toBe(dates.create('2014-09-30 00:00:00.000').toISOString());

        date = dates.create('2014-10-31');
        expect(dates.shift(date, 0, 12).toISOString()).toBe(dates.create('2015-10-31 00:00:00.000').toISOString());

        date = dates.create('2014-10-31');
        expect(dates.shift(date, 0, 13).toISOString()).toBe(dates.create('2015-11-30 00:00:00.000').toISOString());

        date = dates.create('2014-10-31');
        expect(dates.shift(date, 0, -13).toISOString()).toBe(dates.create('2013-09-30 00:00:00.000').toISOString());

        date = dates.create('2014-10-31');
        expect(dates.shift(date, 0, -25).toISOString()).toBe(dates.create('2012-09-30 00:00:00.000').toISOString());
      });

      it('handles edge case leap year', () => {
        let date = dates.create('2016-02-29');
        expect(dates.shift(date, 0, 12).toISOString()).toBe(dates.create('2017-02-28 00:00:00.000').toISOString());
      });
    });
  });

  describe('shiftToNextDayOfType', () => {

    it('shifts to next day of type', () => {
      let date = dates.create('2015-07-09');
      expect(dates.shiftToNextDayOfType(date, 1).toISOString()).toBe(dates.create('2015-07-13 00:00:00.000').toISOString());

      date = dates.create('2015-07-09');
      expect(dates.shiftToNextDayOfType(date, 6).toISOString()).toBe(dates.create('2015-07-11 00:00:00.000').toISOString());
    });
  });

  describe('shiftToNextDayAndDate', () => {

    it('shifts to next date with requested week-day and date', () => {
      let date = dates.create('2020-03-03'); // Tue
      expect(dates.shiftToNextDayAndDate(date, 1, 22).toISOString()).toBe(dates.create('2020-06-22 00:00:00.000').toISOString()); // 1=Mo

      expect(dates.shiftToNextDayAndDate(date, 2, 24).toISOString()).toBe(dates.create('2020-03-24 00:00:00.000').toISOString()); // 2=Tue
    });
  });

  describe('shiftToPreviousDayOfType', () => {

    it('shifts to previous day of type', () => {
      let date = dates.create('2015-07-09');
      expect(dates.shiftToPreviousDayOfType(date, 1).toISOString()).toBe(dates.create('2015-07-06 00:00:00.000').toISOString());

      date = dates.create('2015-07-09');
      expect(dates.shiftToPreviousDayOfType(date, 6).toISOString()).toBe(dates.create('2015-07-04 00:00:00.000').toISOString());
    });
  });

  describe('ensureMonday', () => {

    it('shifts to next monday in direction if it is not a monday yet', () => {
      let date = dates.create('2016-02-09');
      expect(dates.ensureMonday(date, 1).toISOString()).toBe(dates.create('2016-02-15 00:00:00.000').toISOString());

      date = dates.create('2016-02-21');
      expect(dates.ensureMonday(date, -1).toISOString()).toBe(dates.create('2016-02-15 00:00:00.000').toISOString());

      date = dates.create('2016-02-15');
      expect(dates.ensureMonday(date, 1).toISOString()).toBe(dates.create('2016-02-15 00:00:00.000').toISOString());

      date = dates.create('2016-02-15');
      expect(dates.ensureMonday(date, -1).toISOString()).toBe(dates.create('2016-02-15 00:00:00.000').toISOString());
    });
  });

  describe('isSameDay', () => {
    it('returns true if day, month and year matches', () => {
      let date = dates.create('2014-11-21');
      let date2 = dates.create('2014-11-21 11:13');
      expect(dates.isSameDay(date, date2)).toBe(true);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-11-21');
      expect(dates.isSameDay(date, date2)).toBe(true);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-11-20');
      expect(dates.isSameDay(date, date2)).toBe(false);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-10-21');
      expect(dates.isSameDay(date, date2)).toBe(false);

      date = dates.create('2014-11-21');
      date2 = dates.create('2013-11-21');
      expect(dates.isSameDay(date, date2)).toBe(false);

      date = new Date('2014-11-21');
      date2 = new Date('2014-11-20T22:00:00.000-02:00');
      expect(dates.isSameDay(date, date2)).toBe(true);
    });
  });

  describe('compareMonths', () => {
    it('returns the differences in number of months', () => {
      let date = dates.create('2014-11-21');
      let date2 = dates.create('2014-11-21');
      expect(dates.compareMonths(date, date2)).toBe(0);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-12-21');
      expect(dates.compareMonths(date, date2)).toBe(-1);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-10-21');
      expect(dates.compareMonths(date, date2)).toBe(1);
    });

    it('ignores time', () => {
      let date = dates.create('2014-11-21T23:00');
      let date2 = dates.create('2014-11-21');
      expect(dates.compareMonths(date, date2)).toBe(0);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-12-21T15:15');
      expect(dates.compareMonths(date, date2)).toBe(-1);

      date = dates.create('2014-11-21T20:20');
      date2 = dates.create('2014-10-21T15:10');
      expect(dates.compareMonths(date, date2)).toBe(1);
    });

    it('works with different years', () => {
      let date = dates.create('2014-11-21');
      let date2 = dates.create('2013-11-21');
      expect(dates.compareMonths(date, date2)).toBe(12);

      date = dates.create('2014-11-21');
      date2 = dates.create('2015-11-21');
      expect(dates.compareMonths(date, date2)).toBe(-12);

      date = dates.create('2014-11-21');
      date2 = dates.create('2013-10-21');
      expect(dates.compareMonths(date, date2)).toBe(13);

      date = dates.create('2014-11-21');
      date2 = dates.create('2015-12-21');
      expect(dates.compareMonths(date, date2)).toBe(-13);
    });
  });

  describe('compareDays', () => {
    it('returns the differences in number of days', () => {
      let date = dates.create('2014-11-21');
      let date2 = dates.create('2014-11-21');
      expect(dates.compareDays(date, date2)).toBe(0);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-11-25');
      expect(dates.compareDays(date, date2)).toBe(-4);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-11-15');
      expect(dates.compareDays(date, date2)).toBe(6);
    });

    it('ignores time', () => {
      let date = dates.create('2014-11-21T23:00');
      let date2 = dates.create('2014-11-21');
      expect(dates.compareDays(date, date2)).toBe(0);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-11-22T15:15');
      expect(dates.compareDays(date, date2)).toBe(-1);

      date = dates.create('2014-11-21T20:20');
      date2 = dates.create('2014-11-20T15:10');
      expect(dates.compareDays(date, date2)).toBe(1);
    });

    it('works with different month', () => {
      let date = dates.create('2014-11-21');
      let date2 = dates.create('2014-10-21');
      expect(dates.compareDays(date, date2)).toBe(31);

      date = dates.create('2014-11-21');
      date2 = dates.create('2014-12-21');
      expect(dates.compareDays(date, date2)).toBe(-30);
    });

    it('works with different years', () => {
      let date = dates.create('2014-11-21');
      let date2 = dates.create('2013-11-21');
      expect(dates.compareDays(date, date2)).toBe(365);

      date = dates.create('2014-11-21');
      date2 = dates.create('2015-11-21');
      expect(dates.compareDays(date, date2)).toBe(-365);

      date = dates.create('2014-12-31');
      date2 = dates.create('2015-01-01');
      expect(dates.compareDays(date, date2)).toBe(-1);

      date = dates.create('2015-01-01');
      date2 = dates.create('2014-12-31');
      expect(dates.compareDays(date, date2)).toBe(1);
    });
  });

  describe('orderWeekdays', () => {

    it('orders weekdays', () => {
      let weekdays = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'];
      let check0 = dates.orderWeekdays(weekdays, 0);
      let check1 = dates.orderWeekdays(weekdays, 1);
      let check2 = dates.orderWeekdays(weekdays, 2);
      let check3 = dates.orderWeekdays(weekdays, 3);
      expect(check0.join('-')).toBe('So-Mo-Di-Mi-Do-Fr-Sa');
      expect(check1.join('-')).toBe('Mo-Di-Mi-Do-Fr-Sa-So');
      expect(check2.join('-')).toBe('Di-Mi-Do-Fr-Sa-So-Mo');
      expect(check3.join('-')).toBe('Mi-Do-Fr-Sa-So-Mo-Di');
    });
  });

  describe('toJsonDate / parseJsonDate', () => {

    it('can handle missing or invalid inputs', () => {
      // @ts-expect-error
      expect(dates.toJsonDate()).toBe(null);
      // @ts-expect-error
      expect(dates.parseJsonDate()).toBe(null);
      expect(() => {
        dates.parseJsonDate('invalid date string');
      }).toThrow();
    });

    it('can convert JSON and JS dates', () => {
      let date, jsonDate;

      // Test 1 - UTC
      date = dates.parseJsonDate('2014-11-21 00:00:00.000Z');
      expect(date).not.toBe(undefined);
      expect(date).not.toBe(null);
      expect(date.toISOString()).toBe('2014-11-21T00:00:00.000Z');
      jsonDate = dates.toJsonDate(date, true);
      expect(jsonDate).toBe('2014-11-21 00:00:00.000Z');
      // Date only
      date = dates.parseJsonDate('2014-11-21Z');
      expect(date.toISOString()).toBe('2014-11-21T00:00:00.000Z');
      // Time only
      date = dates.parseJsonDate('15:23:00.123Z');
      expect(date.toISOString()).toBe('1970-01-01T15:23:00.123Z');
      expect(() => {
        dates.parseJsonDate('15:23:00');
      }).toThrow(); // missing millis

      // Test 2 - local time zone
      date = dates.parseJsonDate('2014-11-21 00:00:00.000');
      expect(date).not.toBe(undefined);
      expect(date).not.toBe(null);
      // We cannot check for the exact value of date, because we don't know the executing browser's time zone.
      // But we can convert it back to JSON, which should result in the original string (because TZ are the same).
      jsonDate = dates.toJsonDate(date);
      expect(jsonDate).toBe('2014-11-21 00:00:00.000');
      jsonDate = dates.toJsonDate(date, false); // should be the same as above
      expect(jsonDate).toBe('2014-11-21 00:00:00.000');

      // Test 3 - special cases (UTC)
      date = dates.parseJsonDate('0025-11-21 00:00:00.000Z');
      expect(date).not.toBe(undefined);
      expect(date).not.toBe(null);
      expect(date.toISOString()).toBe('0025-11-21T00:00:00.000Z');
      jsonDate = dates.toJsonDate(date, true);
      expect(jsonDate).toBe('0025-11-21 00:00:00.000Z');
      // Date only
      date = dates.parseJsonDate('0025-11-21Z');
      expect(date.toISOString()).toBe('0025-11-21T00:00:00.000Z');

      // Test 4 - year > 9999, see: https://en.wikipedia.org/wiki/ISO_8601#Years
      date = dates.parseJsonDate('+20222-11-21 00:00:00.000Z');
      expect(date.getFullYear()).toBe(20222);
      jsonDate = dates.toJsonDate(date, true);
      expect(jsonDate).toBe('+20222-11-21 00:00:00.000Z');
    });
  });

  describe('create', () => {

    it('can create dates', () => {
      // @ts-expect-error
      expect(dates.create()).toBe(undefined);
      expect(dates.create('')).toBe(undefined);
      expect(() => {
        dates.create('invalid date string');
      }).toThrow();

      expect(dates.create('2014').toISOString()).toBe(dates.create('2014-01-01 00:00:00.000').toISOString());
      expect(dates.create('2014-10').toISOString()).toBe(dates.create('2014-10-01 00:00:00.000').toISOString());
      expect(dates.create('2014-10-31').toISOString()).toBe(dates.create('2014-10-31 00:00:00.000').toISOString());
      expect(dates.create('2014-10-31 23').toISOString()).toBe(dates.create('2014-10-31 23:00:00.000').toISOString());
      expect(dates.create('2014-10-31 23:59').toISOString()).toBe(dates.create('2014-10-31 23:59:00.000').toISOString());
      expect(dates.create('2014-10-31 23:59:58').toISOString()).toBe(dates.create('2014-10-31 23:59:58.000').toISOString());
      expect(dates.create('2014-10-31 23:59:58.882').toISOString()).toBe(dates.create('2014-10-31 23:59:58.882').toISOString());
      expect(dates.create('2014-10-31 23:59:58.882Z').toISOString()).toBe(dates.create('2014-10-31 23:59:58.882Z').toISOString());
    });

    it('works with 5-digits years', () => {
      let date = dates.create('20144-10-31 23:59:58.882Z');
      expect(date.getFullYear()).toBe(20144);
    });
  });

  describe('weekInYear', () => {

    it('can calculate week in year', () => {
      // @ts-expect-error
      expect(dates.weekInYear()).toBe(undefined);
      expect(dates.weekInYear(undefined)).toBe(undefined);
      expect(dates.weekInYear(null)).toBe(undefined);

      // Check week with firstDayOfWeek = monday (1)
      expect(dates.weekInYear(dates.create('2014-12-27'), 1)).toBe(52);
      expect(dates.weekInYear(dates.create('2014-12-28'), 1)).toBe(52);
      expect(dates.weekInYear(dates.create('2014-12-29'), 1)).toBe(1);
      expect(dates.weekInYear(dates.create('2014-12-30'), 1)).toBe(1);
      expect(dates.weekInYear(dates.create('2014-12-31'), 1)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-01'), 1)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-02'), 1)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-03'), 1)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-04'), 1)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-05'), 1)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-06'), 1)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-07'), 1)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-08'), 1)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-09'), 1)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-10'), 1)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-11'), 1)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-12'), 1)).toBe(3);
      expect(dates.weekInYear(dates.create('2015-01-26'), 1)).toBe(5);

      // Check week with firstDayOfWeek = sunday (0)
      expect(dates.weekInYear(dates.create('2014-12-27'), 0)).toBe(52);
      expect(dates.weekInYear(dates.create('2014-12-28'), 0)).toBe(53);
      expect(dates.weekInYear(dates.create('2014-12-29'), 0)).toBe(53);
      expect(dates.weekInYear(dates.create('2014-12-30'), 0)).toBe(53);
      expect(dates.weekInYear(dates.create('2014-12-31'), 0)).toBe(53);
      expect(dates.weekInYear(dates.create('2015-01-01'), 0)).toBe(53);
      expect(dates.weekInYear(dates.create('2015-01-02'), 0)).toBe(53);
      expect(dates.weekInYear(dates.create('2015-01-03'), 0)).toBe(53);
      expect(dates.weekInYear(dates.create('2015-01-04'), 0)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-05'), 0)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-06'), 0)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-07'), 0)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-08'), 0)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-09'), 0)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-10'), 0)).toBe(1);
      expect(dates.weekInYear(dates.create('2015-01-11'), 0)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-12'), 0)).toBe(2);
      expect(dates.weekInYear(dates.create('2015-01-26'), 0)).toBe(4);
    });
  });

  describe('format', () => {

    it('can handle invalid values', () => {
      expect(() => {
        // @ts-expect-error
        dates.format();
      }).toThrow();
      expect(() => {
        // @ts-expect-error
        dates.format(new Date());
      }).toThrow();
      expect(() => {
        // @ts-expect-error
        dates.format('gugus');
      }).toThrow();

      let helper = new LocaleSpecHelper();
      let locale = helper.createLocale(LocaleSpecHelper.DEFAULT_LOCALE);

      expect(dates.format(null, locale)).toBe('');
      expect(dates.format(dates.create('2014-11-21'), locale)).toBe('21.11.2014');
    });

    it('can format valid dates', () => {
      let helper = new LocaleSpecHelper();
      let locale = helper.createLocale(LocaleSpecHelper.DEFAULT_LOCALE);

      expect(dates.format(dates.create('2014-11-21'), locale, 'yy')).toBe('14');
    });
  });

  describe('formatTimestamp', () => {
    let origNewDateFunc: typeof dates.newDate;

    beforeAll(() => {
      dates.newDate = () => dates.create('2024-02-29 15:26:56.789');
    });

    afterAll(() => {
      dates.newDate = origNewDateFunc;
    });

    it('can format timestamps', () => {
      expect(dates.formatTimestamp()).toBe('2024-02-29 15:26:56.789');
      expect(dates.formatTimestamp({withDate: false})).toBe('15:26:56.789');
      expect(dates.formatTimestamp({withDate: false, includeMillis: false})).toBe('15:26:56');
      expect(dates.formatTimestamp({withTime: false, includeMillis: true})).toBe('2024-02-29');
      expect(dates.formatTimestamp({withDate: false, withTime: false, includeMillis: true})).toBe('');

      let testDate = dates.create('2020-10-25 02:29:17.007');
      expect(dates.formatTimestamp({date: testDate})).toBe('2020-10-25 02:29:17.007');
      expect(dates.formatTimestamp({date: testDate, withDate: false})).toBe('02:29:17.007');
      expect(dates.formatTimestamp({date: testDate, withDate: false, includeMillis: false})).toBe('02:29:17');
      expect(dates.formatTimestamp({date: testDate, withTime: false, includeMillis: true})).toBe('2020-10-25');
      expect(dates.formatTimestamp({date: testDate, withDate: false, withTime: false, includeMillis: true})).toBe('');
    });
  });

  describe('compare', () => {

    it('can handle invalid dates', () => {
      // @ts-expect-error
      expect(dates.compare()).toBe(0);

      let date = null;
      let date2 = null;
      expect(dates.compare(date, date2)).toBe(0);

      date = dates.create('2014-11-21');
      expect(dates.compare(date, date2)).toBe(1);

      date = null;
      date2 = dates.create('2014-11-21');
      expect(dates.compare(date, date2)).toBe(-1);

      expect(() => {
        // @ts-expect-error
        dates.compare('invalid value', date2);
      }).toThrow();

      date = null;
      date2 = dates.create('2014-11-21');
      expect(dates.compare(date, date2)).toBe(-1);
    });

    it('can compare valid dates', () => {
      let date = dates.create('2014-11-21');
      let date2 = dates.create('2014-11-21');
      expect(dates.compare(date, date2)).toBe(0);

      date = dates.create('2013-11-21');
      date2 = dates.create('2014-11-21');
      expect(dates.compare(date, date2)).toBe(-1);

      date = dates.create('2015-11-21');
      date2 = dates.create('2014-11-21');
      expect(dates.compare(date, date2)).toBe(1);

      date = dates.create('2016-01-20');
      date2 = dates.create('2016-02-10');
      expect(dates.compare(date, date2)).toBe(-1);

      date = dates.create('2016-02-29');
      date2 = dates.create('2016-03-01');
      expect(dates.compare(date, date2)).toBe(-1);

      date = dates.create('2015-02-29');
      date2 = dates.create('2015-03-01');
      expect(dates.compare(date, date2)).toBe(0);
    });
  });

  describe('equals', () => {

    it('returns true if the dates are equal, false if not', () => {
      let date1 = dates.create('2015-25-11'),
        date2 = dates.create('2015-25-11'),
        date3 = dates.create('2014-25-11');

      expect(dates.equals(null, null)).toBe(true);
      expect(dates.equals(date1, null)).toBe(false);
      expect(dates.equals(null, date1)).toBe(false);
      expect(dates.equals(date1, date2)).toBe(true);
      expect(dates.equals(date1, date3)).toBe(false);
    });
  });

  describe('isLeapYear', () => {

    it('correctly identifies leap years', () => {
      // @ts-expect-error
      expect(dates.isLeapYear()).toBe(false);
      expect(dates.isLeapYear(undefined)).toBe(false);
      expect(dates.isLeapYear(null)).toBe(false);

      expect(dates.isLeapYear(1900)).toBe(false);
      expect(dates.isLeapYear(1996)).toBe(true);
      expect(dates.isLeapYear(1997)).toBe(false);
      expect(dates.isLeapYear(1998)).toBe(false);
      expect(dates.isLeapYear(1999)).toBe(false);
      expect(dates.isLeapYear(2000)).toBe(true);
      expect(dates.isLeapYear(2001)).toBe(false);
      expect(dates.isLeapYear(2002)).toBe(false);
      expect(dates.isLeapYear(2003)).toBe(false);
      expect(dates.isLeapYear(2004)).toBe(true);
      expect(dates.isLeapYear(2005)).toBe(false);
      expect(dates.isLeapYear(2006)).toBe(false);
      expect(dates.isLeapYear(2007)).toBe(false);
      expect(dates.isLeapYear(2008)).toBe(true);
      expect(dates.isLeapYear(2100)).toBe(false);
    });
  });

  describe('combineDateTime', () => {

    it('creates a new date by using date part of param date and time part of param time.', () => {
      let date = dates.combineDateTime(dates.create('2014-11-21 12:23:11.123'), dates.create('2017-12-10 05:15:50.999'));
      expect(date.toISOString()).toBe(dates.create('2014-11-21 05:15:50.999').toISOString());
    });

    it('uses 01-01-1970 as date part if date is omitted', () => {
      let date = dates.combineDateTime(null, dates.create('2017-12-10 05:15:50.999'));
      expect(date.toISOString()).toBe(dates.create('1970-01-01 05:15:50.999').toISOString());
    });

    it('uses 00:00 as time part if time is omitted', () => {
      let date = dates.combineDateTime(dates.create('2017-12-10 05:15:50.999'));
      expect(date.toISOString()).toBe(dates.create('2017-12-10 00:00:00.000').toISOString());
    });
  });
});
