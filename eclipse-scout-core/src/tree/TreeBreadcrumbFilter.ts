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
import {Tree, TreeNode} from '../index';

export default class TreeBreadcrumbFilter {
  tree: Tree;

  constructor(tree: Tree) {
    this.tree = tree;
  }

  accept(node: TreeNode): boolean {
    if (this.tree.selectedNodes.length === 0) {
      return node.parentNode === undefined;
    }
    return this.tree.isNodeInBreadcrumbVisible(node);
  }
}
