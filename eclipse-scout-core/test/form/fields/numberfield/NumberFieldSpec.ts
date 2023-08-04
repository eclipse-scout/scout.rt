/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper, JQueryTesting, LocaleSpecHelper} from '../../../../src/testing/index';
import {DecimalFormat, Locale, NumberField, RoundingMode, scout, Status} from '../../../../src/index';

describe('NumberField', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;
  let locale: Locale;
  let localeHelper: LocaleSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    localeHelper = new LocaleSpecHelper();
    jasmine.Ajax.install();
    jasmine.clock().install();
    locale = localeHelper.createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  describe('setValue', () => {
    let field: NumberField;

    beforeEach(() => {
      field = helper.createField(NumberField);
    });

    it('sets the value and formats it using decimalFormat if the value is valid', () => {
      field.render();
      field.setValue(123.500);
      expect(field.value).toBe(123.5);
      expect(field.displayText).toBe('123.5');
    });

    it('tries to convert the value into a number', () => {
      field.render();
      field.setValue('123.5');
      expect(field.value).toBe(123.5);
      expect(field.displayText).toBe('123.5');
    });

    it('does not set the value if it is invalid', () => {
      field.render();
      field.setValue('asdf');
      expect(field.value).toBe(null);
    });

    it('sets the error status and display text if the value is invalid', () => {
      field.render();
      field.setValue('abc');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof Status).toBe(true);
      expect(field.displayText).toBe('abc');
    });

    it('uses another invalidation message than the value field', () => {
      field.render();
      field.setValue('asdf');
      expect(field.errorStatus.message).toBe('[undefined text: InvalidNumberMessageX]');
    });

  });

  describe('minMaxValue', () => {
    it('is always in order', () => {
      let field = helper.createField(NumberField);
      field.setMinValue(2);
      field.setMaxValue(1);

      expect(field.minValue).toBe(1);
      expect(field.maxValue).toBe(1);

      field.setMaxValue(10);
      field.setMinValue(11);

      expect(field.minValue).toBe(11);
      expect(field.maxValue).toBe(11);
    });

    it('is validated when setting new value', () => {
      let field = helper.createField(NumberField);
      field.setMinValue(1);
      field.setMaxValue(10);

      field.setValue(0);
      expect(field.errorStatus instanceof Status).toBe(true);

      field.setValue(3);
      expect(field.errorStatus).toBe(null);

      field.setValue(13);
      expect(field.errorStatus instanceof Status).toBe(true);
    });

    it('is validated when changing minMaxValue', () => {
      let field = helper.createField(NumberField);
      field.setValue(5);
      expect(field.errorStatus).toBe(null);

      field.setMinValue(6);
      expect(field.errorStatus instanceof Status).toBe(true);

      field.setMinValue(5);
      expect(field.errorStatus).toBe(null);

      field.setMaxValue(4);
      expect(field.errorStatus instanceof Status).toBe(true);

      field.setMaxValue(5);
      expect(field.errorStatus).toBe(null);
    });

    it('works when max or min is null', () => {
      let field = helper.createField(NumberField, helper.session.desktop, {
        minValue: 6,
        maxValue: null
      });
      field.setValue(5);
      expect(field.errorStatus instanceof Status).toBe(true);

      field = helper.createField(NumberField, helper.session.desktop, {
        minValue: null,
        maxValue: 6
      });
      field.setValue(7);
      expect(field.errorStatus instanceof Status).toBe(true);

      field = helper.createField(NumberField);

      field.setValue(5);
      field.setMinValue(6);
      field.setMaxValue(null);
      expect(field.errorStatus instanceof Status).toBe(true);

      field.setMinValue(5);
      field.setMaxValue(null);
      expect(field.errorStatus).toBe(null);

      field.setMaxValue(4);
      field.setMinValue(null);
      expect(field.errorStatus instanceof Status).toBe(true);

      field.setMaxValue(5);
      field.setMinValue(null);
      expect(field.errorStatus).toBe(null);
    });
  });

  describe('acceptInput', () => {
    it('updates the display text after calculation even if the value was not changed', () => {
      let field = helper.createField(NumberField);
      field.render();
      field.$field.val('6');
      field.acceptInput();
      expect(field.displayText).toBe('6');
      expect(field.$field.val()).toBe('6');
      expect(field.value).toBe(6);

      // Enter invalid input
      field.$field.val('3---3');
      field.acceptInput();
      expect(field.displayText).toBe('3---3');
      expect(field.$field.val()).toBe('3---3');
      expect(field.value).toBe(6);
      expect(field.errorStatus instanceof Status).toBe(true);

      // Fix input -> Calculator will change the display text to 6 using parseValue
      field.$field.val('3+3');
      field.acceptInput();
      expect(field.displayText).toBe('6');
      expect(field.$field.val()).toBe('6');
      expect(field.value).toBe(6);
      expect(field.errorStatus).toBe(null);
    });
  });

  describe('setDecimalFormat', () => {
    let field;

    beforeEach(() => {
      field = helper.createField(NumberField);
    });

    it('sets the decimal format', () => {
      expect(field.decimalFormat.pattern).not.toBe('###0.000');

      field.setDecimalFormat(new DecimalFormat(session.locale, '###0.000'));
      expect(field.decimalFormat.pattern).toBe('###0.000');
    });

    it('if the parameter is a string, it is assumed it is the pattern', () => {
      expect(field.decimalFormat.pattern).not.toBe('###0.000');

      field.setDecimalFormat('###0.000');
      expect(field.decimalFormat.pattern).toBe('###0.000');
    });

    it('updates the value and the display text if the format changes', () => {
      field.setDecimalFormat('###0.###');
      field.setValue(123);
      expect(field.value).toBe(123);
      expect(field.displayText).toBe('123');

      field.setDecimalFormat('###0.000');
      expect(field.value).toBe(123);
      expect(field.displayText).toBe('123.000');
    });

    it('updates the value and the display text if the multiplier changes', () => {
      field.setValue(123);
      expect(field.value).toBe(123);
      expect(field.displayText).toBe('123');
      expect(field.decimalFormat.multiplier).toBe(1);

      field.setDecimalFormat({
        pattern: '###0.###',
        multiplier: 100
      });
      expect(field.decimalFormat.multiplier).toBe(100);
      expect(field.value).toBe(123);
      expect(field.displayText).toBe('12300');

      field.setValue(0.01);
      expect(field.value).toBe(0.01);
      expect(field.displayText).toBe('1');

      field.render();
      field.resetValue();
      field.$field.val('111');
      field.acceptInput();
      expect(field.value).toBe(1.11);
      expect(field.displayText).toBe('111');

      field.setDecimalFormat({
        pattern: '###0.###',
        multiplier: 0
      });
      expect(field.decimalFormat.multiplier).toBe(1);
    });

  });

  describe('fractionDigits', () => {
    let field;

    beforeEach(() => {
      field = helper.createField(NumberField);
    });

    it('is applied on setValue', () => {
      field.setValue(123.456789);
      expect(field.value).toBe(123.456789);

      field.setFractionDigits(3);
      field.setValue(123.456789);
      expect(field.value).toBe(123.457);

      field.setFractionDigits(5);
      field.setValue(123.456789);
      expect(field.value).toBe(123.45679);

      field.setFractionDigits(1);
      field.setValue(123.456789);
      expect(field.value).toBe(123.5);
    });

    it('is applied on user input', () => {
      field.render();

      field.$field.val('123.456789');
      field.acceptInput();
      expect(field.value).toBe(123.456789);

      field.setFractionDigits(3);
      field.$field.val('123.456789');
      field.acceptInput();
      expect(field.value).toBe(123.457);

      field.setFractionDigits(5);
      field.$field.val('123.456789');
      field.acceptInput();
      expect(field.value).toBe(123.45679);

      field.setFractionDigits(1);
      field.$field.val('123.456789');
      field.acceptInput();
      expect(field.value).toBe(123.5);
    });

    it('updates the value if it changes', () => {
      field.setValue(123.456789);

      field.setDecimalFormat('###0.#');
      expect(field.value).toBe(123.456789);
      expect(field.displayText).toBe('123.5');

      field.setDecimalFormat('###0.##');
      expect(field.value).toBe(123.456789);
      expect(field.displayText).toBe('123.46');

      field.setFractionDigits(3);
      expect(field.value).toBe(123.457);
      expect(field.displayText).toBe('123.46');

      field.setFractionDigits(1);
      expect(field.value).toBe(123.5);
      expect(field.displayText).toBe('123.5');

      field.setDecimalFormat('###0.000');
      expect(field.value).toBe(123.5);
      expect(field.displayText).toBe('123.500');

      field.setFractionDigits(3);
      expect(field.value).toBe(123.5);
      expect(field.displayText).toBe('123.500');
    });

    it('updates the value if it changes (decimalFormat with multiplier)', () => {
      field.setValue(123.456789);

      field.setDecimalFormat({
        pattern: '###0.#',
        multiplier: 100
      });
      expect(field.value).toBe(123.456789);
      expect(field.displayText).toBe('12345.7');

      field.setFractionDigits(3);
      expect(field.value).toBe(123.457);
      expect(field.displayText).toBe('12345.7');

      field.setFractionDigits(1);
      expect(field.value).toBe(123.5);
      expect(field.displayText).toBe('12350');

      field.setFractionDigits(3);
      expect(field.value).toBe(123.5);
      expect(field.displayText).toBe('12350');
    });

    it('updates the value using the roundingMode of the format', () => {
      field.setFractionDigits(1);
      field.setDecimalFormat('###0.0');
      field.setValue(12.3456789);
      expect(field.value).toBe(12.3);
      expect(field.displayText).toBe('12.3');

      field.setFractionDigits(3);
      expect(field.value).toBe(12.3);
      expect(field.displayText).toBe('12.3');

      field.setValue(12.3456789);
      expect(field.value).toBe(12.346);
      expect(field.displayText).toBe('12.3');

      field.setDecimalFormat({
        pattern: '###0.0',
        roundingMode: RoundingMode.FLOOR
      });
      expect(field.value).toBe(12.346);
      expect(field.displayText).toBe('12.3');

      field.setValue(12.3456789);
      expect(field.value).toBe(12.345);
      expect(field.displayText).toBe('12.3');

      field.setDecimalFormat({
        pattern: '###0.0',
        roundingMode: RoundingMode.CEILING
      });
      expect(field.value).toBe(12.345);
      expect(field.displayText).toBe('12.4');

      field.setValue(12.3456789);
      expect(field.value).toBe(12.346);
      expect(field.displayText).toBe('12.4');
    });
  });

  describe('calculates value', () => {
    let field;

    beforeEach(() => {
      field = helper.createField(NumberField);
    });

    it('with . as separator and \' as grouping char', () => {
      field.render();
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.resetValue();
      field.$field.val('2.0+3.1');
      field.acceptInput();
      expect(field.$field[0].value).toBe('5.1');

      field.resetValue();
      field.$field.val('10\'000*2.0+3.1');
      field.acceptInput();
      expect(field.$field[0].value).toBe('20\'003.1');

      field.resetValue();
      field.$field.val('10.000*2,0+3,1');
      field.acceptInput();
      expect(field.$field[0].value).toBe('10.000*2,0+3,1');
    });

    it('with , as separator and . as grouping char', () => {
      field.render();
      field.decimalFormat.decimalSeparatorChar = ',';
      field.decimalFormat.groupingChar = '.';

      field.resetValue();
      field.$field.val('2,0+3,1');
      field.acceptInput();
      expect(field.$field[0].value).toBe('5,1');

      field.resetValue();
      field.$field.val('10.000*2,0+3,1');
      field.acceptInput();
      expect(field.$field[0].value).toBe('20.003,1');

      // point is stripped and 20+31 is 51
      field.resetValue();
      field.$field.val('2.0+3.1');
      field.acceptInput();
      expect(field.$field[0].value).toBe('51');
    });

    it('of unary expressions', () => {
      field.render();
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.resetValue();
      field.$field.val('123');
      field.acceptInput();
      expect(field.$field[0].value).toBe('123');

      field.resetValue();
      field.$field.val(' 1.23 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('1.23');

      field.resetValue();
      field.$field.val(' 123 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('123');

      field.resetValue();
      field.$field.val('+123');
      field.acceptInput();
      expect(field.$field[0].value).toBe('123');

      field.resetValue();
      field.$field.val('++123');
      field.acceptInput();
      expect(field.$field[0].value).toBe('++123');

      field.resetValue();
      field.$field.val('-123');
      field.acceptInput();
      expect(field.$field[0].value).toBe('-123');

      field.resetValue();
      field.$field.val('--123');
      field.acceptInput();
      expect(field.$field[0].value).toBe('--123');

      field.resetValue();
      field.$field.val('(123)');
      field.acceptInput();
      expect(field.$field[0].value).toBe('123');

      field.resetValue();
      field.$field.val('+(123)');
      field.acceptInput();
      expect(field.$field[0].value).toBe('123');

      field.resetValue();
      field.$field.val('-(123)');
      field.acceptInput();
      expect(field.$field[0].value).toBe('-123');
    });

    it('of sum expressions', () => {
      field.render();
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.resetValue();
      field.$field.val(' 1 + 2 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('3');

      field.resetValue();
      field.$field.val(' -1 - 2 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('-3');

      field.resetValue();
      field.$field.val('1 + 2+  3 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('6');

      field.resetValue();
      field.$field.val('-1 - 2-  3 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('-6');

      field.resetValue();
      field.$field.val('1 + 2+  3+ ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('1 + 2+  3+ ');

      field.resetValue();
      field.$field.val('-1 - 2-  3- ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('-1 - 2-  3- ');

      field.resetValue();
      field.$field.val('1+2-3');
      field.acceptInput();
      expect(field.$field[0].value).toBe('0');
    });

    it('of product expressions', () => {
      field.render();
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.resetValue();
      field.$field.val(' 1 * 2 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('2');

      field.resetValue();
      field.$field.val(' -1 / 2 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('-0.5');

      field.resetValue();
      field.$field.val(' 1 / -2 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('-0.5');

      field.resetValue();
      field.$field.val('1 * 2*  3 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('6');

      field.resetValue();
      field.$field.val('1 / 2/  4 ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('0.125');

      field.resetValue();
      field.$field.val('1 * 2*  3* ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('1 * 2*  3* ');

      field.resetValue();
      field.$field.val('1 / 2/  3/ ');
      field.acceptInput();
      expect(field.$field[0].value).toBe('1 / 2/  3/ ');

      field.resetValue();
      field.$field.val('1*2*3/4');
      field.acceptInput();
      expect(field.$field[0].value).toBe('1.5');
    });

    it('of complex expressions', () => {
      field.render();
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.resetValue();
      field.$field.val('1+2*3+4');
      field.acceptInput();
      expect(field.$field[0].value).toBe('11');

      field.resetValue();
      field.$field.val('1*2+3*4');
      field.acceptInput();
      expect(field.$field[0].value).toBe('14');

      field.resetValue();
      field.$field.val('(1+2)*3+4');
      field.acceptInput();
      expect(field.$field[0].value).toBe('13');

      field.resetValue();
      field.$field.val('(1+2)*(3+4)');
      field.acceptInput();
      expect(field.$field[0].value).toBe('21');

      field.resetValue();
      field.$field.val('(1-2)*(-3+4)');
      field.acceptInput();
      expect(field.$field[0].value).toBe('-1');
    });

    it('of invalid expressions', () => {
      field.render();
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.resetValue();
      field.clearErrorStatus();
      expect(field.value).toBe(null);
      expect(field.errorStatus).toBe(null);
      field.$field.val('1.2.3');
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus).not.toBe(null);

      field.resetValue();
      field.clearErrorStatus();
      expect(field.value).toBe(null);
      expect(field.errorStatus).toBe(null);
      field.$field.val('8+-2'); // valid
      field.acceptInput();
      expect(field.value).toBe(6);
      expect(field.errorStatus).toBe(null);

      field.resetValue();
      field.clearErrorStatus();
      field.$field.val('8+/2'); // invalid
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus).not.toBe(null);

      field.resetValue();
      field.clearErrorStatus();
      field.$field.val('--7');
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus).not.toBe(null);

      field.resetValue();
      field.clearErrorStatus();
      field.$field.val('(6');
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus).not.toBe(null);

      field.resetValue();
      field.clearErrorStatus();
      field.$field.val('2^2'); // not supported
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus).not.toBe(null);

      field.resetValue();
      field.clearErrorStatus();
      field.$field.val('1,5');
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus).not.toBe(null);

      field.resetValue();
      field.clearErrorStatus();
      field.$field.val('1..5');
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus).not.toBe(null);
    });

  });

  describe('label', () => {

    it('focuses the field when clicked', () => {
      let field = scout.create(NumberField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      JQueryTesting.triggerClick(field.$label);
      expect(field.$field).toBeFocused();
    });

  });

  describe('aria properties', () => {

    it('has aria-labelledby set', () => {
      let field = scout.create(NumberField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      expect(field.$field.attr('aria-labelledby')).toBeTruthy();
      expect(field.$field.attr('aria-labelledby')).toBe(field.$label.attr('id'));
      expect(field.$field.attr('aria-label')).toBeFalsy();
    });

    it('has aria-describedby description for its functionality', () => {
      let field = scout.create(NumberField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      let $fieldDescription = field.$container.find('#desc' + field.id + '-func-desc');
      expect(field.$field.attr('aria-describedby')).toBeTruthy();
      expect(field.$field.attr('aria-describedby')).toBe($fieldDescription.eq(0).attr('id'));
      expect(field.$field.attr('aria-description')).toBeFalsy();
    });
  });
});
