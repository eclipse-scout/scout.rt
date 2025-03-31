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
  App, arrays, BookmarkDo, BookmarkDoBuilder, BookmarkDoBuilderModel, BookmarkSupportModel, BookmarkTableRowIdentifierDo, dataObjects, Desktop, HybridManager, IBookmarkPageDo, InitModelOf, MessageBoxes, NodeBookmarkPageDo, objects,
  ObjectWithType, Outline, OutlineBookmarkDefinitionDo, Page, PageWithNodes, PageWithTable, scout, Session, Status, TableBookmarkPageDo, TableRow, UuidPool, webstorage
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
    // FIXME bsh [js-bookmark] add a better implementation
    this.desktop.setBusy(this.loading);
  }

  // --------------------------------------

  protected _getBookmarkStore(): BookmarkDo[] {
    let raw = webstorage.getItemFromLocalStorage('jswidgets:bookmarks');
    return dataObjects.parse(raw, Array<BookmarkDo>);
  }

  protected _setBookmarkStore(bookmarkStore: BookmarkDo[]) {
    if (!bookmarkStore) {
      webstorage.removeItemFromLocalStorage('jswidgets:bookmarks');
      return;
    }

    webstorage.setItemToLocalStorage('jswidgets:bookmarks', dataObjects.stringify(bookmarkStore));
  }

  // FIXME bsh [js-bookmark] Remove and replace with actual implementation
  storeBookmark(bookmark: BookmarkDo): JQuery.Promise<void> {
    return $.resolvedPromise().then(() => {
      if (!bookmark) {
        return;
      }

      let bookmarkStore = this._getBookmarkStore() || [];
      bookmark.key = bookmark.key || UuidPool.get(this.session).take();
      let index = bookmarkStore.findIndex(b => b.key === bookmark.key);
      if (index === -1) {
        bookmarkStore.push(bookmark);
      } else {
        bookmarkStore[index] = bookmark;
      }
      this._setBookmarkStore(bookmarkStore);

      this.desktop.trigger('bookmarksChanged');
    });
  }

  // FIXME bsh [js-bookmark] Remove and replace with actual implementation
  loadBookmark(key: string): JQuery.Promise<BookmarkDo> {
    return $.resolvedPromise().then(() => {
      let bookmarkStore = this._getBookmarkStore() || [];
      return bookmarkStore.find(b => b.key === key) || null;
    });
  }

  loadAllBookmarks(): JQuery.Promise<BookmarkDo[]> {
    return $.resolvedPromise().then(() => {
      return this._getBookmarkStore() || [];
    });
  }

  storeAllBookmarks(bookmarks: BookmarkDo[]): JQuery.Promise<void> {
    return $.resolvedPromise().then(() => {
      this._setBookmarkStore(bookmarks);
    });
  }

  // --------------------------------------

  createBookmark(options?: Omit<BookmarkDoBuilderModel, 'desktop'>): JQuery.Promise<BookmarkDo> {
    let builder = scout.create(BookmarkDoBuilder, {
      desktop: this.desktop,
      createTableRowSelections: false,
      ...options
    });
    return builder.build();
  }

  createBookmarkForRefresh(options?: Omit<BookmarkDoBuilderModel, 'desktop'>): JQuery.Promise<BookmarkDo> {
    let builder = scout.create(BookmarkDoBuilder, {
      desktop: this.desktop,
      fallbackAllowed: false,
      persistableRequired: false,
      createTitle: false,
      createDescription: false,
      createTablePreferences: false,
      ...options
    });
    return builder.build();
  }

  // --------------------------------------

  activateBookmark(bookmark: BookmarkDo): JQuery.Promise<void> {
    if (this.loading) {
      return $.rejectedPromise(BookmarkSupport.ERROR_ALREADY_LOADING);
    }

    if (!(bookmark?.definition instanceof OutlineBookmarkDefinitionDo)) {
      return $.rejectedPromise(BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE);
    }

    let bookmarkDefinition = bookmark.definition;

    this.setLoading(true);
    return $.resolvedPromise()
      .then(() => this._activateBookmarkHybrid(bookmarkDefinition))
      .always(() => {
        this.setLoading(false);
      });
  }

  protected async _activateBookmarkHybrid(bookmarkDefinition: OutlineBookmarkDefinitionDo): Promise<void> {
    let hybridManager = HybridManager.get(this.session);

    // Scout Classic: send the bookmark to the UI server. The client model will first try to resolve
    // as much of the bookmark as it can. The remaining path will then be sent back to the UI using
    // a callback. After that, the hybrid action will end.
    if (hybridManager) {
      await hybridManager.callActionAndWait('ActivateBookmark', {bookmarkDefinition});
      return;
    }

    // Scout JS: resolve everything in the UI, i.e. the entire path is remaining
    let outline = this.desktop.getOutlines().find(outline => {
      let outlineId = outline?.getObjectUuidBuilder().buildId();
      return outlineId === bookmarkDefinition.outlineId;
    });
    let pagePath = bookmarkDefinition.bookmarkedPage
      ? [...bookmarkDefinition.pagePath || [], bookmarkDefinition.bookmarkedPage]
      : null;
    return this.activateBookmarkLocal({
      parentOutline: outline,
      parentPage: null,
      parentBookmarkPage: null,
      pagePath: pagePath
    });
  }

  activateBookmarkLocal(request: ActivateBookmarkRequest): JQuery.Promise<void> {
    return $.resolvedPromise()
      .then(() => this._activateBookmarkLocal(request));
  }

  protected async _activateBookmarkLocal(request: ActivateBookmarkRequest): Promise<void> {
    // Check if we are already on the correct outline
    let outline = request.parentOutline || request.parentPage?.outline;
    if (!outline || !outline.visible || !outline.enabled) {
      throw BookmarkSupport.ERROR_OUTLINE_NOT_FOUND;
    }
    this.desktop.setOutline(outline);
    // FIXME bsh [js-bookmark] bringOutlineToFront?
    if (request.parentPage && request.parentPage.outline !== outline) {
      throw BookmarkSupport.ERROR_PAGE_WRONG_OUTLINE;
    }

    let parentPage = request.parentPage;
    let parentBookmarkPage = request.parentBookmarkPage;

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

    if (parentPage && parentBookmarkPage && request.applyParentBookmarkPage) {
      this._applyBookmarkPage(parentPage, parentBookmarkPage, false);
    }

    if (arrays.empty(request.pagePath)) {
      this._revealPage(parentPage);
      return; // done!
    }

    let pagePath = request.pagePath.slice(); // create copy because array is altered
    while (arrays.hasElements(pagePath)) {
      let bookmarkPage = pagePath[0];
      let page = await this._resolvePage(outline, parentPage, parentBookmarkPage, bookmarkPage);

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

    if (arrays.hasElements(pagePath)) {
      // Path not fully restored
      parentPage.detailTable.setTableStatus(Status.error('Loading the favorite has been canceled because the entry cannot be found in this view.')); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResolutionCanceled')
    }
  }

  protected async _resolvePage(outline: Outline, parentPage: Page, parentBookmarkPage: IBookmarkPageDo, bookmarkPage: IBookmarkPageDo): Promise<Page> {
    if (parentPage) {

      await parentPage.ensureLoadChildren();

      if (parentPage instanceof PageWithTable && parentBookmarkPage instanceof TableBookmarkPageDo) {
        // Lookup child page by PK in parent table (ignore pageParam)
        let row = parentPage.detailTable.rows.find(row => {
          let rowIdentifier = parentPage.getTableRowIdentifier(row);
          return objects.equals(rowIdentifier, parentBookmarkPage.expandedChildRow);
        });
        if (!row) {
          return null; // not found
        }
        // If we found the page, but it is currently filtered by the parent table, remove the filter and try again.
        // If the row is still not accepted, the filter is apparently a non-user filter which cannot be removed -> assume page not found.
        if (!row.page.filterAccepted && parentPage.detailTable.hasUserFilter()) {
          parentPage.detailTable.resetUserFilter();
          parentPage.detailTable.setTableStatus(Status.warning('The column filters have been removed during loading of the favorite.')); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResetColumnFilters')
          if (!row.page.filterAccepted) {
            return null; // still filtered -> not found
          }
        }
        return row.page;
      }
      if (parentPage instanceof PageWithNodes && parentBookmarkPage instanceof NodeBookmarkPageDo) {
        // Lookup child page by pageParam
        return parentPage.childNodes.find(node => node.matchesPageParam(bookmarkPage.pageParam));
      }
    }

    // Lookup child page by pageParam
    return outline.nodes.find(node => node.matchesPageParam(bookmarkPage.pageParam));
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

  handleActivateBookmarkError(error: any): JQuery.Promise<any> {
    if (error === BookmarkSupport.ERROR_ALREADY_LOADING) {
      return MessageBoxes.openOk(this.desktop, 'Another bookmark is currently loading', Status.Severity.ERROR);
    }
    if (error === BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE) {
      // throw new VetoException(TEXTS.get("CannotOpenBookmarkInOriginalPlace")); FIXME bsh [js-bookmark] NLS
      return MessageBoxes.openOk(this.desktop, 'Bookmark cannot be opened at its original location.', Status.Severity.ERROR);
    }
    if (error === BookmarkSupport.ERROR_OUTLINE_NOT_FOUND) {
      // throw new VetoException(TEXTS.get("BookmarkActivationFailedOutlineNotAvailable", outline == null ? TEXTS.get("Unknown") : outline.getTitle())); FIXME bsh [js-bookmark] NLS
      return MessageBoxes.openOk(this.desktop, 'Outline not found', Status.Severity.ERROR);
    }
    if (error === BookmarkSupport.ERROR_PAGE_NOT_FOUND) {
      return MessageBoxes.openOk(this.desktop, 'There has been an error while loading the favorite.', Status.Severity.ERROR); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResolvingFailed')
    }
    return App.get().errorHandler.handle(error);
  }

  // --------------------------------------

  applyBookmarkToPage(page: Page, bookmark: BookmarkDo, saveSearchForm = true) {
    if (!page || !bookmark || !bookmark.definition) {
      return;
    }
    let bookmarkPage = bookmark.definition.bookmarkedPage;
    this._applyBookmarkPage(page, bookmarkPage, saveSearchForm);
  }

  applyBookmarkToPageAndReload(page: Page, bookmark: BookmarkDo, saveSearchForm = true): JQuery.Promise<void> {
    if (!page || !bookmark || !bookmark.definition) {
      return;
    }
    let bookmarkPage = bookmark.definition.bookmarkedPage;
    return $.resolvedPromise()
      .then(() => this._applyBookmarkPageAndReload(page, bookmarkPage, saveSearchForm));
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
  }

  protected _prepareTableColumnPreferences(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
  }

  protected _prepareTileMode(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
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
  }

  protected _prepareChartTableControlState(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
  }

  protected _prepareShowRelatedCustomerData(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
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

export interface ActivateBookmarkRequest {
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
