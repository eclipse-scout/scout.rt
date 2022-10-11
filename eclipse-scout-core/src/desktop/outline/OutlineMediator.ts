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
import {Event, Page, PageWithTable, Table, TableRow} from '../../index';
import {TableRowActionEvent, TableRowOrderChangedEvent, TableRowsUpdatedEvent} from '../../table/TableEventMap';

export default class OutlineMediator {

  init(model: any) {
    // NOP
  }

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
    let childPage = event.row.page;
    if (!childPage) {
      return;
    }

    let outline = childPage.getOutline();
    if (!outline) {
      return;
    }

    outline.selectNode(childPage);
    outline.setNodeExpanded(childPage, true);
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
    page.getOutline().filter();
  }
}
