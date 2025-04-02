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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.table.ChartTableControlChartTypeId;
import org.eclipse.scout.rt.api.data.table.TableColumnId;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("scout.ChartTableControlConfig")
@TypeVersion(Scout_25_2_001.class)
public class ChartTableControlConfigDo extends DoEntity {

  public DoValue<ChartTableControlChartTypeId> chartTypeId() {
    return doValue("chartTypeId");
  }

  public DoValue<TableColumnId> chartGroup1ColumnId() {
    return doValue("chartGroup1ColumnId");
  }

  public DoValue<Integer> chartGroup1Modifier() {
    return doValue("chartGroup1Modifier");
  }

  public DoValue<TableColumnId> chartGroup2ColumnId() {
    return doValue("chartGroup2ColumnId");
  }

  public DoValue<Integer> chartGroup2Modifier() {
    return doValue("chartGroup2Modifier");
  }

  public DoValue<TableColumnId> chartAggregationColumnId() {
    return doValue("chartAggregationColumnId");
  }

  public DoValue<Integer> chartAggregationModifier() {
    return doValue("chartAggregationModifier");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ChartTableControlConfigDo withChartTypeId(ChartTableControlChartTypeId chartTypeId) {
    chartTypeId().set(chartTypeId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ChartTableControlChartTypeId getChartTypeId() {
    return chartTypeId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ChartTableControlConfigDo withChartGroup1ColumnId(TableColumnId chartGroup1ColumnId) {
    chartGroup1ColumnId().set(chartGroup1ColumnId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnId getChartGroup1ColumnId() {
    return chartGroup1ColumnId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ChartTableControlConfigDo withChartGroup1Modifier(Integer chartGroup1Modifier) {
    chartGroup1Modifier().set(chartGroup1Modifier);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getChartGroup1Modifier() {
    return chartGroup1Modifier().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ChartTableControlConfigDo withChartGroup2ColumnId(TableColumnId chartGroup2ColumnId) {
    chartGroup2ColumnId().set(chartGroup2ColumnId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnId getChartGroup2ColumnId() {
    return chartGroup2ColumnId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ChartTableControlConfigDo withChartGroup2Modifier(Integer chartGroup2Modifier) {
    chartGroup2Modifier().set(chartGroup2Modifier);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getChartGroup2Modifier() {
    return chartGroup2Modifier().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ChartTableControlConfigDo withChartAggregationColumnId(TableColumnId chartAggregationColumnId) {
    chartAggregationColumnId().set(chartAggregationColumnId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TableColumnId getChartAggregationColumnId() {
    return chartAggregationColumnId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ChartTableControlConfigDo withChartAggregationModifier(Integer chartAggregationModifier) {
    chartAggregationModifier().set(chartAggregationModifier);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getChartAggregationModifier() {
    return chartAggregationModifier().get();
  }
}
