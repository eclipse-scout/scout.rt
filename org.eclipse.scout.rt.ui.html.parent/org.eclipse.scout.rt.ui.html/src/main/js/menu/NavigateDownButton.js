scout.NavigateDownButton = function(outline, node) {
  scout.NavigateDownButton.parent.call(this, outline, node);
  this._text1 = 'Continue';
  this._text2 = 'Show';
  this.systemType = scout.Button.SYSTEM_TYPE.OK;
  this.id = 'NavigateDownButton';
};
scout.inherits(scout.NavigateDownButton, scout.AbstractNavigationButton);


scout.NavigateDownButton.prototype._isDetail = function() {
  return this.node.detailFormVisible;
};

scout.NavigateDownButton.prototype._toggleDetail = function() {
  return false;
};

scout.NavigateDownButton.prototype._menuEnabled = function() {
  return !this.node.leaf;
};

scout.NavigateDownButton.prototype._drill = function() {
  var drillNode, row, rowIds = this.node.detailTable.selectedRowIds;
  if (rowIds && rowIds.length > 0) {
    row = this.node.detailTable.rowsMap(rowIds[0]);
    drillNode = this.outline._nodeMap[row.nodeId];
    $.log.debug('drill down to node ' + drillNode);
    this.outline.setNodesSelected(drillNode);
    this.outline.setNodeExpanded(drillNode, undefined, false);
  }
};

