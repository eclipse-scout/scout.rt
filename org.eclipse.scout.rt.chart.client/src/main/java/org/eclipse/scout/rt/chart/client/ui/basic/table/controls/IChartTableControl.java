/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.client.ui.basic.table.controls;

import org.eclipse.scout.rt.chart.shared.data.basic.chart.IChartType;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;

public interface IChartTableControl extends ITableControl {
  String PROP_CHART_TYPE = "chartType";
  String PROP_CHART_GROUP_1 = "chartGroup1";
  String PROP_CHART_GROUP_2 = "chartGroup2";
  String PROP_CHART_AGGREGATION = "chartAggregation";

  /**
   * Sets the chart type
   *
   * @see IChartType
   */
  void setChartType(String chartType);

  /**
   * @return chart type
   * @see IChartType
   */
  String getChartType();

  /**
   * set the configuration parameters for the first group represented by the first axis in the chart
   */
  void setGroup1(IChartColumnParam param);

  /**
   * configuration parameters for the first group represented by the first axis in the chart
   */
  IChartColumnParam getGroup1();

  /**
   * set the configuration parameters for the (optional) second group represented for the chart (necessary for bubble
   * chart)
   */
  void setGroup2(IChartColumnParam param);

  /**
   * configuration parameters for the (optional) second group for the chart (necessary for bubble chart)
   */
  IChartColumnParam getGroup2();

  /**
   * set the configuration parameters for the aggregated data for the chart
   */
  void setAggregation(IChartColumnParam param);

  /**
   * configuration for the aggregated data for the chart
   */
  IChartColumnParam getAggregation();
}
