/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Column, MenuBar, Page, scout, Table, TableReloadEvent, TableRow, TableRowActionEvent} from '../../../index';
import $ from 'jquery';

export class PageWithNodes extends Page {

  declare reloadable: boolean;

  constructor() {
    super();
    this.nodeType = Page.NodeType.NODES;
    this.reloadable = false;
  }

  protected override _createDetailTable(): Table {
    let table = scout.create(Table, {
      parent: this.parent,
      id: 'PageWithNodesTable',
      autoResizeColumns: true,
      headerVisible: false,
      hasReloadHandler: this.reloadable,
      columns: [{
        id: 'NodeColumn',
        objectType: Column
      }]
    });
    table.menuBar.setPosition(MenuBar.Position.TOP);
    table.on('rowAction', this._onDetailTableRowAction.bind(this));
    table.on('reload', this._onDetailTableReload.bind(this));
    return table;
  }

  protected override _initDetailTable(table: Table) {
    super._initDetailTable(table);
    // In case the child pages were loaded while the page was NOT selected, the detail table will now still be null.
    // This is the intended behavior. The detail table should not be needed to load the child pages. But when we
    // finally create it, it should be populated with rows according to the loaded child pages. This is normally
    // done in PageWithNodes#loadChildren. But when the detail table is created later, we have to manually create
    // them here. If the child pages are not yet loaded, this has no effect.
    this.rebuildDetailTableInternal();
  }

  protected _onDetailTableRowAction(event: TableRowActionEvent) {
    this.outline.mediator.onTableRowAction(event, this);
  }

  protected _onDetailTableReload(event: TableReloadEvent) {
    this.reloadPage();
  }

  protected _rebuildDetailTable(childPages: Page[]) {
    let table = this.detailTable;
    if (!table) {
      return;
    }
    this._unlinkAllTableRows(table.rows);
    table.deleteAllRows();
    let rows = this._createTableRowsForChildPages(childPages);
    table.insertRows(rows);
  }

  /** @internal */
  rebuildDetailTableInternal() {
    if (this.childrenLoaded) {
      this._rebuildDetailTable(this.childNodes);
    }
  }

  protected _unlinkAllTableRows(rows: TableRow[]) {
    rows.forEach(row => {
      if (row.page) {
        row.page.unlinkWithRow(row);
      }
    });
  }

  protected _createTableRowsForChildPages(childPages: Page[]): TableRow[] {
    return childPages.map(childPage => {
      let row = scout.create(TableRow, {
        parent: this.detailTable,
        cells: [childPage.text]
      });
      childPage.linkWithRow(row);
      return row;
    });
  }

  override loadChildren(): JQuery.Promise<any> {
    this.childrenLoaded = false;
    return this._createChildPages().then(childPages => {
      this._rebuildDetailTable(childPages);
      this.outline.deleteNodes(arrays.diff(this.childNodes, childPages), this);
      this.outline.insertNodes(childPages, this);
      this.childrenLoaded = true;
    });
  }

  /**
   * Override this method to create child pages for this page. The default impl. returns the current `childNodes` list.
   */
  protected _createChildPages(): JQuery.Promise<Page[]> {
    return $.resolvedPromise(this.childNodes);
  }
}
