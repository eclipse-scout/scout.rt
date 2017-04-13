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
describe("Compacttree", function() {
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

  describe("creation", function() {

    it("adds no empty section node", function() {
      //top-level node (section) is only rendered, if there are child nodes
      var model = helper.createModelFixture(1);
      var tree = helper.createCompactTree(model);
      var spy = spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new scout.Range(0, 1));
      tree.render(session.$entryPoint);
      expect(tree.nodes.length).toBe(1);
    });

    it("adds a node with child node", function() {
      var model = helper.createModelFixture(1,1, true);
      var tree = helper.createCompactTree(model);
      var spy = spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new scout.Range(0, 4));
      tree.render(session.$entryPoint);
      expect(tree.nodes.length).toBe(1);
      expect(tree.visibleNodesFlat.length).toBe(2);
    });

    it("adds a node with child nodes in correct order", function() {
      var model = helper.createModelFixture(2,1,true);
      var tree = helper.createCompactTree(model);
      var spy = spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new scout.Range(0, 5));
      tree.render(session.$entryPoint);
      expect(tree.nodes.length).toBe(2);
      expect(tree.visibleNodesFlat.length).toBe(6);

      //check $node
      var firstNode = tree.nodes[0].$node.children();
      expect($(firstNode[0]).hasClass("title")).toBe(true);
      expect($(firstNode[0]).text()).toBe("node 0");
      expect($(firstNode[1]).hasClass("section-node")).toBe(true);
      expect($(firstNode[1]).text()).toBe("node 0_0");
      expect($(firstNode[2]).hasClass("section-node")).toBe(true);
      expect($(firstNode[2]).text()).toBe("node 0_1");
    });

    //deletion
    it("deletes a node", function() {
      var model = helper.createModelFixture(2,1,true);
      var tree = helper.createCompactTree(model);
      var spy = spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new scout.Range(0, 5));
      tree.render(session.$entryPoint);
      tree.deleteNodes([tree.nodes[0].childNodes[0]], tree.nodes[0]);
      expect(tree.nodes.length).toBe(2);
      expect(tree.visibleNodesFlat.length).toBe(5);

      //check $node
      var firstNode = tree.nodes[0].$node.children();
      expect($(firstNode[0]).hasClass("title")).toBe(true);
      expect($(firstNode[0]).text()).toBe("node 0");
      expect($(firstNode[1]).hasClass("section-node")).toBe(true);
      expect($(firstNode[1]).text()).toBe("node 0_1");
    });

    //insertions
    it("inserts a child node", function() {
      var model = helper.createModelFixture(2,1,true);
      var tree = helper.createCompactTree(model);
      var parent0 = tree.nodes[0];
      var child0 = parent0.childNodes[0];
      var spy = spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new scout.Range(0, 5));
      tree.render(session.$entryPoint);
      tree.deleteNodes([child0], parent0);
      tree.insertNodes([child0], parent0);

      expect(tree.nodes.length).toBe(2);
      expect(tree.visibleNodesFlat.length).toBe(6);

      //check $node
      var firstNode = parent0.$node.children();
      expect($(firstNode[0]).hasClass("title")).toBe(true);
      expect($(firstNode[0]).text()).toBe("node 0");
      expect($(firstNode[1]).hasClass("section-node")).toBe(true);
      expect($(firstNode[1]).text()).toBe("node 0_0");
      expect($(firstNode[2]).hasClass("section-node")).toBe(true);
      expect($(firstNode[2]).text()).toBe("node 0_1");
    });

  });

  describe("node click", function() {

    it("calls selectNodes", function() {
      var model = helper.createModelFixture(2,1,true);
      var tree = helper.createCompactTree(model);
      spyOn(tree, 'selectNodes');
      tree.render(session.$entryPoint);

      tree.nodes[0].childNodes[0].$node.triggerMouseDown();
      expect(tree.selectNodes).toHaveBeenCalled();
    });
  });

});
