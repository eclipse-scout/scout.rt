/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, AutoLeafPageWithNodes, BookmarkSupport, BookmarkTableRowIdentifierDo, dataObjects, DoEntity, Event, EventHandler, Form, InitModelOf, LimitedResultInfoContributionDo, ObjectOrModel, Page, PageWithTableEventMap, PageWithTableModel,
  PropertyChangeEvent, scout, SearchFilterTextBuilder, SearchFormTableControl, SearchRequiredTableStatus, Status, Table, TableAllRowsDeletedEvent, TableControl, TableMaxResultsHelper, TableOrganizerMenu, TableReloadEvent, TableReloadReason,
  TableRow, TableRowActionEvent, TableRowOrderChangedEvent, TableRowsDeletedEvent, TableRowsInsertedEvent, TableRowsUpdatedEvent
} from '../../../index';
import $ from 'jquery';

export class PageWithTable extends Page implements PageWithTableModel {
  declare model: PageWithTableModel;
  declare eventMap: PageWithTableEventMap;

  alwaysCreateChildPage: boolean;
  searchRequired = false;
  searchFilterCompleted = false;

  protected _reloadReason: TableReloadReason;
  protected _tableRowDeleteHandler: EventHandler<TableRowsDeletedEvent | TableAllRowsDeletedEvent>;
  protected _tableRowInsertHandler: EventHandler<TableRowsInsertedEvent>;
  protected _tableRowUpdateHandler: EventHandler<TableRowsUpdatedEvent>;
  protected _tableRowActionHandler: EventHandler<TableRowActionEvent>;
  protected _tableRowOrderChangeHandler: EventHandler<TableRowOrderChangedEvent>;
  protected _tableDataLoadHandler: EventHandler<TableReloadEvent>;
  protected _tableControlsChangeHandler: EventHandler<PropertyChangeEvent<TableControl[]>> = this._onTableControlsChange.bind(this);
  protected _searchFormTableControlSearchHandler: EventHandler<Event<SearchFormTableControl>> = this._onSearchFormTableControlSearch.bind(this);
  protected _searchFormTableControlResetHandler: EventHandler<Event<SearchFormTableControl>> = this._onSearchFormTableControlReset.bind(this);

  constructor() {
    super();

    this.nodeType = Page.NodeType.TABLE;
    this.inheritMenusFromParentTablePage = false;
    this.alwaysCreateChildPage = false;

    this._reloadReason = null;
    this._tableRowDeleteHandler = this._onTableRowsDeleted.bind(this);
    this._tableRowInsertHandler = this._onTableRowsInserted.bind(this);
    this._tableRowUpdateHandler = this._onTableRowsUpdated.bind(this);
    this._tableRowActionHandler = this._onTableRowAction.bind(this);
    this._tableRowOrderChangeHandler = this._onTableRowOrderChanged.bind(this);
    this._tableDataLoadHandler = this._onTableReload.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    // display rows as AutoLeafPageWithNodes if outline is compact
    if (this.outline.compact) {
      this.setAlwaysCreateChildPage(true);
      this.setLeaf(false);
    }
  }

  setAlwaysCreateChildPage(alwaysCreateChildPage: boolean) {
    this.alwaysCreateChildPage = alwaysCreateChildPage;
  }

  setSearchRequired(searchRequired: boolean) {
    this.searchRequired = searchRequired;
  }

  setSearchFilterCompleted(searchFilterCompleted: boolean) {
    this.searchFilterCompleted = searchFilterCompleted;
  }

  protected override _initDetailTable(table: Table) {
    super._initDetailTable(table);

    if (this.outline.compact) {
      // set table compact if outline is compact
      table.setCompact(true);

      // disable more-link and enable html to plain text on compact handler
      table.compactHandler.setMoreLinkAvailable(false);
      table.compactHandler.setLineCustomizer(line => line.textBlock.setHtmlToPlainTextEnabled(true));

      // hide all table controls except search form table control
      const searchFormTableControl = this._findSearchFormTableControl(table);
      for (const tableControl of table.tableControls) {
        if (tableControl === searchFormTableControl) {
          continue;
        }
        tableControl.setVisibleGranted(false);
      }
    }

    table.on('rowsDeleted allRowsDeleted', this._tableRowDeleteHandler);
    table.on('rowsInserted', this._tableRowInsertHandler);
    table.on('rowsUpdated', this._tableRowUpdateHandler);
    table.on('rowAction', this._tableRowActionHandler);
    table.on('rowOrderChanged', this._tableRowOrderChangeHandler);
    table.on('reload', this._tableDataLoadHandler);
    table.hasReloadHandler = true;
    table.insertMenus([scout.create(TableOrganizerMenu, {parent: table})]);

    table.on('propertyChange:tableControls', this._tableControlsChangeHandler);
    this._addSearchFormTableControlListeners(this._findSearchFormTableControl(table));

    // Ensure _initDetailTableUiPreferences is only called after _initDetailTable has been completed (including subclasses).
    this.one('propertyChange:detailTable', event => {
      if (event.newValue === table) {
        this._initDetailTableUiPreferences(table);
        this._initDetailTableSearchRequired();
      }
    });
  }

  protected _initDetailTableUiPreferences(table: Table) {
    table.setUiPreferencesEnabled(true);
  }

  protected _initDetailTableSearchRequired() {
    if (this.searchRequired && !this.searchFilterCompleted) {
      this.detailTable?.setTableStatus(SearchRequiredTableStatus.info(this.session.text('TooManyRows')));
      this.getSearchFormTableControl()?.setSelected(true);
    }
  }

  protected override _destroyDetailTable(table: Table) {
    table.off('rowsDeleted allRowsDeleted', this._tableRowDeleteHandler);
    table.off('rowsInserted', this._tableRowInsertHandler);
    table.off('rowsUpdated', this._tableRowUpdateHandler);
    table.off('rowAction', this._tableRowActionHandler);
    table.off('rowOrderChanged', this._tableRowOrderChangeHandler);
    table.off('reload', this._tableDataLoadHandler);

    table.off('propertyChange:tableControls', this._tableControlsChangeHandler);
    this._removeSearchFormTableControlListeners(this._findSearchFormTableControl(table));

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

    this.outline.mediator.onTableRowsDeleted(rows, childPages, this);
  }

  protected _onTableRowsInserted(event: TableRowsInsertedEvent) {
    if (this.leaf) { // when page is a leaf we do nothing at all
      return;
    }

    let rows = arrays.ensure(event.rows);
    let childPages = rows.map(row => this._createChildPageInternal(row)).filter(Boolean);

    this.outline.mediator.onTableRowsInserted(rows, childPages, this);
  }

  protected _onTableRowsUpdated(event: TableRowsUpdatedEvent) {
    this.outline.mediator.onTableRowsUpdated(event, this);
  }

  protected _onTableRowAction(event: TableRowActionEvent) {
    this.outline.mediator.onTableRowAction(event, this);
  }

  protected _onTableRowOrderChanged(event: TableRowOrderChangedEvent) {
    this.outline.mediator.onTableRowOrderChanged(event, this);
  }

  protected _onTableReload(event: TableReloadEvent) {
    this.loadTableData(event.reloadReason);
  }

  protected _onTableControlsChange(e: PropertyChangeEvent<TableControl[]>) {
    // disable search/reset listeners on old search form controls
    arrays.ensure(e.oldValue)
      .filter(tableControl => tableControl instanceof SearchFormTableControl)
      .forEach((searchFormTableControl: SearchFormTableControl) => this._removeSearchFormTableControlListeners(searchFormTableControl));

    // enable search/reset listeners on search form control
    this._addSearchFormTableControlListeners(this.getSearchFormTableControl());
  }

  /**
   * Adds search and reset listener to the given {@link SearchFormTableControl}.
   */
  protected _addSearchFormTableControlListeners(searchFormTableControl: SearchFormTableControl) {
    searchFormTableControl?.on('search', this._searchFormTableControlSearchHandler);
    searchFormTableControl?.on('reset', this._searchFormTableControlResetHandler);
  }

  /**
   * Removes search and reset listener from the given {@link SearchFormTableControl}.
   */
  protected _removeSearchFormTableControlListeners(searchFormTableControl: SearchFormTableControl) {
    searchFormTableControl?.off('search', this._searchFormTableControlSearchHandler);
    searchFormTableControl?.off('reset', this._searchFormTableControlResetHandler);
  }

  protected _onSearchFormTableControlSearch(event: Event<SearchFormTableControl>) {
    this.setSearchFilterCompleted(true);
    this.detailTable.reload(Table.ReloadReason.SEARCH);

    // close search table control after search if outline is compact, otherwise the search form covers the table
    if (this.outline.compact) {
      this.getSearchFormTableControl().setSelected(false);
    }
  }

  protected _onSearchFormTableControlReset(event: Event<SearchFormTableControl>) {
    this._resetTableData();
  }

  /**
   * Reverts the data in the {@link detailTable} to the original state by reloading it. For tables with
   * {@link searchRequired} = true, the data is not reloaded. Instead, the old rows are deleted, a table
   * status is displayed and {@link searchFilterCompleted} is set to false.
   *
   * This method must only be called after the detail table has been created ({@link ensureDetailTable}).
   */
  protected _resetTableData() {
    // It's allowed to have no table - but we don't have to load data in that case
    if (!this.detailTable) {
      return;
    }
    if (this.searchRequired) {
      this.detailTable.deleteAllRows();
      this.setSearchFilterCompleted(false);
      // Reset table status (otherwise, a previous message such as "limited result" message would remain visible)
      this.detailTable.setTableStatus(SearchRequiredTableStatus.info(this.session.text('TooManyRows')));
    } else {
      this.detailTable.reload(Table.ReloadReason.SEARCH);
    }
  }

  protected _createChildPageInternal(row: TableRow): Page {
    // noinspection JSDeprecatedSymbols
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
   * @deprecated use {@link _createChildPage} instead
   */
  createChildPage(row: TableRow): Page {
    return this._createChildPage(row);
  }

  /**
   * Override this method to create a {@link Page} for the given {@link TableRow}.
   *
   * By default, no page is created unless {@link alwaysCreateChildPage} is set to true.
   * In that case, an {@link AutoLeafPageWithNodes} is created.
   */
  protected _createChildPage(row: TableRow): Page {
    return null;
  }

  createDefaultChildPage(row: TableRow): Page {
    return scout.create(AutoLeafPageWithNodes, {
      parent: this.outline,
      row: row
    });
  }

  override ensureLoadChildren(): JQuery.Promise<any> {
    if (this.searchRequired && !this.searchFilterCompleted) {
      // Show table status
      this.ensureDetailTable();
      this._resetTableData();
      return $.resolvedPromise();
    }
    return super.ensureLoadChildren();
  }

  override loadChildren(): JQuery.Promise<any> {
    this.ensureDetailTable();

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

  protected _createSearchFilter(): any {
    return this.getSearchFilter();
  }

  /**
   * Returns the {@link SearchFormTableControl} for the given {@link Table}, or `null` if no {@link SearchFormTableControl} is present.
   */
  protected _findSearchFormTableControl(table: Table): SearchFormTableControl {
    return table?.findTableControl(SearchFormTableControl);
  }

  /**
   * Returns the {@link SearchFormTableControl} for this page, or `null` if no {@link SearchFormTableControl} is present.
   */
  getSearchFormTableControl(): SearchFormTableControl {
    return this._findSearchFormTableControl(this.detailTable);
  }

  /**
   * Returns the search form for this page, or `null` if no search form is present.
   */
  getSearchForm(): Form {
    return this.getSearchFormTableControl()?.form || null;
  }

  /**
   * Returns the exported data of the {@link #getSearchForm search form}, or `undefined` if no search form is present.
   */
  getSearchFilter(): any {
    return this.getSearchForm()?.exportData();
  }

  /**
   * Imports the given data into the {@link #getSearchForm search form}. If no search form is present, nothing happens.
   *
   * @param markAsSaved
   *        If this optional parameter is set to `true`, the form state after the import is marked as the saved state,
   *        i.e. pressing the reset button will revert the form to the new state. Otherwise, the saved state will not be
   *        altered and pressing the reset button will revert the form to whatever was previously the saved state.
   */
  setSearchFilter(searchFilter: any, markAsSaved?: boolean) {
    let searchForm = this.getSearchForm();
    if (!searchForm) {
      return;
    }
    searchForm.setData(searchFilter);
    searchForm.importData();
    if (markAsSaved) {
      searchForm.markAsSaved();
    }
  }

  /**
   * Resets the {@link #getSearchForm search form} to its saved state. If no search form is present, nothing happens.
   */
  resetSearchFilter() {
    this.getSearchForm()?.reset();
  }

  /**
   * Uses {@link SearchFilterTextBuilder} to build a display text for the active search form.
   *
   * @returns the text for the active search form or null if there is no search form.
   */
  async getSearchFilterText(): Promise<string> {
    let form = this.getSearchForm();
    if (!form) {
      return null;
    }
    // If the user modifies search fields but does not hit search, the search text would not reflect the used search filter to load the table.
    // To circumvent this, the actual search filter needs to be imported before building the text.
    let oldData = form.exportData();
    form.importData();

    let text = await scout.create(SearchFilterTextBuilder).build(form);

    // Import the old data to restore the previous search form state.
    form.setData(oldData);
    form.importData();
    return text;
  }

  /**
   * Adds a {@link MaxRowCountContributionDo} to the given request.
   * Typically, this method should be used before sending a request in {@link _loadTableData} to attach the row limit constraints (if existing).
   * The contribution is only added if there is a row limit. Otherwise, the request remains untouched.
   * @example
   * protected override _loadTableData(searchFilter: MyRestrictionDo): JQuery.Promise<MyResponseDo> {
   *   const request = scout.create(MyRequestDo, {
   *       ...
   *       restriction: searchFilter
   *   });
   *   return ajax.postDataObject(url, this._withMaxRowCountContribution(request));
   * }
   * @param dataObject The {@link DoEntity} to which the contribution should be added.
   * @returns the resulting request with the added contribution.
   */
  protected _withMaxRowCountContribution<T>(dataObject: T): T {
    return scout.create(TableMaxResultsHelper).withMaxRowCountContribution(dataObject, this.detailTable, this._reloadReason);
  }

  /**
   * see Java: AbstractPageWithTable#loadChildren that's where the table is reloaded and the tree is rebuilt, called by AbstractTree#P_UIFacade
   */
  loadTableData(reloadReason?: TableReloadReason): JQuery.Promise<any> {
    this.ensureDetailTable();
    this.detailTable.setLoading(true);
    this._reloadReason = reloadReason || this._reloadReason;
    const restoreSelectionInfo = this._getRestoreSelectionInfo();
    const searchFilter = this._createSearchFilter();

    // Ensure the search form data is in sync with the search filter used to load the table data (e.g. required for getSearchFilterText())
    this._updateSearchData(searchFilter);

    const promise = this._loadTableData(searchFilter)
      .then(data => this._onLoadTableDataDone(data, restoreSelectionInfo))
      .catch(error => this._onLoadTableDataFail(error, restoreSelectionInfo));

    // Ensure loading of the detail table is set to false once all loading tasks have been completed.
    // This includes loading the main table data but also column lookup calls.
    this.detailTable.updateBuffer.pushPromise(promise);

    return promise;
  }

  protected _updateSearchData(searchFilter: any) {
    this.getSearchForm()?.setData(searchFilter);
  }

  /**
   * Get info needed to restore the selection after table data was loaded.
   * - {@link RestoreSelectionInfo.restoreSelection} is `true` if a child page of this page is currently selected.
   * - {@link RestoreSelectionInfo.selectedRowKey} is the row key (see {@link TableRow.getKeyValues}) of the row corresponding to the direct child page of this page that is currently selected or a parent of the currently selected page.
   */
  protected _getRestoreSelectionInfo(): RestoreSelectionInfo {
    let restoreSelection = false;
    let selectedRowKey = null;
    if (this.outline.selectedNode()) {
      let node = this.outline.selectedNode();
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
      if (restoreSelection && !this.outline.selectedNode()) {
        let selectedNode = this.detailTable.selectedRow()?.page
          || this.detailTable.getRowByKey(selectedRowKey)?.page
          || this;
        this.outline.selectNode(selectedNode);
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
    return dataObjects.getContribution('scout.LimitedResultInfoContribution', tableData) as LimitedResultInfoContributionDo;
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
  }

  /**
   * This method converts the loaded table data, which can be any object, into table rows.
   * You must override this method unless tableData is already an array of table rows.
   */
  protected _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
    return tableData;
  }

  override getTableRowIdentifier(row: TableRow, allowObjectFallback = false): BookmarkTableRowIdentifierDo {
    return BookmarkSupport.get(this.session).createTableRowIdentifier(this, row, allowObjectFallback);
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
