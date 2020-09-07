/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {icons, inspector, MenuBar, Outline, TableRow, TileOutlineOverview, TreeNode} from '../../../index';
import $ from 'jquery';

/**
 * This class is used differently in online and JS-only case. In the online case we only have instances
 * of Page in an outline. The server sets the property <code>nodeType</code> which is used to distinct
 * between pages with tables and pages with nodes in some cases. In the JS only case, Page is an abstract
 * class and is never instantiated directly, instead we always use subclasses of PageWithTable or PageWithNodes.
 * Implementations of these classes contain code which loads table data or child nodes.
 *
 * @class
 * @extends TreeNode
 */
export default class Page extends TreeNode {

  constructor() {
    super();

    /**
     * This property is set by the server, see: JsonOutline#putNodeType.
     */
    this.nodeType = null;
    this.compactRoot = false;
    this.detailTable = null;
    this.detailTableVisible = true;
    this.detailForm = null;
    this.detailFormVisible = true;
    this.detailFormVisibleByUi = true;
    this.navigateButtonsVisible = true;

    /**
     * This property contains the class-name of the form to be instantiated, when createDetailForm() is called.
     */
    this.detailFormType = null;
    this.tableStatusVisible = true;
    /**
     * True to select the page linked with the selected row when the row was selected. May be useful on touch devices.
     */
    this.drillDownOnRowClick = false;
    /**
     * The icon id which is used for icons in the tile outline overview.
     */
    this.overviewIconId = null;
    this.showTileOverview = false;
  }

  /**
   * This enum defines a node-type. This is basically used for the online case where we only have instances
   * of Page, but never instances of PageWithTable or PageWithNodes. The server simply sets a nodeType
   * instead.
   *
   * @type {{NODES: string, TABLE: string}}
   */
  static NodeType = {
    NODES: 'nodes',
    TABLE: 'table'
  };

  /**
   * Override this function to return a detail form which is displayed in the outline when this page is selected.
   * The default impl. returns null.
   */
  createDetailForm() {
    return null;
  }

  /**
   * @override TreeNode.js
   */
  _init(model) {
    super._init(model);
    icons.resolveIconProperty(this, 'overviewIconId');
    this._internalInitTable();
    this._internalInitDetailForm();
  }

  /**
   * @override TreeNode.js
   */
  _destroy() {
    super._destroy();
    if (this.detailTable) {
      this.detailTable.destroy();
    }
    if (this.detailForm) {
      this.detailForm.destroy();
    }
  }

  _internalInitTable() {
    let table = this.detailTable;
    if (table) {
      // this case is used for Scout classic
      table = this.getOutline()._createChild(table);
    } else {
      table = this._createTable();
    }

    this.setDetailTable(table);
  }

  _internalInitDetailForm() {
    let detailForm = this.detailForm;
    if (detailForm) {
      detailForm = this.getOutline()._createChild(detailForm);
    }

    this.setDetailForm(detailForm);
  }

  /**
   * Override this function to create the internal table. Default impl. returns null.
   */
  _createTable() {
    return null;
  }

  /**
   * Override this function to initialize the internal (detail) table. Default impl. delegates
   * <code>filter</code> events to the outline mediator.
   */
  _initTable(table) {
    table.menuBar.setPosition(MenuBar.Position.TOP);
    table.on('filter', this._onTableFilter.bind(this));
    if (this.drillDownOnRowClick) {
      table.on('rowClick', this._onTableRowClick.bind(this));
      table.setMultiSelect(false);
    }
  }

  _ensureDetailForm() {
    if (this.detailForm) {
      return;
    }
    let form = this.createDetailForm();
    if (form && !form.displayParent) {
      form.setDisplayParent(this.getOutline());
    }
    this.setDetailForm(form);
  }

  /**
   * @override
   */
  _decorate() {
    super._decorate();
    if (!this.$node) {
      return;
    }
    if (this.session.inspector) {
      inspector.applyInfo(this, this.$node);
    }
    this.$node.toggleClass('compact-root', this.compactRoot);
    this.$node.toggleClass('has-tile-overview', this.showTileOverview ||
      (this.compactRoot && this.getOutline().detailContent instanceof TileOutlineOverview));
  }

  // see Java: AbstractPage#pageActivatedNotify
  activate() {
    this._ensureDetailForm();
  }

  // see Java: AbstractPage#pageDeactivatedNotify
  deactivate() {
  }

  /**
   * @returns {Outline} the tree / outline / parent instance. it's all the same,
   *     but it's more intuitive to work with the 'outline' when we deal with pages.
   */
  getOutline() {
    return this.parent;
  }

  /**
   * @returns {Array.<Page>} an array of pages linked with the given rows.
   *   The order of the returned pages will be the same as the order of the rows.
   */
  pagesForTableRows(rows) {
    return rows.map(this.pageForTableRow);
  }

  pageForTableRow(row) {
    if (!row.page) {
      throw new Error('Table-row is not linked to a page');
    }
    return row.page;
  }

  setDetailForm(form) {
    this.detailForm = form;
    if (this.detailForm) {
      this.detailForm.setModal(false);
    }
    if (this.detailForm instanceof scout.TileOverviewForm) {
      this.detailForm.setPage(this);
    }
  }

  setDetailTable(table) {
    if (table) {
      this._initTable(table);
      table.setTableStatusVisible(this.tableStatusVisible);
    }
    this.detailTable = table;
  }

  /**
   * Updates relevant properties from the pages linked with the given rows using the method updatePageFromTableRow and returns the pages.
   *
   * @returns {Array.<Page>} pages linked with the given rows.
   */
  updatePagesFromTableRows(rows) {
    return rows.map(row => {
      let page = row.page;
      page.updatePageFromTableRow(row);
      return page;
    });
  }

  /**
   * Updates relevant properties (text, enabled, htmlEnabled) from the page linked with the given row.
   *
   * @returns {Page} page linked with the given row.
   */
  updatePageFromTableRow(row) {
    let page = row.page;
    page.enabled = row.enabled;
    page.text = page.computeTextForRow(row);
    if (row.cells.length >= 1) {
      page.htmlEnabled = row.cells[0].htmlEnabled;
      page.cssClass = row.cells[0].cssClass;
    }
    return page;
  }

  /**
   * This function creates the text property of this page. The default implementation returns the
   * text from the first cell of the given row. It's allowed to ignore the given row entirely, when you override
   * this function.
   *
   * @param {TableRow} row
   */
  computeTextForRow(row) {
    let text = '';
    if (row.cells.length >= 1) {
      text = row.cells[0].text;
    }
    return text;
  }

  /**
   * @returns {object} a page parameter object used to pass to newly created child pages. Sets the parent
   *     to our outline instance and adds optional other properties. Typically you'll pass an
   *     object (entity-key or arbitrary data) to a child page.
   */
  _pageParam(paramProperties) {
    let param = {
      parent: this.getOutline()
    };
    $.extend(param, paramProperties);
    return param;
  }

  reloadPage() {
    let outline = this.getOutline();
    if (outline) {
      this.loadChildren();
    }
  }

  linkWithRow(row) {
    this.row = row;
    row.page = this;
    this.getOutline().trigger('pageRowLink', {
      page: this,
      row: row
    });
  }

  unlinkWithRow(row) {
    delete this.row;
    delete row.page;
  }

  _onTableFilter(event) {
    this.getOutline().mediator.onTableFilter(event, this);
  }

  _onTableRowClick(event) {
    if (!this.drillDownOnRowClick) {
      return;
    }
    let row = event.row;
    let drillNode = this.pageForTableRow(row);
    this.getOutline().selectNode(drillNode);
    this.detailTable.deselectRow(row);
  }
}
