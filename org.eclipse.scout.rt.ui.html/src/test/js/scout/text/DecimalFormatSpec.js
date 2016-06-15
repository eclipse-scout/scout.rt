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
describe("DecimalFormat", function() {
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

    it("considers decimal separators", function() {
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '###0.00'
      });

      expect(decimalFormat.format(null)).toBe(null);
      expect(decimalFormat.format(0)).toBe('0.00');
      expect(decimalFormat.format(0.000)).toBe('0.00');
      expect(decimalFormat.format(1000.1234)).toBe('1000.12');
      expect(decimalFormat.format(1000.1234)).toBe('1000.12');
      expect(decimalFormat.format(56000.1234)).toBe('56000.12');

      // Without digits before decimal point
      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '.00'
      });

      expect(decimalFormat.format(0)).toBe('.00');
      expect(decimalFormat.format(0.000)).toBe('.00');
      expect(decimalFormat.format(1000.1234)).toBe('1000.12');
      expect(decimalFormat.format(12345.6789)).toBe('12345.68'); // rounding

      locale = helper.createLocale('de-DE');
      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '###0.00'
      });

      expect(decimalFormat.format(0)).toBe('0,00');
      expect(decimalFormat.format(0.000)).toBe('0,00');
      expect(decimalFormat.format(1000.1234)).toBe('1000,12');
    });

    it("considers grouping separators", function() {
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#,##0.00'
      });

      expect(decimalFormat.format(0)).toBe('0.00');
      expect(decimalFormat.format(10)).toBe('10.00');
      expect(decimalFormat.format(100)).toBe('100.00');
      expect(decimalFormat.format(1000.1234)).toBe('1\'000.12');
      expect(decimalFormat.format(50121000.1234)).toBe('50\'121\'000.12');
      expect(decimalFormat.format(100005121000.1234)).toBe('100\'005\'121\'000.12');

      locale = helper.createLocale('de-DE');
      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#,##0.00'
      });

      expect(decimalFormat.format(0)).toBe('0,00');
      expect(decimalFormat.format(10)).toBe('10,00');
      expect(decimalFormat.format(100)).toBe('100,00');
      expect(decimalFormat.format(1000.1234)).toBe('1.000,12');
      expect(decimalFormat.format(50121000.1234)).toBe('50.121.000,12');
      expect(decimalFormat.format(100005121000.1234)).toBe('100.005.121.000,12');
    });

    it("can swap the position of the minus sign", function() {
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '0.0-' // Group separator after decimal separator, 0 after #
      });

      expect(decimalFormat.format(0)).toBe('0.0');
      expect(decimalFormat.format(10)).toBe('10.0');
      expect(decimalFormat.format(-14.234)).toBe('14.2-');
    });

    it("can handle invalid patterns", function() {
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#.##0,00' // Group separator after decimal separator, 0 after #
      });

      expect(decimalFormat.format(0)).toBe('.000');
      expect(decimalFormat.format(10)).toBe('10.000');
      expect(decimalFormat.format(50121000.1234)).toBe('50121000.1234');
      expect(decimalFormat.format(50121000.1234567)).toBe('50121000.12346');
    });

    it("distinguishes digits and zero digits", function() {
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '##0.#'
      });

      expect(decimalFormat.format(0)).toBe('0');
      expect(decimalFormat.format(112)).toBe('112');

      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#.#'
      });

      expect(decimalFormat.format(0)).toBe('');
      expect(decimalFormat.format(112)).toBe('112');

      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '0000.0000'
      });

      expect(decimalFormat.format(1)).toBe('0001.0000');
      expect(decimalFormat.format(125112)).toBe('125112.0000');

      // patterns without separator:
      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#'
      });

      expect(decimalFormat.format(0)).toBe('');
      expect(decimalFormat.format(112)).toBe('112');

      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '0'
      });

      expect(decimalFormat.format(0)).toBe('0');
      expect(decimalFormat.format(112)).toBe('112');
    });

    it("can handle positive and negative subpattern", function() {
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '###0.00;minus 00.0#'
      });

      expect(decimalFormat.format(0)).toBe('0.00');
      expect(decimalFormat.format(0.000)).toBe('0.00');
      expect(decimalFormat.format(1000.1234)).toBe('1000.12');
      expect(decimalFormat.format(12345.6789)).toBe('12345.68'); // rounding

      expect(decimalFormat.format(-1)).toBe('minus 1.00'); // negative pattern is only used for prefix/suffix
      expect(decimalFormat.format(-2.000)).toBe('minus 2.00');
      expect(decimalFormat.format(-1000.1234)).toBe('minus 1000.12');
      expect(decimalFormat.format(-12345.6789)).toBe('minus 12345.68');

      // Formats positive numbers as negative and negative numbers as positive
      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '-0.00;0.00'
      });
      expect(decimalFormat.format(12)).toBe('-12.00');
      expect(decimalFormat.format(-924.566)).toBe('924.57');

      // Normal mode, auto-insertion of minus sign
      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '0.00'
      });
      expect(decimalFormat.format(12)).toBe('12.00');
      expect(decimalFormat.format(-924.566)).toBe('-924.57');

      // Prefix and minus sign position in front
      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '-X0.00'
      });
      expect(decimalFormat.format(12)).toBe('X12.00');
      expect(decimalFormat.format(-924.566)).toBe('-X924.57');

      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: 'X-0.00'
      });
      expect(decimalFormat.format(12)).toBe('X12.00');
      expect(decimalFormat.format(-924.566)).toBe('X-924.57');

      // Minus sign position at end
      decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '0.00-'
      });
      expect(decimalFormat.format(12)).toBe('12.00');
      expect(decimalFormat.format(-924.566)).toBe('924.57-');
    });

    it("can handle exotic symbols", function() {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!!';
      locale.decimalFormatSymbols.groupingSeparator = '~';
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#,##0.00'
      });

      expect(decimalFormat.format(0)).toBe('0!!00');
      expect(decimalFormat.format(0.000)).toBe('0!!00');
      expect(decimalFormat.format(1000.1234)).toBe('1~000!!12');
      expect(decimalFormat.format(12345.6789)).toBe('12~345!!68');
      expect(decimalFormat.format(1234500.6789)).toBe('1~234~500!!68');
      expect(decimalFormat.format(1234500.675)).toBe('1~234~500!!68');
      expect(decimalFormat.format(-1234500.6789)).toBe('M1~234~500!!68');
      expect(decimalFormat.format(-1234500.675)).toBe('M1~234~500!!68');

      expect(decimalFormat.format(-1)).toBe('M1!!00');
    });

    it("can handle percentages, format taken from application", function() {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!';
      locale.decimalFormatSymbols.groupingSeparator = '~';
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#,##0.00\'%\''
      });

      expect(decimalFormat.format(0)).toBe('0!00%');
      expect(decimalFormat.format(0.000)).toBe('0!00%');
      expect(decimalFormat.format(0.111)).toBe('0!11%');
      expect(decimalFormat.format(1000.1234)).toBe('1~000!12%');
      expect(decimalFormat.format(12345.6789)).toBe('12~345!68%');

      expect(decimalFormat.format(-1)).toBe('M1!00%');
    });

    it("can handle multiplier", function() {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!';
      locale.decimalFormatSymbols.groupingSeparator = '~';
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#,##0.00'
      });

      // positive multiplier
      decimalFormat.multiplier = 10;
      expect(decimalFormat.format(0)).toBe('0!00');
      expect(decimalFormat.format(0.000)).toBe('0!00');
      expect(decimalFormat.format(0.111)).toBe('1!11');
      expect(decimalFormat.format(1000.1234)).toBe('10~001!23');
      expect(decimalFormat.format(12345.6789)).toBe('123~456!79');
      expect(decimalFormat.format(-1)).toBe('M10!00');
      expect(decimalFormat.format(-1.002)).toBe('M10!02');

      // negative multiplier
      decimalFormat.multiplier = -10;
      expect(decimalFormat.format(0)).toBe('0!00');
      expect(decimalFormat.format(0.000)).toBe('0!00');
      expect(decimalFormat.format(0.111)).toBe('M1!11');
      expect(decimalFormat.format(1.002)).toBe('M10!02');
      expect(decimalFormat.format(1000.1234)).toBe('M10~001!23');
      expect(decimalFormat.format(12345.6789)).toBe('M123~456!79');
      expect(decimalFormat.format(-1)).toBe('10!00');
      expect(decimalFormat.format(-1.002)).toBe('10!02');
    });

    it("can handle rounding mode", function() {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!';
      locale.decimalFormatSymbols.groupingSeparator = '~';
      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#,##0.00',
        roundingMode: scout.numbers.RoundingMode.CEILING
      });

      expect(decimalFormat.format(0)).toBe('0!00');
      expect(decimalFormat.format(0.000)).toBe('0!00');
      expect(decimalFormat.format(0.111)).toBe('0!12');
      expect(decimalFormat.format(1000.1234)).toBe('1~000!13');
      expect(decimalFormat.format(12345.6789)).toBe('12~345!68');
      expect(decimalFormat.format(-0)).toBe('0!00');
      expect(decimalFormat.format(-0.000)).toBe('0!00');
      expect(decimalFormat.format(-0.111)).toBe('M0!11');
      expect(decimalFormat.format(-1000.1234)).toBe('M1~000!12');
      expect(decimalFormat.format(-12345.6789)).toBe('M12~345!67');

      decimalFormat.allAfter = 0;
      decimalFormat.pattern = '#,##0';
      expect(decimalFormat.format(-1000.1234)).toBe('M1~000');
      expect(decimalFormat.format(1000.1234)).toBe('1~001');
    });

  });

  describe("round", function() {
    it("can handle rounding modes", function() {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!';
      locale.decimalFormatSymbols.groupingSeparator = '~';

      var decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#,##0.00',
        roundingMode: scout.numbers.RoundingMode.CEILING
      });

      expect(decimalFormat.round(0)).toBe(0);
      expect(decimalFormat.round(1000.1234)).toBe(1000.13);
      expect(decimalFormat.round(12345.6789)).toBe(12345.68);
      expect(decimalFormat.round(-1000.1234)).toBe(-1000.12);
      expect(decimalFormat.round(-12345.6789)).toBe(-12345.67);
    });
  });
});
