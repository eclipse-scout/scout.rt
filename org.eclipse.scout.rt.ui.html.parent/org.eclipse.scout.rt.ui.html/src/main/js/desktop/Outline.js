scout.Outline = function() {
  scout.Outline.parent.call(this);
  this._detailTable;
  this._detailForm;
};
scout.inherits(scout.Outline, scout.Tree);

/**
 * @Override
 */
scout.Outline.prototype._initTreeNode = function(parentNode, node) {
  scout.Outline.parent.prototype._initTreeNode.call(this, parentNode, node);

  if (node.detailTable) {
    node.detailTable = this.session.getOrCreateModelAdapter(node.detailTable, this);
  }

  if (node.detailForm) {
    node.detailForm = this.session.getOrCreateModelAdapter(node.detailForm, this);
  }
};

/* user input handling */

scout.Outline.prototype._renderSelection = function($nodes) {
  scout.Outline.parent.prototype._renderSelection.call(this, $nodes);

  if (!$nodes) {
    //Outline does not support multi selection -> [0]
    $nodes = [this._findNodeById(this.selectedNodeIds[0])];
  }

  if ($nodes.length === 0) {
    return;
  }

  var node = $nodes[0].data('node');
  if (node) {
    this._updateOutlineTab(node);
  }
};

scout.Outline.prototype._updateOutlineTab = function(node) {
  // Unlink detail form if it was closed.
  // May happen in the following case:
  // The form gets closed on execPageDeactivated.
  // No detailFormChanged event will be fired because the deactivated page is not selected anymore
  if (node.detailForm && node.detailForm.destroyed) {
    node.detailForm = null;
  }

  var content = node.detailForm;
  var text = node.text;
  if (!content) {
    content = node.detailTable;
  }
  else {
    text = node.detailForm.title;
  }
  this.session.desktop.updateOutlineTab(content, text);
};

/* event handling */

scout.Outline.prototype.onFormChanged = function(nodeId, detailForm) {
  var node = this._nodeMap[nodeId];
  node.detailForm = this.session.getOrCreateModelAdapter(detailForm, this);

  if (this.selectedNodeIds.indexOf(node.id) >= 0) {
    this._updateOutlineTab(node);
  }
};

scout.Outline.prototype.onTableChanged = function(nodeId, detailTable) {
  var node = this._nodeMap[nodeId];
  node.detailTable = this.session.getOrCreateModelAdapter(detailTable, this);

  if (this.selectedNodeIds.indexOf(node.id) >= 0) {
    this._updateOutlineTab(node);
  }
};

scout.Outline.prototype.onModelAction = function(event) {
  if (event.type == 'detailFormChanged') {
    this.onFormChanged(event.nodeId, event.detailForm);
  } else if (event.type == 'detailTableChanged') {
    this.onTableChanged(event.nodeId, event.detailTable);
  } else {
    scout.Outline.parent.prototype.onModelAction.call(this, event);
  }
};
