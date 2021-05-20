/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {MenuBar, Page, scout} from '../../../index';
import $ from 'jquery';

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
  _createDetailTable() {
    let nodeColumn = scout.create('Column', {
      id: 'NodeColumn',
      session: this.session
    });
    let table = scout.create('Table', {
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
    let table = this.detailTable;
    if (!table) {
      return;
    }
    this._unlinkAllTableRows(table.rows);
    table.deleteAllRows();
    let rows = this._createTableRowsForChildPages(childPages);
    table.insertRows(rows);
  }

  _unlinkAllTableRows(rows) {
    rows.forEach(row => {
      if (row.page) {
        row.page.unlinkWithRow(row);
      }
    });
  }

  _createTableRowsForChildPages(childPages) {
    return childPages.map(function(childPage) {
      let row = scout.create('TableRow', {
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
   * @return {$.Deferred}
   */
  _createChildPages() {
    return $.resolvedDeferred(this.childNodes);
  }
}
