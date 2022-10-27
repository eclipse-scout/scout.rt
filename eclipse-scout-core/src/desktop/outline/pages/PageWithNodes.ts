/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Column, MenuBar, Page, scout, Table, TableRow} from '../../../index';
import $ from 'jquery';
import {TableRowActionEvent} from '../../../table/TableEventMap';

export default class PageWithNodes extends Page {

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
    return this._createChildPages().done(childPages => { // FIXME TS can this be changed to then and <any> to void?
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
