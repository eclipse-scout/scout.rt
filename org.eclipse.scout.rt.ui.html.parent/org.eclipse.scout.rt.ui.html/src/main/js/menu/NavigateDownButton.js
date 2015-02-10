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

scout.NavigateDownButton.prototype._buttonEnabled = function() {
  return !this.node.leaf;
};

scout.NavigateDownButton.prototype._drill = function() {
  var rowIds = this.node.detailTable.selectedRowIds;
  if (rowIds && rowIds.length > 0) {
    var row = this.node.detailTable.rowById(rowIds[0]);
    // TODO BSH Tree | Ask AW.E: Why not like this? Offline? What about row action?
//    this.node.detailTable.sendRowAction(row.$row, null);
    var drillNode = this.outline.nodesMap[row.nodeId];
    $.log.debug('drill down to node ' + drillNode);
    if (!drillNode.$node && drillNode.parentNode) {
      this.outline.setNodeExpanded(drillNode.parentNode, drillNode.parentNode.$node, true);
    }
    this.outline.setNodesSelected(drillNode);
    this.outline.setNodeExpanded(drillNode, drillNode.$node, false); // TODO BSH Tree | Ask AW.E why not true?
  }
};

