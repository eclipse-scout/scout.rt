/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, AutoLeafPageWithNodes, DoEntity, EventHandler, Form, FormTableControl, LimitedResultInfoContributionDo, ObjectOrModel, Page, PageWithTableEventMap, PageWithTableModel, scout, Status, Table, TableAllRowsDeletedEvent,
  TableMaxResultsHelper, TableReloadEvent, TableReloadReason, TableRow, TableRowActionEvent, TableRowOrderChangedEvent, TableRowsDeletedEvent, TableRowsInsertedEvent, TableRowsUpdatedEvent
} from '../../../index';
import $ from 'jquery';

export class PageWithTable extends Page implements PageWithTableModel {
  declare model: PageWithTableModel;
  declare eventMap: PageWithTableEventMap;

  alwaysCreateChildPage: boolean;

  protected _tableRowDeleteHandler: EventHandler<TableRowsDeletedEvent | TableAllRowsDeletedEvent>;
  protected _tableRowInsertHandler: EventHandler<TableRowsInsertedEvent>;
  protected _tableRowUpdateHandler: EventHandler<TableRowsUpdatedEvent>;
  protected _tableRowActionHandler: EventHandler<TableRowActionEvent>;
  protected _tableRowOrderChangeHandler: EventHandler<TableRowOrderChangedEvent>;
  protected _tableDataLoadHandler: EventHandler<TableReloadEvent>;

  constructor() {
    super();

    this.nodeType = Page.NodeType.TABLE;
    this.inheritMenusFromParentTablePage = false;
    this.alwaysCreateChildPage = false;

    this._tableRowDeleteHandler = this._onTableRowsDeleted.bind(this);
    this._tableRowInsertHandler = this._onTableRowsInserted.bind(this);
    this._tableRowUpdateHandler = this._onTableRowsUpdated.bind(this);
    this._tableRowActionHandler = this._onTableRowAction.bind(this);
    this._tableRowOrderChangeHandler = this._onTableRowOrderChanged.bind(this);
    this._tableDataLoadHandler = (e: TableReloadEvent) => this.loadTableData(e?.reloadReason);
  }

  protected override _initDetailTable(table: Table) {
    super._initDetailTable(table);
    table.on('rowsDeleted allRowsDeleted', this._tableRowDeleteHandler);
    table.on('rowsInserted', this._tableRowInsertHandler);
    table.on('rowsUpdated', this._tableRowUpdateHandler);
    table.on('rowAction', this._tableRowActionHandler);
    table.on('rowOrderChanged', this._tableRowOrderChangeHandler);
    table.on('reload', this._tableDataLoadHandler);
    table.hasReloadHandler = true;
  }

  protected override _destroyDetailTable(table: Table) {
    table.off('rowsDeleted allRowsDeleted', this._tableRowDeleteHandler);
    table.off('rowsInserted', this._tableRowInsertHandler);
    table.off('rowsUpdated', this._tableRowUpdateHandler);
    table.off('rowAction', this._tableRowActionHandler);
    table.off('rowOrderChanged', this._tableRowOrderChangeHandler);
    table.off('reload', this._tableDataLoadHandler);
    super._destroyDetailTable(table);
  }

  protected _onTableRowsDeleted(event: TableRowsDeletedEvent | TableAllRowsDeletedEvent) {
    if (this.leaf) { // when page is a leaf we do nothing at all
      return;
    }
    const rows = arrays.ensure(event.rows);
    const childPages = [];
    rows.forEach(row => {
      const childPage = row.page;
      if (!childPage) {
        return;
      }
      childPage.unlinkWithRow(row);
      childPages.push(childPage);
    });

    this.getOutline().mediator.onTableRowsDeleted(rows, childPages, this);
  }

  protected _onTableRowsInserted(event: TableRowsInsertedEvent) {
    if (this.leaf) { // when page is a leaf we do nothing at all
      return;
    }

    let rows = arrays.ensure(event.rows);
    let childPages = rows.map(row => this._createChildPageInternal(row)).filter(Boolean);

    this.getOutline().mediator.onTableRowsInserted(rows, childPages, this);
  }

  protected _onTableRowsUpdated(event: TableRowsUpdatedEvent) {
    this.getOutline().mediator.onTableRowsUpdated(event, this);
  }

  protected _onTableRowAction(event: TableRowActionEvent) {
    this.getOutline().mediator.onTableRowAction(event, this);
  }

  protected _onTableRowOrderChanged(event: TableRowOrderChangedEvent) {
    if (event.animating) { // do nothing while row order animation is in progress
      return;
    }
    this.getOutline().mediator.onTableRowOrderChanged(event, this);
  }

  protected _createChildPageInternal(row: TableRow): Page {
    let childPage = this.createChildPage(row);
    if (!childPage && this.alwaysCreateChildPage) {
      childPage = this.createDefaultChildPage(row);
    }
    if (childPage) {
      childPage.linkWithRow(row);
      childPage = childPage.updatePageFromTableRow(row);
    }
    return childPage;
  }

  /**
   * Override this method to return a specific Page instance for the given table-row.
   * The default impl. returns null, which means a AutoLeafPageWithNodes instance will be created for the table-row.
   */
  createChildPage(row: TableRow): Page {
    return null;
  }

  createDefaultChildPage(row: TableRow): Page {
    return scout.create(AutoLeafPageWithNodes, {
      parent: this.getOutline(),
      row: row
    });
  }

  override loadChildren(): JQuery.Promise<any> {
    // It's allowed to have no table - but we don't have to load data in that case
    if (!this.detailTable) {
      return $.resolvedPromise();
    }

    this.childrenLoaded = false;
    const deferred = $.Deferred();
    this.one('load error', e => deferred.resolve());
    this.detailTable.reload();
    return deferred.promise().then(() => {
      this.childrenLoaded = true;
    });
  }

  // FIXME bsh [js-bookmark] Cleanup
  protected _createSearchFilter(): any {
    return this.getSearchFilter();
    // // Cast could be wrong as any table control is in the list.
    // // But as the FormTableControl does not add new public items except the form and the presence of that is form is ensured in the find() method, it may be fine.
    // let controls = this.detailTable.tableControls as FormTableControl[];
    //
    // let firstFormTableControl = arrays.find(controls, tableControl => tableControl.form instanceof Form);
    // if (firstFormTableControl) {
    //   return firstFormTableControl.form.exportData();
    // }
    // return null;
  }

  getSearchForm(): Form {
    let tableControl = this.detailTable.findTableControl(FormTableControl, tableControl => !!tableControl.form);
    return tableControl ? tableControl.form : null;
  }

  getSearchFilter(): any {
    return this.getSearchForm()?.exportData();
  }

  setSearchFilter(searchFilter: any) {
    this.getSearchForm()?.setData(searchFilter);
    this.getSearchForm()?.importData();
  }

  /**
   * Adds a {@link MaxRowCountContributionDo} to the given request.
   * Typically, this method should be used before sending a request in {@link _loadTableData} to attach the row limit constraints (if existing).
   * The contribution is only added if there is a row limit. Otherwise, the request remains untouched.
   * @example
   * protected override _loadTableData(searchFilter: MyRestrictionDo): JQuery.Promise<MyResponseDo> {
   *   const request: MyRequestDo = {
   *     id: '1',
   *     ...
   *     restriction: searchFilter
   *   };
   *   return ajax.postJson(url, this._withMaxRowCountContribution(request));
   * }
   * @param dataObject The {@link DoEntity} to which the contribution should be added.
   * @returns the resulting request with the added contribution.
   */
  protected _withMaxRowCountContribution<T>(dataObject: T): T {
    return scout.create(TableMaxResultsHelper).addMaxRowCountContribution(dataObject, this.detailTable);
  }

  /**
   * see Java: AbstractPageWithTable#loadChildren that's where the table is reloaded and the tree is rebuilt, called by AbstractTree#P_UIFacade
   */
  loadTableData(reloadReason?: TableReloadReason): JQuery.Promise<any> {
    this.ensureDetailTable();
    this.detailTable.setLoading(true);
    const restoreSelectionInfo = this._getRestoreSelectionInfo();
    return this._loadTableData(this._createSearchFilter())
      .then(data => this._onLoadTableDataDone(data, restoreSelectionInfo))
      .catch(error => this._onLoadTableDataFail(error, restoreSelectionInfo));
  }

  /**
   * Get info needed to restore the selection after table data was loaded.
   * - {@link RestoreSelectionInfo.restoreSelection} is `true` if a child page of this page is currently selected.
   * - {@link RestoreSelectionInfo.selectedRowKey} is the row key (see {@link TableRow.getKeyValues}) of the row corresponding to the direct child page of this page that is currently selected or a parent of the currently selected page.
   */
  protected _getRestoreSelectionInfo(): RestoreSelectionInfo {
    let restoreSelection = false;
    let selectedRowKey = null;
    if (this.getOutline().selectedNode()) {
      let node = this.getOutline().selectedNode();
      while (node?.parentNode) {
        if (node.parentNode === this) {
          restoreSelection = true;
          selectedRowKey = node.row?.getKeyValues();
          break;
        }
        node = node.parentNode;
      }
    }
    return {restoreSelection, selectedRowKey};
  }

  /**
   * Restores the selection by the given {@link RestoreSelectionInfo}. If there is no selected page for the current outline, the following page will be selected:
   * 1. The page corresponding to the selected row of the detail table of this page.
   * 2. The page corresponding to the row found by the given former selected row key (@see {@link RestoreSelectionInfo}).
   * 3. This page.
   */
  protected _restoreSelection(restoreSelectionInfo?: RestoreSelectionInfo) {
    if (!restoreSelectionInfo) {
      return;
    }
    try {
      const {restoreSelection, selectedRowKey} = restoreSelectionInfo;
      if (restoreSelection && !this.getOutline().selectedNode()) {
        let selectedNode = this.detailTable.selectedRow()?.page
          || this.detailTable.getRowByKey(selectedRowKey)?.page
          || this;
        this.getOutline().selectNode(selectedNode);
      }
    } catch (e) {
      $.log.warn('Unable to restore selection.', e);
    }
  }

  /**
   * Override this method to load table data (rows to be added to table).
   *
   * This is an asynchronous operation working with a Promise. If table data load is successful,
   * {@link _onLoadTableDataDone} will be called. If a failure occurs while loading table data,
   * {@link _onLoadTableDataFail} will be called.
   *
   * To return static data, use a resolved promise: `return $.resolvedPromise({...});`
   *
   * @param searchFilter The search filter as exported by the search form or null.
   */
  protected _loadTableData(searchFilter: any): JQuery.Promise<any> {
    return $.resolvedPromise();
  }

  /**
   * This method is called when table data load is successful. It should transform the table data
   * object to table rows and add them to the table.
   *
   * @param tableData data loaded by {@link _loadTableData}
   * @param restoreSelectionInfo information needed to restore the selection after table data was loaded
   */
  protected _onLoadTableDataDone(tableData: any, restoreSelectionInfo?: RestoreSelectionInfo) {
    let success = false;
    try {
      const rows = arrays.ensure(this._transformTableDataToTableRows(tableData));
      const limitedResultInfoDo = this._getLimitedResultInfoDo(tableData);
      this._readLimitedResultInfo(rows.length, limitedResultInfoDo); // apply properties from LimitedResultInfoDo to table (must be before replaceRows as this triggers the TableFooter update which already requires the new values).
      this.detailTable.replaceRows(rows);
      this.detailTable.setLimitedResultTableStatus(!!limitedResultInfoDo?.limitedResult); // set table status after replaceRows as the new rows are required
      success = true;
    } finally {
      this._onLoadTableDataAlways(restoreSelectionInfo);
    }
    if (success) {
      this.trigger('load');
    }
  }

  protected _readLimitedResultInfo(numRows: number, limitedResultInfoDo?: LimitedResultInfoContributionDo) {
    if (!limitedResultInfoDo) {
      return;
    }
    // update table properties. The footer is automatically updated after the new rows have been created
    if (scout.create(TableMaxResultsHelper).isLoadMoreDataPossible(numRows, limitedResultInfoDo.estimatedRowCount, limitedResultInfoDo.maxRowCount)) {
      // only update if the next load would be a ReloadReason.OVERRIDE_ROW_LIMIT so that the new limit is used
      this.detailTable.setMaxRowCount(limitedResultInfoDo.maxRowCount);
    }
    this.detailTable.setEstimatedRowCount(limitedResultInfoDo.estimatedRowCount);
  }

  protected _getLimitedResultInfoDo(tableData: any): LimitedResultInfoContributionDo {
    return arrays.ensure(tableData?._contributions)
      .find((contribution: DoEntity) => contribution._type === 'scout.LimitedResultInfoContribution') as LimitedResultInfoContributionDo;
  }

  protected _onLoadTableDataFail(error: any, restoreSelectionInfo?: RestoreSelectionInfo) {
    try {
      this.detailTable.setTableStatus(Status.error({
        message: this.session.text('ErrorWhileLoadingData')
      }));
      $.log.error('Failed to load tableData. error=', error);
      this.detailTable.deleteAllRows();
    } finally {
      this._onLoadTableDataAlways(restoreSelectionInfo);
      this.trigger('error', {error});
    }
  }

  protected _onLoadTableDataAlways(restoreSelectionInfo?: RestoreSelectionInfo) {
    this._restoreSelection(restoreSelectionInfo);
    this.detailTable.setLoading(false);
  }

  /**
   * This method converts the loaded table data, which can be any object, into table rows.
   * You must override this method unless tableData is already an array of table rows.
   */
  protected _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
    return tableData;
  }
}

/**
 * Object containing the info needed to restore the selection after table data was loaded.
 */
export type RestoreSelectionInfo = {
  /**
   * Whether the selection should be restored or not.
   */
  restoreSelection: boolean;
  /**
   * Former selected row key.
   */
  selectedRowKey: any[];
};
