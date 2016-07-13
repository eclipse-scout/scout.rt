scout.Page = function(outline) {
  scout.Page.parent.call(this, outline);

  this.table;
  this.detailForm;
  this.tableVisible = true;
  this.detailFormVisible = true;
};
scout.inherits(scout.Page, scout.TreeNode);

/**
 * @override TreeNode.js
 */
scout.Page.prototype._init = function(model) {
  scout.Page.parent.prototype._init.call(this, model);
  this.table = this._createTable();
};

/**
 * Override this method to create the internal table. Default impl. returns null.
 */
scout.Page.prototype._createTable = function() {
  return null;
};

// AbstractPageWithTable#loadChildren -> hier wird die table geladen und der baum neu aufgebaut
// wird von AbstractTree#P_UIFacade aufgerufen
scout.Page.prototype.loadTableData = function() {
  if (this.table) {
    this.table.deleteAllRows();
    var rows = this._loadTableData();
    if (rows && rows.length > 0) {
      this.table.insertRows(rows);
    }
  }
};

/**
 * Override this method to load table data (rows to be added to table).
 */
scout.Page.prototype._loadTableData = function() {
  // NOP
};

scout.Page.prototype.getTreeNodeFor = function(tableRow) {

};

scout.Page.prototype.getPageFor = function(tableRow) {

};

scout.Page.prototype.getTableRowFor = function(treeNode) {

};

scout.Page.prototype.getTableRowsFor = function(treeNodes) {

};

scout.Page.prototype.addChildPage = function(childPage) {
  this.childNodes.push(childPage);
};
