/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("Outline", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createModelFixture(nodeCount, depth, expanded) {
    return createModel(createModelNodes(nodeCount, depth, expanded));
  }

  function createModel(nodes) {
    var model = createSimpleModel('Outline', session);

    if (nodes) {
      model.nodes = nodes;
    }

    return model;
  }

  function createModelNode(id, text) {
    return {
      "id": id,
      "text": text
    };
  }

  function createModelNodes(nodeCount, depth, expanded) {
    return createModelNodesInternal(nodeCount, depth, expanded);
  }

  function createModelNodesInternal(nodeCount, depth, expanded, parentNode) {
    if (!nodeCount) {
      return;
    }

    var nodes = [],
      nodeId;
    if (!depth) {
      depth = 0;
    }
    for (var i = 0; i < nodeCount; i++) {
      nodeId = i;
      if (parentNode) {
        nodeId = parentNode.id + '_' + nodeId;
      }
      nodes[i] = createModelNode(nodeId, 'node ' + i);
      nodes[i].expanded = expanded;
      if (depth > 0) {
        nodes[i].childNodes = createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
      }
    }
    return nodes;
  }

  function createOutline(model) {
    var tree = new scout.Outline();
    tree.init(model);
    return tree;
  }

  function createNodesDeletedEvent(model, nodeIds, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      nodeIds: nodeIds,
      type: 'nodesDeleted'
    };
  }

  function createAllChildNodesDeletedEvent(model, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      type: 'allChildNodesDeleted'
    };
  }

  describe("dispose", function() {
    var model, tree, node0, node1, node2;

    beforeEach(function() {
      // A large tree is used to properly test recursion
      model = createModelFixture(3, 2, true);
      tree = createOutline(model);
      node0 = model.nodes[0];
      node1 = model.nodes[1];
      node2 = model.nodes[2];
    });

    it("calls onNodeDeleted for every node to be able to cleanup", function() {
      spyOn(tree, '_onNodeDeleted');
      tree.destroy();
      expect(tree._onNodeDeleted.calls.count()).toBe(39);
    });

    it("calls onNodeDeleted for every node (which was not already deleted before) to be able to cleanup", function() {
      spyOn(tree, '_onNodeDeleted');

      var message = {
        events: [createNodesDeletedEvent(model, [node0.id])]
      };
      session._processSuccessResponse(message);
      expect(tree._onNodeDeleted.calls.count()).toBe(13);

      tree._onNodeDeleted.calls.reset();
      tree.destroy();
      expect(tree._onNodeDeleted.calls.count()).toBe(26);
    });

  });

  describe("navigateToTop", function() {

    it("collapses all nodes in bread crumb mode", function() {
      var model = createModelFixture(1, 1);
      var node0 = model.nodes[0];

      var tree = createOutline(model);
      tree.displayStyle = scout.Tree.DisplayStyle.BREADCRUMB;
      tree.render(session.$entryPoint);

      tree.selectNodes(node0);

      expect(tree.selectedNodes.indexOf(node0) > -1).toBe(true);
      expect(node0.expanded).toBe(true);

      tree.navigateToTop();

      expect(tree.selectedNodes.length).toBe(0);
      expect(node0.expanded).toBe(false);
    });
  });

  describe("onModelAction", function() {

    describe("nodesDeleted event", function() {
      var model, tree, node0, node1, node2;

      beforeEach(function() {
        // A large tree is used to properly test recursion
        model = createModelFixture(3, 2, true);
        tree = createOutline(model);
        node0 = model.nodes[0];
        node1 = model.nodes[1];
        node2 = model.nodes[2];
      });

      it("calls onNodeDeleted for every node to be able to cleanup", function() {
        spyOn(tree, '_onNodeDeleted');

        var message = {
          events: [createNodesDeletedEvent(model, [node0.id])]
        };
        session._processSuccessResponse(message);

        expect(tree._onNodeDeleted.calls.count()).toBe(13);
      });

    });

    describe("allChildNodesDeleted event", function() {
      var model, tree, node0, node1, node2;

      beforeEach(function() {
        // A large tree is used to properly test recursion
        model = createModelFixture(3, 2, true);
        tree = createOutline(model);
        node0 = model.nodes[0];
        node1 = model.nodes[1];
        node2 = model.nodes[2];
      });

      it("calls onNodeDeleted for every node to be able to cleanup", function() {
        spyOn(tree, '_onNodeDeleted');

        var message = {
          events: [createAllChildNodesDeletedEvent(model)]
        };
        session._processSuccessResponse(message);

        expect(tree._onNodeDeleted.calls.count()).toBe(39);
        expect(scout.objects.countProperties(tree.nodesMap)).toBe(0);
      });

    });

    describe("selectNodes", function() {
      var model, outline, node;

      beforeEach(function() {
        model = createModelFixture(3, 2, true);
        outline = createOutline(model);
        node = model.nodes[0];
      });

      it("handle navigateUp only once", function() {
        outline.navigateUpInProgress = true;
        outline.selectNodes([]);
        expect(outline.navigateUpInProgress).toBe(false);
      });

      // we must override the _render* methods for this test-case, since we had to
      // implement a lot more of set-up code to make these methods work.
      it("otherwise handle single selection (or do nothing when selection is != 1 node)", function() {
        node.detailFormVisibleByUi = false;
        outline.navigateUpInProgress = false;
        outline._renderSelection = function() {};
        outline._renderMenus = function() {};

        // don't change the visibleByUi flag when selection is != 1
        outline.selectNodes([]);
        expect(node.detailFormVisibleByUi).toBe(false);

        // set the visibleByUi flag to true when selection is exactly 1
        outline.selectNodes([node]);
        expect(node.detailFormVisibleByUi).toBe(true);
      });
    });

  });
});
