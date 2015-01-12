/* global LocaleSpecHelper */
describe("DateFormat", function() {
  var locale;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    helper = new LocaleSpecHelper();
    locale = helper.createLocale('de');
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

  describe("analyze", function() {
    describe("analyzes the text and returns an object with months, years and days", function() {
      it('considers pattern dd.MM.yyyy', function() {
        var pattern = 'dd.MM.yyyy';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('21.12.2014');
        expect(result.day).toBe('21');
        expect(result.month).toBe('12');
        expect(result.year).toBe('2014');
      });

      it('considers pattern dd.MM.yyyy', function() {
        var pattern = 'yyyy-MM-dd';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('2000-08-12');
        expect(result.day).toBe('12');
        expect(result.month).toBe('08');
        expect(result.year).toBe('2000');
      });

      it('considers pattern dd.MM.yyyy', function() {
        var pattern = 'MM/dd/yyyy';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('08/16/1999');
        expect(result.day).toBe('16');
        expect(result.month).toBe('08');
        expect(result.year).toBe('1999');
      });
    });
  });


  describe("weekInYear", function() {

    it("can calculate week in year", function() {
      var dateFormat = new scout.DateFormat(locale);

      expect(dateFormat.weekInYear()).toBe(undefined);
      expect(dateFormat.weekInYear(undefined)).toBe(undefined);
      expect(dateFormat.weekInYear(null)).toBe(undefined);

      expect(dateFormat.weekInYear(scout.dates.create('2014-12-28'))).toBe(52);
      expect(dateFormat.weekInYear(scout.dates.create('2014-12-29'))).toBe(1);
      expect(dateFormat.weekInYear(scout.dates.create('2014-12-30'))).toBe(1);
      expect(dateFormat.weekInYear(scout.dates.create('2014-12-31'))).toBe(1);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-01'))).toBe(1);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-02'))).toBe(1);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-04'))).toBe(1);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-05'))).toBe(2);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-06'))).toBe(2);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-07'))).toBe(2);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-08'))).toBe(2);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-09'))).toBe(2);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-10'))).toBe(2);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-11'))).toBe(2);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-12'))).toBe(3);
      expect(dateFormat.weekInYear(scout.dates.create('2015-01-26'))).toBe(5);
    });

  });

});
