/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, MenuBar, Page, scout, Table, TableRow, TableRowActionEvent} from '../../../index';
import $ from 'jquery';

export class PageWithNodes extends Page {

  constructor() {
    super();
    this.nodeType = Page.NodeType.NODES;
  }

  protected override _createDetailTable(): Table {
    let nodeColumn = scout.create(Column, {
      id: 'NodeColumn',
      session: this.session
    });
    let table = scout.create(Table, {
      parent: this.parent,
      id: 'PageWithNodesTable',
      autoResizeColumns: true,
      headerVisible: false,
      columns: [nodeColumn]
    });
    table.menuBar.setPosition(MenuBar.Position.TOP);
    table.on('rowAction', this._onDetailTableRowAction.bind(this));
    return table;
  }

  protected _onDetailTableRowAction(event: TableRowActionEvent) {
    this.getOutline().mediator.onTableRowAction(event, this);
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
    return this._createChildPages().done(childPages => {
      this._rebuildDetailTable(childPages);
      if (childPages.length > 0) {
        this.getOutline().insertNodes(childPages, this);
      }
      this.childrenLoaded = true;
    });
  }

  /**
   * Override this method to create child pages for this page. The default impl. returns an empty array.
   */
  protected _createChildPages(): JQuery.Promise<Page[]> {
    return $.resolvedPromise(this.childNodes);
  }
}
