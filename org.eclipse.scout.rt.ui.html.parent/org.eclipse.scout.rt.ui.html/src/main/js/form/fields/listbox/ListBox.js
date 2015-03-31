// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ListBox = function() {
  scout.ListBox.parent.call(this);
  this._addAdapterProperties(['table', 'filterBox']);
};
scout.inherits(scout.ListBox, scout.ValueField);

scout.ListBox.prototype._render = function($parent) {
  this.addContainer($parent, 'list-box');

  this.addLabel();
  this.addMandatoryIndicator();
  this.addFieldContainer($('<div>'));

  var htmlComp = new scout.HtmlComponent(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.ListBoxLayout(this, this.table, this.filterBox));

  if (this.table) {
    this._renderTable();
  }
  if (this.filterBox) {
    this._renderFilterBox();
  }

  this.addStatus();
};

scout.ListBox.prototype._renderTable = function() {
  this.table.render(this.$fieldContainer);
  this.addField(this.table.$container);
};

scout.ListBox.prototype._renderFilterBox = function() {
  this.filterBox.render(this.$fieldContainer);
};
