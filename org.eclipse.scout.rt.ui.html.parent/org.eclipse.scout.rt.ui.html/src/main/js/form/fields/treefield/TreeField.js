// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TreeField = function() {
  scout.TreeField.parent.call(this);
  this._addAdapterProperties(['tree']);
};
scout.inherits(scout.TreeField, scout.FormField);

scout.TreeField.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('', 'tree-field');
  this.$label = $('<label>')
    .appendTo(this.$container);

  if (this.tree) {
    this.tree.render(this.$container);
  }
};
