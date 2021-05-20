/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Page, scout, Status} from '../../../index';
import $ from 'jquery';

/**
 * @class
 * @extends Page
 */
export default class PageWithTable extends Page {

  constructor() {
    super();

    this.nodeType = Page.NodeType.TABLE;
    this.alwaysCreateChildPage = false;

    this._tableRowDeleteHandler = this._onTableRowsDeleted.bind(this);
    this._tableRowInsertHandler = this._onTableRowsInserted.bind(this);
    this._tableRowUpdateHandler = this._onTableRowsUpdated.bind(this);
    this._tableRowActionHandler = this._onTableRowAction.bind(this);
    this._tableRowOrderChangeHandler = this._onTableRowOrderChanged.bind(this);
    this._tableDataLoadHandler = this.loadTableData.bind(this);
  }

  /**
   * @override Page
   */
  _initDetailTable(table) {
    super._initDetailTable(table);
    table.on('rowsDeleted allRowsDeleted', this._tableRowDeleteHandler);
    table.on('rowsInserted', this._tableRowInsertHandler);
    table.on('rowsUpdated', this._tableRowUpdateHandler);
    table.on('rowAction', this._tableRowActionHandler);
    table.on('rowOrderChanged', this._tableRowOrderChangeHandler);
    table.on('reload', this._tableDataLoadHandler);
    table.hasReloadHandler = true;
  }

  /**
   * @override Page
   */
  _destroyDetailTable(table) {
    table.off('rowsDeleted allRowsDeleted', this._tableRowDeleteHandler);
    table.off('rowsInserted', this._tableRowInsertHandler);
    table.off('rowsUpdated', this._tableRowUpdateHandler);
    table.off('rowAction', this._tableRowActionHandler);
    table.off('rowOrderChanged', this._tableRowOrderChangeHandler);
    table.off('reload', this._tableDataLoadHandler);
    super._destroyDetailTable(table);
  }

  _onTableRowsDeleted(event) {
    if (this.leaf) { // when page is a leaf we do nothing at all
      return;
    }
    let rows = arrays.ensure(event.rows),
      childPages = rows.map(row => {
        let childPage = row.page;
        childPage.unlinkWithRow(row);
        return childPage;
      }, this);

    this.getOutline().mediator.onTableRowsDeleted(rows, childPages, this);
  }

  /**
   * We must set childNodeIndex on each created childPage because it is required to
   * determine the order of nodes in the tree.
   */
  _onTableRowsInserted(event) {
    if (this.leaf) { // when page is a leaf we do nothing at all
      return;
    }

    let rows = arrays.ensure(event.rows),
      childPages = rows.map(function(row) {
        return this._createChildPageInternal(row);
      }, this);

    this.getOutline().mediator.onTableRowsInserted(rows, childPages, this);
  }

  _onTableRowsUpdated(event) {
    this.getOutline().mediator.onTableRowsUpdated(event, this);
  }

  _onTableRowAction(event) {
    this.getOutline().mediator.onTableRowAction(event, this);
  }

  _onTableRowOrderChanged(event) {
    if (event.animating) { // do nothing while row order animation is in progress
      return;
    }
    this.getOutline().mediator.onTableRowOrderChanged(event, this);
  }

  _createChildPageInternal(row) {
    let childPage = this.createChildPage(row);
    if (childPage === null && this.alwaysCreateChildPage) {
      childPage = this.createDefaultChildPage(row);
    }
    childPage.linkWithRow(row);
    childPage = childPage.updatePageFromTableRow(row);
    return childPage;
  }

  /**
   * Override this method to return a specific Page instance for the given table-row.
   * The default impl. returns null, which means a AutoLeaftPageWithNodes instance will be created for the table-row.
   */
  createChildPage(row) {
    return null;
  }

  createDefaultChildPage(row) {
    return scout.create('AutoLeafPageWithNodes', {
      parent: this.getOutline(),
      row: row
    });
  }

  /**
   * @override TreeNode.js
   */
  loadChildren() {
    // It's allowed to have no table - but we don't have to load data in that case
    if (!this.detailTable) {
      return $.resolvedDeferred();
    }
    return this.loadTableData();
  }

  _createSearchFilter() {
    let firstFormTableControl = arrays.find(this.detailTable.tableControls, tableControl => {
      return tableControl.form;
    });
    if (firstFormTableControl) {
      return firstFormTableControl.form.exportData();
    }
    return null;
  }

  /**
   * see Java: AbstractPageWithTable#loadChildren that's where the table is reloaded and the tree is rebuilt, called by AbstractTree#P_UIFacade
   * @returns {$.Deferred}
   */
  loadTableData() {
    this.ensureDetailTable();
    this.detailTable.deleteAllRows();
    this.detailTable.setLoading(true);
    return this._loadTableData(this._createSearchFilter())
      .then(this._onLoadTableDataDone.bind(this))
      .catch(this._onLoadTableDataFail.bind(this))
      .then(this._onLoadTableDataAlways.bind(this));
  }

  /**
   * Override this method to load table data (rows to be added to table).
   * This is an asynchronous operation working with a Deferred. When table data load is successful
   * <code>_onLoadTableDataDone(data)</code> will be called. When a failure occurs while loading table
   * data <code>_onLoadTableDataFail(data)</code> will be called.
   * <p>
   * When you want to return static data you still need a deferred. But you can resolve it
   * immediately. Example code:
   * <code>
   *   var deferred = $.Deferred();
   *   deferred.resolve([{...},{...}]);
   *   return deferred;
   * </code>
   *
   * @param searchFilter The search filter as exported by the search form or null.
   *
   * @return {Promise}
   */
  _loadTableData(searchFilter) {
    return $.resolvedDeferred();
  }

  /**
   * This method is called when table data load is successful. It should transform the table data
   * object to table rows.
   *
   * @param tableData data loaded by <code>_loadTableData</code>
   */
  _onLoadTableDataDone(tableData) {
    let rows = this._transformTableDataToTableRows(tableData);
    if (rows && rows.length > 0) {
      this.detailTable.insertRows(rows);
    }
  }

  _onLoadTableDataFail(error) {
    this.detailTable.setTableStatus(Status.error({
      message: this.session.text('ErrorWhileLoadingData')
    }));
    $.log.error('Failed to load tableData. error=', error);
  }

  _onLoadTableDataAlways() {
    this.childrenLoaded = true;
    this.detailTable.setLoading(false);
  }

  /**
   * This method converts the loaded table data, which can be any object, into table rows.
   * You must override this method unless tableData is already an array of table rows.
   *
   * @param tableData
   * @returns
   */
  _transformTableDataToTableRows(tableData) {
    return tableData;
  }
}
