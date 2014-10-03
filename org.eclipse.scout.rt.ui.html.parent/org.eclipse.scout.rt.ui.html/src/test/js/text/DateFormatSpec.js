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

      expect(dateFormat.format(new Date("2014-03-21"))).toBe('21.03.14');

      pattern = 'dd.MM.yyyy';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(new Date("2014-03-21"))).toBe('21.03.2014');

      //FIXME does not work
      //      pattern = 'd.M.y';
      //      dateFormat = new scout.DateFormat(locale, pattern);
      //      expect(dateFormat.format(new Date("2014-03-21"))).toBe('21.3.2014');
      //      expect(dateFormat.format(new Date("2004-03-01"))).toBe('1.3.2004');
    });

    it("considers E", function() {
      var pattern = 'E, dd.MM.yy';
      var dateFormat = new scout.DateFormat(locale, pattern);

      expect(dateFormat.format(new Date("2014-03-21"))).toBe('Fr, 21.03.14');

      //FIXME does not work
      //      pattern = 'EEE, dd.MM.yy';
      //      dateFormat = new scout.DateFormat(locale, pattern);
      //      expect(dateFormat.format(new Date("2014-03-21"))).toBe('Fr, 21.03.14');

      //      pattern = 'EEEE, dd.MM.yy';
      //      dateFormat = new scout.DateFormat(locale, pattern);
      //      expect(dateFormat.format(new Date("2014-03-21"))).toBe('Freitag, 21.03.14');

      //      pattern = 'EEEEE, dd.MM.yy';
      //      dateFormat = new scout.DateFormat(locale, pattern);
      //      expect(dateFormat.format(new Date("2014-03-21"))).toBe('Freitag, 21.03.14');
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

});
