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
  App, arrays, BookmarkDo, BookmarkSupportModel, dataObjects, Desktop, HybridActionContextElement, HybridActionContextElements, HybridManager, IBookmarkPageDo, InitModelOf, MessageBoxes, NodeBookmarkPageDo, objects, ObjectWithType, Outline,
  OutlineBookmarkDefinitionDo, Page, PageBookmarkDefinitionDo, PageWithNodes, PageWithTable, scout, Session, SomeRequired, Status, TableBookmarkPageDo, UuidPool, webstorage
} from '../index';

export class BookmarkSupport implements ObjectWithType, BookmarkSupportModel {
  declare model: BookmarkSupportModel;
  declare initModel: SomeRequired<this['model'], 'desktop'>;

  static ERROR_MISSING_OUTLINE = 'missing-outline';
  static ERROR_MISSING_PAGE_PARAM = 'missing-page-param';
  static ERROR_PAGE_NOT_BOOKMARKABLE = 'page-not-bookmarkable';
  static ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER = 'missing-row-bookmark-identifier';
  static ERROR_OUTLINE_NOT_FOUND = 'outline-not-found';
  static ERROR_PAGE_NOT_FOUND = 'page-not-found';
  static ERROR_PAGE_WRONG_OUTLINE = 'page-wrong-outline';

  objectType: string;
  desktop: Desktop;
  loading: boolean;

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

      this.desktop.trigger('bookmarksChanged'); // FIXME bsh [js-bookmark] Improve desktop events
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

  createBookmark(page?: Page): JQuery.Promise<BookmarkDo> {
    return $.resolvedPromise()
      .then(() => this._createBookmark(page));
  }

  protected async _createBookmark(page?: Page): Promise<BookmarkDo> {
    let outline = page?.getOutline() || this.desktop.outline;
    let outlineId = outline?.getObjectUuidBuilder().buildId();
    if (!outlineId) {
      throw BookmarkSupport.ERROR_MISSING_OUTLINE;
    }
    page = page || outline.selectedNode();

    let bookmarkedPage: IBookmarkPageDo = null;
    let pagePath: IBookmarkPageDo[] = [];
    if (page) {
      bookmarkedPage = await this._pageToBookmarkPage(page);

      let parentPage = page.parentNode;
      let childPage = page;
      while (parentPage) {
        let pathEntry = await this._pageToBookmarkPage(parentPage, childPage);
        if (!pathEntry) {
          // non-bookmarkable page, discard entire path
          pagePath = null;
          break;
        }
        // Add bookmarkPage to front of path and repeat for parent page
        pagePath.unshift(pathEntry);
        childPage = parentPage;
        parentPage = parentPage.parentNode;
      }
    }

    let bookmarkDefinition;
    if (pagePath) {
      bookmarkDefinition = scout.create(OutlineBookmarkDefinitionDo, {
        outlineId: outlineId,
        bookmarkedPage: bookmarkedPage,
        pagePath: pagePath
      });
    } else {
      bookmarkDefinition = scout.create(PageBookmarkDefinitionDo, {
        bookmarkedPage: bookmarkedPage
      });
    }
    return scout.create(BookmarkDo, {
      definition: bookmarkDefinition
    });
  }

  // Note: this methode is called multiple times from bottom to top. On the first invocation, the childPage is not set,
  // but later calls pass the childPage for resolving the corresponding row of a table page.
  protected async _pageToBookmarkPage(page: Page, childPage?: Page): Promise<IBookmarkPageDo> {
    if (!page) { // } || !page['bookmarkable']) { // FIXME bsh [js-bookmark] Add 'bookmarkable' flag
      throw BookmarkSupport.ERROR_PAGE_NOT_BOOKMARKABLE;
    }
    if (!page.pageParam) {
      throw BookmarkSupport.ERROR_MISSING_PAGE_PARAM;
    }

    if (page.nodeType === Page.NodeType.NODES) {
      return scout.create(NodeBookmarkPageDo, {
        pageParam: page.pageParam,
        displayText: page.text // FIXME bsh [js-bookmark] Delegate to bookmark adapter
      });
    }

    if (page.nodeType === Page.NodeType.TABLE) {
      let expandedChildRowIdentifier;
      if (childPage) {
        if (childPage.row) {
          // Linked to table row -> get row identifier
          expandedChildRowIdentifier = page.getTableRowIdentifier(childPage.row);
        } else {
          // Not linked to table row -> assume the page param is enough to identify the child page
          // FIXME bsh [js-bookmark] Is this assumption correct? Or do we want to throw an error?
        }
        if (!expandedChildRowIdentifier) { // child row not identifiable
          throw BookmarkSupport.ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER;
        }
      }

      // FIXME bsh [js-bookmark] Only export when requested, see BookmarkDoBuilder#createTableRowSelections
      let selectedChildRowIdentifiers = page.detailTable.selectedRows
        .map(row => page.getTableRowIdentifier(row));

      let searchFilter = await this._getSearchFilter(page);

      return scout.create(TableBookmarkPageDo, {
        pageParam: page.pageParam,
        displayText: page.text, // FIXME bsh [js-bookmark] Delegate to bookmark adapter
        expandedChildRow: expandedChildRowIdentifier,
        selectedChildRows: selectedChildRowIdentifiers,
        searchFilterComplete: true,
        searchData: searchFilter
      });
    }

    throw BookmarkSupport.ERROR_PAGE_NOT_BOOKMARKABLE;
  }

  protected async _getSearchFilter(page: Page): Promise<any> {
    // Local
    if (page instanceof PageWithTable) {
      return page.getSearchFilter();
    }

    // Remote
    return HybridManager.get(this.session).callActionAndWait('ExportSearchData', undefined,
      scout.create(HybridActionContextElements)
        .withElement('page', HybridActionContextElement.of(page.getOutline(), page))
    );
  }

  // --------------------------------------

  openBookmarkInOutline(bookmark: BookmarkDo): JQuery.Promise<void> {
    if (this.loading) {
      MessageBoxes.openOk(this.desktop, 'Another bookmark is currently loading', Status.Severity.ERROR);
      return;
    }

    if (!(bookmark?.definition instanceof OutlineBookmarkDefinitionDo)) {
      // throw new VetoException(TEXTS.get("CannotOpenBookmarkInOriginalPlace")); FIXME bsh [js-bookmark] NLS
      MessageBoxes.openOk(this.desktop, 'Bookmark cannot be opened at its original location.', Status.Severity.ERROR);
      return;
    }

    let bookmarkDefinition = bookmark.definition;

    this.setLoading(true);
    return $.resolvedPromise()
      .then(() => this._openBookmarkHybrid(bookmarkDefinition))
      .catch(err => {
        // FIXME bsh [js-bookmark] Error handling
        if (err === BookmarkSupport.ERROR_OUTLINE_NOT_FOUND) {
          // throw new VetoException(TEXTS.get("BookmarkActivationFailedOutlineNotAvailable", outline == null ? TEXTS.get("Unknown") : outline.getTitle())); FIXME bsh [js-bookmark] NLS
          return MessageBoxes.openOk(this.desktop, 'Outline not found', Status.Severity.ERROR);
        }
        if (err === BookmarkSupport.ERROR_PAGE_NOT_FOUND) {
          return MessageBoxes.openOk(this.desktop, 'There has been an error while loading the favorite.', Status.Severity.ERROR); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResolvingFailed')
        }
        return App.get().errorHandler.handle(err);
      })
      .then(() => {
        this.setLoading(false);
      });
  }

  protected async _openBookmarkHybrid(bookmarkDefinition: OutlineBookmarkDefinitionDo): Promise<void> {
    let hybridManager = HybridManager.get(this.session);

    // Scout Classic: send the bookmark to the UI server. The client model will first try to resolve
    // as much of the bookmark as it can. The remaining path will then be sent back to the UI using
    // a callback. After that, the hybrid action will end.
    if (hybridManager) {
      // FIXME bsh [js-bookmark] Handle error
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
    return this.openBookmarkLocal({
      parentOutline: outline,
      parentPage: null,
      parentBookmarkPage: null,
      pagePath: pagePath
    });
  }

  openBookmarkLocal(request: ActivateBookmarkRequest): JQuery.Promise<void> {
    return $.resolvedPromise()
      .then(() => this._openBookmarkLocal(request));
  }

  protected async _openBookmarkLocal(request: ActivateBookmarkRequest): Promise<void> {
    // Check if we are already on the correct outline
    let outline = request.parentOutline || request.parentPage?.getOutline();
    if (!outline || !outline.visible || !outline.enabled) {
      throw BookmarkSupport.ERROR_OUTLINE_NOT_FOUND;
    }
    this.desktop.setOutline(outline);
    if (request.parentPage && request.parentPage.getOutline() !== outline) {
      throw BookmarkSupport.ERROR_PAGE_WRONG_OUTLINE;
    }

    let pagePath = request.pagePath?.slice(); // create copy because array is altered
    if (arrays.empty(pagePath)) {
      return; // done
    }

    let parentPage = request.parentPage;
    let parentBookmarkPage = request.parentBookmarkPage;

    while (arrays.hasElements(pagePath)) {
      let bookmarkPage = pagePath[0];
      let page = await this._resolvePage(outline, parentPage, parentBookmarkPage, bookmarkPage);

      if (!page) {
        break; // no child page found matching the given bookmarkPage
      }

      await this._applyBookmarkPage(page, bookmarkPage, false);

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
      parentPage.ensureDetailTable(); // ensure detail table is present, so loadChildren() will load the table data
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
    let outline = page.getOutline();

    // expand restored path, expand the target page if it is not a table page
    let expandLeaf = page.nodeType !== Page.NodeType.TABLE;
    this._expandPath(page, expandLeaf);

    outline.deselectAll(); // reselection triggers owner changes of menu in case we come here by execDataChanged --> FIXME bsh [js-bookmark] is this necessary in js?
    outline.selectNode(page);
    outline.revealSelection();
  }

  protected _expandPath(page: Page, expandLeaf: boolean) {
    let outline = page.getOutline();
    if (expandLeaf) {
      outline.expandNode(page, {renderAnimated: false});
    }
    let nodeToExpand = page.parentNode;
    while (nodeToExpand) {
      outline.expandNode(nodeToExpand, {renderAnimated: false});
      nodeToExpand = nodeToExpand.parentNode;
    }
  }

  // --------------------------------------

  applyBookmarkToPage(page: Page, bookmark: BookmarkDo): JQuery.Promise<void> {
    return $.resolvedPromise()
      .then(() => this._applyBookmarkToPage(page, bookmark));
  }

  protected async _applyBookmarkToPage(page: Page, bookmark: BookmarkDo): Promise<void> {
    if (!bookmark) {
      return;
    }
    let bookmarkPage = bookmark.definition.bookmarkedPage;
    await this._applyBookmarkPage(page, bookmarkPage);
  }

  protected async _applyBookmarkPage(page: Page, bookmarkPage: IBookmarkPageDo, saveSearchForm = true): Promise<void> {
    if (page instanceof PageWithTable && bookmarkPage instanceof TableBookmarkPageDo) {
      return this._applyBookmarkToTablePage(page, bookmarkPage, saveSearchForm);
    }
    if (page instanceof PageWithNodes && bookmarkPage instanceof NodeBookmarkPageDo) {
      return this._applyBookmarkToNodePage(page, bookmarkPage);
    }
    // FIXME bsh [js-bookmark] Do we need to handle the "node page" case?
  }

  protected async _applyBookmarkToTablePage(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveSearchForm = true): Promise<void> {
    await this._prepareTablePage(page, bookmarkPage, saveSearchForm);
    this._restoreSelection(page, bookmarkPage);
  }

  protected async _applyBookmarkToNodePage(page: PageWithNodes, bookmarkPage: NodeBookmarkPageDo): Promise<void> {
    // hook-method provided for subclasses
  }

  protected async _prepareTablePage(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveSearchForm = true): Promise<void> {
    page.ensureDetailTable();

    // be careful when changing the order of these, e.g. applying column preferences requires custom columns to be injected first
    await this._prepareTableCustomizerData(page, bookmarkPage);
    await this._prepareTableColumnPreferences(page, bookmarkPage);
    await this._prepareTileMode(page, bookmarkPage);
    await this._prepareSearchFilter(page, bookmarkPage, saveSearchForm);
    await this._prepareUserFilters(page, bookmarkPage);
    await this._prepareChartTableControlState(page, bookmarkPage);
    await this._prepareShowRelatedCustomerData(page, bookmarkPage);
  }

  protected async _prepareTableCustomizerData(page: PageWithTable, bookmarkPage: TableBookmarkPageDo): Promise<void> {
  }

  protected async _prepareTableColumnPreferences(page: PageWithTable, bookmarkPage: TableBookmarkPageDo): Promise<void> {
  }

  protected async _prepareTileMode(page: PageWithTable, bookmarkPage: TableBookmarkPageDo): Promise<void> {
  }

  protected async _prepareSearchFilter(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveSearchForm: boolean): Promise<void> {
    let searchForm = page.getSearchForm();

    let oldSearchData;
    if (searchForm) {
      if (!saveSearchForm) {
        // If the new search data should not be the saved state (i.e. the user can press the "Reset" button to clear
        // the bookmarked search data), remember the original state and reset it after the page has been loaded.
        oldSearchData = searchForm.exportData();
      }
      searchForm.setData(bookmarkPage.searchData);
      searchForm.importData();
    }

    await page.loadChildren();

    if (oldSearchData !== undefined) {
      searchForm.setData(oldSearchData);
    }
  }

  protected async _prepareUserFilters(page: PageWithTable, bookmarkPage: TableBookmarkPageDo): Promise<void> {
  }

  protected async _prepareChartTableControlState(page: PageWithTable, bookmarkPage: TableBookmarkPageDo): Promise<void> {
  }

  protected async _prepareShowRelatedCustomerData(page: PageWithTable, bookmarkPage: TableBookmarkPageDo): Promise<void> {
  }

  protected _restoreSelection(page: PageWithTable, bookmarkPage: TableBookmarkPageDo) {
    if (arrays.hasElements(bookmarkPage.selectedChildRows)) {
      let table = page.detailTable;
      let selectedRowIdentifiers = bookmarkPage.selectedChildRows;
      let selectedRows = table.rows.filter(row => {
        if (!row.filterAccepted) {
          return false; // row must not be filtered out
        }
        let rowIdentifier = page.getTableRowIdentifier(row);
        return selectedRowIdentifiers.some(selectedRowIdentifier => objects.equals(selectedRowIdentifier, rowIdentifier));
      });
      let selectedKeys = selectedRows.map(row => row.getKeyValues());
      // FIXME bsh [js-bookmark] Is this even required or should we just use selectRows()? It seems a bit awkward to convert the rows to keys and then back to rows.
      table.restoreSelection(selectedKeys);
    }
  }
}

export interface ActivateBookmarkRequest {
  parentOutline: Outline;
  parentPage: Page;
  parentBookmarkPage: IBookmarkPageDo;
  pagePath: IBookmarkPageDo[];
}
