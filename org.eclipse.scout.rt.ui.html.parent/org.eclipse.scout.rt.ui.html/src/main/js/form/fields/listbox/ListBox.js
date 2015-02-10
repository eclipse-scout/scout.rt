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
  //TODO nbu add listener to table for rows checked
  var rows = this.table.rows,
  i = 0;
  for(i = 0; i < rows.length; i++){
    if(!rows[i].checked && this.filterCheckedRowsValue){
      rows[i].$row.hide();
    }
    else{
      rows[i].$row.show();
    }
  }
  this.table.updateScrollbar();
};

scout.ListBox.prototype._renderFilterActiveRowsValue = function(){
  //TODO nbu add listener to table for rows enabled
  var rows = this.table.rows,
  i = 0;
  for(i = 0; i < rows.length; i++){
    if(!rows[i].enabled && this.filterActiveRowsValue){
      rows[i].$row.hide();
    }
    else if(rows[i].enabled && !this.filterActiveRowsValue){
      rows[i].$row.hide();
    }
    else{
      rows[i].$row.show();
    }
  }
  this.table.updateScrollbar();
};

scout.ListBox.prototype._renderTable = function($fieldContainer) {
  this.table.render($fieldContainer);
  this.addField(this.table.$container, $fieldContainer);
};

scout.ListBox.prototype._renderFilterBox = function($fieldContainer) {
  this.filterBox.render($fieldContainer);
};
