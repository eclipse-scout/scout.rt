scout.PageWithTable = function(outline) {
  scout.PageWithTable.parent.call(this, outline);

  this.nodeType = "table";
  this.alwaysCreateChildPage = true; // FIXME [awe] 6.1 - change to default 'false'. Check if AutoLeafPageWithNodes work
};
scout.inherits(scout.PageWithTable, scout.Page);

scout.PageWithTable.prototype._init = function() {
  scout.PageWithTable.parent.prototype._init.call(this);
  this.loadChildren();
};

scout.PageWithTable.prototype._createChildPageInternal = function(tableRow) {
  var childPage = this.createChildPage(tableRow);
  if (childPage === null && this.alwaysCreateChildPage) {
    childPage = this.createDefaultChildPage(tableRow);
  }
  return childPage;
};

// AbstractPageWithTable#loadChildren -> hier wird die table geladen und der baum neu aufgebaut
// wird von AbstractTree#P_UIFacade aufgerufen
scout.PageWithTable.prototype.loadTableData = function() {

};

scout.PageWithTable.prototype.createChildPage = function(tableRow) {
  return null;
};

scout.PageWithTable.prototype.createDefaultChildPage = function(tableRow) {
  return new scout.AutoLeafPageWithNodes(this.tree, tableRow);
};

// AbstractPageWithTable#P_TableListener hat einen listener auf der table, über die listener wird
// der baum mit der tabelle synchronisiert

/**
 * @override TreeNode.js
 */
scout.PageWithTable.prototype.loadChildren = function() {
  this.table = this.loadTableData();
  var childPage, childNodes = [];
  // FIXME [awe] 6.1 create child nodes for table rows, check how this is done in Java model
  this.table.rows.forEach(function(row) {
    childPage = this._createChildPageInternal(row);
    if (childPage !== null) {
      childNodes.push(childPage);
    }
  }, this);
  this.childNodes = childNodes;
};
