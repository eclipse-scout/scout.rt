scout.TableConfigurator = function(table) {
  this.table = table;
};

scout.TableConfigurator.prototype.configure = function() {
  this.table.selectionHandler = new scout.TableSelectionHandler(this.table);
  this.table.rowMenuHandler = new scout.TableRowMenuHandler(this.table);
};

scout.TableConfigurator.prototype.render = function() {
  this.table.$dataScroll = this.table.$data.appendDiv(this.table.model.id + '_dataScroll', 'scrollable');
  this.table.scrollbar = new scout.Scrollbar(this.table.$dataScroll, 'y');
};
