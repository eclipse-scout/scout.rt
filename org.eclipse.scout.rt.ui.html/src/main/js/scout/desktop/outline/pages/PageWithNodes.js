scout.PageWithNodes = function(outline) {
  scout.PageWithNodes.parent.call(this, outline);
  this.nodeType = "nodes";
};
scout.inherits(scout.PageWithNodes, scout.Page);

scout.PageWithNodes.prototype._init = function() {
  scout.PageWithNodes.parent.prototype._init.call(this);
  this.loadTableData();
};

/**
 * @override Page.js
 */
scout.PageWithNodes.prototype._createTable = function() {
  var singleColumn = {
    index: 0,
    objectType: 'Column'
  };
  return scout.create('Table', {
    parent: this.tree,
    columns: [singleColumn],
    rows: []
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
  // NOP
  this.tree._onPageChanged2(this);
};
