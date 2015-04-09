scout.NavigateDownButton = function(outline, node) {
  scout.NavigateDownButton.parent.call(this, outline, node);
  this._text1 = 'Continue';
  this._text2 = 'Show';
  this.systemType = scout.Button.SYSTEM_TYPE.OK;
  this.id = 'NavigateDownButton';
  this.keyStroke = 'ENTER';
};
scout.inherits(scout.NavigateDownButton, scout.AbstractNavigationButton);

scout.NavigateDownButton.prototype._isDetail = function() {
  // Button is in "detail mode" if there are both detail form and detail table visible and detail form is _not_ hidden.
  return !!(this.node.detailFormVisible && this.node.detailForm &&
    this.node.detailTableVisible && this.node.detailTable && !this.node.detailFormHiddenByUi);
};

scout.NavigateDownButton.prototype._toggleDetail = function() {
  return false;
};

scout.NavigateDownButton.prototype._buttonEnabled = function() {
  if (this.node.leaf) {
    return false;
  }
  if (this._isDetail()) {
    return true;
  }
  // When node is not a leaf and we're displaying the detail-table - button is only enabled when a single row is selected
  return this.node.detailTable.selectedRowIds.length === 1;
};

scout.NavigateDownButton.prototype._drill = function() {
  var drillNode;

  if (this.node.detailTable) {
    var rowIds = this.node.detailTable.selectedRowIds;
    if (rowIds && rowIds.length > 0) {
      var row = this.node.detailTable.rowById(rowIds[0]);
      drillNode = this.outline.nodesMap[row.nodeId];
    }
  } else {
    drillNode = this.node.childNodes[0];
  }
  if (drillNode) {
    $.log.debug('drill down to node ' + drillNode);
    this.outline.setNodesSelected(drillNode); // this also expands the parent node, if required
    this.outline.setNodeExpanded(drillNode, false);
  }
};
