/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TreeNode = function() {
  this.$node; // FIXME [awe] 6.1 discuss with CGU... properties without assignment do not exist after ctor call
  this.attached = false;
  this.checked = false;
  this.childNodes = [];
  this.enabled = true;
  this.expanded = false;
  this.expandedLazy = false;
  this.filterAccepted = true;
  this.filterDirty;
  this.id;
  this.initialized;
  this.lazyExpandingEnabled = false;
  this.leaf = false;
  this.level;
  this.parentNode;
  this.rendered = false;
  this.text;
};

scout.TreeNode.prototype.init = function(model) {
  this._init(model);
  scout.texts.resolveTextProperty(this, 'text', this.parent.session);
};

scout.TreeNode.prototype.getTree = function() {
  return this.parent;
};

scout.TreeNode.prototype._init = function(model) {
  if (!model.parent) {
    throw new Error('missing property \'parent\'');
  }
  $.extend(this, model);
  scout.defaultValues.applyTo(this);
  // make sure all nodes are TreeNodes
  for (var i = 0; i < this.childNodes.length; i++) {
    this._ensureTreeNode(i);
  }
};

scout.TreeNode.prototype._ensureTreeNode = function(nodeIndex) {
  var node = this.childNodes[nodeIndex];
  if (node instanceof scout.TreeNode) {
    return;
  }
  if (!node.objectType) {
    node.objectType = 'TreeNode';
  }
  node.parent = this.parent;
  scout.defaultValues.applyTo(node);
  this.childNodes[nodeIndex] = scout.create(node);
};

scout.TreeNode.prototype.reset = function() {
  if (this.$node) {
    this.$node.remove();
    delete this.$node;
  }
  this.rendered = false;
  this.attached = false;
};

/**
 * Check if node is in hierarchy of a parent. is used on removal from flat list.
 */
scout.TreeNode.prototype.isChildOf = function(parentNode) {
  if (parentNode === this.parentNode) {
    return true;
  } else if (!this.parentNode) {
    return false;
  }
  return this.parentNode.isChildOf(parentNode);
};

scout.TreeNode.prototype.isFilterAccepted = function(forceFilter) {
  if (this.filterDirty || forceFilter) {
    this.getTree()._applyFiltersForNode(this);
  }
  return this.filterAccepted;
};

scout.TreeNode.prototype.loadChildren = function() {
  // NOP
};
