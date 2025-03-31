/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BaseDoEntity, BookmarkSupport, DoEntity, PageParamDo, typeName} from '../index';

@typeName('crm.Bookmark')
export class BookmarkDo extends BaseDoEntity {
  key: string;
  titles: Record<string, string>;
  description: string;
  definition: IBookmarkDefinitionDo;
}

// --------------------------------------------------

export interface IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
}

@typeName('crm.OutlineBookmarkDefinition')
export class OutlineBookmarkDefinitionDo extends BaseDoEntity implements IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
  outlineId: string;
  /** Path from the outline's root to the {@link bookmarkedPage} */
  pagePath: IBookmarkPageDo[];
}

@typeName('crm.PageBookmarkDefinition')
export class PageBookmarkDefinitionDo extends BaseDoEntity implements IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
}

// --------------------------------------------------

export interface IBookmarkPageDo {
  pageParam?: PageParamDo;
  displayText?: string;
}

@typeName('crm.NodeBookmarkPage')
export class NodeBookmarkPageDo extends BaseDoEntity implements IBookmarkPageDo {
  pageParam?: PageParamDo;
  displayText?: string;
}

@typeName('crm.TableBookmarkPage')
export class TableBookmarkPageDo extends BaseDoEntity implements IBookmarkPageDo {
  pageParam?: PageParamDo;
  displayText?: string;
  expandedChildRow?: BookmarkTableRowIdentifierDo;
  selectedChildRows?: BookmarkTableRowIdentifierDo[];
  searchFilterComplete?: boolean; // FIXME bsh [js-bookmark] always true?
  searchData?: DoEntity;
  tablePreferences?: TableClientUiPreferencesDo;
  chartTableControlConfig?: ChartTableControlConfigDo;
}

@typeName('crm.BookmarkTableRowIdentifier')
export class BookmarkTableRowIdentifierDo extends BaseDoEntity {
  keyComponents: IBookmarkTableRowIdentifierComponentDo[];
}

export interface IBookmarkTableRowIdentifierComponentDo {
}

/**
 * Never serialize this!
 */
@typeName('crm.BookmarkTableRowIdentifierObjectComponent')
export class BookmarkTableRowIdentifierObjectComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: any;
}

@typeName('crm.BookmarkTableRowIdentifierEntityKeyComponent')
export class BookmarkTableRowIdentifierEntityKeyComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;
}

@typeName('crm.BookmarkTableRowIdentifierDateComponent')
export class BookmarkTableRowIdentifierDateComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: Date;
}

@typeName('crm.BookmarkTableRowIdentifierBooleanComponent')
export class BookmarkTableRowIdentifierBooleanComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: boolean;
}

@typeName('crm.BookmarkTableRowIdentifierIntegerComponent')
export class BookmarkTableRowIdentifierIntegerComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: number;
}

@typeName('crm.BookmarkTableRowIdentifierTypedIdComponent')
export class BookmarkTableRowIdentifierTypedIdComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;
}

@typeName('crm.BookmarkTableRowIdentifierStringComponent')
export class BookmarkTableRowIdentifierStringComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;
}

@typeName('crm.BookmarkTableRowIdentifierLongComponent')
export class BookmarkTableRowIdentifierLongComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: number;
}

// --------------------------------------------------

@typeName('suite.ImportSearchData')
export class ImportSearchDataDo extends BaseDoEntity {
  searchData?: DoEntity;
  markAsSaved?: boolean;
}

// --------------------------------------------------

@typeName('crm.ChartTableControlConfig')
export class ChartTableControlConfigDo extends BaseDoEntity {
  chartTypeId?: string;
  chartGroup1ColumnId?: string;
  chartGroup1Modifier?: number;
  chartGroup2ColumnId?: string;
  chartGroup2Modifier?: number;
  chartAggregationColumnId?: string;
  chartAggregationModifier?: number;
}

// --------------------------------------------------

@typeName('crm.TableClientUiPreferences')
export class TableClientUiPreferencesDo extends BaseDoEntity {
  tableId?: string;
  userPreferenceContext?: string;
  tileMode?: boolean;
  tileGlobalKey?: string;
  tablePreferenceProfiles?: Map<string, TableClientUiPreferenceProfileDo>;
}

@typeName('crm.TableClientUiPreferenceProfile')
export class TableClientUiPreferenceProfileDo extends BaseDoEntity {
  columns?: TableColumnClientUiPreferenceDo[];
  userFilters?: IUserFilterStateDo[];
  tableCustomizerData?: ITableCustomizerDo;
}

@typeName('crm.TableColumnClientUiPreference')
export class TableColumnClientUiPreferenceDo extends BaseDoEntity {
  columnId?: string;
  width?: number;
  viewIndex?: number;
  sortOrder?: number;
  sortAscending?: boolean;
  visible?: boolean;
  groupingActive?: boolean;
  aggregationFunctionId?: string;
  backgroundEffectId?: string;
}

export interface IUserFilterStateDo extends DoEntity {
}

@typeName('crm.BooleanColumnUserFilterState')
export class BooleanColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId?: string;
  selectedValues?: Set<boolean>;
}

@typeName('crm.CategoryColorColumnUserFilterState')
export class CategoryColorColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId?: string;
  selectedValues?: Set<string>;
}

@typeName('crm.ChartTableUserFilterState')
export class ChartTableUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  attributeText?: string;
  columnIdX?: string;
  columnIdY?: string;
  stringFilters?: Map<string, string>[];
  numberFilters?: Map<string, number>[];
}

@typeName('crm.ColumnUserFilterState')
export class ColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId?: string;
  selectedValues?: Set<string>;
}

@typeName('crm.DateColumnUserFilterState')
export class DateColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId?: string;
  selectedValues?: Set<number>;
  dateFrom?: Date;
  dateTo?: Date;
}

@typeName('crm.MapTableUserFilterState')
export class MapTableUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId?: string;
  filterIds?: string[];
}

@typeName('crm.NumberColumnUserFilterState')
export class NumberColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId?: string;
  selectedValues?: Set<number>;
  numberFrom?: number;
  numberTo?: number;
}

@typeName('crm.TableTextUserFilterState')
export class TableTextUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  text?: string;
}

@typeName('crm.TextColumnUserFilterState')
export class TextColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId?: string;
  selectedValues?: Set<string>;
  textFilter?: string;
}

export interface ITableCustomizerDo extends DoEntity {
}

// FIXME bsh [js-bookmark] Move to suite/crm
@typeName('crm.CoreTableCustomizer')
export class CoreTableCustomizerDo extends BaseDoEntity implements ITableCustomizerDo {
  columns?: CustomColumnConfigDo[];
}

// FIXME bsh [js-bookmark] Move to suite/crm
@typeName('crm.CustomColumnConfig')
export class CustomColumnConfigDo extends BaseDoEntity {
  id: string;
  selection: DoEntity; // FIXME bsh [js-bookmark] Change to IDataModelSelectionDo
  filterVariantId: string;
  label: string;
  tooltip: string;
  visible: boolean;
  width: number;
  order: number;
  sortIndex: number;
  sortAscending: boolean;
  grouped: boolean;
  multiline: boolean;
}

// --------------------------------------------------

@typeName(PageIdDummyPageParamDo.TYPE_NAME)
export class PageIdDummyPageParamDo extends PageParamDo {
  static TYPE_NAME = 'crm.PageIdDummyPageParam';

  pageId: string;
}

// --------------------------------------------------

@typeName('suite.ActivateBookmarkRequest')
export class ActivateBookmarkRequestDo extends BaseDoEntity {
  parentBookmarkPage: IBookmarkPageDo;
  pagePath: IBookmarkPageDo[];
}

@typeName('suite.BookmarkDoBuilderOptions')
export class BookmarkDoBuilderOptionsDo extends BaseDoEntity {
  createOutline?: boolean;
  persistableRequired?: boolean;
  fallbackAllowed?: boolean;
  createTitle?: boolean;
  createDescription?: boolean;
  createTablePreferences?: boolean;
  createTableRowSelections?: boolean;
}

// --------------------------------------------------

export const bookmarks = {

  // FIXME bsh [js-bookmark] Remove the following debugging/testing methods

  /** @deprecated ONLY FOR TESTING PURPOSES - DO NOT USE */
  async create(): Promise<BookmarkDo> {
    const bookmarkSupport = BookmarkSupport.get();
    let bookmark = await bookmarkSupport.createBookmark();
    await bookmarkSupport.storeBookmark(bookmark);
    return bookmark;
  },

  /** @deprecated ONLY FOR TESTING PURPOSES - DO NOT USE */
  async open(index?: number): Promise<BookmarkDo> {
    const bookmarkSupport = BookmarkSupport.get() as BookmarkSupport & { _getBookmarkStore(): BookmarkDo[] };
    const bookmarkStore = bookmarkSupport._getBookmarkStore();
    if (arrays.empty(bookmarkStore)) {
      return null;
    }
    let bookmark = bookmarkStore[index < 0 ? bookmarkStore.length + index : index] || arrays.last(bookmarkStore);
    await bookmarkSupport.activateBookmark(bookmark);
    return bookmark;
  },

  /** @deprecated ONLY FOR TESTING PURPOSES - DO NOT USE */
  all(): BookmarkDo[] {
    const bookmarkSupport = BookmarkSupport.get() as BookmarkSupport & { _getBookmarkStore(): BookmarkDo[] };
    return bookmarkSupport._getBookmarkStore();
  },

  /** @deprecated ONLY FOR TESTING PURPOSES - DO NOT USE */
  delete(index: number): BookmarkDo {
    const bookmarkSupport = BookmarkSupport.get() as BookmarkSupport & {
      _getBookmarkStore(): BookmarkDo[];
      _setBookmarkStore(bookmarkStore: BookmarkDo[]);
    };
    const bookmarkStore = bookmarkSupport._getBookmarkStore();
    if (arrays.empty(bookmarkStore)) {
      return null;
    }
    let bookmark = bookmarkStore[index < 0 ? bookmarkStore.length + index : index];
    if (bookmark) {
      arrays.remove(bookmarkStore, bookmark);
      bookmarkSupport._setBookmarkStore(bookmarkStore);
    }
    return bookmark;
  }
} as const;
