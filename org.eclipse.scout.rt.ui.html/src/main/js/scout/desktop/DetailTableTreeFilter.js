scout.DetailTableTreeFilter = function() {
};

/**
 * Must correspond with logic in P_TableFilterBasedTreeNodeFilter
 */
scout.DetailTableTreeFilter.prototype.accept = function($node) {
  var row,
    node = $node.data('node'),
    rowId = node.rowId;

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
