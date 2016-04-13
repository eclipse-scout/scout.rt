/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;

import com.bsiag.scout.rt.shared.data.basic.chart.IChartType;

public interface IChartTableControl extends ITableControl {
  String PROP_CHART_TYPE = "chartType";
  String PROP_CHART_GROUP_1 = "chartGroup1";
  String PROP_CHART_GROUP_2 = "chartGroup2";
  String PROP_CHART_AGGRAGATION = "chartAggregation";

  /**
   * Sets the chart type
   *
   * @see IChartType
   */
  void setChartType(int chartType);

  /**
   * @return chart type
   * @see IChartType
   */
  int getChartType();

  /**
   * set the configuration parameters for the first group for the chart
   */
  void setGroup1(IChartColumnParam param);

  /**
   * configuration parameters for the first group for the chart
   */
  IChartColumnParam getGroup1();

  /**
   * set the configuration parameters for the second group for the chart (optional)
   */
  void setGroup2(IChartColumnParam param);

  /**
   * configuration parameters for the second group for the chart
   */
  IChartColumnParam getGroup2();

  /**
   * set the configuration parameters for the aggregation for the chart
   */
  void setAggregation(IChartColumnParam param);

  /**
   * configuration parameters for the aggregation for the chart
   */
  IChartColumnParam getAggregation();

}
