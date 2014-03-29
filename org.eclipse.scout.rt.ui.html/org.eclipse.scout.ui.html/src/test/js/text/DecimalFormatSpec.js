describe("DecimalFormat", function() {
  var scout;
  var symbolsByLocale;

  beforeEach(function() {
    setFixtures(sandbox());
    initSymbols();
    scout = new Scout.Session($('#sandbox'), '1.1');
    scout.locale = createLocale('de_CH');
  });

  afterEach(function() {
    scout = null;
  });

  function createLocale(locale) {
    var symbols = {};
    symbols.decimalFormatSymbols = symbolsByLocale[locale];
    return new Scout.Locale(symbols);
  }

  function initSymbols() {
    symbolsByLocale = {};
    symbolsByLocale.de_CH = createSymbolsForDeCH();
    symbolsByLocale.de_DE = createSymbolsForDeDE();
  }

  function createSymbolsForDeCH() {
    return {
      "digit": "#",
      "zeroDigit": "0",
      "decimalSeparator": ".",
      "groupingSeparator": "'",
      "minusSign": "-",
      "patternSeparator": ";"
    };
  }

  function createSymbolsForDeDE() {
    var symbols = createSymbolsForDeCH();
    symbols.decimalSeparator = ",";
    symbols.groupingSeparator = ".";
    return symbols;
  }

  describe("format", function() {

    it("considers decimal separators", function() {
      var pattern = '###0.00';
      var decimalFormat = new Scout.DecimalFormat(scout, pattern);

      expect(decimalFormat.format(0)).toBe('0.00');
      expect(decimalFormat.format(0.000)).toBe('0.00');
      expect(decimalFormat.format(1000.1234)).toBe('1000.12');

      scout.locale = createLocale('de_DE');
      pattern = '###0,00';
      decimalFormat = new Scout.DecimalFormat(scout, pattern);

      expect(decimalFormat.format(0)).toBe('0,00');
      expect(decimalFormat.format(0.000)).toBe('0,00');
      expect(decimalFormat.format(1000.1234)).toBe('1000,12');
    });

    it("considers grouping separators", function() {
      var pattern = '#\'##0.00';
      var decimalFormat = new Scout.DecimalFormat(scout, pattern);

      expect(decimalFormat.format(0)).toBe('0.00');
      expect(decimalFormat.format(10)).toBe('10.00');
      expect(decimalFormat.format(100)).toBe('100.00');
      expect(decimalFormat.format(1000.1234)).toBe('1\'000.12');
      expect(decimalFormat.format(50121000.1234)).toBe('50\'121\'000.12');
      expect(decimalFormat.format(100005121000.1234)).toBe('100\'005\'121\'000.12');

      scout.locale = createLocale('de_DE');
      pattern = '#.##0,00';
      decimalFormat = new Scout.DecimalFormat(scout, pattern);

      expect(decimalFormat.format(0)).toBe('0,00');
      expect(decimalFormat.format(10)).toBe('10,00');
      expect(decimalFormat.format(100)).toBe('100,00');
      expect(decimalFormat.format(1000.1234)).toBe('1.000,12');
      expect(decimalFormat.format(50121000.1234)).toBe('50.121.000,12');
      expect(decimalFormat.format(100005121000.1234)).toBe('100.005.121.000,12');
    });

    it("distinguishes digits and zero digits", function() {
      var pattern = '##0.#';
      var decimalFormat = new Scout.DecimalFormat(scout, pattern);

      expect(decimalFormat.format(0)).toBe('0');
      expect(decimalFormat.format(112)).toBe('112');

      pattern = '#.#';
      decimalFormat = new Scout.DecimalFormat(scout, pattern);

      expect(decimalFormat.format(0)).toBe('');
      expect(decimalFormat.format(112)).toBe('112');

      //FIXME pattern without separator don't work
      //      pattern = '#';
      //      decimalFormat = new Scout.DecimalFormat(scout, pattern);
      //
      //      expect(decimalFormat.format(0)).toBe('');
      //      expect(decimalFormat.format(112)).toBe('112');
      //
      //      pattern = '0';
      //      decimalFormat = new Scout.DecimalFormat(scout, pattern);
      //
      //      expect(decimalFormat.format(0)).toBe('0');
      //      expect(decimalFormat.format(112)).toBe('112');

      pattern = '0000.0000';
      decimalFormat = new Scout.DecimalFormat(scout, pattern);

      expect(decimalFormat.format(1)).toBe('0001.0000');
      expect(decimalFormat.format(125112)).toBe('125112.0000');
    });

  });

});
