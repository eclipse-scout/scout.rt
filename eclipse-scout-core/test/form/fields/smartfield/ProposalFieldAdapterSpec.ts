/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupRow, ModelAdapterSendOptions, ProposalFieldAdapter, scout, SmartFieldAcceptInputEvent, SmartFieldTouchPopup, StaticLookupCall, Status} from '../../../../src/index';
import {proposalFieldSpecHelper, ProposalFieldSpecHelperCallbacks, ProposalFieldSpecHelperInput, SpecProposalField} from '../../../../src/testing';

describe('ProposalFieldAdapter', () => {

  let session: SandboxSession, field: SpecProposalField, modelAdapter: SpecProposalFieldAdapter;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = scout.create(SpecProposalField, {parent: session.desktop});
    linkWidgetAndAdapter(field, SpecProposalFieldAdapter);
    modelAdapter = field.modelAdapter as SpecProposalFieldAdapter;
    field.setLookupCall(scout.create(StaticLookupCall<string>, {
      session: field.session,
      data: [
        ['foo', 'foo'],
        ['bar', 'bar']
      ]
    }));
  });

  describe('acceptInput-event contains correct data', () => {
    let spy;

    beforeEach(() => {
      spy = spyOn(modelAdapter, '_send');
    });

    // some > foo > bar

    it('write \'some\' > write \'foo\' > write \'bar\' (touchMode: false, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['some', 'foo', 'bar'],
        false,
        false);
    });

    it('write \'some\' > write \'foo\' > write \'bar\' (touchMode: true, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['some', 'foo', 'bar'],
        true,
        false);
    });

    it('write \'some\' > write \'foo\' > write \'bar\' (touchMode: false, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['some', 'foo', 'bar'],
        false,
        true);
    });

    it('write \'some\' > write \'foo\' > write \'bar\' (touchMode: true, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['some', 'foo', 'bar'],
        true,
        true);
    });

    // foo > some > bar

    it('write \'foo\' > write \'some\' > write \'bar\' (touchMode: false, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['foo', 'some', 'bar'],
        false,
        false);
    });

    it('write \'foo\' > write \'some\' > write \'bar\' (touchMode: true, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['foo', 'some', 'bar'],
        true,
        false);
    });

    it('write \'foo\' > write \'some\' > write \'bar\' (touchMode: false, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['foo', 'some', 'bar'],
        false,
        true);
    });

    it('write \'foo\' > write \'some\' > write \'bar\' (touchMode: true, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['foo', 'some', 'bar'],
        true,
        true);
    });

    // foo > bar > some

    it('write \'foo\' > write \'bar\' > write \'some\' (touchMode: false, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['foo', 'bar', 'some'],
        false,
        false);
    });

    it('write \'foo\' > write \'bar\' > write \'some\' (touchMode: true, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['foo', 'bar', 'some'],
        true,
        false);
    });

    it('write \'foo\' > write \'bar\' > write \'some\' (touchMode: false, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['foo', 'bar', 'some'],
        false,
        true);
    });

    it('write \'foo\' > write \'bar\' > write \'some\' (touchMode: true, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['foo', 'bar', 'some'],
        true,
        true);
    });

    // foo > bar > some > thing

    it('lookup \'foo\' > lookup \'bar\' > write \'some\' > write \'thing\' (touchMode: false, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'some', 'thing'],
        false,
        false);
    });

    it('lookup \'foo\' > lookup \'bar\' > write \'some\' > write \'thing\' (touchMode: true, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'some', 'thing'],
        true,
        false);
    });

    it('lookup \'foo\' > lookup \'bar\' > write \'some\' > write \'thing\' (touchMode: false, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'some', 'thing'],
        false,
        true);
    });

    it('lookup \'foo\' > lookup \'bar\' > write \'some\' > write \'thing\' (touchMode: true, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'some', 'thing'],
        true,
        true);
    });

    // some > thing > foo > bar

    it('write \'some\' > write \'thing\' > lookup \'foo\' > lookup \'bar\' (touchMode: false, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['some', 'thing', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        false,
        false);
    });

    it('write \'some\' > write \'thing\' > lookup \'foo\' > lookup \'bar\' (touchMode: true, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['some', 'thing', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        true,
        false);
    });

    it('write \'some\' > write \'thing\' > lookup \'foo\' > lookup \'bar\' (touchMode: false, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['some', 'thing', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        false,
        true);
    });

    it('write \'some\' > write \'thing\' > lookup \'foo\' > lookup \'bar\' (touchMode: true, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['some', 'thing', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        true,
        true);
    });

    // foo > bar > foo > bar

    it('lookup \'foo\' > lookup \'bar\' > write \'foo\' > write \'bar\' (touchMode: false, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'foo', 'bar'],
        false,
        false);
    });

    it('lookup \'foo\' > lookup \'bar\' > write \'foo\' > write \'bar\' (touchMode: true, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'foo', 'bar'],
        true,
        false);
    });

    it('lookup \'foo\' > lookup \'bar\' > write \'foo\' > write \'bar\' (touchMode: false, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'foo', 'bar'],
        false,
        true);
    });

    it('lookup \'foo\' > lookup \'bar\' > write \'foo\' > write \'bar\' (touchMode: true, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'foo', 'bar'],
        true,
        true);
    });

    it('write \'foo\' > write \'bar\' > lookup \'foo\' > lookup \'bar\' (touchMode: false, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['foo', 'bar', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        false,
        false);
    });

    it('write \'foo\' > write \'bar\' > lookup \'foo\' > lookup \'bar\' (touchMode: true, withErrorStatus: false)', async () => {
      await testProposalFieldInputs(
        ['foo', 'bar', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        true,
        false);
    });

    it('write \'foo\' > write \'bar\' > lookup \'foo\' > lookup \'bar\' (touchMode: false, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['foo', 'bar', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        false,
        true);
    });

    it('write \'foo\' > write \'bar\' > lookup \'foo\' > lookup \'bar\' (touchMode: true, withErrorStatus: true)', async () => {
      await testProposalFieldInputs(
        ['foo', 'bar', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        true,
        true);
    });

    async function testProposalFieldInputs(inputs: (ProposalFieldSpecHelperInput | string)[], touchMode?: boolean, withErrorStatus?: boolean) {
      const callbacks: ProposalFieldSpecHelperCallbacks = {
        afterInput: (input: ProposalFieldSpecHelperInput) => {
          const {text} = input;
          expect(field.value).toBe(text);
          expect(field.errorStatus).toBeNull();
        },
        afterSelectLookupRow: (text: string, lookupRow: LookupRow<string>) => expectAcceptInputEvent(text, lookupRow),
        afterAcceptCustomText: (text: string) => expectAcceptInputEvent(text)
      };

      if (withErrorStatus) {
        callbacks.beforeInput = (input: ProposalFieldSpecHelperInput) => field.setErrorStatus(Status.warning('I am a WARNING!'));
      }

      await proposalFieldSpecHelper.testProposalFieldInputs(field, inputs, touchMode, callbacks);
    }

    function expectAcceptInputEvent(text: string, lookupRow?: LookupRow<string>) {
      const displayText = text;
      const value = text;
      let eventData: Partial<SmartFieldAcceptInputEvent> = {displayText, value, errorStatus: null};
      if (lookupRow) {
        eventData = {...eventData, lookupRow};
      }
      expect(spy).toHaveBeenCalledWith('acceptInput', jasmine.objectContaining(eventData), jasmine.anything());
    }

    // select a value, then open and close the touch popup multiple times

    it('write \'foo\' (touchMode: true, withErrorStatus: false, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose(
        'foo',
        false);
    });

    it('write \'foo\' (touchMode: true, withErrorStatus: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose(
        'foo',
        true);
    });

    it('write \'some\' (touchMode: true, withErrorStatus: false, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose(
        'some',
        false);
    });

    it('write \'some\' (touchMode: true, withErrorStatus: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose(
        'some',
        true);
    });

    it('lookup \'foo\' (touchMode: true, withErrorStatus: false, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose(
        {text: 'foo', lookup: true},
        false);
    });

    it('lookup \'foo\' (touchMode: true, withErrorStatus: true, open and close multiple times)', async () => {
      await testTouchProposalFieldOpenClose(
        {text: 'foo', lookup: true},
        true);
    });

    async function testTouchProposalFieldOpenClose(inputOrText: ProposalFieldSpecHelperInput | string, withErrorStatus?: boolean) {
      const expectInput = (input: ProposalFieldSpecHelperInput) => {
        const {text} = input;
        expect(field.value).toBe(text);
        if (withErrorStatus) {
          expect(field.errorStatus).not.toBeNull();
        } else {
          expect(field.errorStatus).toBeNull();
        }
      };
      const callbacks = {
        afterInput: (input: ProposalFieldSpecHelperInput) => {
          if (withErrorStatus) {
            field.setErrorStatus(Status.warning('I am a WARNING!'));
          }
          expectInput(input);
        },
        afterSelectLookupRow: (text: string, lookupRow: LookupRow<string>) => expectAcceptInputEvent(text, lookupRow),
        afterAcceptCustomText: (text: string) => expectAcceptInputEvent(text)
      };

      const input = proposalFieldSpecHelper.ensureInput(inputOrText);

      await proposalFieldSpecHelper.testProposalFieldInputs(field, [input], true, callbacks);

      const callCount = spy.calls.count();

      let i = 0;
      while (i < 5) {
        const popup = await proposalFieldSpecHelper.openPopup(field) as SmartFieldTouchPopup<string>;
        popup.doneAction.doAction();
        expectInput(input);
        expect(spy).toHaveBeenCalledTimes(callCount);
        i++;
      }
    }
  });
});

class SpecProposalFieldAdapter extends ProposalFieldAdapter {
  override _send<Data extends Record<PropertyKey, any>>(type: string, data?: Data, options?: ModelAdapterSendOptions<Data>) {
    super._send(type, data, options);
  }
}
