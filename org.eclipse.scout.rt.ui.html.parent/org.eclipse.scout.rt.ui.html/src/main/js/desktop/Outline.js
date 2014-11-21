scout.Outline = function() {
  scout.Outline.parent.call(this);
  this._addAdapterProperties('defaultDetailForm');
};
scout.inherits(scout.Outline, scout.Tree);

/**
 * @Override
 */
scout.Outline.prototype._render = function($parent) {
  scout.Outline.parent.prototype._render.call(this, $parent);

  if (this.selectedNodeIds.length === 0) {
    this._updateOutlineTab();
  }
};

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
  var content, parentText, nodeText, title, subTitle;
  if (node) {
    // Unlink detail form if it was closed.
    // May happen in the following case:
    // The form gets closed on execPageDeactivated.
    // No detailFormChanged event will be fired because the deactivated page is not selected anymore
    if (node.detailForm && node.detailForm.destroyed) {
      node.detailForm = null;
    }

    if (node.detailForm && node.detailFormVisible) {
      content = node.detailForm;
    } else {
      content = node.detailTable;
    }

    if (node.parentNode && node.parentNode.text) {
      parentText = node.parentNode.text;
    }
    if (node.detailForm && node.detailForm.title) {
      nodeText = node.detailForm.title;
    } else {
      nodeText = node.text;
    }

    if (parentText && nodeText) {
      title = parentText;
      subTitle = nodeText;
    } else if (parentText) {
      title = parentText;
    } else if (nodeText) {
      title = nodeText;
    }
  }
  else if (this.defaultDetailForm) {
    content = this.defaultDetailForm;
    title = this.defaultDetailForm.title;
  }
  this.session.desktop.updateOutlineTab(content, title, subTitle);
};

/* event handling */

scout.Outline.prototype.onFormChanged = function(nodeId, detailForm) {
  var node;
  if (nodeId >= 0) {
    node = this._nodeMap[nodeId];
    node.detailForm = this.session.getOrCreateModelAdapter(detailForm, this);
    //If the following condition is false, the selection state is not synchronized yet which means there is a selection event in the queue which will be processed right afterwards.
    if (this.selectedNodeIds.indexOf(node.id) >= 0) {
      this._updateOutlineTab(node);
    }
  }
  else {
    this.defaultDetailForm = this.session.getOrCreateModelAdapter(detailForm, this);
    this._updateOutlineTab();
  }
};

scout.Outline.prototype.onTableChanged = function(nodeId, detailTable) {
  var node;
  if (nodeId >= 0) {
    node = this._nodeMap[nodeId];
    node.detailTable = this.session.getOrCreateModelAdapter(detailTable, this);
    // If the following condition is false, the selection state is not synchronized yet which means
    // there is a selection event in the queue which will be processed right afterwards.
    if (this.selectedNodeIds.indexOf(node.id) >= 0) {
      this._updateOutlineTab(node);
    }
  }
  else {
    this._updateOutlineTab();
  }
};

scout.Outline.prototype.onPageChanged = function(nodeId, detailFormVisible) {
  var node;
  if (nodeId >= 0) {
    node = this._nodeMap[nodeId];
    if (node.detailFormVisible != detailFormVisible) {
      node.detailFormVisible = detailFormVisible;
      this._updateOutlineTab(node);
    }
  }
};

scout.Outline.prototype.setDetailFormVisible = function(nodeId, visible) {
  var node;
  if (nodeId >= 0) {
    node = this._nodeMap[nodeId];
    if (node.detailFormVisible != visible) {
      node.detailFormVisible = visible;
      node.session.send('pageChanged', this.id, {nodeId: nodeId, detailFormVisible: visible});
    }
  }
};

scout.Outline.prototype.onModelAction = function(event) {
  if (event.type === 'detailFormChanged') {
    this.onFormChanged(event.nodeId, event.detailForm);
  } else if (event.type === 'detailTableChanged') {
    this.onTableChanged(event.nodeId, event.detailTable);
  } else if (event.type == 'pageChanged') {
    this.onPageChanged(event.nodeId, event.detailFormVisible);
  } else {
    scout.Outline.parent.prototype.onModelAction.call(this, event);
  }
};
