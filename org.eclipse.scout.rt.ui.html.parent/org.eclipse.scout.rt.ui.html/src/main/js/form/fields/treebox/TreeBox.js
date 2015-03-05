// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TreeBox = function() {
  scout.TreeBox.parent.call(this);
  this._addAdapterProperties(['tree', 'filterBox']);
};
scout.inherits(scout.TreeBox, scout.ValueField);

scout.TreeBox.prototype._render = function($parent) {
  this.addContainer($parent, 'tree-box');

  this.addLabel();
  this.addMandatoryIndicator();
  this.addFieldContainer($('<div>'));
  var htmlComp = new scout.HtmlComponent(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.TreeBoxLayout(this, this.tree, this.filterBox));

  if (this.tree) {
    this._renderTree();
  }
  if (this.filterBox) {
    // TODO BSH Tree | Only render when filter active
    this._renderFilterBox();
  }

  this.addStatus();
};

scout.TreeBox.prototype._renderTree = function($fieldContainer) {
  this.tree.render(this.$fieldContainer);
  this.addField(this.tree.$container);
};

scout.TreeBox.prototype._renderFilterBox = function($fieldContainer) {
  this.filterBox.render(this.$fieldContainer);
};
