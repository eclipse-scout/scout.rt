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
import {Range} from '../../src/index';
import {TreeSpecHelper} from '../../src/testing/index';
import {triggerMouseDown} from '../../src/testing/jquery-testing';

describe('CompactTree', () => {
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

  describe('creation', () => {

    it('adds no empty section node', () => {
      // top-level node (section) is only rendered, if there are child nodes
      let model = helper.createModelFixture(1);
      let tree = helper.createCompactTree(model);
      spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new Range(0, 1));
      tree.render();
      expect(tree.nodes.length).toBe(1);
    });

    it('adds a node with child node', () => {
      let model = helper.createModelFixture(1, 1, true);
      let tree = helper.createCompactTree(model);
      spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new Range(0, 4));
      tree.render();
      expect(tree.nodes.length).toBe(1);
      expect(tree.visibleNodesFlat.length).toBe(2);
    });

    it('adds a node with child nodes in correct order', () => {
      let model = helper.createModelFixture(2, 1, true);
      let tree = helper.createCompactTree(model);
      spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new Range(0, 5));
      tree.render();
      expect(tree.nodes.length).toBe(2);
      expect(tree.visibleNodesFlat.length).toBe(6);

      // check $node
      let firstNode = tree.nodes[0].$node.children();
      expect($(firstNode[0]).hasClass('title')).toBe(true);
      expect($(firstNode[0]).text()).toBe('node 0');
      expect($(firstNode[1]).hasClass('section-node')).toBe(true);
      expect($(firstNode[1]).text()).toBe('node 0_0');
      expect($(firstNode[2]).hasClass('section-node')).toBe(true);
      expect($(firstNode[2]).text()).toBe('node 0_1');
    });

    // deletion
    it('deletes a node', () => {
      let model = helper.createModelFixture(2, 1, true);
      let tree = helper.createCompactTree(model);
      spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new Range(0, 5));
      tree.render();
      tree.deleteNodes([tree.nodes[0].childNodes[0]], tree.nodes[0]);
      expect(tree.nodes.length).toBe(2);
      expect(tree.visibleNodesFlat.length).toBe(5);

      // check $node
      let firstNode = tree.nodes[0].$node.children();
      expect($(firstNode[0]).hasClass('title')).toBe(true);
      expect($(firstNode[0]).text()).toBe('node 0');
      expect($(firstNode[1]).hasClass('section-node')).toBe(true);
      expect($(firstNode[1]).text()).toBe('node 0_1');
    });

    // insertions
    it('inserts a child node', () => {
      let model = helper.createModelFixture(2, 1, true);
      let tree = helper.createCompactTree(model);
      let parent0 = tree.nodes[0];
      let child0 = parent0.childNodes[0];
      spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new Range(0, 5));
      tree.render();
      tree.deleteNodes([child0], parent0);
      tree.insertNodes([child0], parent0);

      expect(tree.nodes.length).toBe(2);
      expect(tree.visibleNodesFlat.length).toBe(6);

      // check $node
      let firstNode = parent0.$node.children();
      expect($(firstNode[0]).hasClass('title')).toBe(true);
      expect($(firstNode[0]).text()).toBe('node 0');
      expect($(firstNode[1]).hasClass('section-node')).toBe(true);
      expect($(firstNode[1]).text()).toBe('node 0_0');
      expect($(firstNode[2]).hasClass('section-node')).toBe(true);
      expect($(firstNode[2]).text()).toBe('node 0_1');
    });

  });

  describe('node click', () => {

    it('calls selectNodes', () => {
      let model = helper.createModelFixture(2, 1, true);
      let tree = helper.createCompactTree(model);
      spyOn(tree, 'selectNodes');
      tree.render();

      triggerMouseDown(tree.nodes[0].childNodes[0].$node);
      expect(tree.selectNodes).toHaveBeenCalled();
    });
  });

});
