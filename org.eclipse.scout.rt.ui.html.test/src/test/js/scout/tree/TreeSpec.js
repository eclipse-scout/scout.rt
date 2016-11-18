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
describe("Tree", function() {
  var session;
  /** @type {scout.TreeSpecHelper} */
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TreeSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
    $.fx.off = true;
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  describe("creation", function() {
    it("adds nodes", function() {

      var model = helper.createModelFixture(1);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      expect(helper.findAllNodes(tree).length).toBe(1);
    });

    it("does not add notes if no nodes are provided", function() {

      var model = helper.createModelFixture();
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      expect(helper.findAllNodes(tree).length).toBe(0);
    });
  });

  describe("insertNodes", function() {
    var model, tree, node0, node1, node2;

    beforeEach(function() {
      model = helper.createModelFixture(3, 1, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
    });

    describe("inserting a child", function() {

      it("inserts in a reasonable order if childNodeIndex is not set", function() {
        // we want to start with an empty tree for this test
        var rootNodeModel = helper.createModelNode('0', 'root');
        rootNodeModel.expanded = true;
        model = helper.createModel([rootNodeModel]);
        tree = helper.createTree(model);
        tree.render(session.$entryPoint);

        // child nodes
        var nodeModels = [
          helper.createModelNode('0_0', 'node0'),
          helper.createModelNode('0_1', 'node1'),
          helper.createModelNode('0_2', 'node2')
        ];
        // make sure nodes _DON'T_ have a childNodeIndex (since that's usually the case when a programmer calls insertNodes in JS only)
        nodeModels.forEach(function(node) {
          delete node.childNodeIndex;
        });
        var rootNode = tree.nodes[0];
        tree.insertNodes(nodeModels, rootNode);

        // assert order in DOM is 0_0, 0_1, 0_2 (= same order as in array)
        var orderedNodeIdString = '';
        tree.$container.find("[data-level='1']").each(function() {
          orderedNodeIdString += $(this).attr('data-nodeid') + ',';
        });
        expect(orderedNodeIdString).toBe('0_0,0_1,0_2,');
      });

      it("updates model", function() {
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        expect(tree.nodes.length).toBe(3);
        expect(Object.keys(tree.nodesMap).length).toBe(12);

        tree.insertNodes([newNode0Child3], node0);
        expect(node0.childNodes.length).toBe(4);
        expect(node0.childNodes[3].text).toBe(newNode0Child3.text);
        expect(Object.keys(tree.nodesMap).length).toBe(13);
      });

      it("updates model with a complex node containing another node", function() {
        var node1_1_0 = helper.createModelNode('1_1_0', 'node1_1_0', 0);
        tree.insertNodes([node1_1_0], node1.childNodes[1]);

        var node2_1_0 = helper.createModelNode('2_1_0', 'node2_1_0', 1);
        var node2_1_0_0 = helper.createModelNode('2_1_0_0', 'node2_1_0_0', 0);
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

      it("updates html document if parent is expanded", function() {
        tree.render(session.$entryPoint);
        tree.revalidateLayoutTree();
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        expect(helper.findAllNodes(tree).length).toBe(12);

        tree.insertNodes([newNode0Child3], node0);
        expect(helper.findAllNodes(tree).length).toBe(13);
        expect(node0.childNodes[3].$node.text()).toBe(newNode0Child3.text);
      });

      it("updates html document at a specific position", function() {
        tree.render(session.$entryPoint);

        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 2);
        var newNode0Child4 = helper.createModelNode('0_4', 'newNode0Child4', 3);
        expect(helper.findAllNodes(tree).length).toBe(12);

        tree.insertNodes([newNode0Child3, newNode0Child4], node0);
        expect(helper.findAllNodes(tree).length).toBe(14);
        expect(node0.childNodes[2].$node.text()).toBe(newNode0Child3.text);
        expect(node0.childNodes[3].$node.text()).toBe(newNode0Child4.text);
        expect(node0.childNodes[3].$node.attr('data-level')).toBe('1');
        expect(node0.childNodes[3].$node.next().attr('data-level')).toBe('1');
        expect(node0.childNodes[3].$node.next().text()).toBe('node 0_2');

        var newNode1Child3 = helper.createModelNode('1_3', 'newNode1Child3', 1);
        var newNode1Child4 = helper.createModelNode('1_4', 'newNode1Child4', 2);

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
    });

    it("only updates the model if parent is collapsed", function() {
      tree.setNodeExpanded(node0, false);
      tree.render(session.$entryPoint);

      var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
      expect(helper.findAllNodes(tree).length).toBe(9);

      tree.insertNodes([newNode0Child3], node0);
      //Check that the model was updated correctly
      expect(node0.childNodes.length).toBe(4);
      expect(node0.childNodes[3].text).toBe(newNode0Child3.text);
      expect(Object.keys(tree.nodesMap).length).toBe(13);

      //Check that no dom manipulation happened
      expect(helper.findAllNodes(tree).length).toBe(9);
      expect(node0.childNodes[3].$node).toBeUndefined();
    });

    it("expands the parent if parent.expanded = true and the new inserted nodes are the first child nodes", function() {
      model = helper.createModelFixture(3, 0, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
      tree.render(session.$entryPoint);

      var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
      var $node0 = node0.$node;
      // Even tough the nodes were created with expanded=true, the $node should not have
      // been rendered as expanded (because it has no children)
      expect($node0).not.toHaveClass('expanded');
      expect(helper.findAllNodes(tree).length).toBe(3);

      tree.insertNodes([newNode0Child3], node0);
      expect(helper.findAllNodes(tree).length).toBe(4);
      expect(node0.childNodes[0].$node.text()).toBe(newNode0Child3.text);
      expect($node0).toHaveClass('expanded');
    });

    describe("with breadcrumb style", function() {

      beforeEach(function() {
        tree.setDisplayStyle(scout.Tree.DisplayStyle.BREADCRUMB);
      });

      it("inserts a html node if the parent node is selected", function() {
        tree.render(session.$entryPoint);
        tree.revalidateLayoutTree();
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        expect(helper.findAllNodes(tree).length).toBe(3); // top level nodes are visible

        tree.selectNode(node0);
        expect(helper.findAllNodes(tree).length).toBe(4); // only node0 and its child nodes are visible

        tree.insertNodes([newNode0Child3], node0);
        expect(helper.findAllNodes(tree).length).toBe(5);
        expect(node0.childNodes[3].$node.text()).toBe(newNode0Child3.text);
      });

      it("only updates model if the parent node is not selected", function() {
        tree.render(session.$entryPoint);
        tree.revalidateLayoutTree();
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        expect(helper.findAllNodes(tree).length).toBe(3); // top level nodes are visible

        tree.insertNodes([newNode0Child3], node0);
        expect(helper.findAllNodes(tree).length).toBe(3); // still 3 because no node is selected
        expect(node0.childNodes[3].id).toBe(newNode0Child3.id);
        expect(node0.childNodes[3].rendered).toBe(false);
        expect(node0.childNodes[3].attached).toBe(false);
      });

      it("inserts html nodes at a specific position", function() {
        tree.render(session.$entryPoint);

        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 2);
        var newNode0Child4 = helper.createModelNode('0_4', 'newNode0Child4', 3);
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

    it("expands the parent if parent.expanded = true and the new inserted nodes are the first child nodes", function() {
      model = helper.createModelFixture(3, 0, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
      tree.render(session.$entryPoint);

      var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3');
      var $node0 = node0.$node;
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

  describe("updateNodes", function() {
    var model;
    var tree;
    var node0;
    var child0;

    beforeEach(function() {
      model = helper.createModelFixture(3, 3, false);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      child0 = node0.childNodes[0];
    });

    // this test does not create a new node instance/model for an update, but re-uses
    // an already existing node and simply changes a property of that instance
    it("update same node instance", function() {
      expect(node0.leaf).toBe(false);
      tree.render(session.$entryPoint);
      node0.leaf = true;
      tree.updateNode(node0);
      // we expect that _decorateNode has been called and updates the DOM of the tree
      var $treeNode = tree.$container.find('[data-nodeid="' + node0.id + '"]').first();
      expect($treeNode.attr('class')).toContain('leaf');
    });

    describe("enabled update", function() {
      var child0Update;

      beforeEach(function() {
        child0Update = {
          id: child0.id,
          enabled: false
        };
        tree.checkable = true;
      });

      it("updates the enabled state of the model node", function() {
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);
        expect(child0.enabled).toBe(false);
      });

      it("updates the enabled state of the html node, if visible", function() {
        // Render tree and make sure child0 is visible
        tree.render(session.$entryPoint);
        tree.setNodeExpanded(node0, true);
        expect(child0.$node.isEnabled()).toBe(true);
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);

        // Expect node and $node to be disabled
        expect(child0.enabled).toBe(false);
        expect(child0.$node.isEnabled()).toBe(false);
      });

      it("updates the enabled state of the html node after expansion, if not visible", function() {
        // Render tree and make sure child0 is visible
        tree.render(session.$entryPoint);
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

    describe("enabled update on checkable tree", function() {
      var child0Update;

      function $checkbox(node) {
        return node.$node.children('.tree-node-checkbox')
          .children('.check-box');
      }

      beforeEach(function() {
        child0Update = {
          id: child0.id,
          enabled: false
        };
        tree.checkable = true;
      });

      it("updates the enabled state of the model node", function() {
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);
        expect(child0.enabled).toBe(false);
      });

      it("updates the enabled state of the html node, if visible", function() {
        // Render tree and make sure child0 is visible
        tree.render(session.$entryPoint);
        tree.setNodeExpanded(node0, true);
        expect($checkbox(child0).isEnabled()).toBe(true);

        tree.updateNodes([child0Update]);

        // Expect node and $node to be disabled
        expect(child0.enabled).toBe(false);
        expect($checkbox(child0).isEnabled()).toBe(false);
      });

      it("updates the enabled state of the html node after expansion, if not visible", function() {
        // Render tree and make sure child0 is visible
        tree.render(session.$entryPoint);
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

  describe("changeNode", function() {
    var model, tree, nodes, node0, node1, child0, child1_1;

    beforeEach(function() {
      model = helper.createModelFixture(3, 3, false);
      tree = helper.createTree(model);
      nodes = tree.nodes;
      node0 = nodes[0];
      node1 = nodes[1];
      child0 = node0.childNodes[0];
      child1_1 = node1.childNodes[1];
    });

    it("updates the text of the model node", function() {
      node0.text = 'new Text';
      tree.changeNode(node0);
      expect(tree.nodes[0].text).toBe('new Text');
    });

    it("updates the text of the html node", function() {
      tree.render(session.$entryPoint);

      node0.text = 'new Text';
      tree.changeNode(node0);
      var $node0 = node0.$node;
      expect($node0.text()).toBe('new Text');

      // Check whether tree-control is still there
      expect($node0.children('.tree-node-control').length).toBe(1);
    });

    it("updates custom cssClass of model and html node", function() {
      tree.selectedNodes = [node0];
      tree.render(session.$entryPoint);

      node0.cssClass = 'new-css-class';
      tree.changeNode(node0);

      // Check model
      expect(node0.cssClass).toBe('new-css-class');

      // Check gui
      var $node0 = node0.$node;
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

    it("preserves child-of-selected when root nodes get changed", function() {
      tree.selectedNodes = [];
      tree.render(session.$entryPoint);

      var $children = tree.$data.find('.tree-node.child-of-selected');
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

    it("preserves child-of-selected when child nodes get changed", function() {
      tree.selectedNodes = [node1];
      tree.setNodeExpanded(node1, true);
      tree.render(session.$entryPoint);

      var $children = tree.$data.find('.tree-node.child-of-selected');
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

    it("preserves group css class when nodes get updated", function() {
      tree.selectNode(node1);
      tree.render(session.$entryPoint);

      tree._isGroupingEnd = function(node) {
        return node.nodeType === scout.Page.NodeType.TABLE;
      };

      var $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);

      node1.text = 'new text';
      tree.changeNode(node1);
      expect(node1.text).toBe('new text');

      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);
    });
  });

  describe("deleteNodes", function() {
    var model;
    var tree;
    var node0;
    var node1;
    var node2;

    beforeEach(function() {
      // A large tree is used to properly test recursion
      model = helper.createModelFixture(3, 2, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
    });

    describe("deleting a child", function() {

      it("updates model", function() {
        var node2Child0 = node2.childNodes[0];
        var node2Child1 = node2.childNodes[1];
        expect(tree.nodes.length).toBe(3);
        expect(tree.nodes[0]).toBe(node0);
        expect(Object.keys(tree.nodesMap).length).toBe(39);

        tree.deleteNodes([node2Child0], node2);
        expect(tree.nodes[2].childNodes.length).toBe(2);
        expect(tree.nodes[2].childNodes[0]).toBe(node2Child1);
        expect(Object.keys(tree.nodesMap).length).toBe(35);
      });

      it("updates html document", function() {
        tree.setViewRangeSize(40);
        tree.render(session.$entryPoint);

        var node2Child0 = node2.childNodes[0];

        expect(helper.findAllNodes(tree).length).toBe(39);
        expect(node2Child0.$node).toBeDefined();

        tree.deleteNodes([node2Child0], node2);
        expect(helper.findAllNodes(tree).length).toBe(35);
        expect(node2Child0.$node).toBeUndefined();

        expect(node0.$node).toBeDefined();
        expect(node0.childNodes[0].$node).toBeDefined();
        expect(node0.childNodes[1].$node).toBeDefined();
        expect(node0.childNodes[2].$node).toBeDefined();
      });

      it("updates child node indices", function() {
        var node2Child0 = node2.childNodes[0];
        var node2Child1 = node2.childNodes[1];
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

      it("considers view range (distinguishes between rendered and non rendered rows, adjusts viewRangeRendered)", function() {
        //initial view range
        model = helper.createModelFixture(6, 0, false);
        tree = helper.createTree(model);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        var lastNode = tree.nodes[5];

        var spy = spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new scout.Range(1, 4));
        tree.render(session.$entryPoint);
        expect(tree.viewRangeRendered).toEqual(new scout.Range(1, 4));
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
        expect(tree.viewRangeRendered).toEqual(new scout.Range(0, 3));
        expect(tree.$nodes().length).toBe(3);
        expect(tree.nodes.length).toBe(5);

        // delete first rendered
        tree.deleteNodes([node1]);
        expect(tree.viewRangeDirty).toBeTruthy();
        tree._renderViewport();
        expect(tree.viewRangeDirty).toBeFalsy();
        expect(tree.viewRangeRendered).toEqual(new scout.Range(0, 3));
        expect(tree.$nodes().length).toBe(3);
        expect(tree.nodes.length).toBe(4);

        // delete last node not rendered
        tree.deleteNodes([lastNode]);
        expect(tree.viewRangeDirty).toBeTruthy();
        tree._renderViewport();
        expect(tree.viewRangeDirty).toBeFalsy();
        expect(tree.viewRangeRendered).toEqual(new scout.Range(0, 3));
        expect(tree.$nodes().length).toBe(3);
        expect(tree.nodes.length).toBe(3);

        // delete remaining (rendered) nodes
        tree.deleteNodes([tree.nodes[0], tree.nodes[1], tree.nodes[2]]);
        expect(tree.viewRangeDirty).toBeTruthy();
        tree._renderViewport();
        expect(tree.viewRangeDirty).toBeFalsy();
        expect(tree.viewRangeRendered).toEqual(new scout.Range(0, 0));
        expect(tree.$nodes().length).toBe(0);
        expect(tree.nodes.length).toBe(0);
        expect(tree.$fillBefore.height()).toBe(0);
        expect(tree.$fillAfter.height()).toBe(0);
      });

    });

    describe("deleting a root node", function() {
      it("updates model", function() {
        tree.deleteNodes([node0]);
        expect(tree.nodes.length).toBe(2);
        expect(tree.nodes[0]).toBe(node1);
        expect(Object.keys(tree.nodesMap).length).toBe(26);
      });

      it("updates html document", function() {
        tree.setViewRangeSize(30);
        tree.render(session.$entryPoint);

        tree.deleteNodes([node0]);
        expect(tree.visibleNodesFlat.length).toBe(26);
        expect(node0.$node).toBeUndefined();
        expect(tree.nodes.indexOf(node0)).toBe(-1);
        expect(node0.childNodes[0].$node).toBeUndefined();
        expect(node0.childNodes[1].$node).toBeUndefined();
        expect(node0.childNodes[2].$node).toBeUndefined();
      });

      describe("deleting a collapsed root node", function() {
        it("updates model", function() {
          tree.setNodeExpanded(node0, false);
          node0.expanded = false;

          tree.deleteNodes([node0]);
          expect(tree.nodes.length).toBe(2);
          expect(tree.nodes[0]).toBe(node1);
          expect(Object.keys(tree.nodesMap).length).toBe(26);
        });

        it("updates html document", function() {
          tree.setViewRangeSize(30);
          tree.setNodeExpanded(node0, false);
          tree.render(session.$entryPoint);

          tree.deleteNodes([node0]);
          expect(helper.findAllNodes(tree).length).toBe(26);
          expect(node0.$node).toBeUndefined();
          expect(tree.nodes.indexOf(node0)).toBe(-1);
          expect(node0.childNodes[0].$node).toBeUndefined();
          expect(node0.childNodes[1].$node).toBeUndefined();
          expect(node0.childNodes[2].$node).toBeUndefined();
        });
      });
    });

    describe("deleting all nodes", function() {
      it("updates model", function() {
        tree.deleteNodes([node0, node1, node2]);
        expect(tree.nodes.length).toBe(0);
        expect(Object.keys(tree.nodesMap).length).toBe(0);
      });

      it("updates html document", function() {
        tree.render(session.$entryPoint);

        tree.deleteNodes([node0, node1, node2]);
        expect(helper.findAllNodes(tree).length).toBe(0);
      });
    });

  });

  describe("deleteAllChildNodes", function() {
    var model;
    var tree;
    var node0;
    var node1;
    var node2;
    var node1Child0;
    var node1Child1;
    var node1Child2;

    beforeEach(function() {
      model = helper.createModelFixture(3, 1, true);
      tree = helper.createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
      node1Child0 = node1.childNodes[0];
      node1Child1 = node1.childNodes[1];
      node1Child2 = node1.childNodes[1];
    });

    it("deletes all nodes from model", function() {
      expect(tree.nodes.length).toBe(3);
      expect(Object.keys(tree.nodesMap).length).toBe(12);

      tree.deleteAllChildNodes();
      expect(tree.nodes.length).toBe(0);
      expect(Object.keys(tree.nodesMap).length).toBe(0);
    });

    it("deletes all nodes from html document", function() {
      tree.render(session.$entryPoint);

      expect(helper.findAllNodes(tree).length).toBe(12);

      tree.deleteAllChildNodes();
      expect(helper.findAllNodes(tree).length).toBe(0);
    });

    it("deletes all nodes from model for a given parent", function() {
      expect(tree.nodes.length).toBe(3);
      expect(Object.keys(tree.nodesMap).length).toBe(12);

      tree.deleteAllChildNodes(node1);
      expect(node1.childNodes.length).toBe(0);
      expect(Object.keys(tree.nodesMap).length).toBe(9);
    });

    it("deletes all nodes from html document for a given parent", function() {
      tree.render(session.$entryPoint);

      expect(helper.findAllNodes(tree).length).toBe(12);

      tree.deleteAllChildNodes(node1);
      expect(helper.findAllNodes(tree).length).toBe(9);

      //Check that children are removed, parent must still exist
      expect(node1.$node).toBeDefined();
      expect(node1Child0.$node).toBeUndefined();
      expect(node1Child1.$node).toBeUndefined();
      expect(node1Child2.$node).toBeUndefined();
    });

  });

  describe("checkNodes", function() {

    it("checks a subnode -> mark upper nodes ", function() {
      var model = helper.createModelFixture(4, 4);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);
      tree.checkable = true;

      var node;
      //find node with more then one child level
      for (var i = 0; i < tree.nodes.length; i++) {
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

    it("checks a node -> mark upper nodes -> uncheck node and test if node keeps marked because children are checked", function() {
      var model = helper.createModelFixture(4, 4);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);
      tree.checkable = true;
      var node, nodeToCheck;
      //find node with more then one child level
      for (var i = 0; i < tree.nodes.length; i++) {
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
      var tmpNode = nodeToCheck;
      //upper nodes should be marked
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }
      expect(nodeToCheck.childNodes[0].checked).toEqual(true);

      //remove check state on second level node-> second level node should be marked because children of it are checked
      tree.checkNode(nodeToCheck, false);
      expect(nodeToCheck.checked).toEqual(false);
      expect(nodeToCheck.childrenChecked).toEqual(true);
      tmpNode = nodeToCheck;
      //upper nodes should be marked
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }
    });

    it("checks a subnode and its sibling -> mark upper nodes -> uncheck one of the siblings", function() {
      var model = helper.createModelFixture(4, 4);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);
      tree.checkable = true;
      var nodeOne, nodeTwo;
      //find node with more then one child level
      for (var i = 0; i < tree.nodes.length; i++) {
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
      //check if all upper nodes are marked
      var tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }

      //uncheck one of the two siblings
      tree.checkNode(nodeTwo, false);
      //marks on upper should exist
      tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }

      //uncheck second siblings
      tree.checkNode(nodeOne, false);
      //marks on upper should be removed
      tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(false);
      }
    });

    it("does not check a disabled node", function() {
      var model = helper.createModelFixture(4, 4);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);
      tree.checkable = true;

      var node = tree.nodes[0];
      node.enabled = false;
      tree.checkNode(node, true);
      expect(node.checked).toEqual(false);
    });

    it("does not check a node in a disabled tree", function() {
      var model = helper.createModelFixture(4, 4);
      var tree = helper.createTree(model);
      tree.enabled = false;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0];
      tree.checkNode(node, true);
      expect(node.checked).toEqual(false);
    });

    it("never checks two nodes if multiCheck is set to false", function() {
      var model = helper.createModelFixture(4, 4);
      var tree = helper.createTree(model);
      tree.multiCheck = false;
      tree.checkable = true;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0],
        nodeTwo = tree.nodes[1];

      if (node && nodeTwo) {
        tree.checkNode(node, true);
        tree.checkNode(nodeTwo, true);
      }

      var checkedNodes = [];
      for (var j = 0; j < tree.nodes.length; j++) {
        if (tree.nodes[j].checked) {
          checkedNodes.push(tree.nodes[j]);
        }
      }
      expect(checkedNodes.length).toBe(1);
    });

    it("checks children if autoCheckChildren is set to true", function() {
      var model = helper.createModelFixture(2, 2);
      var tree = helper.createTree(model);
      tree.multiCheck = true;
      tree.checkable = true;
      tree.autoCheckChildren = true;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0];
      tree.checkNode(node, true);
      expect(node.checked).toEqual(true);
      // every descendant needs to be checked
      scout.Tree.visitNodes(node.childNodes, function(node) {
        expect(node.checked).toEqual(true);
      });
    });

    it("does not check the children if autoCheckChildren is set to false", function() {
      var model = helper.createModelFixture(4, 4);
      var tree = helper.createTree(model);
      tree.multiCheck = true;
      tree.checkable = true;
      tree.autoCheckChildren = false;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0];
      tree.checkNode(node, true);
      expect(node.checked).toEqual(true);
      // no descendant must be checked
      scout.Tree.visitNodes(node.childNodes, function(node) {
        expect(node.checked).toEqual(false);
      });
    });

    it("does not check nodes if checkable is set to false", function() {
      var model = helper.createModelFixture(4, 4);
      var tree = helper.createTree(model);
      tree.multiCheck = false;
      tree.checkable = false;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0];
      tree.checkNode(node, true);

      var checkedNodes = [];
      for (var j = 0; j < tree.nodes.length; j++) {
        if (tree.nodes[j].checked) {
          checkedNodes.push(tree.nodes[j]);
        }
      }
      expect(checkedNodes.length).toBe(0);
    });

  });

  describe("node click", function() {

    it("calls tree._onNodeMouseDown", function() {
      var model = helper.createModelFixture(1);
      var tree = helper.createTree(model);
      spyOn(tree, '_onNodeMouseDown');
      tree.render(session.$entryPoint);

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerMouseDown();

      expect(tree._onNodeMouseDown).toHaveBeenCalled();
    });

    it("updates model (selection)", function() {
      var model = helper.createModelFixture(1);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      expect(tree.selectedNodes.length).toBe(0);

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerClick();

      expect(tree.selectedNodes.length).toBe(1);
      expect(tree.selectedNodes[0].id).toBe(tree.nodes[0].id);
    });

  });

  describe("node double click", function() {

    beforeEach(function() {
      //Expansion happens with an animation (async).
      //Disabling it makes it possible to test the expansion state after the expansion
      $.fx.off = true;
    });

    afterEach(function() {
      $.fx.off = false;
    });

    it("expands/collapses the node", function() {
      var model = helper.createModelFixture(1, 1, false);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      var $node = tree.$container.find('.tree-node:first');
      expect($node).not.toHaveClass('expanded');

      $node.triggerDoubleClick();
      expect($node).toHaveClass('expanded');

      $node.triggerDoubleClick();
      expect($node).not.toHaveClass('expanded');
    });
  });

  describe("node control double click", function() {

    beforeEach(function() {
      //Expansion happens with an animation (async).
      //Disabling it makes it possible to test the expansion state after the expansion
      $.fx.off = true;
    });

    afterEach(function() {
      $.fx.off = false;
    });

    it("does the same as control single click (does NOT expand and immediately collapse again)", function() {
      var model = helper.createModelFixture(1, 1, false);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      var $nodeControl = tree.$container.find('.tree-node-control:first');
      var $node = $nodeControl.parent();
      expect($node).not.toHaveClass('expanded');

      $nodeControl.triggerDoubleClick();
      expect($node).toHaveClass('expanded');

      // Reset internal state because there is no "sleep" in JS
      tree._doubleClickSupport._lastTimestamp -= 5000; // simulate last click 5 seconds ago

      $nodeControl.triggerDoubleClick();
      expect($node).not.toHaveClass('expanded');
    });
  });

  describe("deselectAll", function() {

    it("clears the selection", function() {
      var model = helper.createModelFixture(1, 1);
      var node0 = model.nodes[0];
      model.selectedNodes = [node0.id];

      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);
      expect(tree.$selectedNodes().length).toBe(1);

      tree.deselectAll();

      //Check model
      expect(tree.selectedNodes.length).toBe(0);

      //Check gui
      expect(tree.$selectedNodes().length).toBe(0);
    });
  });

  describe("selectNodes", function() {

    it("selects a node", function() {
      var model = helper.createModelFixture(3, 3, false);
      var tree = helper.createTree(model);
      var node0 = tree.nodes[0];

      tree.render(session.$entryPoint);
      expect(tree.$selectedNodes().length).toBe(0);
      expect(node0.$node.isSelected()).toBe(false);

      tree.selectNodes([node0]);
      //Check model
      expect(tree.selectedNodes.length).toBe(1);
      expect(tree.selectedNodes[0].id).toBe(node0.id);

      //Check gui
      expect(tree.$selectedNodes().length).toBe(1);
      expect(node0.$node.isSelected()).toBe(true);
    });

    it("selectedNode()", function() {
      var model = helper.createModelFixture(2, 2);
      var tree = helper.createTree(model);
      var node0 = tree.nodes[0];
      var node1 = tree.nodes[1];

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

    it("expands the parents if a hidden node should be selected whose parents are collapsed (revealing the selection)", function() {
      var model = helper.createModelFixture(3, 3, false);
      var tree = helper.createTree(model);
      var node0 = tree.nodes[0];
      var child0 = node0.childNodes[0];
      var grandchild0 = child0.childNodes[0];
      tree.render(session.$entryPoint);

      expect(node0.expanded).toBe(false);
      expect(child0.expanded).toBe(false);
      expect(child0.$node).toBeUndefined();

      tree.selectNodes([grandchild0]);
      expect(node0.expanded).toBe(true);
      expect(child0.expanded).toBe(true);
      expect(tree.$selectedNodes().length).toBe(1);
      expect(grandchild0.$node.isSelected()).toBe(true);
    });

    it("also expands the node if bread crumb mode is enabled", function() {
      var model = helper.createModelFixture(1, 1);
      var tree = helper.createTree(model);
      var node0 = tree.nodes[0];

      tree.displayStyle = scout.Tree.DisplayStyle.BREADCRUMB;
      tree.render(session.$entryPoint);

      tree.selectNodes(node0);

      expect(tree.selectedNodes.indexOf(node0) > -1).toBe(true);
      expect(node0.expanded).toBe(true);
    });

    it("sets css class ancestor-of-selected on every ancestor of the selected element", function() {
      var model = helper.createModelFixture(3, 3);
      var tree = helper.createTree(model);
      var nodes = tree.nodes;
      var node1 = nodes[1];
      var child1 = node1.childNodes[1];
      var grandchild1 = child1.childNodes[1];

      tree.render(session.$entryPoint);

      var $parents = tree.$data.find('.tree-node.ancestor-of-selected');
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

    it("sets css class child-of-selected on direct children of the selected element", function() {
      var model = helper.createModelFixture(3, 1, true);
      var tree = helper.createTree(model);
      var nodes = tree.nodes;
      var node1 = nodes[1];
      var child1 = node1.childNodes[1];

      tree.render(session.$entryPoint);

      var $children = tree.$data.find('.tree-node.child-of-selected');
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

    it("may select a node which is not rendered", function() {
      var model = helper.createModelFixture(3, 3);
      var tree = helper.createTree(model);
      var nodes = tree.nodes;
      var child0 = nodes[1].childNodes[0];
      var child1 = nodes[1].childNodes[1];
      tree.viewRangeSize = 1;
      tree.render(session.$entryPoint);

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

    it("sets parent and ancestor css classes even if nodes are not rendered", function() {
      var model = helper.createModelFixture(3, 3);
      var tree = helper.createTree(model);
      var nodes = tree.nodes;
      var child1 = nodes[1].childNodes[1];
      var grandChild1 = child1.childNodes[1];
      tree.viewRangeSize = 1;
      tree.render(session.$entryPoint);

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

    it("sets child-of-selected css class even if nodes are not rendered", function() {
      var model = helper.createModelFixture(3, 3);
      var tree = helper.createTree(model);
      var nodes = tree.nodes;
      var child1 = nodes[1].childNodes[1];
      var grandChild1 = child1.childNodes[1];
      tree.viewRangeSize = 1;
      tree.render(session.$entryPoint);

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

  describe("expandNode", function() {
    var model, tree, nodes, node1;

    beforeEach(function() {
      model = helper.createModelFixture(3, 3);
      tree = helper.createTree(model);
      nodes = tree.nodes;
      node1 = nodes[1];
    });

    it("sets css class child-of-selected on direct children if the expanded node is selected", function() {
      tree.render(session.$entryPoint);

      tree.selectNodes(node1);
      var $children = tree.$data.find('.tree-node.child-of-selected');
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

    it("renders the child nodes if parent is expanded", function() {
      tree.render(session.$entryPoint);

      var $nodes = helper.findAllNodes(tree);
      expect($nodes.length).toBe(3);

      tree.expandNode(node1);
      $nodes = helper.findAllNodes(tree);
      expect($nodes.length).toBe(6);
      expect(node1.$node[0]).toBe($nodes[1]);
      expect(node1.childNodes[0].$node[0]).toBe($nodes[2]);
      expect(node1.childNodes[1].$node[0]).toBe($nodes[3]);
      expect(node1.childNodes[2].$node[0]).toBe($nodes[4]);
    });

    describe("with breadcrumb style", function() {

      beforeEach(function() {
        tree.setDisplayStyle(scout.Tree.DisplayStyle.BREADCRUMB);
      });

      it("renders the child nodes if parent is expanded", function() {
        tree.render(session.$entryPoint);

        var $nodes = helper.findAllNodes(tree);
        expect($nodes.length).toBe(3);

        tree.selectNode(node1); // select node calls expand node
        $nodes = helper.findAllNodes(tree);
        expect($nodes.length).toBe(4);
        expect(node1.$node[0]).toBe($nodes[0]);
        expect(node1.childNodes[0].$node[0]).toBe($nodes[1]);
        expect(node1.childNodes[1].$node[0]).toBe($nodes[2]);
        expect(node1.childNodes[2].$node[0]).toBe($nodes[3]);
      });
    });
  });

  describe("expandAllParentNodes", function() {

    it("expands all parent nodes of the given node (model)", function() {
      var model = helper.createModelFixture(3, 3);
      var tree = helper.createTree(model);
      var nodes = tree.nodes;

      expect(nodes[0].expanded).toBe(false);
      expect(nodes[0].childNodes[0].expanded).toBe(false);
      expect(nodes[0].childNodes[0].childNodes[0].expanded).toBe(false);

      tree._expandAllParentNodes(nodes[0].childNodes[0].childNodes[0]);
      expect(nodes[0].expanded).toBe(true);
      expect(nodes[0].childNodes[0].expanded).toBe(true);
      expect(nodes[0].childNodes[0].childNodes[0].expanded).toBe(false);
    });

    it("expands all parent nodes of the given node (html)", function() {
      var model = helper.createModelFixture(3, 3);
      var tree = helper.createTree(model);
      var nodes = tree.nodes;
      tree.render(session.$entryPoint);

      expect(nodes[0].$node).not.toHaveClass('expanded');
      expect(nodes[0].childNodes[0].$node).toBeFalsy();
      expect(nodes[0].childNodes[0].childNodes[0].$node).toBeFalsy();

      tree._expandAllParentNodes(nodes[0].childNodes[0].childNodes[0]);
      expect(nodes[0].$node).toHaveClass('expanded');
      expect(nodes[0].childNodes[0].$node).toHaveClass('expanded');
      expect(nodes[0].childNodes[0].childNodes[0].$node).not.toHaveClass('expanded');
    });

  });

  describe("collapseNode", function() {

    it("prevents collapsing in bread crumb mode if node is selected", function() {
      var model = helper.createModelFixture(1, 1);
      var tree = helper.createTree(model);
      var node0 = tree.nodes[0];

      tree.displayStyle = scout.Tree.DisplayStyle.BREADCRUMB;
      tree.render(session.$entryPoint);

      tree.selectNodes(node0);

      expect(tree.selectedNodes.indexOf(node0) > -1).toBe(true);
      expect(node0.expanded).toBe(true);

      tree.collapseNode(node0);

      // Still true
      expect(node0.expanded).toBe(true);
    });
  });

  describe("collapseAll", function() {

    it("collapses all nodes", function() {
      var i;
      var model = helper.createModelFixture(3, 2, true);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      var allNodes = [];
      tree._visitNodes(tree.nodes, function(node) {
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

  describe("updateItemPath", function() {
    var model, tree, node1, child1, grandchild1, nodes;

    beforeEach(function() {
      model = helper.createModelFixture(3, 3);
      tree = helper.createTree(model);
      nodes = tree.nodes;
      node1 = nodes[1];
      child1 = node1.childNodes[1];
      grandchild1 = child1.childNodes[1];
    });

    it("Sets css class group on every element within the same group", function() {
      tree.render(session.$entryPoint);
      tree._isGroupingEnd = function(node) {
        return node.nodeType === scout.Page.NodeType.TABLE;
      };

      tree.selectNodes([]);
      tree._renderViewport();
      var $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(0);

      tree.selectNodes(node1);
      tree._renderViewport();
      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);

      node1.nodeType = scout.Page.NodeType.TABLE;
      tree.selectNodes(child1);
      tree._renderViewport();
      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(child1.$node[0]);

      node1.nodeType = scout.Page.NodeType.TABLE;
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

  describe("updateNodeOrder", function() {
    var model, tree, nodes;

    beforeEach(function() {
      model = helper.createModelFixture(3, 3);
      tree = helper.createTree(model);
      nodes = tree.nodes;
    });

    it("reorders the child nodes if parent is given (model)", function() {
      var parentNode = nodes[1];
      var childNode0 = parentNode.childNodes[0];
      var childNode1 = parentNode.childNodes[1];
      var childNode2 = parentNode.childNodes[2];

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

    it("reorders the child nodes if parent is given and expanded (model)", function() {
      var parentNode = nodes[1];
      var childNode0 = parentNode.childNodes[0];
      var childNode1 = parentNode.childNodes[1];
      var childNode2 = parentNode.childNodes[2];

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

    it("reorders the child nodes if parent is given (html)", function() {
      var parentNode = nodes[1];
      var childNode0 = parentNode.childNodes[0];
      var childNode1 = parentNode.childNodes[1];
      var childNode2 = parentNode.childNodes[2];
      tree.render(session.$entryPoint);
      tree.expandNode(parentNode);

      tree.updateNodeOrder([childNode2, childNode1, childNode0], parentNode);
      var $childNodes = parentNode.$node.nextUntil(nodes[2].$node);
      expect($childNodes.eq(0).data('node').id).toBe(childNode2.id);
      expect($childNodes.eq(1).data('node').id).toBe(childNode1.id);
      expect($childNodes.eq(2).data('node').id).toBe(childNode0.id);
    });

    it("considers view range when updating child node order", function() {
      var parentNode = nodes[0];
      var childNode0 = parentNode.childNodes[0];
      var childNode1 = parentNode.childNodes[1];
      var childNode2 = parentNode.childNodes[2];
      tree.viewRangeSize = 3;
      tree.render(session.$entryPoint);
      tree.expandNode(parentNode);

      // Needs explicit rendering of the viewport because this would be done later by the layout
      tree._renderViewport();
      tree.updateNodeOrder([childNode2, childNode1, childNode0], parentNode);
      // Needs explicit rendering again...
      tree._renderViewport();

      expect(tree.viewRangeRendered).toEqual(new scout.Range(0, 3));
      var $nodes = tree.$nodes();
      expect($nodes.length).toBe(3);
      expect(tree.nodes.length).toBe(3);
      expect(parentNode.childNodes.length).toBe(3);
      expect($nodes.eq(0).data('node').id).toBe(parentNode.id);
      expect($nodes.eq(1).data('node').id).toBe(childNode2.id);
      expect($nodes.eq(2).data('node').id).toBe(childNode1.id);
    });

    it("reorders expanded child nodes if parent is given (model)", function() {
      var parentNode = nodes[1];
      var childNode0 = parentNode.childNodes[0];
      var childNode1 = parentNode.childNodes[1];
      var childNode2 = parentNode.childNodes[2];

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

    it("reorders the root nodes if no parent is given (model)", function() {
      var node0 = nodes[0];
      var node1 = nodes[1];
      var node2 = nodes[2];

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

    it("reorders the root nodes if no parent is given (html)", function() {
      var node0 = nodes[0];
      var node1 = nodes[1];
      var node2 = nodes[2];

      tree.render(session.$entryPoint);
      tree.updateNodeOrder([node2, node1, node0]);
      var $nodes = tree.$nodes();
      expect($nodes.eq(0).data('node').id).toBe(node2.id);
      expect($nodes.eq(1).data('node').id).toBe(node1.id);
      expect($nodes.eq(2).data('node').id).toBe(node0.id);
    });

    it("reorders expanded root nodes if no parent is given (model)", function() {
      var node0 = nodes[0];
      var node1 = nodes[1];
      var node2 = nodes[2];

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

    it("reorders expanded root nodes if no parent is given (html)", function() {
      var node0 = nodes[0];
      var node1 = nodes[1];
      var node2 = nodes[2];

      tree.render(session.$entryPoint);
      tree.expandNode(node1);
      tree.updateNodeOrder([node2, node0, node1]);
      var $nodes = tree.$nodes();
      expect($nodes.eq(0).data('node').id).toBe(node2.id);
      expect($nodes.eq(1).data('node').id).toBe(node0.id);
      expect($nodes.eq(2).data('node').id).toBe(node1.id);
      expect($nodes.eq(3).data('node').id).toBe(node1.childNodes[0].id);
      expect($nodes.eq(4).data('node').id).toBe(node1.childNodes[1].id);
      expect($nodes.eq(5).data('node').id).toBe(node1.childNodes[2].id);
    });
  });

  describe("tree filter", function() {

    it("filters nodes when filter() is called", function() {
      var model = helper.createModelFixture(1, 1, true);
      var tree = helper.createTree(model);
      tree.render(session.$entryPoint);

      var filter = {
        accept: function(node) {
          return node === tree.nodes[0];
        }
      };
      tree.addFilter(filter);
      tree.filter();
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(false);

      tree.removeFilter(filter);
      tree.filter();
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(true);
    });

    it("filters nodes when filter is added and removed", function() {
      var model = helper.createModelFixture(1, 1, true);
      var tree = helper.createTree(model);
      var filter = {
        accept: function(node) {
          return node === tree.nodes[0];
        }
      };
      tree.addFilter(filter);
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(false);

      tree.removeFilter(filter);
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(true);
    });

    it("makes sure only filtered nodes are displayed when node gets expanded", function() {
      var model = helper.createModelFixture(2, 1);
      var tree = helper.createTree(model);
      var filter = {
        accept: function(node) {
          return node === tree.nodes[0] || node === tree.nodes[0].childNodes[0];
        }
      };
      tree.addFilter(filter);
      tree.render(session.$entryPoint);

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

    it("applies filter if a node gets changed", function() {
      var model = helper.createModelFixture(2, 1);
      var tree = helper.createTree(model);
      var filter = {
        accept: function(node) {
          return node.text === 'node 0';
        }
      };
      tree.addFilter(filter);
      tree.render(session.$entryPoint);

      expect(tree.nodes[0].attached).toBe(true);
      expect(tree.nodes[1].attached).toBe(false);

      tree.nodes[0].text = 'new Text';
      tree.changeNode(tree.nodes[0]);

      expect(tree.nodes[0].text).toBe('new Text');
      // text has changed -> filter condition returns false -> must not be visible anymore
      expect(tree.nodes[0].attached).toBe(false);
      expect(tree.nodes[1].attached).toBe(false);
    });

    it("applies filter if a node gets inserted", function() {
      var model = helper.createModelFixture(2, 1, true);
      var tree = helper.createTree(model);
      var filter = {
        accept: function(node) {
          return node.text === 'node 0';
        }
      };
      tree.addFilter(filter);
      tree.render(session.$entryPoint);

      expect(tree.nodes[0].childNodes.length).toBe(2);
      expect(tree.nodes[0].rendered).toBe(true);
      expect(tree.nodes[1].rendered).toBe(false);

      var newNode = helper.createModelNode('', 'newNode0Child1');
      tree.insertNodes([newNode], tree.nodes[0], 2);

      expect(tree.nodes[0].childNodes.length).toBe(3);
      expect(tree.nodes[0].rendered).toBe(true);
      expect(tree.nodes[1].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[0].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[1].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[2].rendered).toBe(false);
      expect(tree.nodes[0].childNodes[2].filterAccepted).toBe(false);

      newNode = helper.createModelNode('', 'node 0', 3);
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
    it("make sure nodes unchanged by filters are attached. See ticket #168957", function() {
      var i,
        topLevelNode3,
        topLevelNodes = [],
        childNodes = [],
        childNodeNames = ['A1','A2','A3','A4','A+B','B1','B2','B3','B4'];

      // child nodes
      for (i = 0; i < childNodeNames.length; i++) {
        childNodes.push(helper.createModelNode('', childNodeNames[i], i));
      }

      // top level nodes
      topLevelNodes.push(helper.createModelNode('', 'TopLevel 1', 0));
      topLevelNodes.push(helper.createModelNode('', 'TopLevel 2', 1));

      topLevelNode3 = helper.createModelNode('', 'TopLevel 3', 2);
      topLevelNode3.childNodes = childNodes;
      topLevelNodes.push(topLevelNode3);

      topLevelNodes.push(helper.createModelNode('', 'TopLevel 4', 3));
      topLevelNodes.push(helper.createModelNode('', 'TopLevel 5', 4));

      // filters
      var model = helper.createModel(topLevelNodes);
      var tree = helper.createTree(model);
      var filterA = {
        accept: function(node) {
          if (node.level === 0) {
            return true;
          } else {
            return scout.strings.startsWith(node.text, 'A');
          }
        }
      };
      var filterB = {
        accept: function(node) {
          if (node.level === 0) {
            return true;
          } else {
            return scout.strings.startsWith(node.text, 'B') || node.text === 'A+B';
          }
        }
      };

      // test
      tree.setViewRangeSize(5);
      tree.setNodeExpanded(tree.visibleNodesFlat[2], true);
      tree.render(session.$entryPoint);

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
  });

  describe("tree enabled/disabled", function() {

    var model;
    var tree;
    var node0;
    var node1;
    var node2;

    beforeEach(function() {
      model = helper.createModelFixture(3, 1, true);
      model.checkable = true;
      model.nodes[2].enabled = false;
      tree = helper.createTree(model);

      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
    });

    it("disables checkboxes when tree is disabled", function() {
      tree.render(session.$entryPoint);

      expect(node0.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).not.toHaveClass('disabled');
      expect(node2.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).toHaveClass('disabled');

      tree.setEnabled(false);
      expect(node0.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).toHaveClass('disabled');
      expect(node2.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).toHaveClass('disabled');

      // Re-enable tree
      tree.setEnabled(true);
      expect(node0.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).not.toHaveClass('disabled');
      expect(node2.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).toHaveClass('disabled');
    });
  });

  describe("test visible list and map", function() {

    describe("with initial all expanded nodes", function() {
      var model, tree;
      beforeEach(function() {
        model = helper.createModelFixture(3, 2, true);
        tree = helper.createTree(model);
      });

      it("init with all expanded in correct order", function() {
        var index = 0;
        tree._visitNodes(tree.nodes, function(node) {
          expect(tree.visibleNodesFlat.indexOf(node) === index).toBeTruthy();
          expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          index++;
        });
      });

      it("collapse a node -> all children have to be removed", function() {
        var collapseNode = tree.nodes[0];
        tree.collapseNode(collapseNode);

        tree.nodes.forEach(function(node) {
          expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
          expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          if (node === collapseNode) {
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            });
          } else {
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            });
          }
        });
      });

      it("filter node -> filtered node and children has to be removed from visible", function() {
        var filterNode = tree.nodes[0];
        var filter = {
          accept: function(node) {
            return node !== filterNode;
          }
        };
        tree.addFilter(filter);

        tree.nodes.forEach(function(node) {
          if (node === filterNode) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            });
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            });
          }
        });
      });

      it("update node -> node is filtered", function() {

        var filter = {
          accept: function(node) {
            return node.enabled;
          }
        };
        tree.addFilter(filter);

        tree._visitNodes(tree.nodes, function(childNode) {
          expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
          expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
        });
        var nodeToChange = tree.nodes[0];

        var clone = {
          checked: nodeToChange.checked,
          childNodeIndex: nodeToChange.childNodeIndex,
          childNodes: nodeToChange.childNodes,
          enabled: false,
          expanded: nodeToChange.expanded,
          expandedLazy: nodeToChange.expandedLazy,
          id: "0",
          lazyExpandingEnabled: nodeToChange.lazyExpandingEnabled,
          leaf: nodeToChange.leaf,
          text: nodeToChange.text
        };

        tree.updateNodes([clone]);

        tree.nodes.forEach(function(node) {
          if (node === nodeToChange) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            });
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            });
          }
        });
      });

      it("insert expanded node to expanded parent", function() {
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        newNode0Child3.expanded = true;
        tree.insertNodes([newNode0Child3], tree.nodes[0]);

        var newNode0Child3Child0 = helper.createModelNode('0_3_1', 'newNode0Child3Child0', 0);
        var treeNodeC3 = tree._nodeById(newNode0Child3.id);
        tree.insertNodes([newNode0Child3Child0], treeNodeC3);
        var treeNodeC3C0 = tree._nodeById(newNode0Child3Child0.id);

        expect(tree.visibleNodesFlat.indexOf(treeNodeC3) > -1).toBeTruthy();
        expect(tree.visibleNodesMap[treeNodeC3.id]).toBeTruthy();
        expect(tree.visibleNodesFlat.indexOf(treeNodeC3C0) > -1).toBeTruthy();
        expect(tree.visibleNodesMap[treeNodeC3C0.id]).toBeTruthy();
      });

      it("insert child node in filtered parent", function() {
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        newNode0Child3.expanded = true;
        var filter = {
          accept: function(node) {
            return tree.nodes[0].id !== node.id;
          }
        };
        tree.addFilter(filter);

        tree.nodes.forEach(function(node) {
          if (node === tree.nodes[0]) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            });
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            });
          }
        });
        tree.insertNodes([newNode0Child3], tree.nodes[0]);
        tree.nodes.forEach(function(node) {
          if (node === tree.nodes[0]) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeFalsy();
              expect(tree.visibleNodesMap[childNode.id]).toBeFalsy();
            });
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
            tree._visitNodes(node.childNodes, function(childNode) {
              expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
              expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
            });
          }
        });

      });

      it("insert child node which should be filtered", function() {
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        newNode0Child3.expanded = true;
        var filter = {
          accept: function(node) {
            return newNode0Child3.id !== node.id;
          }
        };
        tree.addFilter(filter);
        tree._visitNodes(tree.nodes, function(childNode) {
          expect(tree.visibleNodesFlat.indexOf(childNode) > -1).toBeTruthy();
          expect(tree.visibleNodesMap[childNode.id]).toBeTruthy();
        });
        tree.insertNodes([newNode0Child3], tree.nodes[0]);

        var treeNode0C3 = tree._nodeById(newNode0Child3.id);
        tree._visitNodes(tree.nodes, function(node) {
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

    describe("with initial all closed nodes", function() {
      var model, tree;
      beforeEach(function() {
        model = helper.createModelFixture(3, 2, false);
        tree = helper.createTree(model);
      });

      it("init with all collapsed", function() {
        tree._visitNodes(tree.nodes, function(node) {
          if (tree.nodes.indexOf(node) > -1) {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeTruthy();
            expect(tree.visibleNodesMap[node.id]).toBeTruthy();
          } else {
            expect(tree.visibleNodesFlat.indexOf(node) > -1).toBeFalsy();
            expect(tree.visibleNodesMap[node.id]).toBeFalsy();
          }
        });
      });

      it("insert child node collapsed parent", function() {
        var newNode0Child3 = helper.createModelNode('0_3', 'newNode0Child3', 3);
        newNode0Child3.expanded = true;
        tree.insertNodes([newNode0Child3], tree.nodes[0]);
        var newNode0Child3Child0 = helper.createModelNode('0_3_1', 'newNode0Child3Child0', 0);
        tree.insertNodes([newNode0Child3Child0], tree._nodeById(newNode0Child3.id));
        expect(tree.visibleNodesFlat.indexOf(newNode0Child3) > -1).toBeFalsy();
        expect(tree.visibleNodesMap[newNode0Child3.id]).toBeFalsy();
        expect(tree.visibleNodesFlat.indexOf(newNode0Child3Child0) > -1).toBeFalsy();
        expect(tree.visibleNodesMap[newNode0Child3Child0.id]).toBeFalsy();
      });

      it("expand node", function() {
        var node0 = tree.nodes[0];
        tree.expandNode(node0);
        tree._visitNodes(tree.nodes, function(node) {
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
        //check order
        for (var i = 0; i < tree.visibleNodesFlat.length; i++) {
          var nodeInList = tree.visibleNodesFlat[i];
          if (i === 0) {
            expect(nodeInList.id === '0').toBeTruthy();
          } else if (i < 4) {
            expect(nodeInList.id === '0_' + (i - 1)).toBeTruthy();
          } else {
            expect(nodeInList.id === String(i - 3)).toBeTruthy();
          }
        }

      });

      it("expand child node", function() {
        var node0 = tree.nodes[0],
          node0_0 = node0.childNodes[0];
        tree.expandNode(node0);
        tree.expandNode(node0_0);
        tree._visitNodes(tree.nodes, function(node) {
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
        for (var i = 0; i < tree.visibleNodesFlat.length; i++) {
          var nodeInList = tree.visibleNodesFlat[i];
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

  describe("destroy tree nodes", function() {

    var tree;

    beforeEach(function() {
      var model = helper.createModelFixture(2, 1, false);
      tree = helper.createTree(model);
    });

    it("should destroy all tree nodes and set destroyed flag", function()  {
      var nodesMapCopy = $.extend({}, tree.nodesMap);
      scout.objects.values(nodesMapCopy).forEach(function(node) {
        expect(node.destroyed).toBe(false);
      });
      tree.destroy();
      scout.objects.values(nodesMapCopy).forEach(function(node) {
        expect(node.destroyed).toBe(true);
      });
      expect(scout.objects.countOwnProperties(tree.nodesMap)).toBe(0);
      expect(scout.objects.countOwnProperties(tree.nodes.length)).toBe(0);
    });

  });

});
