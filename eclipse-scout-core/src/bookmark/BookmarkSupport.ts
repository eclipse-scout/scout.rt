/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  ActivateBookmarkDataDo, ActivateBookmarkOptionsDo, App, arrays, BaseDoEntity, BookmarkDoBuilder, BookmarkDoBuilderModel, BookmarkSupportModel, BookmarkTableRowIdentifierDo, BookmarkTableRowIdentifierDoFactory,
  ChartTableControlConfigHelper, Constructor, Desktop, HybridManager, IBookmarkDo, IBookmarkPageDo, InitModelOf, MaxRowCountContributionDo, MessageBoxes, NodeBookmarkPageDo, objects, ObjectWithType, Outline, OutlineBookmarkDefinitionDo,
  Page, PageParamDo, PageWithNodes, PageWithTable, scout, Session, Status, TableBookmarkPageDo, TableRow
} from '../index';

export class BookmarkSupport implements ObjectWithType, BookmarkSupportModel {
  declare model: BookmarkSupportModel;

  protected static _INSTANCES: Map<Session, BookmarkSupport> = new Map();

  static ERROR_ALREADY_LOADING = 'already-loading';
  static ERROR_WRONG_DEFINITION_TYPE = 'wrong-definition-type';
  static ERROR_OUTLINE_NOT_FOUND = 'outline-not-found';
  static ERROR_PAGE_NOT_FOUND = 'page-not-found';
  static ERROR_PAGE_WRONG_OUTLINE = 'page-wrong-outline';

  objectType: string;
  desktop: Desktop;
  loading: boolean;

  // --------------------------------------

  /**
   * Returns an instance of {@link BookmarkSupport} for a specific {@link Session}. If no instance is registered
   * for the session yet, a new instance is created.
   *
   * @param session Session object providing a desktop. If this is omitted, the first session of the app is used.
   * If the app does not have any active sessions (e.g. during unit testing), this argument is mandatory.
   */
  static get(session?: Session): BookmarkSupport {
    session = session || App.get().sessions[0];
    scout.assertParameter('session', session);
    let instance = BookmarkSupport._INSTANCES.get(session);
    if (!instance) {
      instance = scout.create(BookmarkSupport, {
        desktop: session.desktop
      });
      BookmarkSupport._INSTANCES.set(session, instance);
    }
    return instance;
  }

  // --------------------------------------

  constructor() {
    this.desktop = null;
    this.loading = false;
  }

  init(model: InitModelOf<this>) {
    Object.assign(this, model);
    scout.assertValue(this.desktop);
  }

  get session(): Session {
    return this.desktop.session;
  }

  setLoading(loading: boolean) {
    this.loading = loading;
    this.desktop.setBusy(this.loading);
  }

  resolveOutline(outlineId: string) {
    return this.desktop.getOutlines().find(outline => {
      let id = outline.buildUuid();
      return id === outlineId;
    });
  }

  // --------------------------------------

  /**
   * Returns `true` if the given page params are equivalent. Unlike {@link BaseDoEntity#equals}, this method
   * ignores certain data object contributions that are considered to be irrelevant when identifying pages
   * (e.g. {@link MaxRowCountContributionDo}).
   */
  pageParamsMatch(pageParam1: PageParamDo, pageParam2: PageParamDo) {
    if (!pageParam1 && !pageParam2) {
      return true;
    }
    if (!pageParam1 || !pageParam2) {
      return false;
    }

    pageParam1 = this._normalizePageParam(pageParam1);
    pageParam2 = this._normalizePageParam(pageParam2);
    return pageParam1.equals(pageParam2);
  }

  protected _normalizePageParam(pageParam: PageParamDo): PageParamDo {
    pageParam = pageParam.clone();
    for (const contribution of this._getIgnoredContributionClassesForPageParamComparison()) {
      pageParam.removeContribution(contribution);
    }
    return pageParam;
  }

  /**
   * @returns contributions that may be added to page params but are irrelevant when comparing page params
   */
  protected _getIgnoredContributionClassesForPageParamComparison(): Constructor<BaseDoEntity>[] {
    return [MaxRowCountContributionDo];
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
  createTableRowIdentifier(page: PageWithTable, row: TableRow, allowObjectFallback = false): BookmarkTableRowIdentifierDo {
    return scout.create(BookmarkTableRowIdentifierDoFactory).createTableRowIdentifier(page, row, allowObjectFallback);
  }

  // --------------------------------------

  createBookmark(options?: Omit<BookmarkDoBuilderModel, 'desktop'>): JQuery.Promise<IBookmarkDo> {
    let builder = scout.create(BookmarkDoBuilder, {
      desktop: this.desktop,
      createTableRowSelections: false,
      ...options
    });
    return builder.build()
      .catch(error => {
        this.handleCreateBookmarkError(error); // FIXME bsh [js-bookmark] Make this optional
        throw error;
      });
  }

  createBookmarkForRefresh(options?: Omit<BookmarkDoBuilderModel, 'desktop'>): JQuery.Promise<IBookmarkDo> {
    let builder = scout.create(BookmarkDoBuilder, {
      desktop: this.desktop,
      fallbackAllowed: false,
      persistableRequired: false,
      createTitle: false,
      createDescription: false,
      createTablePreferences: false,
      ...options
    });
    return builder.build()
      .catch(error => {
        this.handleCreateBookmarkError(error); // FIXME bsh [js-bookmark] Make this optional
        throw error;
      });
  }

  // --------------------------------------

  activateBookmark(bookmark: IBookmarkDo, options?: ActivateBookmarkOptions): JQuery.Promise<void> {
    return $.when(this._activateBookmark(bookmark, options));
  }

  async _activateBookmark(bookmark: IBookmarkDo, options?: ActivateBookmarkOptions): Promise<void> {
    try {
      if (this.loading) {
        // noinspection ExceptionCaughtLocallyJS
        throw BookmarkSupport.ERROR_ALREADY_LOADING;
      }
      this.setLoading(true);
      await this._activateBookmarkHybrid(bookmark, options);
    } catch (error) {
      if (scout.nvl(options?.handleErrors, true)) {
        this.handleActivateBookmarkError(error);
      }
      throw error;
    } finally {
      this.setLoading(false);
    }
  }

  protected async _activateBookmarkHybrid(bookmark: IBookmarkDo, options?: ActivateBookmarkOptions): Promise<void> {
    if (!(bookmark?.definition instanceof OutlineBookmarkDefinitionDo)) {
      throw BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE;
    }
    let bookmarkDefinition = bookmark.definition;

    // Scout Classic: send the bookmark to the UI server. The client model will first try to resolve
    // as much of the bookmark as it can. The remaining path will then be sent back to the UI using
    // a callback. After that, the hybrid action will end.
    let hybridManager = await HybridManager.get(this.session, true);
    if (hybridManager) {
      let data = scout.create(ActivateBookmarkDataDo, {
        bookmarkDefinition,
        options: options ? scout.create(ActivateBookmarkOptionsDo, options) : undefined
      });
      await hybridManager.callActionAndWait('ActivateBookmark', data);
      return;
    }

    // Scout JS: resolve everything in the UI, i.e. the entire path is remaining
    let outline = this.resolveOutline(bookmarkDefinition.outlineId);
    let pagePath = bookmarkDefinition.bookmarkedPage
      ? [...bookmarkDefinition.pagePath || [], bookmarkDefinition.bookmarkedPage]
      : null;
    return this._activateBookmarkLocal({
      parentOutline: outline,
      parentPage: null,
      parentBookmarkPage: null,
      pagePath: pagePath
    }, options);
  }

  activateBookmarkLocal(param: ActivateBookmarkParam, options?: ActivateBookmarkOptions): JQuery.Promise<void> {
    return $.when(this._activateBookmarkLocal(param, options))
      .catch(error => {
        if (scout.nvl(options?.handleErrors, true)) {
          this.handleActivateBookmarkError(error);
        }
        throw error;
      });
  }

  protected async _activateBookmarkLocal(param: ActivateBookmarkParam, options?: ActivateBookmarkOptions): Promise<void> {
    // Check if we are already on the correct outline
    let outline = param.parentOutline || param.parentPage?.outline;
    if (!outline || !outline.visible || !outline.enabled) {
      throw BookmarkSupport.ERROR_OUTLINE_NOT_FOUND;
    }
    if (scout.nvl(options?.activateOutline, true)) {
      this.desktop.setOutline(outline);
      this.desktop.bringOutlineToFront();
    }
    if (param.parentPage && param.parentPage.outline !== outline) {
      throw BookmarkSupport.ERROR_PAGE_WRONG_OUTLINE;
    }

    let parentPage = param.parentPage;
    let parentBookmarkPage = param.parentBookmarkPage;

    // FIXME bsh [js-bookmark] HACKY-HACKY! Find a better solution.
    if (parentPage) {
      let currentPage = outline.selectedNode();
      while (currentPage) {
        if (currentPage === parentPage) {
          // The currently selected page is a child of 'parentPage'.
          // Remove the current selection to prevent unwanted selection restoration (PageWithTable#restoreSelection), which
          // would trigger the loading of the data before the bookmark search data has been applied.
          outline.selectNode(parentPage);
        }
        currentPage = currentPage.parentNode;
      }
    }

    if (parentPage && parentBookmarkPage && param.applyParentBookmarkPage) {
      this._applyBookmarkPage(parentPage, parentBookmarkPage, false);
    }

    if (arrays.empty(param.pagePath)) {
      this._revealPage(parentPage);
      return; // done!
    }

    let pagePath = param.pagePath.slice(); // create copy because array is altered
    while (arrays.hasElements(pagePath)) {
      let bookmarkPage = pagePath[0];
      let page = await this._resolvePage(outline, parentPage, parentBookmarkPage, bookmarkPage, options);

      if (!page) {
        break; // no child page found matching the given bookmarkPage
      }

      await this._applyBookmarkPageAndReload(page, bookmarkPage, false);

      parentPage = page;
      parentBookmarkPage = bookmarkPage;
      pagePath.shift();
    }

    if (!parentPage) {
      throw BookmarkSupport.ERROR_PAGE_NOT_FOUND;
    }

    this._revealPage(parentPage);

    if (arrays.hasElements(pagePath) && scout.nvl(options?.resetViewAndWarnOnFail, true)) {
      // Path not fully restored
      parentPage.detailTable.setTableStatus(Status.error(this.session.text('BookmarkResolutionCanceled')));
    }
  }

  protected async _resolvePage(outline: Outline, parentPage: Page, parentBookmarkPage: IBookmarkPageDo, bookmarkPage: IBookmarkPageDo, options?: ActivateBookmarkOptions): Promise<Page> {
    if (!parentPage) {
      // Lookup top-level page by page param
      return outline.nodes.find(node => node.matchesPageParam(bookmarkPage.pageParam));
    }

    await parentPage.ensureLoadChildren();

    // If the bookmark contains a row identifier, try to find the corresponding row
    if (parentPage instanceof PageWithTable && parentBookmarkPage instanceof TableBookmarkPageDo && parentBookmarkPage.expandedChildRow) {
      let row = parentPage.detailTable.rows.find(row => {
        let rowIdentifier = parentPage.getTableRowIdentifier(row);
        return objects.equals(rowIdentifier, parentBookmarkPage.expandedChildRow);
      });
      // If we found the row, but it is currently filtered by the parent table, remove the filter and try again.
      // If the row is still not accepted, the filter is apparently a non-user filter which cannot be removed -> assume page not found.
      if (row && !row.filterAccepted && parentPage.detailTable.hasUserFilter() && scout.nvl(options?.resetViewAndWarnOnFail, true)) {
        parentPage.detailTable.resetUserFilter();
        parentPage.detailTable.setTableStatus(Status.warning(this.session.text('BookmarkResetColumnFilters')));
        if (!row.filterAccepted) {
          return null; // still filtered -> not found
        }
      }
      if (row) {
        return row.page;
      }
    }

    // For all other cases, identify the child page by page param (works for both PageWithNodes and PageWithTable).
    return parentPage.childNodes.find(node => node.matchesPageParam(bookmarkPage.pageParam));
  }

  protected _revealPage(page: Page) {
    if (!page) {
      return;
    }
    let outline = page.outline;

    // expand restored path, expand the target page if it is not a table page
    let expandLeaf = page.nodeType !== Page.NodeType.TABLE;
    this._expandPath(page, expandLeaf);

    outline.deselectAll(); // reselection triggers owner changes of menu in case we come here by execDataChanged
    outline.selectNode(page);
    outline.revealSelection();
  }

  protected _expandPath(page: Page, expandLeaf: boolean) {
    let outline = page.outline;
    if (expandLeaf) {
      outline.expandNode(page, {renderAnimated: false});
    }
    let nodeToExpand = page.parentNode;
    while (nodeToExpand) {
      outline.expandNode(nodeToExpand, {renderAnimated: false});
      nodeToExpand = nodeToExpand.parentNode;
    }
  }

  handleCreateBookmarkError(error: any): JQuery.Promise<any> {
    if (scout.isOneOf(error,
      BookmarkDoBuilder.ERROR_MISSING_OUTLINE,
      BookmarkDoBuilder.ERROR_MISSING_PAGE_PARAM,
      BookmarkDoBuilder.ERROR_PAGE_NOT_BOOKMARKABLE,
      BookmarkDoBuilder.ERROR_PAGE_PATH_NOT_BOOKMARKABLE,
      BookmarkDoBuilder.ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER
    )) {
      return MessageBoxes.openOk(this.desktop, this.session.text('CannotCreateBookmarkAtThisLocation'), Status.Severity.ERROR);
    }
    return App.get().errorHandler.handle(error);
  }

  handleActivateBookmarkError(error: any): JQuery.Promise<any> {
    if (error === BookmarkSupport.ERROR_ALREADY_LOADING) {
      $.log.error('Another bookmark is currently loading');
      return; // ignore silently
    }
    if (error === BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE) {
      return MessageBoxes.openOk(this.desktop, this.session.text('BookmarkWrongDefinitionType'), Status.Severity.ERROR);
    }
    if (error === BookmarkSupport.ERROR_OUTLINE_NOT_FOUND) {
      return MessageBoxes.openOk(this.desktop, this.session.text('BookmarkOutlineNotFound'), Status.Severity.ERROR);
    }
    if (error === BookmarkSupport.ERROR_PAGE_NOT_FOUND) {
      return MessageBoxes.openOk(this.desktop, this.session.text('BookmarkResolvingFailed'), Status.Severity.ERROR);
    }
    return App.get().errorHandler.handle(error);
  }

  // --------------------------------------

  applyBookmarkToPage(page: Page, bookmark: IBookmarkDo, saveSearchForm = true) {
    if (!page || !bookmark || !bookmark.definition) {
      return;
    }
    let bookmarkPage = bookmark.definition.bookmarkedPage;
    this._applyBookmarkPage(page, bookmarkPage, saveSearchForm);
  }

  applyBookmarkToPageAndReload(page: Page, bookmark: IBookmarkDo, saveSearchForm = true): JQuery.Promise<void> {
    if (!page || !bookmark || !bookmark.definition) {
      return;
    }
    let bookmarkPage = bookmark.definition.bookmarkedPage;
    return $.when(this._applyBookmarkPageAndReload(page, bookmarkPage, saveSearchForm));
  }

  protected async _applyBookmarkPageAndReload(page: Page, bookmarkPage: IBookmarkPageDo, saveSearchForm = true): Promise<void> {
    this._applyBookmarkPage(page, bookmarkPage, saveSearchForm);

    await page.ensureLoadChildren();
    if (page instanceof PageWithTable && bookmarkPage instanceof TableBookmarkPageDo) {
      this._restoreSelection(page, bookmarkPage.selectedChildRows);
    }
  }

  protected _applyBookmarkPage(page: Page, bookmarkPage: IBookmarkPageDo, saveSearchForm = true) {
    if (page instanceof PageWithTable && bookmarkPage instanceof TableBookmarkPageDo) {
      this._applyBookmarkToTablePage(page, bookmarkPage, saveSearchForm);
    } else if (page instanceof PageWithNodes && bookmarkPage instanceof NodeBookmarkPageDo) {
      this._applyBookmarkToNodePage(page, bookmarkPage);
    }
  }

  protected _applyBookmarkToTablePage(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveSearchForm = true) {
    this._prepareTablePage(page, bookmarkPage, saveSearchForm);
  }

  protected _applyBookmarkToNodePage(page: PageWithNodes, bookmarkPage: NodeBookmarkPageDo) {
    // hook-method provided for subclasses
  }

  protected _prepareTablePage(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveSearchForm = true) {
    page.ensureDetailTable();

    // be careful when changing the order of these, e.g. applying column preferences requires custom columns to be injected first
    this._prepareTableCustomizerData(page, bookmarkPage);
    this._prepareTableColumnPreferences(page, bookmarkPage);
    this._prepareTileMode(page, bookmarkPage);
    this._prepareSearchFilter(page, bookmarkPage, saveSearchForm);
    this._prepareUserFilters(page, bookmarkPage);
    this._prepareChartTableControlState(page, bookmarkPage);
    this._prepareShowRelatedCustomerData(page, bookmarkPage);
  }

  protected _prepareTableCustomizerData(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
    // FIXME bsh [js-bookmark] Implement
  }

  protected _prepareTableColumnPreferences(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
    // FIXME bsh [js-bookmark] Implement
  }

  protected _prepareTileMode(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
    // FIXME bsh [js-bookmark] Implement
    // let prefs = bookmarkPage.tablePreferences;
    // if (prefs) {
    //   page.detailTable.setTileMode(prefs.tileMode);
    // }
  }

  protected _prepareSearchFilter(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveSearchForm: boolean) {
    let searchForm = page.getSearchForm();

    if (searchForm && !searchForm.modelAdapter) {
      // If the new search data should not be the saved state (i.e. the user can press the "Reset" button to clear
      // the bookmarked search data), remember the original state and reset it after the page has been loaded.
      let oldSearchData = saveSearchForm ? undefined : searchForm.exportData();
      searchForm.setData(bookmarkPage.searchData);
      searchForm.importData();
      searchForm.setData(oldSearchData);
      if (saveSearchForm) {
        searchForm.markAsSaved();
      }
    } else {
      // FIXME bsh [js-bookmark] HACKY-HACKY! Replace by searchFilter property on page. How to instruct existing hybrid form to import this again?
      page['__searchData'] = bookmarkPage.searchData;
      page['__searchDataMarkAsSaved'] = saveSearchForm;
    }

    // Mark page so ensureChildrenLoaded() will reload the data
    page.childrenLoaded = false;
  }

  protected _prepareUserFilters(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
    // FIXME bsh [js-bookmark] Implement
  }

  protected _prepareChartTableControlState(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
    const helper = scout.create(ChartTableControlConfigHelper);
    helper.importConfig(page, bookmarkPage.chartTableControlConfig);
  }

  protected _prepareShowRelatedCustomerData(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
    // FIXME bsh [js-bookmark] Implement
  }

  protected _restoreSelection(page: PageWithTable, selectedRowIdentifiers: BookmarkTableRowIdentifierDo[]) {
    page.ensureDetailTable();
    let table = page.detailTable;
    if (!table) {
      return;
    }
    let selectedRows: TableRow[] = [];
    if (arrays.hasElements(selectedRowIdentifiers)) {
      selectedRows = table.rows.filter(row => {
        if (!row.filterAccepted) {
          return false; // row must not be filtered out
        }
        let rowIdentifier = page.getTableRowIdentifier(row);
        return selectedRowIdentifiers.some(selectedRowIdentifier => objects.equals(selectedRowIdentifier, rowIdentifier));
      });
    }
    let selectedKeys = selectedRows.map(row => row.getKeyValues());
    // FIXME bsh [js-bookmark] Is this even required or should we just use selectRows()? It seems a bit awkward to convert the rows to keys and then back to rows.
    table.restoreSelection(selectedKeys);
  }
}

export interface ActivateBookmarkOptions {
  /**
   * Specifies whether the target outline should be activated. The default value is `true`.
   */
  activateOutline?: boolean;
  /**
   * If `true`, the user is warned when the bookmark could not be opened. Useful when a persisted bookmark is activated.
   * The default value is `true`.
   */
  resetViewAndWarnOnFail?: boolean;
  /**
   * Specifies whether runtime errors should be handled (e.g. by showing a message). The promise will still be rejected.
   * The default value is `true`.
   */
  handleErrors?: boolean;
}

export interface ActivateBookmarkParam {
  parentOutline?: Outline;
  parentPage?: Page;
  parentBookmarkPage?: IBookmarkPageDo;
  pagePath?: IBookmarkPageDo[];
  /**
   * If set to true, the {@link parentBookmarkPage} is applied to the {@link parentPage} before processing the
   * remaining {@link pagePath}. This is mainly relevant for hybrid pages that only exist in Java as a hull. When
   * handing over the bookmark activation to the UI to finish, the server could only navigate to the hybrid page,
   * but not apply the page state (e.g. table configuration). Therefore, this has to be done in the UI.
   */
  applyParentBookmarkPage?: boolean;
}
