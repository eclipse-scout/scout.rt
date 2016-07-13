scout.TreeNode = function(tree) {
  if (!tree) {
    throw new Error('missing argument tree');
  }

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

  this.tree = tree;
};

scout.TreeNode.prototype.init = function() {
  this._init();
};

scout.TreeNode.prototype._init = function() {
  this.reset();
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
