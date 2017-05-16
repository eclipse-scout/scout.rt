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
describe('TreeNodePosition', function() {

  /** @type {scout.TreeSpecHelper} */
  var helper;

  var session, tree, node0, node1, node2, rootNode;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TreeSpecHelper(session);

    var rootNodeModel = helper.createModelNode('0', 'root');
    rootNodeModel.expanded = true;
    var model = helper.createModel([rootNodeModel]);
    tree = helper.createTree(model);
    rootNode = tree.nodes[0];
  });

  afterEach(function() {
    session = null;
  });

  /**
   * All these tests do test the _findInsertPositionInFlatList. Since the function uses some internal tree properties
   * we test the function indirectly. It's called every time the insert function is called.
   */
  describe('_findInsertPositionInFlatList', function() {

    // 0: root
    // 1: -  node_0
    // 2: - [node_1] <-- insert
    // 3: -  node_2
    it('insert node between two other nodes on the same level', function() {
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
    it('insert node between two other nodes on another level', function() {
      node0 = helper.createModelNode('0_0', 'node0');
      var root1 = helper.createModelNode('1', 'root1', 1);

      tree.render();
      tree.insertNodes([root1], null);
      tree.insertNodes([node0], rootNode);

      expect(tree.visibleNodesFlat[1].text).toBe('node0');
    });

    // 0: root
    // 1: - [node_0] <-- insert
    // 2: -  node_1
    // 3: -  node_2
    it('insert node ahead all other nodes on the same level', function() {
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
    it('insert node below all other nodes on the same level', function() {
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
    it('insert a subtree between two other nodes on the same level', function() {
      node0 = helper.createModelNode('0_0', 'node0');

      node1 = helper.createModelNode('0_1', 'node1', 1);
      var node10 = helper.createModelNode('0_1_0', 'node1_0');
      var node11 = helper.createModelNode('0_1_1', 'node1_1');
      node1.expanded = true;
      node1.childNodes = [node10, node11];

      node2 = helper.createModelNode('0_2', 'node2'),

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
    it('insert a node below another node of the same level with an expanded subtree', function() {
      var node00 = helper.createModelNode('0_0_0', 'node0_0');
      var node01 = helper.createModelNode('0_0_1', 'node0_1');
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

});
