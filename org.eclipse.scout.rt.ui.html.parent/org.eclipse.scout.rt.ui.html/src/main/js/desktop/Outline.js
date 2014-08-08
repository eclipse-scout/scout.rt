scout.Outline = function() {
  scout.Outline.parent.call(this);
  this._detailTable;
  this._detailForm;
};
scout.inherits(scout.Outline, scout.Tree);

scout.Outline.prototype.showPageDetailForm = function(node) {
  //unlink detail form if it was closed
  if (node.detailForm && node.detailForm.destroyed) {
    node.detailForm = null;
  }

  if (this._detailForm && this._detailForm !== node.detailForm) {
    this.session.desktop.removeForm(this._detailForm);
    this._detailForm = null;
  }

  if (node.detailForm) {
    this._detailForm = node.detailForm;
    if (!this._detailForm.rendered) {
      this.session.desktop.addForm(this._detailForm);
    }
  }
};

scout.Outline.prototype.showPageDetailTable = function(node) {
  var detailTable = node.table;

  if (this._detailTable && this._detailTable !== detailTable) {
    this.session.desktop.removePageDetailTable(node, this._detailTable);
    this._detailTable.desktopMenuContributor = false;
    this._detailTable = null;
  }

  if (detailTable) {
    this._detailTable = detailTable;
    this._detailTable.desktopMenuContributor = true;
    if (!this._detailTable.rendered) {
      this.session.desktop.addPageDetailTable(node, this._detailTable);
    }
  }
};

scout.Outline.prototype._setNodeSelected = function(node, $node) {
  scout.Outline.parent.prototype._setNodeSelected.call(this, node, $node);
  if (!node) {
    return;
  }

  this.showPageDetailTable(node);
  this.showPageDetailForm(node);
};


scout.Outline.prototype.setPageDetailFormChanged = function(nodeId, detailForm) {
  var node = this._nodeMap[nodeId];
  node.detailForm = this.session.getOrCreateModelAdapter(detailForm, this);

  if (this._selectedNodes.indexOf(node) >= 0) {
    this.showPageDetailForm(node);
  }
};


scout.Outline.prototype.onModelAction = function(event) {
  if (event.type == 'detailFormChanged') {
    this.setPageDetailFormChanged(event.nodeId, event.detailForm);
  } else {
    scout.Outline.parent.prototype.onModelAction.call(this, event);
  }
};
