/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.LazyNodeFilter = function(tree) { //
  this.tree = tree;
};

scout.LazyNodeFilter.prototype.accept = function(node) {
  if (!node.expanded && node.parentNode && node.parentNode.expandedLazy && node.parentNode.lazyExpandingEnabled && this.tree.lazyExpandingEnabled) {
    // if this node is not expanded and parent is lazyExpanding.
    for (var i = 0; i < this.tree.selectedNodes.length; i++) {
      var selectedNode = this.tree.selectedNodes[i];
      //not initialized selected nodes
      if (typeof selectedNode === 'string') {
        break;
      }
      if (selectedNode === node || selectedNode.isChildOf(node)) {
        return true;
      }
    }
    return false;
  }
  return true;
};
