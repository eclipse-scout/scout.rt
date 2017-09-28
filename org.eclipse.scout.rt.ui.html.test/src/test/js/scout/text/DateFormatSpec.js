/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("DateFormat", function() {
  var locale;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    helper = new scout.LocaleSpecHelper();
    locale = helper.createLocale(scout.LocaleSpecHelper.DEFAULT_LOCALE);
  });

  afterEach(function() {
    locale = null;
  });

  describe("format", function() {

    it("considers d M y", function() {
      var pattern = 'dd.MM.yy';
      var dateFormat = new scout.DateFormat(locale, pattern);

      expect(dateFormat.format(scout.dates.create('2014-03-21'))).toBe('21.03.14');

      pattern = 'dd.MM.yyyy';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2014-03-21'))).toBe('21.03.2014');

      pattern = 'd.M.y';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2014-03-21'))).toBe('21.3.14');
      expect(dateFormat.format(scout.dates.create('2004-03-01'))).toBe('1.3.04');

      pattern = 'dd.MM.yy HH:mm';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2017-01-16'))).toBe('16.01.17 00:00');
    });

    it("considers h H m a", function() {
      var pattern = 'HH:mm';
      var dateFormat = new scout.DateFormat(locale, pattern);

      expect(dateFormat.format(scout.dates.create('2017-01-01 13:01'))).toBe('13:01');
      expect(dateFormat.format(scout.dates.create('2017-01-01 05:01'))).toBe('05:01');

      pattern = 'H:mm';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2017-01-01 13:01'))).toBe('13:01');
      expect(dateFormat.format(scout.dates.create('2017-01-01 05:01'))).toBe('5:01');

      pattern = 'hh:mm';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2017-01-01 13:01'))).toBe('01:01');

      pattern = 'h:mm';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2017-01-01 13:01'))).toBe('1:01');

      pattern = 'h:mm a';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2017-01-01 13:01'))).toBe('1:01 PM');
      expect(dateFormat.format(scout.dates.create('2017-01-01 01:01'))).toBe('1:01 AM');
      expect(dateFormat.format(scout.dates.create('2017-01-01 00:00'))).toBe('12:00 AM');
      expect(dateFormat.format(scout.dates.create('2017-01-01 00:01'))).toBe('12:01 AM');
      expect(dateFormat.format(scout.dates.create('2017-01-01 12:00'))).toBe('12:00 PM');
      expect(dateFormat.format(scout.dates.create('2017-01-01 12:01'))).toBe('12:01 PM');

      pattern = 'hh:mm a';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2017-01-01 13:01'))).toBe('01:01 PM');
      expect(dateFormat.format(scout.dates.create('2017-01-01 01:01'))).toBe('01:01 AM');
    });

    it("considers E", function() {
      var pattern = 'E, dd.MM.yy';
      var dateFormat = new scout.DateFormat(locale, pattern);

      expect(dateFormat.format(scout.dates.create('2014-03-21'))).toBe('Fr, 21.03.14');

      pattern = 'EEE, dd.MM.yy';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2014-03-21'))).toBe('Fr, 21.03.14');

      pattern = 'EEEE, dd.MM.yy';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2014-03-21'))).toBe('Freitag, 21.03.14');

      pattern = 'EEEE, dd.MM.yy';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(scout.dates.create('2014-03-21'))).toBe('Freitag, 21.03.14');
    });

  });

  describe("parse", function() {

    it("considers d M y", function() {
      var pattern = 'dd.MM.yy';
      var dateFormat = new scout.DateFormat(locale, pattern);

      expect(dateFormat.parse('21.03.14').getTime()).toBe(scout.dates.create('2014-03-21').getTime());

      pattern = 'dd.MM.yyyy';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.parse('21.03.2014').getTime()).toBe(scout.dates.create('2014-03-21').getTime());

      pattern = 'd.M.y';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.parse('21.3.14').getTime()).toBe(scout.dates.create('2014-03-21').getTime());
      expect(dateFormat.parse('1.3.04').getTime()).toBe(scout.dates.create('2004-03-01').getTime());
    });

    it("considers h H m a", function() {
      var pattern = 'yyyy-MM-dd HH:mm';
      var dateFormat = new scout.DateFormat(locale, pattern);

      expect(dateFormat.parse('2017-01-01 12:00').getTime()).toBe(scout.dates.create('2017-01-01 12:00').getTime());
      expect(dateFormat.parse('2017-01-01 13:00').getTime()).toBe(scout.dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00').getTime()).toBe(scout.dates.create('2017-01-01 01:00').getTime());

      pattern = 'yyyy-MM-dd H:mm';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.parse('2017-01-01 12:00').getTime()).toBe(scout.dates.create('2017-01-01 12:00').getTime());
      expect(dateFormat.parse('2017-01-01 13:00').getTime()).toBe(scout.dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00').getTime()).toBe(scout.dates.create('2017-01-01 01:00').getTime());
      expect(dateFormat.parse('2017-01-01 1:00').getTime()).toBe(scout.dates.create('2017-01-01 01:00').getTime());

      pattern = 'yyyy-MM-dd hh:mm a';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.parse('2017-01-01 1:00 PM')).toBe(null);
      expect(dateFormat.parse('2017-01-01 1:00 AM')).toBe(null);
      expect(dateFormat.parse('2017-01-01 01:00 PM').getTime()).toBe(scout.dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00 AM').getTime()).toBe(scout.dates.create('2017-01-01 01:00').getTime());
      expect(dateFormat.parse('2017-01-01 12:00 PM').getTime()).toBe(scout.dates.create('2017-01-01 12:00').getTime());
      expect(dateFormat.parse('2017-01-01 12:00 AM').getTime()).toBe(scout.dates.create('2017-01-01 00:00').getTime());

      pattern = 'yyyy-MM-dd h:mm a';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.parse('2017-01-01 1:00 PM').getTime()).toBe(scout.dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 1:00 AM').getTime()).toBe(scout.dates.create('2017-01-01 01:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00 PM').getTime()).toBe(scout.dates.create('2017-01-01 13:00').getTime());
      expect(dateFormat.parse('2017-01-01 01:00 AM').getTime()).toBe(scout.dates.create('2017-01-01 01:00').getTime());
      expect(dateFormat.parse('2017-01-01 12:00 PM').getTime()).toBe(scout.dates.create('2017-01-01 12:00').getTime());
      expect(dateFormat.parse('2017-01-01 12:00 AM').getTime()).toBe(scout.dates.create('2017-01-01 00:00').getTime());
    });
  });

  describe("analyze", function() {
    describe("analyzes the text and returns an object with months, years and days", function() {
      it('considers pattern dd.MM.yyyy', function() {
        var pattern = 'dd.MM.yyyy';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('21.12.2014');
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

      it('considers pattern yyyy-MM-dd', function() {
        var pattern = 'yyyy-MM-dd';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('2000-08-12');
        expect(result.matchInfo.day).toBe('12');
        expect(result.matchInfo.month).toBe('08');
        expect(result.matchInfo.year).toBe('2000');
      });

      it('considers pattern MM/dd/yyy', function() {
        var pattern = 'MM/dd/yyyy';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('08/16/1999');
        expect(result.matchInfo.day).toBe('16');
        expect(result.matchInfo.month).toBe('08');
        expect(result.matchInfo.year).toBe('1999');
      });

      it('checks correct handling of am/pm', function() {
        var pattern = 'yyyy-MM-dd h:mm a';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('2017-01-01 12:00 AM');
        expect(result.dateInfo.hours).toBe(0);
        expect(result.dateInfo.minutes).toBe(0);

        result = dateFormat.analyze('2017-01-01 12:00 PM');
        expect(result.dateInfo.hours).toBe(12);
        expect(result.dateInfo.minutes).toBe(0);

        result = dateFormat.analyze('2017-01-01 1:01 PM');
        expect(result.dateInfo.hours).toBe(13);
        expect(result.dateInfo.minutes).toBe(1);
      });

      it('proposes valid dates for pattern dd.MM.yyyy', function() {
        var pattern = 'dd.MM.yyyy';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('2', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('02.02.2016');

        result = dateFormat.analyze('21', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('21.02.2016');

        result = dateFormat.analyze('29', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('29.02.2016');

        result = dateFormat.analyze('29', scout.dates.create('2015-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('29.03.2015');

        result = dateFormat.analyze('30', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('30.03.2016');

        result = dateFormat.analyze('31', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('31.03.2016');

        result = dateFormat.analyze('31', scout.dates.create('2016-03-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('31.03.2016');

        result = dateFormat.analyze('31', scout.dates.create('2016-04-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('31.05.2016');

        result = dateFormat.analyze('32', scout.dates.create('2016-04-01'));
        expect(result.predictedDate).toBe(null);
      });

      it('proposes valid dates for pattern MM.yyyy', function() {
        var pattern = 'MM.yyyy';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('02.2017', scout.dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('02.2017');

        result = dateFormat.analyze('04.2017', scout.dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('04.2017');

        result = dateFormat.analyze('05.2017', scout.dates.create('2017-03-31'));
        expect(dateFormat.format(result.predictedDate)).toBe('05.2017');

        result = dateFormat.analyze('05.2017', scout.dates.create('2017-03-10'));
        expect(dateFormat.format(result.predictedDate)).toBe('05.2017');
      });

      it('proposes valid dates for pattern yyyy', function() {
        var pattern = 'yyyy';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('2017', scout.dates.create('2016-02-29'));
        expect(dateFormat.format(result.predictedDate)).toBe('2017');
      });

      it('proposes valid times', function() {
        var pattern = 'HH:mm';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('2', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('02:00');

        result = dateFormat.analyze('20', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('20:00');

        pattern = 'h:mm a';
        dateFormat = new scout.DateFormat(locale, pattern);

        result = dateFormat.analyze('2', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('2:00 AM');

        result = dateFormat.analyze('20', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('2:00 AM');

        result = dateFormat.analyze('0', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('1:00 AM');

        result = dateFormat.analyze('1', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('1:00 AM');

        result = dateFormat.analyze('11:59', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('11:59 AM');

        result = dateFormat.analyze('11:59 p', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('11:59 PM');

        result = dateFormat.analyze('11:59 pm', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('11:59 PM');

        result = dateFormat.analyze('11:59 a', scout.dates.create('2016-02-01'));
        expect(dateFormat.format(result.predictedDate)).toBe('11:59 AM');
      });
    });
  });

});
