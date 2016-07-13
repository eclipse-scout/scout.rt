scout.Page = function(outline) {
  scout.Page.parent.call(this, outline);

  this.table;
  this.detailForm;
  this.tableVisible = true;
  this.detailFormVisible = true;
};
scout.inherits(scout.Page, scout.TreeNode);

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
