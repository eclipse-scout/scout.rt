/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupRow, QueryBy, RemoteEvent, scout, SmartField, SmartFieldPopup, Status} from '../../../../src/index';
import {FormSpecHelper, JQueryTesting} from '../../../../src/testing/index';

describe('SmartFieldRemote', () => {

  // This spec contains test that use the SmartFieldAdapter (= remote case)

  let session: SandboxSession, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    removePopups(session);
    removePopups(session, '.touch-popup');
  });

  function createSmartFieldWithAdapter(): SmartField<any> {
    let model = helper.createFieldModel(SmartField);
    let smartField = new SmartField();
    smartField.init(model);
    linkWidgetAndAdapter(smartField, 'SmartFieldAdapter');
    return smartField;
  }

  describe('openPopup', () => {
    let smartField: SmartField<any>;

    beforeEach(() => {
      smartField = createSmartFieldWithAdapter();
      smartField.render();
      smartField.$field.val('foo');
    });

    it('must "browse all" when field is valid and browse parameter is true', () => {
      smartField.openPopup(true);
      sendQueuedAjaxCalls();
      let expectedEvent = new RemoteEvent(smartField.id, 'lookupByAll', {
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('must "lookup by text" when called without arguments and display-text is not empty', () => {
      smartField.openPopup();
      jasmine.clock().tick(500); // because we use a debounce in SmartField
      sendQueuedAjaxCalls();
      let expectedEvent = new RemoteEvent(smartField.id, 'lookupByText', {
        showBusyIndicator: false,
        text: 'foo'
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('must "lookup by text" when error status is NOT_UNIQUE, even though the browse parameter is true', () => {
      smartField.errorStatus = Status.error({
        message: 'bar',
        code: SmartField.ErrorCode.NOT_UNIQUE
      });
      smartField.openPopup(true);
      jasmine.clock().tick(500); // because we use a debounce in SmartField
      sendQueuedAjaxCalls();
      let expectedEvent = new RemoteEvent(smartField.id, 'lookupByText', {
        showBusyIndicator: false,
        text: 'foo'
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });
  });

  describe('acceptInput', () => {
    let smartField;

    beforeEach(() => {
      smartField = createSmartFieldWithAdapter();
    });

    it('must set displayText', () => {
      smartField.render();
      smartField.$field.val('foo');
      smartField.acceptInput();
      expect(smartField.displayText).toBe('foo');
    });

    it('must call clearTimeout() for pending lookups', () => {
      smartField.render();
      smartField._pendingLookup = null;
      smartField.$field.val('bar');
      smartField._lookupByTextOrAll();
      expect(smartField._pendingLookup).toBeTruthy();
      smartField.acceptInput();
      expect(smartField._pendingLookup).toBe(null);
    });

    it('don\'t send acceptInput event when display-text has not changed', () => {
      smartField._lastSearchText = 'foo';
      smartField.render();
      smartField.$field.val('foo');
      smartField.acceptInput();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

    it('send acceptInput event when lookup row is set and display-text has not changed', () => {
      let lookupRow = scout.create(LookupRow, {
        key: 123,
        text: 'foo'
      }, {
        ensureUniqueId: false
      });

      smartField._lastSearchText = 'foo';
      smartField.render();
      smartField.$field.val('foo');
      smartField.popup = scout.create(SmartFieldPopup, {
        parent: smartField,
        field: smartField,
        lookupResult: {
          seqNo: 0, // must match smartField.lookupSeqNo
          queryBy: QueryBy.ALL,
          lookupRows: [lookupRow]
        }
      });
      smartField.acceptInput();

      sendQueuedAjaxCalls();
      let expectedEvent = new RemoteEvent(smartField.id, 'acceptInput', {
        value: 123,
        displayText: 'foo',
        errorStatus: null,
        lookupRow: {
          objectType: 'LookupRow',
          key: 123,
          text: 'foo',
          parentKey: null,
          active: true,
          enabled: true,
          additionalTableRowData: null,
          cssClass: null,
          iconId: null,
          tooltipText: null,
          backgroundColor: null,
          foregroundColor: null,
          font: null
        },
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('do a "lookup by text" when display-text has changed and no lookup row is set', () => {
      smartField.displayText = 'foo';
      smartField.render();
      // simulate the user has typed some text. Normally this would be done in _onFieldKeyDown/Up
      // since we don't want to work with key-event in this test, we must set the _userWasTyping flag manually
      smartField.$field.val('bar');
      smartField._userWasTyping = true;
      // --- end of simulation ---
      smartField.acceptInput();

      sendQueuedAjaxCalls();
      let expectedEvent = new RemoteEvent(smartField.id, 'lookupByText', {
        text: 'bar',
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

  });

  describe('touch mode', () => {
    let smartField;

    beforeEach(() => {
      smartField = createSmartFieldWithAdapter();
    });

    function resolveLookupCall(lookupCall) {
      lookupCall.resolveLookup({
        queryBy: QueryBy.ALL,
        lookupRows: [scout.create(LookupRow, {
          key: 123,
          text: 'foo'
        })]
      });
      jasmine.clock().tick(500);
    }

    it('opens a touch popup when smart field gets touched', () => {
      let lookupCallClone = null;
      smartField.touchMode = true;
      smartField.render();
      smartField.on('prepareLookupCall', event => {
        lookupCallClone = event.lookupCall;
      });

      JQueryTesting.triggerClick(smartField.$field);
      resolveLookupCall(lookupCallClone);
      expect(smartField.popup.rendered).toBe(true);
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);

      smartField.popup.close();
      expect(smartField.popup).toBe(null);
      expect($('.touch-popup').length).toBe(0);
      expect($('.smart-field-popup').length).toBe(0);

      // Expect same behavior after a second click
      JQueryTesting.triggerClick(smartField.$field);
      resolveLookupCall(lookupCallClone);
      expect(smartField.popup.rendered).toBe(true);
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);
      smartField.popup.close();
    });

    it('shows smartfield with same text as clicked smartfield', () => {
      let lookupCallClone = null;
      smartField.touchMode = true;
      smartField.displayText = 'row 1';
      smartField.render();
      smartField.on('prepareLookupCall', event => {
        lookupCallClone = event.lookupCall;
      });
      JQueryTesting.triggerClick(smartField.$field);
      resolveLookupCall(lookupCallClone);
      expect(smartField.popup.rendered).toBe(true);
      expect(smartField.popup._field.displayText).toBe(smartField.displayText);
      expect(smartField.popup._field.$field.val()).toBe(smartField.displayText);
      smartField.popup.close();
    });

  });

});
