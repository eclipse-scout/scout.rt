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
describe('NumberFieldAdapter', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  describe('parse', function() {
    var field;

    beforeEach(function() {
      field = helper.createField('NumberField');
      linkWidgetAndAdapter(field, 'NumberFieldAdapter');
    });

    it('prevents setting the error status', function() {
      // The parsing might fail on JS side, but the server might handle it -> don't show the error status, let the server do it
      // Example: property percentage is not known by the DecimalFormat.js yet, parse would fail.
      // But even if that will be implemented some time the developer may still override execParseValue and implement a custom parse logic in Java
      field.render();
      field.$field.val('asdf');
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('asdf');
      expect(field.errorStatus).toBe(null);
    });

  });

  it('supports the calculator', function() {
    // Check if the calculator still works if a model adapter is attached
    var field = helper.createField('NumberField');
    linkWidgetAndAdapter(field, 'NumberFieldAdapter');
    field.render();
    field.decimalFormat.decimalSeparatorChar = '.';
    field.decimalFormat.groupingChar = '\'';

    field.$field.val('2.0+3.1');
    field.acceptInput();
    expect(field.$field[0].value).toBe('5.1');
  });

});
