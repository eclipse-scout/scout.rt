scout.Outline = function() {
  scout.Outline.parent.call(this);
  this._detailTable;
  this._detailForm;
};
scout.inherits(scout.Outline, scout.Tree);

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
  if (this._detailTable && this._detailTable !== node.table) {
    this._detailTable.remove();
    this._detailTable = null;
  }

  if (node.table) {
    this._detailTable = node.table;
    if (!this._detailTable.rendered) {
      this.session.desktop.updateOutline(this._detailTable, node.text);
    }
  }
};

/* user input handling */

scout.Outline.prototype._setNodeSelected = function(node, $node) {
  scout.Outline.parent.prototype._setNodeSelected.call(this, node, $node);

  if (node) {
    this._showForm(node);
    this._showTable(node);
  }
};

/* event handling */

scout.Outline.prototype.formChanged = function(nodeId, detailForm) {
  var node = this._nodeMap[nodeId];
  node.detailForm = this.session.getOrCreateModelAdapter(detailForm, this);

  if (this._selectedNodes.indexOf(node) >= 0) {
    this._showForm(node);
  }
};


scout.Outline.prototype.onModelAction = function(event) {
  if (event.type == 'detailFormChanged') {
    this.formChanged(event.nodeId, event.detailForm);
  } else {
    scout.Outline.parent.prototype.onModelAction.call(this, event);
  }
};
