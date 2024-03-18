/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Filter, Tree, TreeNode} from '../index';

export type LazyNodeFilterTreeNode = TreeNode & { _lazyNodeFilterAccepted?: boolean };

export class LazyNodeFilter implements Filter<LazyNodeFilterTreeNode> {
  tree: Tree;

  constructor(tree: Tree) {
    this.tree = tree;
  }

  accept(node: LazyNodeFilterTreeNode): boolean {
    if (node.expanded) {
      return true;
    }
    // not expanded: remove lazy expand marker (forget lazy expanded children)
    node.childNodes.forEach((child: LazyNodeFilterTreeNode) => {
      child._lazyNodeFilterAccepted = false;
    });

    if (!node.parentNode || !node.parentNode.expandedLazy || !node.parentNode.lazyExpandingEnabled || !this.tree.lazyExpandingEnabled) {
      // no lazy expanding supported
      return true;
    }

    // if this node is not expanded and parent is lazyExpanding.
    for (let i = 0; i < this.tree.selectedNodes.length; i++) {
      let selectedNode = this.tree.selectedNodes[i];
      if (typeof selectedNode === 'string') {
        break;
      }
      if (selectedNode === node) {
        node._lazyNodeFilterAccepted = true;
        return true;
      }
    }
    return !!node._lazyNodeFilterAccepted;
  }
}
