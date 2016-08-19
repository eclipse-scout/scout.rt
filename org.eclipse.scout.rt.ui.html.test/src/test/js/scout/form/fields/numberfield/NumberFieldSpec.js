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
describe("NumberField", function() {
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

  describe("parse input (calculate value)", function() {
    var field;

    beforeEach(function() {
      field = helper.createField('NumberField');
    });

    it("with . as separator and ' as grouping char", function() {
      field.render(session.$entryPoint);
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.$field.val('2.0+3.1');
      field._parse();
      expect(field.$field[0].value).toBe('5.1');

      field.$field.val('10\'000*2.0+3.1');
      field._parse();
      expect(field.$field[0].value).toBe('20\'003.1');

      field.$field.val('10.000*2,0+3,1');
      field._parse();
      expect(field.$field[0].value).toBe('10.000*2,0+3,1');
    });

    it("with , as separator and . as grouping char", function() {
      field.render(session.$entryPoint);
      field.decimalFormat.decimalSeparatorChar = ',';
      field.decimalFormat.groupingChar = '.';

      field.$field.val('2,0+3,1');
      field._parse();
      expect(field.$field[0].value).toBe('5,1');

      field.$field.val('10.000*2,0+3,1');
      field._parse();
      expect(field.$field[0].value).toBe('20.003,1');

      //point is stripped and 20+31 is 51
      field.$field.val('2.0+3.1');
      field._parse();
      expect(field.$field[0].value).toBe('51');
    });

    it("of unary expressions", function() {
      field.render(session.$entryPoint);
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.$field.val('123');
      field._parse();
      expect(field.$field[0].value).toBe('123');

      field.$field.val(' 1.23 ');
      field._parse();
      expect(field.$field[0].value).toBe('1.23');

      field.$field.val(' 123 ');
      field._parse();
      expect(field.$field[0].value).toBe('123');

      field.$field.val('+123');
      field._parse();
      expect(field.$field[0].value).toBe('123');

      field.$field.val('++123');
      field._parse();
      expect(field.$field[0].value).toBe('++123');

      field.$field.val('-123');
      field._parse();
      expect(field.$field[0].value).toBe('-123');

      field.$field.val('--123');
      field._parse();
      expect(field.$field[0].value).toBe('--123');

      field.$field.val('(123)');
      field._parse();
      expect(field.$field[0].value).toBe('123');

      field.$field.val('+(123)');
      field._parse();
      expect(field.$field[0].value).toBe('123');

      field.$field.val('-(123)');
      field._parse();
      expect(field.$field[0].value).toBe('-123');
    });

    it("of sum expressions", function() {
      field.render(session.$entryPoint);
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.$field.val(' 1 + 2 ');
      field._parse();
      expect(field.$field[0].value).toBe('3');

      field.$field.val(' -1 - 2 ');
      field._parse();
      expect(field.$field[0].value).toBe('-3');

      field.$field.val('1 + 2+  3 ');
      field._parse();
      expect(field.$field[0].value).toBe('6');

      field.$field.val('-1 - 2-  3 ');
      field._parse();
      expect(field.$field[0].value).toBe('-6');

      field.$field.val('1 + 2+  3+ ');
      field._parse();
      expect(field.$field[0].value).toBe('1 + 2+  3+ ');

      field.$field.val('-1 - 2-  3- ');
      field._parse();
      expect(field.$field[0].value).toBe('-1 - 2-  3- ');

      field.$field.val('1+2-3');
      field._parse();
      expect(field.$field[0].value).toBe('0');
    });

    it("of product expressions", function() {
      field.render(session.$entryPoint);
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.$field.val(' 1 * 2 ');
      field._parse();
      expect(field.$field[0].value).toBe('2');

      field.$field.val(' -1 / 2 ');
      field._parse();
      expect(field.$field[0].value).toBe('-0.5');

      field.$field.val(' 1 / -2 ');
      field._parse();
      expect(field.$field[0].value).toBe('-0.5');

      field.$field.val('1 * 2*  3 ');
      field._parse();
      expect(field.$field[0].value).toBe('6');

      field.$field.val('1 / 2/  4 ');
      field._parse();
      expect(field.$field[0].value).toBe('0.125');

      field.$field.val('1 * 2*  3* ');
      field._parse();
      expect(field.$field[0].value).toBe('1 * 2*  3* ');

      field.$field.val('1 / 2/  3/ ');
      field._parse();
      expect(field.$field[0].value).toBe('1 / 2/  3/ ');

      field.$field.val('1*2*3/4');
      field._parse();
      expect(field.$field[0].value).toBe('1.5');
    });

    it("of complex expressions", function() {
      field.render(session.$entryPoint);
      field.decimalFormat.decimalSeparatorChar = '.';
      field.decimalFormat.groupingChar = '\'';

      field.$field.val('1+2*3+4');
      field._parse();
      expect(field.$field[0].value).toBe('11');

      field.$field.val('1*2+3*4');
      field._parse();
      expect(field.$field[0].value).toBe('14');

      field.$field.val('(1+2)*3+4');
      field._parse();
      expect(field.$field[0].value).toBe('13');

      field.$field.val('(1+2)*(3+4)');
      field._parse();
      expect(field.$field[0].value).toBe('21');

      field.$field.val('(1-2)*(-3+4)');
      field._parse();
      expect(field.$field[0].value).toBe('-1');
    });

  });

});
