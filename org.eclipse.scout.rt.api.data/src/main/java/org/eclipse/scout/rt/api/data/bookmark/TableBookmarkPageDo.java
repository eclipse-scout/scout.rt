/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.bookmark;

import java.util.Collection;
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.page.IPageParamDo;
import org.eclipse.scout.rt.api.data.page.ISearchDo;
import org.eclipse.scout.rt.api.data.table.TableClientUiPreferenceProfileDo;
import org.eclipse.scout.rt.api.data.table.TableClientUiPreferencesDo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("scout.TableBookmarkPage")
@TypeVersion(Scout_25_2_001.class)
public class TableBookmarkPageDo extends DoEntity implements IBookmarkPageDo {

  public DoValue<IPageParamDo> pageParam() {
    return doValue(PAGE_PARAM);
  }

  public DoValue<String> displayText() {
    return doValue("displayText");
  }

  public DoValue<BookmarkTableRowIdentifierDo> expandedChildRow() {
    return doValue("expandedChildRow");
  }

  public DoSet<BookmarkTableRowIdentifierDo> selectedChildRows() {
    return doSet("selectedChildRows");
  }

  public DoValue<TableClientUiPreferencesDo> tablePreferences() {
    return doValue("tablePreferences");
  }

  public DoValue<Boolean> searchFilterComplete() {
    return doValue("searchFilterComplete");
  }

  public DoValue<ISearchDo> searchData() {
    return doValue("searchData");
  }

  public DoValue<IChartTableControlConfigDo> chartTableControlConfig() {
    return doValue("chartTableControlConfig");
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public TableClientUiPreferenceProfileDo getBookmarkedTablePreference() {
    return getTablePreferences() != null ? getTablePreferences().getBookmarkedTablePreferenceProfile() : null;
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withPageParam(IPageParamDo pageParam) {
    pageParam().set(pageParam);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public IPageParamDo getPageParam() {
    return pageParam().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withDisplayText(String displayText) {
    displayText().set(displayText);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public String getDisplayText() {
    return displayText().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withExpandedChildRow(BookmarkTableRowIdentifierDo expandedChildRow) {
    expandedChildRow().set(expandedChildRow);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkTableRowIdentifierDo getExpandedChildRow() {
    return expandedChildRow().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withSelectedChildRows(Collection<? extends BookmarkTableRowIdentifierDo> selectedChildRows) {
    selectedChildRows().updateAll(selectedChildRows);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withSelectedChildRows(BookmarkTableRowIdentifierDo... selectedChildRows) {
    selectedChildRows().updateAll(selectedChildRows);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<BookmarkTableRowIdentifierDo> getSelectedChildRows() {
    return selectedChildRows().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withTablePreferences(TableClientUiPreferencesDo tablePreferences) {
    tablePreferences().set(tablePreferences);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableClientUiPreferencesDo getTablePreferences() {
    return tablePreferences().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withSearchFilterComplete(Boolean searchFilterComplete) {
    searchFilterComplete().set(searchFilterComplete);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getSearchFilterComplete() {
    return searchFilterComplete().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isSearchFilterComplete() {
    return nvl(getSearchFilterComplete());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withSearchData(ISearchDo searchData) {
    searchData().set(searchData);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ISearchDo getSearchData() {
    return searchData().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableBookmarkPageDo withChartTableControlConfig(IChartTableControlConfigDo chartTableControlConfig) {
    chartTableControlConfig().set(chartTableControlConfig);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IChartTableControlConfigDo getChartTableControlConfig() {
    return chartTableControlConfig().get();
  }
}
