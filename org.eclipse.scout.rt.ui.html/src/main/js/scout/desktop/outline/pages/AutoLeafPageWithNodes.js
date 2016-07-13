scout.AutoLeafPageWithNodes = function(outline, tableRow) {
  scout.AutoLeafPageWithNodes.parent.call(this, outline);
  this.tableRow = tableRow;
};
scout.inherits(scout.AutoLeafPageWithNodes, scout.Page);

/**
 * @override Page.js
 */
scout.AutoLeafPageWithNodes.prototype._init = function() {
  this.text = this.tableRow.cells[0];
};
