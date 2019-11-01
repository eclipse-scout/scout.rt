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
import {MenuBar} from '../../../index';
import {scout} from '../../../index';
import {Page} from '../../../index';
import * as $ from 'jquery';

/**
 * @class
 * @extends Page
 */
export default class PageWithNodes extends Page {

constructor() {
  super();

  this.nodeType = Page.NodeType.NODES;
}


/**
 * @override Page.js
 */
_createTable() {
  var nodeColumn = scout.create('Column', {
    id: 'NodeColumn',
    session: this.session
  });
  var table = scout.create('Table', {
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

_onDetailTableRowAction(event) {
  this.getOutline().mediator.onTableRowAction(event, this);
}

_rebuildDetailTable(childPages) {
  var table = this.detailTable;
  this._unlinkAllTableRows(table.rows);
  table.deleteAllRows();
  var rows = this._createTableRowsForChildPages(childPages);
  table.insertRows(rows);
}

_unlinkAllTableRows(rows) {
  rows.forEach(function(row) {
    if (row.page) {
      row.page.unlinkWithRow(row);
    }
  });
}

_createTableRowsForChildPages(childPages) {
  return childPages.map(function(childPage) {
    var row = scout.create('TableRow', {
      parent: this.detailTable,
      cells: [childPage.text]
    });
    childPage.linkWithRow(row);
    return row;
  }, this);
}

/**
 * @override TreeNode.js
 */
loadChildren() {
  this.childrenLoaded = false;
  return this._createChildPages().done(function(childPages) {
    this._rebuildDetailTable(childPages);
    if (childPages.length > 0) {
      this.getOutline().insertNodes(childPages, this);
    }
    this.childrenLoaded = true;
  }.bind(this));
}

/**
 * Override this method to create child pages for this page. The default impl. returns an empty array.
 * @return {$.Deferred}
 */
_createChildPages() {
  return $.resolvedDeferred(this.childNodes);
}
}
