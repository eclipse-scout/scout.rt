/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Tree, TreeNode} from '../index';

export class TreeBreadcrumbFilter {
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
