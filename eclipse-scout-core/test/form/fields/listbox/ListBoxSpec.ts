/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Code, CodeLookupCall, codes, CodeType, ListBox, ListBoxAriaRules, ListBoxModel, LookupCall, LookupResult, LookupRow, QueryBy, scout, StaticLookupCall, Status, Table} from '../../../../src/index';
import {DummyLookupCall, EmptyDummyLookupCall, ErroneousLookupCall, FormSpecHelper, LanguageDummyLookupCall, TableSpecHelper} from '../../../../src/testing/index';
import {InitModelOf, ObjectOrModel} from '../../../../src/scout';
import $ from 'jquery';

describe('ListBox', () => {
  let session: SandboxSession, field: ListBox<any>, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new ListBox();
    helper = new FormSpecHelper(session);
    codes.add([{
      id: 'ListBoxSpec_CodeType',
      objectType: CodeType,
      codes: [
        {
          id: 1,
          objectType: Code,
          texts: {
            'de': 'value 1'
          }
        },
        {
          id: 2,
          objectType: Code,
          texts: {
            'de': 'value 2'
          }
        }
      ]
    }
    ]);
  });

  afterEach(() => {
    codes.remove('ListBoxSpec_CodeType');
  });

  class SpecListBox extends ListBox<any> {
    override _lookupByAllDone(result: LookupResult<any>) {
      super._lookupByAllDone(result);
    }

    override _executeLookup(lookupCall: LookupCall<any>, abortExisting?: boolean): Promise<LookupResult<any>> {
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

      // $.promiseAll resolves to an array of results since 2 promises are passed
      $.promiseAll([lookupPrepared, lookupDone]).then(([prepareEvent]) => {
        expect(prepareEvent.lookupCall instanceof DummyLookupCall).toBe(true);
      })
        .catch(fail)
        .finally(done);
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

      // $.promiseAll resolves to an array of results since 2 promises are passed
      $.promiseAll([lookupPrepared, lookupDone]).then(([prepareEvent]) => {
        expect(prepareEvent.lookupCall instanceof DummyLookupCall).toBe(true);
        expect(box.getCheckedLookupRows().length).toBe(1);
      })
        .catch(fail)
        .finally(done);
    });

    it('when setValue is called, load and set the correct lookup rows', async () => {
      field = createFieldWithLookupCall();
      field.setValue([1, 3]);
      await field.when('lookupCallDone');
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

      // lookup was already executed, table rows are already loaded -> no new lookup call, syncing the value is synchronous
      field.setValue([2]);
      expect(field.displayText).toBe('Bar');
      expect(field.value).toEqual([2]);
      expect(field.getCheckedLookupRows().length).toBe(1);
      expect(field.getCheckedLookupRows()[0].key).toBe(2);
    });

  });

  describe('clear', () => {

    it('clears the value', async () => {
      let field = createFieldWithLookupCall();
      await field.when('lookupCallDone');

      field.setValue([1, 2]);

      expect(field.value).toEqual([1, 2]);
      expect(field.displayText).toBe('Foo, Bar');
      expect(field.table.checkedRows().length).toBe(2);
      expect(field.getCheckedLookupRows().length).toBe(2);

      field.clear();
      expect(field.value).toEqual([]);
      expect(field.displayText).toBe('');
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.getCheckedLookupRows()).toEqual([]);
    });

    it('uncheck all rows', async () => {
      let field = createFieldWithLookupCall();
      await field.when('lookupCallDone');

      field.setValue([1, 2, 3]);
      expect(field.value).toEqual([1, 2, 3]);
      expect(field.table.checkedRows().length).toBe(3);
      expect(field.displayText).toBe('Foo, Bar, Baz');

      field.clear();
      expect(field.value).toEqual([]);
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.displayText).toBe('');
    });
  });

  describe('setEnabled', () => {
    it('should disable check rows', async () => {
      let field = createFieldWithLookupCall();
      await field.when('lookupCallDone');

      field.setEnabled(false);
      field.table.checkAll();
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

    it('switching should refill table', async () => {
      let field = createFieldWithLookupCall({}, {
        objectType: LanguageDummyLookupCall
      });

      field.setValue([100, 500]);
      await field.when('lookupCallDone');
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('English, Swiss-German');
      expect(field.table.rows.length).toBe(5);
      expect(field.table.checkedRows().length).toBe(2);

      let newLookupCall = scout.create(DummyLookupCall, {
        session: session
      });
      field.setLookupCall(newLookupCall);
      await field.when('lookupCallDone');
      // dont change value when lookupCall changes
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('');
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.table.rows.length).toBe(3);
    });

    it('switching to a lookup call returning no results should clear table', async () => {
      let field = createFieldWithLookupCall({}, {
        objectType: DummyLookupCall
      });
      await field.when('lookupCallDone');
      field.setValue([100, 500]);
      expect(field.table.rows.length).toBe(3);

      let newLookupCall = scout.create(EmptyDummyLookupCall, {
        session: session
      });
      field.setLookupCall(newLookupCall);
      await field.when('lookupCallDone');
      // dont change value when lookupCall changes
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('');
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.table.rows.length).toBe(0);
    });

    it('switching to a lookup call without a lookup error should remove the error', async () => {
      let field = createFieldWithLookupCall({}, {
        objectType: ErroneousLookupCall
      });
      await field.when('lookupCallDone');
      expect(field.lookupStatus).not.toBe(null);

      let newLookupCall = scout.create(DummyLookupCall, {
        session: session
      });
      field.setLookupCall(newLookupCall);
      await field.when('lookupCallDone');
      expect(field.lookupStatus).toBe(null);
      expect(field.table.rows.length).toBe(3);
    });

    it('should be cloned and prepared for each lookup', async () => {
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
        expect(event.lookupCall).not.toBe(field.lookupCall);
        expect(event.type).toBe('prepareLookupCall');
        expect(event.source).toBe(field);

        event.lookupCall['customProperty'] = preparedPropertyValue; // change property for this call. Must not have any effect on the next call
        eventCounter++;
      });

      field.setValue([1]); // triggers lookup call by key
      await field.when('lookupCallDone');
      expect(field.value).toEqual([1]);
      expect(field.displayText).toBe('Foo' + preparedPropertyValue);

      field.setValue(null);

      field.table.checkRows(field.table.rows[2]);

      expect(field.value).toEqual([3]);
      expect(field.displayText).toBe('Baz' + preparedPropertyValue);

      expect(eventCounter).toBe(1);
    });

    it('is set to CodeLookupCall if a codeType is set', async () => {
      let listBox = scout.create(ListBox, {
        parent: session.desktop,
        codeType: 'ListBoxSpec_CodeType'
      });
      listBox.render();
      await listBox.when('lookupCallDone');
      expect(listBox.lookupCall).toBeInstanceOf(CodeLookupCall);
      expect(listBox.table.rows.length).toBe(2);
    });

    it('is set to CodeLookupCall if a codeType is set, even dynamically', async () => {
      let listBox = scout.create(ListBox, {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall'
      });
      listBox.setCodeType('ListBoxSpec_CodeType');
      listBox.render();
      await listBox.when('lookupCallDone');
      expect(listBox.lookupCall).toBeInstanceOf(CodeLookupCall);
      expect(listBox.table.rows.length).toBe(2);
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

    it('_executeLookup should always remove lookup-status (but not the error-status)', async () => {
      let field = createFieldWithLookupCall();
      let lookupStatus = Status.warning({
        message: 'bar'
      });
      let errorStatus = Status.error({
        message: 'foo'
      });
      field.setLookupStatus(lookupStatus);
      field.setErrorStatus(errorStatus);
      await field._executeLookup(field.lookupCall.cloneForAll());
      expect(field.errorStatus).toBe(errorStatus);
      expect(field.lookupStatus).toBe(null);
    });

    it('should be executed when lookup call is set', async () => {
      let field = createFieldWithLookupCall();
      await field.when('lookupCallDone');

      expect(field.table.rows.length).toBe(3);
    });

    it('should not set an error status if lookup returned no results', async () => {
      let field = createFieldWithLookupCall({}, {objectType: EmptyDummyLookupCall});
      await field.when('lookupCallDone');
      expect(field.table.rows.length).toBe(0);
      expect(field.errorStatus).toBe(null);
    });
  });

  describe('value', () => {

    it('should be synchronized when rows are checked', async () => {
      let field = createFieldWithLookupCall();
      await field.when('lookupCallDone');

      field.table.checkAll();
      expect(field.value).toEqual([1, 2, 3]);
      expect(field.displayText).toBe('Foo, Bar, Baz');
      expect(field.table.checkedRows().length).toBe(3);

      field.table.uncheckAll();
      expect(field.value).toEqual([]);
      expect(field.table.checkedRows().length).toBe(0);
      expect(field.displayText).toBe('');

      field.table.checkRow(field.table.rows[1]);
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

    it('uses a lookup call to format the value', async () => {
      let model = helper.createFieldModel(ListBox, session.desktop, {
        lookupCall: lookupCall
      });
      let listBox = scout.create(ListBox, model);
      listBox.render();
      await listBox.when('lookupCallDone');
      expect(listBox.displayText).toBe('');
      listBox.setValue([1]);
      expect(listBox.value).toEqual([1]);
      expect(listBox.displayText).toBe('Foo');
      listBox.setValue([2]);
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
      expect(listBox.value).toEqual([]);
      expect(listBox.displayText).toBe('');

      listBox.setValue(undefined);
      expect(listBox.value).toEqual([]);
      expect(listBox.displayText).toBe('');
    });
  });

  describe('table', () => {
    it('uses list box specific default values', () => {
      let listBox = scout.create(ListBox, {
        parent: session.desktop
      });
      expect(listBox.table.checkable).toBe(true);
      expect(listBox.table.headerVisible).toBe(false);
    });

    it('can be customized', () => {
      let listBox = scout.create(ListBox, {
        parent: session.desktop,
        table: {
          objectType: Table,
          headerVisible: true
        }
      });
      expect(listBox.table.checkable).toBe(true); // Must stay true
      expect(listBox.table.headerVisible).toBe(true);
    });
  });

  describe('aria properties', () => {

    it('has aria-labelledby set', () => {
      let listBox = scout.create(ListBox, {
        parent: session.desktop,
        label: 'hello'
      });
      listBox.render();
      expect(listBox.table.$data.attr('aria-labelledby')).toBe(listBox.$label.attr('id'));
      expect(listBox.table.$data.attr('aria-label')).toBeFalsy();
    });

    it('has listbox aria rules set', () => {
      let listBox = createFieldWithLookupCall();
      expect(listBox.table.ariaRules instanceof ListBoxAriaRules).toBe(true);
    });

    it('has a table with aria role listbox', () => {
      let listBox = createFieldWithLookupCall();
      expect(listBox.table.$data).toHaveAttr('role', 'listbox');
    });

    it('has rows with aria role option', async () => {
      let listBox = createFieldWithLookupCall();
      await listBox.when('lookupCallDone');
      expect(listBox.table.rows.length).toBeGreaterThan(0);
      listBox.table.rows.forEach(row => {
        expect(row.$row).toHaveAttr('role', 'option');
      });
    });

    it('has rows with posinset', async () => {
      let tableHelper = new TableSpecHelper(session);
      let lookupData = [];
      for (let i = 0; i < 20; i++) {
        lookupData.push([i, `${i}`]);
      }
      let listBox = scout.create(ListBox, {
        parent: session.desktop,
        lookupCall: {
          objectType: StaticLookupCall,
          data: lookupData
        }
      });
      listBox.table.setViewRangeSize(3);
      listBox.render();
      await listBox.when('lookupCallDone');
      tableHelper.assertAriaPosInSetAndSize(listBox.table.rows, 0, 20);
      tableHelper.assertNotAriaRowIndexAndCount(listBox.table);

      listBox.table.scrollTo(arrays.last(listBox.table.rows));
      tableHelper.assertAriaPosInSetAndSize(listBox.table.rows, 17, 20);
      tableHelper.assertNotAriaRowIndexAndCount(listBox.table);

      listBox.table.addFilter(row => row.lookupRow.key === 3);
      listBox.table.$rows().stop(false, true); // Finish filter animation
      tableHelper.assertAriaPosInSetAndSize(listBox.table.rows, 0, 1);
      tableHelper.assertNotAriaRowIndexAndCount(listBox.table);
    });
  });
});
