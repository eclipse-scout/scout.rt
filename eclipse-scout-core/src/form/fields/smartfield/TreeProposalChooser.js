/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, objects, ProposalChooser, scout, Tree} from '../../../index';

export default class TreeProposalChooser extends ProposalChooser {

  constructor() {
    super();
  }

  _createModel() {
    let tree = scout.create('Tree', {
      parent: this,
      requestFocusOnNodeControlMouseDown: false,
      scrollToSelection: true,
      textFilterEnabled: false
    });
    tree.on('nodeClick', this._onNodeClick.bind(this));
    return tree;
  }

  _onNodeClick(event) {
    this.triggerLookupRowSelected(event.node);
  }

  selectedRow() {
    return this.model.selectedNode();
  }

  isBrowseLoadIncremental() {
    return this.smartField.browseLoadIncremental;
  }

  getSelectedLookupRow() {
    let selectedNode = this.model.selectedNode();
    if (!selectedNode) {
      return null;
    }
    return selectedNode.lookupRow;
  }

  selectFirstLookupRow() {
    if (this.model.nodes.length) {
      this.model.selectNode(this.model.nodes[0]);
    }
  }

  clearSelection() {
    this.model.deselectAll();
  }

  /**
   * @param {LookupResult} result
   */
  setLookupResult(result) {
    let treeNodes, treeNodesFlat,
      lookupRows = result.lookupRows,
      appendResult = scout.nvl(result.appendResult, false);

    if (appendResult) {
      treeNodesFlat = lookupRows.map(this._createTreeNode.bind(this));
      treeNodes = this._flatListToSubTree(treeNodesFlat);
      if (treeNodes.length) {
        let parentNode = null;
        treeNodes.forEach(treeNode => {
          parentNode = this.model.nodesMap[treeNode.parentId];
          this._appendChildNode(parentNode, treeNode);
        });
        if (parentNode) {
          this.model.insertNodes(treeNodes, parentNode);
        }
      } else {
        // remove control icon, when no child nodes are available
        let node = this.model.nodesMap[result.rec];
        node.leaf = true;
        node.childrenLoaded = true;
        this.model.updateNode(node);
      }
    } else {
      this.model.deleteAllChildNodes();
      treeNodesFlat = lookupRows.map(this._createTreeNode.bind(this));
      treeNodes = this._flatListToSubTree(treeNodesFlat);
      if (result.byText) {
        this._expandAllParentNodes(treeNodesFlat);
      }
      this.model.insertNodes(treeNodes);
    }

    this._selectProposal(result, treeNodesFlat);
  }

  _expandAllParentNodes(treeNodesFlat) {
    // when tree node is a leaf or children are not loaded yet
    let leafs = treeNodesFlat.reduce((aggr, treeNode) => {
      if (treeNode.leaf || !treeNode.childNodesLoaded && treeNode.childNodes.length === 0) {
        aggr.push(treeNode);
      }
      return aggr;
    }, []);
    leafs.forEach(expandPath.bind(this));

    function expandPath(treeNode) {
      if (!treeNode.parentNode || treeNode.parentNode.expanded) {
        return;
      }
      treeNode = treeNode.parentNode;
      while (treeNode) {
        this.model.setNodeExpanded(treeNode, true);
        treeNode = treeNode.parentNode;
      }
    }
  }

  trySelectCurrentValue() {
    let currentValue = this.smartField.getValueForSelection();
    if (objects.isNullOrUndefined(currentValue)) {
      return;
    }
    let allTreeNodes = objects.values(this.model.nodesMap);
    let treeNode = arrays.find(allTreeNodes, node => {
      return node.lookupRow.key === currentValue;
    });
    if (treeNode) {
      this.model.selectNode(treeNode);
    }
  }

  _createTreeNode(lookupRow) {
    let
      initialLeaf = true,
      expandAll = this.smartField.browseAutoExpandAll,
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
      enabled: lookupRow.enabled,
      htmlEnabled: false,
      iconId: lookupRow.iconId,
      id: lookupRow.key,
      parentId: lookupRow.parentKey,
      expanded: expandAll,
      initialExpanded: expandAll,
      text: lookupRow.text,
      lookupRow: lookupRow,
      leaf: initialLeaf,
      tooltipText: lookupRow.tooltipText
    });
  }

  /**
   * This function is required in the 'accept input' case to find out
   * if we have exactly one lookup row that matches. With a tree this is a bit difficult
   * because the lookup call does not only return the lookup rows with a match, but also
   * their parent nodes up to the root node (which don't match).
   *
   * Note: because we only match nodes that have the property leaf set to true, it's not
   * possible to accept a node with accept input that is not a leaf.
   *
   * @returns {TreeNode[]} the leafs in the current tree model.
   */
  findLeafs() {
    let leafs = [];
    Tree.visitNodes((node, parentNode) => {
      if (node.leaf || !node.childNodes.length) {
        leafs.push(node);
      }
    }, this.model.nodes);
    return leafs;
  }

  /**
   * This function creates a sub-tree from a list of flat tree nodes. It sets the parent/child references
   * between the nodes and returns the top-level nodes of the sub-tree. This subtree is not yet attached
   * to the real tree (= this.model).
   */
  _flatListToSubTree(treeNodesFlat) {
    // 1. put all nodes with the same parent in a map (key=parentId, value=[nodes])
    let nodesMap = {};
    treeNodesFlat.forEach(treeNode => {
      if (!objects.isNullOrUndefined(treeNode.id)) {
        nodesMap[treeNode.id] = treeNode;
      }
    });

    let rootNodes = [];

    // 2. based on this map, set the childNodes references on the treeNodes
    treeNodesFlat.forEach(treeNode => {
      let parentNode = nodesMap[treeNode.parentId];
      if (parentNode === treeNode) {
        throw new Error('Cannot link a node to itself. Id: ' + treeNode.parentId);
      }
      if (parentNode) {
        this._appendChildNode(parentNode, treeNode);
      } else {
        treeNode.childNodeIndex = rootNodes.length;
        treeNode.parentNode = null;
        rootNodes.push(treeNode);
      }
    });

    return rootNodes;
  }

  /**
   * This functions appends a tree node to a parent node and sets the required flags on the parent node.
   */
  _appendChildNode(parentNode, treeNode) {
    if (!parentNode.childNodes) {
      parentNode.childNodes = [];
    }
    treeNode.childNodeIndex = parentNode.childNodes.length;
    treeNode.parentNode = parentNode;
    parentNode.childNodes.push(treeNode);
    parentNode.leaf = false;
    parentNode.childrenLoaded = true;
  }

  clearLookupRows() {
    this.model.deleteAllNodes();
  }
}
