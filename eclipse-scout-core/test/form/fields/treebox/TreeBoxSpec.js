/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {QueryBy, scout, Status, TreeBox} from '../../../../src/index';
import {DummyLookupCall, FormSpecHelper} from '../../../../src/testing/index';

describe('TreeBox', () => {
  let session, field, helper;

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

  function createFieldWithLookupCall(model, lookupCallModel) {
    lookupCallModel = $.extend({
      objectType: 'DummyLookupCall'
    }, lookupCallModel);

    model = $.extend({}, {
      parent: session.desktop,
      lookupCall: lookupCallModel
    }, model);
    let box = scout.create('TreeBox', model);
    box.render();
    return box;
  }

  function expandAll(treeBox) {
    treeBox.tree.visitNodes(node => treeBox.tree.expandNode(node, {renderAnimated: false}));
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
      let box = scout.create('TreeBox', {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall'
      });

      let lookupPrepared = box.when('prepareLookupCall');
      let lookupDone = box.when('lookupCallDone');
      box.refreshLookup();
      jasmine.clock().tick(500);

      $.promiseAll([lookupPrepared, lookupDone]).then(event => {
        expect(event.lookupCall.objectType).toBe('DummyLookupCall');
      })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(500);
    });

    it('LookupCall can be prepared if value is configured', done => {
      let box = scout.create('TreeBox', {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall',
        value: 3
      });

      let lookupPrepared = box.when('prepareLookupCall');
      let lookupDone = box.when('lookupCallDone');
      box.render();
      jasmine.clock().tick(500);

      $.promiseAll([lookupPrepared, lookupDone]).then(event => {
        expect(event.lookupCall.objectType).toBe('DummyLookupCall');
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

    it('shows lookup rows in the order defined by the lookup call', () => {
      field = createFieldWithLookupCall({
        lookupCall: {
          objectType: 'StaticLookupCall',
          data: [
            ['a', 'A'],
            ['c', 'C'],
            ['b', 'B']
          ]
        }
      });
      spyOn(field, '_logWarning').and.callThrough();
      jasmine.clock().tick(300);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A;C;B');
      expect(field._logWarning).toHaveBeenCalledTimes(0);
    });

    it('supports hierarchical lookup calls', () => {
      field = createFieldWithLookupCall({
        lookupCall: {
          objectType: 'StaticLookupCall',
          hierarchical: true,
          data: [
            ['ab', 'A/B', 'a'],
            ['aaa', 'A/A/A', 'aa'],
            ['aa', 'A/A', 'a'],
            ['a', 'A', null],
            ['ca', 'C/A', 'c'],
            ['c', 'C', null],
            ['b', 'B', null],
            ['aba', 'A/B/A', 'ab'],
            ['cb', 'C/B', 'c']
          ]
        }
      });
      spyOn(field, '_logWarning').and.callThrough();
      jasmine.clock().tick(300);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A;C;B');
      expandAll(field);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A;A/B;A/B/A;A/A;A/A/A;C;C/A;C/B;B');
      expect(field._logWarning).toHaveBeenCalledTimes(0);
    });

    it('ignores lookup rows with null key', () => {
      field = createFieldWithLookupCall({
        lookupCall: {
          objectType: 'StaticLookupCall',
          hierarchical: true,
          data: [
            ['a', 'A', null],
            [null, 'Null', 'a'], // should be ignored
            ['b', 'B', null]
          ]
        }
      });
      spyOn(field, '_logWarning').and.callThrough();
      jasmine.clock().tick(300);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A;B');
      expandAll(field);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A;B');
      expect(field._logWarning).toHaveBeenCalledTimes(1);
    });

    it('ignores lookup rows that reference themselves', () => {
      field = createFieldWithLookupCall({
        lookupCall: {
          objectType: 'StaticLookupCall',
          hierarchical: true,
          data: [
            ['a', 'A', null],
            ['c', 'C', 'c'], // should be ignored
            ['b', 'B', 'a'],
            ['a', 'A2', 'b'] // should be ignored
          ]
        }
      });
      spyOn(field, '_logWarning').and.callThrough();
      jasmine.clock().tick(300);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A');
      expandAll(field);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A;B');
      expect(field._logWarning).toHaveBeenCalledTimes(2);
    });

    it('ignores lookup rows with disconnected sub-trees', () => {
      field = createFieldWithLookupCall({
        lookupCall: {
          objectType: 'StaticLookupCall',
          hierarchical: true,
          data: [
            ['a', 'A', null],
            ['b', 'B', 'x'], // should be ignored
            ['c', 'C', 'b'], // should be ignored
            ['d', 'D', 'e'], // should be ignored
            ['e', 'E', 'd'] //  should be ignored
          ]
        }
      });
      spyOn(field, '_logWarning').and.callThrough();
      jasmine.clock().tick(300);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A');
      expandAll(field);
      expect(field.tree.visibleNodesFlat.map(n => n.text).join(';')).toBe('A');
      expect(field._logWarning).toHaveBeenCalledTimes(0);
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
        objectType: 'LanguageDummyLookupCall'
      });

      field.setValue([100, 500]);
      jasmine.clock().tick(300);
      expect(field.value).toEqual([100, 500]);
      expect(field.displayText).toBe('English, Swiss-German');
      expect(field.tree.visibleNodesFlat.length).toBe(5);
      expect(field.tree.checkedNodes.length).toBe(2);

      let newLookupCall = scout.create('DummyLookupCall', {
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

    it('should be cloned and prepared for each lookup', () => {
      let templatePropertyValue = 11;
      let preparedPropertyValue = 22;
      let eventCounter = 0;
      let field = createFieldWithLookupCall({}, {
        customProperty: templatePropertyValue,
        _dataToLookupRow: function(data) { // overwrite mapping function to use the custom property
          return scout.create('LookupRow', {
            key: data[0],
            text: data[1] + this.customProperty
          });
        }
      });
      field.on('prepareLookupCall', event => {
        expect(event.lookupCall.customProperty).toBe(templatePropertyValue);
        expect(event.lookupCall.id).not.toBe(field.lookupCall.id);
        expect(event.type).toBe('prepareLookupCall');
        expect(event.source).toBe(field);

        event.lookupCall.customProperty = preparedPropertyValue; // change property for this call. Must not have any effect on the next call
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
    it('should set error status when result has an exception', () => {
      let field = createFieldWithLookupCall();
      field._lookupByAllDone({
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
      lookupCall = scout.create('DummyLookupCall', {
        session: session
      });
    });

    it('uses a lookup call to format the value', () => {
      let model = helper.createFieldModel('TreeBox', session.desktop, {
        lookupCall: lookupCall
      });
      let treeBox = scout.create('TreeBox', model);
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
      let model = helper.createFieldModel('TreeBox', session.desktop, {
        lookupCall: lookupCall
      });
      let treeBox = scout.create('TreeBox', model);
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
      let model = helper.createFieldModel('TreeBox', session.desktop, {
        lookupCall: lookupCall
      });
      let treeBox = scout.create('TreeBox', model);
      treeBox.tree.autoCheckChildren = true;

      // Checking nodes by model should not auto-check child nodes
      treeBox.setValue([1]);
      jasmine.clock().tick(300);
      expect(treeBox.value).toEqual([1]);
      expect(treeBox.tree.checkedNodes.length).toBe(1);
    });

  });

  describe('label', () => {

    it('is linked with the field', () => {
      let treeBox = scout.create('TreeBox', {
        parent: session.desktop
      });
      treeBox.render();
      expect(treeBox.$field.attr('aria-labelledby')).toBeTruthy();
      expect(treeBox.$field.attr('aria-labelledby')).toBe(treeBox.$label.attr('id'));
    });
  });

});
