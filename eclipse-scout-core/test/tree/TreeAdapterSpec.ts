/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {defaultValues, RemoteEvent, Tree} from '../../src/index';
import {JQueryTesting, TreeSpecHelper} from '../../src/testing/index';

describe('TreeAdapter', () => {
  let session: SandboxSession;
  let helper: TreeSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TreeSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  let defaults = {
    'defaults': {
      'Tree': {
        'a': 123
      },
      'TreeNode': {
        'b': 234
      }
    },
    'objectTypeHierarchy': {
      'Widget': {
        'Tree': null
      }
    }
  };

  describe('node click', () => {
    it('sends selection and click events in one call in this order', () => {
      let model = helper.createModelFixture(1);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop);
      tree.render();

      let $node = tree.$container.find('.tree-node:first');
      JQueryTesting.triggerClick($node);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodesSelected', 'nodeClick']);
    });

    it('sends selection, check and click events if tree is checkable and checkbox has been clicked', () => {
      let model = helper.createModelFixture(1);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      tree.checkable = true;
      tree.render();

      let $checkbox = tree.$container.find('.tree-node:first').children('.tree-node-checkbox')
        .children('div');
      JQueryTesting.triggerClick($checkbox);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodesSelected', 'nodesChecked', 'nodeClick']);
    });

    it('does not send click if mouse down happens on another node than mouseup', () => {
      let model = helper.createModelFixture(2);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      tree.render();

      let $node0 = tree.nodes[0].$node;
      let $node1 = tree.nodes[1].$node;
      JQueryTesting.triggerMouseDown($node0);
      JQueryTesting.triggerMouseUp($node1);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let requestData = mostRecentJsonRequest();
      // Must contain only selection event (of first node), no clicked
      expect(requestData).toContainEventsExactly([{
        nodeIds: [tree.nodes[0].id],
        target: tree.id,
        type: 'nodesSelected'
      }]);
    });

    it('does not send click if mouse down does not happen on a node', () => {
      let model = helper.createModelFixture(1);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      session.$entryPoint.makeDiv().cssHeight(10).cssWidth(10);
      tree.render();

      let $node0 = tree.nodes[0].$node;
      JQueryTesting.triggerMouseDown($node0);
      // @ts-expect-error
      JQueryTesting.triggerMouseUp($(window), {
        position: {
          left: 0,
          top: 0
        }
      });

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let requestData = mostRecentJsonRequest();
      // Must contain only selection event (of first node), no clicked
      expect(requestData).toContainEventsExactly([{
        nodeIds: [tree.nodes[0].id],
        target: tree.id,
        type: 'nodesSelected'
      }]);

      jasmine.Ajax.uninstall();
      jasmine.Ajax.install();

      // @ts-expect-error
      JQueryTesting.triggerMouseDown($(window), {
        position: {
          left: 0,
          top: 0
        }
      });
      JQueryTesting.triggerMouseUp($node0);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });
  });

  describe('node double click', () => {
    it('sends clicked, selection, action and expansion events', () => {
      let model = helper.createModelFixture(1, 1, false);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop);
      tree.render();

      let $node = tree.$container.find('.tree-node:first');
      JQueryTesting.triggerDoubleClick($node);

      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['nodesSelected', 'nodeClick', 'nodeAction', 'nodeExpanded']);
    });
  });

  describe('node control double click', () => {
    it('sends clicked, selection, action and expansion events', () => {
      let model = helper.createModelFixture(1, 1, false);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop);
      tree.render();

      let $node = tree.$container.find('.tree-node:first');
      JQueryTesting.triggerDoubleClick($node);

      sendQueuedAjaxCalls();

      // clicked has to be after selected otherwise it is not possible to get the selected row in execNodeClick
      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['nodesSelected', 'nodeClick', 'nodeAction', 'nodeExpanded']);
    });
  });

  describe('selectNodes', () => {
    it('sends nodeExpanded for the parents if a hidden node should be selected whose parents are collapsed (revealing the selection)', () => {
      let model = helper.createModelFixture(3, 3, false);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      let node0 = tree.nodes[0];
      let child0 = node0.childNodes[0];
      let grandchild0 = child0.childNodes[0];
      tree.render();

      expect(node0.expanded).toBe(false);
      expect(child0.expanded).toBe(false);
      expect(child0.$node).toBe(null);

      tree.selectNodes([grandchild0]);
      expect(node0.expanded).toBe(true);
      expect(child0.expanded).toBe(true);
      expect(tree.$selectedNodes().length).toBe(1);
      expect(grandchild0.$node.isSelected()).toBe(true);

      sendQueuedAjaxCalls();

      let event0 = new RemoteEvent(tree.id, 'nodeExpanded', {
        nodeId: node0.id,
        expanded: true,
        expandedLazy: false
      });
      let event1 = new RemoteEvent(tree.id, 'nodeExpanded', {
        nodeId: child0.id,
        expanded: true,
        expandedLazy: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([event0, event1]);
    });

    it('does not send selection event if triggered by server', () => {
      let model = helper.createModelFixture(2, 2);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;

      adapter.onModelEvent({
        target: model.id,
        type: 'nodesSelected',
        nodeIds: [tree.nodes[1].id]
      });
      sendQueuedAjaxCalls();
      expect(tree.selectedNodes[0]).toBe(tree.nodes[1]);
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

  });

  describe('checkNodes', () => {

    it('sends checked event of checked node if triggered by server', () => {
      let model = helper.createModelFixture(2, 2);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      tree.checkable = true;
      expect(tree.nodes[1].checked).toBe(false);

      adapter.onModelAction({
        target: model.id,
        type: 'nodesChecked',
        nodes: [{
          id: tree.nodes[1].id,
          checked: true
        }]
      });
      expect(tree.nodes[1].checked).toBe(true);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
    });

    it('sends checked event of checked node and its children if triggered by server', () => {
      let model = helper.createModelFixture(2, 1);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      tree.checkable = true;
      tree.setAutoCheckChildren(true);

      // Arrange
      let node = tree.nodes[0];
      let childNode1 = node.childNodes[0];
      let childNode2 = node.childNodes[1];

      // Act
      adapter.onModelAction({
        target: model.id,
        type: 'nodesChecked',
        nodes: [{
          id: node.id,
          checked: true
        }]
      });

      // Assert
      expect(node.checked).toBe(true);
      expect(childNode1.checked).toBe(true);
      expect(childNode2.checked).toBe(true);
      sendQueuedAjaxCalls();
      let nodeIds = getNodesOfLastEventAjaxCall().map(n => n.nodeId);
      expect(nodeIds).toEqual(jasmine.arrayWithExactContents([node.id, childNode1.id, childNode2.id]));
    });

    it('sends checked event of checked node and its children if triggered by server', () => {
      let model = helper.createModelFixture(2, 1);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      tree.checkable = true;
      tree.setAutoCheckChildren(true);

      // Arrange
      let node = tree.nodes[0];
      let childNode1 = node.childNodes[0];
      let childNode2 = node.childNodes[1];
      tree.checkNode(childNode1);

      // Act
      adapter.onModelAction({
        target: model.id,
        type: 'nodesChecked',
        nodes: [{
          id: childNode2.id,
          checked: true
        }]
      });

      // Assert
      expect(node.checked).toBe(true);
      expect(childNode1.checked).toBe(true);
      expect(childNode2.checked).toBe(true);
      sendQueuedAjaxCalls();
      let nodeIds = getNodesOfLastEventAjaxCall(1).map(n => n.nodeId);
      expect(nodeIds).toEqual(jasmine.arrayWithExactContents([node.id, childNode2.id]));
    });

    const getNodesOfLastEventAjaxCall = (eventIndex = 0, request?: JasmineAjaxRequest) => {
      return getLastEvents(request)[eventIndex].nodes as { nodeId: string; checked: boolean }[];
    };

    const getLastEvents = (request = jasmine.Ajax.requests.mostRecent()): RemoteEvent[] => {
      return (<any>request.data()).events as RemoteEvent[];
    };
  });

  describe('setNodesExpanded', () => {
    it('does not send expand event if triggered by server', () => {
      let model = helper.createModelFixture(2, 2);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      let node = tree.nodes[1];
      expect(node.expanded).toBe(false);

      adapter.onModelEvent({
        target: model.id,
        type: 'nodeExpanded',
        nodeId: node.id,
        expanded: true,
        expandedLazy: false
      });
      sendQueuedAjaxCalls();
      expect(node.expanded).toBe(true);
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });
  });

  describe('collapseAll', () => {
    it('sends nodeExpanded for every collapsed node', () => {
      let model = helper.createModelFixture(3, 2, true);
      let adapter = helper.createTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      tree.render();

      let allNodes = [];
      tree.visitNodes(node => {
        allNodes.push(node);
      });

      tree.collapseAll();
      // A nodeExpanded event must be sent for every node because all nodes were initially expanded
      sendQueuedAjaxCalls();
      expect(mostRecentJsonRequest().events.length).toBe(allNodes.length);
    });
  });

  describe('onModelAction', () => {

    describe('nodesInserted event', () => {
      let model;
      let tree;
      let adapter;
      let node0;
      let node1;
      let node2;

      beforeEach(() => {
        model = helper.createModelFixture(3, 1, true);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        node2 = tree.nodes[2];
      });

      it('calls insertNodes', () => {
        spyOn(tree, 'insertNodes');

        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', {childNodeIndex: 3});
        let event = helper.createNodesInsertedEvent(model, [newNode0Child3], node0.id);
        adapter.onModelAction(event);
        expect(tree.insertNodes).toHaveBeenCalledWith([newNode0Child3], tree.nodes[0]);
      });

      it('applies defaultValues to nodes', () => {
        defaultValues.init(defaults);
        model = helper.createModelFixture(3, 1, true);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        expect(tree.a).toBe(123);
        expect(tree.nodes[0].b).toBe(234);
        expect(tree.nodes[0].childNodes[3]).toBe(undefined);

        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', {childNodeIndex: 3});
        let event = helper.createNodesInsertedEvent(model, [newNode0Child3], node0.id);
        adapter.onModelAction(event);
        expect(tree.a).toBe(123);
        expect(tree.nodes[0].childNodes[3].b).toBe(234);
      });
    });

    describe('nodesDeleted event', () => {
      let model;
      let tree;
      let adapter;
      let node0;
      let node1;
      let node2;

      beforeEach(() => {
        // A large tree is used to properly test recursion
        model = helper.createModelFixture(3, 2, true);
        tree = helper.createTree(model);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        node2 = tree.nodes[2];
      });

      it('calls deleteNodes', () => {
        spyOn(tree, 'deleteNodes');

        let node2Child0 = node2.childNodes[0];
        let event = helper.createNodesDeletedEvent(model, [node2Child0.id], node2.id);
        adapter.onModelAction(event);
        expect(tree.deleteNodes).toHaveBeenCalledWith([node2Child0], node2);
      });

    });

    describe('allChildNodesDeleted event', () => {
      let model;
      let tree;
      let adapter;
      let node0;
      let node1;
      let node2;
      let node1Child0;
      let node1Child1;
      let node1Child2;

      beforeEach(() => {
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

      it('calls deleteAllChildNodes', () => {
        spyOn(tree, 'deleteAllChildNodes');

        let event = helper.createAllChildNodesDeletedEvent(model);
        adapter.onModelAction(event);
        expect(tree.deleteAllChildNodes).toHaveBeenCalled();
      });

    });

    describe('nodesSelected event', () => {
      let model;
      let tree;
      let adapter;
      let node0;
      let child0;
      let grandchild0;

      beforeEach(() => {
        model = helper.createModelFixture(3, 3, false);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        child0 = node0.childNodes[0];
        grandchild0 = child0.childNodes[0];
      });

      it('calls selectNodes', () => {
        spyOn(tree, 'selectNodes');

        let event = helper.createNodesSelectedEvent(model, [node0.id]);
        adapter.onModelAction(event);
        expect(tree.selectNodes).toHaveBeenCalledWith([node0]);
      });

    });

    describe('nodeChanged event', () => {
      let model, tree, adapter, nodes, node0, node1, child0, child1_1;

      beforeEach(() => {
        model = helper.createModelFixture(3, 3, false);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        nodes = tree.nodes;
        node0 = nodes[0];
        node1 = nodes[1];
        child0 = node0.childNodes[0];
        child1_1 = node1.childNodes[1];
      });

      it('calls changeNode', () => {
        spyOn(tree, 'changeNode');

        let event = helper.createNodeChangedEvent(model, node0.id);
        adapter.onModelAction(event);
        expect(tree.changeNode).toHaveBeenCalled();
      });

      it('updates the text of the node', () => {
        tree.render();
        let event = helper.createNodeChangedEvent(model, node0.id);
        event.text = 'new Text';
        let message = {
          events: [event]
        };
        session._processSuccessResponse(message);
        expect(node0.text).toBe(event.text);
        expect(node0.$text.text()).toBe('new Text');
      });

    });

    describe('nodesUpdated event', () => {
      let model;
      let tree;
      let adapter;
      let node0;
      let child0;

      beforeEach(() => {
        model = helper.createModelFixture(3, 3, false);
        tree = helper.createTree(model);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        child0 = node0.childNodes[0];
      });

      it('calls updateNodes', () => {
        spyOn(tree, 'updateNodes');

        let child0Update = {
          id: child0.id,
          enabled: false
        };
        let event = helper.createNodesUpdatedEvent(model, [child0Update]);
        adapter.onModelAction(event);
        expect(tree.updateNodes).toHaveBeenCalledWith([child0Update]);
      });
    });

    describe('childNodeOrderChanged event', () => {
      let model;
      let tree;
      let adapter;
      let node0;
      let child0;

      beforeEach(() => {
        model = helper.createModelFixture(3, 3, false);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        child0 = node0.childNodes[0];
      });

      it('calls updateNodeOrder', () => {
        spyOn(tree, 'updateNodeOrder');
        let parentNode = tree.nodes[1];
        let childNode0 = parentNode.childNodes[0];
        let childNode1 = parentNode.childNodes[1];
        let childNode2 = parentNode.childNodes[2];

        let event = helper.createChildNodeOrderChangedEvent(model, [childNode2.id, childNode1.id, childNode0.id], parentNode.id);
        adapter.onModelAction(event);
        expect(tree.updateNodeOrder).toHaveBeenCalledWith([childNode2, childNode1, childNode0], parentNode);
      });

    });

    describe('multiple events', () => {
      let model;
      let tree;
      let adapter;
      let node0;
      let node1;
      let node2;

      beforeEach(() => {
        model = helper.createModelFixture(3, 1, true);
        adapter = helper.createTreeAdapter(model);
        tree = adapter.createWidget(model, session.desktop);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        node2 = tree.nodes[2];
      });

      it('handles delete, collapse, insert, expand events correctly', () => {
        tree.render();

        // Delete child nodes from node0
        let message = {
          events: [helper.createAllChildNodesDeletedEvent(model, node0.id)]
        };
        session._processSuccessResponse(message);
        expect(node0.childNodes.length).toBe(0);
        expect(helper.findAllNodes(tree).length).toBe(9);

        // Collapse node0
        let $node0 = node0.$node;
        message = {
          events: [helper.createNodeExpandedEvent(model, node0.id, false)]
        };
        session._processSuccessResponse(message);
        expect(node0.expanded).toBe(false);
        expect($node0).not.toHaveClass('expanded');

        // Insert new child node at node0
        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
        message = {
          events: [helper.createNodesInsertedEvent(model, [newNode0Child3], node0.id)]
        };
        session._processSuccessResponse(message);

        // Model should be updated, html nodes not added because node still is collapsed
        expect(node0.childNodes.length).toBe(1);
        expect(node0.childNodes[0].text).toBe(newNode0Child3.text);
        expect(Object.keys(tree.nodesMap).length).toBe(10);
        expect(helper.findAllNodes(tree).length).toBe(9); // Still 9 nodes

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
