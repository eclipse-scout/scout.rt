/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.shared.data.basic.chart;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Support for the config of a chart. One can add, remove or modify properties using this wrapper class. In the end an
 * object of this class is transformed into JSON format like
 *
 * <pre>
 * {
 *   type: "bar",
 *   options: {
 *     legend: {
 *       position: "bottom"
 *     }
 *   }
 * }
 * </pre>
 *
 * and represents the config of a chart.
 */
@Bean
public interface IChartConfig extends Serializable {

  String TOP = "top";
  String BOTTOM = "bottom";
  String LEFT = "left";
  String RIGHT = "right";
  String CENTER = "center";

  int DEFAULT_MAX_SEGMENTS_PIE = 5;

  String SPEEDO_CHART_GREEN_AREA_POSITION = "speedo.greenAreaPosition";

  String SALESFUNNEL_NORMALIZED = "salesfunnel.normalized";
  String SALESFUNNEL_CALC_CONVERSION_RATE = "salesfunnel.calcConversionRate";

  String VENN_NUMBER_OF_CIRCLES = "venn.numberOfCircles";

  /**
   * Indicates the value from which the animation should be started. If not set, the animation starts from 0.
   */
  String FULFILLMENT_START_VALUE = "fulfillment.startValue";

  IChartConfig copy();

  /**
   * Add a property.
   *
   * @param name
   *          The name of the property. Recursive properties have to be separated by "." and indices of arrays have to
   *          be specified in the form "[i]" (e.g. "options.legend.position", "options.scales.xAxes[0].scaleLabel").
   * @param value
   *          The value of the property. Has to be not null.
   * @return The modified {@link ChartConfig} object.
   */
  IChartConfig withProperty(String name, Object value);

  /**
   * Remove a property.
   *
   * @param name
   *          The name of the property. Recursive properties have to be separated by "." and indices of arrays have to
   *          be specified in the form "[i]" (e.g. "options.legend.position", "options.scales.xAxes[0].scaleLabel").
   * @return The modified {@link ChartConfig} object.
   */
  IChartConfig removeProperty(String name);

  /**
   * Get a property.
   *
   * @param name
   *          The name of the property. Recursive properties have to be separated by "." and indices of arrays have to
   *          be specified in the form "[i]" (e.g. "options.legend.position", "options.scales.xAxes[0].scaleLabel").
   * @return The value of the property or {@code null} if it is not set.
   */
  Object getProperty(String name);

  Map<String, Object> getProperties();

  IChartConfig addProperties(IChartConfig config, boolean override);

  IChartConfig withType(String type);

  IChartConfig removeType();

  String getType();

  IChartConfig withAutoColor(boolean autoColor);

  IChartConfig removeAutoColor();

  boolean isAutoColor();

  IChartConfig withMaxSegments(int maxSegments);

  IChartConfig removeMaxSegments();

  int getMaxSegments();

  IChartConfig withClickable(boolean clickable);

  IChartConfig removeClickable();

  boolean isClickable();

  IChartConfig withAnimated(boolean animated);

  IChartConfig removeAnimated();

  boolean isAnimated();

  IChartConfig withTooltipsEnabled(boolean tooltipsEnabled);

  IChartConfig removeTooltipsEnabled();

  boolean isTooltipsEnabled();

  IChartConfig withLegendDisplay(boolean legendDisplay);

  IChartConfig removeLegendDisplay();

  boolean isLegendDisplay();

  IChartConfig withLegendPosition(String legendPosition);

  IChartConfig removeLegendPosition();

  String getLegendPosition();

  IChartConfig withLegendPositionTop();

  IChartConfig withLegendPositionBottom();

  IChartConfig withLegendPositionLeft();

  IChartConfig withLegendPositionRight();

  IChartConfig withXAxesLabel(String label);

  IChartConfig removeXAxesLabel();

  String getXAxesLabel();

  IChartConfig withYAxesLabel(String label);

  IChartConfig removeYAxesLabel();

  String getYAxesLabel();
}
