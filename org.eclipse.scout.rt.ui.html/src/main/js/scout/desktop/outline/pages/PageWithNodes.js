/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * @class
 * @extends scout.Page
 */
scout.PageWithNodes = function() {
  scout.PageWithNodes.parent.call(this);

  this.nodeType = scout.Page.NodeType.NODES;
};
scout.inherits(scout.PageWithNodes, scout.Page);

/**
 * @override Page.js
 */
scout.PageWithNodes.prototype._createTable = function() {
  var nodeColumn = scout.create('Column', {
    id: 'NodeColumn',
    session: this.session
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

scout.PageWithNodes.prototype._rebuildDetailTable = function(childPages) {
  var table = this.detailTable;
  this._unlinkAllTableRows(table.rows);
  table.deleteAllRows();
  var rows = this._createTableRowsForChildPages(childPages);
  table.insertRows(rows);
};

scout.PageWithNodes.prototype._unlinkAllTableRows = function(rows) {
  rows.forEach(function(row) {
    if (row.page) {
      row.page.unlinkWithRow(row);
    }
  });
};

scout.PageWithNodes.prototype._createTableRowsForChildPages = function(childPages) {
  return childPages.map(function(childPage) {
    var row = scout.create('TableRow', {
      parent: this.detailTable,
      cells: [childPage.text]}
    );
    childPage.linkWithRow(row);
    return row;
  }, this);
};

/**
 * @override TreeNode.js
 */
scout.PageWithNodes.prototype.loadChildren = function() {
  this.childrenLoaded = false;
  return this._createChildPages().done(function(childPages) {
    this._rebuildDetailTable(childPages);
    if (childPages.length > 0) {
      this.getOutline().insertNodes(childPages, this);
    }
    this.childrenLoaded = true;
  }.bind(this));
};

/**
 * Override this method to create child pages for this page. The default impl. returns an empty array.
 * @return {$.Deferred}
 */
scout.PageWithNodes.prototype._createChildPages = function() {
  return $.resolvedDeferred();
};

