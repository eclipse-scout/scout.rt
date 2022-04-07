/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, LookupBox, objects, scout, TreeBoxLayout} from '../../../index';

export default class TreeBox extends LookupBox {

  constructor() {
    super();
    this.tree = null;
    this._populating = false;
    this._addWidgetProperties(['tree', 'filterBox']);
  }

  _init(model) {
    super._init(model);
    this.tree.on('nodesChecked', this._onTreeNodesChecked.bind(this));
    this.tree.setScrollTop(this.scrollTop);
  }

  _initStructure(value) {
    if (!this.tree) {
      this.tree = this._createDefaultTreeBoxTree();
    }
  }

  _initValue(value) {
    if (!this.tree) {
      this.tree = this._createDefaultTreeBoxTree();
    }
    super._initValue(value);
  }

  _render() {
    super._render();
    this.$container.addClass('tree-box');
  }

  _createFieldContainerLayout() {
    return new TreeBoxLayout(this, this.tree, this.filterBox);
  }

  _renderStructure($fieldContainer) {
    this.tree.render(this.$fieldContainer);
    this.addField(this.tree.$container);
  }

  _onTreeNodesChecked(event) {
    if (this._populating) {
      return;
    }
    this._syncTreeToValue();
  }

  _syncTreeToValue() {
    if (!this.lookupCall || this._valueSyncing) {
      return;
    }
    this._valueSyncing = true;
    let valueArray = objects.values(this.tree.nodesMap).filter(node => {
      return node.checked;
    }).map(node => {
      return node.id;
    });

    this.setValue(valueArray);
    this._valueSyncing = false;
  }

  _valueChanged() {
    super._valueChanged();
    this._syncValueToTree(this.value);
  }

  _syncValueToTree(newValue) {
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
        objects.values(this.tree.nodesMap).forEach(function(node) {
          if (arrays.containsAny(newValue, node.id)) {
            this.tree.checkNode(node, true, opts);
          }
        }, this);
      }

      this._updateDisplayText();
    } finally {
      this._valueSyncing = false;
    }
  }

  uncheckAll(options) {
    for (let nodeId in this.tree.nodesMap) {
      if (this.tree.nodesMap.hasOwnProperty(nodeId)) {
        this.tree.uncheckNode(this.tree.nodesMap[nodeId], options);
      }
    }
  }

  _lookupByAllDone(result) {
    if (super._lookupByAllDone(result)) {
      this._populateTree(result);
    }
  }

  _populateTree(result) {
    let topLevelNodes = [];
    this._populating = true;
    this._populateTreeRecursive(null, topLevelNodes, result.lookupRows);

    this.tree.deleteAllNodes();
    this.tree.insertNodes(topLevelNodes);
    this._populating = false;

    this._syncValueToTree(this.value);
  }

  _populateTreeRecursive(parentKey, nodesArray, lookupRows) {
    let node;
    lookupRows.forEach(function(lookupRow) {
      if (lookupRow.parentKey === parentKey) {
        node = this._createNode(lookupRow);
        this._populateTreeRecursive(node.id, node.childNodes, lookupRows);
        node.leaf = !node.childNodes.length;
        nodesArray.push(node);
      }
    }, this);
  }

  /**
   * Returns a lookup row for each node currently checked.
   */
  getCheckedLookupRows() {
    if (this.value === null || arrays.empty(this.value) || this.tree.nodes.length === 0) {
      return [];
    }

    return objects.values(this.tree.nodesMap).filter(node => {
      return node.checked;
    }).map(node => {
      return node.lookupRow;
    });
  }

  _createNode(lookupRow) {
    let
      node = scout.create('TreeNode', {
        parent: this.tree,
        id: lookupRow.key,
        text: lookupRow.text,
        lookupRow: lookupRow
      });

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

  _createDefaultTreeBoxTree() {
    return scout.create('Tree', {
      parent: this,
      checkable: true
    });
  }

  /**
   * @override
   */
  getDelegateScrollable() {
    return this.tree;
  }
}
