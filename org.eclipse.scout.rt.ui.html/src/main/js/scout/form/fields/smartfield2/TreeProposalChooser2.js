/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TreeProposalChooser2 = function() {
  scout.TreeProposalChooser2.parent.call(this);
};
scout.inherits(scout.TreeProposalChooser2, scout.ProposalChooser2);

scout.TreeProposalChooser2.prototype._createModel = function() {
  var tree = scout.create('Tree', {
    parent: this
  });
  tree.on('nodeClicked', this._onNodeClicked.bind(this));
  return tree;
};

scout.TreeProposalChooser2.prototype._onNodeClicked = function(event) {
  this.triggerLookupRowSelected(event.node);
};

scout.TreeProposalChooser2.prototype.triggerLookupRowSelected = function(node) {
  node = node || this.model.selectedNode();
  if (!node.enabled) {
    return;
  }
  this.trigger('lookupRowSelected', {
    lookupRow: node.lookupRow
  });
};

scout.TreeProposalChooser2.prototype.isBrowseLoadIncremental = function() {
  return this._smartField().browseLoadIncremental;
};

scout.TreeProposalChooser2.prototype.getSelectedLookupRow = function() {
  var selectedNode = this.model.selectedNode();
  if (!selectedNode) {
    return null;
  }
  return selectedNode.lookupRow;
};

scout.TreeProposalChooser2.prototype.selectFirstLookupRow = function() {
  if (this.model.nodes.length) {
    this.model.selectNode(this.model.nodes[0]);
  }
};

/**
 * @param {scout.LookupRow[]} lookupRows
 * @param {boolean} appendResult whether or not we must delete the tree
 */
scout.TreeProposalChooser2.prototype.setLookupRows = function(lookupRows, appendResult) {
  var treeNodes, treeNodesFlat;

  if (appendResult) {
    treeNodesFlat = lookupRows.map(this._createTreeNode.bind(this));
    treeNodes = this._flatListToSubTree(treeNodesFlat);
    if (treeNodes.length) {
      var parentNode = null; // FIXME [awe] 7.0 - SF2: better return parentKey with result
      treeNodes.forEach(function(treeNode) {
        parentNode = this.model.nodesMap[treeNode.parentId];
        this._appendChildNode(parentNode, treeNode);
      }.bind(this));
      if (parentNode) {
        this.model.insertNodes(treeNodes, parentNode);
      }
    }
  } else {
    this.model.deleteAllChildNodes();
    treeNodesFlat = lookupRows.map(this._createTreeNode.bind(this));
    treeNodes = this._flatListToSubTree(treeNodesFlat);
    this.model.insertNodes(treeNodes);
  }
};

scout.TreeProposalChooser2.prototype._createTreeNode = function(lookupRow) {
  var
    initialLeaf = true,
    expandAll = this._smartField().browseAutoExpandAll,
    loadIncremental = this.isBrowseLoadIncremental();

  if (loadIncremental) {
    // when smartfield / lookup is configured as 'load incremental' it cannot expand all tree nodes
    // because then we'd load the whole tree anyway, which is not the idea of load incremental
    expandAll = false;

    // when smartfield / lookup is configured as 'load incremental' we don't know if a node has children
    // or not until we've made a lookup for that node. Thus all nodes are initially leaf=false, so the UI
    // shows the expand icon.
    initialLeaf = false;
  }

  return scout.create('ProposalTreeNode', {
    parent: this.model,
    proposalChooser: this,
    childNodeIndex: 0,
    htmlEnabled: false,
    iconId: lookupRow.iconId,
    id: lookupRow.key,
    parentId: lookupRow.parentKey,
    expanded: expandAll,
    initialExpanded: expandAll,
    text: lookupRow.text,
    lookupRow: lookupRow,
    leaf: initialLeaf
  });
};

/**
 * This function creates a sub-tree from a list of flat tree nodes. It sets the parent/child references
 * between the nodes and returns the top-level nodes of the sub-tree. This subtree is not yet attached
 * to the real tree (= this.model).
 */
scout.TreeProposalChooser2.prototype._flatListToSubTree = function(treeNodesFlat) {
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
      this._appendChildNode(parentNode, treeNode);
    } else {
      treeNode.childNodeIndex = rootNodes.length;
      treeNode.parentNode = null;
      rootNodes.push(treeNode);
    }
  }.bind(this));

  return rootNodes;
};

/**
 * This functions appends a tree node to a parent node and sets the required flags on the parent node.
 */
scout.TreeProposalChooser2.prototype._appendChildNode = function(parentNode, treeNode) {
  if (!parentNode.childNodes) {
    parentNode.childNodes = [];
  }
  treeNode.childNodeIndex = parentNode.childNodes.length;
  treeNode.parentNode = parentNode;
  parentNode.childNodes.push(treeNode);
  parentNode.leaf = false;
  parentNode.childrenLoaded = true;
};
