// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TreeBox = function() {
  scout.TreeBox.parent.call(this);
  this._addAdapterProperties(['tree']);
};
scout.inherits(scout.TreeBox, scout.ValueField);

scout.TreeBox.prototype._render = function($parent) {
  this.addContainer($parent, 'tree-box');
  this.addLabel();
  this.addStatus();
  if (this.tree) {
    this._renderTree();
  }

  // TODO BSH Add "filter" boxes
  // TODO BSH Check if this files can be merge with TreeField.js
};

/**
 * Will also be called by model adapter on property change event
 */
scout.TreeBox.prototype._renderTree = function() {
  this.tree.render(this.$container);
  this.addField(this.tree.$container);
};

scout.TreeBox.prototype._removeTree = function(oldTree) {
  oldTree.remove();
  this.removeField();
};
