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
/* global LocaleSpecHelper */
describe("DateFormat", function() {
  var locale;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    helper = new LocaleSpecHelper();
    locale = helper.createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
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

      it('proposes valid dates', function() {
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
    });
  });

});
