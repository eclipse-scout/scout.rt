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
  App, arrays, BookmarkDo, bookmarks, BookmarkSupportModel, dataObjects, Desktop, HybridActionContextElement, HybridActionContextElements, HybridManager, IBookmarkPageDo, InitModelOf, MessageBoxes, NodeBookmarkPageDo, ObjectWithType,
  Outline, OutlineBookmarkDefinitionDo, Page, PageBookmarkDefinitionDo, PageWithTable, scout, Session, SomeRequired, Status, TableBookmarkPageDo, UuidPool, webstorage
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

  createBookmark(): JQuery.Promise<BookmarkDo> {
    let outlineId = this.desktop.outline?.getObjectUuidBuilder().buildId();
    if (!outlineId) {
      // throw new VetoException(TEXTS.get("CannotCreateBookmarkAtThisLocation"));
      return $.rejectedPromise(BookmarkSupport.ERROR_MISSING_OUTLINE);
    }

    let selectedPage = this.desktop.outline.selectedNode();
    return this._pageToBookmark(selectedPage)
      .then(bookmarkedPage => this._createBookmark(outlineId, selectedPage, bookmarkedPage));
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
          // let outline = page.getOutline();
          // return outline.getSearchFilterForPage(page);

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
      .then(page => {
        if (!page) {
          return $.rejectedPromise(BookmarkSupport.ERROR_PAGE_NOT_FOUND);
        }
        this._revealPage(page, pagePath);
      });
  }

  protected _resolveNextPageInPath(pagePath: IBookmarkPageDo[], parent: Outline | Page, parentPageDefinition: IBookmarkPageDo): JQuery.Promise<Page> {
    let parentPage = parent instanceof Page ? parent : null;

    if (arrays.empty(pagePath)) {
      return $.resolvedPromise(parentPage); // done!
    }

    let pageDefinition = pagePath.shift();
    return this._resolvePage(pageDefinition, parent, parentPageDefinition)
      .then((page: Page) => {
        if (!page) {
          // Unable to find a page that matches the requested page definition. Put it back to the page path (so later
          // code will know that not the entire path was successfully consumed) and return the last known page.
          pagePath.unshift(pageDefinition); // put it back
          return parentPage;
        }

        page.activate();

        // Restore selection
        if (page.nodeType === Page.NodeType.TABLE && page.detailTable) {
          let selectedChildRows = pageDefinition instanceof TableBookmarkPageDo ? pageDefinition.selectedChildRows : null;
          if (arrays.hasElements(selectedChildRows)) {
            // FIXME bsh [js-bookmark] Handle hierarchical table, see Table#restoreSelection
            let normalizedRowIdentifiers = selectedChildRows
              .map(bookmarkIdentifier => bookmarks.stringifyNormalized(bookmarkIdentifier));
            let selectedRows = page.detailTable.rows.filter(row => {
              let normalizedRowIdentifier = bookmarks.stringifyNormalized(page.getTableRowIdentifier(row));
              return normalizedRowIdentifiers.includes(normalizedRowIdentifier);
            });
            page.detailTable.selectRows(selectedRows);
          }
        }

        // Apply search filter
        if (page instanceof PageWithTable) {
          if (pageDefinition instanceof TableBookmarkPageDo) {
            page.setSearchFilter(pageDefinition.searchData);
          } else {
            page.resetSearchFilter();
          }
        }

        return page.loadChildren()
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
              // Lookup child page by parent PK (ignore PageParam)
              let parentRowBookmarkIdentifier = parentPageDefinition.expandedChildRow;
              let normalizedParentRowIdentifier = bookmarks.stringifyNormalized(parentRowBookmarkIdentifier);
              let row = parent.detailTable.rows.find(row => {
                let normalizedRowIdentifier = bookmarks.stringifyNormalized(parent.getTableRowIdentifier(row));
                return normalizedRowIdentifier === normalizedParentRowIdentifier;
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
          if (page && !page.filterAccepted && parent.detailTable?.hasUserFilter()) {
            parent.detailTable.resetUserFilter();
            if (!page.filterAccepted) {
              return null; // still filtered
            }
          }
          return page;
        });
    }

    return null;
  }

  protected _revealPage(page: Page, remainingPagePath: IBookmarkPageDo[]) {
    let pathFullyRestored = arrays.empty(remainingPagePath);
    let outline = page.getOutline();

    // expand restored path, expand the target page if it is not a table page
    let expandLeaf = page.nodeType !== Page.NodeType.TABLE;
    this._expandPath(page, expandLeaf);

    outline.deselectAll(); // reselection triggers owner changes of menu in case we come here by execDataChanged --> FIXME bsh [js-bookmark] is this necessary in js?
    outline.selectNode(page);
    outline.revealSelection();

    if (!pathFullyRestored) {
      page.detailTable?.setTableStatus(Status.error('Loading the favorite has been canceled because the entry cannot be found in this view.')); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResolutionCanceled')
    }
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
    if (bookmarkPage instanceof TableBookmarkPageDo) {
      let tablePage = scout.assertInstance(page, PageWithTable);
      return this._applyBookmarkToTablePage(tablePage, bookmarkPage);
    }
    // FIXME bsh [js-bookmark] Do we need to handle the "node page" case?
    return $.resolvedPromise();
  }

  protected _applyBookmarkToTablePage(tablePage: PageWithTable, tableBookmarkPage: TableBookmarkPageDo): JQuery.Promise<void> {
    return this._prepareTablePage(tablePage, tableBookmarkPage, true)
      .then(() => this._restoreSelection(tablePage, tableBookmarkPage));
  }

  protected _prepareTablePage(tablePage: PageWithTable, tableBookmarkPage: TableBookmarkPageDo, saveSearchForm: boolean): JQuery.Promise<void> {
    // FIXME bsh [js-bookmark] Reset table preferences & search form (see CoreBookmarkClientService#openBookmarkInTablePage)
    tablePage.setSearchFilter(tableBookmarkPage.searchData);
    tablePage.resetSearchFilter();
    let promise = tablePage.detailTable.loading ? tablePage.detailTable.when('propertyChange:loading').then(() => null) : $.resolvedPromise();
    return promise.then(() => {
      // FIXME bsh [js-bookmark] Implement
      // // be careful when changing the order of these, e.g. applying column preferences requires custom columns to be injected first
      // prepareTableCustomizerData(tablePage, tableBookmarkPage);
      // prepareTableColumnPreferences(tablePage, tableBookmarkPage);
      // prepareTileMode(tablePage, tableBookmarkPage);
      // prepareSearchFilter(tablePage, tableBookmarkPage, saveSearchForm);
      // prepareUserFilters(tablePage, tableBookmarkPage);
      // prepareChartTableControlState(tablePage, tableBookmarkPage);
      // prepareShowRelatedCustomerData(tablePage, tableBookmarkPage);
    });
  }

  protected _restoreSelection(tablePage: PageWithTable, tableBookmarkPage: TableBookmarkPageDo) {
    // FIXME bsh [js-bookmark] Implement
    // if (bookmarkTablePage.getSelectedChildRows().isEmpty()) {
    //   return;
    // }
    //
    // ITable table = tablePage.getTable();
    // tablePage.ensureChildrenLoaded();
    // Set<BookmarkTableRowIdentifierDo> selectionSet = bookmarkTablePage.getSelectedChildRows();
    // List<ITableRow> rowList = new ArrayList<>();
    // for (ITableRow row : table.getRows()) {
    //   BookmarkTableRowIdentifierDo testSelectedRow = createTestRowTableRowIdentifier(tablePage, row);
    //   if (row.isFilterAccepted() //row must not be filtered out
    //     && (selectionSet.contains(testSelectedRow))) {
    //     rowList.add(row);
    //   }
    // }
    //
    // if (!rowList.isEmpty()) {
    //   table.selectRows(rowList);
    // }
  }
}

export interface ActivateBookmarkRequest {
  parentOutline: Outline;
  parentPage: Page;
  parentBookmarkPage: IBookmarkPageDo;
  pagePath: IBookmarkPageDo[];
}
