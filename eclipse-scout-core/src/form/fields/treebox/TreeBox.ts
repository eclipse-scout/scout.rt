/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, InitModelOf, LookupBox, LookupResult, LookupRow, objects, scout, Tree, TreeBoxLayout, TreeBoxModel, TreeNode, TreeNodesCheckedEvent, TreeNodeUncheckOptions, Widget} from '../../../index';

export class TreeBox<TValue> extends LookupBox<TValue> implements TreeBoxModel<TValue> {
  tree: Tree;
  protected _populating: boolean;

  constructor() {
    super();
    this.tree = null;
    this._populating = false;
    this._addWidgetProperties(['tree', 'filterBox']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.tree.on('nodesChecked', this._onTreeNodesChecked.bind(this));
    this.tree.setScrollTop(this.scrollTop);
  }

  protected _initStructure(value: TValue[]) {
    if (!this.tree) {
      this.tree = this._createDefaultTreeBoxTree();
    }
  }

  protected override _initValue(value: TValue[]) {
    if (!this.tree) {
      this.tree = this._createDefaultTreeBoxTree();
    }
    super._initValue(value);
  }

  protected override _render() {
    super._render();
    this.$container.addClass('tree-box');
  }

  protected _createFieldContainerLayout(): TreeBoxLayout {
    return new TreeBoxLayout(this, this.tree, this.filterBox);
  }

  protected _renderStructure() {
    this.tree.render(this.$fieldContainer);
    this.addField(this.tree.$container);
  }

  protected _onTreeNodesChecked(event: TreeNodesCheckedEvent) {
    if (this._populating) {
      return;
    }
    this._syncTreeToValue();
  }

  protected _syncTreeToValue() {
    if (!this.lookupCall || this._valueSyncing) {
      return;
    }
    this._valueSyncing = true;
    let valueArray = objects.values(this.tree.nodesMap)
      .filter(node => node.checked)
      .map((node: TreeBoxTreeNode<TValue>) => node.lookupRow.key);

    this.setValue(valueArray);
    this._valueSyncing = false;
  }

  protected override _valueChanged() {
    super._valueChanged();
    this._syncValueToTree(this.value);
  }

  protected _syncValueToTree(newValue: TValue[]) {
    if (!this.lookupCall || this._valueSyncing || !this.initialized) {
      return;
    }

    this._valueSyncing = true;
    let opts = {
      checkOnlyEnabled: false,
      checkChildren: false
    };
    try {
      if (arrays.empty(newValue)) {
        this.uncheckAll(opts);
      } else {
        // if lookup was not executed yet: do it now.
        let lookupScheduled = this._ensureLookupCallExecuted();
        if (lookupScheduled) {
          return; // was the first lookup: tree has no nodes yet. cancel sync. Will be executed again after lookup execution.
        }

        this.uncheckAll(opts);
        objects.values(this.tree.nodesMap).forEach((node: TreeBoxTreeNode<TValue>) => {
          if (arrays.contains(newValue, node.lookupRow.key)) {
            this.tree.checkNode(node, true, opts);
          }
        }, this);
      }

      this._updateDisplayText();
    } finally {
      this._valueSyncing = false;
    }
  }

  uncheckAll(options: TreeNodeUncheckOptions) {
    for (let nodeId in this.tree.nodesMap) {
      if (this.tree.nodesMap.hasOwnProperty(nodeId)) {
        this.tree.uncheckNode(this.tree.nodesMap[nodeId], options);
      }
    }
  }

  protected override _lookupByAllDone(result: LookupResult<TValue>): boolean {
    if (super._lookupByAllDone(result)) {
      this._populateTree(result);
      return true;
    }
    return false;
  }

  protected _populateTree(result: LookupResult<TValue>) {
    let topLevelNodes = [];
    this._populating = true;
    this._populateTreeRecursive(null, topLevelNodes, result.lookupRows);

    this.tree.deleteAllNodes();
    this.tree.insertNodes(topLevelNodes);
    this._populating = false;

    this._syncValueToTree(this.value);
  }

  protected _populateTreeRecursive(parentKey: TValue, nodesArray: TreeNode[], lookupRows: LookupRow<TValue>[]) {
    let node: TreeBoxTreeNode<TValue>;
    lookupRows.forEach(function(lookupRow) {
      if (lookupRow.parentKey === parentKey) {
        node = this._createNode(lookupRow);
        this._populateTreeRecursive(node.lookupRow.key, node.childNodes, lookupRows);
        node.leaf = !node.childNodes.length;
        nodesArray.push(node);
      }
    }, this);
  }

  /**
   * Returns a lookup row for each node currently checked.
   */
  getCheckedLookupRows(): LookupRow<TValue>[] {
    if (this.value === null || arrays.empty(this.value) || this.tree.nodes.length === 0) {
      return [];
    }

    return objects.values(this.tree.nodesMap)
      .filter(node => node.checked)
      .map((node: TreeBoxTreeNode<TValue>) => node.lookupRow);
  }

  protected _createNode(lookupRow: LookupRow<TValue>): TreeBoxTreeNode<TValue> {
    let node = scout.create(TreeNode, {
      parent: this.tree,
      text: lookupRow.text,
      lookupRow: lookupRow
    }) as TreeBoxTreeNode<TValue>;

    if (lookupRow.iconId) {
      node.iconId = lookupRow.iconId;
    }
    if (lookupRow.tooltipText) {
      node.tooltipText = lookupRow.tooltipText;
    }
    if (lookupRow.backgroundColor) {
      node.backgroundColor = lookupRow.backgroundColor;
    }
    if (lookupRow.foregroundColor) {
      node.foregroundColor = lookupRow.foregroundColor;
    }
    if (lookupRow.font) {
      node.font = lookupRow.font;
    }
    if (lookupRow.enabled === false) {
      node.enabled = false;
    }
    if (lookupRow.cssClass) {
      node.cssClass = lookupRow.cssClass;
    }
    if (lookupRow.active === false) {
      node.active = false;
      node.cssClass = (node.cssClass ? (node.cssClass + ' ') : '') + 'inactive';
    }

    return node;
  }

  protected _createDefaultTreeBoxTree(): Tree {
    return scout.create(Tree, {
      parent: this,
      checkable: true
    });
  }

  override getDelegateScrollable(): Widget {
    return this.tree;
  }
}

export type TreeBoxTreeNode<TValue> = TreeNode & {
  lookupRow: LookupRow<TValue>;
  active?: boolean;
};
