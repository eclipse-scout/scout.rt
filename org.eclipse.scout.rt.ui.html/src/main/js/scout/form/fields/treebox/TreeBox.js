/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TreeBox = function() {
  scout.TreeBox.parent.call(this);
  this.tree = null;
  this._addWidgetProperties(['tree', 'filterBox']);
};
scout.inherits(scout.TreeBox, scout.LookupBox);

scout.TreeBox.prototype._init = function(model) {
  scout.TreeBox.parent.prototype._init.call(this, model);
  this.tree.on('nodesChecked', this._onTreeNodesChecked.bind(this));
  this.tree.setScrollTop(this.scrollTop);
};

scout.TreeBox.prototype._initStructure = function(value) {
  if (!this.tree) {
    this.tree = this._createDefaultTreeBoxTree();
  }
};

scout.TreeBox.prototype._initValue = function(value) {
  if (!this.tree) {
    this.tree = this._createDefaultTreeBoxTree();
  }
  scout.TreeBox.parent.prototype._initValue.call(this, value);
};

scout.TreeBox.prototype._render = function() {
  scout.TreeBox.parent.prototype._render.call(this);
  this.$container.addClass('tree-box');
};

scout.TreeBox.prototype._createLayout = function() {
  return new scout.TreeBoxLayout(this, this.tree, this.filterBox);
};

scout.TreeBox.prototype._renderStructure = function($fieldContainer) {
  this.tree.render(this.$fieldContainer);
  this.tree.htmlComp.pixelBasedSizing = true;
  this.addField(this.tree.$container);
};

scout.TreeBox.prototype._onTreeNodesChecked = function(event) {
  this._syncTreeToValue();
};

scout.TreeBox.prototype._syncTreeToValue = function() {
  if (!this.lookupCall || this._valueSyncing) {
    return;
  }
  this._valueSyncing = true;
  var valueArray = scout.objects.values(this.tree.nodesMap).filter(function(node) {
    return node.checked;
  }).map(function(node) {
    return node.id;
  });

  this.setValue(valueArray);
  this._valueSyncing = false;
};

scout.TreeBox.prototype._valueChanged = function() {
  scout.TreeBox.parent.prototype._valueChanged.call(this);
  this._syncValueToTree(this.value);
};

scout.TreeBox.prototype._syncValueToTree = function(newValue) {
  if (!this.lookupCall || this._valueSyncing || !this.initialized) {
    return;
  }

  this._valueSyncing = true;
  var opts = {
    checkOnlyEnabled: false
  };
  try {
    if (scout.arrays.empty(newValue)) {
      this.uncheckAll(opts);
    } else {
      // if lookup was not executed yet: do it now.
      var lookupScheduled = this._ensureLookupCallExecuted();
      if (lookupScheduled) {
        return; // was the first lookup: tree has no nodes yet. cancel sync. Will be executed again after lookup execution.
      }

      this.uncheckAll(opts);
      scout.objects.values(this.tree.nodesMap).forEach(function(node) {
        if (scout.arrays.containsAny(newValue, node.id)) {
          this.tree.checkNode(node, true, opts);
        }
      }, this);
    }

    this._updateDisplayText();
  } finally {
    this._valueSyncing = false;
  }
};

scout.TreeBox.prototype.uncheckAll = function(options) {
  for (var nodeId in this.tree.nodesMap) {
    if (this.tree.nodesMap.hasOwnProperty(nodeId)) {
      this.tree.uncheckNode(this.tree.nodesMap[nodeId], options);
    }
  }
};

scout.TreeBox.prototype._lookupByAllDone = function(result) {
  if (scout.TreeBox.parent.prototype._lookupByAllDone.call(this, result)) {
    this._populateTree(result);
  }
};

scout.TreeBox.prototype._populateTree = function(result) {
  var topLevelNodes = [];

  this._poulateTreeRecursive(null, topLevelNodes, result.lookupRows);

  this.tree.deleteAllNodes();
  this.tree.insertNodes(topLevelNodes);

  this._syncValueToTree(this.value);
};

scout.TreeBox.prototype._poulateTreeRecursive = function(parentKey, nodesArray, lookupRows) {
  var node;
  lookupRows.forEach(function(lookupRow) {
    if (lookupRow.parentKey === parentKey) {
      node = this._createNode(lookupRow);
      this._poulateTreeRecursive(node.id, node.childNodes, lookupRows);
      node.leaf = !node.childNodes.length;
      nodesArray.push(node);
    }
  }, this);
};

/**
 * Returns a lookup row for each node currently checked.
 */
scout.TreeBox.prototype.getCheckedLookupRows = function() {
  if (this.value === null || scout.arrays.empty(this.value) || this.tree.nodes.length === 0) {
    return [];
  }

  return scout.objects.values(this.tree.nodesMap).filter(function(node) {
    return node.checked;
  }).map(function(node) {
    return node.lookupRow;
  });
};

scout.TreeBox.prototype._createNode = function(lookupRow) {
  var
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
  if (lookupRow.active === false) {
    node.active = false;
  }
  if (lookupRow.cssClass) {
    node.cssClass = lookupRow.cssClass;
  }

  return node;
};

scout.TreeBox.prototype._createDefaultTreeBoxTree = function() {
  return scout.create('Tree', {
    parent: this,
    checkable: true
  });
};

/**
 * @override
 */
scout.TreeBox.prototype.getDelegateScrollable = function() {
  return this.tree;
};
