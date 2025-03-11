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
  OutlineBookmarkDefinitionDo, Page, PageBookmarkDefinitionDo, PageWithTable, scout, Session, SomeRequired, Status, TableBookmarkPageDo, UuidPool, webstorage
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
    let pageToBookmark = page || this.desktop.outline?.selectedNode();
    let outline = pageToBookmark?.getOutline();

    let outlineId = outline?.getObjectUuidBuilder().buildId();
    if (!outlineId) {
      // throw new VetoException(TEXTS.get("CannotCreateBookmarkAtThisLocation"));
      return $.rejectedPromise(BookmarkSupport.ERROR_MISSING_OUTLINE);
    }

    return this._pageToBookmark(pageToBookmark)
      .then(bookmarkedPage => this._createBookmark(outlineId, pageToBookmark, bookmarkedPage));
  }

  protected _createBookmark(outlineId: string, page: Page, bookmarkedPage: IBookmarkPageDo): JQuery.Promise<BookmarkDo> {
    // Recursive function that returns the pagePath from the root to the given page  (as a promise)
    let buildPagePath = (currentPage: Page, pagePath: IBookmarkPageDo[] = []): JQuery.Promise<IBookmarkPageDo[]> => {
      let parentPage = currentPage.parentNode;
      if (!parentPage) {
        return $.resolvedPromise(pagePath); // done
      }
      return this._pageToBookmark(parentPage, currentPage).then(pathEntry => {
        if (!pathEntry) {
          // non-bookmarkable page, discard entire path
          return $.resolvedPromise(null);
        }
        return buildPagePath(parentPage, [pathEntry, ...pagePath]);
      });
    };

    return buildPagePath(page).then(pagePath => {
      let bookmarkDefinition = null;
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
    });
  }

  // This methode is called from bottom to top. On the first invocation, the childPage is not
  // set, but later calls pass the childPage for resolve the corresponding row of a table page.
  protected _pageToBookmark(page: Page, childPage?: Page): JQuery.Promise<IBookmarkPageDo> {
    if (!page) { // } || !page['bookmarkable']) { // FIXME bsh [js-bookmark] Add 'bookmarkable' flag
      return $.rejectedPromise(BookmarkSupport.ERROR_PAGE_NOT_BOOKMARKABLE);
    }
    if (!page.pageParam) {
      return $.rejectedPromise(BookmarkSupport.ERROR_MISSING_PAGE_PARAM);
    }

    if (page.nodeType === Page.NodeType.NODES) {
      let bookmarkedPage = scout.create(NodeBookmarkPageDo, {
        pageParam: page.pageParam,
        displayText: page.text // FIXME bsh [js-bookmark] Delegate to bookmark adapter
      });
      return $.resolvedPromise(bookmarkedPage);
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
          return $.rejectedPromise(BookmarkSupport.ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER);
        }
      }

      // FIXME bsh [js-bookmark] Only export when requested, see BookmarkDoBuilder#createTableRowSelections
      let selectedChildRowIdentifiers = page.detailTable.selectedRows
        .map(row => page.getTableRowIdentifier(row));

      return $.resolvedPromise()
        .then(() => {
          // Local
          if (page instanceof PageWithTable) {
            return page.getSearchFilter();
          }
          // Remote
          return HybridManager.get(this.session).callActionAndWait('ExportSearchData', undefined,
            scout.create(HybridActionContextElements)
              .withElement('page', HybridActionContextElement.of(page.getOutline(), page))
          );
        })
        .then(searchFilter => {
          let bookmarkedPage = scout.create(TableBookmarkPageDo, {
            pageParam: page.pageParam,
            displayText: page.text, // FIXME bsh [js-bookmark] Delegate to bookmark adapter
            expandedChildRow: expandedChildRowIdentifier,
            selectedChildRows: selectedChildRowIdentifiers,
            searchFilterComplete: true,
            searchData: searchFilter
          });
          return $.resolvedPromise(bookmarkedPage);
        });
    }

    return $.rejectedPromise(BookmarkSupport.ERROR_PAGE_NOT_BOOKMARKABLE);
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

  protected _openBookmarkHybrid(bookmarkDefinition: OutlineBookmarkDefinitionDo): JQuery.Promise<void> {
    let hybridManager = HybridManager.get(this.session);

    // Scout Classic: send the bookmark to the UI server. The client model will first try to resolve
    // as much of the bookmark as it can. The remaining path will then be sent back to the UI using
    // a callback. After that, the hybrid action will end.
    if (hybridManager) {
      // FIXME bsh [js-bookmark] Handle error
      return hybridManager.callActionAndWaitWithContext('ActivateBookmark', {bookmarkDefinition}).then(result => null);
    }

    // Scout JS: resolve everything in the UI, i.e. the entire path is remaining
    return this.openBookmarkLocal({
      parentOutline: null,
      parentPage: null,
      parentBookmarkPage: null,
      pagePath: [...bookmarkDefinition.pagePath, bookmarkDefinition.bookmarkedPage]
    });
  }

  openBookmarkLocal(request: ActivateBookmarkRequest): JQuery.Promise<void> {
    // Check if we are already on the correct outline
    let outline = request.parentOutline || request.parentPage?.getOutline();
    if (!outline || !outline.visible || !outline.enabled) {
      return $.rejectedPromise(BookmarkSupport.ERROR_OUTLINE_NOT_FOUND);
    }
    this.desktop.setOutline(outline);
    if (request.parentPage && request.parentPage.getOutline() !== outline) {
      return $.rejectedPromise(BookmarkSupport.ERROR_PAGE_WRONG_OUTLINE);
    }

    let pagePath = request.pagePath?.slice(); // create copy because array is altered
    let parent = request.parentPage || outline;
    let parentPageDefinition = request.parentBookmarkPage;
    return this._resolveNextPageInPath(pagePath, parent, parentPageDefinition)
      .then(([page, bookmarkPage]) => {
        if (!page) {
          return $.rejectedPromise(BookmarkSupport.ERROR_PAGE_NOT_FOUND);
        }
        if (arrays.hasElements(pagePath)) {
          // Path not fully restored
          page.detailTable?.setTableStatus(Status.error('Loading the favorite has been canceled because the entry cannot be found in this view.')); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResolutionCanceled')
        }
        this._revealPage(page);
      });
  }

  protected _resolveNextPageInPath(pagePath: IBookmarkPageDo[], parent: Outline | Page, parentPageDefinition: IBookmarkPageDo): JQuery.Promise<[Page, IBookmarkPageDo]> {
    let parentPage = parent instanceof Page ? parent : null;

    if (arrays.empty(pagePath)) {
      return $.resolvedPromise([parentPage, parentPageDefinition]); // done!
    }

    let pageDefinition = pagePath.shift();
    return this._resolvePage(pageDefinition, parent, parentPageDefinition)
      .then(page => {
        if (!page) {
          // Unable to find a page that matches the requested page definition. Put it back to the page path (so later
          // code will know that not the entire path was successfully consumed) and return the last known page.
          pagePath.unshift(pageDefinition); // put it back
          return [parentPage, parentPageDefinition]; // done!
        }
        return this._applyBookmarkPage(page, pageDefinition)
          .then(() => this._resolveNextPageInPath(pagePath, page, pageDefinition));
      });
  }

  protected _resolvePage(pageDefinition: IBookmarkPageDo, parent: Page | Outline, parentPageDefinition: IBookmarkPageDo): JQuery.Promise<Page> {
    if (parent instanceof Outline) {
      // Lookup child page by pageParam
      let result = parent.nodes.find(node => node.matchesPageParam(pageDefinition.pageParam));
      return $.resolvedPromise(result);
    }

    if (parent instanceof Page) {
      parent.ensureDetailTable(); // ensure detail table is present, so loadChildren() will load the table data
      return parent.ensureLoadChildren()
        .then(() => {
          if (parent.nodeType === Page.NodeType.TABLE) {
            if (parentPageDefinition instanceof TableBookmarkPageDo) {
              // Lookup child page by parent PK (ignore pageParam)
              let parentRowIdentifier = parentPageDefinition.expandedChildRow;
              let row = parent.detailTable.rows.find(row => {
                let rowIdentifier = parent.getTableRowIdentifier(row);
                return objects.equals(rowIdentifier, parentRowIdentifier);
              });
              if (row) {
                return row.page;
              }
            }
            return null; // not found
          }
          if (parent.nodeType === Page.NodeType.NODES) {
            // Lookup child page by pageParam
            return parent.childNodes.find(node => node.matchesPageParam(pageDefinition.pageParam));
          }
          return null; // not found
        })
        .then((page: Page) => {
          // If we found the page, but it is currently filtered by the parent table, remove the filter and try again.
          // If the row is still not accepted, the filter is apparently a non-user filter which cannot be removed -> assume page not found.
          if (page && !page.filterAccepted && parent.detailTable?.hasUserFilter()) {
            parent.detailTable.resetUserFilter();
            parent.detailTable.setTableStatus(Status.warning('The column filters have been removed during loading of the favorite.')); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResetColumnFilters')
            if (!page.filterAccepted) {
              return null; // still filtered -> not found
            }
          }
          return page;
        });
    }

    return null;
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
    if (!bookmark) {
      return $.resolvedPromise();
    }

    return this._applyBookmarkToPage(page, bookmark)
      .then(() => {
        // prevent bookmarks from being defined recursively. has some nasty side-effects and is ill-defined:
        // FIXME bsh [js-bookmark] Implement
        // m_bookmarkHelper.get().disableBookmarkMenus(targetPage);
      });
  }

  protected _applyBookmarkToPage(page: Page, bookmark: BookmarkDo): JQuery.Promise<void> {
    let bookmarkPage = bookmark.definition.bookmarkedPage;
    return this._applyBookmarkPage(page, bookmarkPage);
  }

  protected _applyBookmarkPage(page: Page, bookmarkPage: IBookmarkPageDo): JQuery.Promise<void> {
    if (page instanceof PageWithTable && bookmarkPage instanceof TableBookmarkPageDo) {
      return this._applyBookmarkToTablePage(page, bookmarkPage);
    }
    // FIXME bsh [js-bookmark] Do we need to handle the "node page" case?
    return $.resolvedPromise();
  }

  protected _applyBookmarkToTablePage(page: PageWithTable, bookmarkPage: TableBookmarkPageDo): JQuery.Promise<void> {
    return this._prepareTablePage(page, bookmarkPage, true)
      .then(() => this._restoreSelection(page, bookmarkPage));
  }

  protected _prepareTablePage(page: PageWithTable, bookmarkPage: TableBookmarkPageDo, saveSearchForm: boolean): JQuery.Promise<void> {
    page.ensureDetailTable(); // FIXME bsh [js-bookmark] Check if this is still necessary if searchFilter is migrated to a property

    // FIXME bsh [js-bookmark] Implement
    // // be careful when changing the order of these, e.g. applying column preferences requires custom columns to be injected first
    // prepareTableCustomizerData(tablePage, tableBookmarkPage);
    // prepareTableColumnPreferences(tablePage, tableBookmarkPage);
    // prepareTileMode(tablePage, tableBookmarkPage);
    // prepareSearchFilter(tablePage, tableBookmarkPage, saveSearchForm);
    // prepareUserFilters(tablePage, tableBookmarkPage);
    // prepareChartTableControlState(tablePage, tableBookmarkPage);
    // prepareShowRelatedCustomerData(tablePage, tableBookmarkPage);

    page.setSearchFilter(bookmarkPage.searchData);
    // FIXME bsh [js-bookmark] searchFilterComplete???
    return page.loadChildren();
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
      table.restoreSelection(selectedKeys);
      // FIXME bsh [js-bookmark] Required? See Table#restoreSelection
      // if (table.hierarchical) {
      //   table.expandParentRows(selectedRows);
      // }
      // table.selectRows(selectedRows);
    }
  }
}

export interface ActivateBookmarkRequest {
  parentOutline: Outline;
  parentPage: Page;
  parentBookmarkPage: IBookmarkPageDo;
  pagePath: IBookmarkPageDo[];
}
