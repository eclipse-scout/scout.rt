/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.shared.data.basic.chart;

/**
 * @since 5.2
 */
public interface IChartType {

  int PIE = 1;
  int LINE = 2;
  int BAR = 3;
  int BAR_HORIZONTAL = 4;
  int SCATTER = 5;
  int FULFILLMENT = 6;
  int SPEEDO = 7;
  int SALESFUNNEL = 8;
  int VENN = 9;
  int DONUT = 10;

  /**
   * PossibleValues see SpeedoChartRenderer.js:<br/>
   * scout.SpeedoChartRenderer.GREEN_AREA_POSITION_LEFT = 1;<br/>
   * scout.SpeedoChartRenderer.GREEN_AREA_POSITION_CENTER = 2;<br/>
   * scout.SpeedoChartRenderer.GREEN_AREA_POSITION_RIGHT = 3;<br/>
   */
  String PROP_SPEEDO_CHART_GREEN_AREA_POSITION = "greenAreaPosition";

  String PROP_SALESFUNNEL_NORMALIZED = "normalized";
  String PROP_SALESFUNNEL_CALC_CONVERSION_RATE = "calcConversionRate";

  String PROP_VENN_NUMBER_OF_CIRCLES = "NumberOfCircles";
}
