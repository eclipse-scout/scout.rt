/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {FormSpecHelper} from '../../../../src/testing/index';
import {IntegerField, ParsingFailedStatus, RoundingMode, scout, Status, ValidationFailedStatus} from '../../../../src/index';

describe('IntegerField', () => {

  let session: SandboxSession, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  it('parseAndSetValue', () => {
    let field = scout.create(IntegerField, {
      parent: session.desktop
    });

    // remove fraction digits
    field.parseAndSetValue('123');
    expect(field.value).toBe(123);
    field.parseAndSetValue('12.3');
    expect(field.value).toBe(12);
    field.parseAndSetValue('1.23');
    expect(field.value).toBe(1);
    field.parseAndSetValue('.123');
    expect(field.value).toBe(0);
    field.parseAndSetValue(' 123 ');
    expect(field.value).toBe(123);

    field.parseAndSetValue('1.1');
    expect(field.value).toBe(1); // round down
    field.parseAndSetValue('1.9');
    expect(field.value).toBe(2); // round up

    // empty-ish
    field.parseAndSetValue(null);
    expect(field.value).toBe(null);
    field.parseAndSetValue('');
    expect(field.value).toBe(null);
    field.parseAndSetValue(' ');
    expect(field.value).toBe(null);

    // invalid numbers
    expect(field.errorStatus).toBeNull();
    field.parseAndSetValue('1.2.3');
    expect(field.errorStatus).not.toBeNull();
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

  describe('fractionDigits', () => {
    let field;

    beforeEach(() => {
      field = helper.createField(IntegerField);
    });

    it('is always 0', () => {
      expect(field.fractionDigits).toBe(0);

      field.setFractionDigits(42);
      expect(field.fractionDigits).toBe(0);

      field.setFractionDigits(3);
      expect(field.fractionDigits).toBe(0);

      field.setFractionDigits(null);
      expect(field.fractionDigits).toBe(0);

      field.setFractionDigits(undefined);
      expect(field.fractionDigits).toBe(0);

      field = helper.createField(IntegerField, null, {fractionDigits: 42});
      expect(field.fractionDigits).toBe(0);
    });

    it('updates the value using the roundingMode of the format', () => {
      field.setDecimalFormat('###0.0');
      field.setValue(12.3456789);
      expect(field.value).toBe(12);
      expect(field.displayText).toBe('12.0');

      field.setDecimalFormat({
        pattern: '###0.0',
        roundingMode: RoundingMode.FLOOR
      });
      field.setValue(12.3456789);
      expect(field.value).toBe(12);
      expect(field.displayText).toBe('12.0');

      field.setDecimalFormat({
        pattern: '###0.0',
        roundingMode: RoundingMode.CEILING
      });
      field.setValue(12.3456789);
      expect(field.value).toBe(13);
      expect(field.displayText).toBe('13.0');
    });
  });

  describe('aria properties', () => {

    it('has aria-describedby description for its functionality', () => {
      let field = helper.createField(IntegerField);
      field.render();
      let $fieldDescription = field.$container.find('#desc' + field.id + '-func-desc');
      expect(field.$field.attr('aria-describedby')).toBeTruthy();
      expect(field.$field.attr('aria-describedby')).toBe($fieldDescription.eq(0).attr('id'));
      expect(field.$field.attr('aria-description')).toBeFalsy();
    });
  });
});
