/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, InitModelOf, Page, PageWithNodes, PageWithTable, Table, TableRow, TableRowActionEvent, TableRowOrderChangedEvent, TableRowsUpdatedEvent} from '../../index';

export class OutlineMediator {

  init(model: InitModelOf<this>) {
    // NOP
  }

  // ------------------------------
  //   Table -> Tree
  // ------------------------------

  protected _skipEvent(page: Page): boolean {
    return page === null || page.getOutline() === null || page.leaf;
  }

  onTableRowsInserted(rows: TableRow[], childPages: Page[], pageWithTable: PageWithTable) {
    if (this._skipEvent(pageWithTable)) {
      return;
    }

    pageWithTable.getTree().insertNodes(childPages, pageWithTable);
  }

  onTableRowsDeleted(rows: TableRow[], childPages: Page[], pageWithTable: PageWithTable) {
    if (this._skipEvent(pageWithTable)) {
      return;
    }

    pageWithTable.getTree().deleteNodes(childPages, pageWithTable);
  }

  onTableRowsUpdated(event: TableRowsUpdatedEvent, pageWithTable: PageWithTable) {
    if (this._skipEvent(pageWithTable)) {
      return;
    }

    let pages = pageWithTable.updatePagesFromTableRows(event.rows);
    pageWithTable.getTree().updateNodes(pages);
  }

  onTableRowAction(event: TableRowActionEvent, page: Page) {
    if (this._skipEvent(page)) {
      return;
    }

    let drillNode = event.row.page;
    page.getOutline().drillDown(drillNode);
  }

  onTableRowOrderChanged(event: TableRowOrderChangedEvent, pageWithTable: PageWithTable) {
    if (this._skipEvent(pageWithTable)) {
      return;
    }

    let table = event.source;
    let childPages = pageWithTable.pagesForTableRows(table.rows);
    pageWithTable.getOutline().updateNodeOrder(childPages, pageWithTable);
  }

  onTableFilter(event: Event<Table>, page: Page) {
    if (this._skipEvent(page)) {
      return;
    }

    page.getOutline().filter();
  }

  // ------------------------------
  //   Tree -> Table
  // ------------------------------

  onPageSelected(selectedPage: Page) {
    if (!selectedPage || !selectedPage.parentNode) {
      return;
    }

    const table = selectedPage.parentNode.detailTable;
    const row = selectedPage.row;
    if (!table || !row || table !== row.getTable()) {
      return;
    }
    table.selectRow(row);
  }

  onChildPagesChanged(page: Page) {
    // This method is called by the outline when one of the child pages of the given page has been changed.
    // If it is a node page, update the corresponding detail table. If it is a table page, we don't have to
    // do anything, because table pages work in the opposite way (table -> tree, see methods above).
    if (page instanceof PageWithNodes) {
      page.rebuildDetailTableInternal();
    }
  }
}
