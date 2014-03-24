describe("DateFormat", function() {
  var scout;
  var symbolsByLocale;

  beforeEach(function() {
    setFixtures(sandbox());
    initSymbols();
    scout = new Scout.Session($('#sandbox'), '1.1');
    scout.locale = createLocale('de');
  });

  afterEach(function() {
    scout = null;
  });

  function createLocale(locale) {
    var symbols = {};
    symbols.dateFormatSymbols = symbolsByLocale[locale];
    return new Scout.Locale(symbols);
  }

  function initSymbols() {
    symbolsByLocale = {};
    symbolsByLocale.de = createSymbolsForDe();
  }

  function createSymbolsForDe() {
    return {
      "weekdays": ['', 'Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
      "weekdaysShort": ['', 'So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
      "months": ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember', ''],
      "monthsShort": ['Jan', 'Feb', 'Mrz', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez', ''],
      "am": "AM",
      "pm": "PM"
    };
  }

  function dateInMillis(dateStr) {
    return new Date(dateStr).getTime();
  }

  describe("format", function() {

    it("considers d M y", function() {
      var pattern = 'dd.MM.yy';
      var dateFormat = new Scout.DateFormat(scout, pattern);

      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('21.03.14');

      pattern = 'dd.MM.yyyy';
      dateFormat = new Scout.DateFormat(scout, pattern);
      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('21.03.2014');

      //FIXME does not work
      //      pattern = 'd.M.y';
      //      dateFormat = new Scout.DateFormat(scout, pattern);
      //      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('21.3.2014');
      //      expect(dateFormat.format(dateInMillis("2004-03-01"))).toBe('1.3.2004');
    });

    it("considers E", function() {
      var pattern = 'E, dd.MM.yy';
      var dateFormat = new Scout.DateFormat(scout, pattern);

      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('Fr, 21.03.14');

      //FIXME does not work
      //      pattern = 'EEE, dd.MM.yy';
      //      dateFormat = new Scout.DateFormat(scout, pattern);
      //      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('Fr, 21.03.14');

      //      pattern = 'EEEE, dd.MM.yy';
      //      dateFormat = new Scout.DateFormat(scout, pattern);
      //      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('Freitag, 21.03.14');

      //      pattern = 'EEEEE, dd.MM.yy';
      //      dateFormat = new Scout.DateFormat(scout, pattern);
      //      expect(dateFormat.format(dateInMillis("2014-03-21"))).toBe('Freitag, 21.03.14');
    });

  });

});
