/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupCall, LookupCallModel, LookupResult, LookupRow, QueryBy, scout, Status, TreeBox, TreeBoxModel} from '../../../../src/index';
import {DummyLookupCall, EmptyDummyLookupCall, ErroneousLookupCall, FormSpecHelper, LanguageDummyLookupCall} from '../../../../src/testing/index';
import {InitModelOf} from '../../../../src/scout';

describe('TreeBox', () => {
  let session: SandboxSession, field: TreeBox<any>, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new TreeBox();
    helper = new FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  class SpecTreeBox<T> extends TreeBox<T> {
    override _lookupByAllDone(result: LookupResult<T>) {
      super._lookupByAllDone(result);
    }

    override _executeLookup(lookupCall: LookupCall<T>, abortExisting?: boolean): JQuery.Promise<LookupResult<T>> {
      return super._executeLookup(lookupCall, abortExisting);
    }
  }

  function createFieldWithLookupCall<T>(model?: TreeBoxModel<T>, lookupCallModel?: LookupCallModel<T>): SpecTreeBox<T> {
    lookupCallModel = $.extend({
      objectType: DummyLookupCall
    }, lookupCallModel);

    model = $.extend({}, {
      parent: session.desktop,
      lookupCall: lookupCallModel
    }, model);
    let box = scout.create((SpecTreeBox<T>), model as InitModelOf<TreeBox<T>>);
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
      let box = scout.create(TreeBox, {
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
      let box = scout.create(TreeBox, {
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
      expect(field.tree.checkedNodes.length).toBe(2);
      expect(field.getCheckedLookupRows().length).toBe(2);

      field.clear();
      jasmine.clock().tick(500);
      expect(field.value).toEqual([]);
      expect(field.displayText).toBe('');
      expect(field.tree.checkedNodes.length).toBe(0);
      expect(field.getCheckedLookupRows()).toEqual([]);
    });

    it('uncheck all rows', () => {
      let field = createFieldWithLookupCall();
      jasmine.clock().tick(500);

      field.setValue([1, 2, 3]);
      jasmine.clock().tick(500);
      expect(field.value).toEqual([1, 2, 3]);
      expect(field.tree.checkedNodes.length).toBe(3);
      expect(field.displayText).toBe('Foo, Bar, Baz');

      field.clear();
      jasmine.clock().tick(500);
      expect(field.value).toEqual([]);
      expect(field.tree.checkedNodes.length).toBe(0);
      expect(field.displayText).toBe('');
    });
  });

  describe('setEnabled', () => {
    it('should disable check rows', () => {
      let field = createFieldWithLookupCall();
      jasmine.clock().tick(500);

      field.setEnabled(false);
      field.tree.expandNode(field.tree.visibleNodesFlat[0]);
      field.tree.checkNodes(field.tree.visibleNodesFlat);
      jasmine.clock().tick(500);
      expect(field.value).toEqual([]);
      expect(field.getCheckedLookupRows()).toEqual([]);
      expect(field.displayText).toBe('');
      expect(field.tree.checkedNodes.length).toBe(0);

      field.tree.checkNodes(field.tree.visibleNodesFlat[2]);
      expect(field.value).toEqual([]);
      field.tree.checkNodes(field.tree.visibleNodesFlat[2], {checkOnlyEnabled: false});
      expect(field.value).toEqual([3]);

      field.setValue([1]);
      expect(field.value).toEqual([1]);
    });
  });

  describe('lookupCall', () => {

    it('switching should refill tree', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: LanguageDummyLookupCall
      });

      field.setValue([100, 500]);
      jasmine.clock().tick(300);
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('English, Swiss-German');
      expect(field.tree.visibleNodesFlat.length).toBe(5);
      expect(field.tree.checkedNodes.length).toBe(2);

      let newLookupCall = scout.create(DummyLookupCall, {
        session: session
      });
      field.setLookupCall(newLookupCall);
      jasmine.clock().tick(300);
      // dont change value when lookupCall changes
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('');
      expect(field.tree.checkedNodes.length).toBe(0);
      expect(field.tree.visibleNodesFlat.length).toBe(1);
      field.tree.expandNode(field.tree.visibleNodesFlat[0]);
      expect(field.tree.visibleNodesFlat.length).toBe(3);
    });

    it('switching to a lookup call returning no results should clear table', () => {
      let field = createFieldWithLookupCall({}, {
        objectType: DummyLookupCall
      });
      field.setValue([100, 500]);
      jasmine.clock().tick(300);
      expect(field.tree.visibleNodesFlat.length).toBe(1);

      let newLookupCall = scout.create(EmptyDummyLookupCall, {
        session: session
      });
      field.setLookupCall(newLookupCall);
      jasmine.clock().tick(300);
      // dont change value when lookupCall changes
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('');
      expect(field.tree.checkedNodes.length).toBe(0);
      expect(field.tree.visibleNodesFlat.length).toBe(0);
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
      expect(field.tree.visibleNodesFlat.length).toBe(1);
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

      field.tree.checkNodes(field.tree.visibleNodesFlat[2]);
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

      expect(field.tree.visibleNodesFlat.length).toBe(1);
    });

    it('should not set an error status if lookup returned no results', () => {
      let field = createFieldWithLookupCall({}, {objectType: EmptyDummyLookupCall});
      jasmine.clock().tick(300);
      expect(field.tree.visibleNodesFlat.length).toBe(0);
      expect(field.errorStatus).toBe(null);
    });
  });

  describe('value', () => {

    it('should be synchronized when rows are checked', () => {
      let field = createFieldWithLookupCall();
      jasmine.clock().tick(500);

      field.tree.expandNode(field.tree.visibleNodesFlat[0]);
      field.tree.checkNodes(field.tree.visibleNodesFlat);
      jasmine.clock().tick(300);
      expect(field.value).toEqual([1, 2, 3]);
      expect(field.displayText).toBe('Foo, Bar, Baz');
      expect(field.tree.checkedNodes.length).toBe(3);

      field.tree.uncheckNodes(field.tree.visibleNodesFlat);
      jasmine.clock().tick(300);
      expect(field.value).toEqual([]);
      expect(field.tree.checkedNodes.length).toBe(0);
      expect(field.displayText).toBe('');

      field.tree.checkNode(field.tree.visibleNodesFlat[1]);
      jasmine.clock().tick(500);
      expect(field.value).toEqual([2]);
      expect(field.displayText).toBe('Bar');
      expect(field.tree.checkedNodes.length).toBe(1);
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
      let model = helper.createFieldModel(TreeBox, session.desktop, {
        lookupCall: lookupCall
      });
      let treeBox = scout.create(TreeBox, model);
      expect(treeBox.displayText).toBe('');
      treeBox.setValue([1]);
      jasmine.clock().tick(300);
      expect(treeBox.value).toEqual([1]);
      expect(treeBox.displayText).toBe('Foo');
      treeBox.setValue([2]);
      jasmine.clock().tick(300);
      expect(treeBox.value).toEqual([2]);
      expect(treeBox.displayText).toBe('Bar');
    });

    it('returns empty string if value is null or undefined', () => {
      let model = helper.createFieldModel(TreeBox, session.desktop, {
        lookupCall: lookupCall
      });
      let treeBox = scout.create(TreeBox, model);
      expect(treeBox.displayText).toBe('');

      treeBox.setValue(null);
      jasmine.clock().tick(300);
      expect(treeBox.value).toEqual([]);
      expect(treeBox.displayText).toBe('');

      treeBox.setValue(undefined);
      jasmine.clock().tick(300);
      expect(treeBox.value).toEqual([]);
      expect(treeBox.displayText).toBe('');
    });

    it('does not auto-check child nodes if node is checked by model', () => {
      let model = helper.createFieldModel(TreeBox, session.desktop, {
        lookupCall: lookupCall
      });
      let treeBox = scout.create(TreeBox, model);
      treeBox.tree.autoCheckChildren = true;

      // Checking nodes by model should not auto-check child nodes
      treeBox.setValue([1]);
      jasmine.clock().tick(300);
      expect(treeBox.value).toEqual([1]);
      expect(treeBox.tree.checkedNodes.length).toBe(1);
    });

  });

  describe('aria properties', () => {

    it('has aria-labelledby set', () => {
      let treeBox = scout.create(TreeBox, {
        parent: session.desktop,
        label: 'test'
      });
      treeBox.render();
      expect(treeBox.$field.attr('aria-labelledby')).toBeTruthy();
      expect(treeBox.$field.attr('aria-labelledby')).toBe(treeBox.$label.attr('id'));
      expect(treeBox.$field.attr('aria-label')).toBeFalsy();
    });
  });

});
