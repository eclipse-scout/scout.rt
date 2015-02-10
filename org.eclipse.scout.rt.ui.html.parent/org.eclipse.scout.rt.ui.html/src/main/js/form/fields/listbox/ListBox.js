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

  var $fieldContainer = $('<div>');
  var htmlComp = new scout.HtmlComponent($fieldContainer, this.session);
  htmlComp.setLayout(new scout.ListBoxLayout(this, this.table, this.filterBox));

  if (this.table) {
    this._renderTable($fieldContainer);
  }
  if(this.filterBox){
    this._renderFilterBox($fieldContainer);
  }

  this.addStatus();
};

scout.ListBox.prototype._renderFilterCheckedRowsValue = function(){
  //TODO nbu
};

scout.ListBox.prototype._renderFilterActiveRowsValue = function(){
  //TODO nbu
};

scout.ListBox.prototype._renderTable = function($fieldContainer) {
  this.table.render($fieldContainer);
  this.addField(this.table.$container, $fieldContainer);
};

scout.ListBox.prototype._renderFilterBox = function($fieldContainer) {
  this.filterBox.render($fieldContainer);
};
