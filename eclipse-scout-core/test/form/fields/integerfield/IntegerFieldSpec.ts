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

import {FormSpecHelper} from '../../../../src/testing/index';
import {IntegerField, ParsingFailedStatus, scout, Status, ValidationFailedStatus} from '../../../../src/index';

describe('IntegerField', () => {

  let session: SandboxSession, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  class SpecIntegerField extends IntegerField {
    override _parseValue(displayText: string): number {
      return super._parseValue(displayText);
    }
  }

  it('_parseValue', () => {
    let field = scout.create(SpecIntegerField, {
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
    let field = scout.create(IntegerField, {
      parent: session.desktop
    });
    field.render();
    runTest(field, '123456', 123456, '123\'456');
    runTest(field, '1.23', 1, '1');
    runTest(field, '', null, '');
  });

  it('Test min/max values', () => {
    let field = scout.create(IntegerField, {
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

  function runTest(field: IntegerField, userInput: string, expectedValue: number, expectedDisplayText: string, expectError?: boolean) {
    expectError = scout.nvl(expectError, false);
    field.$field.val(userInput);
    field.acceptInput();
    expect(field.value).toBe(expectedValue);
    expect(field.displayText).toBe(expectedDisplayText);
    if (expectError) {
      expect(field.errorStatus instanceof Status).toBe(true);
    }
  }

  describe('errorStatus', () => {
    it('parse, validate and custom-error', () => {
      let field = scout.create(IntegerField, {
        parent: session.desktop
      });

      // invalid number
      field.parseAndSetValue('foo');
      expect(field.errorStatus.containsStatus(ParsingFailedStatus)).toBe(true);
      expect(field.errorStatus.children.length).toEqual(1);
      field.setErrorStatus(null);

      // add a validator
      let validator = () => {
        throw 'Never valid';
      };
      field.addValidator(validator);
      field.parseAndSetValue('123');
      expect(field.errorStatus.containsStatus(ValidationFailedStatus)).toBe(true);
      expect(field.errorStatus.children.length).toEqual(1);

      // now set a functional error (typically added by business-logic)
      // we use "set" here intentionally in place of "add"
      field.setErrorStatus(Status.error('functional'));
      expect(field.errorStatus.containsStatus(Status)).toBe(true);
      expect(field.errorStatus.children).toBe(null);
      field.removeValidator(validator);

      // now make a parse error
      // the existing (non-multi) error status should be transformed into a multi-status, so we have two status at the same time
      field.parseAndSetValue('foo');
      expect(field.errorStatus.containsStatus(ParsingFailedStatus)).toBe(true);
      expect(field.errorStatus.children.length).toBe(2);
      expect(field.errorStatus.children.some(status => {
        return status.message === 'functional';
      })).toBe(true);

      // when the parse error is resolved, the multi status will remain
      field.parseAndSetValue('123');
      expect(field.errorStatus.containsStatus(ParsingFailedStatus)).toBe(false);
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.message).toEqual('functional');
    });
  });

});
