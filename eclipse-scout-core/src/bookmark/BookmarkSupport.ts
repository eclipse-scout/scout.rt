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
  App, arrays, BaseDoEntity, BookmarkDo, BookmarkDoBuilder, BookmarkDoBuilderModel, BookmarkSupportModel, BookmarkTableRowIdentifierDo, BookmarkTableRowIdentifierDoFactory, ChartTableControlConfigHelper, Constructor, Desktop, IBookmarkDo,
  IBookmarkPageDo, InitModelOf, MaxRowCountContributionDo, MessageBoxes, NodeBookmarkPageDo, objects, ObjectWithType, Outline, OutlineBookmarkDefinitionDo, Page, PageParamDo, PageWithNodes, PageWithTable, scout, Session, Status,
  TableBookmarkPageDo, TableRow, TableUiPreferences, tableUiPreferences
} from '../index';
import $ from 'jquery';

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
   * Returns an instance of {@link BookmarkSupport} for the given {@link Session}. If no instance is registered
   * for the session yet, a new instance is created.
   *
   * @param session Session object providing a desktop. If this is omitted, the first session of the app is used.
   * If the app does not have any active sessions (e.g. during unit testing), this argument is mandatory.
   */
  static get(session?: Session): BookmarkSupport {
    session = session || App.get().sessions[0];
    scout.assertParameter('session', session);
    return objects.getOrSetIfAbsent(BookmarkSupport._INSTANCES, session, session => scout.create(BookmarkSupport, {desktop: session.desktop}));
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

  /**
   * Creates a new bookmark for the specified page, or the {@link Outline#activePage active page} of the {@link Desktop#outline current outline}
   * if no explicit page is specified. By default, {@link CreateBookmarkParam#createTableRowSelections} is set to `false`.
   *
   * @param param Optional parameters to {@link BookmarkDoBuilder}, can be used to override the defaults.
   * @param options Optional settings to change the behavior of this method.
   */
  createBookmark(param?: CreateBookmarkParam, options?: CreateBookmarkOptions): JQuery.Promise<IBookmarkDo> {
    let builder = scout.create(BookmarkDoBuilder, $.extend({
      desktop: this.desktop,
      createTableRowSelections: false
    }, param));
    return builder.build()
      .catch(error => {
        if (scout.nvl(options?.handleErrors, true)) {
          return this.handleCreateBookmarkError(error);
        }
        throw error;
      });
  }

  /**
   * Creates a bookmark for the given page to be used to refresh an outline. It differs from {@link createBookmark}
   * in that the resulting bookmark can contain non-bookmarkable pages and non-serializable data. Additionally,
   * title and description are not returned.
   */
  createBookmarkForRefresh(param?: CreateBookmarkParam, options?: CreateBookmarkOptions): JQuery.Promise<IBookmarkDo> {
    return this.createBookmark($.extend({
      fallbackAllowed: false,
      persistableRequired: false,
      createTitle: false,
      createDescription: false,
      createTablePreferences: false
    }, param), options);
  }

  /**
   * Returns the implementation-specific identifier for the given bookmark, or `undefined` if the bookmark does not
   * have a recognizable identifier.
   */
  getBookmarkId(bookmark: IBookmarkDo): string {
    if (bookmark instanceof BookmarkDo) {
      return bookmark.id;
    }
    return undefined;
  }

  // --------------------------------------

  /**
   * Navigates to the original location of the given bookmark.
   *
   * @param options Optional settings to change the behavior of this method
   */
  activateBookmark(bookmark: IBookmarkDo, options?: ActivateBookmarkOptions): JQuery.Promise<void> {
    return $.when(this._activateBookmarkAsync(bookmark, options));
  }

  // Native-promise version of activateBookmark()
  protected async _activateBookmarkAsync(bookmark: IBookmarkDo, options?: ActivateBookmarkOptions): Promise<void> {
    try {
      if (!(bookmark?.definition instanceof OutlineBookmarkDefinitionDo)) {
        // noinspection ExceptionCaughtLocallyJS
        throw BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE;
      }
      if (this.loading) {
        // noinspection ExceptionCaughtLocallyJS
        throw BookmarkSupport.ERROR_ALREADY_LOADING;
      }
      this.setLoading(true);
      try {
        await this._activateOutlineBookmarkDefinition(bookmark.definition, options);
      } finally {
        this.setLoading(false);
      }
    } catch (error) {
      if (scout.nvl(options?.handleErrors, true)) {
        return this.handleActivateBookmarkError(error);
      }
      throw error;
    }
  }

  protected async _activateOutlineBookmarkDefinition(bookmarkDefinition: OutlineBookmarkDefinitionDo, options: ActivateBookmarkOptions): Promise<void> {
    let outline = this.resolveOutline(bookmarkDefinition.outlineId);
    let pagePath = bookmarkDefinition.pagePath || [];
    let bookmarkedPage = bookmarkDefinition.bookmarkedPage;
    let bookmarkPath = bookmarkedPage ? [...pagePath, bookmarkedPage] : null;

    await this._activateBookmarkPath({
      parentOutline: outline,
      pagePath: bookmarkPath
    }, options);
  }

  /**
   * Navigates to the page specified by the given page path, starting from the given parent page or outline. Every page on the
   * way is resolved and populated according to the bookmark page, but only the last page is selected at the end.
   *
   * @param param Specifies the starting point and the page path to activate from there
   * @param options Optional settings to change the behavior of this method
   */
  activateBookmarkPath(param: ActivateBookmarkPathParam, options?: ActivateBookmarkOptions): JQuery.Promise<void> {
    return $.when(this._activateBookmarkPathAsync(param, options));
  }

  // Native-promise version of activateBookmarkPath()
  protected async _activateBookmarkPathAsync(param: ActivateBookmarkPathParam, options?: ActivateBookmarkOptions): Promise<void> {
    try {
      if (this.loading) {
        // noinspection ExceptionCaughtLocallyJS
        throw BookmarkSupport.ERROR_ALREADY_LOADING;
      }
      this.setLoading(true);
      try {
        await this._activateBookmarkPath(param, options);
      } finally {
        this.setLoading(false);
      }
    } catch (error) {
      if (scout.nvl(options?.handleErrors, true)) {
        return this.handleActivateBookmarkError(error);
      }
      throw error;
    }
  }

  protected async _activateBookmarkPath(param: ActivateBookmarkPathParam, options?: ActivateBookmarkOptions): Promise<void> {
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

    // Check if the currently selected page is a child of 'parentPage'. If yes, change the current selection to parentPage.
    // Otherwise, after reloading the parent page, the selection would be restored (see PageWithTable#restoreSelection).
    // This would cause the child page to be loaded _before_ the search data from the bookmark has been applied, resulting
    // in the wrong data being shown.
    // TODO bsh [js-bookmark] This workaround has a negative side-effect in that the parent page is visible for a short moment. Is there a better solution?
    if (parentPage) {
      let currentPage = outline.selectedNode();
      while (currentPage) {
        if (currentPage === parentPage) {
          outline.selectNode(parentPage);
        }
        currentPage = currentPage.parentNode;
      }
    }

    if (parentPage && parentBookmarkPage) {
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

  /**
   * Handles errors that occurred during bookmark creation.
   */
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

  /**
   * Handles errors that occurred during bookmark activation.
   */
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

  /**
   * Adapts the given target page with the information from the given bookmark.
   * Useful when bookmarked page is opened inline, i.e. in bookmark outline.
   *
   * @param saveState Specifies whether the new state of the page (table configuration, search form) should be the
   * saved state, i.e. resetting to factory settings should revert the page to the state from the bookmark rather
   * than to the original state. The default value is `true`.
   */
  applyBookmarkToPage(page: Page, bookmark: IBookmarkDo, saveState = true) {
    if (!page || !bookmark || !bookmark.definition) {
      return;
    }
    let bookmarkPage = bookmark.definition.bookmarkedPage;
    this._applyBookmarkPage(page, bookmarkPage, saveState);
  }

  /**
   * Same as {@link applyBookmarkToPage}, but also reloads the page. The returned promise is not resolved until the
   * reload is done.
   */
  applyBookmarkToPageAndReload(page: Page, bookmark: IBookmarkDo, saveState = true): JQuery.Promise<void> {
    return $.when(this._applyBookmarkToPageAndReloadAsync(page, bookmark, saveState));
  }

  // Native-promise version of applyBookmarkToPageAndReload()
  protected async _applyBookmarkToPageAndReloadAsync(page: Page, bookmark: IBookmarkDo, saveState = true): Promise<void> {
    if (!page || !bookmark || !bookmark.definition) {
      return;
    }
    let bookmarkPage = bookmark.definition.bookmarkedPage;
    await this._applyBookmarkPageAndReload(page, bookmarkPage, saveState);
  }

  protected async _applyBookmarkPageAndReload(page: Page, bookmarkPage: IBookmarkPageDo, saveState = true): Promise<void> {
    this._applyBookmarkPage(page, bookmarkPage, saveState);

    await page.ensureLoadChildren();
    if (page instanceof PageWithTable && bookmarkPage instanceof TableBookmarkPageDo) {
      this._restoreSelection(page, bookmarkPage.selectedChildRows);
    }
  }

  protected _applyBookmarkPage(page: Page, bookmarkPage: IBookmarkPageDo, saveState = true) {
    if (page instanceof PageWithTable && bookmarkPage instanceof TableBookmarkPageDo) {
      this._applyBookmarkToTablePage(page, bookmarkPage, saveState);
    } else if (page instanceof PageWithNodes && bookmarkPage instanceof NodeBookmarkPageDo) {
      this._applyBookmarkToNodePage(page, bookmarkPage);
    }
  }

  protected _applyBookmarkToTablePage(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveState = true) {
    this._prepareTablePage(page, bookmarkPage, saveState);
  }

  protected _applyBookmarkToNodePage(page: PageWithNodes, bookmarkPage: NodeBookmarkPageDo) {
    // hook-method provided for subclasses
  }

  protected _prepareTablePage(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveState = true) {
    page.ensureDetailTable();

    this._prepareTablePreferences(page, bookmarkPage, saveState);
    this._prepareSearchFilter(page, bookmarkPage, saveState);
    this._prepareChartTableControlState(page, bookmarkPage);
  }

  protected _prepareTablePreferences(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveState: boolean) {
    const table = page.detailTable;
    const prefs = bookmarkPage.tablePreferences;
    const profile = tableUiPreferences.getProfile(prefs, TableUiPreferences.PROFILE_ID_BOOKMARK);

    tableUiPreferences.apply(table, prefs);
    tableUiPreferences.applyProfile(table, profile, {applyUserFilters: true});

    if (saveState) {
      // Save the current state as initial state so the user can revert the table to this state
      table.saveInitialUiPreferences();
    } else {
      // Store applied settings as GLOBAL setting
      tableUiPreferences.storeGlobalProfile(table);
    }
  }

  protected _prepareSearchFilter(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveState: boolean) {
    // Import search data
    page.setSearchFilter(bookmarkPage.searchData, saveState);
    page.setSearchFilterCompleted(bookmarkPage.searchFilterComplete);
    if (page.searchFilterCompleted) {
      page.setSearchRequired(false);
    }

    // Mark page as dirty so ensureChildrenLoaded() will reload the data
    page.childrenLoaded = false;
  }

  protected _prepareChartTableControlState(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
    const helper = scout.create(ChartTableControlConfigHelper);
    helper.importConfig(page, bookmarkPage.chartTableControlConfig);
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
    table.expandParentRows(selectedRows);
    table.selectRows(selectedRows);
  }
}

export type CreateBookmarkParam = Omit<BookmarkDoBuilderModel, 'desktop'>;

export interface CreateBookmarkOptions {
  /**
   * Specifies whether runtime errors should be handled (e.g. by showing a message). The promise will still be rejected.
   * The default value is `true`.
   */
  handleErrors?: boolean;
}

export interface ActivateBookmarkPathParam {
  /**
   * The outline where to activate the page path. If this is omitted, the desktop's active outline is used instead.
   */
  parentOutline?: Outline;
  /**
   * The page where to start activating the page path. If this is omitted, the process starts at the outline root.
   */
  parentPage?: Page;
  /**
   * Optional bookmark information for the specified parent page. If set, this information is applied to parent page.
   */
  parentBookmarkPage?: IBookmarkPageDo;
  /**
   * The list of path elements to activate, beginning at the specified starting point. The element is processed
   * from left to right, i.e. the last entry corresponds to the page that will be selected at the end.
   */
  pagePath?: IBookmarkPageDo[];
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
