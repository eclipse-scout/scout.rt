/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class LazyNodeFilter {

  constructor(tree) { //
    this.tree = tree;
  }

  accept(node) {
    if (!node.expanded && node.parentNode && node.parentNode.expandedLazy && node.parentNode.lazyExpandingEnabled && this.tree.lazyExpandingEnabled) {
      // if this node is not expanded and parent is lazyExpanding.
      for (let i = 0; i < this.tree.selectedNodes.length; i++) {
        let selectedNode = this.tree.selectedNodes[i];
        // not initialized selected nodes
        if (typeof selectedNode === 'string') {
          break;
        }
        if (selectedNode === node || selectedNode.isDescendantOf(node)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }
}
