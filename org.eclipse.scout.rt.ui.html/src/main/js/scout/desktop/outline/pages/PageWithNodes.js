/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  var table = scout.create('Table', {
    parent: this.parent,
    id: 'PageWithNodesTable',
    autoResizeColumns: true,
    headerVisible: false,
    columns: [nodeColumn]
  });
  table.on('rowAction', this._onDetailTableRowAction.bind(this));
  return table;
};

scout.PageWithNodes.prototype._onDetailTableRowAction = function(event) {
  var clickedRow = event.source.rowsMap[event.row.id];
  var nodeToSelect = clickedRow.node;
  this.getOutline().selectNode(nodeToSelect);
};

/**
 * @override Page.js
 */
scout.PageWithNodes.prototype._loadTableData = function() {
  var i = 0, row, rows = [],
    deferred = $.Deferred();
  this.childNodes.forEach(function(node) {
    // we add an additional property 'node' to the table-row. This way, we
    // don't need an additional map to link the table-row with the tree-node
    row = {
      id: i.toString(),
      cells: [node.text],
      node: node
    };
    rows.push(row);
    i++;
  });
  deferred.resolve(rows);
  return deferred;
};

/**
 * @override TreeNode.js
 */
scout.PageWithNodes.prototype.loadChildren = function() {
  return this.loadTableData();
};
