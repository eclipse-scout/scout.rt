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
scout.PageWithTable = function() {
  scout.PageWithTable.parent.call(this);

  this.nodeType = "table";
  this.alwaysCreateChildPage = true; // FIXME [awe] 6.1 - change to default 'false'. Check if AutoLeafPageWithNodes work
};
scout.inherits(scout.PageWithTable, scout.Page);

scout.PageWithTable.prototype._createChildPageInternal = function(tableRow) {
  var childPage = this.createChildPage(tableRow);
  if (childPage === null && this.alwaysCreateChildPage) {
    childPage = this.createDefaultChildPage(tableRow);
  }
  return childPage;
};

/**
 * Override this method to return a specific Page instance for the given table-row.
 * The default impl. returns null, which means a AutoLeaftPageWithNodes instance will be created for the table-row.
 */
scout.PageWithTable.prototype.createChildPage = function(tableRow) {
  return null;
};

scout.PageWithTable.prototype.createDefaultChildPage = function(tableRow) {
  return new scout.AutoLeafPageWithNodes(tableRow);
};

// FIXME [awe] 6.1 - check P_TableListener usage:
// AbstractPageWithTable#P_TableListener hat einen listener auf der table, über die listener wird
// der baum mit der tabelle synchronisiert

/**
 * @override TreeNode.js
 */
scout.PageWithTable.prototype.loadChildren = function() {
  // It's allowed to have no table - but we don't have to load data in that case
  if (!this.detailTable) {
    return $.resolvedDeferred();
  }

  return this.loadTableData()
    .done(function() {
      var childPage, childNodes = [];
      this.detailTable.rows.forEach(function(row) {
        childPage = this._createChildPageInternal(row);
        if (childPage !== null) {
          childNodes.push(childPage);
        }
      }, this);
      this.getOutline().insertNodes(childNodes, this);
    }.bind(this));
};
