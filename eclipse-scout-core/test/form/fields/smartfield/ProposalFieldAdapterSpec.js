/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ObjectFactory, scout, Status} from '../../../../src/index';
import {proposalFieldSpecHelper} from '../../../../src/testing';

describe('ProposalFieldAdapter', () => {

  let session, field, modelAdapter;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    ObjectFactory.get().register('SpecProposalField', () => proposalFieldSpecHelper.createSpecProposalField());

    field = scout.create('SpecProposalField', {parent: session.desktop});
    linkWidgetAndAdapter(field, 'ProposalFieldAdapter');
    modelAdapter = field.modelAdapter;
    field.setLookupCall(scout.create('StaticLookupCall', {
      session: field.session,
      data: [
        ['foo', 'foo'],
        ['bar', 'bar']
      ]
    }));
  });

  afterEach(() => {
    ObjectFactory.get().unregister('SpecProposalField');
  });

  describe('acceptInput-event contains correct data', () => {
    let spy;

    beforeEach(() => {
      spy = spyOn(modelAdapter, '_send');
    });

    // some > foo > bar

    it('write \'some\' > write \'foo\' > write \'bar\' (touchMode: false, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['some', 'foo', 'bar'],
        false,
        false));

    it('write \'some\' > write \'foo\' > write \'bar\' (touchMode: true, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['some', 'foo', 'bar'],
        true,
        false));

    it('write \'some\' > write \'foo\' > write \'bar\' (touchMode: false, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['some', 'foo', 'bar'],
        false,
        true));

    it('write \'some\' > write \'foo\' > write \'bar\' (touchMode: true, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['some', 'foo', 'bar'],
        true,
        true));

    // foo > some > bar

    it('write \'foo\' > write \'some\' > write \'bar\' (touchMode: false, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['foo', 'some', 'bar'],
        false,
        false));

    it('write \'foo\' > write \'some\' > write \'bar\' (touchMode: true, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['foo', 'some', 'bar'],
        true,
        false));

    it('write \'foo\' > write \'some\' > write \'bar\' (touchMode: false, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['foo', 'some', 'bar'],
        false,
        true));

    it('write \'foo\' > write \'some\' > write \'bar\' (touchMode: true, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['foo', 'some', 'bar'],
        true,
        true));

    // foo > bar > some

    it('write \'foo\' > write \'bar\' > write \'some\' (touchMode: false, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['foo', 'bar', 'some'],
        false,
        false));

    it('write \'foo\' > write \'bar\' > write \'some\' (touchMode: true, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['foo', 'bar', 'some'],
        true,
        false));

    it('write \'foo\' > write \'bar\' > write \'some\' (touchMode: false, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['foo', 'bar', 'some'],
        false,
        true));

    it('write \'foo\' > write \'bar\' > write \'some\' (touchMode: true, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['foo', 'bar', 'some'],
        true,
        true));

    // foo > bar > some > thing

    it('lookup \'foo\' > lookup \'bar\' > write \'some\' > write \'thing\' (touchMode: false, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'some', 'thing'],
        false,
        false));

    it('lookup \'foo\' > lookup \'bar\' > write \'some\' > write \'thing\' (touchMode: true, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'some', 'thing'],
        true,
        false));

    it('lookup \'foo\' > lookup \'bar\' > write \'some\' > write \'thing\' (touchMode: false, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'some', 'thing'],
        false,
        true));

    it('lookup \'foo\' > lookup \'bar\' > write \'some\' > write \'thing\' (touchMode: true, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'some', 'thing'],
        true,
        true));

    // some > thing > foo > bar

    it('write \'some\' > write \'thing\' > lookup \'foo\' > lookup \'bar\' (touchMode: false, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['some', 'thing', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        false,
        false));

    it('write \'some\' > write \'thing\' > lookup \'foo\' > lookup \'bar\' (touchMode: true, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['some', 'thing', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        true,
        false));

    it('write \'some\' > write \'thing\' > lookup \'foo\' > lookup \'bar\' (touchMode: false, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['some', 'thing', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        false,
        true));

    it('write \'some\' > write \'thing\' > lookup \'foo\' > lookup \'bar\' (touchMode: true, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['some', 'thing', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        true,
        true));

    // foo > bar > foo > bar

    it('lookup \'foo\' > lookup \'bar\' > write \'foo\' > write \'bar\' (touchMode: false, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'foo', 'bar'],
        false,
        false));

    it('lookup \'foo\' > lookup \'bar\' > write \'foo\' > write \'bar\' (touchMode: true, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'foo', 'bar'],
        true,
        false));

    it('lookup \'foo\' > lookup \'bar\' > write \'foo\' > write \'bar\' (touchMode: false, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'foo', 'bar'],
        false,
        true));

    it('lookup \'foo\' > lookup \'bar\' > write \'foo\' > write \'bar\' (touchMode: true, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        [{text: 'foo', lookup: true}, {text: 'bar', lookup: true}, 'foo', 'bar'],
        true,
        true));

    it('write \'foo\' > write \'bar\' > lookup \'foo\' > lookup \'bar\' (touchMode: false, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['foo', 'bar', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        false,
        false));

    it('write \'foo\' > write \'bar\' > lookup \'foo\' > lookup \'bar\' (touchMode: true, withErrorStatus: false)', async () =>
      await testProposalFieldInputs(
        ['foo', 'bar', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        true,
        false));

    it('write \'foo\' > write \'bar\' > lookup \'foo\' > lookup \'bar\' (touchMode: false, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['foo', 'bar', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        false,
        true));

    it('write \'foo\' > write \'bar\' > lookup \'foo\' > lookup \'bar\' (touchMode: true, withErrorStatus: true)', async () =>
      await testProposalFieldInputs(
        ['foo', 'bar', {text: 'foo', lookup: true}, {text: 'bar', lookup: true}],
        true,
        true));

    async function testProposalFieldInputs(inputs, touchMode, withErrorStatus) {
      const callbacks = {
        afterInput: (input) => {
          const {text} = input;
          expect(field.value).toBe(text);
          expect(field.errorStatus).toBeNull();
        },
        afterSelectLookupRow: (text, lookupRow) => expectAcceptInputEvent(text, lookupRow),
        afterAcceptCustomText: (text) => expectAcceptInputEvent(text)
      };

      if (withErrorStatus) {
        callbacks.beforeInput = (input) => field.setErrorStatus(Status.warning('I am a WARNING!'));
      }

      await proposalFieldSpecHelper.testProposalFieldInputs(field, inputs, touchMode, callbacks);
    }

    function expectAcceptInputEvent(text, lookupRow) {
      const displayText = text;
      const value = text;
      let eventData = {displayText, value, errorStatus: null};
      if (lookupRow) {
        eventData = {...eventData, lookupRow};
      }
      expect(spy).toHaveBeenCalledWith('acceptInput', jasmine.objectContaining(eventData), jasmine.anything());
    }
  });
});
