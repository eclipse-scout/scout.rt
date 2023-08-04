/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ListBox, ListBoxModel, ListBoxTableAccessibilityRenderer, LookupCall, LookupResult, LookupRow, QueryBy, scout, Status} from '../../../../src/index';
import {DummyLookupCall, EmptyDummyLookupCall, ErroneousLookupCall, FormSpecHelper, LanguageDummyLookupCall} from '../../../../src/testing/index';
import {InitModelOf, ObjectOrModel} from '../../../../src/scout';
import $ from 'jquery';

describe('ListBox', () => {
  let session: SandboxSession, field: ListBox<any>, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new ListBox();
    helper = new FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  class SpecListBox extends ListBox<any> {
    override _lookupByAllDone(result: LookupResult<any>) {
      super._lookupByAllDone(result);
    }

    override _executeLookup(lookupCall: LookupCall<any>, abortExisting?: boolean): JQuery.Promise<LookupResult<any>> {
      return super._executeLookup(lookupCall, abortExisting);
    }
  }

  function createFieldWithLookupCall(model?: ListBoxModel<any>, lookupCallModel?: ObjectOrModel<LookupCall<any>> | string): SpecListBox {
    lookupCallModel = $.extend({
      objectType: DummyLookupCall
    }, lookupCallModel);

    model = $.extend({}, {
      parent: session.desktop,
      lookupCall: lookupCallModel
    }, model);
    let box = scout.create(SpecListBox, model as InitModelOf<ListBox<any>>);
    box.render();
    return box;
  }

  describe('general behavior', () => {
    it('defaults', () => {
      expect(field.value).toEqual([]);
      expect(field.displayText).toBe(null);
      expect(field.getCheckedLookupRows()).toEqual([]);
    });

    it('init LookupCall when configured as string', () => {
      field = createFieldWithLookupCall();
      expect(field.lookupCall instanceof DummyLookupCall).toBe(true);
    });

    it('LookupCall can be prepared if value is set explicitly', done => {
      let box = scout.create(ListBox, {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall'
      });

      let lookupPrepared = box.when('prepareLookupCall');
      let lookupDone = box.when('lookupCallDone');
      box.refreshLookup();
      jasmine.clock().tick(500);

      $.promiseAll([lookupPrepared, lookupDone]).then(event => {
        expect(event.lookupCall instanceof DummyLookupCall).toBe(true);
      })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(500);
    });

    it('LookupCall can be prepared if value is configured', done => {
      let box = scout.create(ListBox, {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall',
        value: 3
      });

      let lookupPrepared = box.when('prepareLookupCall');
      let lookupDone = box.when('lookupCallDone');
      box.render();
      jasmine.clock().tick(500);

      $.promiseAll([lookupPrepared, lookupDone]).then(event => {
        expect(event.lookupCall instanceof DummyLookupCall).toBe(true);
        expect(box.getCheckedLookupRows().length).toBe(1);
      })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(500);
    });

    it('when setValue is called, load and set the correct lookup rows', () => {
      field = createFieldWithLookupCall();
      field.setValue([1, 3]);
      jasmine.clock().tick(300);
      expect(field.value).toEqual([1, 3]);
      expect(field.displayText).toBe('Foo, Baz');
      expect(field.getCheckedLookupRows().length).toBe(2);
      expect(field.getCheckedLookupRows()[0].key).toBe(1);
      expect(field.getCheckedLookupRows()[1].key).toBe(3);

      // set the value to null again
      field.setValue(null);
      expect(field.getCheckedLookupRows()).toEqual([]);
      expect(field.value).toEqual([]);
      expect(field.displayText).toBe('');

      field.setValue([2]);
      jasmine.clock().tick(300);
      expect(field.displayText).toBe('Bar');
      expect(field.value).toEqual([2]);
      expect(field.getCheckedLookupRows().length).toBe(1);
      expect(field.getCheckedLookupRows()[0].key).toBe(2);
    });

  });

  describe('clear', () => {

    it('clears the value', () => {
      let field = createFieldWithLookupCall();

      field.setValue([1, 2]);
      jasmine.clock().tick(500);

      expect(field.value).toEqual([1, 2]);
      expect(field.displayText).toBe('Foo, Bar');
      expect(field.table.checkedRows().length).toBe(2);
      expect(field.getCheckedLookupRows().length).toBe(2);

      field.clear();
      jasmine.clock().tick(500);
      expect(field.value).toEqual([]);
      expect(field.displayText).toBe('');
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.getCheckedLookupRows()).toEqual([]);
    });

    it('uncheck all rows', () => {
      let field = createFieldWithLookupCall();
      jasmine.clock().tick(500);

      field.setValue([1, 2, 3]);
      jasmine.clock().tick(500);
      expect(field.value).toEqual([1, 2, 3]);
      expect(field.table.checkedRows().length).toBe(3);
      expect(field.displayText).toBe('Foo, Bar, Baz');

      field.clear();
      jasmine.clock().tick(500);
      expect(field.value).toEqual([]);
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.displayText).toBe('');
    });
  });

  describe('setEnabled', () => {
    it('should disable check rows', () => {
      let field = createFieldWithLookupCall();
      jasmine.clock().tick(500);

      field.setEnabled(false);
      field.table.checkAll();
      jasmine.clock().tick(500);
      expect(field.value).toEqual([]);
      expect(field.getCheckedLookupRows()).toEqual([]);
      expect(field.displayText).toBe('');
      expect(field.table.checkedRows().length).toBe(0);

      field.table.checkRows(field.table.rows[2]);
      expect(field.value).toEqual([]);
      field.table.checkRows(field.table.rows[2], {checkOnlyEnabled: false});
      expect(field.value).toEqual([3]);

      field.setValue([1]);
      expect(field.value).toEqual([1]);
    });
  });

  describe('lookupCall', () => {

    it('switching should refill table', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: LanguageDummyLookupCall
      });

      field.setValue([100, 500]);
      jasmine.clock().tick(300);
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('English, Swiss-German');
      expect(field.table.rows.length).toBe(5);
      expect(field.table.checkedRows().length).toBe(2);

      let newLookupCall = scout.create(DummyLookupCall, {
        session: session
      });
      field.setLookupCall(newLookupCall);
      jasmine.clock().tick(300);
      // dont change value when lookupCall changes
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('');
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.table.rows.length).toBe(3);
    });

    it('switching to a lookup call returning no results should clear table', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: DummyLookupCall
      });
      field.setValue([100, 500]);
      jasmine.clock().tick(300);
      expect(field.table.rows.length).toBe(3);

      let newLookupCall = scout.create(EmptyDummyLookupCall, {
        session: session
      });
      field.setLookupCall(newLookupCall);
      jasmine.clock().tick(300);
      // dont change value when lookupCall changes
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('');
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.table.rows.length).toBe(0);
    });

    it('switching to a lookup call without a lookup error should remove the error', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: ErroneousLookupCall
      });
      jasmine.clock().tick(300);
      expect(field.lookupStatus).not.toBe(null);

      let newLookupCall = scout.create(DummyLookupCall, {
        session: session
      });
      field.setLookupCall(newLookupCall);
      jasmine.clock().tick(300);
      expect(field.lookupStatus).toBe(null);
      expect(field.table.rows.length).toBe(3);
    });

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

      field.setValue([1]); // triggers lookup call by key
      jasmine.clock().tick(500);
      expect(field.value).toEqual([1]);
      expect(field.displayText).toBe('Foo' + preparedPropertyValue);

      field.setValue(null);
      jasmine.clock().tick(500);

      field.table.checkRows(field.table.rows[2]);
      jasmine.clock().tick(500);

      expect(field.value).toEqual([3]);
      expect(field.displayText).toBe('Baz' + preparedPropertyValue);

      expect(eventCounter).toBe(1);
    });
  });

  describe('lookup', () => {
    it('should set lookup status when result has an exception', () => {
      let field = createFieldWithLookupCall();
      field._lookupByAllDone({
        queryBy: QueryBy.ALL,
        lookupRows: [],
        exception: 'a total disaster'
      });
      expect(field.lookupStatus.severity).toBe(Status.Severity.WARNING);
      expect(field.lookupStatus.message).toBe('a total disaster');
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
      field._executeLookup(field.lookupCall.cloneForAll());
      jasmine.clock().tick(500);
      expect(field.errorStatus).toBe(errorStatus);
      expect(field.lookupStatus).toBe(null);
    });

    it('should be executed when lookup call is set', () => {
      let field = createFieldWithLookupCall();
      jasmine.clock().tick(500);

      expect(field.table.rows.length).toBe(3);
    });

    it('should not set an error status if lookup returned no results', () => {
      let field = createFieldWithLookupCall({}, {objectType: EmptyDummyLookupCall});
      jasmine.clock().tick(300);
      expect(field.table.rows.length).toBe(0);
      expect(field.errorStatus).toBe(null);
    });
  });

  describe('value', () => {

    it('should be synchronized when rows are checked', () => {
      let field = createFieldWithLookupCall();
      jasmine.clock().tick(500);

      field.table.checkAll();
      jasmine.clock().tick(300);
      expect(field.value).toEqual([1, 2, 3]);
      expect(field.displayText).toBe('Foo, Bar, Baz');
      expect(field.table.checkedRows().length).toBe(3);

      field.table.uncheckAll();
      jasmine.clock().tick(300);
      expect(field.value).toEqual([]);
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.displayText).toBe('');

      field.table.checkRow(field.table.rows[1]);
      jasmine.clock().tick(500);
      expect(field.value).toEqual([2]);
      expect(field.displayText).toBe('Bar');
      expect(field.table.checkedRows().length).toBe(1);
    });

    it('updates saveNeeded when changed', () => {
      let listBox = createFieldWithLookupCall();
      expect(listBox.saveNeeded).toBe(false);

      listBox.setValue([1]);
      expect(listBox.saveNeeded).toBe(true);

      listBox.setValue([2]);
      expect(listBox.saveNeeded).toBe(true);

      listBox.setValue(null);
      expect(listBox.saveNeeded).toBe(false);

      listBox.setValue([1, 2]);
      expect(listBox.saveNeeded).toBe(true);

      listBox.markAsSaved();
      expect(listBox.saveNeeded).toBe(false);

      listBox.setValue([1]);
      expect(listBox.saveNeeded).toBe(true);

      listBox.setValue([1, 2]);
      expect(listBox.saveNeeded).toBe(false);
    });
  });

  describe('_formatValue', () => {
    let lookupCall;

    beforeEach(() => {
      lookupCall = scout.create(DummyLookupCall, {
        session: session
      });
    });

    it('uses a lookup call to format the value', () => {
      let model = helper.createFieldModel(ListBox, session.desktop, {
        lookupCall: lookupCall
      });
      let listBox = scout.create(ListBox, model);
      expect(listBox.displayText).toBe('');
      listBox.setValue([1]);
      jasmine.clock().tick(300);
      expect(listBox.value).toEqual([1]);
      expect(listBox.displayText).toBe('Foo');
      listBox.setValue([2]);
      jasmine.clock().tick(300);
      expect(listBox.value).toEqual([2]);
      expect(listBox.displayText).toBe('Bar');
    });

    it('returns empty string if value is null or undefined', () => {
      let model = helper.createFieldModel(ListBox, session.desktop, {
        lookupCall: lookupCall
      });
      let listBox = scout.create(ListBox, model);
      expect(listBox.displayText).toBe('');

      listBox.setValue(null);
      jasmine.clock().tick(300);
      expect(listBox.value).toEqual([]);
      expect(listBox.displayText).toBe('');

      listBox.setValue(undefined);
      jasmine.clock().tick(300);
      expect(listBox.value).toEqual([]);
      expect(listBox.displayText).toBe('');
    });

  });

  describe('aria properties', () => {

    it('has aria-labelledby set', () => {
      let listBox = scout.create(ListBox, {
        parent: session.desktop,
        label: 'hello'
      });
      listBox.render();
      expect(listBox.$field.attr('aria-labelledby')).toBeTruthy();
      expect(listBox.$field.attr('aria-labelledby')).toBe(listBox.$label.attr('id'));
      expect(listBox.$field.attr('aria-label')).toBeFalsy();
    });

    it('has a ListBoxTableAccessibilityRenderer set as its accessibility renderer', () => {
      let listBox = createFieldWithLookupCall();
      expect(listBox.table.accessibilityRenderer instanceof ListBoxTableAccessibilityRenderer).toBe(true);
    });

    it('has a table with aria role listbox', () => {
      let listBox = createFieldWithLookupCall();
      expect(listBox.table.$container).toHaveAttr('role', 'listbox');
    });

    it('has a data section with role group', () => {
      let listBox = createFieldWithLookupCall();
      expect(listBox.table.$data).toHaveAttr('role', 'group');
    });

    it('has rows with aria role option', () => {
      let listBox = createFieldWithLookupCall();
      listBox.table.rows.forEach(row => {
        expect(row.$row).toHaveAttr('role', 'option');
      });
    });
  });
});
