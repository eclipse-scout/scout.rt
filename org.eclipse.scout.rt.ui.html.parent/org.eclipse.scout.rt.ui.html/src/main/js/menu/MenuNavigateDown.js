scout.MenuNavigateDown = function() {
  scout.MenuNavigateDown.parent.call(this);
  this._text1 = 'Continue';
  this._text2 = 'Show';
};
scout.inherits(scout.MenuNavigateDown, scout.AbstractOutlineNavigationMenu);


scout.MenuNavigateDown.prototype._isDetail = function(node) {
  return node.detailFormVisible;
};

scout.MenuNavigateDown.prototype._toggleDetail = function() {
  return false;
};

scout.MenuNavigateDown.prototype._menuEnabled = function(node) {
  return true;
};

scout.MenuNavigateDown.prototype._drill = function(node) {
  var drillNode, row, rowIds = node.detailTable.selectedRowIds;
  if (rowIds && rowIds.length > 0) {
    row = node.detailTable.rowById(rowIds[0]);
    drillNode = this.outline._nodeMap[row.nodeId];
    $.log.debug('drill down to node ' + drillNode);
    this.outline.setNodesSelected(drillNode);
    this.outline.setNodeExpanded(drillNode, undefined, false);
  }
};

