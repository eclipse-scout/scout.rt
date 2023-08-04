/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {graphics, icons, objects, Page, Range, scout, scrollbars, strings, Tree, TreeField, TreeModel, TreeNode} from '../../src/index';
import {JQueryTesting, SpecTree, TreeSpecHelper} from '../../src/testing/index';

describe('Tree', () => {
  let session: SandboxSession;
  let helper: TreeSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TreeSpecHelper(session);
    // Tree node expansion happens with an animation (async).
    // Disabling it makes it possible to test the expansion state after the expansion
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

  describe('creation', () => {
    it('adds nodes', () => {
      let model = helper.createModelFixture(1);
      let tree = helper.createTree(model);
      tree.render();

      expect(helper.findAllNodes(tree).length).toBe(1);
    });

    it('does not add nodes if no nodes are provided', () => {
      let model = helper.createModelFixture();
      let tree = helper.createTree(model);
      tree.render();

      expect(helper.findAllNodes(tree).length).toBe(0);
    });

    it('sets childNodeIndices', () => {
      let model = helper.createModelFixture(2, 2);
      let tree = helper.createTree(model);
      expect(tree.nodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[0].childNodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[0].childNodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[1].childNodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].childNodes[1].childNodeIndex).toBe(1);
    });
  });

  describe('insertNodes', () => {
    let model: TreeModel, tree: Tree, node0: TreeNode, node1: TreeNode, node2: TreeNode;

    beforeEach(() => {
      model = helper.createModelFixture(3, 1, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
    });

    it('inserts in a reasonable order if tree is empty', () => {
      // we want to start with an empty tree for this test
      let rootNodeModel = helper.createModelNode('0', 'root');
      rootNodeModel.expanded = true;
      model = helper.createModel([rootNodeModel]);
      tree = helper.createTree(model);
      tree.render();

      // child nodes
      let nodeModels = [
        helper.createModelNode('0_0', 'node0'),
        helper.createModelNode('0_1', 'node1'),
        helper.createModelNode('0_2', 'node2')
      ];
      let rootNode = tree.nodes[0];
      tree.insertNodes(nodeModels, rootNode);

      // assert order in DOM is 0_0, 0_1, 0_2 (= same order as in array)
      let orderedNodeIdString = '';
      tree.$container.find('[data-level=\'1\']').each(function() {
        orderedNodeIdString += $(this).attr('data-nodeid') + ',';
      });
      expect(orderedNodeIdString).toBe('0_0,0_1,0_2,');
    });

    it('appends new nodes at the bottom', () => {
      tree.render();

      let nodeModels = [
        helper.createModelNode('a', 'node A'),
        helper.createModelNode('b', 'node B')
      ];
      tree.insertNodes(nodeModels);
      expect(tree.nodes[2].id).toBe(node2.id);
      expect(tree.nodes[3].text).toBe('node A');
      expect(tree.nodes[4].text).toBe('node B');
      expect(tree.nodes[3].$node.prev().text()).toBe('node 2_2');
      expect(tree.nodes[3].$node.text()).toBe('node A');
      expect(tree.nodes[3].$node.next().text()).toBe('node B');

      nodeModels = [
        helper.createModelNode('2_a', 'node 2_A'),
        helper.createModelNode('2_b', 'node 2_B')
      ];
      tree.insertNodes(nodeModels, node2);
      expect(node2.childNodes[2].text).toBe('node 2_2');
      expect(node2.childNodes[3].text).toBe('node 2_A');
      expect(node2.childNodes[3].childNodeIndex).toBe(3);
      expect(node2.childNodes[4].text).toBe('node 2_B');
      expect(node2.childNodes[4].childNodeIndex).toBe(4);
      expect(node2.childNodes[3].$node.prev().text()).toBe('node 2_2');
      expect(node2.childNodes[3].$node.text()).toBe('node 2_A');
      expect(node2.childNodes[3].$node.next().text()).toBe('node 2_B');
      expect(node2.childNodes[3].$node.next().next().text()).toBe('node A');

      nodeModels = [
        helper.createModelNode('2_c', 'node 2_C')
      ];
      tree.insertNodes(nodeModels, node2);
      expect(node2.childNodes[5].text).toBe('node 2_C');
      expect(node2.childNodes[5].childNodeIndex).toBe(5);
      expect(node2.childNodes[5].$node.prev().text()).toBe('node 2_B');
      expect(node2.childNodes[5].$node.text()).toBe('node 2_C');
      expect(node2.childNodes[5].$node.next().text()).toBe('node A');
    });

    it('appends new nodes with child nodes at the bottom', () => {
      tree.render();

      let childNodeModels = [
        helper.createModelNode('a_0', 'node A_0'),
        helper.createModelNode('a_1', 'node A_1')
      ];
      let nodeModels = [
        helper.createModelNode('a', 'node A', {expanded: true, childNodes: childNodeModels}),
        helper.createModelNode('b', 'node B')
      ];
      tree.insertNodes(nodeModels);
      expect(tree.nodes[2].id).toBe(node2.id);
      expect(tree.nodes[3].text).toBe('node A');
      expect(tree.nodes[3].childNodeIndex).toBe(3);
      expect(tree.nodes[4].text).toBe('node B');
      expect(tree.nodes[4].childNodeIndex).toBe(4);
      expect(tree.nodes[3].$node.prev().text()).toBe('node 2_2');
      expect(tree.nodes[3].$node.text()).toBe('node A');
      expect(tree.nodes[3].$node.next().text()).toBe('node A_0');
      expect(tree.nodes[3].childNodes[0].text).toBe('node A_0');
      expect(tree.nodes[3].childNodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[3].childNodes[1].text).toBe('node A_1');
      expect(tree.nodes[3].childNodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[3].childNodes[0].$node.prev().text()).toBe('node A');
      expect(tree.nodes[3].childNodes[0].$node.text()).toBe('node A_0');
      expect(tree.nodes[3].childNodes[0].$node.next().text()).toBe('node A_1');
      expect(tree.nodes[3].childNodes[0].$node.next().next().text()).toBe('node B');
    });

    it('inserts new nodes at a specific position if index is set', () => {
      let nodeModels = [
        helper.createModelNode('a', 'node A'),
        helper.createModelNode('b', 'node B')
      ];
      tree.insertNodes(nodeModels, null, 1);
      expect(tree.nodes[0].text).toBe('node 0');
      expect(tree.nodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].text).toBe('node A');
      expect(tree.nodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[2].text).toBe('node B');
      expect(tree.nodes[2].childNodeIndex).toBe(2);
      expect(tree.nodes[3].text).toBe('node 1');
      expect(tree.nodes[3].childNodeIndex).toBe(3);
      expect(tree.nodes[4].text).toBe('node 2');
      expect(tree.nodes[4].childNodeIndex).toBe(4);
    });

    it('inserts new child nodes at a specific position if index is set', () => {
      let nodeModels = [
        helper.createModelNode('a', 'node A'),
        helper.createModelNode('b', 'node B')
      ];
      tree.insertNodes(nodeModels, tree.nodes[0], 1);
      expect(tree.nodes[0].childNodes[0].text).toBe('node 0_0');
      expect(tree.nodes[0].childNodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[0].childNodes[1].text).toBe('node A');
      expect(tree.nodes[0].childNodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[0].childNodes[2].text).toBe('node B');
      expect(tree.nodes[0].childNodes[2].childNodeIndex).toBe(2);
      expect(tree.nodes[0].childNodes[3].text).toBe('node 0_1');
      expect(tree.nodes[0].childNodes[3].childNodeIndex).toBe(3);
      expect(tree.nodes[0].childNodes[4].text).toBe('node 0_2');
      expect(tree.nodes[0].childNodes[4].childNodeIndex).toBe(4);
    });

    it('inserts new nodes at a specific position if index is set and ignores childNodeIndex', () => {
      let nodeModels = [
        helper.createModelNode('a', 'node A', {childNodeIndex: 0}),
        helper.createModelNode('b', 'node B', {childNodeIndex: 2})
      ];
      tree.insertNodes(nodeModels, null, 1);
      expect(tree.nodes[0].text).toBe('node 0');
      expect(tree.nodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].text).toBe('node A');
      expect(tree.nodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[2].text).toBe('node B');
      expect(tree.nodes[2].childNodeIndex).toBe(2);
      expect(tree.nodes[3].text).toBe('node 1');
      expect(tree.nodes[3].childNodeIndex).toBe(3);
      expect(tree.nodes[4].text).toBe('node 2');
      expect(tree.nodes[4].childNodeIndex).toBe(4);
    });

    it('inserts a new node at a specific position if childNodeIndex is set and others at the end', () => {
      let nodeModels = [
        helper.createModelNode('a', 'node A', {childNodeIndex: 1}),
        helper.createModelNode('b', 'node B')
      ];
      tree.insertNodes(nodeModels);
      expect(tree.nodes[0].text).toBe('node 0');
      expect(tree.nodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].text).toBe('node A');
      expect(tree.nodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[2].text).toBe('node 1');
      expect(tree.nodes[2].childNodeIndex).toBe(2);
      expect(tree.nodes[3].text).toBe('node 2');
      expect(tree.nodes[3].childNodeIndex).toBe(3);
      expect(tree.nodes[4].text).toBe('node B');
      expect(tree.nodes[4].childNodeIndex).toBe(4);
    });

    it('inserts new nodes at a specific position if childNodeIndices are set', () => {
      let nodeModels = [
        helper.createModelNode('a', 'node A', {childNodeIndex: 1}),
        helper.createModelNode('b', 'node B', {childNodeIndex: 2})
      ];
      tree.insertNodes(nodeModels);
      expect(tree.nodes[0].text).toBe('node 0');
      expect(tree.nodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].text).toBe('node A');
      expect(tree.nodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[2].text).toBe('node B');
      expect(tree.nodes[2].childNodeIndex).toBe(2);
      expect(tree.nodes[3].text).toBe('node 1');
      expect(tree.nodes[3].childNodeIndex).toBe(3);
      expect(tree.nodes[4].text).toBe('node 2');
      expect(tree.nodes[4].childNodeIndex).toBe(4);
    });

    it('inserts new nodes at specific positions if non consecutive childNodeIndices are set', () => {
      let nodeModels = [
        helper.createModelNode('a', 'node A', {childNodeIndex: 0}),
        helper.createModelNode('b', 'node B', {childNodeIndex: 2}) // Will be before node 1 and not node 2 because node A is inserted at first
      ];
      tree.insertNodes(nodeModels);
      expect(tree.nodes[0].text).toBe('node A');
      expect(tree.nodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].text).toBe('node 0');
      expect(tree.nodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[2].text).toBe('node B');
      expect(tree.nodes[2].childNodeIndex).toBe(2);
      expect(tree.nodes[3].text).toBe('node 1');
      expect(tree.nodes[3].childNodeIndex).toBe(3);
      expect(tree.nodes[4].text).toBe('node 2');
      expect(tree.nodes[4].childNodeIndex).toBe(4);
    });

    it('updates model', () => {
      let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
      expect(tree.nodes.length).toBe(3);
      expect(Object.keys(tree.nodesMap).length).toBe(12);

      tree.insertNodes([newNode0Child3], node0);
      expect(node0.childNodes.length).toBe(4);
      expect(node0.childNodes[3].text).toBe(newNode0Child3.text);
      expect(Object.keys(tree.nodesMap).length).toBe(13);
    });

    it('updates model with a complex node containing another node', () => {
      let node1_1_0 = helper.createModelNode('1_1_0', 'node1_1_0');
      tree.insertNodes([node1_1_0], node1.childNodes[1]);

      let node2_1_0 = helper.createModelNode('2_1_0', 'node2_1_0');
      let node2_1_0_0 = helper.createModelNode('2_1_0_0', 'node2_1_0_0');
      node2_1_0.childNodes = [node2_1_0_0];
      node2_1_0.expanded = true;
      tree.insertNodes([node2_1_0], node2.childNodes[1]);

      expect(node2.childNodes.length).toBe(3);
      expect(node2.childNodes[0].childNodes.length).toBe(0);
      expect(node2.childNodes[1].childNodes.length).toBe(1);
      expect(node1.childNodes[1].childNodes[0].childNodes.length).toBe(0);
      expect(node1.childNodes[1].childNodes[0].text).toBe(node1_1_0.text);
      expect(node2.childNodes[1].childNodes[0].text).toBe(node2_1_0.text);
      expect(node2.childNodes[1].childNodes[0].childNodes[0].text).toBe(node2_1_0_0.text);
      expect(Object.keys(tree.nodesMap).length).toBe(15);
    });

    it('updates html document if parent is expanded', () => {
      tree.render();
      tree.revalidateLayoutTree();
      let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
      expect(helper.findAllNodes(tree).length).toBe(12);

      tree.insertNodes([newNode0Child3], node0);
      expect(helper.findAllNodes(tree).length).toBe(13);
      expect(node0.childNodes[3].$node.text()).toBe(newNode0Child3.text);
    });

    it('updates html document at a specific position', () => {
      tree.render();

      let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', {childNodeIndex: 2});
      let newNode0Child4 = helper.createModelNode('0_4', 'newNode0Child4', {childNodeIndex: 3});
      expect(helper.findAllNodes(tree).length).toBe(12);

      tree.insertNodes([newNode0Child3, newNode0Child4], node0);
      expect(helper.findAllNodes(tree).length).toBe(14);
      expect(node0.childNodes[2].$node.text()).toBe(newNode0Child3.text);
      expect(node0.childNodes[3].$node.text()).toBe(newNode0Child4.text);
      expect(node0.childNodes[3].$node.attr('data-level')).toBe('1');
      expect(node0.childNodes[3].$node.next().attr('data-level')).toBe('1');
      expect(node0.childNodes[3].$node.next().text()).toBe('node 0_2');

      let newNode1Child3 = helper.createModelNode('1_3', 'newNode1Child3', {childNodeIndex: 1});
      let newNode1Child4 = helper.createModelNode('1_4', 'newNode1Child4', {childNodeIndex: 2});

      tree.insertNodes([newNode1Child3, newNode1Child4]);
      expect(helper.findAllNodes(tree).length).toBe(16);
      expect(tree.nodes[1].$node.prev().text()).toBe('node 0_2');
      expect(tree.nodes[1].$node.prev().attr('data-level')).toBe('1');
      expect(tree.nodes[1].$node.text()).toBe(newNode1Child3.text);
      expect(tree.nodes[1].$node.attr('data-level')).toBe('0');
      expect(tree.nodes[2].$node.text()).toBe(newNode1Child4.text);
      expect(tree.nodes[2].$node.attr('data-level')).toBe('0');
      expect(tree.nodes[2].$node.next().attr('data-level')).toBe('0');
      expect(tree.nodes[2].$node.next().text()).toBe('node 1');
    });

    it('only updates the model if parent is collapsed', () => {
      tree.setNodeExpanded(node0, false);
      tree.render();

      let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
      expect(helper.findAllNodes(tree).length).toBe(9);

      tree.insertNodes([newNode0Child3], node0);
      // Check that the model was updated correctly
      expect(node0.childNodes.length).toBe(4);
      expect(node0.childNodes[3].text).toBe(newNode0Child3.text);
      expect(Object.keys(tree.nodesMap).length).toBe(13);

      // Check that no dom manipulation happened
      expect(helper.findAllNodes(tree).length).toBe(9);
      expect(node0.childNodes[3].$node).toBe(null);
    });

    it('expands the parent if parent.expanded = true and the new inserted nodes are the first child nodes', () => {
      model = helper.createModelFixture(3, 0, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
      tree.render();

      let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
      let $node0 = node0.$node;
      // Even tough the nodes were created with expanded=true, the $node should not have
      // been rendered as expanded (because it has no children)
      expect($node0).not.toHaveClass('expanded');
      expect(helper.findAllNodes(tree).length).toBe(3);

      tree.insertNodes([newNode0Child3], node0);
      expect(helper.findAllNodes(tree).length).toBe(4);
      expect(node0.childNodes[0].$node.text()).toBe(newNode0Child3.text);
      expect($node0).toHaveClass('expanded');
    });

    describe('with breadcrumb style', () => {

      beforeEach(() => {
        tree.setDisplayStyle(Tree.DisplayStyle.BREADCRUMB);
      });

      it('inserts a html node if the parent node is selected', () => {
        tree.render();
        tree.revalidateLayoutTree();
        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
        expect(helper.findAllNodes(tree).length).toBe(3); // top level nodes are visible

        tree.selectNode(node0);
        expect(helper.findAllNodes(tree).length).toBe(4); // only node0 and its child nodes are visible

        tree.insertNodes([newNode0Child3], node0);
        expect(helper.findAllNodes(tree).length).toBe(5);
        expect(node0.childNodes[3].$node.text()).toBe(newNode0Child3.text);
      });

      it('only updates model if the parent node is not selected', () => {
        tree.render();
        tree.revalidateLayoutTree();
        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
        expect(helper.findAllNodes(tree).length).toBe(3); // top level nodes are visible

        tree.insertNodes([newNode0Child3], node0);
        expect(helper.findAllNodes(tree).length).toBe(3); // still 3 because no node is selected
        expect(node0.childNodes[3].id).toBe(newNode0Child3.id);
        expect(node0.childNodes[3].rendered).toBe(false);
        expect(node0.childNodes[3].attached).toBe(false);
      });

      it('inserts html nodes at a specific position', () => {
        tree.render();

        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', {childNodeIndex: 2});
        let newNode0Child4 = helper.createModelNode('0_4', 'newNode0Child4', {childNodeIndex: 3});
        tree.selectNode(node0);
        expect(helper.findAllNodes(tree).length).toBe(4);

        tree.insertNodes([newNode0Child3, newNode0Child4], node0);
        expect(helper.findAllNodes(tree).length).toBe(6);
        expect(node0.childNodes[2].$node.text()).toBe(newNode0Child3.text);
        expect(node0.childNodes[3].$node.text()).toBe(newNode0Child4.text);
        expect(node0.childNodes[3].$node.attr('data-level')).toBe('1');
        expect(node0.childNodes[3].$node.next().attr('data-level')).toBe('1');
        expect(node0.childNodes[3].$node.next().text()).toBe('node 0_2');
      });
    });

    it('expands the parent if parent.expanded = true and the new inserted nodes are the first child nodes', () => {
      model = helper.createModelFixture(3, 0, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
      tree.render();

      let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
      let $node0 = node0.$node;
      // Even tough the nodes were created with expanded=true, the $node should not have
      // been rendered as expanded (because it has no children)
      expect($node0).not.toHaveClass('expanded');
      expect(helper.findAllNodes(tree).length).toBe(3);

      tree.insertNodes([newNode0Child3], node0);
      expect(helper.findAllNodes(tree).length).toBe(4);
      expect(node0.childNodes[0].$node.text()).toBe(newNode0Child3.text);
      expect($node0).toHaveClass('expanded');
    });

  });

  describe('updateNodes', () => {
    let model;
    let tree;
    let node0;
    let child0;

    beforeEach(() => {
      model = helper.createModelFixture(3, 3, false);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      child0 = node0.childNodes[0];
    });

    // this test does not create a new node instance/model for an update, but re-uses
    // an already existing node and simply changes a property of that instance
    it('update same node instance', () => {
      expect(node0.leaf).toBe(false);
      tree.render();
      node0.leaf = true;
      tree.updateNode(node0);
      // we expect that _decorateNode has been called and updates the DOM of the tree
      let $treeNode = tree.$container.find('[data-nodeid="' + node0.id + '"]').first();
      expect($treeNode.attr('class')).toContain('leaf');
    });

    describe('enabled update', () => {
      let child0Update;

      beforeEach(() => {
        child0Update = {
          id: child0.id,
          enabled: false
        };
        tree.checkable = true;
      });

      it('updates the enabled state of the model node', () => {
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);
        expect(child0.enabled).toBe(false);
      });

      it('updates the enabled state of the html node, if visible', () => {
        // Render tree and make sure child0 is visible
        tree.render();
        tree.setNodeExpanded(node0, true);
        expect(child0.$node.isEnabled()).toBe(true);
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);

        // Expect node and $node to be disabled
        expect(child0.enabled).toBe(false);
        expect(child0.$node.isEnabled()).toBe(false);
      });

      it('updates the enabled state of the html node after expansion, if not visible', () => {
        // Render tree and make sure child0 is visible
        tree.render();
        tree.setNodeExpanded(node0, true);
        expect(child0.$node.isEnabled()).toBe(true);

        // Make sure child0 is not visible anymore
        tree.setNodeExpanded(node0, false);
        expect(child0.attached).toBeFalsy();

        tree.updateNodes([child0Update]);

        // Mode state needs to be updated, $node is still node visible
        expect(child0.enabled).toBe(false);
        expect(child0.attached).toBeFalsy();

        // Expand node -> node gets visible and needs to be disabled
        tree.setNodeExpanded(node0, true);
        expect(child0.$node.isEnabled()).toBe(false);
      });
    });

    describe('enabled update on checkable tree', () => {
      let child0Update;

      function $checkbox(node) {
        return node.$node.children('.tree-node-checkbox')
          .children('.check-box');
      }

      beforeEach(() => {
        child0Update = {
          id: child0.id,
          enabled: false
        };
        tree.checkable = true;
      });

      it('updates the enabled state of the model node', () => {
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);
        expect(child0.enabled).toBe(false);
      });

      it('updates the enabled state of the html node, if visible', () => {
        // Render tree and make sure child0 is visible
        tree.render();
        tree.setNodeExpanded(node0, true);
        expect($checkbox(child0).isEnabled()).toBe(true);

        tree.updateNodes([child0Update]);

        // Expect node and $node to be disabled
        expect(child0.enabled).toBe(false);
        expect($checkbox(child0).isEnabled()).toBe(false);
      });

      it('updates the enabled state of the html node after expansion, if not visible', () => {
        // Render tree and make sure child0 is visible
        tree.render();
        tree.setNodeExpanded(node0, true);
        expect($checkbox(child0).isEnabled()).toBe(true);

        // Make sure child0 is not visible anymore
        tree.setNodeExpanded(node0, false);
        expect(child0.attached).toBeFalsy();
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);

        // Mode state needs to be updated, $node is still node visible
        expect(child0.enabled).toBe(false);
        expect(child0.attached).toBeFalsy();

        // Expand node -> node gets visible and needs to be disabled
        tree.setNodeExpanded(node0, true);
        expect($checkbox(child0).isEnabled()).toBe(false);
      });
    });
  });

  describe('changeNode', () => {
    let model, tree, nodes, node0, node1, child0, child1_1;

    beforeEach(() => {
      model = helper.createModelFixture(3, 3, false);
      tree = helper.createTree(model);
      nodes = tree.nodes;
      node0 = nodes[0];
      node1 = nodes[1];
      child0 = node0.childNodes[0];
      child1_1 = node1.childNodes[1];
    });

    it('updates the text of the model node', () => {
      node0.text = 'new Text';
      tree.changeNode(node0);
      expect(tree.nodes[0].text).toBe('new Text');
    });

    it('updates the text of the html node', () => {
      tree.render();

      node0.text = 'new Text';
      tree.changeNode(node0);
      let $node0 = node0.$node;
      expect($node0.text()).toBe('new Text');

      // Check whether tree-control is still there
      expect($node0.children('.tree-node-control').length).toBe(1);
    });

    it('updates custom cssClass of model and html node', () => {
      tree.selectedNodes = [node0];
      tree.render();

      node0.cssClass = 'new-css-class';
      tree.changeNode(node0);

      // Check model
      expect(node0.cssClass).toBe('new-css-class');

      // Check gui
      let $node0 = node0.$node;
      expect($node0).toHaveClass('new-css-class');

      // check if other classes are still there
      expect($node0).toHaveClass('tree-node');
      expect($node0).toHaveClass('selected');

      // Check if removal works
      node0.cssClass = null;
      tree.changeNode(node0);

      // Check model
      expect(node0.cssClass).toBeFalsy();

      // Check gui
      $node0 = node0.$node;
      expect($node0).not.toHaveClass('new-css-class');
      // check if other classes are still there
      expect($node0).toHaveClass('tree-node');
      expect($node0).toHaveClass('selected');
    });

    it('preserves child-of-selected when root nodes get changed', () => {
      tree.selectedNodes = [];
      tree.render();

      let $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[2].$node[0]);

      node1.text = 'new text';
      tree.changeNode(node1);
      expect(node1.text).toBe('new text');

      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[2].$node[0]);
    });

    it('preserves child-of-selected when child nodes get changed', () => {
      tree.selectedNodes = [node1];
      tree.setNodeExpanded(node1, true);
      tree.render();

      let $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(node1.childNodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(node1.childNodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(node1.childNodes[2].$node[0]);

      child1_1.text = 'new text';
      tree.changeNode(child1_1);
      expect(child1_1.text).toBe('new text');

      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(node1.childNodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(node1.childNodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(node1.childNodes[2].$node[0]);
    });

    it('preserves group css class when nodes get updated', () => {
      tree.selectNode(node1);
      tree.render();

      tree._isGroupingEnd = node => node.nodeType === Page.NodeType.TABLE;

      let $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);

      node1.text = 'new text';
      tree.changeNode(node1);
      expect(node1.text).toBe('new text');

      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);
    });

    it('update node paddings is done later and tree does not fail if tree was removed in the meantime', done => {
      node0.iconId = icons.CHART;
      node0.expanded = true;
      tree.nodePaddingLevelDiffParentHasIcon = 10;
      tree.render();
      let orig = tree._updateNodePaddingsLeft;
      // If queueMicrotask fails in changeNode, test won't fail -> create spy with try catch to ensure test fails
      spyOn(tree, SpecTree.prototype._updateNodePaddingsLeft.name).and.callFake(() => {
        try {
          orig();
        } catch (err) {
          fail(err);
        }
      });
      tree.changeNode(node0);
      expect(tree._changeNodeTaskScheduled).toBe(true);
      tree.remove();

      queueMicrotask(() => {
        expect(tree._changeNodeTaskScheduled).toBe(false);
        done();
      });
    });
  });

  describe('deleteNodes', () => {
    let model;
    let tree;
    let node0;
    let node1;
    let node2;

    beforeEach(() => {
      // A large tree is used to properly test recursion
      model = helper.createModelFixture(3, 2, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
    });

    describe('deleting a child', () => {

      it('updates model', () => {
        let node2Child0 = node2.childNodes[0];
        let node2Child1 = node2.childNodes[1];
        expect(tree.nodes.length).toBe(3);
        expect(tree.nodes[0]).toBe(node0);
        expect(Object.keys(tree.nodesMap).length).toBe(39);

        tree.deleteNodes([node2Child0], node2);
        expect(tree.nodes[2].childNodes.length).toBe(2);
        expect(tree.nodes[2].childNodes[0]).toBe(node2Child1);
        expect(Object.keys(tree.nodesMap).length).toBe(35);
      });

      it('updates html document', () => {
        tree.setViewRangeSize(40);
        tree.render();

        let node2Child0 = node2.childNodes[0];

        expect(helper.findAllNodes(tree).length).toBe(39);
        expect(node2Child0.$node).toBeDefined();

        tree.deleteNodes([node2Child0], node2);
        expect(helper.findAllNodes(tree).length).toBe(35);
        expect(node2Child0.$node).toBe(null);

        expect(node0.$node).toBeDefined();
        expect(node0.childNodes[0].$node).toBeDefined();
        expect(node0.childNodes[1].$node).toBeDefined();
        expect(node0.childNodes[2].$node).toBeDefined();
      });

      it('updates child node indices', () => {
        let node2Child0 = node2.childNodes[0];
        expect(tree.nodes.length).toBe(3);
        expect(node2.childNodes.length).toBe(3);
        expect(node2.childNodes[0].childNodeIndex).toBe(0);
        expect(node2.childNodes[1].childNodeIndex).toBe(1);
        expect(node2.childNodes[2].childNodeIndex).toBe(2);

        tree.deleteNodes([node2Child0], node2);
        expect(node2.childNodes.length).toBe(2);
        expect(node2.childNodes[0].childNodeIndex).toBe(0);
        expect(node2.childNodes[1].childNodeIndex).toBe(1);

        tree.deleteNodes([tree.nodes[1]]);
        expect(tree.nodes.length).toBe(2);
        expect(tree.nodes[0].childNodeIndex).toBe(0);
        expect(tree.nodes[1].childNodeIndex).toBe(1);
      });

      it('considers view range (distinguishes between rendered and non rendered nodes, adjusts viewRangeRendered)', () => {
        // initial view range
        model = helper.createModelFixture(6, 0, false);
        tree = helper.createTree(model);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        let lastNode = tree.nodes[5];

        let spy = spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new Range(1, 4));
        tree.render();
        expect(tree.viewRangeRendered).toEqual(new Range(1, 4));
        expect(tree.$nodes().length).toBe(3);
        expect(tree.nodes.length).toBe(6);

        // reset spy -> view range now starts from 0
        spy.and.callThrough();
        tree.viewRangeSize = 3;

        // delete first (not rendered)
        tree.deleteNodes([node0]);
        expect(tree.viewRangeDirty).toBeTruthy();
        tree._renderViewport();
        expect(tree.viewRangeDirty).toBeFalsy();
        expect(tree.viewRangeRendered).toEqual(new Range(0, 3));
        expect(tree.$nodes().length).toBe(3);
        expect(tree.nodes.length).toBe(5);

        // delete first rendered
        tree.deleteNodes([node1]);
        expect(tree.viewRangeDirty).toBeTruthy();
        tree._renderViewport();
        expect(tree.viewRangeDirty).toBeFalsy();
        expect(tree.viewRangeRendered).toEqual(new Range(0, 3));
        expect(tree.$nodes().length).toBe(3);
        expect(tree.nodes.length).toBe(4);

        // delete last node not rendered
        tree.deleteNodes([lastNode]);
        expect(tree.viewRangeDirty).toBeTruthy();
        tree._renderViewport();
        expect(tree.viewRangeDirty).toBeFalsy();
        expect(tree.viewRangeRendered).toEqual(new Range(0, 3));
        expect(tree.$nodes().length).toBe(3);
        expect(tree.nodes.length).toBe(3);

        // delete remaining (rendered) nodes
        tree.deleteNodes([tree.nodes[0], tree.nodes[1], tree.nodes[2]]);
        expect(tree.viewRangeDirty).toBeTruthy();
        tree._renderViewport();
        expect(tree.viewRangeDirty).toBeFalsy();
        expect(tree.viewRangeRendered).toEqual(new Range(0, 0));
        expect(tree.$nodes().length).toBe(0);
        expect(tree.nodes.length).toBe(0);
        expect(tree.$fillBefore.height()).toBe(0);
        expect(tree.$fillAfter.height()).toBe(0);
      });

      /**
       * With only a single node, $data.outerHeight() / tree.nodeHeight would result in 1 (because both have equal height)
       * Without the correction from ticket #262890 the function would return 2 (1 * 2), but it must return min. 4.
       */
      it('calculateViewRangeSize should not return values < 4', () => {
        model = helper.createModelFixture(1, 0, false);
        tree = helper.createTree(model);
        tree.render();
        expect(tree.calculateViewRangeSize()).toBe(4);
      });
    });

    describe('deleting a root node', () => {
      it('updates model', () => {
        tree.deleteNodes([node0]);
        expect(tree.nodes.length).toBe(2);
        expect(tree.nodes[0]).toBe(node1);
        expect(Object.keys(tree.nodesMap).length).toBe(26);
      });

      it('updates html document', () => {
        tree.setViewRangeSize(30);
        tree.render();

        tree.deleteNodes([node0]);
        expect(tree.visibleNodesFlat.length).toBe(26);
        expect(node0.$node).toBe(null);
        expect(tree.nodes.indexOf(node0)).toBe(-1);
        expect(node0.childNodes[0].$node).toBe(null);
        expect(node0.childNodes[1].$node).toBe(null);
        expect(node0.childNodes[2].$node).toBe(null);
      });

      describe('deleting a collapsed root node', () => {
        it('updates model', () => {
          tree.setNodeExpanded(node0, false);
          node0.expanded = false;

          tree.deleteNodes([node0]);
          expect(tree.nodes.length).toBe(2);
          expect(tree.nodes[0]).toBe(node1);
          expect(Object.keys(tree.nodesMap).length).toBe(26);
        });

        it('updates html document', () => {
          tree.setViewRangeSize(30);
          tree.setNodeExpanded(node0, false);
          tree.render();

          tree.deleteNodes([node0]);
          expect(helper.findAllNodes(tree).length).toBe(26);
          expect(node0.$node).toBe(null);
          expect(tree.nodes.indexOf(node0)).toBe(-1);
          expect(node0.childNodes[0].$node).toBe(null);
          expect(node0.childNodes[1].$node).toBe(null);
          expect(node0.childNodes[2].$node).toBe(null);
        });
      });
    });

    describe('deleting all nodes', () => {
      it('updates model', () => {
        tree.deleteNodes([node0, node1, node2]);
        expect(tree.nodes.length).toBe(0);
        expect(Object.keys(tree.nodesMap).length).toBe(0);
      });

      it('updates html document', () => {
        tree.render();

        tree.deleteNodes([node0, node1, node2]);
        expect(helper.findAllNodes(tree).length).toBe(0);
      });
    });

    describe('deleting child nodes without commentParentNode', () => {

      it('updates model', () => {
        let node1Child2 = node1.childNodes[2];
        let node2Child0 = node2.childNodes[0];
        let node2Child1 = node2.childNodes[1];
        let node2Child2 = node2.childNodes[2];
        expect(tree.nodes.length).toBe(3);
        expect(tree.nodes[0]).toBe(node0);
        expect(Object.keys(tree.nodesMap).length).toBe(39); // 3 + 9 + 27

        tree.deleteNodes([node1Child2, node2Child0, node2Child1, node0]); // <-- no second argument (common parent node)
        expect(tree.nodes.length).toBe(2);
        expect(tree.nodes[0]).toBe(node1);
        expect(tree.nodes[1]).toBe(node2);
        expect(tree.nodes[0].childNodes.length).toBe(2);
        expect(tree.nodes[1].childNodes.length).toBe(1);
        expect(tree.nodes[1].childNodes[0]).toBe(node2Child2);
        expect(Object.keys(tree.nodesMap).length).toBe(14); // 39 - (1 + 3 + 9) - 3*(1 + 3) = 39 - 13 - 12
      });
    });

    it('deselects the deleted nodes', () => {
      tree.render();
      let childNode1_1 = tree.nodes[1].childNodes[1];
      let childNode2_1 = tree.nodes[2].childNodes[1];
      tree.selectNode(node0);
      expect(tree.selectedNodes.length).toBe(1);
      tree.deleteNode(node0);
      expect(tree.selectedNodes.length).toBe(0);

      tree.selectNode(childNode1_1);
      expect(tree.selectedNodes.length).toBe(1);
      tree.deleteNode(node1);
      expect(tree.selectedNodes.length).toBe(0);

      tree.selectNode(childNode2_1.childNodes[0]);
      expect(tree.selectedNodes.length).toBe(1);
      tree.deleteAllChildNodes(node2);
      expect(tree.selectedNodes.length).toBe(0);
    });

    it('unchecks the deleted nodes', () => {
      tree.setCheckable(true);
      tree.render();
      let childNode1_1 = tree.nodes[1].childNodes[1];
      let childNode2_1 = tree.nodes[2].childNodes[1];
      tree.checkNode(node0);
      expect(tree.checkedNodes.length).toBe(1);
      tree.deleteNode(node0);
      expect(tree.checkedNodes.length).toBe(0);

      tree.checkNode(childNode1_1);
      expect(tree.checkedNodes.length).toBe(1);
      tree.deleteNode(node1);
      expect(tree.checkedNodes.length).toBe(0);

      tree.checkNode(childNode2_1.childNodes[0]);
      expect(tree.checkedNodes.length).toBe(1);
      tree.deleteAllChildNodes(node2);
      expect(tree.checkedNodes.length).toBe(0);
    });

  });

  describe('deleteAllChildNodes', () => {
    let model;
    let tree;
    let node0;
    let node1;
    let node2;
    let node1Child0;
    let node1Child1;
    let node1Child2;

    beforeEach(() => {
      model = helper.createModelFixture(3, 1, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
      node1Child0 = node1.childNodes[0];
      node1Child1 = node1.childNodes[1];
      node1Child2 = node1.childNodes[1];
    });

    it('deletes all nodes from model', () => {
      expect(tree.nodes.length).toBe(3);
      expect(Object.keys(tree.nodesMap).length).toBe(12);

      tree.deleteAllChildNodes();
      expect(tree.nodes.length).toBe(0);
      expect(Object.keys(tree.nodesMap).length).toBe(0);
    });

    it('deletes all nodes from html document', () => {
      tree.render();

      expect(helper.findAllNodes(tree).length).toBe(12);

      tree.deleteAllChildNodes();
      expect(helper.findAllNodes(tree).length).toBe(0);
    });

    it('deletes all nodes from model for a given parent', () => {
      expect(tree.nodes.length).toBe(3);
      expect(Object.keys(tree.nodesMap).length).toBe(12);

      tree.deleteAllChildNodes(node1);
      expect(node1.childNodes.length).toBe(0);
      expect(Object.keys(tree.nodesMap).length).toBe(9);
    });

    it('deletes all nodes from html document for a given parent', () => {
      tree.render();

      expect(helper.findAllNodes(tree).length).toBe(12);

      tree.deleteAllChildNodes(node1);
      expect(helper.findAllNodes(tree).length).toBe(9);

      // Check that children are removed, parent must still exist
      expect(node1.$node).toBeDefined();
      expect(node1Child0.$node).toBe(null);
      expect(node1Child1.$node).toBe(null);
      expect(node1Child2.$node).toBe(null);
    });

  });

  describe('checkNodes', () => {

    function findCheckedNodes(nodes) {
      let checkedNodes = [];
      for (let j = 0; j < nodes.length; j++) {
        if (nodes[j].checked) {
          checkedNodes.push(nodes[j]);
        }
      }
      return checkedNodes;
    }

    it('checks a subnode -> mark upper nodes ', () => {
      let model = helper.createModelFixture(4, 4);
      let tree = helper.createTree(model);
      tree.render();
      tree.checkable = true;

      let node;
      // find node with more than one child level
      for (let i = 0; i < tree.nodes.length; i++) {
        if (tree.nodes[i].childNodes && tree.nodes[i].childNodes.length > 0 && tree.nodes[i].childNodes[0].childNodes && tree.nodes[i].childNodes[0].childNodes.length > 0) {
          node = tree.nodes[i].childNodes[0].childNodes[0];
          break;
        }
      }

      if (node) {
        tree.checkNode(node, true);
      }

      while (node.parentNode) {
        node = node.parentNode;
        expect(node.childrenChecked).toEqual(true);
      }
    });

    it('checks a node -> mark upper nodes -> uncheck node and test if node keeps marked because children are checked', () => {
      let model = helper.createModelFixture(4, 4);
      let tree = helper.createTree(model);
      tree.render();
      tree.checkable = true;
      let node, nodeToCheck;
      // find node with more than one child level
      for (let i = 0; i < tree.nodes.length; i++) {
        if (tree.nodes[i].childNodes && tree.nodes[i].childNodes.length > 0 && tree.nodes[i].childNodes[0].childNodes && tree.nodes[i].childNodes[0].childNodes.length > 0) {
          node = tree.nodes[i].childNodes[0].childNodes[0];
          nodeToCheck = tree.nodes[i].childNodes[0];
          break;
        }
      }

      if (node) {
        tree.checkNode(node, true);
      }
      tree.checkNode(nodeToCheck, true);
      let tmpNode = nodeToCheck;
      // upper nodes should be marked
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }
      expect(nodeToCheck.childNodes[0].checked).toEqual(true);

      // remove check state on second level node-> second level node should be marked because children of it are checked
      tree.checkNode(nodeToCheck, false);
      expect(nodeToCheck.checked).toEqual(false);
      expect(nodeToCheck.childrenChecked).toEqual(true);
      tmpNode = nodeToCheck;
      // upper nodes should be marked
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }
    });

    it('checks a subnode and its sibling -> mark upper nodes -> uncheck one of the siblings', () => {
      let model = helper.createModelFixture(4, 4);
      let tree = helper.createTree(model);
      tree.render();
      tree.checkable = true;
      let nodeOne, nodeTwo;
      // find node with more than one child level
      for (let i = 0; i < tree.nodes.length; i++) {
        if (tree.nodes[i].childNodes && tree.nodes[i].childNodes.length > 0 && tree.nodes[i].childNodes[0].childNodes && tree.nodes[i].childNodes[0].childNodes.length > 1) {
          nodeOne = tree.nodes[i].childNodes[0].childNodes[0];
          nodeTwo = tree.nodes[i].childNodes[0].childNodes[1];
          break;
        }
      }
      if (nodeOne && nodeTwo) {
        tree.checkNode(nodeOne, true);
        tree.checkNode(nodeTwo, true);
      }
      // check if all upper nodes are marked
      let tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }

      // uncheck one of the two siblings
      tree.checkNode(nodeTwo, false);
      // marks on upper should exist
      tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }

      // uncheck second siblings
      tree.checkNode(nodeOne, false);
      // marks on upper should be removed
      tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(false);
      }
    });

    it('does not check a disabled node', () => {
      let model = helper.createModelFixture(4, 4);
      let tree = helper.createTree(model);
      tree.render();
      tree.checkable = true;

      let node = tree.nodes[0];
      node.enabled = false;
      tree.checkNode(node, true);
      expect(node.checked).toEqual(false);
    });

    it('does not check a node in a disabled tree', () => {
      let model = helper.createModelFixture(4, 4);
      let tree = helper.createTree(model);
      tree.setEnabled(false);
      tree.render();

      let node = tree.nodes[0];
      tree.checkNode(node, true);
      expect(node.checked).toEqual(false);
    });

    it('never checks two nodes if multiCheck is set to false', () => {
      let model = helper.createModelFixture(4, 4);
      let tree = helper.createTree(model);
      tree.multiCheck = false;
      tree.checkable = true;
      tree.render();

      let node = tree.nodes[0],
        nodeTwo = tree.nodes[1];

      if (node && nodeTwo) {
        tree.checkNode(node, true);
        tree.checkNode(nodeTwo, true);
      }

      let checkedNodes = findCheckedNodes(tree.nodes);
      expect(checkedNodes.length).toBe(1);
    });

    it('checks children if autoCheckChildren is set to true', () => {
      let model = helper.createModelFixture(2, 2);
      let tree = helper.createTree(model);
      tree.multiCheck = true;
      tree.checkable = true;
      tree.autoCheckChildren = true;
      tree.render();

      let node = tree.nodes[0];
      tree.checkNode(node, true);
      expect(node.checked).toEqual(true);
      // every descendant needs to be checked
      Tree.visitNodes(node => {
        expect(node.checked).toEqual(true);
      }, node.childNodes);
    });

    it('does not check the children if autoCheckChildren is set to false', () => {
      let model = helper.createModelFixture(4, 4);
      let tree = helper.createTree(model);
      tree.multiCheck = true;
      tree.checkable = true;
      tree.autoCheckChildren = false;
      tree.render();

      let node = tree.nodes[0];
      tree.checkNode(node, true);
      expect(node.checked).toEqual(true);
      // no descendant must be checked
      Tree.visitNodes(node => {
        expect(node.checked).toEqual(false);
      }, node.childNodes);
    });

    it('does not check nodes if checkable is set to false', () => {
      let model = helper.createModelFixture(4, 4);
      let tree = helper.createTree(model);
      tree.multiCheck = false;
      tree.checkable = false;
      tree.render();

      let node = tree.nodes[0];
      tree.checkNode(node, true);

      let checkedNodes = [];
      for (let j = 0; j < tree.nodes.length; j++) {
        if (tree.nodes[j].checked) {
          checkedNodes.push(tree.nodes[j]);
        }
      }
      expect(checkedNodes.length).toBe(0);
    });

    it('checkablestyle.checkbox_tree_node checks node with click event', () => {
      let model = helper.createModelFixture(5, 0);
      model.checkableStyle = Tree.CheckableStyle.CHECKBOX_TREE_NODE;
      model.checkable = true;
      let tree = helper.createTree(model);
      tree.render();

      let nodes = tree.nodes;

      let checkedNodes = findCheckedNodes(nodes);
      expect(checkedNodes.length).toBe(0);

      JQueryTesting.triggerClick(tree.$nodes().eq(2));
      JQueryTesting.triggerClick(tree.$nodes().eq(1));

      checkedNodes = findCheckedNodes(nodes);
      expect(checkedNodes.length).toBe(2);

      // unchecking node 1 wouldn't work since then the tree's doubleClick handler would detect the second click as a double click
      JQueryTesting.triggerClick(tree.$nodes().eq(2));

      checkedNodes = findCheckedNodes(nodes);
      expect(checkedNodes.length).toBe(1);
    });

  });

  describe('node click', () => {

    it('calls tree._onNodeMouseDown', () => {
      let model = helper.createModelFixture(1);
      let tree = helper.createTree(model);
      spyOn(tree, '_onNodeMouseDown');
      tree.render();

      let $node = tree.$container.find('.tree-node:first');
      JQueryTesting.triggerMouseDown($node);

      expect(tree._onNodeMouseDown).toHaveBeenCalled();
    });

    it('updates model (selection)', () => {
      let model = helper.createModelFixture(1);
      let tree = helper.createTree(model);
      tree.render();

      expect(tree.selectedNodes.length).toBe(0);

      let $node = tree.$container.find('.tree-node:first');
      JQueryTesting.triggerClick($node);

      expect(tree.selectedNodes.length).toBe(1);
      expect(tree.selectedNodes[0].id).toBe(tree.nodes[0].id);
    });

  });

  describe('node double click', () => {
    it('expands/collapses the node', () => {
      let model = helper.createModelFixture(1, 1, false);
      let tree = helper.createTree(model);
      tree.render();

      let $node = tree.$container.find('.tree-node:first');
      expect($node).not.toHaveClass('expanded');

      JQueryTesting.triggerDoubleClick($node);
      expect($node).toHaveClass('expanded');

      JQueryTesting.triggerDoubleClick($node);
      expect($node).not.toHaveClass('expanded');
    });
  });

  describe('node control double click', () => {
    it('does the same as control single click (does NOT expand and immediately collapse again)', () => {
      let model = helper.createModelFixture(1, 1, false);
      let tree = helper.createTree(model);
      tree.render();

      let $nodeControl = tree.$container.find('.tree-node-control:first');
      let $node = $nodeControl.parent();
      expect($node).not.toHaveClass('expanded');

      JQueryTesting.triggerDoubleClick($nodeControl);
      expect($node).toHaveClass('expanded');

      // Reset internal state because there is no "sleep" in JS
      tree._doubleClickSupport._lastTimestamp -= 5000; // simulate last click 5 seconds ago

      JQueryTesting.triggerDoubleClick($nodeControl);
      expect($node).not.toHaveClass('expanded');
    });
  });

  describe('checkable node double click', () => {
    it('doesn\'t expands/collapses the node with checkable style checkbox_tree_node (default)', () => {
      let model = helper.createModelFixture(1, 1, false);
      model.checkable = true;
      let tree = helper.createTree(model);
      tree.render();

      let $node = tree.$container.find('.tree-node:first');
      expect($node).not.toHaveClass('expanded');

      JQueryTesting.triggerDoubleClick($node);
      expect($node).not.toHaveClass('expanded');
    });

    it('expands/collapses the node with checkable style checkbox', () => {
      let model = helper.createModelFixture(1, 1, false);
      model.checkableStyle = Tree.CheckableStyle.CHECKBOX;
      model.checkable = true;
      let tree = helper.createTree(model);
      tree.render();

      let $node = tree.$container.find('.tree-node:first');
      expect($node).not.toHaveClass('expanded');

      JQueryTesting.triggerDoubleClick($node);
      expect($node).toHaveClass('expanded');

      JQueryTesting.triggerDoubleClick($node);
      expect($node).not.toHaveClass('expanded');
    });
  });

  describe('deselectAll', () => {

    it('clears the selection', () => {
      let model = helper.createModelFixture(1, 1);
      let node0 = model.nodes[0];
      model.selectedNodes = [node0.id];

      let tree = helper.createTree(model);
      tree.render();
      expect(tree.$selectedNodes().length).toBe(1);

      tree.deselectAll();

      // Check model
      expect(tree.selectedNodes.length).toBe(0);

      // Check gui
      expect(tree.$selectedNodes().length).toBe(0);
    });
  });

  describe('selectNodes', () => {

    it('selects a node', () => {
      let model = helper.createModelFixture(3, 3, false);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];

      tree.render();
      expect(tree.$selectedNodes().length).toBe(0);
      expect(node0.$node.isSelected()).toBe(false);

      tree.selectNodes([node0]);
      // Check model
      expect(tree.selectedNodes.length).toBe(1);
      expect(tree.selectedNodes[0].id).toBe(node0.id);

      // Check gui
      expect(tree.$selectedNodes().length).toBe(1);
      expect(node0.$node.isSelected()).toBe(true);
    });

    it('selectedNode()', () => {
      let model = helper.createModelFixture(2, 2);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];
      let node1 = tree.nodes[1];

      // single selection
      tree.selectNode(node0);
      expect(tree.selectedNode()).toBe(node0);
      tree.deselectAll();

      // multi selection
      tree.selectNodes([node0, node1]);
      expect(tree.selectedNode()).toBe(node0);
      tree.deselectAll();

      // no selection
      expect(tree.selectedNode()).toBe(null);
    });

    it('expands the parents if a hidden node should be selected whose parents are collapsed (revealing the selection)', () => {
      let model = helper.createModelFixture(3, 3, false);
      let tree = helper.createTree(model);
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
    });

    it('only shows selected in breadcrumb mode', () => {
      let model = helper.createModelFixture(10, 2);
      let tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      let nodeToExpand = tree.nodes[1];
      tree._expandAllParentNodes(nodeToExpand);
      tree.setNodeExpanded(nodeToExpand, true);
      tree.setNodeExpanded(nodeToExpand, false);
      expect(nodeToExpand.expanded).toBe(false);

      let firstExpanded = nodeToExpand.childNodes[2];
      // tree.setNodeExpanded(firstExpanded, true);
      tree.doNodeAction(firstExpanded, true);
      expect(firstExpanded.expanded).toBe(true);

      let secondExpanded = nodeToExpand.childNodes[3];
      // tree.setNodeExpanded(secondExpanded, true);
      tree.doNodeAction(secondExpanded, true);
      expect(secondExpanded.expanded).toBe(true);

      tree.selectNode(firstExpanded);
      expect(secondExpanded.filterAccepted).toBe(true); // still visible

      tree.setDisplayStyle(Tree.DisplayStyle.BREADCRUMB);

      expect(secondExpanded.filterAccepted).toBe(false); // no longer visible
    });

    it('only shows selected in breadcrumb mode even if a child has filter accepted=true', () => {
      let model = helper.createModelFixture(2, 2);
      let tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      // To reproduce manually in an outline tree, the node needs to be expanded, collapsed and expanded again.
      tree.setNodeExpanded(tree.nodes[0], true);

      let firstChild = tree.nodes[0].childNodes[0];
      let secondChild = tree.nodes[0].childNodes[1];
      tree.setNodeExpanded(firstChild, true);
      tree.setNodeExpanded(firstChild, false); // Remove from visible list, filterAccepted is still true

      tree.selectNode(secondChild);
      expect(firstChild.filterAccepted).toBe(true);
      expect(firstChild.childNodes[0].filterAccepted).toBe(true);
      expect(secondChild.filterAccepted).toBe(true);

      tree.setDisplayStyle(Tree.DisplayStyle.BREADCRUMB);
      expect(firstChild.filterAccepted).toBe(false);
      expect(firstChild.childNodes[0].filterAccepted).toBe(false);
      expect(secondChild.filterAccepted).toBe(true);
    });

    it('in breadcrumb mode renders children and removes preceding sibling nodes', () => {
      let model = helper.createModelFixture(10, 3);
      let tree = helper.createTree(model);
      let node0_3 = tree.nodes[0].childNodes[3];
      tree.render(session.$entryPoint);
      tree.selectNode(tree.nodes[0]);
      tree.expandNode(tree.nodes[0]);
      tree.setDisplayStyle(Tree.DisplayStyle.BREADCRUMB);
      tree.selectNode(node0_3);

      // nodes before selected node must not be visible in bread crumb mode
      for (let i = 2; i >= 0; i--) {
        expect(tree.nodes[0].childNodes[i].filterAccepted).toBe(false);
        expect(tree.nodes[0].childNodes[i].attached).toBe(false);
      }
      expect(node0_3.filterAccepted).toBe(true);
      expect(node0_3.attached).toBe(true);
      expect(node0_3.childNodes[0].attached).toBe(true);
      expect(node0_3.childNodes[1].attached).toBe(true);
    });

    it('also expands the node if bread crumb mode is enabled', () => {
      let model = helper.createModelFixture(1, 1);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];

      tree.displayStyle = Tree.DisplayStyle.BREADCRUMB;
      tree.render();

      tree.selectNodes(node0);

      expect(tree.selectedNodes.indexOf(node0) > -1).toBe(true);
      expect(node0.expanded).toBe(true);
    });

    it('also expands the parents in breadcrumb mode if a hidden node should be selected after being expanded and collapsed while in its hidden state', () => {
      let model = helper.createModelFixture(3, 3, false);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];
      let node1 = tree.nodes[1];

      tree.setDisplayStyle(Tree.DisplayStyle.BREADCRUMB);
      tree.render(session.$entryPoint);

      expect(node0.expanded).toBe(false);
      expect(node1.expanded).toBe(false);

      tree.selectNodes([node0]);
      expect(node0.$node.isSelected()).toBe(true);
      expect(node0.expanded).toBe(true);
      expect(tree.$selectedNodes().length).toBe(1);

      tree.setNodeExpanded(node1, true);
      tree.setNodeExpanded(node1, false);
      tree.selectNode(node1);

      expect(node1.$node.isSelected()).toBe(true);
      expect(node1.expanded).toBe(true);
      expect(tree.$selectedNodes().length).toBe(1);
    });

    it('sets css class ancestor-of-selected on every ancestor of the selected element', () => {
      let model = helper.createModelFixture(3, 3);
      let tree = helper.createTree(model);
      let nodes = tree.nodes;
      let node1 = nodes[1];
      let child1 = node1.childNodes[1];
      let grandchild1 = child1.childNodes[1];

      tree.render();

      let $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(0);

      tree.selectNodes(node1);
      jasmine.clock().tick(1);
      $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(0);

      tree.selectNodes(child1);
      jasmine.clock().tick(1);
      $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(1);
      expect($parents.eq(0)[0]).toBe(nodes[1].$node[0]);

      tree.selectNodes(grandchild1);
      jasmine.clock().tick(1);
      $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(2);
      expect($parents.eq(0)[0]).toBe(nodes[1].$node[0]);
      expect($parents.eq(1)[0]).toBe(nodes[1].childNodes[1].$node[0]);

      tree.deselectAll();
      $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(0);
    });

    it('sets css class child-of-selected on direct children of the selected element', () => {
      let model = helper.createModelFixture(3, 1, true);
      let tree = helper.createTree(model);
      let nodes = tree.nodes;
      let node1 = nodes[1];

      tree.render();

      let $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[2].$node[0]);

      // all nodes are expanded
      tree.selectNodes(node1);
      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[1].childNodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].childNodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[1].childNodes[2].$node[0]);

      tree.deselectAll();
      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[2].$node[0]);
    });

    it('may select a node which is not rendered', () => {
      let model = helper.createModelFixture(3, 3);
      let tree = helper.createTree(model);
      let nodes = tree.nodes;
      let child0 = nodes[1].childNodes[0];
      let child1 = nodes[1].childNodes[1];
      tree.viewRangeSize = 1;
      tree.render();

      expect(nodes[1].rendered).toBe(false);
      expect(child0.rendered).toBe(false);
      expect(child1.rendered).toBe(false);

      tree.selectNodes(child1);
      expect(child0.rendered).toBe(false);
      expect(child1.rendered).toBe(false);

      tree._renderViewRangeForNode(child1);
      expect(child0.rendered).toBe(false);
      expect(child1.rendered).toBe(true);
      expect(child1.$node).toHaveClass('selected');

      // Select another not rendered node (makes sure remove selection works well)
      tree.selectNodes(child0);
      tree._renderViewRangeForNode(child0);
      expect(child0.rendered).toBe(true);
      expect(child0.$node).toHaveClass('selected');
      expect(child1.attached).toBe(false);
      expect(child1.$node).not.toHaveClass('selected');
    });

    it('sets parent and ancestor css classes even if nodes are not rendered', () => {
      let model = helper.createModelFixture(3, 3);
      let tree = helper.createTree(model);
      let nodes = tree.nodes;
      let child1 = nodes[1].childNodes[1];
      let grandChild1 = child1.childNodes[1];
      tree.viewRangeSize = 1;
      tree.render();

      // Only render one node
      tree._expandAllParentNodes(grandChild1);
      tree._renderViewRangeForNode(grandChild1);
      expect(nodes[1].rendered).toBe(false);
      expect(grandChild1.rendered).toBe(true);

      // Selected the rendered node -> parent and child nodes won't be updated because they are not rendered
      tree.selectNodes(grandChild1);
      expect(grandChild1.$node).toHaveClass('selected');

      // Render range for parent
      tree._renderViewRangeForNode(child1);
      expect(child1.$node).toHaveClass('parent-of-selected');

      // Render range for grand parent
      tree._renderViewRangeForNode(nodes[1]);
      expect(nodes[1].$node).toHaveClass('ancestor-of-selected');
    });

    it('sets child-of-selected css class even if nodes are not rendered', () => {
      let model = helper.createModelFixture(3, 3);
      let tree = helper.createTree(model);
      let nodes = tree.nodes;
      let child1 = nodes[1].childNodes[1];
      let grandChild1 = child1.childNodes[1];
      tree.viewRangeSize = 1;
      tree.render();

      // Only render one node
      tree._expandAllParentNodes(grandChild1);
      tree._renderViewRangeForNode(child1);
      expect(nodes[1].rendered).toBe(false);
      expect(child1.rendered).toBe(true);
      expect(grandChild1.rendered).toBe(false);

      // Selected the rendered node -> child nodes won't be updated because they are not rendered
      tree.selectNodes(child1);
      expect(child1.$node).toHaveClass('selected');

      // Render range for grandChild
      tree._renderViewRangeForNode(grandChild1);
      expect(grandChild1.$node).toHaveClass('child-of-selected');
    });
  });

  describe('expandNode', () => {
    let model, tree, nodes, node1;

    beforeEach(() => {
      model = helper.createModelFixture(3, 3);
      tree = helper.createTree(model);
      nodes = tree.nodes;
      node1 = nodes[1];
    });

    it('sets css class child-of-selected on direct children if the expanded node is selected', () => {
      tree.render();

      tree.selectNodes(node1);
      let $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(0);

      tree.expandNode(node1);
      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[1].childNodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].childNodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[1].childNodes[2].$node[0]);

      tree.collapseNode(node1);
      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(0);
    });

    it('renders the child nodes if parent is expanded', () => {
      tree.render();

      let $nodes = helper.findAllNodes(tree);
      expect($nodes.length).toBe(3);

      tree.expandNode(node1);
      $nodes = helper.findAllNodes(tree);
      expect($nodes.length).toBe(6);
      expect(node1.$node[0]).toBe($nodes[1]);
      expect(node1.childNodes[0].$node[0]).toBe($nodes[2]);
      expect(node1.childNodes[1].$node[0]).toBe($nodes[3]);
      expect(node1.childNodes[2].$node[0]).toBe($nodes[4]);
    });

    describe('with breadcrumb style', () => {

      beforeEach(() => {
        tree.setDisplayStyle(Tree.DisplayStyle.BREADCRUMB);
      });

      it('renders the child nodes if parent is expanded', () => {
        tree.render();

        let $nodes = helper.findAllNodes(tree);
        expect($nodes.length).toBe(3);

        tree.selectNode(node1); // select node calls expand node
        $nodes = helper.findAllNodes(tree);
        expect($nodes.length).toBe(4);
        expect(node1.$node[0]).toBe($nodes[0]);
        expect(node1.childNodes[0].$node[0]).toBe($nodes[1]);
        expect(node1.childNodes[1].$node[0]).toBe($nodes[2]);
        expect(node1.childNodes[2].$node[0]).toBe($nodes[3]);
      });

      it('ensures top level nodes are rendered', () => {
        tree.render(session.$entryPoint);

        let node0 = tree.nodes[0];
        let node1 = tree.nodes[1];
        tree.selectNode(node1);
        tree.deleteNodes([node1]);
        tree.expandNode(node0);
        tree.selectNode(node0);

        expect(tree.visibleNodesMap[node0.id]).toBe(true);
        expect(node0.rendered).toBe(true);
        expect(node0.attached).toBe(true);
      });
    });
  });

  describe('expandAllParentNodes', () => {

    it('expands all parent nodes of the given node (model)', () => {
      let model = helper.createModelFixture(3, 3);
      let tree = helper.createTree(model);
      let nodes = tree.nodes;

      expect(nodes[0].expanded).toBe(false);
      expect(nodes[0].childNodes[0].expanded).toBe(false);
      expect(nodes[0].childNodes[0].childNodes[0].expanded).toBe(false);

      tree._expandAllParentNodes(nodes[0].childNodes[0].childNodes[0]);
      expect(nodes[0].expanded).toBe(true);
      expect(nodes[0].childNodes[0].expanded).toBe(true);
      expect(nodes[0].childNodes[0].childNodes[0].expanded).toBe(false);
    });

    it('expands all parent nodes of the given node (html)', () => {
      let model = helper.createModelFixture(3, 3);
      let tree = helper.createTree(model);
      let nodes = tree.nodes;
      tree.render();

      expect(nodes[0].$node).not.toHaveClass('expanded');
      expect(nodes[0].childNodes[0].$node).toBeFalsy();
      expect(nodes[0].childNodes[0].childNodes[0].$node).toBeFalsy();

      tree._expandAllParentNodes(nodes[0].childNodes[0].childNodes[0]);
      expect(nodes[0].$node).toHaveClass('expanded');
      expect(nodes[0].childNodes[0].$node).toHaveClass('expanded');
      expect(nodes[0].childNodes[0].childNodes[0].$node).not.toHaveClass('expanded');
    });

  });

  describe('lazyExpandCollapse', () => {

    it('manual expand -> manual collapse node', () => {
      let model = helper.createModelFixture(3, 2);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];
      tree.render();

      expect(node0.expanded).toBe(false);
      tree.expandNode(node0, {lazy: false});
      expect(node0.expanded).toBe(true);
      tree.collapseNode(node0, {lazy: false});
      expect(node0.expanded).toBe(false);
    });

    it('manual expand -> lazy collapse node', () => {
      let model = helper.createModelFixture(3, 2);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];
      tree.render();

      expect(node0.expanded).toBe(false);
      tree.expandNode(node0, {lazy: false});
      expect(node0.expanded).toBe(true);
      tree.collapseNode(node0, {lazy: true});
      expect(node0.expanded).toBe(false);
    });

    it('lazy expand -> manual collapse node', () => {
      let model = helper.createModelFixture(3, 2);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];
      tree.render();

      expect(node0.expanded).toBe(false);
      tree.expandNode(node0, {lazy: true});
      expect(node0.expanded).toBe(true);
      tree.collapseNode(node0, {lazy: false});
      expect(node0.expanded).toBe(false);
    });

    it('lazy expand -> lazy collapse node', () => {
      let model = helper.createModelFixture(3, 2);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];
      tree.render();

      expect(node0.expanded).toBe(false);
      tree.expandNode(node0, {lazy: true});
      expect(node0.expanded).toBe(true);
      tree.collapseNode(node0, {lazy: true});
      expect(node0.expanded).toBe(false);
    });

    it('renders nodes correctly when disabling lazy expansion', () => {
      let model = helper.createModelFixture();
      let tree = helper.createTree(model);
      tree.render();
      // Simulate table page > node page > table page -> table page has lazyExpanding set to true, node page to false
      let nodes = helper.createModelNodes(3, 0, {
        lazyExpandingEnabled: true,
        expandedLazy: true
      });
      nodes[1].childNodes = [
        helper.createModelNode('1_0', '1_0'),
        helper.createModelNode('1_1', '1_1'),
        helper.createModelNode('1_2', '1_2')
      ];
      tree.insertNodes(nodes);
      let node1 = tree.nodes[1];

      let $nodes = helper.findAllNodes(tree);
      expect($nodes.length).toBe(3);
      nodes = [
        helper.createModelNode('1_1_0', '1_1_0', {
          lazyExpandingEnabled: true,
          expandedLazy: true
        }), helper.createModelNode('1_1_1', '1_1_1', {
          lazyExpandingEnabled: true,
          expandedLazy: true
        }),
        helper.createModelNode('1_1_2', '1_1_2', {
          lazyExpandingEnabled: true,
          expandedLazy: true
        })];
      tree.insertNodes(nodes, node1.childNodes[1]);
      tree.expandNode(node1, {lazy: false});
      $nodes = helper.findAllNodes(tree);
      expect($nodes.length).toBe(6);
      expect(node1.$node[0]).toBe($nodes[1]);
      expect(node1.childNodes[0].$node[0]).toBe($nodes[2]);
      expect(node1.childNodes[1].$node[0]).toBe($nodes[3]);
      expect(node1.childNodes[2].$node[0]).toBe($nodes[4]);
      expect(tree.visibleNodesFlat.map(n => n.text)).toEqual(['node 0', 'node 1', '1_0', '1_1', '1_2', 'node 2']);

      tree.expandNode(node1.childNodes[1], {lazy: false});
      $nodes = helper.findAllNodes(tree);
      expect($nodes.length).toBe(9);
      expect(node1.$node[0]).toBe($nodes[1]);
      expect(node1.childNodes[0].$node[0]).toBe($nodes[2]);
      expect(node1.childNodes[1].$node[0]).toBe($nodes[3]);
      expect(node1.childNodes[1].childNodes[0].$node[0]).toBe($nodes[4]);
      expect(node1.childNodes[1].childNodes[1].$node[0]).toBe($nodes[5]);
      expect(node1.childNodes[1].childNodes[2].$node[0]).toBe($nodes[6]);
      expect(node1.childNodes[2].$node[0]).toBe($nodes[7]);
      expect(tree.visibleNodesFlat.map(n => n.text)).toEqual(['node 0', 'node 1', '1_0', '1_1', '1_1_0', '1_1_1', '1_1_2', '1_2', 'node 2']);
    });

  });

  describe('collapseNode', () => {

    it('prevents collapsing in bread crumb mode if node is selected', () => {
      let model = helper.createModelFixture(1, 1);
      let tree = helper.createTree(model);
      let node0 = tree.nodes[0];

      tree.displayStyle = Tree.DisplayStyle.BREADCRUMB;
      tree.render();

      tree.selectNodes(node0);

      expect(tree.selectedNodes.indexOf(node0) > -1).toBe(true);
      expect(node0.expanded).toBe(true);

      tree.collapseNode(node0);

      // Still true
      expect(node0.expanded).toBe(true);
    });
  });

  describe('collapseAll', () => {

    it('collapses all nodes', () => {
      let i;
      let model = helper.createModelFixture(3, 2, true);
      let tree = helper.createTree(model);
      tree.render();

      let allNodes = [];
      tree.visitNodes(node => {
        allNodes.push(node);
      });

      for (i = 0; i < allNodes.length; i++) {
        expect(allNodes[i].expanded).toBe(true);
      }

      tree.collapseAll();
      for (i = 0; i < allNodes.length; i++) {
        expect(allNodes[i].expanded).toBe(false);
      }
    });
  });

  describe('updateItemPath', () => {
    let model, tree, node1, child1, grandchild1, nodes;

    beforeEach(() => {
      model = helper.createModelFixture(3, 3);
      tree = helper.createTree(model);
      nodes = tree.nodes;
      node1 = nodes[1];
      child1 = node1.childNodes[1];
      grandchild1 = child1.childNodes[1];
    });

    it('Sets css class group on every element within the same group', () => {
      tree.render();
      tree._isGroupingEnd = node => node.nodeType === Page.NodeType.TABLE;

      tree.selectNodes([]);
      tree._renderViewport();
      let $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(0);

      tree.selectNodes(node1);
      tree._renderViewport();
      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);

      node1.nodeType = Page.NodeType.TABLE;
      tree.selectNodes(child1);
      tree._renderViewport();
      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(child1.$node[0]);

      node1.nodeType = Page.NodeType.TABLE;
      tree.selectNodes(grandchild1);
      tree._renderViewport();
      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(4);
      expect($groupNodes.eq(0)[0]).toBe(child1.$node[0]);
      expect($groupNodes.eq(1)[0]).toBe(child1.childNodes[0].$node[0]);
      expect($groupNodes.eq(2)[0]).toBe(child1.childNodes[1].$node[0]);
      expect($groupNodes.eq(3)[0]).toBe(child1.childNodes[2].$node[0]);
    });
  });

  describe('updateNodeOrder', () => {
    let model, tree, nodes;

    beforeEach(() => {
      model = helper.createModelFixture(3, 3);
      tree = helper.createTree(model);
      nodes = tree.nodes;
    });

    it('reorders the child nodes if parent is given (model)', () => {
      let parentNode = nodes[1];
      let childNode0 = parentNode.childNodes[0];
      let childNode1 = parentNode.childNodes[1];
      let childNode2 = parentNode.childNodes[2];

      tree.updateNodeOrder([childNode2, childNode1, childNode0], parentNode);
      expect(tree.nodes[1].childNodes.length).toBe(3);
      expect(tree.nodes[1].childNodes[0]).toBe(childNode2);
      expect(tree.nodes[1].childNodes[1]).toBe(childNode1);
      expect(tree.nodes[1].childNodes[2]).toBe(childNode0);

      // verify indices
      expect(tree.nodes[1].childNodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].childNodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[1].childNodes[2].childNodeIndex).toBe(2);

      // verify flat list (no node is expanded -> only 3 nodes visible)
      expect(tree.visibleNodesFlat.length).toBe(3);
      expect(tree.visibleNodesFlat[0]).toBe(nodes[0]);
      expect(tree.visibleNodesFlat[1]).toBe(nodes[1]);
      expect(tree.visibleNodesFlat[2]).toBe(nodes[2]);
    });

    it('reorders the child nodes if parent is given and expanded (model)', () => {
      let parentNode = nodes[1];
      let childNode0 = parentNode.childNodes[0];
      let childNode1 = parentNode.childNodes[1];
      let childNode2 = parentNode.childNodes[2];

      tree.expandNode(parentNode);
      tree.updateNodeOrder([childNode2, childNode1, childNode0], parentNode);
      expect(tree.nodes[1].childNodes.length).toBe(3);
      expect(tree.nodes[1].childNodes[0]).toBe(childNode2);
      expect(tree.nodes[1].childNodes[1]).toBe(childNode1);
      expect(tree.nodes[1].childNodes[2]).toBe(childNode0);

      // verify indices
      expect(tree.nodes[1].childNodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].childNodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[1].childNodes[2].childNodeIndex).toBe(2);

      // verify flat list
      expect(tree.visibleNodesFlat.length).toBe(6);
      expect(tree.visibleNodesFlat[0]).toBe(nodes[0]);
      expect(tree.visibleNodesFlat[1]).toBe(nodes[1]);
      expect(tree.visibleNodesFlat[2]).toBe(childNode2);
      expect(tree.visibleNodesFlat[3]).toBe(childNode1);
      expect(tree.visibleNodesFlat[4]).toBe(childNode0);
      expect(tree.visibleNodesFlat[5]).toBe(nodes[2]);
    });

    it('reorders the child nodes if parent is given (html)', () => {
      let parentNode = nodes[1];
      let childNode0 = parentNode.childNodes[0];
      let childNode1 = parentNode.childNodes[1];
      let childNode2 = parentNode.childNodes[2];
      tree.render();
      tree.expandNode(parentNode);

      tree.updateNodeOrder([childNode2, childNode1, childNode0], parentNode);
      let $childNodes = parentNode.$node.nextUntil(nodes[2].$node);
      expect($childNodes.eq(0).data('node').id).toBe(childNode2.id);
      expect($childNodes.eq(1).data('node').id).toBe(childNode1.id);
      expect($childNodes.eq(2).data('node').id).toBe(childNode0.id);
    });

    it('considers view range when updating child node order', () => {
      let parentNode = nodes[0];
      let childNode0 = parentNode.childNodes[0];
      let childNode1 = parentNode.childNodes[1];
      let childNode2 = parentNode.childNodes[2];
      tree.viewRangeSize = 3;
      tree.render();
      tree.expandNode(parentNode);

      // Needs explicit rendering of the viewport because this would be done later by the layout
      tree._renderViewport();
      tree.updateNodeOrder([childNode2, childNode1, childNode0], parentNode);
      // Needs explicit rendering again...
      tree._renderViewport();

      expect(tree.viewRangeRendered).toEqual(new Range(0, 3));
      let $nodes = tree.$nodes();
      expect($nodes.length).toBe(3);
      expect(tree.nodes.length).toBe(3);
      expect(parentNode.childNodes.length).toBe(3);
      expect($nodes.eq(0).data('node').id).toBe(parentNode.id);
      expect($nodes.eq(1).data('node').id).toBe(childNode2.id);
      expect($nodes.eq(2).data('node').id).toBe(childNode1.id);
    });

    it('reorders expanded child nodes if parent is given (model)', () => {
      let parentNode = nodes[1];
      let childNode0 = parentNode.childNodes[0];
      let childNode1 = parentNode.childNodes[1];
      let childNode2 = parentNode.childNodes[2];

      tree.expandNode(parentNode);
      tree.expandNode(childNode1);
      tree.updateNodeOrder([childNode2, childNode0, childNode1], parentNode);
      expect(tree.nodes[1].childNodes.length).toBe(3);
      expect(tree.nodes[1].childNodes[0]).toBe(childNode2);
      expect(tree.nodes[1].childNodes[1]).toBe(childNode0);
      expect(tree.nodes[1].childNodes[2]).toBe(childNode1);

      // verify indices
      expect(tree.nodes[1].childNodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].childNodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[1].childNodes[2].childNodeIndex).toBe(2);

      // verify flat list
      expect(tree.visibleNodesFlat.length).toBe(9);
      expect(tree.visibleNodesFlat[0]).toBe(nodes[0]);
      expect(tree.visibleNodesFlat[1]).toBe(nodes[1]);
      expect(tree.visibleNodesFlat[2]).toBe(childNode2);
      expect(tree.visibleNodesFlat[3]).toBe(childNode0);
      expect(tree.visibleNodesFlat[4]).toBe(childNode1);
      expect(tree.visibleNodesFlat[5]).toBe(childNode1.childNodes[0]);
      expect(tree.visibleNodesFlat[6]).toBe(childNode1.childNodes[1]);
      expect(tree.visibleNodesFlat[7]).toBe(childNode1.childNodes[2]);
      expect(tree.visibleNodesFlat[8]).toBe(nodes[2]);
    });

    it('reorders the root nodes if no parent is given (model)', () => {
      let node0 = nodes[0];
      let node1 = nodes[1];
      let node2 = nodes[2];

      tree.updateNodeOrder([node2, node1, node0]);
      expect(tree.nodes.length).toBe(3);
      expect(tree.nodes[0]).toBe(node2);
      expect(tree.nodes[1]).toBe(node1);
      expect(tree.nodes[2]).toBe(node0);

      // verify indices
      expect(tree.nodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[2].childNodeIndex).toBe(2);

      // verify flat list
      expect(tree.visibleNodesFlat.length).toBe(3);
      expect(tree.visibleNodesFlat[0]).toBe(node2);
      expect(tree.visibleNodesFlat[1]).toBe(node1);
      expect(tree.visibleNodesFlat[2]).toBe(node0);
    });

    it('reorders the root nodes if no parent is given (html)', () => {
      let node0 = nodes[0];
      let node1 = nodes[1];
      let node2 = nodes[2];

      tree.render();
      tree.updateNodeOrder([node2, node1, node0]);
      let $nodes = tree.$nodes();
      expect($nodes.eq(0).data('node').id).toBe(node2.id);
      expect($nodes.eq(1).data('node').id).toBe(node1.id);
      expect($nodes.eq(2).data('node').id).toBe(node0.id);
    });

    it('reorders expanded root nodes if no parent is given (model)', () => {
      let node0 = nodes[0];
      let node1 = nodes[1];
      let node2 = nodes[2];

      tree.expandNode(node1);
      tree.updateNodeOrder([node2, node0, node1]);
      expect(tree.nodes.length).toBe(3);
      expect(tree.nodes[0]).toBe(node2);
      expect(tree.nodes[1]).toBe(node0);
      expect(tree.nodes[2]).toBe(node1);

      // verify indices
      expect(tree.nodes[0].childNodeIndex).toBe(0);
      expect(tree.nodes[1].childNodeIndex).toBe(1);
      expect(tree.nodes[2].childNodeIndex).toBe(2);

      // verify flat list
      expect(tree.visibleNodesFlat.length).toBe(6);
      expect(tree.visibleNodesFlat[0]).toBe(node2);
      expect(tree.visibleNodesFlat[1]).toBe(node0);
      expect(tree.visibleNodesFlat[2]).toBe(node1);
      expect(tree.visibleNodesFlat[3]).toBe(node1.childNodes[0]);
      expect(tree.visibleNodesFlat[4]).toBe(node1.childNodes[1]);
      expect(tree.visibleNodesFlat[5]).toBe(node1.childNodes[2]);
    });

    it('reorders expanded root nodes if no parent is given (html)', () => {
      let node0 = nodes[0];
      let node1 = nodes[1];
      let node2 = nodes[2];

      tree.render();
      tree.expandNode(node1);
      tree.updateNodeOrder([node2, node0, node1]);
      let $nodes = tree.$nodes();
      expect($nodes.eq(0).data('node').id).toBe(node2.id);
      expect($nodes.eq(1).data('node').id).toBe(node0.id);
      expect($nodes.eq(2).data('node').id).toBe(node1.id);
      expect($nodes.eq(3).data('node').id).toBe(node1.childNodes[0].id);
      expect($nodes.eq(4).data('node').id).toBe(node1.childNodes[1].id);
      expect($nodes.eq(5).data('node').id).toBe(node1.childNodes[2].id);
    });
  });

  describe('tree filter', () => {

    it('filters nodes when filter() is called', () => {
      let model = helper.createModelFixture(1, 1, true);
      let tree = helper.createTree(model);
      tree.render();

      let filter = {
        accept: node => node === tree.nodes[0]
      };
      tree.addFilter(filter);
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(false);

      tree.removeFilter(filter);
      tree.filter();
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(true);
    });

    it('filters nodes when filter is added and removed', () => {
      let model = helper.createModelFixture(1, 1, true);
      let tree = helper.createTree(model);
      let filter = {
        accept: node => node === tree.nodes[0]
      };
      tree.addFilter(filter);
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(false);

      tree.removeFilter(filter);
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(true);
    });

    it('makes sure only filtered nodes are displayed when node gets expanded', () => {
      let model = helper.createModelFixture(2, 1);
      let tree = helper.createTree(model);
      let filter = {
        accept: node => node === tree.nodes[0] || node === tree.nodes[0].childNodes[0]
      };
      tree.addFilter(filter);
      tree.render();

      expect(tree.nodes[0].rendered).toBe(true);
      expect(tree.nodes[1].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[0].rendered).toBeFalsy();
      expect(tree.nodes[0].childNodes[1].rendered).toBeFalsy();

      tree.expandNode(tree.nodes[0]);
      expect(tree.nodes[0].rendered).toBe(true);
      expect(tree.nodes[1].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[0].rendered).toBe(true);
      expect(tree.nodes[0].childNodes[1].rendered).toBe(false);
    });

    it('applies filter if a node gets changed', () => {
      let model = helper.createModelFixture(2, 1);
      let tree = helper.createTree(model);
      let filter = {
        accept: node => node.text === 'node 0'
      };
      tree.addFilter(filter);
      tree.render();

      expect(tree.nodes[0].attached).toBe(true);
      expect(tree.nodes[1].attached).toBe(false);

      tree.nodes[0].text = 'new Text';
      tree.changeNode(tree.nodes[0]);

      expect(tree.nodes[0].text).toBe('new Text');
      // text has changed -> filter condition returns false -> must not be visible anymore
      expect(tree.nodes[0].attached).toBe(false);
      expect(tree.nodes[1].attached).toBe(false);
    });

    it('applies filter if a node gets inserted', () => {
      let model = helper.createModelFixture(2, 1, true);
      let tree = helper.createTree(model);
      let filter = {
        accept: node => node.text === 'node 0'
      };
      tree.addFilter(filter);
      tree.render();

      expect(tree.nodes[0].childNodes.length).toBe(2);
      expect(tree.nodes[0].rendered).toBe(true);
      expect(tree.nodes[1].rendered).toBe(false);

      let newNode = helper.createModelNode('', 'newNode0Child1');
      tree.insertNodes([newNode], tree.nodes[0]);
      expect(tree.nodes[0].childNodes.length).toBe(3);
      expect(tree.nodes[0].rendered).toBe(true);
      expect(tree.nodes[1].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[0].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[1].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[2].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[2].filterAccepted).toBe(false);

      newNode = helper.createModelNode('', 'node 0');
      tree.insertNodes([newNode], tree.nodes[0]);
      expect(tree.nodes[0].childNodes.length).toBe(4);
      expect(tree.nodes[0].rendered).toBe(true);
      expect(tree.nodes[1].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[0].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[1].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[2].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[2].filterAccepted).toBe(false);
      expect(tree.nodes[0].childNodes[3].rendered).toBe(true);
      expect(tree.nodes[0].childNodes[3].filterAccepted).toBe(true);
    });

    /**
     * This test makes sure the bugfix from ticket #168957 still works.
     *
     * Without the bugfix, an exception was thrown on the second call to _renderViewport().
     * The reason was, that node 'A+B' was initially outside the view-range and when the
     * filter has changed, the B-nodes below A+B were rendered at the wrong position in
     * the tree, because A+B was not attached, so later the check in _insertNodeInDOMAtPlace
     * failed and caused the error. To fix the error, we now make sure the node is attached
     * by calling showNode in Tree#filter.
     */
    it('make sure nodes unchanged by filters are attached. See ticket #168957', () => {
      let i,
        topLevelNode3,
        topLevelNodes = [],
        childNodes = [],
        childNodeNames = ['A1', 'A2', 'A3', 'A4', 'A+B', 'B1', 'B2', 'B3', 'B4'];

      // child nodes
      for (i = 0; i < childNodeNames.length; i++) {
        childNodes.push(helper.createModelNode('', childNodeNames[i]));
      }

      // top level nodes
      topLevelNodes.push(helper.createModelNode('', 'TopLevel 1'));
      topLevelNodes.push(helper.createModelNode('', 'TopLevel 2'));

      topLevelNode3 = helper.createModelNode('', 'TopLevel 3');
      topLevelNode3.childNodes = childNodes;
      topLevelNodes.push(topLevelNode3);

      topLevelNodes.push(helper.createModelNode('', 'TopLevel 4'));
      topLevelNodes.push(helper.createModelNode('', 'TopLevel 5'));

      // filters
      let model = helper.createModel(topLevelNodes);
      let tree = helper.createTree(model);
      let filterA = {
        accept: node => {
          if (node.level === 0) {
            return true;
          }
          return strings.startsWith(node.text, 'A');

        }
      };
      let filterB = {
        accept: node => {
          if (node.level === 0) {
            return true;
          }
          return strings.startsWith(node.text, 'B') || node.text === 'A+B';

        }
      };

      // test
      tree.setViewRangeSize(5);
      tree.setNodeExpanded(tree.visibleNodesFlat[2], true);
      tree.render();

      tree.addFilter(filterA);
      tree._renderViewport();

      tree.addFilter(filterB);
      tree.removeFilter(filterA);
      tree._renderViewport();

      // check expected tree state
      expect(tree.visibleNodesFlat.length).toBe(10); // 5 top level nodes + 5 child nodes
      expect(tree.visibleNodesFlat[0].text).toBe('TopLevel 1');
      expect(tree.visibleNodesFlat[1].text).toBe('TopLevel 2');
      expect(tree.visibleNodesFlat[2].text).toBe('TopLevel 3');
      expect(tree.visibleNodesFlat[3].text).toBe('A+B');
      expect(tree.visibleNodesFlat[4].text).toBe('B1');
    });

    it('shows nodes correctly if nodes are made hidden right before', done => {
      let model = helper.createModelFixture(2, 1, true);
      let tree = helper.createTree(model);
      let node1 = tree.nodes[1];
      let filter = {
        accept: node => node.text === 'node 0'
      };
      tree.render();

      $.fx.off = false;
      jasmine.clock().uninstall();

      tree.addFilter(filter);
      tree.removeFilter(filter);

      node1.$node.promise().then(() => {
        expect(node1.rendered).toBe(true);
        expect(node1.attached).toBe(true);
        expect(node1.$node).not.toHaveClass('hiding showing');
        expect(tree.runningAnimations).toBe(0);
        done();
      });
    });

    it('show/hide parents correctly depending on their children', () => {
      let model = helper.createModelFixture(3, 2, true),
        tree = helper.createTree(model),
        node_0 = tree.nodesMap['0'],
        node_0_0 = tree.nodesMap['0_0'],
        node_0_0_0 = tree.nodesMap['0_0_0'], node_0_0_1 = tree.nodesMap['0_0_1'], node_0_0_2 = tree.nodesMap['0_0_2'],
        node_0_1 = tree.nodesMap['0_1'],
        node_0_1_0 = tree.nodesMap['0_1_0'], node_0_1_1 = tree.nodesMap['0_1_1'], node_0_1_2 = tree.nodesMap['0_1_2'],
        node_0_2 = tree.nodesMap['0_2'],
        node_0_2_0 = tree.nodesMap['0_2_0'], node_0_2_1 = tree.nodesMap['0_2_1'], node_0_2_2 = tree.nodesMap['0_2_2'],
        node_1 = tree.nodesMap['1'],
        node_1_0 = tree.nodesMap['1_0'],
        node_1_0_0 = tree.nodesMap['1_0_0'], node_1_0_1 = tree.nodesMap['1_0_1'], node_1_0_2 = tree.nodesMap['1_0_2'],
        node_1_1 = tree.nodesMap['1_1'],
        node_1_1_0 = tree.nodesMap['1_1_0'], node_1_1_1 = tree.nodesMap['1_1_1'], node_1_1_2 = tree.nodesMap['1_1_2'],
        node_1_2 = tree.nodesMap['1_2'],
        node_1_2_0 = tree.nodesMap['1_2_0'], node_1_2_1 = tree.nodesMap['1_2_1'], node_1_2_2 = tree.nodesMap['1_2_2'],
        node_2 = tree.nodesMap['2'],
        node_2_0 = tree.nodesMap['2_0'],
        node_2_0_0 = tree.nodesMap['2_0_0'], node_2_0_1 = tree.nodesMap['2_0_1'], node_2_0_2 = tree.nodesMap['2_0_2'],
        node_2_1 = tree.nodesMap['2_1'],
        node_2_1_0 = tree.nodesMap['2_1_0'], node_2_1_1 = tree.nodesMap['2_1_1'], node_2_1_2 = tree.nodesMap['2_1_2'],
        node_2_2 = tree.nodesMap['2_2'],
        node_2_2_0 = tree.nodesMap['2_2_0'], node_2_2_1 = tree.nodesMap['2_2_1'], node_2_2_2 = tree.nodesMap['2_2_2'],
        allNodes = [
          node_0,
          node_0_0,
          node_0_0_0, node_0_0_1, node_0_0_2,
          node_0_1,
          node_0_1_0, node_0_1_1, node_0_1_2,
          node_0_2,
          node_0_2_0, node_0_2_1, node_0_2_2,
          node_1,
          node_1_0,
          node_1_0_0, node_1_0_1, node_1_0_2,
          node_1_1,
          node_1_1_0, node_1_1_1, node_1_1_2,
          node_1_2,
          node_1_2_0, node_1_2_1, node_1_2_2,
          node_2,
          node_2_0,
          node_2_0_0, node_2_0_1, node_2_0_2,
          node_2_1,
          node_2_1_0, node_2_1_1, node_2_1_2,
          node_2_2,
          node_2_2_0, node_2_2_1, node_2_2_2
        ],
        expectExactlyNodesToBeVisible = exactlyVisibleNodes => {
          Tree.visitNodes(node => {
            if (exactlyVisibleNodes.indexOf(node) > -1) {
              expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            } else {
              expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[node.id]).toBeFalsy();
            }
          }, tree.nodes);
        };

      tree.render();

      [node_2, node_2_0, node_2_1, node_2_2].forEach(node => {
        node.enabled = false;
      });

      let idEndsWith0Filter = node => strings.endsWith(node.id, '0'),
        level0Filter = node => node.level === 0,
        enabledFilter = node => node.enabled;

      expectExactlyNodesToBeVisible(allNodes);

      tree.addFilter(idEndsWith0Filter);

      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0, node_0_0, node_0_0_0, node_0_1_0, node_0_2_0, node_1_0, node_1_0_0, node_1_1_0, node_1_2_0, node_2_0, node_2_0_0, node_2_1_0, node_2_2_0,
        // nodes not matching the filter but with children matching the filter
        node_0_1, node_0_2, node_1, node_1_1, node_1_2, node_2, node_2_1, node_2_2
      ]);

      tree.addFilter(level0Filter);

      // notice the nodes that where only visible due to their children are now hidden as there are no children anymore that match the filter
      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0
        // nodes not matching the filter but with children matching the filter
        // -
      ]);

      tree.removeFilter(idEndsWith0Filter);

      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0, node_1, node_2
        // nodes not matching the filter but with children matching the filter
        // -
      ]);

      tree.setFilters(idEndsWith0Filter);

      // notice node_1 and node_2 changed from "nodes matching the filter" to "nodes not matching the filter but with children matching the filter"
      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0, node_0_0, node_0_0_0, node_0_1_0, node_0_2_0, node_1_0, node_1_0_0, node_1_1_0, node_1_2_0, node_2_0, node_2_0_0, node_2_1_0, node_2_2_0,
        // nodes not matching the filter but with children matching the filter
        node_0_1, node_0_2, node_1, node_1_1, node_1_2, node_2, node_2_1, node_2_2
      ]);

      tree.addFilter(enabledFilter);

      // situation stays the same as all nodes in level 2 are enabled, only node_2_0 is now a "node not matching the filter but with children matching the filter"
      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0, node_0_0, node_0_0_0, node_0_1_0, node_0_2_0, node_1_0, node_1_0_0, node_1_1_0, node_1_2_0, node_2_0_0, node_2_1_0, node_2_2_0,
        // nodes not matching the filter but with children matching the filter
        node_0_1, node_0_2, node_1, node_1_1, node_1_2, node_2, node_2_0, node_2_1, node_2_2
      ]);

      // set node_2_0_0 and node_2_1_0 disabled, this will apply the filters again on those nodes
      tree.updateNodes([node_2_0_0, node_2_1_0].map(node => ({
        id: node.id,
        enabled: false
      })));

      // as node_2_0_0 and node_2_1_0 are now disabled, they and their parent node_2_0 and node_2_1 are no longer visible
      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0, node_0_0, node_0_0_0, node_0_1_0, node_0_2_0, node_1_0, node_1_0_0, node_1_1_0, node_1_2_0, node_2_2_0,
        // nodes not matching the filter but with children matching the filter
        node_0_1, node_0_2, node_1, node_1_1, node_1_2, node_2, node_2_2
      ]);

      // set node_2_2_0 disabled, this will apply the filters again on those nodes
      tree.updateNodes({
        id: node_2_2_0.id,
        enabled: false
      });

      // as node_2_2_0 is now disabled, the whole 2-branch (node_2 and all children) is no longer visible
      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0, node_0_0, node_0_0_0, node_0_1_0, node_0_2_0, node_1_0, node_1_0_0, node_1_1_0, node_1_2_0,
        // nodes not matching the filter but with children matching the filter
        node_0_1, node_0_2, node_1, node_1_1, node_1_2
      ]);

      // set the remaining nodes in the 2-branch on level 2 disabled, this will apply the filters again on those nodes
      tree.updateNodes([node_2_0_1, node_2_0_2, node_2_1_1, node_2_1_2, node_2_2_1, node_2_2_2].map(node => ({
        id: node.id,
        enabled: false
      })));

      // as the whole 2-branch was already invisible the situation stays the same
      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0, node_0_0, node_0_0_0, node_0_1_0, node_0_2_0, node_1_0, node_1_0_0, node_1_1_0, node_1_2_0,
        // nodes not matching the filter but with children matching the filter
        node_0_1, node_0_2, node_1, node_1_1, node_1_2
      ]);

      tree.removeFilter(idEndsWith0Filter);

      // the 0-branch and the 1-branch are enabled and therefore visible, the 2-branch stays invisible
      expectExactlyNodesToBeVisible([
        // nodes matching the filter
        node_0, node_0_0, node_0_0_0, node_0_0_1, node_0_0_2, node_0_1, node_0_1_0, node_0_1_1, node_0_1_2, node_0_2, node_0_2_0, node_0_2_1, node_0_2_2,
        node_1, node_1_0, node_1_0_0, node_1_0_1, node_1_0_2, node_1_1, node_1_1_0, node_1_1_1, node_1_1_2, node_1_2, node_1_2_0, node_1_2_1, node_1_2_2
        // nodes not matching the filter but with children matching the filter
        // -
      ]);

      tree.setFilters([]);

      expectExactlyNodesToBeVisible(allNodes);
    });
  });

  describe('test visible list and map', () => {

    describe('with initial all expanded nodes', () => {
      let model, tree;
      beforeEach(() => {
        model = helper.createModelFixture(3, 2, true);
        tree = helper.createTree(model);
      });

      it('init with all expanded in correct order', () => {
        let index = 0;
        tree.visitNodes(node => {
          expect(tree.visibleNodesFlat.indexOf(node) === index).toBeTruthy();
          expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          index++;
        });
      });

      it('collapse a node -> all children have to be removed', () => {
        let collapseNode = tree.nodes[0];
        tree.collapseNode(collapseNode);

        tree.nodes.forEach(node => {
          expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
          expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          if (node === collapseNode) {
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            }, node.childNodes);
          } else {
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            }, node.childNodes);
          }
        });
      });

      it('filter node -> filtered node is visible due to not filtered children', () => {
        let filterNode = tree.nodes[0];
        let filter = {
          accept: node => node !== filterNode
        };
        tree.addFilter(filter);

        tree.nodes.forEach(node => {
          if (node === filterNode) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            }, node.childNodes);
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            }, node.childNodes);
          }
        });

        tree.collapseNode(filterNode);

        tree.nodes.forEach(node => {
          if (node === filterNode) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            }, node.childNodes);
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            }, node.childNodes);
          }
        });
      });

      it('update node -> node is filtered', () => {

        let filter = {
          accept: node => node.enabled
        };
        tree.addFilter(filter);

        tree.visitNodes(childNode => {
          expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
          expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
        });

        let nodeToChange = tree.nodes[0],
          clones = [];

        tree.visitNodes(node => {
          if (node.level === 0 && node !== nodeToChange) {
            return true;
          }
          clones.push({
            checked: node.checked,
            childNodeIndex: node.childNodeIndex,
            childNodes: node.childNodes,
            enabled: false,
            expanded: node.expanded,
            expandedLazy: node.expandedLazy,
            id: node.id,
            lazyExpandingEnabled: node.lazyExpandingEnabled,
            leaf: node.leaf,
            text: node.text
          });
          return false;
        });

        tree.updateNodes(clones);

        tree.nodes.forEach(node => {
          if (node === nodeToChange) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            }, node.childNodes);
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            }, node.childNodes);
          }
        });
      });

      it('insert expanded node to expanded parent', () => {
        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
        newNode0Child3.expanded = true;
        tree.insertNodes([newNode0Child3], tree.nodes[0]);

        let newNode0Child3Child0 = helper.createModelNode('0_3_1', 'newNode0Child3Child0');
        let treeNodeC3 = tree.nodeById(newNode0Child3.id);
        tree.insertNodes([newNode0Child3Child0], treeNodeC3);
        let treeNodeC3C0 = tree.nodeById(newNode0Child3Child0.id);

        expect(tree.visibleNodesFlat.indexOf(treeNodeC3) > -1).toBeTruthy();
        expect(tree.visibleNodesMap[treeNodeC3.id]).toBeTruthy();
        expect(tree.visibleNodesFlat.indexOf(treeNodeC3C0) > -1).toBeTruthy();
        expect(tree.visibleNodesMap[treeNodeC3C0.id]).toBeTruthy();
      });

      it('insert child node in filtered parent', () => {
        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
        newNode0Child3.expanded = true;
        let filter = {
          accept: node => !(strings.startsWith(node.id, '0') && !strings.endsWith(node.id, '3'))
        };
        tree.addFilter(filter);

        tree.nodes.forEach(node => {
          if (node === tree.nodes[0]) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            }, node.childNodes);
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            }, node.childNodes);
          }
        });
        tree.insertNodes([newNode0Child3], tree.nodes[0]);
        tree.nodes.forEach(node => {
          if (node === tree.nodes[0]) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            Tree.visitNodes(childNode => {
              if (childNode.id === '0_3') {
                expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
                expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
              } else {
                expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
                expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
              }
            }, node.childNodes);
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            Tree.visitNodes(childNode => {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            }, node.childNodes);
          }
        });

      });

      it('insert child node which should be filtered', () => {
        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
        newNode0Child3.expanded = true;
        let filter = {
          accept: node => newNode0Child3.id !== node.id
        };
        tree.addFilter(filter);
        tree.visitNodes(childNode => {
          expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
          expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
        });
        tree.insertNodes([newNode0Child3], tree.nodes[0]);

        let treeNode0C3 = tree.nodeById(newNode0Child3.id);
        tree.visitNodes(node => {
          if (node === treeNode0C3) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          }
        });
      });
    });

    describe('with initial all closed nodes', () => {
      let model, tree;
      beforeEach(() => {
        model = helper.createModelFixture(3, 2, false);
        tree = helper.createTree(model);
      });

      it('init with all collapsed', () => {
        tree.visitNodes(node => {
          if (tree.nodes.indexOf(node) > -1) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
          }
        });
      });

      it('insert child node collapsed parent', () => {
        let newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
        newNode0Child3.expanded = true;
        tree.insertNodes([newNode0Child3], tree.nodes[0]);
        let newNode0Child3Child0 = helper.createModelNode('0_3_1', 'newNode0Child3Child0');
        tree.insertNodes([newNode0Child3Child0], tree.nodeById(newNode0Child3.id));
        expect(tree.visibleNodesFlat.indexOf(newNode0Child3) > -1).toBeFalsy();
        expect(tree.visibleNodesMap[newNode0Child3.id]).toBeFalsy();
        expect(tree.visibleNodesFlat.indexOf(newNode0Child3Child0) > -1).toBeFalsy();
        expect(tree.visibleNodesMap[newNode0Child3Child0.id]).toBeFalsy();
      });

      it('expand node', () => {
        let node0 = tree.nodes[0];
        tree.expandNode(node0);
        tree.visitNodes(node => {
          if (node.parentNode === node0) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          } else if (tree.nodes.indexOf(node) > -1) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
          }
        });
        // check order
        for (let i = 0; i < tree.visibleNodesFlat.length; i++) {
          let nodeInList = tree.visibleNodesFlat[i];
          if (i === 0) {
            expect(nodeInList.id === '0').toBeTruthy();
          } else if (i < 4) {
            expect(nodeInList.id === '0_' + (i - 1)).toBeTruthy();
          } else {
            expect(nodeInList.id === String(i - 3)).toBeTruthy();
          }
        }

      });

      it('expand child node', () => {
        let node0 = tree.nodes[0],
          node0_0 = node0.childNodes[0];
        tree.expandNode(node0);
        tree.expandNode(node0_0);
        tree.visitNodes(node => {
          if (node.parentNode === node0) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          } else if (node.parentNode && node.parentNode === node0_0 && node.parentNode.parentNode === node0) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          } else if (tree.nodes.indexOf(node) > -1) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
          }
        });
        for (let i = 0; i < tree.visibleNodesFlat.length; i++) {
          let nodeInList = tree.visibleNodesFlat[i];
          if (i === 0) {
            expect(nodeInList.id === '0').toBeTruthy();
          } else if (i < 7) {
            if (i === 1) {
              expect(nodeInList.id === '0_0').toBeTruthy();
            } else if (i < 5) {
              expect(nodeInList.id === '0_0_' + (i - 2)).toBeTruthy();
            } else {
              expect(nodeInList.id === '0_' + (i - 4)).toBeTruthy();
            }
          } else {
            expect(nodeInList.id === String(i - 6)).toBeTruthy();
          }
        }
      });
    });
  });

  describe('destroy', () => {

    it('should destroy all tree nodes and set destroyed flag', () => {
      let model = helper.createModelFixture(2, 1, false);
      let tree = helper.createTree(model);
      let nodesMapCopy = $.extend({}, tree.nodesMap);
      objects.values(nodesMapCopy).forEach(node => {
        expect(node.destroyed).toBe(false);
      });
      tree.destroy();
      objects.values(nodesMapCopy).forEach(node => {
        expect(node.destroyed).toBe(true);
      });
      expect(objects.countOwnProperties(tree.nodesMap)).toBe(0);
      expect(tree.nodes.length).toBe(0);
    });

    it('with animateRemoval should prevent upcoming dom modifications', () => {
      $('<style>' +
        '.tree-node {height: 30px; }' +
        '.tree {height: 50px; overflow: hidden;}' +
        '.tree-data.scrollable-tree {height: 50px; overflow: hidden;}' +
        '</style>').appendTo($('#sandbox'));
      let model = helper.createModelFixture(10, 1, false);
      let tree = helper.createTree(model);
      tree.render();
      tree.scrollToBottom();
      tree._onScroll();

      // Don't remove tree immediately after destroying
      tree.animateRemoval = true;
      tree.destroy();
      expect(tree.$nodes().length).toBe(10);
      expect(tree.viewRangeRendered.size()).toBe(10);

      // Delete and insert must not modify dom when tree is being removed
      tree.deleteAllNodes();
      tree.insertNodes(helper.createModelNodes(15));
      expect(tree.$nodes().length).toBe(10);
      expect(tree.viewRangeRendered.size()).toBe(10);

      tree.$data[0].scrollTop = 0; // Ensure onScroll will try to render the viewport because scrollTop has changed
      tree._onScroll();
      expect(tree.$nodes().length).toBe(10);
      expect(tree.viewRangeRendered.size()).toBe(10);
      // Must not throw exception
    });
  });

  describe('ensureExpansionVisible', () => {
    let model, tree, nodes, $scrollable, nodeHeight;

    beforeEach(() => {
      $('<style>' +
        '.tree-node {height: 28px; }' +
        '.tree {height: 230px; overflow: hidden;}' +
        '.tree-data.scrollable-tree {height: 230px; overflow: hidden;}' +
        '</style>').appendTo($('#sandbox'));

      model = helper.createModelFixture(10, 2, false);
      tree = helper.createTree(model);
      nodes = tree.nodes;

      tree.render();
      expect(tree.nodeHeight).toBe(28);
      $scrollable = tree.get$Scrollable();
      nodeHeight = tree.nodeHeight;
    });

    it('scrolls current node to the top when expanding a large child set', () => {
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[1].$node), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[9].$node), $scrollable)).toBe(false);
      expect(nodes[7].expanded).toBe(false);
      tree.selectNode(nodes[7]);
      tree.setNodeExpanded(nodes[7], true);
      expect(nodes[7].expanded).toBe(true);
      // node6 should be visible (one above the expanded node)
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[6].$node), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[7].$node), $scrollable)).toBe(true);
      // node8 isn't visible anymore since node7's children use up all the space
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[8].$node), $scrollable)).toBe(false);
    });

    it('scrolls current node up so that the full expansion is visible plus half a node at the bottom', () => {
      nodes[7].childNodes = [nodes[7].childNodes[0], nodes[7].childNodes[1]];
      tree.selectNode(nodes[7]);
      tree.setNodeExpanded(nodes[7], true);
      expect(nodes[7].expanded).toBe(true);
      // first visible node should be node3 (one above the expanded node)
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[6].$node), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[7].$node), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[7].childNodes[0].$node), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[7].childNodes[1].$node), $scrollable)).toBe(true);
      // half of node8 should still be visible after the expansion
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[8].$node), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(nodes[9].$node), $scrollable)).toBe(false);
    });
  });

  describe('invisible', () => {

    it('does not try to read node width when invisible', () => {
      let treeField = scout.create(TreeField, {
        parent: session.desktop,
        visible: false
      });
      let model = helper.createModelFixture(3, 0, true);
      let tree = helper.createTree(model);
      treeField.render();

      treeField.setTree(tree);
      expect(tree.rendered).toBe(true);
      expect(tree.nodes[0].width).toBeUndefined();

      treeField.validateLayout();
      expect(treeField.htmlComp.valid).toBe(false);
      expect(tree.htmlComp.valid).toBe(false);
      expect(tree.nodes[0].width).toBeUndefined();

      treeField.setVisible(true);
      expect(tree.nodes[0].width).toBeUndefined();

      treeField.validateLayout();
      expect(treeField.htmlComp.valid).toBe(true);
      expect(tree.htmlComp.valid).toBe(true);
      expect(tree.nodes[0].width).toBeGreaterThan(0);
    });
  });

  describe('scrollTo', () => {
    it('does not scroll if node is invisible due to filter', () => {
      let model = helper.createModelFixture(3, 2);
      let tree = helper.createTree(model);
      let node = tree.nodes[1];
      tree.render();
      tree.addFilter({
        accept: n => node !== n
      });
      tree.scrollTo(node);
      // Expect no error and no scrolling
      expect(tree.$data[0].scrollTop).toBe(0);
    });
  });

  describe('aria properties', () => {
    let model;
    let tree: SpecTree;
    let node0;
    let child0;

    beforeEach(() => {
      model = helper.createModelFixture(3, 3, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      child0 = node0.childNodes[0];
    });

    it('has aria role tree', () => {
      tree.render();
      expect(tree.$container).toHaveAttr('role', 'tree');
    });

    it('has aria-multiselectable set to true if multiCheck enabled', () => {
      tree.multiCheck = false;
      tree.render();
      expect(tree.$container.attr('aria-multiselectable')).toBeFalsy();

      tree = helper.createTree(model);
      tree.multiCheck = true;
      tree.render();
      expect(tree.$container).toHaveAttr('aria-multiselectable', 'true');
    });

    it('has nodes with aria role treeitem', () => {
      tree.render();
      let $allNodes = helper.findAllNodes(tree);
      expect($allNodes.length).toBeGreaterThan(0);
      $allNodes.each((index, $node) => {
        expect($node).toHaveAttr('role', 'treeitem');
      });
    });

    it('has nodes with a correct aria-level set', () => {
      tree.render();
      let $allNodes = helper.findAllNodes(tree);
      expect($allNodes.length).toBeGreaterThan(0);
      $allNodes.each((index, htmlNode) => {
        expect($(htmlNode)).toHaveAttr('aria-level', String(parseInt($(htmlNode).attr('data-level')) + 1));
      });
    });
  });
});
