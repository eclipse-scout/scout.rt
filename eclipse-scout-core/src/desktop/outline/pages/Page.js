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
import {Event, EventSupport, Form, icons, inspector, MenuBar, scout, TileOutlineOverview, TileOverviewForm, TreeNode, Widget} from '../../../index';
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
    this.events = new EventSupport();
    this.events.registerSubTypePredicate('propertyChange', (event, propertyName) => {
      return event.propertyName === propertyName;
    });
    this._tableFilterHandler = this._onTableFilter.bind(this);
    this._tableRowClickHandler = this._onTableRowClick.bind(this);
    this._detailTableModel = null;
    this._detailFormModel = null;
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
   * @override TreeNode.js
   */
  _init(model) {
    this._detailTableModel = Page._removePropertyIfLazyLoading(model, 'detailTable');
    this._detailFormModel = Page._removePropertyIfLazyLoading(model, 'detailForm');

    super._init(model);
    icons.resolveIconProperty(this, 'overviewIconId');

    // init necessary if the properties are still available (e.g. Scout classic)
    this._internalInitTable();
    this._internalInitDetailForm();
  }

  static _removePropertyIfLazyLoading(object, name) {
    let prop = object[name];
    if (typeof prop === 'string') {
      // Scout Classic: it is an object id -> do not remove it. directly create the widget. lazy loading is done on backend
      return null;
    }
    if (prop instanceof Widget) {
      // it already is a widget. directly use it.
      return null;
    }

    // otherwise: remove the property and return it
    delete object[name];
    return prop;
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
    let tableModel = this.detailTable;
    if (tableModel) {
      // this case is used for Scout classic
      let newDetailTable = this.getOutline()._createChild(tableModel);
      this._setDetailTable(newDetailTable);
    }
  }

  _internalInitDetailForm() {
    let formModel = this.detailForm;
    if (formModel) {
      let newDetailForm = this.getOutline()._createChild(formModel);
      this._setDetailForm(newDetailForm);
    }
  }

  ensureDetailTable() {
    if (this.detailTable) {
      return;
    }
    this.setDetailTable(this.createDetailTable());
  }

  /**
   * Creates the detail table
   * @returns {Table} the created table or null
   */
  createDetailTable() {
    let detailTable = this._createDetailTable();
    if (!detailTable && this._detailTableModel) {
      detailTable = this.getOutline()._createChild(this._detailTableModel);
      this._detailTableModel = null; // no longer needed
    }
    return detailTable;
  }

  /**
   * Override this function to create the internal table. Default impl. returns null.
   *
   * @returns {Table}
   */
  _createDetailTable() {
    return null;
  }

  ensureDetailForm() {
    if (this.detailForm) {
      return;
    }
    this.setDetailForm(this.createDetailForm());
  }

  /**
   * Creates the detail form
   * @returns {Form|*} the created form or null
   */
  createDetailForm() {
    let detailForm = this._createDetailForm();
    if (!detailForm && this._detailFormModel) {
      detailForm = this.getOutline()._createChild(this._detailFormModel);
      this._detailFormModel = null; // no longer needed
    }
    return detailForm;
  }

  /**
   * Override this function to return a detail form which is displayed in the outline when this page is selected.
   * The default implementation returns null.
   *
   * @returns {Form|*}
   */
  _createDetailForm() {
    return null;
  }

  /**
   * Override this function to initialize the internal detail form.
   * @param {Form} form the form to initialize.
   */
  _initDetailForm(form) {
    if (form instanceof Form) {
      form.setModal(false);
      form.setClosable(false);

      form.setDisplayHint(Form.DisplayHint.VIEW);
      form.setDisplayViewId('C');

      form.setShowOnOpen(false);
    }
    if (form instanceof TileOverviewForm) {
      form.setPage(this);
    }
  }

  /**
   * Override this function to destroy the internal (detail) form.
   * @param {Form} form the form to destroy.
   */
  _destroyDetailForm(form) {
    if (form instanceof TileOverviewForm) {
      form.setPage(null);
    }
    if (form.owner === this.getOutline()) {
      // in Scout classic the owner is not an outline but the NullWidget.
      // Then the destroy is controlled by the backend
      form.destroy();
    }
  }

  /**
   * Override this function to initialize the internal (detail) table.
   * Default impl. delegates filter events to the outline mediator.
   * @param {Table} table The table to initialize.
   */
  _initDetailTable(table) {
    table.menuBar.setPosition(MenuBar.Position.TOP);
    table.on('filter', this._tableFilterHandler);
    if (this.drillDownOnRowClick) {
      table.on('rowClick', this._tableRowClickHandler);
      table.setMultiSelect(false);
    }
    table.setTableStatusVisible(this.tableStatusVisible);
  }

  /**
   * Override this function to destroy the internal (detail) table.
   * @param {Table} table the table to destroy.
   */
  _destroyDetailTable(table) {
    table.off('filter', this._tableFilterHandler);
    table.off('rowClick', this._tableRowClickHandler);
    if (table.owner === this.getOutline()) {
      // in Scout classic the owner is not an outline but the NullWidget.
      // Then the destroy is controlled by the backend
      table.destroy();
    }
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
    this.ensureDetailTable();
    this.ensureDetailForm();
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

  /**
   * @param {Form} form The new form
   */
  setDetailForm(form) {
    if (form === this.detailForm) {
      return;
    }
    this._setDetailForm(form);
  }

  _setDetailForm(form) {
    let oldDetailForm = this.detailForm;
    if (oldDetailForm !== form && oldDetailForm instanceof Widget) {
      // must be a widget to be destroyed. At startup in Scout Classic it might be a string (the widget id)
      this._destroyDetailForm(oldDetailForm);
    }
    this.detailForm = form;
    if (form) {
      this._initDetailForm(form);
    }
    this.triggerPropertyChange('detailForm', oldDetailForm, form);
  }

  /**
   * @param {Table} table The new table
   */
  setDetailTable(table) {
    if (table === this.detailTable) {
      return;
    }
    this._setDetailTable(table);
  }

  _setDetailTable(table) {
    let oldDetailTable = this.detailTable;
    if (oldDetailTable !== table && oldDetailTable instanceof Widget) {
      // must be a widget to be destroyed. At startup in Scout Classic it might be a string (the widget id)
      this._destroyDetailTable(oldDetailTable);
    }
    this.detailTable = table;
    if (table) {
      this._initDetailTable(table);
    }
    this.triggerPropertyChange('detailTable', oldDetailTable, table);
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

  /**
   * Triggers a property change for a single property.
   */
  triggerPropertyChange(propertyName, oldValue, newValue) {
    scout.assertParameter('propertyName', propertyName);
    let event = new Event({
      propertyName: propertyName,
      oldValue: oldValue,
      newValue: newValue
    });
    this.trigger('propertyChange', event);
    return event;
  }

  trigger(type, event) {
    event = event || {};
    event.source = this;
    this.events.trigger(type, event);
  }

  one(type, func) {
    this.events.one(type, func);
  }

  on(type, func) {
    return this.events.on(type, func);
  }

  off(type, func) {
    this.events.off(type, func);
  }
}
