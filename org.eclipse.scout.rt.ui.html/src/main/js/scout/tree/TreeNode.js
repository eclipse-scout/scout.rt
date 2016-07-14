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
  this._resolveTextKeys();
};

scout.TreeNode.prototype._resolveTextKeys = function() {
  this.text = scout.textProperties.resolveTextKeys(this.text);
};

scout.TreeNode.prototype._init = function(model) {
  // FIXME [awe] 6.1 ... discuss: wenn wir TreeNode/Pages aus model.json bauen, dann wird nur die property parent "vererbt"
  // nicht jedoch andere properties wie z.B. tree. Schauen wie wir das machen wollen. Entweder über eine zu implementierende Methode
  // setParent() auf unseren Objects, oder durch ändern von TreeNode#tree auf TreeNode#parent.
  if (!model.tree && model.parent) {
    model.tree = model.parent;
  }
  if (!model.tree) {
    throw new Error('missing property \'tree\'');
  }
  $.extend(this, model);
  scout.defaultValues.applyTo(this);

  var i, childNode;
  for (i = 0; i < this.childNodes.length; i++) {
    childNode = this.childNodes[i];
    if (childNode instanceof scout.TreeNode) {
      continue;
    }
    childNode.tree = this.tree;
    this.childNodes[i] = scout.create(childNode);
  }
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
