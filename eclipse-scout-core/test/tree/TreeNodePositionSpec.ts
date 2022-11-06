/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Dimension, TreeNode, TreeNodeModel} from '../../src/index';
import {TreeSpecHelper} from '../../src/testing/index';
import SpecTree from '../../src/testing/tree/SpecTree';

describe('TreeNodePosition', () => {

  let helper: TreeSpecHelper;
  let session: SandboxSession, tree: SpecTree, node0: TreeNodeModel, node1: TreeNodeModel, node2: TreeNodeModel, rootNode: TreeNode;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TreeSpecHelper(session);

    let rootNodeModel = helper.createModelNode('0', 'root');
    rootNodeModel.expanded = true;
    let model = helper.createModel([rootNodeModel]);
    tree = helper.createTree(model);
    rootNode = tree.nodes[0];
  });

  afterEach(() => {
    session = null;
  });

  /**
   * All these tests do test the _findInsertPositionInFlatList. Since the function uses some internal tree properties
   * we test the function indirectly. It's called every time the insert function is called.
   */
  describe('_findInsertPositionInFlatList', () => {

    // 0: root
    // 1: -  node_0
    // 2: - [node_1] <-- insert
    // 3: -  node_2
    it('insert node between two other nodes on the same level', () => {
      node0 = helper.createModelNode('0_0', 'node0');
      node1 = helper.createModelNode('0_1', 'node1', 1);
      node2 = helper.createModelNode('0_2', 'node2');

      tree.render();
      tree.insertNodes([node0, node2], rootNode);
      tree.insertNodes([node1], rootNode);

      expect(tree.visibleNodesFlat[2].text).toBe('node1');
    });

    // 0: root
    // 1: - [node_0] <-- insert
    // 2: root1
    it('insert node between two other nodes on another level', () => {
      node0 = helper.createModelNode('0_0', 'node0');
      let root1 = helper.createModelNode('1', 'root1', 1);

      tree.render();
      tree.insertNodes([root1], null);
      tree.insertNodes([node0], rootNode);

      expect(tree.visibleNodesFlat[1].text).toBe('node0');
    });

    // 0: root
    // 1: - [node_0] <-- insert
    // 2: -  node_1
    // 3: -  node_2
    it('insert node ahead all other nodes on the same level', () => {
      node0 = helper.createModelNode('0_0', 'node0', 0);
      node1 = helper.createModelNode('0_1', 'node1');
      node2 = helper.createModelNode('0_2', 'node2');

      tree.render();
      tree.insertNodes([node1, node2], rootNode);
      tree.insertNodes([node0], rootNode);

      expect(tree.visibleNodesFlat[1].text).toBe('node0');
    });

    // 0: root
    // 1: -  node_0
    // 2: -  node_1
    // 3: - [node_2] <-- insert
    it('insert node below all other nodes on the same level', () => {
      node0 = helper.createModelNode('0_0', 'node0', 0);
      node1 = helper.createModelNode('0_1', 'node1', 1);
      node2 = helper.createModelNode('0_2', 'node2', 2);

      tree.render();
      tree.insertNodes([node0, node1], rootNode);
      tree.insertNodes([node2], rootNode);

      expect(tree.visibleNodesFlat[3].text).toBe('node2');
    });

    // 0: root
    // 1: -  node_0
    // 2: - [node_1]     <-- insert
    // 3:   - [node 1_0] <-- insert
    // 4:   - [node 1_1] <-- insert
    // 5: -  node_2
    it('insert a subtree between two other nodes on the same level', () => {
      node0 = helper.createModelNode('0_0', 'node0');

      node1 = helper.createModelNode('0_1', 'node1', 1);
      let node10 = helper.createModelNode('0_1_0', 'node1_0');
      let node11 = helper.createModelNode('0_1_1', 'node1_1');
      node1.expanded = true;
      node1.childNodes = [node10, node11];

      node2 = helper.createModelNode('0_2', 'node2');
      tree.render();
      tree.insertNodes([node0, node2], rootNode);
      tree.insertNodes([node1], rootNode);

      expect(tree.visibleNodesFlat[2].text).toBe('node1');
      expect(tree.visibleNodesFlat[3].text).toBe('node1_0');
      expect(tree.visibleNodesFlat[4].text).toBe('node1_1');
    });

    // 0: root
    // 1: -  node_0
    // 2:    - node_0_0
    // 3:    - node_0_1
    // 4:[node_1]     <-- insert
    it('insert a node below another node of the same level with an expanded subtree', () => {
      let node00 = helper.createModelNode('0_0_0', 'node0_0');
      let node01 = helper.createModelNode('0_0_1', 'node0_1');
      node0 = helper.createModelNode('0_0', 'node0');
      node0.expanded = true;
      node0.childNodes = [node00, node01];

      node1 = helper.createModelNode('1', 'root1', 1);

      tree.render();
      tree.insertNodes([node0], rootNode);
      tree.insertNodes([node1], null);

      expect(tree.visibleNodesFlat[4].text).toBe('root1');
    });

  });

  describe('_addChildrenToFlatListIfExpanded', () => {

    // Node 1
    // Node 2
    // +- Node 2.1
    // Node 3
    // +- Node 3.1
    // Node 4
    // +- Node 4.1
    // +- Node 4.2
    // +- Node 4.3
    // +- (...)
    // +- Node 4.24
    // Node 5
    // +- Node 5.1
    it('expands collapsed node', () => {
      // This tree does not use a root node
      tree = helper.createTree(helper.createModel([]));

      function createNode(id: string, text: string, expanded: boolean, childNodeIndex?: number): TreeNodeModel {
        let node = helper.createModelNode(id, text, childNodeIndex);
        node.expanded = expanded;
        return node;
      }

      let n1 = createNode('n1', 'Node 1', true, 0);
      let n2 = createNode('n2', 'Node 2', true, 1);
      let n2_1 = createNode('n2_1', 'Node 2.1', true);
      let n3 = createNode('n3', 'Node 3', true, 2);
      let n3_1 = createNode('n3_1', 'Node 3.1', true);
      let n4 = createNode('n4', 'Node 4', false, 3);
      let n4_1 = createNode('n4_1', 'Node 4.1', true, 0);
      let n4_2 = createNode('n4_2', 'Node 4.2', true, 1);
      let n4_3 = createNode('n4_3', 'Node 4.3', true, 2);
      let n4_4 = createNode('n4_4', 'Node 4.4', true, 3);
      let n4_5 = createNode('n4_5', 'Node 4.5', true, 4);
      let n4_6 = createNode('n4_6', 'Node 4.6', true, 5);
      let n4_7 = createNode('n4_7', 'Node 4.7', true, 6);
      let n4_8 = createNode('n4_8', 'Node 4.8', true, 7);
      let n4_9 = createNode('n4_9', 'Node 4.9', true, 8);
      let n4_10 = createNode('n4_10', 'Node 4.10', true, 9);
      let n4_11 = createNode('n4_11', 'Node 4.11', true, 10);
      let n4_12 = createNode('n4_12', 'Node 4.12', true, 11);
      let n4_13 = createNode('n4_13', 'Node 4.13', true, 12);
      let n4_14 = createNode('n4_14', 'Node 4.14', true, 13);
      let n4_15 = createNode('n4_15', 'Node 4.15', true, 14);
      let n4_16 = createNode('n4_16', 'Node 4.16', true, 15);
      let n4_17 = createNode('n4_17', 'Node 4.17', true, 16);
      let n4_18 = createNode('n4_18', 'Node 4.18', true, 17);
      let n4_19 = createNode('n4_19', 'Node 4.19', true, 18);
      let n4_20 = createNode('n4_20', 'Node 4.20', true, 19);
      let n4_21 = createNode('n4_21', 'Node 4.21', true, 20);
      let n4_22 = createNode('n4_22', 'Node 4.22', true, 21);
      let n4_23 = createNode('n4_23', 'Node 4.23', true, 22);
      let n4_24 = createNode('n4_24', 'Node 4.24', true, 23);
      let n5 = createNode('n5', 'Node 5', true, 4);
      let n5_1 = createNode('n5_1', 'Node 5.1', true);

      tree.insertNodes([n1, n2, n3, n4, n5], null);
      tree.insertNodes([n2_1], tree.nodes[1]);
      tree.insertNodes([n3_1], tree.nodes[2]);
      tree.insertNodes([n4_1, n4_2, n4_3, n4_4, n4_5, n4_6, n4_7, n4_8, n4_9, n4_10, n4_11, n4_12, n4_13, n4_14, n4_15, n4_16, n4_17, n4_18, n4_19, n4_20, n4_21, n4_22, n4_23, n4_24], tree.nodes[3]);
      tree.insertNodes([n5_1], tree.nodes[4]);

      tree.render();
      tree.htmlComp.setSize(new Dimension(190, 190));
      tree.viewRangeSize = 14;

      function expectVisibleNodesFlatToBe(nodes) {
        let expected = tree.visibleNodesFlat.map(node => {
          return node.id;
        }).join(', ');
        let actual = nodes.map(node => {
          return node.id;
        }).join(', ');
        expect(expected).toEqual(actual);
      }

      // Check visibleNodesFlat

      expectVisibleNodesFlatToBe([
        n1,
        n2,
        n2_1,
        n3,
        n3_1,
        n4,
        n5,
        n5_1
      ]);

      // Expand "node 4" and check visibleNodesFlat again

      tree.setNodeExpanded(tree.nodes[3], true, {
        renderAnimated: false
      });

      expectVisibleNodesFlatToBe([
        n1,
        n2,
        n2_1,
        n3,
        n3_1,
        n4,
        n4_1,
        n4_2,
        n4_3,
        n4_4,
        n4_5,
        n4_6,
        n4_7,
        n4_8,
        n4_9,
        n4_10,
        n4_11,
        n4_12,
        n4_13,
        n4_14,
        n4_15,
        n4_16,
        n4_17,
        n4_18,
        n4_19,
        n4_20,
        n4_21,
        n4_22,
        n4_23,
        n4_24,
        n5,
        n5_1
      ]);
    });

    // Node 0
    // +- Node 1
    //    +- Node 1.1
    //    +- Node 1.2
    //    +- Node 1.3
    //    +- (...)
    //    +- Node 1.13
    // +- Node 2
    // +- Node 3
    //    +- Node 3.1
    it('expands collapsed node with different levels in insertBatch', () => {
      // This tree does not use a root node
      tree = helper.createTree(helper.createModel([]));

      function createNode(id: string, text: string, expanded: boolean, childNodeIndex?: number): TreeNodeModel {
        let node = helper.createModelNode(id, text, childNodeIndex);
        node.expanded = expanded;
        return node;
      }

      let n0 = createNode('n0', 'Node 0', true, 0);
      let n1 = createNode('n1', 'Node 1', true, 0);
      let n1_1 = createNode('n1_1', 'Node 1.1', true, 0);
      let n1_2 = createNode('n1_2', 'Node 1.2', true, 1);
      let n1_3 = createNode('n1_3', 'Node 1.3', true, 2);
      let n1_4 = createNode('n1_4', 'Node 1.4', true, 3);
      let n1_5 = createNode('n1_5', 'Node 1.5', true, 4);
      let n1_6 = createNode('n1_6', 'Node 1.6', true, 5);
      let n1_7 = createNode('n1_7', 'Node 1.7', true, 6);
      let n1_8 = createNode('n1_8', 'Node 1.8', true, 7);
      let n1_9 = createNode('n1_9', 'Node 1.9', true, 8);
      let n1_10 = createNode('n1_10', 'Node 1.10', true, 9);
      let n1_11 = createNode('n1_11', 'Node 1.11', true, 10);
      let n1_12 = createNode('n1_12', 'Node 1.12', true, 11);
      let n1_13 = createNode('n1_13', 'Node 1.13', true, 12);
      let n2 = createNode('n2', 'Node 2', true, 1);
      let n3 = createNode('n3', 'Node 3', true, 2);
      let n3_1 = createNode('n3_1', 'Node 3.1', true);

      tree.insertNodes([n0], null);
      tree.insertNodes([n1, n2, n3], tree.nodes[0]);
      tree.insertNodes([n1_1, n1_2, n1_3, n1_4, n1_5, n1_6, n1_7, n1_8, n1_9, n1_10, n1_11, n1_12, n1_13], tree.nodes[0].childNodes[0]);
      tree.insertNodes([n3_1], tree.nodes[0].childNodes[2]);

      tree.render();
      tree.htmlComp.setSize(new Dimension(190, 190));
      tree.viewRangeSize = 14;

      function expectVisibleNodesFlatToBe(nodes) {
        let expected = tree.visibleNodesFlat.map(node => {
          return node.id;
        }).join(', ');
        let actual = nodes.map(node => {
          return node.id;
        }).join(', ');
        expect(expected).toEqual(actual);
      }

      // Check visibleNodesFlat

      expectVisibleNodesFlatToBe([
        n0,
        n1,
        n1_1,
        n1_2,
        n1_3,
        n1_4,
        n1_5,
        n1_6,
        n1_7,
        n1_8,
        n1_9,
        n1_10,
        n1_11,
        n1_12,
        n1_13,
        n2,
        n3,
        n3_1
      ]);

      // Collapse "node 0" and check visibleNodesFlat again

      tree.setNodeExpanded(tree.nodes[0], false, {
        renderAnimated: false
      });

      expectVisibleNodesFlatToBe([
        n0
      ]);

      // Expand "node 0" and check visibleNodesFlat again

      tree.setNodeExpanded(tree.nodes[0], true, {
        renderAnimated: false
      });

      expectVisibleNodesFlatToBe([
        n0,
        n1,
        n1_1,
        n1_2,
        n1_3,
        n1_4,
        n1_5,
        n1_6,
        n1_7,
        n1_8,
        n1_9,
        n1_10,
        n1_11,
        n1_12,
        n1_13,
        n2,
        n3,
        n3_1
      ]);
    });

  });

});
