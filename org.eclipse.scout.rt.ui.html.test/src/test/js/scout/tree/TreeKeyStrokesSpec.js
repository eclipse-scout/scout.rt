/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("TreeKeyStrokes", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TreeSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("key up", function() {

    it("selects the above node in collapsed tree", function() {
      var model = helper.model = helper.createModelFixture(3, 3, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0],
        node1 = tree.nodes[1],
        node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node2]);

      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node1]);
      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node0]);
      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node0]);
    });

    it("selects the above node node in expanded tree", function() {
      var model = helper.model = helper.createModelFixture(3, 1, true);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0],
        node1 = tree.nodes[1],
        node0Child2 = node0.childNodes[2],
        node1Child0 = node1.childNodes[0],
        node1Child1 = node1.childNodes[1];

      tree.render();
      helper.selectNodesAndAssert(tree, [node1Child1]);

      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node1Child0]);
      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node1]);
      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node0Child2]);
    });

    it("selects the last node if no node is selected yet", function() {
      var model = helper.model = helper.createModelFixture(3, 3, false);
      var tree = helper.createTree(model);

      var node2 = tree.nodes[2];

      tree.render();
      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node2]);
    });

    it("selects the only node if there is only one", function() {
      var model = helper.model = helper.createModelFixture(1, 0, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];
      tree.render();
      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node0]);

      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node0]);

      tree.deselectAll();

      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node0]);
      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node0]);
    });
    it("does nothing if first node already is selected", function() {
      var model = helper.model = helper.createModelFixture(3, 3, false);
      var tree = helper.createTree(model);

      tree.render();
      var node0 = tree.nodes[0];
      helper.selectNodesAndAssert(tree, [node0]);
      tree.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(tree, [node0]);
    });
  });

  describe("key down", function() {
    it("selects the node below in collapsed tree", function() {
      var model = helper.model = helper.createModelFixture(3, 3, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0],
        node1 = tree.nodes[1],
        node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);

      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node1]);
      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node2]);
      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node2]);
    });

    it("selects the first node if no row is selected yet", function() {
      var model = helper.model = helper.createModelFixture(3, 3, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];

      tree.render();

      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node0]);
    });

    it("selects the above node node in expanded tree", function() {
      var model = helper.model = helper.createModelFixture(3, 1, true);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0],
        node1 = tree.nodes[1],
        node0Child2 = node0.childNodes[2],
        node1Child0 = node1.childNodes[0],
        node1Child1 = node1.childNodes[1];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0Child2]);

      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node1]);
      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node1Child0]);
      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node1Child1]);
    });

    it("selects the only node if there is only one", function() {
      var model = helper.model = helper.createModelFixture(1, 0, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];
      tree.render();
      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node0]);

      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node0]);

      tree.deselectAll();

      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node0]);
      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node0]);
    });

    it("does nothing if last node already is selected", function() {
      var model = helper.model = helper.createModelFixture(3, 1, false);
      var tree = helper.createTree(model);

      var node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node2]);

      tree.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(tree, [node2]);
    });
  });

  describe("Home", function() {
    it("selects first node in collapsed tree", function() {
      var model = helper.model = helper.createModelFixture(3, 1, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0],
        node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node2]);

      tree.$data.triggerKeyDown(scout.keys.HOME);
      helper.assertSelection(tree, [node0]);
    });
    it("selects first node in expanded tree", function() {
      var model = helper.model = helper.createModelFixture(3, 1, true);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0],
        node0Child2 = node0.childNodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0Child2]);

      tree.$data.triggerKeyDown(scout.keys.HOME);
      helper.assertSelection(tree, [node0]);
      tree._visitNodes(tree.nodes, function(node) {
        expect(node.expanded).toBeFalsy();
      });

    });
  });

  describe("Subtract", function() {

    it(" collapses a node", function() {
      var model = helper.model = helper.createModelFixture(3, 1, true);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);
      expect(node0.expanded).toBeTruthy();

      tree.$data.triggerKeyDown(scout.keys.SUBTRACT);
      expect(node0.expanded).toBeFalsy();
    });

    it(" collapses a node and drill up", function() {
      var model = helper.model = helper.createModelFixture(3, 1, true);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0],
        node0Child0 = node0.childNodes[0];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0Child0]);
      expect(node0Child0.expanded).toBeTruthy();

      tree.$data.triggerKeyDown(scout.keys.SUBTRACT);
      expect(node0Child0.expanded).toBeFalsy();
      tree.$data.triggerKeyDown(scout.keys.SUBTRACT);
      helper.assertSelection(tree, [node0]);
      expect(node0.expanded).toBeTruthy();
      tree.$data.triggerKeyDown(scout.keys.SUBTRACT);
      expect(node0.expanded).toBeFalsy();
    });

  });

  describe("Add", function() {
    it(" expands a node", function() {
      var model = helper.model = helper.createModelFixture(3, 1, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);
      expect(node0.expanded).toBeFalsy();

      tree.$data.triggerKeyDown(scout.keys.ADD);
      expect(node0.expanded).toBeTruthy();
    });

    it(" expands a node and drill down", function() {
      var model = helper.model = helper.createModelFixture(3, 1, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);
      expect(node0.expanded).toBeFalsy();

      tree.$data.triggerKeyDown(scout.keys.ADD);
      expect(node0.expanded).toBeTruthy();
      tree.$data.triggerKeyDown(scout.keys.ADD);
      helper.assertSelection(tree, [node0.childNodes[0]]);
    });
  });

  describe("End", function() {
    it(" jumps to last node", function() {
      var model = helper.model = helper.createModelFixture(3, 1, false);
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];
      var node2 = tree.nodes[2];

      tree.render();
      helper.selectNodesAndAssert(tree, [node0]);
      expect(node0.expanded).toBeFalsy();

      tree.$data.triggerKeyDown(scout.keys.END);
      helper.assertSelection(tree, [node2]);
    });

  });

  describe("space", function() {

    it("does nothing if no nodes are selected", function() {
      var model = helper.model = helper.createModelFixture(3, 1, false);
      model.checkable = true;
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];
      tree.checkNode(node0, true);
      expect(node0.checked).toBeTruthy();
      tree.render();
      tree.$data.triggerKeyDown(scout.keys.SPACE);

      tree._visitNodes(tree.nodes, function(node) {
        if (node === node0) {
          expect(node.checked).toBeTruthy();
        } else {
          expect(node.checked).toBeFalsy();
        }
      });
    });

    it("checks the selected node ", function() {
      var model = helper.model = helper.createModelFixture(3, 1, false);
      model.checkable = true;
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];
      tree.render();
      expect(node0.checked).toBeFalsy();
      helper.selectNodesAndAssert(tree, [node0]);

      tree.$data.triggerKeyDown(scout.keys.SPACE);

      tree._visitNodes(tree.nodes, function(node) {
        if (node === node0) {
          expect(node.checked).toBeTruthy();
        } else {
          expect(node.checked).toBeFalsy();
        }
      });
    });

    it("unchecks the selected node ", function() {
      var model = helper.model = helper.createModelFixture(3, 1, false);
      model.checkable = true;
      var tree = helper.createTree(model);

      var node0 = tree.nodes[0];
      tree.render();
      tree.checkNode(node0, true);
      expect(node0.checked).toBeTruthy();
      helper.selectNodesAndAssert(tree, [node0]);

      tree.$data.triggerKeyDown(scout.keys.SPACE);

      tree._visitNodes(tree.nodes, function(node) {
        expect(node.checked).toBeFalsy();
      });
    });

  });

});
