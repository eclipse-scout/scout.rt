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

/* create form and table */

scout.Outline.prototype._showForm = function(node) {
  if (this._detailForm && this._detailForm !== node.detailForm) {
    this._detailForm.remove();
    this._detailForm = null;
  }

  if (node.detailForm) {
    this._detailForm = node.detailForm;
    if (!this._detailForm.rendered) {
      this.session.desktop.updateOutline(this._detailForm, this._detailForm.title);
    }
  }
};

scout.Outline.prototype._showTable = function(node) {
  if (this._detailTable && this._detailTable !== node.detailTable) {
    this._detailTable.remove();
    this._detailTable = null;
  }

  if (node.detailTable) {
    this._detailTable = node.detailTable;
    if (!this._detailTable.rendered) {
      this.session.desktop.updateOutline(this._detailTable, node.text);
      this.scrollbar.initThumb();
    }
  }
};

/* user input handling */

scout.Outline.prototype._renderSelection = function($nodes) {
  scout.Outline.parent.prototype._renderSelection.call(this, $nodes);

  if (!$nodes) {
    $nodes = [this._findNodeById(this.selectedNodeIds[0])];
  }

  if ($nodes.length === 0) {
    return;
  }

  //Outline does not support multi selection
  var node = $nodes[0].data('node');
  if (node) {
    this._showForm(node);
    this._showTable(node);
  }
};

/* event handling */

scout.Outline.prototype.onFormChanged = function(nodeId, detailForm) {
  var node = this._nodeMap[nodeId];
  node.detailForm = this.session.getOrCreateModelAdapter(detailForm, this);

  if (this.selectedNodeIds.indexOf(node.id) >= 0) {
    this._showForm(node);
  }
};

scout.Outline.prototype.onTableChanged = function(nodeId, detailTable) {
  var node = this._nodeMap[nodeId];
  node.detailTable = this.session.getOrCreateModelAdapter(detailTable, this);

  if (this.selectedNodeIds.indexOf(node.id) >= 0) {
    this._showTable(node);
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
