scout.TableInfoLoadTooltip = function() {
  scout.TableInfoLoadTooltip.parent.call(this);
};
scout.inherits(scout.TableInfoLoadTooltip, scout.Tooltip);

scout.TableInfoLoadTooltip.prototype._init = function(options) {
  scout.TableInfoLoadTooltip.parent.prototype._init.call(this, options);

  this.tableFooter = options.tableFooter;
};

scout.TableInfoLoadTooltip.prototype._renderText = function() {
  var table = this.tableFooter.table,
    numRows = table.rows.length;

  this.$content.appendSpan().text(this.session.text('ui.NumRowsLoaded', this.tableFooter.computeCountInfo(numRows)));
  this.$content.appendBr();
  this.$content.appendSpan('link')
    .text(this.session.text('ui.ReloadData'))
    .on('click', this._onReloadClick.bind(this));
};

scout.TableInfoLoadTooltip.prototype._onReloadClick = function() {
  this.tableFooter.table.reload();
  this.remove();
};
