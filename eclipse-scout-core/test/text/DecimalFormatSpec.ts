/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DecimalFormat, Locale, RoundingMode} from '../../src/index';
import {LocaleSpecHelper} from '../../src/testing/index';

describe('DecimalFormat', () => {
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

  describe('constructor', () => {
    it('uses the pattern from the options', () => {
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '###0.00'
      });
      expect(decimalFormat.pattern).toBe('###0.00');
    });

    it('if the options parameter is a string, it assumes it is the pattern', () => {
      // mainly done to be consistent with the DateFormat.js
      let decimalFormat = new DecimalFormat(locale, '###0.00');
      expect(decimalFormat.pattern).toBe('###0.00');
    });
  });

  describe('format', () => {

    it('considers decimal separators', () => {
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '###0.00'
      });

      expect(decimalFormat.format(null)).toBe(null);
      expect(decimalFormat.format(0)).toBe('0.00');
      expect(decimalFormat.format(0.000)).toBe('0.00');
      expect(decimalFormat.format(1000.1234)).toBe('1000.12');
      expect(decimalFormat.format(1000.1234)).toBe('1000.12');
      expect(decimalFormat.format(56000.1234)).toBe('56000.12');

      // Without digits before decimal point
      decimalFormat = new DecimalFormat(locale, {
        pattern: '.00'
      });

      expect(decimalFormat.format(0)).toBe('.00');
      expect(decimalFormat.format(0.000)).toBe('.00');
      expect(decimalFormat.format(1000.1234)).toBe('1000.12');
      expect(decimalFormat.format(12345.6789)).toBe('12345.68'); // rounding

      locale = helper.createLocale('de-DE');
      decimalFormat = new DecimalFormat(locale, {
        pattern: '###0.00'
      });

      expect(decimalFormat.format(0)).toBe('0,00');
      expect(decimalFormat.format(0.000)).toBe('0,00');
      expect(decimalFormat.format(1000.1234)).toBe('1000,12');
    });

    it('considers grouping separators', () => {
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '#,##0.00'
      });

      expect(decimalFormat.format(0)).toBe('0.00');
      expect(decimalFormat.format(10)).toBe('10.00');
      expect(decimalFormat.format(100)).toBe('100.00');
      expect(decimalFormat.format(1000.1234)).toBe('1\'000.12');
      expect(decimalFormat.format(50121000.1234)).toBe('50\'121\'000.12');
      expect(decimalFormat.format(100005121000.1234)).toBe('100\'005\'121\'000.12');

      locale = helper.createLocale('de-DE');
      decimalFormat = new DecimalFormat(locale, {
        pattern: '#,##0.00'
      });

      expect(decimalFormat.format(0)).toBe('0,00');
      expect(decimalFormat.format(10)).toBe('10,00');
      expect(decimalFormat.format(100)).toBe('100,00');
      expect(decimalFormat.format(1000.1234)).toBe('1.000,12');
      expect(decimalFormat.format(50121000.1234)).toBe('50.121.000,12');
      expect(decimalFormat.format(100005121000.1234)).toBe('100.005.121.000,12');
    });

    it('is lenient with grouping separators', () => {
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '#,##0.00'
      });

      expect(decimalFormat.parse('123’123’123')).toBe(123123123);
      expect(decimalFormat.parse('123\'123\'123')).toBe(123123123);
      expect(decimalFormat.parse('123´123´123')).toBe(123123123);
      expect(decimalFormat.parse('123\u202F123\u202F123')).toBe(123123123);
      let func = decimalFormat.parse.bind(decimalFormat, '123~123~123');
      expect(func).toThrowError();
    });

    it('can swap the position of the minus sign', () => {
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '0.0-' // Group separator after decimal separator, 0 after #
      });

      expect(decimalFormat.format(0)).toBe('0.0');
      expect(decimalFormat.format(10)).toBe('10.0');
      expect(decimalFormat.format(-14.234)).toBe('14.2-');
    });

    it('can handle invalid patterns', () => {
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '#.##0,00' // Group separator after decimal separator, 0 after #
      });

      expect(decimalFormat.format(0)).toBe('.000');
      expect(decimalFormat.format(10)).toBe('10.000');
      expect(decimalFormat.format(50121000.1234)).toBe('50121000.1234');
      expect(decimalFormat.format(50121000.1234567)).toBe('50121000.12346');
    });

    it('distinguishes digits and zero digits', () => {
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '##0.#'
      });

      expect(decimalFormat.format(0)).toBe('0');
      expect(decimalFormat.format(112)).toBe('112');

      decimalFormat = new DecimalFormat(locale, {
        pattern: '#.#'
      });

      expect(decimalFormat.format(0)).toBe('');
      expect(decimalFormat.format(112)).toBe('112');

      decimalFormat = new DecimalFormat(locale, {
        pattern: '0000.0000'
      });

      expect(decimalFormat.format(1)).toBe('0001.0000');
      expect(decimalFormat.format(125112)).toBe('125112.0000');

      // patterns without separator:
      decimalFormat = new DecimalFormat(locale, {
        pattern: '#'
      });

      expect(decimalFormat.format(0)).toBe('');
      expect(decimalFormat.format(112)).toBe('112');

      decimalFormat = new DecimalFormat(locale, {
        pattern: '0'
      });

      expect(decimalFormat.format(0)).toBe('0');
      expect(decimalFormat.format(112)).toBe('112');
    });

    it('can handle positive and negative subpattern', () => {
      let decimalFormat = new DecimalFormat(locale, {
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
      decimalFormat = new DecimalFormat(locale, {
        pattern: '-0.00;0.00'
      });
      expect(decimalFormat.format(12)).toBe('-12.00');
      expect(decimalFormat.format(-924.566)).toBe('924.57');

      // Normal mode, auto-insertion of minus sign
      decimalFormat = new DecimalFormat(locale, {
        pattern: '0.00'
      });
      expect(decimalFormat.format(12)).toBe('12.00');
      expect(decimalFormat.format(-924.566)).toBe('-924.57');

      // Prefix and minus sign position in front
      decimalFormat = new DecimalFormat(locale, {
        pattern: '-X0.00'
      });
      expect(decimalFormat.format(12)).toBe('X12.00');
      expect(decimalFormat.format(-924.566)).toBe('-X924.57');

      decimalFormat = new DecimalFormat(locale, {
        pattern: 'X-0.00'
      });
      expect(decimalFormat.format(12)).toBe('X12.00');
      expect(decimalFormat.format(-924.566)).toBe('X-924.57');

      // Minus sign position at end
      decimalFormat = new DecimalFormat(locale, {
        pattern: '0.00-'
      });
      expect(decimalFormat.format(12)).toBe('12.00');
      expect(decimalFormat.format(-924.566)).toBe('924.57-');
    });

    it('can handle exotic symbols', () => {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!!';
      locale.decimalFormatSymbols.groupingSeparator = '~';
      let decimalFormat = new DecimalFormat(locale, {
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

    it('can handle percentages, format taken from application', () => {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!';
      locale.decimalFormatSymbols.groupingSeparator = '~';
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '#,##0.00\'%\''
      });

      expect(decimalFormat.format(0)).toBe('0!00%');
      expect(decimalFormat.format(0.000)).toBe('0!00%');
      expect(decimalFormat.format(0.111)).toBe('0!11%');
      expect(decimalFormat.format(1000.1234)).toBe('1~000!12%');
      expect(decimalFormat.format(12345.6789)).toBe('12~345!68%');

      expect(decimalFormat.format(-1)).toBe('M1!00%');
    });

    it('can handle multiplier', () => {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!';
      locale.decimalFormatSymbols.groupingSeparator = '~';
      let decimalFormat = new DecimalFormat(locale, {
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

      // rounding
      decimalFormat = new DecimalFormat(locale, {
        pattern: '##0',
        multiplier: 100
      });
      expect(decimalFormat.round(0)).toBe(0);
      expect(decimalFormat.round(0.000)).toBe(0);
      expect(decimalFormat.round(0.1234)).toBe(0.12);
      expect(decimalFormat.round(0.1298)).toBe(0.13);
      expect(decimalFormat.round(1.0002)).toBe(1);
      expect(decimalFormat.round(1000.1234)).toBe(1000.12);
      expect(decimalFormat.round(12345.6789)).toBe(12345.68);
      expect(decimalFormat.round(-1)).toBe(-1);
      expect(decimalFormat.round(-1.002)).toBe(-1);
      expect(decimalFormat.round(-1.008)).toBe(-1.01);
    });

    it('can handle rounding mode', () => {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!';
      locale.decimalFormatSymbols.groupingSeparator = '~';
      let decimalFormat = new DecimalFormat(locale, {
        pattern: '#,##0.00',
        roundingMode: RoundingMode.CEILING
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

  describe('round', () => {
    it('can handle rounding modes', () => {
      locale.decimalFormatSymbols.minusSign = 'M';
      locale.decimalFormatSymbols.decimalSeparator = '!';
      locale.decimalFormatSymbols.groupingSeparator = '~';

      let decimalFormat = new DecimalFormat(locale, {
        pattern: '#,##0.00',
        roundingMode: RoundingMode.CEILING
      });

      expect(decimalFormat.round(0)).toBe(0);
      expect(decimalFormat.round(1000.1234)).toBe(1000.13);
      expect(decimalFormat.round(12345.6789)).toBe(12345.68);
      expect(decimalFormat.round(-1000.1234)).toBe(-1000.12);
      expect(decimalFormat.round(-12345.6789)).toBe(-12345.67);
    });
  });
});
