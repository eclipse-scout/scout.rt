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
  this.id;
  this.initialized;
  this.checked;
  this.text;
  this.rendered;
  this.attached;
  this.$node;
  this.parentNode;
  this.level;
  this.filterDirty;
  this.expanded = false;
  this.childNodes = [];
};

scout.TreeNode.prototype.init = function(model) {
  this._init(model);
};

scout.TreeNode.prototype._init = function(model) {
  if (!model.tree) {
    throw new Error('missing property \'tree\'');
  }
  $.extend(this, model);
  scout.defaultValues.applyTo(this);
};

scout.TreeNode.prototype.reset = function() {
  this.rendered = false;
  this.attached = false;
  delete this.$node;
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
    this.tree._applyFiltersForNode(this);
  }
  return this.filterAccepted;
};

scout.TreeNode.prototype.loadChildren = function() {
  // NOP
};
