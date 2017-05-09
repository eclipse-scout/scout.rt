scout.TreeProposalChooser2 = function() {
  scout.TreeProposalChooser2.parent.call(this);
};
scout.inherits(scout.TreeProposalChooser2, scout.ProposalChooser2);

scout.TreeProposalChooser2.prototype._createModel = function() {

  var tree = scout.create('Tree', {
    parent: this
  });

  tree.on('nodeClicked', this._triggerLookupRowSelected.bind(this));

  return tree;
};

scout.TreeProposalChooser2.prototype._triggerLookupRowSelected = function(event) {
  if (!event.node.enabled) {
    return;
  }
  this.trigger('lookupRowSelected', {
    lookupRow: this.getSelectedLookupRow()
  });
};

scout.TreeProposalChooser2.prototype.getSelectedLookupRow = function() {
  var selectedNode = this.model.selectedNode();
  if (!selectedNode) {
    return null;
  }
  return selectedNode.lookupRow;
};

scout.TreeProposalChooser2.prototype.setLookupRows = function(lookupRows) {
  var treeNodes = [];

  this.model.deleteAllChildNodes();
  lookupRows.forEach(function(lookupRow) {
    treeNodes.push(this._createTreeNode(lookupRow));
  }, this);
  this.model.insertNodes(treeNodes);
};

scout.TreeProposalChooser2.prototype._createTreeNode = function(lookupRow) {
  var node = {
    childNodeIndex: 0,
    htmlEnabled: false,
    iconId: 'icon/form.png',
    id: lookupRow.key,
    initialExpanded: false,
    leaf: true,
    text: lookupRow.text,
    lookupRow: lookupRow
  };
  return node;
};
