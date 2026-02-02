/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, PageParamDo, typeName} from './index';

export interface IBookmarkDo extends BaseDoEntity {
  definition: IBookmarkDefinitionDo;
}

@typeName('scout.Bookmark')
export class BookmarkDo extends BaseDoEntity implements IBookmarkDo {
  definition: IBookmarkDefinitionDo;
  id: string;
  title: string;
  description: string;
}

// --------------------------------------------------

export interface IBookmarkDefinitionDo extends BaseDoEntity {
  bookmarkedPage: IBookmarkPageDo;
}

@typeName('scout.OutlineBookmarkDefinition')
export class OutlineBookmarkDefinitionDo extends BaseDoEntity implements IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
  outlineId: string;
  /** Path from the outline's root to the {@link bookmarkedPage} */
  pagePath: IBookmarkPageDo[];
}

@typeName('scout.PageBookmarkDefinition')
export class PageBookmarkDefinitionDo extends BaseDoEntity implements IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
}

// --------------------------------------------------

export interface IBookmarkPageDo extends BaseDoEntity {
  pageParam?: PageParamDo;
  displayText?: string;
}

@typeName('scout.NodeBookmarkPage')
export class NodeBookmarkPageDo extends BaseDoEntity implements IBookmarkPageDo {
  pageParam: PageParamDo;
  displayText: string;
}

@typeName('scout.TableBookmarkPage')
export class TableBookmarkPageDo extends BaseDoEntity implements IBookmarkPageDo {
  pageParam: PageParamDo;
  displayText: string;
  expandedChildRow: BookmarkTableRowIdentifierDo;
  selectedChildRows: BookmarkTableRowIdentifierDo[];
  searchFilterComplete: boolean;
  searchData: ISearchDo;
  tablePreferences: TableClientUiPreferencesDo;
  chartTableControlConfig: IChartTableControlConfigDo;
}

export interface IChartTableControlConfigDo extends BaseDoEntity {
}

export interface ISearchDo extends BaseDoEntity {
}

@typeName('scout.BookmarkTableRowIdentifier')
export class BookmarkTableRowIdentifierDo extends BaseDoEntity {
  keyComponents: IBookmarkTableRowIdentifierComponentDo[];
}

export interface IBookmarkTableRowIdentifierComponentDo extends BaseDoEntity {
}

/**
 * Never serialize this!
 */
@typeName('scout.BookmarkTableRowIdentifierObjectComponent')
export class BookmarkTableRowIdentifierObjectComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: any;
}

@typeName('scout.BookmarkTableRowIdentifierDateComponent')
export class BookmarkTableRowIdentifierDateComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: Date;
}

@typeName('scout.BookmarkTableRowIdentifierBooleanComponent')
export class BookmarkTableRowIdentifierBooleanComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: boolean;
}

@typeName('scout.BookmarkTableRowIdentifierIntegerComponent')
export class BookmarkTableRowIdentifierIntegerComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: number;
}

@typeName('scout.BookmarkTableRowIdentifierStringComponent')
export class BookmarkTableRowIdentifierStringComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;
}

@typeName('scout.BookmarkTableRowIdentifierLongComponent')
export class BookmarkTableRowIdentifierLongComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: number;
}

// --------------------------------------------------

@typeName('scout.TableClientUiPreferences')
export class TableClientUiPreferencesDo extends BaseDoEntity {
  tableId: string;
  userPreferenceContext: string;
  tileMode: boolean;
  tileGlobalKey: string;
  tablePreferenceProfiles: Map<string, TableClientUiPreferenceProfileDo>;
}

@typeName('scout.TableClientUiPreferenceProfile')
export class TableClientUiPreferenceProfileDo extends BaseDoEntity {
  columns: TableColumnClientUiPreferenceDo[];
  userFilters: IUserFilterStateDo[];
  tableCustomizerData: ITableCustomizerDo;
}

@typeName('scout.TableColumnClientUiPreference')
export class TableColumnClientUiPreferenceDo extends BaseDoEntity {
  columnId: string;
  viewIndex: number;
  visible: boolean;
  width: number;
  sortOrder: number;
  sortAscending: boolean;
  groupingActive: boolean;
  aggregationFunctionId: string;
  backgroundEffectId: string;
}

export interface IUserFilterStateDo extends BaseDoEntity {
}

@typeName('scout.BooleanColumnUserFilterState')
export class BooleanColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId: string;
  selectedValues: Set<boolean>;
}

@typeName('scout.ColumnUserFilterState')
export class ColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId: string;
  selectedValues: Set<string>;
}

@typeName('scout.DateColumnUserFilterState')
export class DateColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId: string;
  selectedValues: Set<number>;
  dateFrom: Date;
  dateTo: Date;
}

@typeName('scout.NumberColumnUserFilterState')
export class NumberColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId: string;
  selectedValues: Set<number>;
  numberFrom: number;
  numberTo: number;
}

@typeName('scout.TableTextUserFilterState')
export class TableTextUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  text: string;
}

@typeName('scout.TextColumnUserFilterState')
export class TextColumnUserFilterStateDo extends BaseDoEntity implements IUserFilterStateDo {
  columnId: string;
  selectedValues: Set<string>;
  textFilter: string;
}

/**
 * Marker interface for the "customizer data" of a {@link TableCustomizer}.
 */
export interface ITableCustomizerDo extends BaseDoEntity {
}

/**
 * Marker interface for a data object describing a "column configuration". Used when working with a {@link TableCustomizer}.
 */
export interface IColumnConfigDo extends BaseDoEntity {
}

// --------------------------------------------------

@typeName('scout.UiPreferences')
export class UiPreferencesDo extends BaseDoEntity {
  tablePreferences: TableClientUiPreferencesDo[];
}

@typeName('scout.UiPreferencesUpdate')
export class UiPreferencesUpdateDo extends BaseDoEntity {
  preferences: UiPreferencesDo;
}
