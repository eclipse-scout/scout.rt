/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

scout.OutlineMediator.prototype.onTableRowsInserted = function(tableRows, childPages, pageWithTable) {
  if (pageWithTable === null || pageWithTable.getTree() === null || pageWithTable.leaf) {
    return;
  }
  pageWithTable.getTree().insertNodes(childPages, pageWithTable);
};

scout.OutlineMediator.prototype.onTableRowAction = function(event, page) {
  var childPage = page.pageForTableRow(event.row);
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

//public void mediateTableRowsUpdated(TableEvent e, IPageWithTable<?> pageWithTable) {
//
//public void mediateTableRowsDeleted(List<? extends IPage> childNodes, IPageWithTable pageWithTable) {
//

