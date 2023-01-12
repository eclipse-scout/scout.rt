/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  ButtonTile, ChildModelOf, EnumObject, Event, EventHandler, EventListener, EventMapOf, EventModel, EventSupport, Form, HtmlComponent, icons, InitModelOf, inspector, MenuBar, Outline, PageEventMap, PageModel, PropertyChangeEvent, scout,
  Table, TableRow, TableRowClickEvent, TileOutlineOverview, TileOverviewForm, TreeNode, Widget
} from '../../../index';
import $ from 'jquery';

/**
 * This class is used differently in online and JS-only case. In the online case we only have instances
 * of Page in an outline. The server sets the property <code>nodeType</code> which is used to distinct
 * between pages with tables and pages with nodes in some cases. In the JS only case, Page is an abstract
 * class and is never instantiated directly, instead we always use subclasses of PageWithTable or PageWithNodes.
 * Implementations of these classes contain code which loads table data or child nodes.
 */
export class Page extends TreeNode implements PageModel {
  declare model: PageModel;
  declare eventMap: PageEventMap;
  declare self: Page;
  declare parent: Outline;
  declare childNodes: Page[];
  declare parentNode: Page;

  /**
   * This property is set by the server, see: JsonOutline#putNodeType.
   */
  nodeType: NodeType;
  compactRoot: boolean;
  detailTable: Table;
  detailTableVisible: boolean;
  detailForm: Form;
  detailFormVisible: boolean;
  detailFormVisibleByUi: boolean;
  navigateButtonsVisible: boolean;
  tableStatusVisible: boolean;
  htmlComp: HtmlComponent;
  /**
   * True to select the page linked with the selected row when the row was selected. May be useful on touch devices.
   */
  drillDownOnRowClick: boolean;
  /**
   * The icon id which is used for icons in the tile outline overview.
   */
  overviewIconId: string;
  showTileOverview: boolean;
  row: TableRow;
  tile: ButtonTile;
  events: EventSupport;

  protected _tableFilterHandler: EventHandler<Event<Table>>;
  protected _tableRowClickHandler: EventHandler<TableRowClickEvent>;
  protected _detailTableModel: ChildModelOf<Table>;
  /** @internal */
  _detailFormModel: ChildModelOf<Form>;

  constructor() {
    super();

    this.nodeType = null;
    this.compactRoot = false;
    this.detailTable = null;
    this.detailTableVisible = true;
    this.detailForm = null;
    this.detailFormVisible = true;
    this.detailFormVisibleByUi = true;
    this.navigateButtonsVisible = true;

    this.tableStatusVisible = true;
    this.drillDownOnRowClick = false;
    this.overviewIconId = null;
    this.showTileOverview = false;
    this.events = new EventSupport();
    this.events.registerSubTypePredicate('propertyChange', (event, propertyName) => event.propertyName === propertyName);
    this._tableFilterHandler = this._onTableFilter.bind(this);
    this._tableRowClickHandler = this._onTableRowClick.bind(this);
    this._detailTableModel = null;
    this._detailFormModel = null;
  }

  /**
   * This enum defines a node-type. This is basically used for the Scout Classic case where we only have instances
   * of Page, but never instances of PageWithTable or PageWithNodes. The server simply sets a nodeType instead.
   */
  static NodeType = {
    NODES: 'nodes',
    TABLE: 'table'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    this._detailTableModel = Page._removePropertyIfLazyLoading(model, 'detailTable') as ChildModelOf<Table>;
    this._detailFormModel = Page._removePropertyIfLazyLoading(model, 'detailForm') as ChildModelOf<Form>;

    super._init(model);
    icons.resolveIconProperty(this, 'overviewIconId');

    // init necessary if the properties are still available (e.g. Scout classic)
    this._internalInitTable();
    this._internalInitDetailForm();
  }

  protected static _removePropertyIfLazyLoading(object: PageModel, name: string): any {
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

  protected override _destroy() {
    super._destroy();
    if (this.detailTable) {
      this.detailTable.destroy();
    }
    if (this.detailForm) {
      this.detailForm.destroy();
    }
  }

  protected _internalInitTable() {
    let tableModel = this.detailTable;
    if (tableModel) {
      // this case is used for Scout classic
      let newDetailTable = this.getOutline()._createChild(tableModel);
      this._setDetailTable(newDetailTable);
    }
  }

  protected _internalInitDetailForm() {
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
   * @returns the created table or null
   */
  createDetailTable(): Table {
    let detailTable = this._createDetailTable();
    if (!detailTable && this._detailTableModel) {
      detailTable = this.getOutline()._createChild(this._detailTableModel);
      this._detailTableModel = null; // no longer needed
    }
    return detailTable;
  }

  /**
   * Override this function to create the internal table. Default impl. returns null.
   */
  protected _createDetailTable(): Table {
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
   * @returns the created form or null
   */
  createDetailForm(): Form {
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
   */
  protected _createDetailForm(): Form {
    return null;
  }

  /**
   * Override this function to initialize the internal detail form.
   * @param form the form to initialize.
   */
  protected _initDetailForm(form: Form) {
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
   * @param form the form to destroy.
   */
  protected _destroyDetailForm(form: Form) {
    if (form instanceof TileOverviewForm) {
      form.setPage(null);
    }
    if (form.owner === this.getOutline()) {
      // in Scout classic the owner is not an outline but the NullWidget.
      // Then destroy is controlled by the backend
      form.destroy();
    }
  }

  /**
   * Override this function to initialize the internal (detail) table.
   * Default impl. delegates filter events to the outline mediator.
   * @param table The table to initialize.
   */
  protected _initDetailTable(table: Table) {
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
   * @param table the table to destroy.
   */
  protected _destroyDetailTable(table: Table) {
    table.off('filter', this._tableFilterHandler);
    table.off('rowClick', this._tableRowClickHandler);
    if (table.owner === this.getOutline()) {
      // in Scout classic the owner is not an outline but the NullWidget.
      // Then destroy is controlled by the backend
      table.destroy();
    }
  }

  /** @internal */
  override _decorate() {
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
    // NOP
  }

  /**
   * @returns the tree / outline / parent instance. it's all the same,
   *     but it's more intuitive to work with the 'outline' when we deal with pages.
   */
  getOutline(): Outline {
    return this.parent;
  }

  /**
   * @returns an array of pages linked with the given rows.
   *   The order of the returned pages will be the same as the order of the rows.
   */
  pagesForTableRows(rows: TableRow[]): Page[] {
    return rows.map(this.pageForTableRow);
  }

  pageForTableRow(row: TableRow): Page {
    if (!row.page) {
      throw new Error('Table-row is not linked to a page');
    }
    return row.page;
  }

  /**
   * @param form The new form
   */
  setDetailForm(form: Form) {
    if (form === this.detailForm) {
      return;
    }
    this._setDetailForm(form);
  }

  protected _setDetailForm(form: Form) {
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
   * @param table The new table
   */
  setDetailTable(table: Table) {
    if (table === this.detailTable) {
      return;
    }
    this._setDetailTable(table);
  }

  protected _setDetailTable(table: Table) {
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
   * @returns pages linked with the given rows.
   */
  updatePagesFromTableRows(rows: TableRow[]): Page[] {
    return rows.map(row => row.page.updatePageFromTableRow(row));
  }

  /**
   * Updates relevant properties (text, enabled, htmlEnabled) from the page linked with the given row.
   *
   * @returns page linked with the given row.
   */
  updatePageFromTableRow(row: TableRow): Page {
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
   */
  computeTextForRow(row: TableRow): string {
    let text = '';
    if (row.cells.length >= 1) {
      text = row.cells[0].text;
    }
    return text;
  }

  /**
   * @returns a page parameter object used to pass to newly created child pages. Sets the parent
   *     to our outline instance and adds optional other properties. Typically, you'll pass an
   *     object (entity-key or arbitrary data) to a child page.
   */
  protected _pageParam<T extends object>(paramProperties: T): T {
    let param = {
      parent: this.getOutline()
    };
    $.extend(param, paramProperties);
    return param as T;
  }

  reloadPage() {
    let outline = this.getOutline();
    if (outline) {
      this.loadChildren();
    }
  }

  linkWithRow(row: TableRow) {
    this.row = row;
    row.page = this;
    this.getOutline().trigger('pageRowLink', {
      page: this,
      row: row
    });
  }

  unlinkWithRow(row: TableRow) {
    delete this.row;
    delete row.page;
  }

  protected _onTableFilter(event: Event<Table>) {
    this.getOutline().mediator.onTableFilter(event, this);
  }

  protected _onTableRowClick(event: TableRowClickEvent) {
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
  triggerPropertyChange<T>(propertyName: string, oldValue: T, newValue: T): PropertyChangeEvent<T, this> {
    scout.assertParameter('propertyName', propertyName);
    return this.trigger('propertyChange', {
      propertyName: propertyName,
      oldValue: oldValue,
      newValue: newValue
    }) as PropertyChangeEvent<T, this>;
  }

  trigger<K extends string & keyof EventMapOf<Page>>(type: K, eventOrModel?: Event<Page> | EventModel<EventMapOf<Page>[K]>): EventMapOf<Page>[K] {
    let event: Event<Page>;
    if (eventOrModel instanceof Event) {
      event = eventOrModel;
    } else {
      event = new Event(eventOrModel);
    }
    event.source = this;
    this.events.trigger(type, event);
    return event;
  }

  one<K extends string & keyof EventMapOf<this>>(type: K, handler: EventHandler<EventMapOf<this>[K] & Event<this>>) {
    this.events.one(type, handler);
  }

  on<K extends string & keyof EventMapOf<this>>(type: K, handler: EventHandler<(EventMapOf<this>)[K] & Event<this>>): EventListener {
    return this.events.on(type, handler);
  }

  off<K extends string & keyof EventMapOf<this>>(type: K, handler?: EventHandler<EventMapOf<this>[K]>) {
    this.events.off(type, handler);
  }
}

export type NodeType = EnumObject<typeof Page.NodeType>;
