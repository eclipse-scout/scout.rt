/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateFormat, dates, Locale, strings} from '../../src/index';
import {LocaleSpecHelper} from '../../src/testing/index';

describe('DateFormat', () => {
  let locale: Locale;
  let helper: LocaleSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    helper = new LocaleSpecHelper();
    locale = helper.createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
  });

  afterEach(() => {
    locale = null;
  });

  describe('format', () => {

    it('considers d M y', () => {
      let pattern = 'dd.MM.yy';
      let dateFormat = new DateFormat(locale, pattern);

      expect(dateFormat.format(dates.create('2014-03-21'))).toBe('21.03.14');

      pattern = 'dd.MM.yyyy';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2014-03-21'))).toBe('21.03.2014');
      expect(dateFormat.format(dates.create('20144-03-21'))).toBe('21.03.20144');

      pattern = 'd.M.y';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2014-03-21'))).toBe('21.3.14');
      expect(dateFormat.format(dates.create('2004-03-01'))).toBe('1.3.04');

      pattern = 'dd.MM.yy HH:mm';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2017-01-16'))).toBe('16.01.17 00:00');
    });

    it('considers h H m a', () => {
      let pattern = 'HH:mm';
      let dateFormat = new DateFormat(locale, pattern);

      expect(dateFormat.format(dates.create('2017-01-01 13:01'))).toBe('13:01');
      expect(dateFormat.format(dates.create('2017-01-01 05:01'))).toBe('05:01');

      pattern = 'H:mm';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2017-01-01 13:01'))).toBe('13:01');
      expect(dateFormat.format(dates.create('2017-01-01 05:01'))).toBe('5:01');

      pattern = 'hh:mm';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2017-01-01 13:01'))).toBe('01:01');

      pattern = 'h:mm';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2017-01-01 13:01'))).toBe('1:01');

      pattern = 'h:mm a';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2017-01-01 13:01'))).toBe('1:01 PM');
      expect(dateFormat.format(dates.create('2017-01-01 01:01'))).toBe('1:01 AM');
      expect(dateFormat.format(dates.create('2017-01-01 00:00'))).toBe('12:00 AM');
      expect(dateFormat.format(dates.create('2017-01-01 00:01'))).toBe('12:01 AM');
      expect(dateFormat.format(dates.create('2017-01-01 12:00'))).toBe('12:00 PM');
      expect(dateFormat.format(dates.create('2017-01-01 12:01'))).toBe('12:01 PM');

      pattern = 'hh:mm a';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2017-01-01 13:01'))).toBe('01:01 PM');
      expect(dateFormat.format(dates.create('2017-01-01 01:01'))).toBe('01:01 AM');
    });

    it('considers E', () => {
      let pattern = 'E, dd.MM.yy';
      let dateFormat = new DateFormat(locale, pattern);

      expect(dateFormat.format(dates.create('2014-03-21'))).toBe('Fr, 21.03.14');

      pattern = 'EEE, dd.MM.yy';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2014-03-21'))).toBe('Fr, 21.03.14');

      pattern = 'EEEE, dd.MM.yy';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2014-03-21'))).toBe('Freitag, 21.03.14');

      pattern = 'EEEE, dd.MM.yy';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(dates.create('2014-03-21'))).toBe('Freitag, 21.03.14');
    });

    it('considers ss SSS Z', () => {
      let date = dates.create('2014-03-21 13:01'),
        offset = Math.abs(date.getTimezoneOffset()),
        isNegative = offset !== date.getTimezoneOffset(),
        timeZone = (isNegative ? '-' : '+') + strings.padZeroLeft(Math.floor(offset / 60), 2) + strings.padZeroLeft(offset % 60, 2);

      let pattern = 'yyyy-MM-dd HH:mm:ss';
      let dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(date)).toBe('2014-03-21 13:01:00');

      pattern = 'yyyy-MM-dd HH:mm:ss.SSS';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(date)).toBe('2014-03-21 13:01:00.000');

      pattern = 'yyyy-MM-ddTHH:mm:ss.SSSZ';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.format(date)).toBe('2014-03-21T13:01:00.000' + timeZone);
    });
  });

  describe('parse', () => {

    it('considers d M y', () => {
      let pattern = 'dd.MM.yy';
      let dateFormat = new DateFormat(locale, pattern);

      expect(dateFormat.parse('21.03.14').getTime()).toBe(dates.create('2014-03-21').getTime());

      pattern = 'dd.MM.yyyy';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.parse('21.03.2014').getTime()).toBe(dates.create('2014-03-21').getTime());

      pattern = 'd.M.y';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.parse('21.3.14').getTime()).toBe(dates.create('2014-03-21').getTime());
      expect(dateFormat.parse('1.3.04').getTime()).toBe(dates.create('2004-03-01').getTime());
    });

    it('considers h H m a', () => {
      let pattern = 'yyyy-MM-dd HH:mm';
      let dateFormat = new DateFormat(locale, pattern);

      expect(dateFormat.parse('2017-01-01 12:00').getTime()).toBe(dates.create('2017-01-01 12:00').getTime());
      expect(dateFormat.parse('2017-01-01 13:00').getTime()).toBe(dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00').getTime()).toBe(dates.create('2017-01-01 01:00').getTime());

      pattern = 'yyyy-MM-dd H:mm';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.parse('2017-01-01 12:00').getTime()).toBe(dates.create('2017-01-01 12:00').getTime());
      expect(dateFormat.parse('2017-01-01 13:00').getTime()).toBe(dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00').getTime()).toBe(dates.create('2017-01-01 01:00').getTime());
      expect(dateFormat.parse('2017-01-01 1:00').getTime()).toBe(dates.create('2017-01-01 01:00').getTime());

      pattern = 'yyyy-MM-dd hh:mm a';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.parse('2017-01-01 1:00 PM')).toBe(null);
      expect(dateFormat.parse('2017-01-01 1:00 AM')).toBe(null);
      expect(dateFormat.parse('2017-01-01 01:00 PM').getTime()).toBe(dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00 AM').getTime()).toBe(dates.create('2017-01-01 01:00').getTime());
      expect(dateFormat.parse('2017-01-01 12:00 PM').getTime()).toBe(dates.create('2017-01-01 12:00').getTime());
      expect(dateFormat.parse('2017-01-01 12:00 AM').getTime()).toBe(dates.create('2017-01-01 00:00').getTime());

      pattern = 'yyyy-MM-dd h:mm a';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.parse('2017-01-01 1:00 PM').getTime()).toBe(dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 1:00 AM').getTime()).toBe(dates.create('2017-01-01 01:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00 PM').getTime()).toBe(dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00 AM').getTime()).toBe(dates.create('2017-01-01 01:00').getTime());
      expect(dateFormat.parse('2017-01-01 12:00 PM').getTime()).toBe(dates.create('2017-01-01 12:00').getTime());
      expect(dateFormat.parse('2017-01-01 12:00 AM').getTime()).toBe(dates.create('2017-01-01 00:00').getTime());
    });

    it('considers ss SSS Z', () => {
      let pattern = 'yyyy-MM-dd HH:mm:ss';
      let dateFormat = new DateFormat(locale, pattern);

      expect(dateFormat.parse('2017-01-01 12:00:05').getTime()).toBe(dates.create('2017-01-01 12:00:05').getTime());
      expect(dateFormat.parse('2017-01-01 13:00:05').getTime()).toBe(dates.create('2017-01-01 13:00:05').getTime());
      expect(dateFormat.parse('2017-01-01 01:00:05').getTime()).toBe(dates.create('2017-01-01 01:00:05').getTime());

      pattern = 'yyyy-MM-dd HH:mm:ss.SSS';
      dateFormat = new DateFormat(locale, pattern);
      expect(dateFormat.parse('2017-01-01 12:00:05.123').getTime()).toBe(dates.create('2017-01-01 12:00:05.123').getTime());
      expect(dateFormat.parse('2017-01-01 13:00:05.123').getTime()).toBe(dates.create('2017-01-01 13:00:05.123').getTime());
      expect(dateFormat.parse('2017-01-01 01:00:05.123').getTime()).toBe(dates.create('2017-01-01 01:00:05.123').getTime());

      pattern = 'yyyy-MM-ddTHH:mm:ss.SSSZ';
      dateFormat = new DateFormat(locale, pattern);
      let refDate = dates.create('2017-01-01 12:00:05.123');
      refDate.setMinutes(refDate.getMinutes() - refDate.getTimezoneOffset() - 6 * 60);
      expect(dateFormat.parse('2017-01-01T12:00:05.123-0600').getTime()).toBe(refDate.getTime());
    });
  });

  describe('analyze', () => {
    describe('analyzes the text and returns an object with months, years and days', () => {
      it('considers pattern dd.MM.yyyy', () => {
        let pattern = 'dd.MM.yyyy';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('21.12.2014');
        expect(result.matchInfo.day).toBe('21');
        expect(result.matchInfo.month).toBe('12');
        expect(result.matchInfo.year).toBe('2014');
        expect(result.parsedPattern).toBe('dd.MM.yyyy');

        result = dateFormat.analyze('21.8.2014');
        expect(result.matchInfo.day).toBe('21');
        expect(result.matchInfo.month).toBe('8');
        expect(result.matchInfo.year).toBe('2014');
        expect(result.parsedPattern).toBe('dd.M.yyyy');
      });

      it('considers pattern yyyy-MM-dd', () => {
        let pattern = 'yyyy-MM-dd';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('2000-08-12');
        expect(result.matchInfo.day).toBe('12');
        expect(result.matchInfo.month).toBe('08');
        expect(result.matchInfo.year).toBe('2000');
      });

      it('considers pattern MM/dd/yyy', () => {
        let pattern = 'MM/dd/yyyy';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('08/16/1999');
        expect(result.matchInfo.day).toBe('16');
        expect(result.matchInfo.month).toBe('08');
        expect(result.matchInfo.year).toBe('1999');
      });

      it('checks correct handling of am/pm', () => {
        let pattern = 'yyyy-MM-dd h:mm a';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('2017-01-01 12:00 AM');
        expect(result.dateInfo.hours).toBe(0);
        expect(result.dateInfo.minutes).toBe(0);

        result = dateFormat.analyze('2017-01-01 12:00 PM');
        expect(result.dateInfo.hours).toBe(12);
        expect(result.dateInfo.minutes).toBe(0);

        result = dateFormat.analyze('2017-01-01 1:01 PM');
        expect(result.dateInfo.hours).toBe(13);
        expect(result.dateInfo.minutes).toBe(1);
      });

      it('proposes valid dates for pattern dd.MM.yyyy', () => {
        let pattern = 'dd.MM.yyyy';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('2', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('02.02.2016');

        result = dateFormat.analyze('21', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('21.02.2016');

        result = dateFormat.analyze('29', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('29.02.2016');

        result = dateFormat.analyze('29', dates.create('2015-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('29.03.2015');

        result = dateFormat.analyze('30', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('30.03.2016');

        result = dateFormat.analyze('31', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('31.03.2016');

        result = dateFormat.analyze('31', dates.create('2016-03-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('31.03.2016');

        result = dateFormat.analyze('31', dates.create('2016-04-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('31.05.2016');

        result = dateFormat.analyze('32', dates.create('2016-04-01'));
        expect(result.predictedDate).toBe(null);

        result = dateFormat.analyze('2', dates.create('2024-02-29'));
        expect(dateFormat.format(result.predictedDate)).toBe('02.02.2024');

        result = dateFormat.analyze('3', dates.create('2024-02-29'));
        expect(dateFormat.format(result.predictedDate)).toBe('03.02.2024');

        result = dateFormat.analyze('30', dates.create('2024-02-29'));
        expect(dateFormat.format(result.predictedDate)).toBe('30.03.2024');
      });

      it('proposes valid dates for pattern MM.yyyy', () => {
        let pattern = 'MM.yyyy';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('02.2017', dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('02.2017');

        result = dateFormat.analyze('04.2017', dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('04.2017');

        result = dateFormat.analyze('05.2017', dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('05.2017');

        result = dateFormat.analyze('05.2017', dates.create('2017-03-10'));
        expect(dateFormat.format(result.predictedDate)).toBe('05.2017');
      });

      it('proposes valid dates for pattern yyyy', () => {
        let pattern = 'yyyy';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('2017', dates.create('2016-02-29'));
        expect(dateFormat.format(result.predictedDate)).toBe('2017');
      });

      it('proposes valid dates for pattern yyyy-MM', () => {
        let pattern = 'yyyy-MM';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('17-2', dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('2017-02');

        result = dateFormat.analyze('17-4', dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('2017-04');

        result = dateFormat.analyze('17-5', dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('2017-05');

        result = dateFormat.analyze('17-5', dates.create('2017-03-10'));
        expect(dateFormat.format(result.predictedDate)).toBe('2017-05');

        result = dateFormat.analyze('2', dates.create('2024-02-29')); // feb-29 does not exist in 2002 -> should still predict february
        expect(dateFormat.format(result.predictedDate)).toBe('2002-02');

        result = dateFormat.analyze('24', dates.create('2024-02-29'));
        expect(dateFormat.format(result.predictedDate)).toBe('2024-02');

        result = dateFormat.analyze('2-3', dates.create('2024-02-29'));
        expect(dateFormat.format(result.predictedDate)).toBe('2002-03');
      });

      it('proposes valid times', () => {
        let pattern = 'HH:mm';
        let dateFormat = new DateFormat(locale, pattern);

        let result = dateFormat.analyze('2', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('02:00');

        result = dateFormat.analyze('20', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('20:00');

        pattern = 'h:mm a';
        dateFormat = new DateFormat(locale, pattern);

        result = dateFormat.analyze('2', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('2:00 AM');

        result = dateFormat.analyze('20', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('2:00 AM');

        result = dateFormat.analyze('0', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('1:00 AM');

        result = dateFormat.analyze('1', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('1:00 AM');

        result = dateFormat.analyze('11:59', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('11:59 AM');

        result = dateFormat.analyze('11:59 p', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('11:59 PM');

        result = dateFormat.analyze('11:59 pm', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('11:59 PM');

        result = dateFormat.analyze('11:59 a', dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('11:59 AM');
      });
    });
  });
});
