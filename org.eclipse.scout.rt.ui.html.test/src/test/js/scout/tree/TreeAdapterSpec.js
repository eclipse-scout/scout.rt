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
describe("TreeAdapter", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TreeSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  describe("node click", function() {
    it("sends selection and click events in one call in this order", function() {
      var model = helper.createModelFixture(1);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      tree.render();

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerClick();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodesSelected', 'nodeClicked']);
    });

    it("sends selection, check and click events if tree is checkable and checkbox has been clicked", function() {
      var model = helper.createModelFixture(1);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      tree.checkable = true;
      tree.render();

      var $checkbox = tree.$container.find('.tree-node:first').children('.tree-node-checkbox')
        .children('div');
      $checkbox.triggerClick();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodesSelected', 'nodesChecked', 'nodeClicked']);
    });

    it("does not send click if mouse down happens on another node than mouseup", function() {
      var model = helper.createModelFixture(2);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      tree.render();

      var $node0 = tree.nodes[0].$node;
      var $node1 = tree.nodes[1].$node;
      $node0.triggerMouseDown();
      $node1.triggerMouseUp();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      // Must contain only selection event (of first node), no clicked
      expect(requestData).toContainEventsExactly([{
        nodeIds: [tree.nodes[0].id],
        target: tree.id,
        type: 'nodesSelected'
      }]);
    });

    it("does not send click if mouse down does not happen on a node", function() {
      var model = helper.createModelFixture(1);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      var $div = session.$entryPoint.makeDiv().cssHeight(10).cssWidth(10);
      tree.render();

      var $node0 = tree.nodes[0].$node;
      $node0.triggerMouseDown();
      $(window).triggerMouseUp({
        position: {
          left: 0,
          top: 0
        }
      });

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      // Must contain only selection event (of first node), no clicked
      expect(requestData).toContainEventsExactly([{
        nodeIds: [tree.nodes[0].id],
        target: tree.id,
        type: 'nodesSelected'
      }]);

      jasmine.Ajax.uninstall();
      jasmine.Ajax.install();

      $(window).triggerMouseDown({
        position: {
          left: 0,
          top: 0
        }
      });
      $node0.triggerMouseUp();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });
  });

  describe("node double click", function() {
    it("sends clicked, selection, action and expansion events", function() {
      var model = helper.createModelFixture(1, 1, false);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      tree.render();

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerDoubleClick();

      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['nodesSelected', 'nodeClicked', 'nodeAction', 'nodeExpanded']);
    });
  });

  describe("node control double click", function() {
    it("sends clicked, selection, action and expansion events", function() {
      var model = helper.createModelFixture(1, 1, false);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      tree.render();

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerDoubleClick();

      sendQueuedAjaxCalls();

      // clicked has to be after selected otherwise it is not possible to get the selected row in execNodeClick
      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['nodesSelected', 'nodeClicked', 'nodeAction', 'nodeExpanded']);
    });
  });

  describe("selectNodes", function() {
    it("sends nodeExpanded for the parents if a hidden node should be selected whose parents are collapsed (revealing the selection)", function() {
      var model = helper.createModelFixture(3, 3, false);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      var node0 = tree.nodes[0];
      var child0 = node0.childNodes[0];
      var grandchild0 = child0.childNodes[0];
      tree.render();

      expect(node0.expanded).toBe(false);
      expect(child0.expanded).toBe(false);
      expect(child0.$node).toBeUndefined();

      tree.selectNodes([grandchild0]);
      expect(node0.expanded).toBe(true);
      expect(child0.expanded).toBe(true);
      expect(tree.$selectedNodes().length).toBe(1);
      expect(grandchild0.$node.isSelected()).toBe(true);

      sendQueuedAjaxCalls();

      var event0 = new scout.RemoteEvent(tree.id, 'nodeExpanded', {
        nodeId: node0.id,
        expanded: true,
        expandedLazy: false
      });
      var event1 = new scout.RemoteEvent(tree.id, 'nodeExpanded', {
        nodeId: child0.id,
        expanded: true,
        expandedLazy: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([event0, event1]);
    });

    it("does not send selection event if triggered by server", function() {
      var model = helper.createModelFixture(2, 2);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);

      adapter._onNodesSelected([tree.nodes[1].id]);
      sendQueuedAjaxCalls();
      expect(tree.selectedNodes[0]).toBe(tree.nodes[1]);
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

  });

  describe("checkNodes", function() {

    it("does not send checked event if triggered by server", function() {
      var model = helper.createModelFixture(2, 2);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      tree.checkable = true;
      expect(tree.nodes[1].checked).toBe(false);

      adapter._onNodesChecked([{
        id: tree.nodes[1].id,
        checked: true
      }]);
      expect(tree.nodes[1].checked).toBe(true);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

  });

  describe("setNodesExpanded", function() {

    it("does not send expand event if triggered by server", function() {
      var model = helper.createModelFixture(2, 2);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      var node = tree.nodes[1];
      expect(node.expanded).toBe(false);

      adapter._onNodeExpanded(node.id, {
        nodeId: node.id,
        expanded: true,
        expandedLazy: false
      });
      sendQueuedAjaxCalls();
      expect(node.expanded).toBe(true);
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

  });

  describe("collapseAll", function() {
    it("sends nodeExpanded for every collapsed node", function() {
      var i;
      var model = helper.createModelFixture(3, 2, true);
      var adapter = helper.createTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      tree.render();

      var allNodes = [];
      tree._visitNodes(tree.nodes, function(node) {
        allNodes.push(node);
      });

      tree.collapseAll();
      // A nodeExpanded event must be sent for every node because all nodes were initially expanded
      sendQueuedAjaxCalls();
      expect(mostRecentJsonRequest().events.length).toBe(allNodes.length);
    });
  });

  describe("onModelAction", function() {

    describe("nodesInserted event", function() {
      var model;
      var tree;
      var adapter;
      var node0;
      var node1;
      var node2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 1, true);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        node2 = tree.nodes[2];
      });

      it("calls insertNodes", function() {
        spyOn(tree, 'insertNodes');

        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        var event = helper.createNodesInsertedEvent(model, [newNode0Child3], node0.id);
        adapter.onModelAction(event);
        expect(tree.insertNodes).toHaveBeenCalledWith([newNode0Child3], tree.nodes[0]);
      });

    });

    describe("nodesDeleted event", function() {
      var model;
      var tree;
      var adapter;
      var node0;
      var node1;
      var node2;

      beforeEach(function() {
        // A large tree is used to properly test recursion
        model = helper.createModelFixture(3, 2, true);
        tree = helper.createTree(model);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        node2 = tree.nodes[2];
      });

      it("calls deleteNodes", function() {
        spyOn(tree, 'deleteNodes');

        var node2Child0 = node2.childNodes[0];
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        var event = helper.createNodesDeletedEvent(model, [node2Child0.id], node2.id);
        adapter.onModelAction(event);
        expect(tree.deleteNodes).toHaveBeenCalledWith([node2Child0], node2);
      });

    });

    describe("allChildNodesDeleted event", function() {
      var model;
      var tree;
      var adapter;
      var node0;
      var node1;
      var node2;
      var node1Child0;
      var node1Child1;
      var node1Child2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 1, true);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        node2 = tree.nodes[2];
        node1Child0 = node1.childNodes[0];
        node1Child1 = node1.childNodes[1];
        node1Child2 = node1.childNodes[1];
      });

      it("calls deleteAllChildNodes", function() {
        spyOn(tree, 'deleteAllChildNodes');

        var event = helper.createAllChildNodesDeletedEvent(model);
        adapter.onModelAction(event);
        expect(tree.deleteAllChildNodes).toHaveBeenCalled();
      });

    });

    describe("nodesSelected event", function() {
      var model;
      var tree;
      var adapter;
      var node0;
      var child0;
      var grandchild0;

      beforeEach(function() {
        model = helper.createModelFixture(3, 3, false);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        child0 = node0.childNodes[0];
        grandchild0 = child0.childNodes[0];
      });

      it("calls selectNodes", function() {
        spyOn(tree, 'selectNodes');

        var event = helper.createNodesSelectedEvent(model, [node0.id]);
        adapter.onModelAction(event);
        expect(tree.selectNodes).toHaveBeenCalledWith([node0]);
      });

    });

    describe("nodeChanged event", function() {
      var model, tree, adapter, nodes, node0, node1, child0, child1_1;

      beforeEach(function() {
        model = helper.createModelFixture(3, 3, false);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        nodes = tree.nodes;
        node0 = nodes[0];
        node1 = nodes[1];
        child0 = node0.childNodes[0];
        child1_1 = node1.childNodes[1];
      });

      it("calls changeNode", function() {
        spyOn(tree, 'changeNode');

        var event = helper.createNodeChangedEvent(model, node0.id);
        adapter.onModelAction(event);
        expect(tree.changeNode).toHaveBeenCalledWith(node0);
      });

      it("updates the text of the node", function() {
        tree.render();
        var event = helper.createNodeChangedEvent(model, node0.id);
        event.text = 'new Text';
        var message = {
          events: [event]
        };
        session._processSuccessResponse(message);
        expect(node0.text).toBe(event.text);
        expect(node0.$text.text()).toBe('new Text');
      });

    });

    describe("nodesUpdated event", function() {
      var model;
      var tree;
      var adapter;
      var node0;
      var child0;

      beforeEach(function() {
        model = helper.createModelFixture(3, 3, false);
        tree = helper.createTree(model);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        child0 = node0.childNodes[0];
      });

      it("calls updateNodes", function() {
        spyOn(tree, 'updateNodes');

        var child0Update = {
          id: child0.id,
          enabled: false
        };
        var event = helper.createNodesUpdatedEvent(model, [child0Update]);
        adapter.onModelAction(event);
        expect(tree.updateNodes).toHaveBeenCalledWith([child0Update]);
      });
    });

    describe("childNodeOrderChanged event", function() {
      var model;
      var tree;
      var adapter;
      var node0;
      var child0;

      beforeEach(function() {
        model = helper.createModelFixture(3, 3, false);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        child0 = node0.childNodes[0];
      });

      it("calls updateNodeOrder", function() {
        spyOn(tree, 'updateNodeOrder');
        var parentNode = tree.nodes[1];
        var childNode0 = parentNode.childNodes[0];
        var childNode1 = parentNode.childNodes[1];
        var childNode2 = parentNode.childNodes[2];

        var event = helper.createChildNodeOrderChangedEvent(model, [childNode2.id, childNode1.id, childNode0.id], parentNode.id);
        adapter.onModelAction(event);
        expect(tree.updateNodeOrder).toHaveBeenCalledWith([childNode2, childNode1, childNode0], parentNode);
      });

    });

    describe("multiple events", function() {
      var model;
      var tree;
      var adapter;
      var node0;
      var node1;
      var node2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 1, true);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        node2 = tree.nodes[2];
      });

      it("handles delete, collapse, insert, expand events correctly", function() {
        tree.render();

        // Delete child nodes from node0
        var message = {
          events: [helper.createAllChildNodesDeletedEvent(model, node0.id)]
        };
        session._processSuccessResponse(message);
        expect(node0.childNodes.length).toBe(0);
        expect(helper.findAllNodes(tree).length).toBe(9);

        // Collapse node0
        var $node0 = node0.$node;
        message = {
          events: [helper.createNodeExpandedEvent(model, node0.id, false)]
        };
        session._processSuccessResponse(message);
        expect(node0.expanded).toBe(false);
        expect($node0).not.toHaveClass('expanded');

        // Insert new child node at node0
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
        message = {
          events: [helper.createNodesInsertedEvent(model, [newNode0Child3], node0.id)]
        };
        session._processSuccessResponse(message);

        // Model should be updated, html nodes not added because node still is collapsed
        expect(node0.childNodes.length).toBe(1);
        expect(node0.childNodes[0].text).toBe(newNode0Child3.text);
        expect(Object.keys(tree.nodesMap).length).toBe(10);
        expect(helper.findAllNodes(tree).length).toBe(9); //Still 9 nodes

        // Expand again
        message = {
          events: [helper.createNodeExpandedEvent(model, node0.id, true)]
        };
        session._processSuccessResponse(message);

        expect(node0.expanded).toBe(true);
        expect($node0).toHaveClass('expanded');

        // Html nodes should now be added
        expect(helper.findAllNodes(tree).length).toBe(10);
        expect(node0.childNodes[0].$node).toBeDefined();
      });
    });
  });
});
