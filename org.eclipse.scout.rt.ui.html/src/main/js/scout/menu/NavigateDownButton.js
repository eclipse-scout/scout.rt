scout.NavigateDownButton = function(outline, node) {
  scout.NavigateDownButton.parent.call(this, outline, node);
  this._text = 'Down';
  this.defaultMenu = true;
  this.objectType = 'NavigateDownButton';
  this.keyStroke = 'Enter';
  this.initKeyStrokeParts();
};
scout.inherits(scout.NavigateDownButton, scout.AbstractNavigationButton);

scout.NavigateDownButton.prototype._buttonEnabled = function() {
  var node = this.outline.nodesMap[this.outline.selectedNodeIds[0]];
  // TODO: use this.outline.selectedRow


  if (node) {
    return !node.leaf;
  } else {
    return false;
  }
};

scout.NavigateDownButton.prototype._drill = function() {
  var drillNode;

  if (this.node.detailTable) {
    var rows = this.node.detailTable.selectedRows;
    if (rows.length > 0) {
      var row = rows[0];
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
