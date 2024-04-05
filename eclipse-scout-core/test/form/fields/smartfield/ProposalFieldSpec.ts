/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupRow, ProposalField, QueryBy, scout, SmartFieldTouchPopup, StaticLookupCall, Status} from '../../../../src/index';
import {proposalFieldSpecHelper, ProposalFieldSpecHelperInput, SpecProposalField} from '../../../../src/testing';

describe('ProposalField', () => {

  let session: SandboxSession, field: SpecProposalField, lookupRow: LookupRow<string>;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = scout.create(SpecProposalField, {
      parent: session.desktop,
      lookupCall: 'DummyLookupCall'
    });
    lookupRow = scout.create((LookupRow<string>), {
      key: '123',
      text: 'Foo'
    });
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    removePopups(session);
    removePopups(session, '.touch-popup');
  });

  describe('proposal field', () => {

    it('defaults', () => {
      expect(field.maxLength).toBe(4000);
      expect(field.trimText).toBe(true);
    });

    /**
     * Proposal field acts as regular string field when setValue is called
     * No matter if the typed text exists as record in the lookup call, we
     * simply set value/display text to it.
     */
    it('setValue', () => {
      field.setValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('Foo');

      field.setValue('Xyz');
      expect(field.value).toBe('Xyz');
      expect(field.displayText).toBe('Xyz');
      expect(field.errorStatus).toBe(null);
    });

    it('is empty when text is empty', () => {
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(field.empty).toBe(true);

      field.setValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('Foo');
      expect(field.empty).toBe(false);

      field.setValue('');
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(field.empty).toBe(true);

      field.setValue('Xyz');
      expect(field.value).toBe('Xyz');
      expect(field.displayText).toBe('Xyz');
      expect(field.empty).toBe(false);

      field.setValue(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(field.empty).toBe(true);
    });

    it('setLookupRow should set value too', () => {
      let lookupRow = scout.create((LookupRow<string>), {
        key: '123',
        text: 'Foo'
      });
      field.setLookupRow(lookupRow);
      expect(field.value).toBe('Foo');
      expect(field.lookupRow).toBe(lookupRow);

      field.setLookupRow(null);
      expect(field.value).toBe(null);
      expect(field.lookupRow).toBe(null);
    });

    it('should set error status when result has an exception', () => {
      field._lookupByTextOrAllDone({
        lookupRows: [],
        queryBy: QueryBy.ALL,
        exception: 'proposal lookup failed'
      });
      expect(field.errorStatus.severity).toBe(Status.Severity.ERROR);
      expect(field.errorStatus.message).toBe('proposal lookup failed');
    });
  });

  /**
   * When the lookupOnAcceptByText flag is set, make sure that when clear()
   * _customTextAccepted is called and not _acceptByText. _customTextAccepted
   * will trigger the acceptInput event which is also sent to the Scout server.
   * # 221199
   */
  it('lookupOnAcceptByText', () => {
    field.render();
    field.lookupOnAcceptByText = true;
    field.setValue('Foo');

    let acceptInputCalled = false;
    field.on('acceptInput', () => {
      acceptInputCalled = true;
    });
    field.clear();

    expect(acceptInputCalled).toBe(true);
  });

  /**
   * When aboutToBlurByMouseDown is called, the value of the proposal field
   * should not be deleted, regardless of the value of
   * this.lookupOnAcceptByText.
   * # 2345061
   */
  it('when lookupOnAcceptByText=true the value is not deleted when aboutToBlurByMouseDown is called', () => {
    field.render();
    field.lookupOnAcceptByText = true;

    field.$field.focus();
    field.$field.val('Foo');
    field._userWasTyping = true;
    field.aboutToBlurByMouseDown(undefined);
    jasmine.clock().tick(300);
    expect(field.displayText).toBe('Foo');
    expect(field.$field.val()).toBe('Foo');
  });

  it('should return value for last search-text', () => {
    field.setValue('Bar');
    expect(field._getLastSearchText()).toBe('Bar');
  });

  it('should clear error status on search text change', () => {
    field.setErrorStatus(Status.error('functional'));
    expect(field.errorStatus.containsStatus(Status)).toBe(true);
    field._acceptInput(false, 'Bar', false, true, null);
    expect(field['getErrorStatus']).toBeUndefined();
  });

  describe('displayText, value, errorStatus and lookupRow are always in a consistent state', () => {

    beforeEach(() => {
      jasmine.clock().uninstall();
      field = scout.create(SpecProposalField, {
        parent: session.desktop,
        lookupCall: {
          objectType: StaticLookupCall,
          data: [
            ['ok', 'ok'],
            ['warning', 'warning'],
            ['throw', 'throw'],
            ['no error', 'no error']
          ]
        }
      });
      field.addValidator(value => {
        field.clearErrorStatus();

        if ('ok' === value) {
          field.setErrorStatus(Status.ok({message: 'This has severity OK.'}));
          return value;
        }

        if ('warning' === value) {
          field.setErrorStatus(Status.warning({message: 'This has severity WARNING.'}));
          return value;
        }

        if ('throw' === value) {
          throw 'This is an exception.';
        }

        return value;
      });
    });

    // foo > ok > foo > ok

    it('write \'foo\' > write \'ok\' > write \'foo\' > write \'ok\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['foo', 'ok', 'foo', 'ok'],
        false);
    });

    it('write \'foo\' > write \'ok\' > write \'foo\' > write \'ok\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['foo', 'ok', 'foo', 'ok'],
        true);
    });

    it('write \'foo\' > lookup \'ok\' > write \'foo\' > lookup \'ok\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['foo', {text: 'ok', lookup: true}, 'foo', {text: 'ok', lookup: true}],
        false);
    });

    it('write \'foo\' > lookup \'ok\' > write \'foo\' > lookup \'ok\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['foo', {text: 'ok', lookup: true}, 'foo', {text: 'ok', lookup: true}],
        true);
    });

    // no error > ok > no error > ok

    it('write \'no error\' > lookup \'ok\' > write \'no error\' > lookup \'ok\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['no error', {text: 'ok', lookup: true}, 'no error', {text: 'ok', lookup: true}],
        false);
    });

    it('write \'no error\' > lookup \'ok\' > write \'no error\' > lookup \'ok\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['no error', {text: 'ok', lookup: true}, 'no error', {text: 'ok', lookup: true}],
        true);
    });

    it('lookup \'no error\' > lookup \'ok\' > lookup \'no error\' > lookup \'ok\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'no error', lookup: true}, {text: 'ok', lookup: true}, {text: 'no error', lookup: true}, {text: 'ok', lookup: true}],
        false);
    });

    it('lookup \'no error\' > lookup \'ok\' > lookup \'no error\' > lookup \'ok\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'no error', lookup: true}, {text: 'ok', lookup: true}, {text: 'no error', lookup: true}, {text: 'ok', lookup: true}],
        true);
    });

    // foo > throw > foo > throw

    it('write \'foo\' > write \'throw\' > write \'foo\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['foo', 'throw', 'foo', 'throw'],
        false);
    });

    it('write \'foo\' > write \'throw\' > write \'foo\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['foo', 'throw', 'foo', 'throw'],
        true);
    });

    it('write \'foo\' > lookup \'throw\' > write \'foo\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['foo', {text: 'throw', lookup: true}, 'foo', {text: 'throw', lookup: true}],
        false);
    });

    it('write \'foo\' > lookup \'throw\' > write \'foo\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['foo', {text: 'throw', lookup: true}, 'foo', {text: 'throw', lookup: true}],
        true);
    });

    // no error > throw > no error > throw

    it('write \'no error\' > lookup \'throw\' > write \'no error\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['no error', {text: 'throw', lookup: true}, 'no error', {text: 'throw', lookup: true}],
        false);
    });

    it('write \'no error\' > lookup \'throw\' > write \'no error\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['no error', {text: 'throw', lookup: true}, 'no error', {text: 'throw', lookup: true}],
        true);
    });

    it('lookup \'no error\' > lookup \'throw\' > lookup \'no error\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'no error', lookup: true}, {text: 'throw', lookup: true}, {text: 'no error', lookup: true}, {text: 'throw', lookup: true}],
        false);
    });

    it('lookup \'no error\' > lookup \'throw\' > lookup \'no error\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'no error', lookup: true}, {text: 'throw', lookup: true}, {text: 'no error', lookup: true}, {text: 'throw', lookup: true}],
        true);
    });

    // ok > throw > ok > throw

    it('write \'ok\' > write \'throw\' > write \'ok\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', 'ok', 'throw'],
        false);
    });

    it('write \'ok\' > write \'throw\' > write \'ok\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', 'ok', 'throw'],
        true);
    });

    it('write \'ok\' > write \'throw\' > write \'ok\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', 'ok', {text: 'throw', lookup: true}],
        false);
    });

    it('write \'ok\' > write \'throw\' > write \'ok\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', 'ok', {text: 'throw', lookup: true}],
        true);
    });

    it('write \'ok\' > write \'throw\' > lookup \'ok\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', {text: 'ok', lookup: true}, 'throw'],
        false);
    });

    it('write \'ok\' > write \'throw\' > lookup \'ok\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', {text: 'ok', lookup: true}, 'throw'],
        true);
    });

    it('write \'ok\' > write \'throw\' > lookup \'ok\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', {text: 'ok', lookup: true}, {text: 'throw', lookup: true}],
        false);
    });

    it('write \'ok\' > write \'throw\' > lookup \'ok\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', {text: 'ok', lookup: true}, {text: 'throw', lookup: true}],
        true);
    });

    it('write \'ok\' > lookup \'throw\' > write \'ok\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, 'ok', 'throw'],
        false);
    });

    it('write \'ok\' > lookup \'throw\' > write \'ok\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, 'ok', 'throw'],
        true);
    });

    it('write \'ok\' > lookup \'throw\' > write \'ok\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, 'ok', {text: 'throw', lookup: true}],
        false);
    });

    it('write \'ok\' > lookup \'throw\' > write \'ok\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, 'ok', {text: 'throw', lookup: true}],
        true);
    });

    it('write \'ok\' > lookup \'throw\' > lookup \'ok\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, {text: 'ok', lookup: true}, 'throw'],
        false);
    });

    it('write \'ok\' > lookup \'throw\' > lookup \'ok\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, {text: 'ok', lookup: true}, 'throw'],
        true);
    });

    it('write \'ok\' > lookup \'throw\' > lookup \'ok\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, {text: 'ok', lookup: true}, {text: 'throw', lookup: true}],
        false);
    });

    it('write \'ok\' > lookup \'throw\' > lookup \'ok\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, {text: 'ok', lookup: true}, {text: 'throw', lookup: true}],
        true);
    });

    it('lookup \'ok\' > write \'throw\' > write \'ok\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', 'ok', 'throw'],
        false);
    });

    it('lookup \'ok\' > write \'throw\' > write \'ok\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', 'ok', 'throw'],
        true);
    });

    it('lookup \'ok\' > write \'throw\' > write \'ok\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', 'ok', {text: 'throw', lookup: true}],
        false);
    });

    it('lookup \'ok\' > write \'throw\' > write \'ok\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', 'ok', {text: 'throw', lookup: true}],
        true);
    });

    it('lookup \'ok\' > write \'throw\' > lookup \'ok\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', {text: 'ok', lookup: true}, 'throw'],
        false);
    });

    it('lookup \'ok\' > write \'throw\' > lookup \'ok\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', {text: 'ok', lookup: true}, 'throw'],
        true);
    });

    it('lookup \'ok\' > write \'throw\' > lookup \'ok\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', {text: 'ok', lookup: true}, {text: 'throw', lookup: true}],
        false);
    });

    it('lookup \'ok\' > write \'throw\' > lookup \'ok\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', {text: 'ok', lookup: true}, {text: 'throw', lookup: true}],
        true);
    });

    it('lookup \'ok\' > lookup \'throw\' > write \'ok\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, 'ok', 'throw'],
        false);
    });

    it('lookup \'ok\' > lookup \'throw\' > write \'ok\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, 'ok', 'throw'],
        true);
    });

    it('lookup \'ok\' > lookup \'throw\' > write \'ok\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, 'ok', {text: 'throw', lookup: true}],
        false);
    });

    it('lookup \'ok\' > lookup \'throw\' > write \'ok\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, 'ok', {text: 'throw', lookup: true}],
        true);
    });

    it('lookup \'ok\' > lookup \'throw\' > lookup \'ok\' > write \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, {text: 'ok', lookup: true}, 'throw'],
        false);
    });

    it('lookup \'ok\' > lookup \'throw\' > lookup \'ok\' > write \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, {text: 'ok', lookup: true}, 'throw'],
        true);
    });

    it('lookup \'ok\' > lookup \'throw\' > lookup \'ok\' > lookup \'throw\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, {text: 'ok', lookup: true}, {text: 'throw', lookup: true}],
        false);
    });

    it('lookup \'ok\' > lookup \'throw\' > lookup \'ok\' > lookup \'throw\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, {text: 'ok', lookup: true}, {text: 'throw', lookup: true}],
        true);
    });

    // ok > throw > warning

    it('write \'ok\' > write \'throw\' > write \'warning\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', 'warning'],
        false);
    });

    it('write \'ok\' > write \'throw\' > write \'warning\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', 'warning'],
        true);
    });

    it('write \'ok\' > write \'throw\' > lookup \'warning\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', {text: 'warning', lookup: true}],
        false);
    });

    it('write \'ok\' > write \'throw\' > lookup \'warning\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', 'throw', {text: 'warning', lookup: true}],
        true);
    });

    it('write \'ok\' > lookup \'throw\' > write \'warning\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, 'warning'],
        false);
    });

    it('write \'ok\' > lookup \'throw\' > write \'warning\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, 'warning'],
        true);
    });

    it('write \'ok\' > lookup \'throw\' > lookup \'warning\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, {text: 'warning', lookup: true}],
        false);
    });

    it('write \'ok\' > lookup \'throw\' > lookup \'warning\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        ['ok', {text: 'throw', lookup: true}, {text: 'warning', lookup: true}],
        true);
    });

    it('lookup \'ok\' > write \'throw\' > write \'warning\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', 'warning'],
        false);
    });

    it('lookup \'ok\' > write \'throw\' > write \'warning\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', 'warning'],
        true);
    });

    it('lookup \'ok\' > write \'throw\' > lookup \'warning\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', {text: 'warning', lookup: true}],
        false);
    });

    it('lookup \'ok\' > write \'throw\' > lookup \'warning\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, 'throw', {text: 'warning', lookup: true}],
        true);
    });

    it('lookup \'ok\' > lookup \'throw\' > write \'warning\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, 'warning'],
        false);
    });

    it('lookup \'ok\' > lookup \'throw\' > write \'warning\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, 'warning'],
        true);
    });

    it('lookup \'ok\' > lookup \'throw\' > lookup \'warning\' (touchMode: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, {text: 'warning', lookup: true}],
        false);
    });

    it('lookup \'ok\' > lookup \'throw\' > lookup \'warning\' (touchMode: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'ok', lookup: true}, {text: 'throw', lookup: true}, {text: 'warning', lookup: true}],
        true);
    });

    async function testProposalFieldInputs(inputs: (ProposalFieldSpecHelperInput | string)[], touchMode?: boolean) {
      await proposalFieldSpecHelper.testProposalFieldInputs(field, inputs, touchMode, {
        afterInput: (input: ProposalFieldSpecHelperInput) => {
          const {text, lookup} = input;

          // displayText always equals text
          expect(field.displayText).toBe(text);

          // value equals text iff there is no validation error (see validator)
          if ('throw' === text) {
            expect(field.value).not.toBe(text);
          } else {
            expect(field.value).toBe(text);
          }

          // correct errorStatus is set (see validator)
          if ('ok' === text) {
            expect(field.errorStatus).not.toBeNull();
            expect(field.errorStatus.severity).toBe(Status.Severity.OK);
            expect(field.errorStatus.message).toBe('This has severity OK.');
          } else if ('warning' === text) {
            expect(field.errorStatus).not.toBeNull();
            expect(field.errorStatus.severity).toBe(Status.Severity.WARNING);
            expect(field.errorStatus.message).toBe('This has severity WARNING.');
          } else if ('throw' === text) {
            expect(field.errorStatus).not.toBeNull();
            expect(field.errorStatus.severity).toBe(Status.Severity.ERROR);
            expect(field.errorStatus.message).toBe('This is an exception.');
          } else {
            expect(field.errorStatus).toBeNull();
          }

          // lookupRow is set and contains the correct values iff a lookupRow was selected
          if (lookup) {
            expect(field.lookupRow).not.toBeNull();
            expect(field.lookupRow.text).toBe(text);
            expect(field.lookupRow.key).toBe(text);
          } else {
            expect(field.lookupRow).toBeNull();
          }
        }
      });
    }

    // select a value, then open and close the touch popup multiple times

    it('write \'ok\' (touchMode: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose('ok');
    });

    it('write \'warning\' (touchMode: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose('warning');
    });

    it('write \'throw\' (touchMode: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose('throw');
    });

    it('write \'no error\' (touchMode: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose('no error');
    });

    it('lookup \'ok\' (touchMode: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose({text: 'ok', lookup: true});
    });

    it('lookup \'warning\' (touchMode: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose({text: 'warning', lookup: true});
    });

    it('lookup \'throw\' (touchMode: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose({text: 'throw', lookup: true});
    });

    it('lookup \'no error\' (touchMode: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose({text: 'no error', lookup: true});
    });

    async function testTouchProposalFieldOpenClose(inputOrText: ProposalFieldSpecHelperInput | string) {
      const expectInput = (input: ProposalFieldSpecHelperInput) => {
        const {text, lookup} = input;

        // displayText always equals text
        expect(field.displayText).toBe(text);

        // value equals text iff there is no validation error (see validator)
        if ('throw' === text) {
          expect(field.value).not.toBe(text);
        } else {
          expect(field.value).toBe(text);
        }

        // correct errorStatus is set (see validator)
        if ('ok' === text) {
          expect(field.errorStatus).not.toBeNull();
          expect(field.errorStatus.severity).toBe(Status.Severity.OK);
          expect(field.errorStatus.message).toBe('This has severity OK.');
        } else if ('warning' === text) {
          expect(field.errorStatus).not.toBeNull();
          expect(field.errorStatus.severity).toBe(Status.Severity.WARNING);
          expect(field.errorStatus.message).toBe('This has severity WARNING.');
        } else if ('throw' === text) {
          expect(field.errorStatus).not.toBeNull();
          expect(field.errorStatus.severity).toBe(Status.Severity.ERROR);
          expect(field.errorStatus.message).toBe('This is an exception.');
        } else {
          expect(field.errorStatus).toBeNull();
        }

        // lookupRow is set and contains the correct values iff a lookupRow was selected
        if (lookup) {
          expect(field.lookupRow).not.toBeNull();
          expect(field.lookupRow.text).toBe(text);
          expect(field.lookupRow.key).toBe(text);
        } else {
          expect(field.lookupRow).toBeNull();
        }
      };

      const input = proposalFieldSpecHelper.ensureInput(inputOrText);

      await proposalFieldSpecHelper.testProposalFieldInputs(field, [input], true, {
        afterInput: expectInput
      });

      let i = 0;
      while (i < 5) {
        const popup = await proposalFieldSpecHelper.openPopup(field) as SmartFieldTouchPopup<string>;
        popup.doneAction.doAction();
        expectInput(input);
        i++;
      }
    }
  });
});
