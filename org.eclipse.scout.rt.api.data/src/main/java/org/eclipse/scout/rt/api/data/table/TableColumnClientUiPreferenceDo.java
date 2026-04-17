/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("scout.TableColumnClientUiPreference")
@TypeVersion(Scout_25_2_001.class)
public class TableColumnClientUiPreferenceDo extends DoEntity {

  public DoValue<TableColumnId> columnId() {
    return doValue("columnId");
  }

  public DoValue<Integer> viewIndex() {
    return doValue("viewIndex");
  }

  public DoValue<Boolean> visible() {
    return doValue("visible");
  }

  public DoValue<Integer> width() {
    return doValue("width");
  }

  public DoValue<Integer> sortOrder() {
    return doValue("sortOrder");
  }

  public DoValue<Boolean> sortAscending() {
    return doValue("sortAscending");
  }

  public DoValue<Boolean> groupingActive() {
    return doValue("groupingActive");
  }

  public DoValue<TableColumnAggregationFunctionId> aggregationFunctionId() {
    return doValue("aggregationFunctionId");
  }

  public DoValue<TableColumnBackgroundEffectId> backgroundEffectId() {
    return doValue("backgroundEffectId");
  }

  public DoValue<DateGroupType> dateGroupType() {
    return doValue("dateGroupType");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withColumnId(TableColumnId columnId) {
    columnId().set(columnId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnId getColumnId() {
    return columnId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withViewIndex(Integer viewIndex) {
    viewIndex().set(viewIndex);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getViewIndex() {
    return viewIndex().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withVisible(Boolean visible) {
    visible().set(visible);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getVisible() {
    return visible().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isVisible() {
    return nvl(getVisible());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withWidth(Integer width) {
    width().set(width);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getWidth() {
    return width().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withSortOrder(Integer sortOrder) {
    sortOrder().set(sortOrder);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getSortOrder() {
    return sortOrder().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withSortAscending(Boolean sortAscending) {
    sortAscending().set(sortAscending);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getSortAscending() {
    return sortAscending().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isSortAscending() {
    return nvl(getSortAscending());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withGroupingActive(Boolean groupingActive) {
    groupingActive().set(groupingActive);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getGroupingActive() {
    return groupingActive().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isGroupingActive() {
    return nvl(getGroupingActive());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withAggregationFunctionId(TableColumnAggregationFunctionId aggregationFunctionId) {
    aggregationFunctionId().set(aggregationFunctionId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnAggregationFunctionId getAggregationFunctionId() {
    return aggregationFunctionId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withBackgroundEffectId(TableColumnBackgroundEffectId backgroundEffectId) {
    backgroundEffectId().set(backgroundEffectId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnBackgroundEffectId getBackgroundEffectId() {
    return backgroundEffectId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnClientUiPreferenceDo withDateGroupType(DateGroupType dateGroupType) {
    dateGroupType().set(dateGroupType);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateGroupType getDateGroupType() {
    return dateGroupType().get();
  }
}
