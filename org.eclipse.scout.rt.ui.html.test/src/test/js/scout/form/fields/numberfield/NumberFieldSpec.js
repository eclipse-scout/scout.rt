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
describe('NumberField', function() {
  var session;
  var helper;
  var locale;
  var localeHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    localeHelper = new scout.LocaleSpecHelper();
    jasmine.Ajax.install();
    jasmine.clock().install();
    locale = localeHelper.createLocale(scout.LocaleSpecHelper.DEFAULT_LOCALE);
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  describe('setValue', function() {
    var field;

    beforeEach(function() {
      field = helper.createField('NumberField');
    });

    it('sets the value and formats it using decimalFormat if the value is valid', function() {
      field.render();
      field.setValue(123.500);
      expect(field.value).toBe(123.5);
      expect(field.displayText).toBe('123.5');
    });

    it('does not set the value if it is invalid', function() {
      field.render();
      field.setValue('123.5'); // not a number
      expect(field.value).toBe(null);
    });

    it('uses another invalidation message than the value field', function() {
      field.render();
      field.setValue('123.5');
      expect(field.errorStatus.message).toBe('[undefined text: InvalidNumberMessageX]');
    });

  });

  describe('setDecimalFormat', function() {
    var field;

    beforeEach(function() {
      field = helper.createField('NumberField');
    });

    it('sets the decimal format', function() {
      expect(field.decimalFormat.pattern).not.toBe('###0.000');

      field.setDecimalFormat(new scout.DecimalFormat(session.locale, '###0.000'));
      expect(field.decimalFormat.pattern).toBe('###0.000');
    });

    it('if the parameter is a string, it is assumed it is the pattern', function() {
      expect(field.decimalFormat.pattern).not.toBe('###0.000');

      field.setDecimalFormat('###0.000');
      expect(field.decimalFormat.pattern).toBe('###0.000');
    });

    it('updates the value and the display text if the format changes', function() {
      field.setDecimalFormat('###0.###');
      field.setValue(123);
      expect(field.value).toBe(123);
      expect(field.displayText).toBe('123');

      field.setDecimalFormat('###0.000');
      expect(field.value).toBe(123);
      expect(field.displayText).toBe('123.000');
    });

  });

  describe('calculates value', function() {
    var field;

    beforeEach(function() {
      field = helper.createField('NumberField');
    });

    it('with . as separator and \' as grouping char', function() {
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

    it('with , as separator and . as grouping char', function() {
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

      //point is stripped and 20+31 is 51
      field.resetValue();
      field.$field.val('2.0+3.1');
      field.acceptInput();
      expect(field.$field[0].value).toBe('51');
    });

    it('of unary expressions', function() {
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

    it('of sum expressions', function() {
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

    it('of product expressions', function() {
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

    it('of complex expressions', function() {
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

  });

});
