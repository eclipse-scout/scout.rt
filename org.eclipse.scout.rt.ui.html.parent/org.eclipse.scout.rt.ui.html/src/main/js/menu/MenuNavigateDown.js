scout.MenuNavigateDown = function(outline, node) {
  scout.MenuNavigateDown.parent.call(this, outline, node);
  this._text1 = 'Continue';
  this._text2 = 'Show';
  this.defaultMenu = true;
};
scout.inherits(scout.MenuNavigateDown, scout.AbstractOutlineNavigationMenu);


scout.MenuNavigateDown.prototype._isDetail = function() {
  return this.node.detailFormVisible;
};

scout.MenuNavigateDown.prototype._toggleDetail = function() {
  return false;
};

scout.MenuNavigateDown.prototype._menuEnabled = function() {
  return !this.node.leaf;
};

scout.MenuNavigateDown.prototype._drill = function() {
  var drillNode, row, rowIds = this.node.detailTable.selectedRowIds;
  if (rowIds && rowIds.length > 0) {
    row = this.node.detailTable.rowById(rowIds[0]);
    drillNode = this.outline._nodeMap[row.nodeId];
    $.log.debug('drill down to node ' + drillNode);
    this.outline.setNodesSelected(drillNode);
    this.outline.setNodeExpanded(drillNode, undefined, false);
  }
};

