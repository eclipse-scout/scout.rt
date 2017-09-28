/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.OutlineMediator = function() {
};

scout.OutlineMediator.prototype.init = function(model) {
};

scout.OutlineMediator.prototype._skipEvent = function(page) {
  return page === null || page.getOutline() === null || page.leaf;
};

scout.OutlineMediator.prototype.onTableRowsInserted = function(rows, childPages, pageWithTable) {
  if (this._skipEvent(pageWithTable)) {
    return;
  }
  pageWithTable.getTree().insertNodes(childPages, pageWithTable);
};

scout.OutlineMediator.prototype.onTableRowsDeleted = function(rows, childPages, pageWithTable) {
  if (this._skipEvent(pageWithTable)) {
    return;
  }
  pageWithTable.getTree().deleteNodes(childPages, pageWithTable);
};

scout.OutlineMediator.prototype.onTableRowsUpdated = function(event, pageWithTable) {
  if (this._skipEvent(pageWithTable)) {
    return;
  }

  var pages = pageWithTable.updatePagesFromTableRows(event.rows);
  pageWithTable.getTree().updateNodes(pages);
};

scout.OutlineMediator.prototype.onTableRowAction = function(event, page) {
  var childPage = event.row.page;
  if (!childPage) {
    return;
  }

  var outline = childPage.getOutline();
  if (!outline) {
    return;
  }

  outline.selectNode(childPage);
  outline.setNodeExpanded(childPage, true);
};

scout.OutlineMediator.prototype.onTableRowOrderChanged = function(event, pageWithTable) {
  if (this._skipEvent(pageWithTable)) {
    return;
  }

  var table = event.source;
  var childPages = pageWithTable.pagesForTableRows(table.rows);
  pageWithTable.getOutline().updateNodeOrder(childPages, pageWithTable);
};

scout.OutlineMediator.prototype.onTableFilter = function(event, page) {
  page.getOutline().filter();
};

