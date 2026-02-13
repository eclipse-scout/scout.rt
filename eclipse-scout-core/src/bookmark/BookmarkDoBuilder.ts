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
  arrays, BookmarkDo, BookmarkDoBuilderModel, BookmarkSupport, BookmarkTableRowIdentifierDo, ChartTableControlConfigHelper, Desktop, Form, IBookmarkDefinitionDo, IBookmarkDo, IBookmarkPageDo, IChartTableControlConfigDo, InitModelOf,
  NodeBookmarkPageDo, ObjectWithType, OutlineBookmarkDefinitionDo, Page, PageBookmarkDefinitionDo, PageWithTable, scout, Session, strings, TableBookmarkPageDo, TableClientUiPreferencesDo, TableUiPreferences, tableUiPreferences
} from '../index';

export class BookmarkDoBuilder implements ObjectWithType, BookmarkDoBuilderModel {
  declare model: BookmarkDoBuilderModel;

  static ERROR_MISSING_OUTLINE = 'missing-outline';
  static ERROR_MISSING_PAGE_PARAM = 'missing-page-param';
  static ERROR_UNSUPPORTED_NODE_TYPE = 'unsupported-node-type';
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

  // Map to store texts computed by _tablePageToTableBookmarkPage() for later use in _createBookmarkDescription()
  protected _searchFilterTexts = new Map<TableBookmarkPageDo, string>();

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

  protected async _createBookmarkDefinition(): Promise<IBookmarkDefinitionDo> {
    let page = this.page;
    let outline = page?.outline || this.desktop.outline;
    let outlineId = outline?.buildUuid();
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
      while (parentPage && !parentPage.compactRoot) {
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

  // This method is called multiple times from bottom to top. On the first invocation, the childPage is not set,
  // but later calls pass the childPage for resolving the corresponding row of a table page.
  protected async _pageToBookmarkPage(page: Page, childPage: Page): Promise<IBookmarkPageDo> {
    if (!page.pageParam) {
      throw BookmarkDoBuilder.ERROR_MISSING_PAGE_PARAM;
    }
    if (page.nodeType === Page.NodeType.NODES) {
      return this._pageToNodeBookmarkPage(page, childPage);
    }
    if (page.nodeType === Page.NodeType.TABLE) {
      return this._pageToTableBookmarkPage(page, childPage);
    }
    throw BookmarkDoBuilder.ERROR_UNSUPPORTED_NODE_TYPE;
  }

  protected async _pageToNodeBookmarkPage(page: Page, childPage?: Page): Promise<NodeBookmarkPageDo> {
    return scout.create(NodeBookmarkPageDo, {
      pageParam: page.pageParam,
      displayText: page.getDisplayText()
    });
  }

  protected async _pageToTableBookmarkPage(page: Page, childPage?: Page): Promise<TableBookmarkPageDo> {
    if (page instanceof PageWithTable) {
      return this._tablePageToTableBookmarkPage(page, childPage);
    }
    throw BookmarkDoBuilder.ERROR_PAGE_NOT_BOOKMARKABLE;
  }

  protected async _tablePageToTableBookmarkPage(page: PageWithTable, childPage?: Page): Promise<TableBookmarkPageDo> {
    let expandedChildRowIdentifier = this._createExpandedTableRowIdentifier(page, childPage);
    let selectedChildRowIdentifiers = this._createSelectedTableRowIdentifiers(page);
    let searchFilterComplete = !page.searchRequired || page.searchFilterCompleted;
    let searchData = await this._createSearchFilterForBookmark(page);
    let tablePreferences = await this._createTablePreferencesForBookmark(page);
    let chartTableControlConfig = await this._createChartTableControlConfigForBookmark(page);

    if (childPage && !expandedChildRowIdentifier && !childPage.pageParam) {
      // child row not identifiable
      throw BookmarkDoBuilder.ERROR_MISSING_ROW_BOOKMARK_IDENTIFIER;
    }

    let bookmarkPage = scout.create(TableBookmarkPageDo, {
      pageParam: page.pageParam,
      displayText: page.getDisplayText(),
      expandedChildRow: expandedChildRowIdentifier,
      selectedChildRows: selectedChildRowIdentifiers,
      searchFilterComplete: searchFilterComplete,
      searchData: searchData,
      tablePreferences: tablePreferences,
      chartTableControlConfig: chartTableControlConfig
    });

    // Compute search filter text and put it into a map for later use in _createBookmarkDescription()
    if (this.createDescription && searchData) {
      let searchFilterText = await page.getSearchFilterText();
      if (searchFilterText) {
        this._searchFilterTexts.set(bookmarkPage, searchFilterText);
      }
    }

    return bookmarkPage;
  }

  protected _createExpandedTableRowIdentifier(page: PageWithTable, childPage: Page): BookmarkTableRowIdentifierDo {
    // If child page is linked to a table row, check if a special table row identifier is needed.
    // Otherwise, we assume that the page param is enough to identify the child page.
    if (childPage && childPage.row) {
      return page.getTableRowIdentifier(childPage.row, !this.persistableRequired);
    }
    return null;
  }

  protected _createSelectedTableRowIdentifiers(page: PageWithTable): BookmarkTableRowIdentifierDo[] {
    if (!this.createTableRowSelections) {
      return [];
    }
    if (page.childrenLoaded) {
      return page.detailTable.selectedRows
        .map(row => page.getTableRowIdentifier(row, !this.persistableRequired));
    }
    return [];
  }

  protected async _createSearchFilterForBookmark(page: PageWithTable): Promise<any> {
    let searchForm = page.getSearchForm();
    if (!searchForm) {
      return null;
    }
    return this._exportSearchFormData(searchForm);
  }

  protected async _exportSearchFormData(searchForm: Form): Promise<any> {
    return searchForm.exportData();
  }

  protected async _createTablePreferencesForBookmark(page: PageWithTable): Promise<TableClientUiPreferencesDo> {
    if (!this.createTablePreferences) {
      return null;
    }
    if (!page.detailTable) {
      return null;
    }
    let table = page.detailTable;
    let prefs = tableUiPreferences.create(table);
    let profile = tableUiPreferences.createProfile(table, {includeUserFilters: true});
    prefs.tablePreferenceProfiles = new Map([
      [TableUiPreferences.PROFILE_ID_BOOKMARK, profile]
    ]);
    return prefs;
  }

  protected async _createChartTableControlConfigForBookmark(page: PageWithTable): Promise<IChartTableControlConfigDo> {
    const helper = scout.create(ChartTableControlConfigHelper);
    return helper.exportConfig(page);
  }

  // --------------------------------------

  protected async _createBookmark(bookmarkDefinition: IBookmarkDefinitionDo): Promise<IBookmarkDo> {
    let bookmarkTitle = await this._createBookmarkTitle(bookmarkDefinition);
    let bookmarkDescription = await this._createBookmarkDescription(bookmarkDefinition);
    return scout.create(BookmarkDo, {
      definition: bookmarkDefinition,
      title: bookmarkTitle || undefined,
      description: bookmarkDescription || undefined
    });
  }

  protected async _createBookmarkTitle(bookmarkDefinition: IBookmarkDefinitionDo): Promise<string> {
    if (!this.createTitle) {
      return null;
    }
    let titleSegments: string[] = [];
    if (bookmarkDefinition instanceof OutlineBookmarkDefinitionDo) {
      let outline = BookmarkSupport.get(this.session).resolveOutline(bookmarkDefinition.outlineId);
      if (outline) {
        titleSegments.push(outline.title);
      }

      bookmarkDefinition.pagePath
        .map(p => p.displayText)
        .forEach(s => titleSegments.push(s));
    }
    if (bookmarkDefinition.bookmarkedPage) {
      titleSegments.push(bookmarkDefinition.bookmarkedPage.displayText);
    }

    titleSegments = titleSegments.filter(s => strings.hasText(s));
    return arrays.hasElements(titleSegments) ? titleSegments.join(' - ') : this.session.text('Bookmark');
  }

  protected async _createBookmarkDescription(bookmarkDefinition: IBookmarkDefinitionDo): Promise<string> {
    if (!this.createDescription) {
      return null;
    }

    let pagePath = [
      ...(bookmarkDefinition instanceof OutlineBookmarkDefinitionDo ? bookmarkDefinition.pagePath : []),
      bookmarkDefinition.bookmarkedPage
    ].filter(Boolean);

    let lines = [];
    pagePath.forEach((bookmarkPage, index) => {
      let indent = '  ';
      let prefix = strings.repeat(indent, index);

      let displayText = bookmarkPage.displayText || this.session.text('Node');
      lines.push(prefix + displayText);

      if (bookmarkPage instanceof TableBookmarkPageDo) {
        // Get search filter text added by _tablePageToTableBookmarkPage()
        let searchFilterText = this._searchFilterTexts.get(bookmarkPage);
        // Split search filter text into lines and add them to the description (with additional indentation)
        if (searchFilterText) {
          lines.push(...searchFilterText.split('\n').map(s => prefix + indent + s));
        }
      }
    });
    return lines.join('\n');
  }
}
