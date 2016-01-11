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
scout.DetailTableTreeFilter = function() { //
};

/**
 * Must correspond with logic in P_TableFilterBasedTreeNodeFilter
 */
scout.DetailTableTreeFilter.prototype.accept = function(node) {
  var row;

  if (!node.parentNode) {
    // top level nodes may not be filtered
    return true;
  }
  if (!node.parentNode.filterAccepted) {
    // hide node if parent node is hidden
    return false;
  }
  if (!node.parentNode.detailTable) {
    // if parent has no detail table, node.row won't be set
    // detailTable may be undefined if node.detailTableVisible is false
    return true;
  }
  if (!node.row) {
    // link not yet established, as soon as row gets inserted and filtered, a refilter will be triggered on the tree
    return true;
  }
  return node.row.filterAccepted;
};
