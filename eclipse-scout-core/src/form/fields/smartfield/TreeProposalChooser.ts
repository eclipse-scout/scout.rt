/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, arrays, LookupRow, objects, ProposalChooser, ProposalTreeNode, scout, SmartFieldLookupResult, Tree, TreeLayoutResetter, TreeNode, TreeNodeClickEvent, TreeNodesSelectedEvent} from '../../../index';

export class TreeProposalChooser<TValue> extends ProposalChooser<TValue, Tree, ProposalTreeNode<TValue>> {

  protected override _createContent(): Tree {
    let tree = scout.create(Tree, {
      parent: this,
      requestFocusOnNodeControlMouseDown: false,
      scrollToSelection: true,
      textFilterEnabled: false
    });
    tree.on('nodeClick', this._onNodeClick.bind(this));
    tree.on('nodesSelected', this._onNodeSelected.bind(this));
    return tree;
  }

  protected override _postRender() {
    super._postRender();
    let node = this.content.selectedNode();
    this._renderSelectedNode(node);
    aria.hasPopup(this.smartField.$field, 'tree');
  }

  protected _onNodeSelected(event: TreeNodesSelectedEvent) {
    let node = this.content.selectedNode();
    this._renderSelectedNode(node);
  }

  protected _renderSelectedNode(node: TreeNode) {
    if (node && node.$node) {
      aria.linkElementWithActiveDescendant(this.smartField.$field, node.$node);
    } else {
      aria.removeActiveDescendant(this.smartField.$field);
    }
  }

  protected override _createLayoutResetter(): TreeLayoutResetter {
    return scout.create(TreeLayoutResetter, this.content);
  }

  protected _onNodeClick(event: TreeNodeClickEvent) {
    this.triggerLookupRowSelected(event.node as ProposalTreeNode<TValue>);
  }

  override selectedRow(): ProposalTreeNode<TValue> {
    return this.content.selectedNode() as ProposalTreeNode<TValue>;
  }

  isBrowseLoadIncremental(): boolean {
    return this.smartField.browseLoadIncremental;
  }

  override getSelectedLookupRow(): LookupRow<TValue> {
    let selectedNode = this.content.selectedNode() as ProposalTreeNode<TValue>;
    if (!selectedNode) {
      return null;
    }
    return selectedNode.lookupRow;
  }

  override selectFirstLookupRow() {
    if (this.content.nodes.length) {
      this.content.selectNode(this.content.nodes[0]);
    }
  }

  override clearSelection() {
    this.content.deselectAll();
  }

  override setLookupResult(result: SmartFieldLookupResult<TValue>) {
    let treeNodes, treeNodesFlat,
      lookupRows = result.lookupRows,
      appendResult = scout.nvl(result.appendResult, false);

    if (appendResult) {
      treeNodesFlat = this._lookupRowsToFlatList(lookupRows);
      treeNodes = this._flatListToSubTree(treeNodesFlat);
      if (treeNodes.length) {
        let parentNode = null;
        treeNodes.forEach(treeNode => {
          parentNode = this.content.nodesMap[treeNode.parentId];
          this._appendChildNode(parentNode, treeNode);
        });
        if (parentNode) {
          this.content.insertNodes(treeNodes, parentNode);
        }
      } else {
        // remove control icon, when no child nodes are available
        let node = this.content.nodesMap[this._createNodeId(result.rec)];
        node.leaf = true;
        node.childrenLoaded = true;
        this.content.updateNode(node);
      }
    } else {
      this.content.deleteAllChildNodes();
      treeNodesFlat = this._lookupRowsToFlatList(lookupRows);
      treeNodes = this._flatListToSubTree(treeNodesFlat);
      if (result.byText) {
        this._expandAllParentNodes(treeNodesFlat);
      }
      this.content.insertNodes(treeNodes);
    }

    this._selectProposal(result, treeNodesFlat);
  }

  protected _expandAllParentNodes(treeNodesFlat: TreeNode[]) {
    // when tree node is a leaf or children are not loaded yet
    let leafs = treeNodesFlat.reduce((aggr, treeNode) => {
      if (treeNode.leaf || !treeNode.childrenLoaded && treeNode.childNodes.length === 0) {
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
        this.content.setNodeExpanded(treeNode, true);
        treeNode = treeNode.parentNode;
      }
    }
  }

  override trySelectCurrentValue() {
    let currentValue = this.smartField.getValueForSelection();
    if (objects.isNullOrUndefined(currentValue)) {
      return;
    }
    let allTreeNodes = objects.values(this.content.nodesMap) as ProposalTreeNode<TValue>[];
    let treeNode = arrays.find(allTreeNodes, node => {
      return node.lookupRow.key === currentValue;
    });
    if (treeNode) {
      this.content.selectNode(treeNode);
    }
  }

  protected _createTreeNode(lookupRow: LookupRow<TValue>): ProposalTreeNode<TValue> {
    let
      initialLeaf = true,
      expandAll = this.smartField.browseAutoExpandAll,
      loadIncremental = this.isBrowseLoadIncremental();

    if (loadIncremental) {
      // when smartfield / lookup is configured as 'load incremental' it cannot expand all tree nodes
      // because then we'd load the whole tree anyway, which is not the idea of load incremental
      expandAll = false;

      // when smartfield / lookup is configured as 'load incremental' we don't know if a node has children
      // or not until we've made a lookup for that node. Thus, all nodes are initially leaf=false, so the UI
      // shows the expand icon.
      initialLeaf = false;
    }

    return scout.create(ProposalTreeNode, {
      parent: this.content,
      proposalChooser: this,
      enabled: lookupRow.enabled,
      htmlEnabled: false,
      iconId: lookupRow.iconId,
      id: this._createNodeId(lookupRow.key),
      parentId: this._createNodeId(lookupRow.parentKey),
      expanded: expandAll,
      initialExpanded: expandAll,
      text: lookupRow.text,
      lookupRow: lookupRow,
      leaf: initialLeaf,
      tooltipText: lookupRow.tooltipText
    }) as ProposalTreeNode<TValue>;
  }

  protected _createNodeId(value: TValue): string {
    if (objects.isNullOrUndefined(value)) {
      return value;
    }
    if (typeof value === 'string') {
      return value;
    }
    return JSON.stringify(value);
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
   * @returns the leafs in the current tree content.
   */
  findLeafs(): ProposalTreeNode<TValue>[] {
    let leafs = [];
    Tree.visitNodes((node, parentNode) => {
      if (node.leaf || !node.childNodes.length) {
        leafs.push(node);
      }
    }, this.content.nodes);
    return leafs;
  }

  /**
   * This function creates a list of flat tree nodes from a list of lookup rows.
   * Nodes with duplicate ids are filtered, only the first node with the same id is kept.
   */
  protected _lookupRowsToFlatList(lookupRows: LookupRow<TValue>[]): ProposalTreeNode<TValue>[] {
    let nodeIds = new Set();
    return lookupRows.map(this._createTreeNode.bind(this))
      .filter(node => {
        return nodeIds.has(node.id) ? false : nodeIds.add(node.id);
      });
  }

  /**
   * This function creates a subtree from a list of flat tree nodes. It sets the parent/child references
   * between the nodes and returns the top-level nodes of the subtree. This subtree is not yet attached
   * to the real tree (= this.content).
   */
  protected _flatListToSubTree(treeNodesFlat: ProposalTreeNode<TValue>[]): ProposalTreeNode<TValue>[] {
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
        treeNode.parentNode = null;
        rootNodes.push(treeNode);
      }
    });

    return rootNodes;
  }

  /**
   * This functions appends a tree node to a parent node and sets the required flags on the parent node.
   */
  protected _appendChildNode(parentNode: TreeNode, treeNode: TreeNode) {
    if (!parentNode.childNodes) {
      parentNode.childNodes = [];
    }
    treeNode.parentNode = parentNode;
    parentNode.childNodes.push(treeNode);
    parentNode.leaf = false;
    parentNode.childrenLoaded = true;
  }

  override clearLookupRows() {
    this.content.deleteAllNodes();
  }
}
