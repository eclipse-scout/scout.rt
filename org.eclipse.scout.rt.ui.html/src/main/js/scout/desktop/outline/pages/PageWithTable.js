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

/**
 * @class
 * @extends scout.Page
 */
scout.PageWithTable = function() {
  scout.PageWithTable.parent.call(this);

  this.nodeType = scout.Page.NodeType.TABLE;
  this.alwaysCreateChildPage = false;
};
scout.inherits(scout.PageWithTable, scout.Page);

/**
 * @override scout.Page
 */
scout.PageWithTable.prototype._initTable = function(table) {
  scout.PageWithTable.parent.prototype._initTable.call(this, table);
  table.on('rowsDeleted allRowsDeleted', this._onTableRowsDeleted.bind(this));
  table.on('rowsInserted', this._onTableRowsInserted.bind(this));
  table.on('rowsUpdated', this._onTableRowsUpdated.bind(this));
  table.on('rowAction', this._onTableRowAction.bind(this));
  table.on('rowOrderChanged', this._onTableRowOrderChanged.bind(this));
};

scout.PageWithTable.prototype._onTableRowsDeleted = function(event) {
  if (this.leaf) { // when page is a leaf we do nothing at all
    return;
  }
  var rows = scout.arrays.ensure(event.rows),
    childPages = rows.map(function(row) {
      var childPage = row.page;
      scout.Page.unlinkRowWithPage(row, childPage);
      return childPage;
    }, this);

  this.getOutline().mediator.onTableRowsDeleted(rows, childPages, this);
};

/**
 * We must set childNodeIndex on each created childPage because it is required to
 * determine the order of nodes in the tree.
 */
scout.PageWithTable.prototype._onTableRowsInserted = function(event) {
  if (this.leaf) { // when page is a leaf we do nothing at all
    return;
  }

  var rows = scout.arrays.ensure(event.rows),
    childPages = rows.map(function(row) {
      return this._createChildPageInternal(row);
    }, this);

  this.getOutline().mediator.onTableRowsInserted(rows, childPages, this);
};

scout.PageWithTable.prototype._onTableRowsUpdated = function(event) {
  this.getOutline().mediator.onTableRowsUpdated(event, this);
};

scout.PageWithTable.prototype._onTableRowAction = function(event) {
  this.getOutline().mediator.onTableRowAction(event, this);
};

scout.PageWithTable.prototype._onTableRowOrderChanged = function(event) {
  if (event.animating) { // do nothing while row order animation is in progress
    return;
  }
  this.getOutline().mediator.onTableRowOrderChanged(event, this);
};

scout.PageWithTable.prototype._createChildPageInternal = function(row) {
  var childPage = this.createChildPage(row);
  if (childPage === null && this.alwaysCreateChildPage) {
    childPage = this.createDefaultChildPage(row);
  }
  scout.Page.linkRowWithPage(row, childPage);
  return childPage;
};

/**
 * Override this method to return a specific Page instance for the given table-row.
 * The default impl. returns null, which means a AutoLeaftPageWithNodes instance will be created for the table-row.
 */
scout.PageWithTable.prototype.createChildPage = function(row) {
  return null;
};

scout.PageWithTable.prototype.createDefaultChildPage = function(row) {
  return scout.create('AutoLeafPageWithNodes', {
    parent: this.getOutline(),
    row: row
  });
};

/**
 * @override TreeNode.js
 */
scout.PageWithTable.prototype.loadChildren = function() {
  // It's allowed to have no table - but we don't have to load data in that case
  if (!this.detailTable) {
    return $.resolvedDeferred();
  }
  return this.loadTableData();
};

/**
 * see Java: AbstractPageWithTable#loadChildren that's where the table is reloaded and the tree is rebuilt, called by AbstractTree#P_UIFacade
 * @returns {$.Deferred}
 */
scout.PageWithTable.prototype.loadTableData = function() {
  this.detailTable.deleteAllRows();
  this.detailTable.setLoading(true);
  return this._loadTableData()
    .done(this._onLoadTableDataDone.bind(this))
    .fail(this._onLoadTableDataFail.bind(this))
    .always(this._onLoadTableDataAlways.bind(this));
};

/**
 * Override this method to load table data (rows to be added to table).
 * This is an asynchronous operation working with a Deferred. When table data load is successful
 * <code>_onLoadTableData(data)</code> will be called. When a failure occurs while loading table
 * data <code>_onLoadTableFail(data)</code> will be called.
 * <p>
 * When you want to return static data you still need a deferred. But you can resolve it
 * immediately. Example code:
 * <code>
 *   var deferred = $.Deferred();
 *   deferred.resolve([{...},{...}]);
 *   return deferred;
 * </code>
 *
 * @return {$.Deferred}
 */
scout.PageWithTable.prototype._loadTableData = function() {
  return $.resolvedDeferred();
};

/**
 * This method is called when table data load is successful. It should transform the table data
 * object to table rows.
 *
 * @param tableData data loaded by <code>_loadTableData</code>
 */
scout.PageWithTable.prototype._onLoadTableDataDone = function(tableData) {
  var rows = this._transformTableDataToTableRows(tableData);
  if (rows && rows.length > 0) {
    this.detailTable.insertRows(rows);
  }
};

scout.PageWithTable.prototype._onLoadTableDataFail = function(error) {
  this.detailTable.setTableStatus(scout.Status.error({
    message: this.session.text("ErrorWhileLoadingData")
  }));
  $.log.error('Failed to load tableData. error=', error);
};

scout.PageWithTable.prototype._onLoadTableDataAlways = function() {
  this.childrenLoaded = true;
  this.detailTable.setLoading(false);
};

/**
 * This method converts the loaded table data, which can be any object, into table rows.
 * You must override this method unless tableData is already an array of table rows.
 *
 * @param tableData
 * @returns
 */
scout.PageWithTable.prototype._transformTableDataToTableRows = function(tableData) {
  return tableData;
};
