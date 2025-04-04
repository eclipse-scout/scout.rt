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
  BookmarkDo, BookmarkDoBuilderModel, BookmarkDoBuilderOptionsDo, BookmarkTableRowIdentifierDo, ChartTableControlConfigDo, Desktop, HybridActionContextElement, HybridActionContextElements, HybridManager, IBookmarkDefinitionDo, IBookmarkDo,
  IBookmarkPageDo, InitModelOf, NodeBookmarkPageDo, ObjectWithType, OutlineBookmarkDefinitionDo, Page, PageBookmarkDefinitionDo, PageWithTable, scout, Session, TableBookmarkPageDo, TableClientUiPreferencesDo
} from '../index';

export class BookmarkDoBuilder implements ObjectWithType, BookmarkDoBuilderModel {
  declare model: BookmarkDoBuilderModel;

  static ERROR_MISSING_OUTLINE = 'missing-outline';
  static ERROR_MISSING_PAGE_PARAM = 'missing-page-param';
  static ERROR_PAGE_NOT_BOOKMARKABLE = 'page-not-bookmarkable';
  static ERROR_PAGE_PATH_NOT_BOOKMARKABLE = 'page-path-not-bookmarkable';
  static ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER = 'missing-row-bookmark-identifier';

  objectType: string;
  desktop: Desktop;
  page: Page;

  // builder options
  createOutline: boolean;
  persistableRequired: boolean;
  fallbackAllowed: boolean;
  createTitle: boolean;
  createDescription: boolean;
  createTablePreferences: boolean;
  createTableRowSelections: boolean;

  // --------------------------------------

  constructor() {
    this.createOutline = true;
    this.persistableRequired = true;
    this.fallbackAllowed = true;
    this.createTitle = true;
    this.createDescription = true;
    this.createTablePreferences = true;
    this.createTableRowSelections = true;
  }

  init(model: InitModelOf<this>) {
    Object.assign(this, model);
    scout.assertValue(this.desktop);
  }

  get session(): Session {
    return this.desktop.session;
  }

  // --------------------------------------

  build(): JQuery.Promise<IBookmarkDo> {
    return $.when(this._build());
  }

  protected async _build(): Promise<IBookmarkDo> {
    let bookmarkDefinition = await this._createBookmarkDefinition();
    return this._createBookmark(bookmarkDefinition);
  }

  protected async _createBookmark(bookmarkDefinition: IBookmarkDefinitionDo): Promise<IBookmarkDo> {
    return scout.create(BookmarkDo, {
      definition: bookmarkDefinition
    });
  }

  protected async _createBookmarkDefinition(): Promise<IBookmarkDefinitionDo> {
    let page = this.page;
    let outline = page?.outline || this.desktop.outline;
    let outlineId = outline?.getObjectUuidBuilder().buildId();
    if (!outlineId) {
      throw BookmarkDoBuilder.ERROR_MISSING_OUTLINE;
    }
    if (page === undefined) {
      page = outline.activePage();
    }

    let createOutlineBookmarkDefinition = this.createOutline;

    // Create page definition for page to bookmark (used by both bookmark definition types)
    let bookmarkedPage = page ? await this._pageToBookmarkPage(page, null) : null;

    // Create page path (only for outline bookmark definition type)
    let pagePath: IBookmarkPageDo[] = [];
    if (createOutlineBookmarkDefinition && page) {
      let parentPage = page.parentNode;
      let childPage = page;
      while (parentPage) {
        let pathEntry = await this._pageToBookmarkPage(parentPage, childPage);
        if (!pathEntry) {
          // non-bookmarkable page
          if (this.fallbackAllowed) {
            createOutlineBookmarkDefinition = false; // fall back to PageBookmarkDefinitionDo
            break;
          }
          throw BookmarkDoBuilder.ERROR_PAGE_PATH_NOT_BOOKMARKABLE;
        }
        // Add bookmarkPage to front of path and repeat for parent page
        pagePath.unshift(pathEntry);
        childPage = parentPage;
        parentPage = parentPage.parentNode;
      }
    }

    let bookmarkDefinition;
    if (createOutlineBookmarkDefinition) {
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
    return bookmarkDefinition;
  }

  // Note: this methode is called multiple times from bottom to top. On the first invocation, the childPage is not set,
  // but later calls pass the childPage for resolving the corresponding row of a table page.
  protected async _pageToBookmarkPage(page: Page, childPage: Page): Promise<IBookmarkPageDo> {
    if (!page) {
      throw BookmarkDoBuilder.ERROR_PAGE_NOT_BOOKMARKABLE;
    }
    if (!page.pageParam) {
      throw BookmarkDoBuilder.ERROR_MISSING_PAGE_PARAM;
    }

    if (page.nodeType === Page.NodeType.NODES) {
      return this._pageToNodeBookmarkPage(page, childPage);
    }
    if (page.nodeType === Page.NodeType.TABLE) {
      return this._pageToTableBookmarkPage(page, childPage);
    }

    throw BookmarkDoBuilder.ERROR_PAGE_NOT_BOOKMARKABLE;
  }

  protected async _pageToNodeBookmarkPage(page: Page, childPage?: Page): Promise<NodeBookmarkPageDo> {
    return scout.create(NodeBookmarkPageDo, {
      pageParam: page.pageParam,
      displayText: page.getDisplayText()
    });
  }

  protected async _pageToTableBookmarkPage(page: Page, childPage?: Page): Promise<TableBookmarkPageDo> {
    if (page instanceof PageWithTable) {
      return this._pageToTableBookmarkPageLocal(page, childPage);
    }
    return this._pageToTableBookmarkPageRemote(page, childPage);
  }

  protected async _pageToTableBookmarkPageLocal(page: PageWithTable, childPage?: Page): Promise<TableBookmarkPageDo> {
    let expandedChildRowIdentifier: BookmarkTableRowIdentifierDo = null;
    if (childPage) {
      if (childPage.row) {
        // Linked to table row -> get row identifier
        expandedChildRowIdentifier = page.getTableRowIdentifier(childPage.row, !this.persistableRequired);
      } else {
        // Not linked to table row -> assume the page param is enough to identify the child page
      }
      if (!expandedChildRowIdentifier) { // child row not identifiable
        throw BookmarkDoBuilder.ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER;
      }
    }

    let selectedChildRowIdentifiers: BookmarkTableRowIdentifierDo[] = [];
    if (this.createTableRowSelections && page.childrenLoaded) {
      selectedChildRowIdentifiers = page.detailTable.selectedRows
        .map(row => page.getTableRowIdentifier(row, !this.persistableRequired));
    }

    let searchFilterComplete = true;
    let searchData = await this._createSearchFilterForBookmark(page);
    let tablePreferences = await this._createTablePreferencesForBookmark(page);
    let chartTableControlConfig = await this._createChartTableControlConfigForBookmark(page);

    return scout.create(TableBookmarkPageDo, {
      pageParam: page.pageParam,
      displayText: page.getDisplayText(),
      expandedChildRow: expandedChildRowIdentifier,
      selectedChildRows: selectedChildRowIdentifiers,
      searchFilterComplete: searchFilterComplete,
      searchData: searchData,
      tablePreferences: tablePreferences,
      chartTableControlConfig: chartTableControlConfig
    });
  }

  protected async _pageToTableBookmarkPageRemote(page: Page, childPage?: Page): Promise<TableBookmarkPageDo> {
    let createBookmarkOptions = scout.create(BookmarkDoBuilderOptionsDo, {
      createOutline: false,
      persistableRequired: this.persistableRequired,
      createTitle: false,
      createDescription: false,
      createTablePreferences: this.createTablePreferences,
      createTableRowSelections: this.createTableRowSelections
    });

    let contextElements = scout.create(HybridActionContextElements)
      .withElement('page', HybridActionContextElement.of(page.outline, page));
    if (childPage) {
      contextElements.withElement('childPage', HybridActionContextElement.of(childPage.outline, childPage));
    }

    let bookmark = await HybridManager.get(this.session).callActionAndWait('CreateBookmark', createBookmarkOptions, contextElements) as BookmarkDo;
    if (!(bookmark?.definition?.bookmarkedPage instanceof TableBookmarkPageDo)) {
      throw BookmarkDoBuilder.ERROR_PAGE_NOT_BOOKMARKABLE;
    }
    return bookmark.definition.bookmarkedPage;
  }

  protected async _createSearchFilterForBookmark(page: PageWithTable): Promise<any> {
    // FIXME bsh [js-bookmark] Use page.getSearchFilter() instead - but how to deal with hybrid search forms?
    let searchForm = page.getSearchForm();
    if (!searchForm) {
      return null;
    }
    if (searchForm.modelAdapter) {
      return HybridManager.get(this.session).callActionAndWait('ExportSearchData', undefined,
        scout.create(HybridActionContextElements)
          .withElement('searchForm', HybridActionContextElement.of(searchForm))
      );
    }
    return searchForm.exportData();
  }

  protected async _createTablePreferencesForBookmark(page: PageWithTable): Promise<TableClientUiPreferencesDo> {
    if (!this.createTablePreferences) {
      return null;
    }
    // FIXME bsh [js-bookmark] Implement
    return null;
  }

  protected async _createChartTableControlConfigForBookmark(page: PageWithTable): Promise<ChartTableControlConfigDo> {
    return null;
  }
}
