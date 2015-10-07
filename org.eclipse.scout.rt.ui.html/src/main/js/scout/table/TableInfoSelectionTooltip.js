scout.TableInfoSelectionTooltip = function() {
  scout.TableInfoSelectionTooltip.parent.call(this);
};
scout.inherits(scout.TableInfoSelectionTooltip, scout.Tooltip);

scout.TableInfoSelectionTooltip.prototype._init = function(options) {
  scout.TableInfoSelectionTooltip.parent.prototype._init.call(this, options);

  this.tableFooter = options.tableFooter;
};

scout.TableInfoSelectionTooltip.prototype._renderText = function() {
  var table = this.tableFooter.table,
    numRowsSelected = table.selectedRows.length,
    numRows = table.rows.length,
    all = scout.helpers.nvl(all, (numRows === numRowsSelected));

  this.$content.appendSpan().text(this.session.text('ui.NumRowsSelected', this.tableFooter.computeCountInfo(numRowsSelected)));
  this.$content.appendBr();
  this.$content.appendSpan('link')
    .text(this.session.text('ui.SelectNone'))
    .on('click', this._onSelectNoneClick.bind(this));
  this.$content.appendBr();
  this.$content.appendSpan('link')
    .text(this.session.text('ui.SelectAll'))
    .on('click', this._onSelectAllClick.bind(this));
};

scout.TableInfoSelectionTooltip.prototype._onSelectNoneClick = function() {
  this.tableFooter.table.clearSelection();
  this.remove();
};

scout.TableInfoSelectionTooltip.prototype._onSelectAllClick = function() {
  this.tableFooter.table.selectAll();
  this.remove();
};
