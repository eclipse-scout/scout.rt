/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

describe('TreeNode', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('isAncestorOf', function() {
    it('returns true if the node is an ancestor of the given node ', function() {
      var tree = scout.create('Tree', {parent: session.desktop});
      var rootNode = scout.create('TreeNode', {
        parent: tree
      });
      var parentNode = scout.create('TreeNode', {
        parent: tree,
        parentNode: rootNode
      });
      var node = scout.create('TreeNode', {
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
});
