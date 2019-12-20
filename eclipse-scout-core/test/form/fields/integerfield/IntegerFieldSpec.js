/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {FormSpecHelper} from '@eclipse-scout/testing';
import {IntegerField, scout, Status} from '../../../../src/index';

describe('IntegerField', () => {

  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  it('_parseValue', () => {
    var field = scout.create('IntegerField', {
      parent: session.desktop
    });

    // remove fraction digits
    expect(field._parseValue('123')).toBe(123);
    expect(field._parseValue('12.3')).toBe(12);
    expect(field._parseValue('1.23')).toBe(1);
    expect(field._parseValue('.123')).toBe(0);
    expect(field._parseValue(' 123 ')).toBe(123);

    expect(field._parseValue('1.1')).toBe(1); // round down
    expect(field._parseValue('1.9')).toBe(2); // round up

    // empty-ish
    expect(field._parseValue(null)).toBe(null);
    expect(field._parseValue('')).toBe(null);
    expect(field._parseValue(' ')).toBe(null);

    // invalid numbers
    expect(() => field._parseValue('1.2.3')).toThrow();
  });

  it('Test user input', () => {
    var field = scout.create('IntegerField', {
      parent: session.desktop
    });
    field.render();
    runTest(field, '123456', 123456, '123\'456');
    runTest(field, '1.23', 1, '1');
    runTest(field, '', null, '');
  });

  it('Test min/max values', () => {
    var field = scout.create('IntegerField', {
      parent: session.desktop,
      minValue: 0,
      maxValue: 99
    });
    field.render();
    runTest(field, '1', 1, '1');
    runTest(field, '12', 12, '12');
    runTest(field, '123', 12, '123', true);
    runTest(field, '1234', 12, '1\'234', true);
  });

  function runTest(field, userInput, expectedValue, expectedDisplayText, expectError) {
    expectError = scout.nvl(expectError, false);
    field.$field.val(userInput);
    field.acceptInput();
    expect(field.value).toBe(expectedValue);
    expect(field.displayText).toBe(expectedDisplayText);
    if (expectError) {
      expect(field.errorStatus instanceof Status).toBe(true);
    }
  }

});
