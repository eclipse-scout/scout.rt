/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class OutlineMediator {

  init(model) {
  }

  _skipEvent(page) {
    return page === null || page.getOutline() === null || page.leaf;
  }

  onTableRowsInserted(rows, childPages, pageWithTable) {
    if (this._skipEvent(pageWithTable)) {
      return;
    }
    pageWithTable.getTree().insertNodes(childPages, pageWithTable);
  }

  onTableRowsDeleted(rows, childPages, pageWithTable) {
    if (this._skipEvent(pageWithTable)) {
      return;
    }
    pageWithTable.getTree().deleteNodes(childPages, pageWithTable);
  }

  onTableRowsUpdated(event, pageWithTable) {
    if (this._skipEvent(pageWithTable)) {
      return;
    }

    let pages = pageWithTable.updatePagesFromTableRows(event.rows);
    pageWithTable.getTree().updateNodes(pages);
  }

  onTableRowAction(event, page) {
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

  onTableRowOrderChanged(event, pageWithTable) {
    if (this._skipEvent(pageWithTable)) {
      return;
    }

    let table = event.source;
    let childPages = pageWithTable.pagesForTableRows(table.rows);
    pageWithTable.getOutline().updateNodeOrder(childPages, pageWithTable);
  }

  onTableFilter(event, page) {
    page.getOutline().filter();
  }
}
