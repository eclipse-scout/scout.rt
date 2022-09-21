/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

export default class LazyNodeFilter {

  constructor(tree) {
    this.tree = tree;
  }

  accept(node) {
    if (node.expanded) {
      return true;
    }
    // not expanded: remove lazy expand marker (forget lazy expanded children)
    node.childNodes.forEach(child => {
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
