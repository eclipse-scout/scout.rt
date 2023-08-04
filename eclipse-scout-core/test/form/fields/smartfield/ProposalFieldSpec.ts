/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, LookupRow, ProposalField, scout, Status} from '../../../../src/index';
import {SmartFieldLookupResult} from '../../../../src/form/fields/smartfield/SmartField';
import {QueryBy} from '../../../../src';
import {JQueryTesting} from '../../../../src/testing';

describe('ProposalField', () => {

  let session: SandboxSession, field: SpecProposalField, lookupRow: LookupRow<string>;

  class SpecProposalField extends ProposalField {
    declare _userWasTyping: boolean;

    override _lookupByTextOrAllDone(result: SmartFieldLookupResult<string>) {
      super._lookupByTextOrAllDone(result);
    }

    override _getLastSearchText(): string {
      return super._getLastSearchText();
    }

    override _acceptInput(sync: boolean, searchText: string, searchTextEmpty: boolean, searchTextChanged: boolean, selectedLookupRow: LookupRow<string>): JQuery.Promise<void> | void {
      return super._acceptInput(sync, searchText, searchTextEmpty, searchTextChanged, selectedLookupRow);
    }
  }

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

  describe('aria properties', () => {

    it('has aria role combobox', () => {
      field.render();
      expect(field.$field).toHaveAttr('role', 'combobox');
    });

    it('has aria-describedby description for its functionality', () => {
      field.render();
      let $fieldDescription = field.$container.find('#desc' + field.id + '-func-desc');
      expect(field.$field.attr('aria-describedby')).toBeTruthy();
      expect(field.$field.attr('aria-describedby')).toBe($fieldDescription.eq(0).attr('id'));
      expect(field.$field.attr('aria-description')).toBeFalsy();
    });

    it('has a non empty status container that lists count of available options', () => {
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(field.$screenReaderStatus).toHaveAttr('role', 'status');
      expect(field.$screenReaderStatus).toHaveClass('sr-only');
      expect(field.$screenReaderStatus.children('.sr-lookup-row-count').length).toBe(1);
      expect(field.$screenReaderStatus.children('.sr-lookup-row-count').eq(0)).not.toBeEmpty();
    });

    it('has a aria-expanded set correctly if pop up is open/closed', () => {
      field.render();
      expect(field.$field).toHaveAttr('aria-expanded', 'false');
      field.$field.focus(); // must be focused, otherwise popup will not open
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(field.$field).toHaveAttr('aria-expanded', 'true');
      field.closePopup();
    });

    it('has a aria-controls set correctly if pop up is open/closed', () => {

      field.render();
      expect(field.$field.attr('aria-controls')).toBeFalsy();
      field.$field.focus(); // must be focused, otherwise popup will not open
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(field.$field.attr('aria-controls')).toBe(field.popup.$container.attr('id'));
      field.closePopup();
    });

    it('has a aria-activedescendant set correctly if pop up is open/closed', () => {
      field.render();
      expect(field.$field.attr('aria-activedescendant')).toBeFalsy();
      field.$field.focus(); // must be focused, otherwise popup will not open
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      JQueryTesting.triggerKeyDown(field.$field, keys.DOWN);
      expect(field.$field.attr('aria-activedescendant')).toBeTruthy();
      field.closePopup();
    });
  });
});
