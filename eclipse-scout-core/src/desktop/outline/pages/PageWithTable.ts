/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, AutoLeafPageWithNodes, EventHandler, Form, FormTableControl, InitModelOf, ObjectOrModel, Page, PageWithTableModel, scout, Status, Table, TableAllRowsDeletedEvent, TableReloadEvent, TableRow, TableRowActionEvent,
  TableRowOrderChangedEvent, TableRowsDeletedEvent, TableRowsInsertedEvent, TableRowsUpdatedEvent
} from '../../../index';
import $ from 'jquery';

export class PageWithTable extends Page implements PageWithTableModel {
  declare model: PageWithTableModel;

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
    this.alwaysCreateChildPage = false;

    this._tableRowDeleteHandler = this._onTableRowsDeleted.bind(this);
    this._tableRowInsertHandler = this._onTableRowsInserted.bind(this);
    this._tableRowUpdateHandler = this._onTableRowsUpdated.bind(this);
    this._tableRowActionHandler = this._onTableRowAction.bind(this);
    this._tableRowOrderChangeHandler = this._onTableRowOrderChanged.bind(this);
    this._tableDataLoadHandler = this.loadTableData.bind(this);
  }

  override init(model: InitModelOf<this>) {
    super.init(model);
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
    let rows = arrays.ensure(event.rows),
      childPages = rows.map(row => {
        let childPage = row.page;
        childPage.unlinkWithRow(row);
        return childPage;
      });

    this.getOutline().mediator.onTableRowsDeleted(rows, childPages, this);
  }

  /**
   * We must set childNodeIndex on each created childPage because it is required to
   * determine the order of nodes in the tree.
   */
  protected _onTableRowsInserted(event: TableRowsInsertedEvent) {
    if (this.leaf) { // when page is a leaf we do nothing at all
      return;
    }

    let rows = arrays.ensure(event.rows),
      childPages = rows.map(row => this._createChildPageInternal(row));

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
    if (childPage === null && this.alwaysCreateChildPage) {
      childPage = this.createDefaultChildPage(row);
    }
    childPage.linkWithRow(row);
    childPage = childPage.updatePageFromTableRow(row);
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
    return this.loadTableData();
  }

  protected _createSearchFilter(): any {
    // Cast could be wrong as any table control is in the list.
    // But as the FormTableControl does not add new public items except the form and the presence of that is form is ensured in the find() method, it may be fine.
    let controls = this.detailTable.tableControls as FormTableControl[];

    let firstFormTableControl = arrays.find(controls, tableControl => tableControl.form instanceof Form);
    if (firstFormTableControl) {
      return firstFormTableControl.form.exportData();
    }
    return null;
  }

  /**
   * see Java: AbstractPageWithTable#loadChildren that's where the table is reloaded and the tree is rebuilt, called by AbstractTree#P_UIFacade
   */
  loadTableData(): JQuery.Promise<any> {
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
   * This is an asynchronous operation working with a Promise. If table data load is successful,
   * <code>_onLoadTableDataDone(data)</code> will be called. If a failure occurs while loading table
   * data, <code>_onLoadTableDataFail(data)</code> will be called.
   * <p>
   * If you want to return static data, you can return a resolvedPromise:
   * <code>return $.resolvedPromise([{...},{...}]);</code>
   *
   * @param searchFilter The search filter as exported by the search form or null.
   */
  protected _loadTableData(searchFilter: any): JQuery.Promise<any> {
    return $.resolvedPromise();
  }

  /**
   * This method is called when table data load is successful. It should transform the table data
   * object to table rows.
   *
   * @param tableData data loaded by <code>_loadTableData</code>
   */
  protected _onLoadTableDataDone(tableData: any) {
    let rows = this._transformTableDataToTableRows(tableData);
    if (rows && rows.length > 0) {
      this.detailTable.insertRows(rows);
    }
  }

  protected _onLoadTableDataFail(error: any) {
    this.detailTable.setTableStatus(Status.error({
      message: this.session.text('ErrorWhileLoadingData')
    }));
    $.log.error('Failed to load tableData. error=', error);
  }

  protected _onLoadTableDataAlways() {
    this.childrenLoaded = true;
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
