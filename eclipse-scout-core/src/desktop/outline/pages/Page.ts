/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, BaseDoEntity, BookmarkSupport, BookmarkTableRowIdentifierDo, ButtonTile, ChildModelOf, Column, comparators, Constructor, dataObjects, DoTypeResolver, EnumObject, Event, EventHandler, EventListener, EventMapOf, EventModel,
  EventSupport, Form, HtmlComponent, InitModelOf, inspector, Menu, MenuBar, MenuOwner, ObjectIdProvider, ObjectOrType, ObjectWithUuid, Outline, PageDetailMenuContributor, PageEventMap, PageModel, ParentTablePageMenuContributor,
  PropertyChangeEvent, RequiredUnlessNotSubclass, scout, SomeRequired, strings, Table, TableRow, TableRowClickEvent, TileOutlineOverview, TileOverviewForm, TreeNode, typeName, UuidPathOptions, Widget
} from '../../../index';

/**
 * This class is used differently in online and JS-only case. In the online case we only have instances
 * of Page in an outline. The server sets the property <code>nodeType</code> which is used to distinct
 * between pages with tables and pages with nodes in some cases. In the JS only case, Page is an abstract
 * class and is never instantiated directly, instead we always use subclasses of PageWithTable or PageWithNodes.
 * Implementations of these classes contain code which loads table data or child nodes.
 */
export class Page extends TreeNode implements PageModel, ObjectWithUuid {
  declare initModel: SomeRequired<this['model'], 'parent'> & PageParamRequiredIfDeclared<this>;
  declare model: PageModel;
  declare eventMap: PageEventMap;
  declare self: Page;
  declare parent: Outline;
  declare childNodes: Page[];
  declare parentNode: Page;

  uuid: string;
  pageParam: PageParamDo;
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
  overviewText: string;
  overviewIconId: string;
  overviewHtmlEnabled: boolean;
  showTileOverview: boolean;
  inheritMenusFromParentTablePage: boolean;
  detailMenuContributors: PageDetailMenuContributor[];
  row: TableRow;
  tile: ButtonTile;
  events: EventSupport;
  pageChanging: number;
  userPreferenceContext: string;

  // Inspector infos (are only available for remote pages)
  modelClass: string;
  classId: string;

  protected _tableFilterHandler: EventHandler<Event<Table>>;
  protected _tableRowClickHandler: EventHandler<TableRowClickEvent>;
  protected _detailTableModel: ChildModelOf<Table>;

  /** @internal */
  _detailFormModel: ChildModelOf<Form>;
  protected _detailMenusChangeHandler: (event: Event<MenuOwner>) => void;

  constructor() {
    super();

    this.uuid = null;
    this.pageParam = null;
    this.nodeType = null;
    this.compactRoot = false;
    this.detailTable = null;
    this.detailTableVisible = true;
    this.detailForm = null;
    this.detailFormVisible = true;
    this.detailFormVisibleByUi = true;
    this.navigateButtonsVisible = true;
    this.modelClass = null;
    this.classId = null;
    this.tableStatusVisible = true;
    this.drillDownOnRowClick = false;
    this.showTileOverview = false;
    this.inheritMenusFromParentTablePage = true;
    this.detailMenuContributors = [];
    this.events = new EventSupport();
    this.events.registerSubTypeProvider('propertyChange', (event: PropertyChangeEvent) => event.propertyName);
    this.pageChanging = 0;
    this._tableFilterHandler = this._onTableFilter.bind(this);
    this._tableRowClickHandler = this._onTableRowClick.bind(this);
    this._detailTableModel = null;
    this._detailFormModel = null;
    this._detailMenusChangeHandler = this._onMenuOwnerMenusChange.bind(this);
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
    try {
      this.setPageChanging(true);
      this._detailTableModel = Page._removePropertyIfLazyLoading(model, 'detailTable') as ChildModelOf<Table>;
      this._detailFormModel = Page._removePropertyIfLazyLoading(model, 'detailForm') as ChildModelOf<Form>;

      super._init(model);
      this._resolveIconIds(['overviewIconId']);
      this._setPageParam(this.pageParam);

      let detailMenuContributors = this._createDetailMenuContributors();
      if (model.detailMenuContributors) {
        detailMenuContributors = [...detailMenuContributors, ...model.detailMenuContributors];
      }
      this._setDetailMenuContributors(detailMenuContributors);

      // init necessary if the properties are still available (e.g. Scout classic)
      this._internalInitTable();
      this._internalInitDetailForm();
    } finally {
      this.setPageChanging(false);
    }
  }

  /**
   * Writes the static model to the page instance and initializes the {@link pageParam}.
   * This allows the {@link PageResolver} to find the correct page without having to initialize it completely.
   * A page initialized with this method does not need to be destroyed.
   * **Important:** Always use {@link scout.create} to create and initialize page instances. This method is *only* intended to be used for page resolving!
   */
  minimalInit() {
    let staticModel = this._jsonModel();
    Object.assign(this, staticModel);
    this._setPageParam(this.pageParam);
  }

  buildUuid(useFallback?: boolean): string {
    return ObjectIdProvider.get().uuid(this, useFallback);
  }

  buildUuidPath(options?: UuidPathOptions): string {
    return ObjectIdProvider.get().uuidPath(this, options);
  }

  setUuid(uuid: string) {
    this.uuid = uuid;
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
    this.trigger('destroying');
    super._destroy();

    // DetailTable/Form may still be the model objects and not the real ones if minimalInit() was called instead of the regular init()
    if (this.detailTable instanceof Table) {
      this.detailTable.destroy();
      this.detailTable = null;
    }
    if (this.detailForm instanceof Form) {
      this.detailForm.destroy();
      this.detailForm = null;
    }
    this.trigger('destroy');
  }

  setOverviewText(overviewText: string) {
    this.overviewText = overviewText;
  }

  setOverviewIconId(overviewIconId: string) {
    this.overviewIconId = overviewIconId;
  }

  setOverviewHtmlEnabled(overviewHtmlEnabled: boolean) {
    this.overviewHtmlEnabled = overviewHtmlEnabled;
  }

  protected _createDetailMenuContributors(): ObjectOrType<PageDetailMenuContributor>[] {
    return [ParentTablePageMenuContributor];
  }

  protected _setDetailMenuContributors(contributors: ObjectOrType<PageDetailMenuContributor>[]) {
    this.detailMenuContributors = (contributors || []).map(contributor => {
      contributor = scout.ensure(contributor);
      contributor.setPage(this);
      return contributor;
    });
  }

  protected _internalInitTable() {
    let tableModel = this.detailTable;
    if (tableModel) {
      // this case is used for Scout classic
      let newDetailTable = this.outline._createChild(tableModel);
      this._setDetailTable(newDetailTable);
    }
  }

  protected _internalInitDetailForm() {
    let formModel = this.detailForm;
    if (formModel) {
      let newDetailForm = this.outline._createChild(formModel);
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
      detailTable = this.outline._createChild(this._detailTableModel);
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
      detailForm = this.outline._createChild(this._detailFormModel);
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
      this._updateDetailFormMenus();
    }
    if (form instanceof TileOverviewForm) {
      form.setPage(this);
    }
  }

  protected _updateDetailMenus() {
    this._updateDetailFormMenus();
    this._updateDetailTableMenus();
  }

  protected _updateDetailFormMenus() {
    this._updateDetailMenusForMenuOwner(this.detailForm?.rootGroupBox);
  }

  protected _updateDetailTableMenus() {
    this._updateDetailMenusForMenuOwner(this.detailTable);
  }

  protected _updateDetailMenusForMenuOwner(menuOwner: MenuOwner) {
    if (!menuOwner) {
      return;
    }

    menuOwner.off('propertyChange:menus', this._detailMenusChangeHandler);

    // This function may be called multiple times -> Remove already contributed menus first to ensure they are not added multiple times
    let menus = menuOwner.menus.filter((menu: ContributedMenu) => !menu.__contributed);
    let adaptedMenus = menus;
    for (const contributor of this.detailMenuContributors) {
      adaptedMenus = contributor.contribute(adaptedMenus, menuOwner);
    }
    // Mark all new menus as contributed
    arrays.diff(adaptedMenus, menus).forEach((newMenu: ContributedMenu) => {
      newMenu.__contributed = true;
    });
    menuOwner.setMenus(adaptedMenus);

    menuOwner.on('propertyChange:menus', this._detailMenusChangeHandler);
  }

  protected _onMenuOwnerMenusChange(event: Event<MenuOwner>) {
    this._updateDetailMenusForMenuOwner(event.source);
  }

  isMenuInheritedFromParentTablePage(menu: Menu): boolean {
    return this.inheritMenusFromParentTablePage;
  }

  /**
   * Override this function to destroy the internal (detail) form.
   * @param form the form to destroy.
   */
  protected _destroyDetailForm(form: Form) {
    if (form instanceof TileOverviewForm) {
      form.setPage(null);
    }
    if (form.owner === this.outline) {
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
    table.uuidParent = this;
    table.userPreferenceContext = this.userPreferenceContext;
    table.menuBar.setPosition(MenuBar.Position.TOP);
    table.on('filter', this._tableFilterHandler);
    if (this.drillDownOnRowClick) {
      table.on('rowClick', this._tableRowClickHandler);
      table.setMultiSelect(false);
    }
    table.setTableStatusVisible(this.tableStatusVisible);
    this._updateDetailTableMenus();
  }

  /**
   * Override this function to destroy the internal (detail) table.
   * @param table the table to destroy.
   */
  protected _destroyDetailTable(table: Table) {
    table.off('filter', this._tableFilterHandler);
    table.off('rowClick', this._tableRowClickHandler);
    if (table.owner === this.outline) {
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
    inspector.applyInfo(this, this.$node);
    this.$node.toggleClass('compact-root', this.compactRoot);
    this.$node.toggleClass('has-tile-overview', this.showTileOverview ||
      (this.compactRoot && this.outline.detailContent instanceof TileOutlineOverview));
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
  get outline(): Outline {
    return this.parent;
  }

  /**
   * Returns an array of pages linked with the given rows.
   * The order of the returned pages corresponds to the order of the rows.
   * Rows that are not linked to a page are ignored.
   * Each page is only returned once even if it is linked with multiple rows.
   */
  pagesForTableRows(rows: TableRow[]): Page[] {
    return rows.map(row => row.page)
      .filter((value, index, array) => value && array.indexOf(value) === index); // remove duplicates
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
      form.one('destroy', () => {
        if (this.detailForm === form) {
          this.detailForm = null;
        }
      });
      this._initDetailForm(form);
    }
    this.triggerPropertyChange('detailForm', oldDetailForm, form);
    this.outline.pageChanged(this);
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
      table.one('destroy', () => {
        if (this.detailTable === table) {
          this.detailTable = null;
        }
      });
      this._initDetailTable(table);
    }
    this.triggerPropertyChange('detailTable', oldDetailTable, table);
    this.outline.pageChanged(this);
  }

  /**
   * Updates relevant properties from the pages linked with the given rows using the method updatePageFromTableRow and
   * returns the pages. Rows that are not linked to a page are ignored.
   *
   * @returns pages linked with the given rows.
   */
  updatePagesFromTableRows(rows: TableRow[]): Page[] {
    return rows
      .filter(row => !!row.page)
      .map(row => row.page.updatePageFromTableRow(row));
  }

  /**
   * Updates relevant properties (text, enabled, htmlEnabled) from the page linked with the given row.
   * Only call this method if {@link TableRow#page} is set!
   *
   * @returns page linked with the given row.
   */
  updatePageFromTableRow(row: TableRow): Page {
    let page = row.page;
    page.enabled = row.enabled;

    // keep in sync with token: [5vv7MGGQ5BQY5NXX7CwJ9tmL4]
    const summaryColumns = page._computeSummaryColumns(row);
    page.text = page.computeTextForRow(row, summaryColumns);

    // get properties from cell of first summary column
    const firstSummaryColumn = summaryColumns[0];
    if (firstSummaryColumn) {
      const cell = firstSummaryColumn.cell(row);
      page.htmlEnabled = cell.htmlEnabled;
      page.cssClass = cell.cssClass;
      page.iconId = cell.iconId || row.iconId;
    }

    return page;
  }

  /**
   * This function creates the text property of this page. The default implementation returns {@link Column#cellText} of all summary columns.
   */
  computeTextForRow(row: TableRow, summaryColumns?: Column[]): string {
    if (!summaryColumns) {
      summaryColumns = this._computeSummaryColumns(row);
    }
    return strings.join(' ', ...summaryColumns.map(summaryColumn => summaryColumn.cellText(row)));
  }

  /**
   * Computes the summary columns of the given {@link TableRow}. The summary columns are
   * <ol>
   *   <li> the {@link Table#compactColumn} if it exists and the {@link Outline} is compact
   *   <li> the {@link Table#summaryColumns}
   *   <li> the first visible column
   * </ol>
   */
  protected _computeSummaryColumns(row: TableRow): Column<any>[] {
    if (!row) {
      return [];
    }

    const table = row.table;

    // use compact column if outline is compact
    if (this.outline.compact && table.compactColumn) {
      return [table.compactColumn];
    }

    const summaryColumns = table.summaryColumns();
    if (summaryColumns.length) {
      return summaryColumns;
    }

    // find the first visible column considering the originally defined column order (ignoring the column order changes the user did)
    const columns = table.visibleColumns(false, true);
    columns.sort((c1, c2) => comparators.NUMERIC.compare(c1.index, c2.index));
    return columns.slice(0, 1);
  }

  /**
   * Returns the `text` property of this page as plain text.
   */
  getDisplayText(): string {
    if (this.htmlEnabled) {
      return strings.plainText(this.text);
    }
    return this.text;
  }

  reloadPage() {
    let outline = this.outline;
    if (outline) {
      this.loadChildren();
    }
  }

  linkWithRow(row: TableRow) {
    this.row = row;
    row.page = this;
    this.outline.trigger('pageRowLink', {
      page: this,
      row: row
    });
  }

  unlinkWithRow(row: TableRow) {
    delete this.row;
    delete row.page;
  }

  protected _onTableFilter(event: Event<Table>) {
    this.outline.mediator.onTableFilter(event, this);
  }

  protected _onTableRowClick(event: TableRowClickEvent) {
    if (!this.drillDownOnRowClick) {
      return;
    }
    if (this.leaf) {
      return;
    }
    this.outline.drillDown(event.row.page);
    event.source.deselectRow(event.row);
  }

  matchesPageParam(pageParam: PageParamDo): boolean {
    return BookmarkSupport.get(this.session).pageParamsMatch(this.pageParam, pageParam);
  }

  protected _setPageParam(pageParam: PageParamDo) {
    if (pageParam) {
      scout.assertInstance(pageParam, PageParamDo);
    } else {
      pageParam = this._computeDummyPageParam();
    }
    this.pageParam = pageParam;
  }

  protected _computeDummyPageParam(): PageParamDo {
    let pageId = this.buildUuid();
    if (pageId) {
      return scout.create(PageIdDummyPageParamDo, {pageId});
    }
    return null;
  }

  /**
   * Returns an identifier for the given row that can be stored in a bookmark or used to find the same row again when the
   * bookmark is activated. Usually, it consists of all primary key values.
   *
   * By default, all components of a row identifier have to be persistable. If one of the primary keys is of an unsupported
   * type, an error is thrown. To return a (non-persistable) {@link BookmarkTableRowIdentifierObjectComponentDo} instead,
   * set the optional argument `allowObjectFallback` to `true`.
   *
   * This method can also return `null`. In that case, the child page is identified by its page param.
   */
  getTableRowIdentifier(row: TableRow, allowObjectFallback = false): BookmarkTableRowIdentifierDo {
    return null;
  }

  setPageChanging(changing: boolean) {
    if (changing) {
      this.pageChanging++;
      return;
    }
    if (this.pageChanging) {
      this.pageChanging--;
    }
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

interface ContributedMenu extends Menu {
  /**
   * @returns true if the menu was contributed which means the original menu list did not contain it.
   */
  __contributed?: boolean;
}

/**
 * A page param contains all parameters that are required to create a {@link Page}.
 * Being able to create a page in a generic way is essential when bookmarks are used.
 *
 * If the application uses bookmarks then each page needs to provide its own page param that inherits from this base class.
 * If the page does not have any parameters, the page param can be omitted.
 *
 * If the application does not use bookmarks, page params are not required.
 *
 * @see BookmarkSupport
 */
export class PageParamDo extends BaseDoEntity {
}

/**
 * Default page param that is used by bookmarks to identify pages that do not provide a {@link PageParamDo}.
 * It stores the page's ID so it can be found again when activating the bookmark.
 */
@typeName('scout.PageIdDummyPageParam')
export class PageIdDummyPageParamDo extends PageParamDo {
  pageId: string;
}

/**
 * If a specific {@link PageParamDo} exists on server side but there is no equivalent on JS side, a {@link BaseDoEntity} would be created.
 * This resolver ensures a real {@link PageParamDo} instance will be created instead of a {@link BaseDoEntity}
 * whenever a data object is deserialized whose type ends with 'PageParam', if there is no explicit page param found.
 *
 * This guarantees all page params are actual instances of {@link PageParamDo}.
 */
export class PageParamDoTypeResolver implements DoTypeResolver {
  resolve(rawObj: Record<string, any>): Constructor<BaseDoEntity> {
    if (rawObj?._type?.endsWith('PageParam')) {
      return PageParamDo;
    }
    return null;
  }
}

dataObjects.doTypeResolvers.push(new PageParamDoTypeResolver());

/**
 * Makes the pageParam of the given page required if the page declares a concrete page param (= a subclass of {@link PageParamDo}).
 */
export type PageParamRequiredIfDeclared<TPage extends Page> = RequiredUnlessNotSubclass<TPage, 'pageParam', PageParamDo>;
