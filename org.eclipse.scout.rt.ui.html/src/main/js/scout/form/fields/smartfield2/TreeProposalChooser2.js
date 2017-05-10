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
  var treeNodesFlat = [];
  this.model.deleteAllChildNodes();
  lookupRows.forEach(function(lookupRow) {
    treeNodesFlat.push(this._createTreeNode(lookupRow));
  }, this);

  var treeNodes = this._flatListToTree(treeNodesFlat);
  this.model.insertNodes(treeNodes);
};

scout.TreeProposalChooser2.prototype._createTreeNode = function(lookupRow) {
  var node = {
    childNodeIndex: 0,
    htmlEnabled: false,
    iconId: lookupRow.iconId,
    id: lookupRow.key,
    parentId: scout.nvl(lookupRow.parentKey, 0),
    expanded: true,
    initialExpanded: true,
    text: lookupRow.text,
    lookupRow: lookupRow,
    leaf: true // later set to false, when child nodes are added
  };
  return node;
};

scout.TreeProposalChooser2.prototype._flatListToTree = function(treeNodesFlat) {
  // 1. put all nodes with the same parent in a map (key=parentId, value=[nodes])
  var nodesMap = {};
  treeNodesFlat.forEach(function(treeNode) {
    nodesMap[treeNode.id] = treeNode;
  });

  var rootNodes = [];

  // 2. based on this map, set the childNodes references on the treeNodes
  treeNodesFlat.forEach(function(treeNode) {
    var parentNode = nodesMap[treeNode.parentId];
    if (parentNode) {
      if (!parentNode.childNodes) {
        parentNode.childNodes = [];
      }
      treeNode.childNodeIndex = parentNode.childNodes.length;
      treeNode.parentNode = parentNode;
      parentNode.childNodes.push(treeNode);
      parentNode.leaf = false;
    } else {
      treeNode.childNodeIndex = rootNodes.length;
      treeNode.parentNode = null;
      rootNodes.push(treeNode);
    }
  });

  return rootNodes;
};
