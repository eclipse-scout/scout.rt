scout.PageWithNodes = function() {
  scout.PageWithNodes.parent.call(this);
  this.nodeType = "nodes";
};
scout.inherits(scout.PageWithNodes, scout.Page);

/**
 * @override Page.js
 */
scout.PageWithNodes.prototype._createTable = function() {
  var nodeColumn = scout.create('Column', {
    index: 0,
    id: 'NodeColumn'
  });
  return scout.create('Table', {
    parent: this.tree,
    id: 'PageWithNodesTable',
    autoResizeColumns: true,
    columns: [nodeColumn]
  });
};

/**
 * @override Page.js
 */
scout.PageWithNodes.prototype._loadTableData = function() {
  var i = 0, rows = [];
  this.childNodes.forEach(function(node) {
    rows.push({id: i.toString(), cells: [node.text]});
    i++;
  });
  return rows;
};

/**
 * @override TreeNode.js
 */
scout.PageWithNodes.prototype.loadChildren = function() {
  // FIXME 6.1 [awe] - remove this hack
  this.tree._onPageChanged2(this);
};
