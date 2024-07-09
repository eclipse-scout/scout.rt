/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  fields, keys, LookupResult, LookupRow, PrepopulatedLookupCall, ProposalChooser, QueryBy, scout, SmartField, SmartFieldModel, SmartFieldMultiline, SmartFieldPopup, SmartFieldTouchPopup, StaticLookupCall, Status, strings,
  ValidationFailedStatus
} from '../../../../src/index';
import {ColumnDescriptorDummyLookupCall, DelayedStaticLookupCall, DummyLookupCall, FormSpecHelper, JQueryTesting, MicrotaskStaticLookupCall} from '../../../../src/testing/index';
import {LookupCall} from '../../../../src/lookup/LookupCall';
import {SmartFieldLookupResult} from '../../../../src/form/fields/smartfield/SmartField';
import {FullModelOf, InitModelOf, ObjectOrModel} from '../../../../src/scout';
import $ from 'jquery';

describe('SmartField', () => {

  let session: SandboxSession, field: SpecSmartField, lookupRow: LookupRow<number>, helper: FormSpecHelper;

  class SpecSmartField extends SmartField<number> {
    declare _userWasTyping: boolean;
    declare _lastSearchText: string;
    declare _pendingOpenPopup: boolean;

    override _readSearchText(): string {
      return super._readSearchText();
    }

    override _onFieldKeyUp(event: JQuery.KeyUpEvent) {
      super._onFieldKeyUp(event);
    }

    override _acceptByText(sync: boolean, searchText: string) {
      super._acceptByText(sync, searchText);
    }

    override _lookupByTextOrAll(browse?: boolean, searchText?: string, searchAlways?: boolean): JQuery.Promise<any> {
      return super._lookupByTextOrAll(browse, searchText, searchAlways);
    }

    override _lookupByTextOrAllDone(result: SmartFieldLookupResult<number>) {
      super._lookupByTextOrAllDone(result);
    }

    override _executeLookup(lookupCall: LookupCall<number>, abortExisting?: boolean): JQuery.Promise<SmartFieldLookupResult<number>> {
      return super._executeLookup(lookupCall, abortExisting);
    }

    override _formatValue(value: number): string | JQuery.Promise<string> {
      return super._formatValue(value);
    }

    override _copyValuesFromField(otherField: SmartField<number>) {
      super._copyValuesFromField(otherField);
    }

    override _getLastSearchText(): string {
      return super._getLastSearchText();
    }
  }

  class SpecSmartFieldTouchPopup extends SmartFieldTouchPopup<number> {
    declare _widget: ProposalChooser<number, any, any>;
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new SpecSmartField();
    lookupRow = scout.create((LookupRow<number>), {
      key: 123,
      text: 'Foo'
    });
    helper = new FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    removePopups(session);
    removePopups(session, '.touch-popup');
  });

  function createFieldWithLookupCall(model?: SmartFieldModel<number>, lookupCallModel?: ObjectOrModel<LookupCall<number>> | string): SpecSmartField {
    lookupCallModel = $.extend({
      objectType: DummyLookupCall
    }, lookupCallModel);

    model = $.extend({}, {
      parent: session.desktop,
      lookupCall: lookupCallModel
    }, model);
    return scout.create(SmartField, model as InitModelOf<SmartField<number>>) as SpecSmartField;
  }

  function findTableProposals(): string[] {
    let proposals: string[] = [];
    session.desktop.$container.find('.table-row').each(function() {
      proposals.push($(this).find('.table-cell').first().text());
    });
    return proposals;
  }

  describe('general behavior', () => {

    it('defaults', () => {
      expect(field.displayStyle).toBe('default');
      expect(field.value).toBe(null);
      expect(field.displayText).toBe(null);
      expect(field.lookupRow).toBe(null);
      expect(field.popup).toBe(null);
    });

    it('setLookupRow', () => {
      field.setLookupRow(lookupRow);
      expect(field.value).toBe(123);
      expect(field.lookupRow).toBe(lookupRow);
      expect(field.displayText).toBe('Foo');
    });

    it('init LookupCall when configured as string', () => {
      field = createFieldWithLookupCall();
      expect(field.lookupCall instanceof DummyLookupCall).toBe(true);
    });

    it('when setValue is called, load and set the correct lookup row', () => {
      field = createFieldWithLookupCall();
      field.setValue(1);
      jasmine.clock().tick(300);
      expect(field.displayText).toBe('Foo');
      expect(field.value).toBe(1);
      expect(field.lookupRow.key).toBe(1);

      // set the value to null again
      field.setValue(null);
      expect(field.lookupRow).toBe(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');

      field.setValue(2);
      jasmine.clock().tick(300);
      expect(field.displayText).toBe('Bar');
      expect(field.value).toBe(2);
      expect(field.lookupRow.key).toBe(2);
    });

    it('load proposals for the current displayText', () => {
      field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.$field.val('b');
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(300);
      expect(field.$container.hasClass('loading')).toBe(false); // loading indicator is not shown before 400 ms
      jasmine.clock().tick(300);
      // expect we have 2 table rows
      expect(field.popup).not.toBe(null);
      expect(findTableProposals()).toEqual(['Bar', 'Baz']);
    });

    it('reset active filter', () => {
      field = createFieldWithLookupCall();
      field.setActiveFilterEnabled(true);
      field.setActiveFilter('FALSE');
      field.markAsSaved();
      field.setActiveFilter('UNDEFINED');
      expect(field.activeFilter).toEqual('UNDEFINED');
      field.resetValue();
      expect(field.activeFilter).toEqual('FALSE');
    });

    it('updates display text correctly even after consecutive setValue calls', done => {
      jasmine.clock().uninstall();
      field = createFieldWithLookupCall({}, {
        objectType: MicrotaskStaticLookupCall,
        data: [[1, 'Foo'], [2, 'Bar', 1]]
      });
      field.setValue(1);
      expect(field.value).toBe(1);
      expect(field.displayText).toBe(''); // Not updated yet

      field.setValue(2);
      expect(field.value).toBe(2);
      expect(field.displayText).toBe(''); // Not updated yet

      // Double setTimeout is necessary to ensure the expectations are checked at the end after every other task
      setTimeout(() => {
        setTimeout(() => {
          expect(field.value).toBe(2);
          expect(field.displayText).toBe('Bar'); // Updated to latest setValue call
          done();
        });
      });
    });
  });

  describe('clear', () => {

    it('clears the value', () => {
      let field = createFieldWithLookupCall();
      jasmine.clock().tick(500);
      field.render();
      field.$field.focus();
      field.setValue(1);
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);
      jasmine.clock().tick(500);
      expect(field.value).toBe(1);
      expect(field.displayText).toBe('Foo');
      expect(field.$field.val()).toBe('Foo');
      let popup = field.popup as SmartFieldPopup<number>;
      expect(popup.proposalChooser.content.selectedRows.length).toBe(1);

      field.clear();
      jasmine.clock().tick(500);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(field.$field.val()).toBe('');
      expect(popup.proposalChooser.content.selectedRows.length).toBe(0);
    });

    it('clears the value, also in embedded mode', () => {
      let field = createFieldWithLookupCall({
        touchMode: true
      });
      field.render();
      field.setValue(1);
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);
      jasmine.clock().tick(500);
      expect(field.value).toBe(1);
      expect(field.displayText).toBe('Foo');
      expect(field.$field.text()).toBe('Foo');
      let popup = field.popup as SpecSmartFieldTouchPopup;
      expect(popup._widget.content.selectedRows.length).toBe(1);

      popup._field.$field.focus();
      popup._field.clear();
      jasmine.clock().tick(500);
      expect(popup._field.value).toBe(null);
      expect(popup._field.displayText).toBe('');
      expect(popup._field.$field.val()).toBe('');
      expect(popup._widget.content.selectedRows.length).toBe(0);

      popup.close();
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(field.$field.val()).toBe('');
    });

    it('clears the value, also in touch mode', () => {
      let field = createFieldWithLookupCall({
        touchMode: true
      });
      field.render();
      field.setValue(1);
      jasmine.clock().tick(500);
      expect(field.value).toBe(1);
      expect(field.displayText).toBe('Foo');
      expect(field.$field.text()).toBe('Foo');

      field.clear();
      jasmine.clock().tick(500);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(field.lookupRow).toBe(null);
      expect(field.$field.val()).toBe('');
    });

    it('does not close the popup but does a browse all', () => {
      // This is especially important for mobile, but makes sense for regular case too.
      let field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.$field.val('b');
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);

      // do not animate the removal of the SmartFieldPopup. Otherwise it will not be removed yet, when the new one should be opened. Then nothing is updated.
      field.popup.animateRemoval = false;

      expect(field.popup).not.toBe(null);
      expect(findTableProposals()).toEqual(['Bar', 'Baz']);

      field.clear();
      jasmine.clock().tick(500);
      expect(field.popup).not.toBe(null);
      expect(findTableProposals()).toEqual(['Foo', 'Bar', 'Baz']);
    });

  });

  describe('touch popup', () => {

    it('marks field as clearable even if the field is not focused', () => {
      let field = createFieldWithLookupCall({
        touchMode: true
      });
      field.render();
      field.$field.focus();
      field.setValue(1);
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);
      jasmine.clock().tick(500);
      let popup = field.popup as SpecSmartFieldTouchPopup;
      expect(popup).not.toBe(null);
      expect(popup._field.$field.val()).toBe('Foo');
      expect(popup._field.$container).toHaveClass('clearable-always');
    });

    it('stays open if active / inactive radio buttons are clicked', () => {
      let field = createFieldWithLookupCall({
        touchMode: true,
        activeFilterEnabled: true
      });
      field.render();
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);
      jasmine.clock().tick(500);
      let popup = field.popup as SpecSmartFieldTouchPopup;
      popup._widget.activeFilterGroup.radioButtons[1].select();
      jasmine.clock().tick(500);
      expect(popup).not.toBe(null);
    });

    it('stays open even if there are no results (with active filter)', () => {
      // Use case: Click on touch smart field, select inactive radio button, clear the text in the field -> smart field has to stay open
      let field = createFieldWithLookupCall({
        touchMode: true,
        activeFilterEnabled: true
      });
      field.render();
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);
      jasmine.clock().tick(500);
      let popup = field.popup as SpecSmartFieldTouchPopup;
      popup._widget.activeFilterGroup.radioButtons[1].select();
      // Simulate that lookup call does not return any data (happens if user clicks 'inactive' radio button and there are no inactive rows
      popup._field.lookupCall['data'] = [];
      popup._field.$field.focus();
      JQueryTesting.triggerKeyDown(popup._field.$field, keys.BACKSPACE);
      // @ts-expect-error
      popup._field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(popup).not.toBe(null);
    });

    it('removes tooltip from original field on open and displays it again when closed', () => {
      let field = createFieldWithLookupCall({
        touchMode: true,
        errorStatus: Status.error({
          message: 'foo'
        })
      });
      field.render();
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);
      jasmine.clock().tick(500);
      expect(field.tooltip().rendered).toBe(false);
      let popup = field.popup as SpecSmartFieldTouchPopup;
      expect(popup._field.tooltip().rendered).toBe(true);

      popup.close();
      jasmine.clock().tick(500);
      expect(field.popup).toBe(null);
      expect(field.tooltip().rendered).toBe(true);
    });

    it('does not draw glass pane over tooltip', () => {
      let field = createFieldWithLookupCall({
        touchMode: true,
        errorStatus: Status.error({
          message: 'foo'
        })
      });
      field.render();
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);
      jasmine.clock().tick(500);
      let popup = field.popup as SpecSmartFieldTouchPopup;
      expect(popup._field.tooltip().rendered).toBe(true);
      expect(popup._field.tooltip().$container.find('.glasspane').length).toBe(0);
    });

    it('delegates lookup events to original field', () => {
      let field = createFieldWithLookupCall({
        touchMode: true
      });
      let prepareLookupCallCounter = 0;
      let lookupCallDoneCounter = 0;
      let onPrepareLookupCall = () => {
        prepareLookupCallCounter++;
      };
      let onLookupCallDone = () => {
        lookupCallDoneCounter++;
      };
      field.on('prepareLookupCall', onPrepareLookupCall.bind(field));
      field.on('lookupCallDone', onLookupCallDone.bind(field));
      field.render();
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);
      jasmine.clock().tick(500);

      let popup = field.popup as SpecSmartFieldTouchPopup;
      let oldPrepareLookupCallCounter = prepareLookupCallCounter;
      let oldLookupCallDoneCounter = lookupCallDoneCounter;
      popup._field.setValue(1);
      jasmine.clock().tick(500);
      JQueryTesting.triggerClick(field.$field);

      expect(prepareLookupCallCounter).toBe(oldPrepareLookupCallCounter + 1);
      expect(lookupCallDoneCounter).toBe(oldLookupCallDoneCounter + 1);
    });

  });

  describe('acceptInput', () => {

    it('should not be triggered, when search text is (still) empty or equals to the text of the lookup row', () => {
      let field = createFieldWithLookupCall();
      let eventTriggered = false;
      field.render();
      field.on('acceptInput', () => {
        eventTriggered = true;
      });
      // empty case
      field.acceptInput();
      expect(eventTriggered).toBe(false);

      // text equals case
      field.setValue(1); // set lookup row [1, Foo]
      jasmine.clock().tick(500);
      expect(field.lookupRow.text).toBe('Foo');
      expect(field.$field.val()).toBe('Foo');
      expect(field.value).toBe(1);
      field.acceptInput();
      expect(eventTriggered).toBe(false);
    });

    // ticket #214831
    it('should not be triggered, when search text is (still) empty or equals to the text of the lookup row (lookupRow.text is null)', () => {
      let field = createFieldWithLookupCall({}, {
        showText: false
      });
      let eventTriggered = false;
      field.render();
      field.on('acceptInput', () => {
        eventTriggered = true;
      });
      // empty case
      field.acceptInput();
      expect(eventTriggered).toBe(false);

      // text equals case
      field.setValue(1); // set lookup row [1, null]
      jasmine.clock().tick(500);
      expect(field.lookupRow.text).toBe(null);
      expect(field.$field.val()).toBe('');
      expect(field.value).toBe(1);
      field.acceptInput();
      expect(eventTriggered).toBe(false);
    });

    // ticket #221944
    describe('should (not) reset selected lookup row', () => {
      let field, selectedLookupRow, searchTextChanged;

      // mocks for popup, lookup-row
      beforeEach(() => {
        field = createFieldWithLookupCall();
        selectedLookupRow = {};
        field.popup = {
          lookupResult: {
            seqNo: 7
          },
          getSelectedLookupRow: () => selectedLookupRow
        };
        field._userWasTyping = false;
        field.lookupSeqNo = 7;
        searchTextChanged = false;
      });

      it('use lookup row', () => {
        let lookupRow = field._getSelectedLookupRow(false);
        expect(lookupRow).toBe(selectedLookupRow);
      });

      it('reset when popup is closed', () => {
        field.popup = null;
        expect(field._getSelectedLookupRow(false)).toBe(null);
      });

      it('reset when user was typing or search-text has changed', () => {
        field._userWasTyping = true;
        expect(field._getSelectedLookupRow(true)).toBe(null);
      });

      it('reset when lookup result is out-dated', () => {
        field.lookupSeqNo = 8;
        expect(field._getSelectedLookupRow(false)).toBe(null);
      });

    });

    // test for ticket #228288
    it('adds CSS class from selected lookup-row to field', () => {
      let field = createFieldWithLookupCall();
      expect(strings.hasText(field.cssClass)).toBe(false);
      field.setValue(1);
      jasmine.clock().tick(500);
      expect(field.cssClass).toEqual('foo');
      field.setValue(null);
      jasmine.clock().tick(500);
      expect(strings.hasText(field.cssClass)).toBe(false);
    });

    it('does not trigger unnecessary property change event for cssClass when lookup-row is selected', () => {
      let eventCount = 0;
      let field = scout.create(SmartField, {
        parent: session.desktop,
        lookupCall: {
          objectType: PrepopulatedLookupCall,
          lookupRows: [{
            objectType: LookupRow,
            key: 1,
            text: 'hi'
          }, {
            objectType: LookupRow,
            key: 2,
            text: 'hello',
            cssClass: 'cls'
          }]
        }
      });
      field.on('propertyChange:cssClass', () => eventCount++);
      expect(field.cssClass).toBe(null);
      field.setValue(1);
      jasmine.clock().tick(500);
      expect(field.cssClass).toBe(null);
      expect(eventCount).toBe(0);

      field.setValue(2);
      jasmine.clock().tick(500);
      expect(field.cssClass).toBe('cls');
      expect(eventCount).toBe(1);
    });
  });

  describe('lookupCall', () => {

    it('should be cloned and prepared for each lookup', () => {
      let templatePropertyValue = 11;
      let preparedPropertyValue = 22;
      let eventCounter = 0;
      let field = createFieldWithLookupCall({}, {
        customProperty: templatePropertyValue,
        _dataToLookupRow: function(data) { // overwrite mapping function to use the custom property
          return scout.create(LookupRow, {
            key: data[0],
            text: data[1] + this.customProperty
          });
        }
      });
      field.on('prepareLookupCall', event => {
        expect(event.lookupCall['customProperty']).toBe(templatePropertyValue);
        expect(event.lookupCall.id).not.toBe(field.lookupCall.id);
        expect(event.type).toBe('prepareLookupCall');
        expect(event.source).toBe(field);

        event.lookupCall['customProperty'] = preparedPropertyValue; // change property for this call. Must not have any effect on the next call
        eventCounter++;
      });

      field.setValue(1); // triggers lookup call by key
      jasmine.clock().tick(500);
      expect(field.value).toBe(1);
      expect(field.displayText).toBe('Foo' + preparedPropertyValue);

      field._acceptByText(false, 'Bar'); // triggers lookup call by text
      jasmine.clock().tick(500);
      expect(field.value).toBe(2);
      expect(field.displayText).toBe('Bar' + preparedPropertyValue);

      expect(eventCounter).toBe(2);
    });

  });

  describe('lookup', () => {

    it('should increase lookupSeqNo when a lookup is executed', () => {
      let field = createFieldWithLookupCall();
      field.render();
      field.$field.focus();
      expect(field.lookupSeqNo).toBe(0);
      field._lookupByTextOrAll(false, 'Bar');
      jasmine.clock().tick(500);
      expect(field.lookupSeqNo).toBe(1);
      expect(field.popup.lookupResult.seqNo).toBe(1); // seqNo must be set on the lookupResult of the popup
    });

    it('should set error status when result has an exception', () => {
      let field = createFieldWithLookupCall();
      field._lookupByTextOrAllDone({
        queryBy: QueryBy.ALL,
        lookupRows: [],
        exception: 'a total disaster'
      });
      expect(field.errorStatus.severity).toBe(Status.Severity.ERROR);
      expect(field.errorStatus.message).toBe('a total disaster');
    });

    it('_executeLookup should always remove lookup-status (but not the error-status)', () => {
      let field = createFieldWithLookupCall();
      let lookupStatus = Status.warning({
        message: 'bar'
      });
      let errorStatus = Status.error({
        message: 'foo'
      });
      field.setLookupStatus(lookupStatus);
      field.setErrorStatus(errorStatus);
      field._executeLookup(field.lookupCall.cloneForKey(1));
      jasmine.clock().tick(500);
      expect(field.errorStatus).toBe(errorStatus);
      expect(field.lookupStatus).toBe(null);
    });

    /**
     * The hierarchical result contains 2 lookup-rows, but only leafs are counted when the numLookupRows
     * property is set which is used to determine whether or not the result is unqiue.
     */
    it('hierarchical lookup with unique result', () => {
      let field = createFieldWithLookupCall({
        browseHierarchy: true
      }, {
        hierarchical: true
      });
      let result = null;
      field.render();
      field.$field.val('Bar');
      field._lookupByTextOrAll()
        .then(result0 => {
          result = result0;
        });
      jasmine.clock().tick(500); // 2 ticks required for promises in StaticLookupCall.js
      jasmine.clock().tick(500);
      expect(result.numLookupRows).toBe(1);
      expect(result.lookupRows.length).toBe(2); // 2 because parent row has been added to result
      expect(result.uniqueMatch.text).toBe('Bar');
      expect(result.byText).toBe(true);
    });

    it('lookupByKey should set first lookup-row from result as this.lookupRow', () => {
      let field = createFieldWithLookupCall();
      let displayText = null;
      let result = field._formatValue(3) as JQuery.Promise<string>; // triggers lookup by key
      result.then(displayText0 => {
        displayText = displayText0;
      });
      jasmine.clock().tick(500);
      expect(displayText).toBe('Baz');
    });

    it('lookupByKey should set a validation warning status if there was an error during processing', () => {
      let field = createFieldWithNoDataKeyLookupCall(true);
      field.setValue(4 /* non-existing key */); // triggers lookup by key
      jasmine.clock().tick(500);
      expect(field.errorStatus).toBeInstanceOf(ValidationFailedStatus);
      expect(field.errorStatus.isWarning()).toBeTrue();
    });

    it('lookupByKey should set a validation warning status if the lookup call returns no lookup row', () => {
      let field = createFieldWithNoDataKeyLookupCall(false);
      field.setValue(0); // triggers lookup by key
      jasmine.clock().tick(500);
      expect(field.errorStatus).toBeInstanceOf(ValidationFailedStatus);
      expect(field.errorStatus.isWarning()).toBeTrue();
    });

    it('lookupByKey should not set a validation warning status if lookup was aborted', async () => {
      jasmine.clock().uninstall();
      let field = createFieldWithLookupCall({}, {
        objectType: MicrotaskStaticLookupCall,
        data: [[1, 'Foo'], [2, 'Bar', 1]]
      });
      field.setValue(1);
      field.setValue(2);
      await field.when('propertyChange:displayText');
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(2);
      expect(field.displayText).toBe('Bar');

      // Check again after catch() in _formatValue has been executed
      await sleep();
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(2);
      expect(field.displayText).toBe('Bar');
    });

    it('lookupByKey should not set a validation warning status if a second setValue call sets a valid value with setTimeout', async () => {
      jasmine.clock().uninstall();
      let field = createFieldWithLookupCall({}, {
        objectType: MicrotaskStaticLookupCall
      });
      field.setValue(1);
      await sleep();
      (field.lookupCall as StaticLookupCall<any>).data = [[1, 'Foo'], [2, 'Bar', 1]];
      field.setValue(2);

      await field.when('propertyChange:displayText');
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(2);
      expect(field.displayText).toBe('Bar');
    });

    it('lookupByKey should not set a validation warning status if first lookup was aborted and second still running', async () => {
      jasmine.clock().uninstall();
      let field = createFieldWithLookupCall({}, {
        objectType: DelayedStaticLookupCall,
        data: [[1, 'Foo'], [2, 'Bar', 1]]
      });
      field.setValue(1);
      field.setValue(2); // aborts previous set value
      let lookupCall = (field._currentLookupCall as DelayedStaticLookupCall<any>);
      await sleep(); // triggers abort, catch in _formatValue may not be executed yet
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(2);
      expect(field.displayText).toBe('');

      lookupCall.resolve(); // Executes lookup -> triggers catch in _formatValue, updateDisplayTextPending is not true anymore -> don't set invalid text because lookup call is aborted
      await field.when('propertyChange:displayText');
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(2);
      expect(field.displayText).toBe('Bar');
    });

    function createFieldWithNoDataKeyLookupCall(rejectPromise: boolean): SmartField<any> {
      return scout.create(SmartField, {
        parent: session.desktop,
        lookupCall: {
          objectType: NoDataKeyLookupCall,
          rejectPromise: rejectPromise
        }
      });
    }

    class NoDataKeyLookupCall extends LookupCall<number> {
      rejectPromise: boolean;

      override _getByKey(key: number): JQuery.Promise<LookupResult<number>> {
        let deferred = $.Deferred();
        setTimeout(() => {
          if (this.rejectPromise) {
            deferred.reject();
          } else {
            deferred.resolve({
              queryBy: QueryBy.KEY,
              lookupRows: []
            });
          }
        }, 0);
        return deferred.promise();
      }
    }
  });

  describe('touch / embed', () => {

    it('must clone properties required for embedded field', () => {
      let field = createFieldWithLookupCall({
        touchMode: true,
        activeFilter: 'TRUE',
        activeFilterEnabled: true,
        activeFilterLabels: ['a', 'b', 'c'],
        browseLoadIncremental: true
      });
      let embedded = field.clone({
        parent: session.desktop
      });
      expect(embedded.activeFilter).toBe('TRUE');
      expect(embedded.activeFilterEnabled).toBe(true);
      expect(embedded.activeFilterLabels).toEqual(['a', 'b', 'c']);
      expect(embedded.browseLoadIncremental).toBe(true);
    });

    it('_copyValuesFromField', () => {
      let touchField = createFieldWithLookupCall();
      let embeddedField = touchField.clone({
        parent: session.desktop
      });
      embeddedField.setLookupRow(scout.create((LookupRow<number>), {
        key: 123,
        text: 'baz'
      }));
      embeddedField.setErrorStatus(Status.error({
        message: 'bar'
      }));
      embeddedField.setDisplayText('Foo');

      touchField._copyValuesFromField(embeddedField);

      expect(touchField.lookupRow.text).toBe('baz');
      expect(touchField.errorStatus.message).toBe('bar');
      expect(touchField.displayText).toBe('Foo');
    });

  });

  describe('searchRequired', () => {

    it('opens popup if search available and searchRequired=true', () => {
      let field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.setSearchRequired(true);
      field.setDisplayText('Fo'); // DummyLookupCall contains row named 'Foo'.
      field.openPopup();
      jasmine.clock().tick(500);

      expect(field.isPopupOpen()).toBe(true);
      expect(findTableProposals()).toEqual(['Foo']);
      expect(field.lookupStatus).toBe(null);
    });

    it('opens popup if no search available and searchRequired=false', () => {
      let field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.openPopup();
      jasmine.clock().tick(500);

      expect(field.isPopupOpen()).toBe(true);
      expect(findTableProposals()).toEqual(['Foo', 'Bar', 'Baz']);
    });

    it('has no popup if no search available and searchRequired=true', () => {
      let field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.setSearchRequired(true);
      field.openPopup();
      jasmine.clock().tick(500);

      expect(field.isPopupOpen()).toBe(false);
      expect(field.lookupStatus.code).toBe(SmartField.ErrorCode.SEARCH_REQUIRED);
    });

    it('has empty popup if no search available and searchRequired=true and touch', () => {
      let field = createFieldWithLookupCall({
        touchMode: true
      });
      field.render();
      field.setSearchRequired(true);
      field.openPopup();
      jasmine.clock().tick(500);

      expect(field.isPopupOpen()).toBe(true);
      expect(findTableProposals().length).toBe(0);
      expect(field.lookupStatus.code).toBe(SmartField.ErrorCode.SEARCH_REQUIRED);
    });

  });

  describe('maxBrowseRowCount', () => {

    it('default - don\'t limit lookup rows', () => {
      let field = createFieldWithLookupCall();
      expect(field.browseMaxRowCount).toBe(100);
      field.render();
      field.$field.focus();
      let result = {
        queryBy: QueryBy.ALL,
        lookupRows: [1, 2, 3, 4, 5]
      };
      // @ts-expect-error
      field._lookupByTextOrAllDone(result);
      expect(result.lookupRows.length).toBe(5); // no limit required
      let popup = field.popup as SmartFieldPopup<number>;
      expect(popup.proposalChooser.status).toBe(null);
    });

    it('limit lookup rows', () => {
      let field = createFieldWithLookupCall({
        browseMaxRowCount: 3
      });
      field.render();
      field.$field.focus();
      let result = {
        queryBy: QueryBy.ALL,
        lookupRows: [1, 2, 3, 4, 5]
      };
      // @ts-expect-error
      field._lookupByTextOrAllDone(result);
      expect(result.lookupRows.length).toBe(3);
      expect(result.lookupRows[2]).toBe(3); // last element in array should be '3'
      let popup = field.popup as SmartFieldPopup<number>;
      expect(popup.proposalChooser.status.severity).toBe(Status.Severity.INFO);
    });

  });

  describe('aboutToBlurByMouseDown', () => { // see ticket #228888

    it('should not perform lookup for search by text', () => {
      let field = createFieldWithLookupCall();
      let eventTriggered = false;
      field.render();
      field.on('acceptInput', () => {
        eventTriggered = true;
      });
      field.$field.focus();

      field.setValue(1);
      jasmine.clock().tick(300);
      expect(field.displayText).toBe('Foo');

      field.$field.val('search!');
      field._userWasTyping = true;
      field.aboutToBlurByMouseDown(undefined);
      jasmine.clock().tick(300);

      // test if _acceptByText has been called with sync=true
      // this should reset the display text and trigger the acceptInput event
      expect(field.displayText).toBe('Foo');
      expect(field.$field.val()).toBe('Foo');
      expect(field._lastSearchText).toBe(null);
      expect(eventTriggered).toBe(true);
    });

  });

  describe('_onFieldKeyDown', () => {

    beforeEach(() => {
      field = createFieldWithLookupCall();
    });

    it('must update flag _userWasTyping', () => {
      // initial-state
      expect(field._userWasTyping).toBe(false);

      // send a regular key-press (no navigation)
      // @ts-expect-error
      field._onFieldKeyDown({
        which: keys.A
      });
      expect(field._userWasTyping).toBe(true);

      // when the display text is set, reset the userWasTyping flag
      // this is especially important in the remote case where the
      // server may send a new display text as a result of execFormatValue
      // in that case we don't want the SmartField to start a new search
      // by text, because the user has not typed anything into the field.
      field.setDisplayText('foo');
      expect(field.displayText).toEqual('foo');
      expect(field._userWasTyping).toBe(false);
    });

  });

  describe('_onFieldKeyUp', () => {

    beforeEach(() => {
      field = createFieldWithLookupCall();
    });

    it('does not call openPopup() when TAB, CTRL or ALT has been pressed', () => {
      field.render();
      field.openPopup = browse => {
        return null;
      };

      let keyEvents = [{
        which: keys.TAB
      }, {
        ctrlKey: true,
        which: keys.A
      }, {
        altKey: true,
        which: keys.A
      }];

      spyOn(field, 'openPopup');
      keyEvents.forEach(event => {
        // @ts-expect-error
        field._onFieldKeyUp(event);
      });
      expect(field.openPopup).not.toHaveBeenCalled();
    });

    it('calls _lookupByTextOrAll() when a character key has been pressed', () => {
      field.render();
      field._pendingOpenPopup = true;
      field._lookupByTextOrAll = () => {
        return null;
      };
      let event = {
        which: keys.A
      };
      spyOn(field, '_lookupByTextOrAll').and.callThrough();
      // @ts-expect-error
      field._onFieldKeyUp(event);
      expect(field._lookupByTextOrAll).toHaveBeenCalled();
    });

    /**
     * This should be a Selenium test, but since the key events in Selenium are different from
     * key events generated by a human being, we cannot reproduce the problem that way. Thus this
     * test simply checks if the _lookupByTextOrAll() function handles the case correctly. When
     * the test fails for some reason you should test the case described in ticket #226643 by
     * yourself in a running Scout application.
     * <p>
     * We expect undefined, because the function simply returns in that case. Every other logical
     * branch in the function would return a promise.
     */
    it('should not perform lookup when Ctrl+A has been pressed', () => {
      field.render();
      field.setValue(1);
      jasmine.clock().tick(300);
      expect(field.lookupRow.text).toBe('Foo');

      // case 1: text from lookup-row is the same as the search-text
      field._pendingOpenPopup = true;
      expect(field._lookupByTextOrAll(false, 'Foo')).toBe(undefined);
      expect(field._pendingOpenPopup).toBe(false);

      // case 2: last search-text is the same as the search-text
      field._lastSearchText = 'Homer';
      field._pendingOpenPopup = true;
      expect(field._lookupByTextOrAll(false, 'Homer')).toBe(undefined);
      expect(field._pendingOpenPopup).toBe(false);

      // every other case should return a promise
      field._pendingOpenPopup = true;
      expect(field._lookupByTextOrAll(false, 'Marge')).not.toBe(undefined);
      expect(field._pendingOpenPopup).toBe(true);
    });

    it('should return text from lookup-row for last search-text', () => {
      field.setLookupRow(scout.create((LookupRow<number>), {
        key: 0,
        text: 'Foo'
      }));
      expect(field._getLastSearchText()).toBe('Foo');
    });

  });

  describe('_formatValue', () => {
    let lookupCall: DummyLookupCall;

    beforeEach(() => {
      lookupCall = scout.create(DummyLookupCall, {
        session: session
      });
    });

    it('uses a lookup call to format the value', () => {
      let model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall
      });
      let smartField = scout.create(SmartField, model);
      expect(smartField.displayText).toBe('');
      smartField.setValue(1);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(1);
      expect(smartField.displayText).toBe('Foo');
      smartField.setValue(2);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(2);
      expect(smartField.displayText).toBe('Bar');
    });

    it('returns empty string if value is null or undefined', () => {
      let model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall
      });
      let smartField = scout.create(SmartField, model);
      expect(smartField.displayText).toBe('');
      smartField.setValue(null);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
      smartField.setValue(undefined);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
    });

    it('aborts previous lookup call when setValue() is called multiple times', () => {

      // --- Case 1: initial value exists, then value is set to null ---

      let smartField = scout.create(SmartField, {
        parent: session.desktop,
        lookupCall,
        value: 1
      });
      expect(smartField.value).toBe(1);
      expect(smartField.displayText).toBe(null);

      smartField.setValue(null);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');

      jasmine.clock().tick(300);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');

      // --- Case 2: value is set to '1', then to null again ---

      smartField = scout.create(SmartField, {
        parent: session.desktop,
        lookupCall
      });
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');

      smartField.setValue(1);
      expect(smartField.value).toBe(1);
      expect(smartField.displayText).toBe('');

      smartField.setValue(null);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');

      jasmine.clock().tick(300);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');

      // --- Case 3: value is set to '1', then to '2' ---

      smartField = scout.create(SmartField, {
        parent: session.desktop,
        lookupCall
      });
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');

      smartField.setValue(1);
      expect(smartField.value).toBe(1);
      expect(smartField.displayText).toBe('');

      smartField.setValue(2);
      expect(smartField.value).toBe(2);
      expect(smartField.displayText).toBe('');

      jasmine.clock().tick(300);
      expect(smartField.value).toBe(2);
      expect(smartField.displayText).toBe('Bar');
    });
  });

  describe('multiline', () => {

    let lookupCall;

    beforeEach(() => {
      lookupCall = scout.create(DummyLookupCall, {
        session: session,
        multiline: true
      });
    });

    it('_readSearchText() must concat text of input element and additional lines - required for acceptInput', () => {
      let model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall,
        value: 1
      }) as FullModelOf<SmartField<number>>;
      let smartField = scout.create(SpecSmartField, model);
      jasmine.clock().tick(300);
      smartField.render();
      expect(smartField._readDisplayText()).toEqual('1:Foo');
      expect(smartField._readSearchText()).toEqual('1:Foo\n2:Foo');

      smartField.$field.val('1:Meep');
      expect(smartField._readDisplayText()).toEqual('1:Meep');
      expect(smartField._readSearchText()).toEqual('1:Meep\n2:Foo');
    });

    it('multi-line lookupcall on single-line field', () => {
      // will be displayed multi-line in proposal, but single-line as display text
      let model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall,
        value: 1
      }) as FullModelOf<SmartField<number>>;
      let smartField = scout.create(SpecSmartField, model);
      jasmine.clock().tick(300);
      smartField.render();
      expect(smartField.value).toBe(1);
      expect(fields.valOrText(smartField.$field)).toBe('1:Foo');
      expect(smartField.displayText).toEqual('1:Foo\n2:Foo');
    });

    it('multi-line lookupcall on multi-line field', () => {
      // _additionalLines will be rendered to _$multilineField
      let model = helper.createFieldModel('SmartFieldMultiline', session.desktop, {
        lookupCall: lookupCall,
        value: 1
      }) as FullModelOf<SmartField<number>>;
      let smartFieldMultiline = scout.create(SmartFieldMultiline, model);
      jasmine.clock().tick(300);
      smartFieldMultiline.render();
      expect(smartFieldMultiline.value).toBe(1);
      expect(fields.valOrText(smartFieldMultiline.$field)).toBe('1:Foo');
      // @ts-expect-error
      expect(smartFieldMultiline._$multilineLines.text()).toEqual('2:Foo');
    });
  });

  describe('label', () => {

    it('focuses the field when clicked', () => {
      let smartField = scout.create(SmartField, {
        parent: session.desktop,
        label: 'label',
        lookupCall: 'DummyLookupCall'
      });
      smartField.render();
      JQueryTesting.triggerClick(smartField.$label);
      jasmine.clock().tick(500);
      expect(smartField.popup).toBeTruthy();

      smartField.popup.close();
    });

    it('focuses the field when clicked (also in multiline mode)', () => {
      let smartField = scout.create(SmartFieldMultiline, {
        parent: session.desktop,
        label: 'label',
        lookupCall: 'DummyLookupCall'
      });
      smartField.render();
      JQueryTesting.triggerClick(smartField.$label);
      jasmine.clock().tick(500);
      expect(smartField.popup).toBeTruthy();

      smartField.popup.close();
    });

  });

  describe('column descriptors', () => {
    it('with default lookup column at first position renders lookup row column at first position', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: ColumnDescriptorDummyLookupCall
      });

      field.columnDescriptors = [{
        // First column (for lookup row text) is not visible
      }, {
        propertyName: 'column1',
        width: 120
      }, {
        propertyName: 'column2',
        width: 100
      }];

      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.$field.val('Bar');
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      let popup = field.popup as SmartFieldPopup<any>;
      expect(popup.proposalChooser.content.rows[0].cells[0].text).toBe('Bar');
      expect(popup.proposalChooser.content.rows[0].cells[1].text).toBe('Bar column1');
      expect(popup.proposalChooser.content.rows[0].cells[2].text).toBe('Bar column2');
    });

    it('with default lookup column in the middle renders lookup row column in the middle', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: ColumnDescriptorDummyLookupCall
      });

      field.columnDescriptors = [{
        propertyName: 'column1',
        width: 120
      }, {
        // First column (for lookup row text) is not visible
      }, {
        propertyName: 'column2',
        width: 100,
        cssClass: 'css-column2'
      }];

      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.$field.val('Bar');
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      let popup = field.popup as SmartFieldPopup<any>;
      expect(popup.proposalChooser.content.rows[0].cells[0].text).toBe('Bar column1');
      expect(popup.proposalChooser.content.rows[0].cells[1].text).toBe('Bar');
      expect(popup.proposalChooser.content.rows[0].cells[2].text).toBe('Bar column2');
      expect(popup.proposalChooser.content.rows[0].cells[2].cssClass).toBe('css-column2');
    });
  });

  describe('proposalChooser', () => {

    it('does not support mouse move selection', () => {
      const field = createFieldWithLookupCall();
      field.render();
      field.$field.focus();
      field._lookupByTextOrAllDone({
        queryBy: QueryBy.ALL,
        // @ts-expect-error
        lookupRows: [1, 2, 3, 4, 5]
      });
      expect((field.popup as SmartFieldPopup<number>).proposalChooser.content.selectionHandler.mouseMoveSelectionEnabled).toBeFalse();
    });
  });

  describe('aria properties', () => {

    it('has aria role combobox', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: ColumnDescriptorDummyLookupCall
      });
      field.render();
      expect(field.$field).toHaveAttr('role', 'combobox');
    });

    it('has aria-labelledby set', () => {
      let smartField = scout.create(SmartField, {
        parent: session.desktop,
        label: 'test'
      });
      smartField.render();
      expect(smartField.$field.attr('aria-labelledby')).toBeTruthy();
      expect(smartField.$field.attr('aria-labelledby')).toBe(smartField.$label.attr('id'));
      expect(smartField.$field.attr('aria-label')).toBeFalsy();
    });

    it('has aria-labelledby set in multiline mode', () => {
      let smartField = scout.create(SmartFieldMultiline, {
        parent: session.desktop,
        label: 'label'
      });
      smartField.render();
      expect(smartField.$field.attr('aria-labelledby')).toBeTruthy();
      expect(smartField.$field.attr('aria-labelledby')).toBe(smartField.$label.attr('id'));
      expect(smartField.$field.attr('aria-label')).toBeFalsy();
    });

    it('has aria-describedby description for its functionality', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: ColumnDescriptorDummyLookupCall
      });
      field.render();
      let $fieldDescription = field.$container.find('#desc' + field.id + '-func-desc');
      expect(field.$field.attr('aria-describedby')).toBeTruthy();
      expect(field.$field.attr('aria-describedby')).toBe($fieldDescription.eq(0).attr('id'));
      expect(field.$field.attr('aria-description')).toBeFalsy();
    });

    it('has a non empty status container that lists count of available options', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: ColumnDescriptorDummyLookupCall
      });
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
      let field = createFieldWithLookupCall({}, {
        objectType: ColumnDescriptorDummyLookupCall
      });
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
      let field = createFieldWithLookupCall({}, {
        objectType: ColumnDescriptorDummyLookupCall
      });
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
      let field = createFieldWithLookupCall({}, {
        objectType: ColumnDescriptorDummyLookupCall
      });
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
