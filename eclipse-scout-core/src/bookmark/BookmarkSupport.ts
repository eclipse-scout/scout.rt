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
  ActivateBookmarkResultDo, App, arrays, BookmarkDo, bookmarks, BookmarkSupportModel, BookmarkTableRowIdentifierDo, Desktop, DoRegistry, HybridManager, IBookmarkPageDo, InitModelOf, MessageBoxes, NodeBookmarkPageDo, objects, ObjectWithType,
  Outline, OutlineBookmarkDefinitionDo, Page, PageBookmarkDefinitionDo, PageResolver, scout, Session, SomeRequired, Status, TableBookmarkPageDo, UuidPool, webstorage
} from '../index';

export class BookmarkSupport implements ObjectWithType, BookmarkSupportModel {
  declare model: BookmarkSupportModel;
  declare initModel: SomeRequired<this['model'], 'desktop'>;

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
        let objectType = DoRegistry.get().toConstructor(value._type);
        if (objectType) {
          let model = Object.assign({}, value); // shallow copy to keep original object intact
          delete model._type;
          delete model._typeVersion; // always ignore type version
          return scout.create(objectType, model);
        }
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

    const replacer = (key, value) => {
      if (objects.isPlainObject(value) && value.objectType) {
        let json = Object.assign({}, value); // shallow copy to keep original object intact
        json._type = DoRegistry.get().toJsonType(value.objectType);
        delete json.objectType;
        return json;
      }
      return value;
    };

    webstorage.setItemToLocalStorage('jswidgets:bookmarks', JSON.stringify(bookmarkStore, replacer));
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

  createBookmark(): BookmarkDo {
    let outlineId = this.desktop.outline?.getBookmarkAdapter()?.buildId();
    if (!outlineId) {
      return null;
    }

    let selectedPage = this.desktop.outline.selectedNode();
    let bookmarkedPage = this._pageToBookmark(selectedPage);
    if (!bookmarkedPage) {
      // FIXME bsh [js-bookmark] How to handle errors? VetoException? Special return object?
      MessageBoxes.openOk(this.desktop, 'Page cannot be bookmarked (missing pageParam)', Status.Severity.ERROR);
      return null;
    }

    let pagePath = [];
    let currentPage = selectedPage;
    while (currentPage.parentNode) {
      let parentPage = currentPage.parentNode;
      let pathEntry = this._pageToBookmark(parentPage, currentPage);
      if (!pathEntry) {
        // non-bookmarkable page, discard entire path
        pagePath = null;
        break;
      }
      pagePath.unshift(pathEntry);
      currentPage = parentPage;
    }

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
  }

  protected _pageToBookmark(page: Page, childPage?: Page): IBookmarkPageDo {
    let bookmarkedPage: IBookmarkPageDo = null;
    if (page && page.pageParam) { // FIXME bsh [js-bookmark] Check bookmarkable
      if (page.nodeType === Page.NodeType.NODES) {
        bookmarkedPage = scout.create(NodeBookmarkPageDo, {
          pageParam: page.pageParam,
          displayText: page.text
        });
      } else if (page.nodeType === Page.NodeType.TABLE) {
        // FIXME bsh [js-bookmark] Handle case where bookmarkIdentifier is not present -> throw new VetoException(TEXTS.get("CannotCreateBookmarkAtThisLocation"))
        if (childPage && !childPage.row?.bookmarkIdentifier) {
          return null; // child row not identifiable
        }
        let expandedChildRowIdentifier = childPage?.row?.bookmarkIdentifier;
        // FIXME bsh [js-bookmark] Only export when requested, see BookmarkDoBuilder#createTableRowSelections
        let selectedChildRowIdentifiers = page.detailTable.selectedRows.map(row => row.bookmarkIdentifier).filter(Boolean);
        bookmarkedPage = scout.create(TableBookmarkPageDo, {
          pageParam: page.pageParam,
          displayText: page.text,
          expandedChildRow: expandedChildRowIdentifier,
          selectedChildRows: selectedChildRowIdentifiers
        });
      }
    }
    return bookmarkedPage;
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
      .then(() => {
        let hybridManager = HybridManager.get(this.session);
        if (hybridManager) {
          let jsonBookmarkDefinition = bookmarks.toTypedJson(bookmarkDefinition);
          let hybridActionData = {
            bookmarkDefinition: jsonBookmarkDefinition
          };
          return hybridManager.callActionAndWait('ActivateBookmark', hybridActionData).then((result: ActivateBookmarkResultDo) => {
            return scout.create(ActivateBookmarkResultDo, bookmarks.toObjectModel(result));
          });
        }
        scout.create(ActivateBookmarkResultDo, {
          remainingPagePath: [...bookmarkDefinition.pagePath, bookmarkDefinition.bookmarkedPage],
          parentBookmarkPage: null
        });
      })
      .then((result: ActivateBookmarkResultDo) => {
        if (arrays.empty(result?.remainingPagePath)) {
          return; // done, we are already on the correct page
        }

        // Check if we are already on the correct outline
        let outline = this.desktop.outline;
        if (outline?.getBookmarkAdapter()?.buildId() !== bookmarkDefinition.outlineId) {
          outline = this.desktop.getOutlines().find(outline => {
            let outlineId = outline.getBookmarkAdapter()?.buildId();
            return outlineId === bookmarkDefinition.outlineId;
          });
        }
        if (!outline || !outline.visible) {
          // throw new VetoException(TEXTS.get("BookmarkActivationFailedOutlineNotAvailable", outline == null ? TEXTS.get("Unknown") : outline.getTitle())); FIXME bsh [js-bookmark] NLS
          MessageBoxes.openOk(this.desktop, 'Outline not found', Status.Severity.ERROR);
          return;
        }
        this.desktop.setOutline(outline);

        let pagePath = result.remainingPagePath.slice(); // create copy because arrays is altered
        let parent = outline.selectedNode() || outline;
        let parentRowBookmarkIdentifier = result.parentBookmarkPage instanceof TableBookmarkPageDo ? result.parentBookmarkPage.expandedChildRow : null;
        return this._loadNextPageInPath(pagePath, parent, parentRowBookmarkIdentifier)
          .then(page => {
            if (!page) {
              console.log('Page not found', result, pagePath);
              MessageBoxes.openOk(this.desktop, 'There has been an error while loading the favorite.', Status.Severity.ERROR); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResolvingFailed')
              return;
            }
            let pathFullyRestored = arrays.empty(pagePath);

            // FIXME bsh [js-bookmark] This block should not be necessary, but the tree is broken :( #378077
            {
              let expandNode = page;
              if (!pathFullyRestored || page.nodeType === Page.NodeType.TABLE) {
                // don't expand target node
                expandNode = page.parentNode;
              }
              while (expandNode) {
                outline.expandNode(expandNode, {renderAnimated: false});
                expandNode = expandNode.parentNode;
              }
            }

            outline.deselectAll(); // reselection triggers owner changes of menu in case we come here by execDataChanged FIXME bsh [js-bookmark] is this necessary in js?
            outline.selectNode(page);
            outline.revealSelection();

            if (!pathFullyRestored) {
              page.detailTable?.setTableStatus(Status.error('Loading the favorite has been canceled because the entry cannot be found in this view.')); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResolutionCanceled')
            }
          });
      })
      .catch(err => {
        // FIXME bsh [js-bookmark] Error handling
        App.get().errorHandler.handle(err);
        MessageBoxes.openOk(this.desktop, 'There has been an error while loading the favorite.', Status.Severity.ERROR); // FIXME bsh [js-bookmark] NLS: this.session.text('BookmarkResolvingFailed')
      })
      .then(() => {
        this.setLoading(false);
      });
  }

  _loadNextPageInPath(pagePath: IBookmarkPageDo[], parent: Outline | Page, parentRowBookmarkIdentifier: BookmarkTableRowIdentifierDo): JQuery.Promise<Page> {
    if (arrays.empty(pagePath)) {
      return $.resolvedPromise(parent instanceof Page ? parent : null); // done!
    }

    let pageDefinition = pagePath.shift();

    return $.resolvedPromise()
      .then(() => {
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
                  return parent.ensureLoadChildren()
                    .then(() => row?.page);
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
        if (parent instanceof Outline) {
          // Lookup child page by pageParam
          return parent.nodes.find(node => node.matchesPageParam(pageDefinition.pageParam));
        }
        return null;
      })
      .then((page: Page) => {
        if (!page) {
          return parent instanceof Page ? parent : null; // not found
        }

        // Restore selection of last table page
        // FIXME bsh [js-bookmark] Handle hierarchical table, see Table#restoreSelection
        if (
          arrays.empty(pagePath) && page instanceof Page && page.nodeType === Page.NodeType.TABLE && page.detailTable &&
          pageDefinition instanceof TableBookmarkPageDo && arrays.hasElements(pageDefinition.selectedChildRows)
        ) {
          let normalizedRowIdentifiers = pageDefinition.selectedChildRows.map(bookmarkIdentifier => bookmarks.stringifyNormalized(bookmarkIdentifier));
          let selectedRows = page.detailTable.rows.filter(row => {
            let normalizedRowIdentifier = bookmarks.stringifyNormalized(row.bookmarkIdentifier);
            return normalizedRowIdentifiers.includes(normalizedRowIdentifier);
          });
          page.detailTable.selectRows(selectedRows);
        }

        page.activate();
        return page.ensureLoadChildren()
          // FIXME bsh [js-bookmark] Remove debug code
          // .then(() => {
          //   let deferred = $.Deferred();
          //   setTimeout(() => deferred.resolve(), 1000);
          //   return deferred.promise();
          // })
          .then(() => this._loadNextPageInPath(pagePath, page, pageDefinition instanceof TableBookmarkPageDo ? pageDefinition.expandedChildRow : null));
      });
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

