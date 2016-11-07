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
scout.Page = function() {
  scout.Page.parent.call(this);

  this.detailTable;
  this.detailTableVisible = true;
  this.detailForm;
  this.detailFormVisible = true;

  /**
   * This property contains the class-name of the form to be instantiated, when createDetailForm() is called.
   */
  this.detailFormType = null;
  this.tableStatusVisible = true;
};
scout.inherits(scout.Page, scout.TreeNode);

/**
 * Override this function to return a detail form which is displayed in the outline when this page is selected.
 * The default impl. returns null.
 */
scout.Page.prototype.createDetailForm = function() {
  return null;
};

/**
 * @override TreeNode.js
 */
scout.Page.prototype._init = function(model) {
  scout.Page.parent.prototype._init.call(this, model);
  if (model.detailTable) { // FIXME [awe] 6.1 - try to get rid of this switch (required for case when server sends detailTable)
    this.detailTable = model.detailTable;
  } else {
    var table = this._createTable();
    this._initTable(table);
    this.detailTable = table;
    if (this.detailTable) {
      this.detailTable.setTableStatusVisible(this.tableStatusVisible);
    }
  }
};

/**
 * Override this function to create the internal table. Default impl. returns null.
 */
scout.Page.prototype._createTable = function() {
  return null;
};

/**
 * Override this function to initialize the internal (detail) table. Default impl. does nothing.
 */
scout.Page.prototype._initTable = function(table) {
  // NOP
};

scout.Page.prototype._linkTableRowWithPage = function(tableRow, page) {
  tableRow.page = page;
  page.tableRow = tableRow;
};

scout.Page.prototype._unlinkTableRowWithPage = function(tableRow, page) {
  delete tableRow.page;
  delete page.tableRow;
};

scout.Page.prototype._ensureDetailForm = function() {
  if (this.detailForm) {
    return;
  }
  this.detailForm = this.createDetailForm();
};

// see Java: AbstractPage#pageActivatedNotify
scout.Page.prototype.activate = function() {
  this._ensureDetailForm();
};

// see Java: AbstractPage#pageDeactivatedNotify
scout.Page.prototype.deactivate = function() {
};

/**
 * @returns The tree / outline / parent instance. it's all the same, but it's more
 *     intuitive to work with the 'outline' when we deal with pages.
 */
scout.Page.prototype.getOutline = function() {
  return this.parent;
};

/**
 * @returns an array of child pages for the given table rows. The order of the child pages is the same as the order of the rows.
 *   which means this function can be used to sort child pages in the same order as the table.
 */
scout.Page.prototype.pagesForTableRows = function(tableRows) {
  return tableRows.map(function(tableRow) {
    return tableRow.page;
  }, this);
};

/**
 * @returns a page parameter object used to pass to newly created child pages. Sets the parent
 *     to our outline instance and adds optional other properties. Typically you'll pass an
 *     object (entity-key or arbitrary data) to a child page.
 */
scout.Page.prototype._pageParam = function(paramProperties) {
  var param = {
    parent: this.getOutline()
  };
  $.extend(param, paramProperties);
  return param;
};
