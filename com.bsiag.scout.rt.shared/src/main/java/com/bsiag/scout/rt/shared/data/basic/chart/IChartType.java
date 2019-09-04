/*
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.shared.data.basic.chart;

/**
 * @since 5.2
 */
public interface IChartType {

  int PIE = 1;
  int LINE = 2;
  int BAR_VERTICAL = 3;
  int BAR_HORIZONTAL = 4;
  int SCATTER = 5;
  int FULFILLMENT = 6;
  int SPEEDO = 7;
  int SALESFUNNEL = 8;
  int VENN = 9;
  int DONUT = 10;

  int SPEEDO_GREEN_AREA_POSITION_LEFT = 1;
  int SPEEDO_GREEN_AREA_POSITION_CENTER = 2;
  int SPEEDO_GREEN_AREA_POSITION_RIGHT = 3;

  /**
   * Possible values see <code>SPEEDO_GREEN_AREA_*</code> constants.
   *
   * @see SpeedoChartRenderer.js
   */
  String PROP_SPEEDO_CHART_GREEN_AREA_POSITION = "greenAreaPosition";

  String PROP_SALESFUNNEL_NORMALIZED = "normalized";
  String PROP_SALESFUNNEL_CALC_CONVERSION_RATE = "calcConversionRate";

  String PROP_VENN_NUMBER_OF_CIRCLES = "NumberOfCircles";

  /**
   * Indicates the value from which the animation should be started. If not set, the animation starts from 0.
   */
  String PROP_FULFILLMENT_START_VALUE = "startValue";
}
