scout.DetailTableTreeFilter = function() {
};

scout.DetailTableTreeFilter.prototype.accept = function($node) {
  var row,
    node = $node.data('node'),
    rowId = node.rowId;

  if (!node.parentNode) {
    // top level nodes may not be filtered
    return true;
  }
  if (!node.parentNode.detailTable) {
    // if parent has no detail table, node.row won't be set
    // detailTable may be undefined if node.detailTableVisible is false
    return true;
  }
  return !node.row.rejectedByFilter;
};
