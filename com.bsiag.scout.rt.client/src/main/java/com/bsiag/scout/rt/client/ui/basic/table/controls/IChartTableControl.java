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

}
