/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  ActivateBookmarkResultDo, App, arrays, BaseDoEntity, BookmarkDo, bookmarks, BookmarkSupportModel, BookmarkTableRowIdentifierDo, Desktop, DoRegistry, HybridActionContextElement, HybridManager, IBookmarkPageDo, InitModelOf, MessageBoxes,
  NodeBookmarkPageDo, objects, ObjectWithType, Outline, OutlineBookmarkDefinitionDo, Page, PageBookmarkDefinitionDo, PageResolver, PageWithTable, scout, Session, SomeRequired, Status, TableBookmarkPageDo, UuidPool, webstorage
} from '../index';

export class BookmarkSupport implements ObjectWithType, BookmarkSupportModel {
  declare model: BookmarkSupportModel;
  declare initModel: SomeRequired<this['model'], 'desktop'>;

  static ERROR_MISSING_OUTLINE = 'missing-outline';
  static ERROR_MISSING_PAGE_PARAM = 'missing-page-param';
  static ERROR_PAGE_NOT_BOOKMARKABLE = 'page-not-bookmarkable';
  static ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER = 'page-not-bookmarkable';
  static ERROR_OUTLINE_NOT_FOUND = 'outline-not-found';
  static ERROR_PAGE_NOT_FOUND = 'page-not-found';

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
    const reviver = (key, value) => {
      if (objects.isPlainObject(value) && value._type) {
        let model = Object.assign({}, value); // shallow copy to keep original object intact
        model.objectType = DoRegistry.get().toObjectType(value._type) || 'BaseDoEntity';
        // Note: keep _type for later conversion to json again. This is important for types that are only known in Java.
        delete model._typeVersion; // always ignore type version
        return scout.create(model);
      }
      return value;
    };

    return JSON.parse(webstorage.getItemFromLocalStorage('jswidgets:bookmarks'), reviver);
  }

  protected _setBookmarkStore(bookmarkStore: BookmarkDo[]) {
    if (!bookmarkStore) {
      webstorage.removeItemFromLocalStorage('jswidgets:bookmarks');
      return;
    }

    let jsonBookmarkStore = bookmarks.toTypedJson(bookmarkStore);
    webstorage.setItemToLocalStorage('jswidgets:bookmarks', JSON.stringify(jsonBookmarkStore));
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
    let outlineId = this.desktop.outline?.getBookmarkAdapter().buildId();
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
      // FIXME bsh [js-bookmark] Handle case where bookmarkIdentifier is not present -> throw new VetoException(TEXTS.get("CannotCreateBookmarkAtThisLocation"))
      if (childPage && !childPage.row?.bookmarkIdentifier) { // child row not identifiable
        return $.rejectedPromise(BookmarkSupport.ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER);
      }

      let expandedChildRowIdentifier = childPage?.row?.bookmarkIdentifier;
      // FIXME bsh [js-bookmark] Only export when requested, see BookmarkDoBuilder#createTableRowSelections
      let selectedChildRowIdentifiers = page.detailTable.selectedRows.map(row => row.bookmarkIdentifier).filter(Boolean);
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
            HybridActionContextElement.of(page.getOutline(), page));
        })
        .then(searchFilter => {
          if (searchFilter && !(searchFilter instanceof BaseDoEntity) && !searchFilter._type) {
            searchFilter = bookmarks.toTypedJson(searchFilter);
            if (!searchFilter._type) {
              throw new Error('Missing _type for search filter');
            }
          }
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
      .then(() => this._openBookmarkRemote(bookmarkDefinition))
      .then(result => {
        if (arrays.empty(result?.remainingPagePath)) {
          return; // done, we are already on the correct page
        }
        return this._openBookmarkLocal(bookmarkDefinition, result);
      })
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

  protected _openBookmarkRemote(bookmarkDefinition: OutlineBookmarkDefinitionDo): JQuery.Promise<ActivateBookmarkResultDo> {
    let hybridManager = HybridManager.get(this.session);

    if (hybridManager) {
      // Scout Classic: send the bookmark to the UI server first, let the client model resolve as much of the bookmark
      // as it can, then resolved the remaining path in the UI
      let jsonBookmarkDefinition = bookmarks.toTypedJson(bookmarkDefinition);
      let hybridActionData = {
        bookmarkDefinition: jsonBookmarkDefinition
      };
      return hybridManager.callActionAndWait('ActivateBookmark', hybridActionData)
        .then((result: ActivateBookmarkResultDo) => {
          return scout.create(ActivateBookmarkResultDo, bookmarks.toObjectModel(result));
        });
    }

    // Scout JS: resolve everything in the UI, i.e. the entire path is remaining
    return $.resolvedPromise().then(() => {
      return scout.create(ActivateBookmarkResultDo, {
        targetBookmarkPage: null,
        remainingPagePath: [...bookmarkDefinition.pagePath, bookmarkDefinition.bookmarkedPage]
      });
    });
  }

  protected _openBookmarkLocal(bookmarkDefinition: OutlineBookmarkDefinitionDo, result: ActivateBookmarkResultDo): JQuery.Promise<void> {
    // Check if we are already on the correct outline
    let outline = this.desktop.outline;
    if (!outline || outline.getBookmarkAdapter().buildId() !== bookmarkDefinition.outlineId) {
      outline = this.desktop.getOutlines().find(outline => {
        let outlineId = outline.getBookmarkAdapter().buildId();
        return outlineId === bookmarkDefinition.outlineId;
      });
    }
    if (!outline || !outline.visible) {
      return $.rejectedPromise(BookmarkSupport.ERROR_OUTLINE_NOT_FOUND);
    }
    this.desktop.setOutline(outline);

    // // FIXME bsh [js-bookmark] Find a better solution to transfer the parent from the UI server to here!
    // let pagePath = result.remainingPagePath.slice(); // create copy because arrays is altered
    // let parent = (result.targetBookmarkPage && outline.selectedNode()) || outline;
    // let parentRowBookmarkIdentifier = result.targetBookmarkPage instanceof TableBookmarkPageDo ? result.targetBookmarkPage.expandedChildRow : null;
    let pagePath = [...bookmarkDefinition.pagePath, bookmarkDefinition.bookmarkedPage];
    let parent = outline;
    let parentRowBookmarkIdentifier = null;
    return this._resolveNextPageInPath(pagePath, parent, parentRowBookmarkIdentifier)
      .then(page => {
        if (!page) {
          return $.rejectedPromise(BookmarkSupport.ERROR_PAGE_NOT_FOUND);
        }
        this._revealPage(page, pagePath);
      });
  }

  protected _resolveNextPageInPath(pagePath: IBookmarkPageDo[], parent: Outline | Page, parentRowBookmarkIdentifier: BookmarkTableRowIdentifierDo): JQuery.Promise<Page> {
    let parentPage = parent instanceof Page ? parent : null;

    if (arrays.empty(pagePath)) {
      return $.resolvedPromise(parentPage); // done!
    }

    let pageDefinition = pagePath.shift();
    return this._resolvePage(pageDefinition, parent, parentRowBookmarkIdentifier)
      .then((page: Page) => {
        if (!page) {
          // Unable to find a page that matches the requested page definition. Put it back to the page path (so later
          // code will know that not the entire path was successfully consumed) and return the last known page.
          pagePath.unshift(pageDefinition); // put it back
          return parentPage;
        }

        page.activate();

        let expandedChildRow = pageDefinition instanceof TableBookmarkPageDo ? pageDefinition.expandedChildRow : null;
        let selectedChildRows = pageDefinition instanceof TableBookmarkPageDo ? pageDefinition.selectedChildRows : null;

        // Restore selection of last table page
        // FIXME bsh [js-bookmark] Handle hierarchical table, see Table#restoreSelection
        if (arrays.empty(pagePath) && page.nodeType === Page.NodeType.TABLE && page.detailTable && arrays.hasElements(selectedChildRows)) {
          let normalizedRowIdentifiers = selectedChildRows.map(bookmarkIdentifier => bookmarks.stringifyNormalized(bookmarkIdentifier));
          let selectedRows = page.detailTable.rows.filter(row => {
            let normalizedRowIdentifier = bookmarks.stringifyNormalized(row.bookmarkIdentifier);
            return normalizedRowIdentifiers.includes(normalizedRowIdentifier);
          });
          page.detailTable.selectRows(selectedRows);
        }

        if (page instanceof PageWithTable) {
          if (pageDefinition instanceof TableBookmarkPageDo) {
            page.setSearchFilter(pageDefinition.searchData);
          } else {
            page.resetSearchFilter();
          }
        }

        return page.loadChildren()
          .then(() => this._resolveNextPageInPath(pagePath, page, expandedChildRow));
      });
  }

  protected _resolvePage(pageDefinition: IBookmarkPageDo, parent: Page | Outline, parentRowBookmarkIdentifier: BookmarkTableRowIdentifierDo): JQuery.Promise<Page> {
    if (parent instanceof Outline) {
      // Lookup child page by pageParam
      let result = parent.nodes.find(node => node.matchesPageParam(pageDefinition.pageParam));
      return $.resolvedPromise(result);
    }

    if (parent instanceof Page) {
      return parent.ensureLoadChildren()
        .then(() => {
          if (parent.nodeType === Page.NodeType.TABLE) {
            parent.ensureDetailTable(); // FIXME bsh [js-bookmark] This does not work for classic pages!!!
            // Lookup child page by parent PK (ignore PageParam)
            let normalizedParentRowIdentifier = bookmarks.stringifyNormalized(parentRowBookmarkIdentifier);
            let row = parent.detailTable.rows.find(row => {
              let normalizedRowIdentifier = bookmarks.stringifyNormalized(row.bookmarkIdentifier);
              return normalizedRowIdentifier === normalizedParentRowIdentifier;
            });
            if (row) {
              return row.page;
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

  createPageForBookmark(outline: Outline, bookmark: BookmarkDo): Page {
    // FIXME bsh [js-bookmark] Implement (BookmarkTablePage.execCreateChildPage, BookmarkClientDomain.createPageForBookmark)

    const pageParam = bookmark?.definition?.bookmarkedPage?.pageParam;
    const pageObjectType = PageResolver.get().findObjectTypeForPageParam(pageParam);
    if (pageObjectType) {
      return scout.create(pageObjectType, {
        parent: outline,
        pageParam
      });
    }
    return null;
  }

  openBookmarkInPage(page: Page, bookmark: BookmarkDo) {
    // FIXME bsh [js-bookmark] Implement (CoreBookmarkClientService.openBookmarkInPage)
  }
}

