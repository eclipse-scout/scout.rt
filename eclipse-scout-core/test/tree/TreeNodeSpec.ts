/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {scout, Tree, TreeNode} from '../../src/index';

describe('TreeNode', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('isAncestorOf', () => {
    it('returns true if the node is an ancestor of the given node ', () => {
      let tree = scout.create(Tree, {parent: session.desktop});
      let rootNode = scout.create(TreeNode, {
        parent: tree
      });
      let parentNode = scout.create(TreeNode, {
        parent: tree,
        parentNode: rootNode
      });
      let node = scout.create(TreeNode, {
        parent: tree,
        parentNode: parentNode
      });
      expect(rootNode.isAncestorOf(parentNode)).toBe(true);
      expect(rootNode.isAncestorOf(node)).toBe(true);
      expect(parentNode.isAncestorOf(node)).toBe(true);

      expect(node.isAncestorOf(parentNode)).toBe(false);
      expect(node.isAncestorOf(rootNode)).toBe(false);
      expect(node.isAncestorOf(node)).toBe(false);
    });
  });

  describe('isDescendantOf', () => {
    it('returns true if the node is a descendant of the given node ', () => {
      let tree = scout.create(Tree, {parent: session.desktop});
      let rootNode = scout.create(TreeNode, {
        parent: tree
      });
      let parentNode = scout.create(TreeNode, {
        parent: tree,
        parentNode: rootNode
      });
      let node = scout.create(TreeNode, {
        parent: tree,
        parentNode: parentNode
      });
      expect(node.isDescendantOf(parentNode)).toBe(true);
      expect(node.isDescendantOf(rootNode)).toBe(true);

      expect(rootNode.isDescendantOf(parentNode)).toBe(false);
      expect(rootNode.isDescendantOf(node)).toBe(false);
      expect(parentNode.isDescendantOf(node)).toBe(false);
      expect(node.isDescendantOf(node)).toBe(false);
    });
  });
});
