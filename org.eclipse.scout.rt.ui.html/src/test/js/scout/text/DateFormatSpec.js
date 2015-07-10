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

      it('considers pattern dd.MM.yyyy', function() {
        var pattern = 'yyyy-MM-dd';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('2000-08-12');
        expect(result.matchInfo.day).toBe('12');
        expect(result.matchInfo.month).toBe('08');
        expect(result.matchInfo.year).toBe('2000');
      });

      it('considers pattern dd.MM.yyyy', function() {
        var pattern = 'MM/dd/yyyy';
        var dateFormat = new scout.DateFormat(locale, pattern);

        var result = dateFormat.analyze('08/16/1999');
        expect(result.matchInfo.day).toBe('16');
        expect(result.matchInfo.month).toBe('08');
        expect(result.matchInfo.year).toBe('1999');
      });
    });
  });

});
