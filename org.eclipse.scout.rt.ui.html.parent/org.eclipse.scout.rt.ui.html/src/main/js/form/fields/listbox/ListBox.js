// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ListBox = function() {
  scout.ListBox.parent.call(this);
  this._addAdapterProperties(['table']);
};
scout.inherits(scout.ListBox, scout.ValueField);

scout.ListBox.prototype._render = function($parent) {
  this.addContainer($parent, 'list-box');
  this.addLabel();
  this.addMandatoryIndicator();
  if (this.table) {
    this._renderTable();
  }

  this.addStatus();
};

scout.ListBox.prototype._renderTable = function() {
  this.table.render(this.$container);
  this.addField(this.table.$container);
};
