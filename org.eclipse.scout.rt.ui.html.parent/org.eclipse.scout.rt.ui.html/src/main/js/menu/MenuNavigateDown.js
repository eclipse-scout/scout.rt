scout.MenuNavigateDown = function() {
  scout.MenuNavigateDown.parent.call(this);
  this._addAdapterProperties('outline');
  this._clickBehavior = function() {};
};
scout.inherits(scout.MenuNavigateDown, scout.Menu);

scout.MenuNavigateDown.prototype.init = function(model, session) {
  scout.MenuNavigateDown.parent.prototype.init.call(this, model, session);
  this._listener = function(event) {
    var text, node = event.node;
    if (node.detailForm && node.detailFormVisible) {
      this._clickBehavior = this._showDetailTable.bind(this, node);
      text = session.text('Continue');
    } else {
      this._clickBehavior = this._drillDown.bind(this, node);
      text = session.text('Show');
    }
    $(this.$container).text(text);
  }.bind(this);
  this.outline.events.on('outlineUpdated nodesSelected', this._listener);
};

scout.MenuNavigateDown.prototype._onMenuClicked = function(event) {
  this._clickBehavior.call(this);
};

scout.MenuNavigateDown.prototype._showDetailTable = function(node) {
  node.detailFormVisible = false;
  $.log.debug('show detail-table');
  this.outline._updateOutlineTab(node);
};

scout.MenuNavigateDown.prototype._drillDown = function(node) {
  var row, rowIds = node.detailTable.selectedRowIds;
  if (rowIds && rowIds.length > 0) {
    row = node.detailTable.rowById(rowIds[0]);

    // TODO AWE: (navi) C.GU fragen... das könnten wir sicher auch noch schöner machen, API?
    var drillNode = this.outline._nodeMap[row.nodeId];
    var $drillRoot = this.outline.$nodeById(node.id);
    $.log.debug('drill down to node ' + drillNode);

    // TODO AWE: (navi) C.GU fragen... in einer methode zusammenführen?
    this.outline.setNodesSelected(drillNode);
    this.outline.setNodeExpanded(drillNode, undefined, false);
  }
};

scout.MenuNavigateDown.prototype.dispose = function() {
  scout.MenuNavigateDown.parent.prototype.dispose.call(this);
  this.outline.events.off('outlineUpdated nodesSelected', this._listener);
};



