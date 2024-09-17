/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/* eslint-disable no-multi-assign */
import {keys} from '../../src/index';
import {JQueryTesting, TreeSpecHelper} from '../../src/testing/index';

describe('TreeKeyStrokes', () => {
  let session: SandboxSession;
  let helper: TreeSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TreeSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('key up', () => {

    it('selects the above node in collapsed tree', () => {
      let model = helper.createModelFixture(3, 3, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0],
        node1 = tree.nodes[1],
        node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node2]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node1]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node0]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node0]);
    });

    it('selects the above node node in expanded tree', () => {
      let model = helper.createModelFixture(3, 1, true);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0],
        node1 = tree.nodes[1],
        node0Child2 = node0.childNodes[2],
        node1Child0 = node1.childNodes[0],
        node1Child1 = node1.childNodes[1];

      tree.render();
      helper.selectNodesAndAssert(tree, [node1Child1]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node1Child0]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node1]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node0Child2]);
    });

    it('selects the last node if no node is selected yet', () => {
      let model = helper.createModelFixture(3, 3, false);
      let tree = helper.createTree(model);

      let node2 = tree.nodes[2];

      tree.render();
      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node2]);
    });

    it('selects the only node if there is only one', () => {
      let model = helper.createModelFixture(1, 0, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];
      tree.render();
      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node0]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node0]);

      tree.deselectAll();

      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node0]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node0]);
    });
    it('does nothing if first node already is selected', () => {
      let model = helper.createModelFixture(3, 3, false);
      let tree = helper.createTree(model);

      tree.render();
      let node0 = tree.nodes[0];
      helper.selectNodesAndAssert(tree, [node0]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.UP);
      helper.assertSelection(tree, [node0]);
    });
  });

  describe('key down', () => {
    it('selects the node below in collapsed tree', () => {
      let model = helper.createModelFixture(3, 3, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0],
        node1 = tree.nodes[1],
        node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node1]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node2]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node2]);
    });

    it('selects the first node if no row is selected yet', () => {
      let model = helper.createModelFixture(3, 3, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];

      tree.render();

      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node0]);
    });

    it('selects the above node node in expanded tree', () => {
      let model = helper.createModelFixture(3, 1, true);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0],
        node1 = tree.nodes[1],
        node0Child2 = node0.childNodes[2],
        node1Child0 = node1.childNodes[0],
        node1Child1 = node1.childNodes[1];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0Child2]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node1]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node1Child0]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node1Child1]);
    });

    it('selects the only node if there is only one', () => {
      let model = helper.createModelFixture(1, 0, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];
      tree.render();
      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node0]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node0]);

      tree.deselectAll();

      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node0]);
      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node0]);
    });

    it('does nothing if last node already is selected', () => {
      let model = helper.createModelFixture(3, 1, false);
      let tree = helper.createTree(model);

      let node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node2]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.DOWN);
      helper.assertSelection(tree, [node2]);
    });
  });

  describe('Home', () => {
    it('selects first node in collapsed tree', () => {
      let model = helper.createModelFixture(3, 1, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0],
        node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node2]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.HOME);
      helper.assertSelection(tree, [node0]);
    });
    it('selects first node in expanded tree', () => {
      let model = helper.createModelFixture(3, 1, true);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0],
        node0Child2 = node0.childNodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0Child2]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.HOME);
      helper.assertSelection(tree, [node0]);
      tree.visitNodes(node => {
        expect(node.expanded).toBeFalsy();
      });
    });
  });

  describe('Subtract', () => {

    it(' collapses a node', () => {
      let model = helper.createModelFixture(3, 1, true);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);
      expect(node0.expanded).toBeTruthy();

      JQueryTesting.triggerKeyDown(tree.$data, keys.LEFT);
      expect(node0.expanded).toBeFalsy();
    });

    it(' collapses a node and drill up', () => {
      let model = helper.createModelFixture(3, 2, true);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0],
        node0Child0 = node0.childNodes[0];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0Child0]);
      expect(node0Child0.expanded).toBeTruthy();

      JQueryTesting.triggerKeyDown(tree.$data, keys.LEFT);
      expect(node0Child0.expanded).toBeFalsy();
      JQueryTesting.triggerKeyDown(tree.$data, keys.SUBTRACT);
      helper.assertSelection(tree, [node0]);
      expect(node0.expanded).toBeTruthy();
      JQueryTesting.triggerKeyDown(tree.$data, keys.SUBTRACT);
      expect(node0.expanded).toBeFalsy();
    });

  });

  describe('Add', () => {
    it(' expands a node', () => {
      let model = helper.createModelFixture(3, 1, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);
      expect(node0.expanded).toBeFalsy();

      JQueryTesting.triggerKeyDown(tree.$data, keys.RIGHT);
      expect(node0.expanded).toBeTruthy();
    });

    it(' expands a node and drill down', () => {
      let model = helper.createModelFixture(3, 1, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);
      expect(node0.expanded).toBeFalsy();

      JQueryTesting.triggerKeyDown(tree.$data, keys.ADD);
      expect(node0.expanded).toBeTruthy();
      JQueryTesting.triggerKeyDown(tree.$data, keys.ADD);
      helper.assertSelection(tree, [node0.childNodes[0]]);
    });
  });

  describe('End', () => {
    it(' jumps to last node', () => {
      let model = helper.createModelFixture(3, 1, false);
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];
      let node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);
      expect(node0.expanded).toBeFalsy();

      JQueryTesting.triggerKeyDown(tree.$data, keys.END);
      helper.assertSelection(tree, [node2]);
    });

  });

  describe('space', () => {

    it('does nothing if no nodes are selected', () => {
      let model = helper.createModelFixture(3, 1, false);
      model.checkable = true;
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];
      tree.checkNode(node0, true);
      expect(node0.checked).toBeTruthy();
      tree.render();
      JQueryTesting.triggerKeyDown(tree.$data, keys.SPACE);

      tree.visitNodes(node => {
        if (node === node0) {
          expect(node.checked).toBeTruthy();
        } else {
          expect(node.checked).toBeFalsy();
        }
      });
    });

    it('checks the selected node ', () => {
      let model = helper.createModelFixture(3, 1, false);
      model.checkable = true;
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];
      tree.render();
      expect(node0.checked).toBeFalsy();
      helper.selectNodesAndAssert(tree, [node0]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.SPACE);

      tree.visitNodes(node => {
        if (node === node0) {
          expect(node.checked).toBeTruthy();
        } else {
          expect(node.checked).toBeFalsy();
        }
      });
    });

    it('unchecks the selected node ', () => {
      let model = helper.createModelFixture(3, 1, false);
      model.checkable = true;
      let tree = helper.createTree(model);

      let node0 = tree.nodes[0];
      tree.render();
      tree.checkNode(node0, true);
      expect(node0.checked).toBeTruthy();
      helper.selectNodesAndAssert(tree, [node0]);

      JQueryTesting.triggerKeyDown(tree.$data, keys.SPACE);

      tree.visitNodes(node => {
        expect(node.checked).toBeFalsy();
      });
    });

  });

});
