scout.MobileTableConfigurator = function(table) {
  this.table = table;
};

scout.MobileTableConfigurator.prototype.configure = function() {
  this.selectionHandler = new scout.TableSelectionHandler(this.table);
  this.selectionHandler.mouseMoveSelectionEnabled = false;
};

scout.MobileTableConfigurator.prototype.render = function() {
  if (!this.table.$data.hasClass('scrollable-y')) {
    this.table.$data.addClass('scrollable-y');
  }
  //no need for an additional div, but we need to keep $dataScroll because it is used widely
  this.table.$dataScroll = this.table.$data;
};
