scout.TableConfigurator = function(table) {
  this.table = table;
};

scout.TableConfigurator.prototype.configure = function() {
  this.table.selectionHandler = new scout.TableSelectionHandler(this.table);
};

scout.TableConfigurator.prototype.render = function() {
  this.table.$dataScroll = this.table.$data.appendDIV('scrollable-y');
  this.table.scrollbar = new scout.Scrollbar(this.table.$dataScroll, 'y');
};
