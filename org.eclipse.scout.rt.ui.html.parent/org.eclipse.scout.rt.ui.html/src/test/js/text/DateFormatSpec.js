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

  function dateInMillis(dateStr) {
    return new Date(dateStr).getTime();
  }

  describe("format", function() {

    it("considers d M y", function() {
      var pattern = 'dd.MM.yy';
      var dateFormat = new scout.DateFormat(locale, pattern);

      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('21.03.14');

      pattern = 'dd.MM.yyyy';
      dateFormat = new scout.DateFormat(locale, pattern);
      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('21.03.2014');

      //FIXME does not work
      //      pattern = 'd.M.y';
      //      dateFormat = new scout.DateFormat(locale, pattern);
      //      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('21.3.2014');
      //      expect(dateFormat.format(dateInMillis("2004-03-01"))).toBe('1.3.2004');
    });

    it("considers E", function() {
      var pattern = 'E, dd.MM.yy';
      var dateFormat = new scout.DateFormat(locale, pattern);

      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('Fr, 21.03.14');

      //FIXME does not work
      //      pattern = 'EEE, dd.MM.yy';
      //      dateFormat = new scout.DateFormat(locale, pattern);
      //      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('Fr, 21.03.14');

      //      pattern = 'EEEE, dd.MM.yy';
      //      dateFormat = new scout.DateFormat(locale, pattern);
      //      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('Freitag, 21.03.14');

      //      pattern = 'EEEEE, dd.MM.yy';
      //      dateFormat = new scout.DateFormat(locale, pattern);
      //      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('Freitag, 21.03.14');
    });

  });

});
