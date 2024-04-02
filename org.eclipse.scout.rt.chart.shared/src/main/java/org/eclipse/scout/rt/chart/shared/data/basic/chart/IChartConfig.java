/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.shared.data.colorscheme.IColorScheme;

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

  String LEGEND_LABELS_BOX_WIDTH = "options.plugins.legend.labels.boxWidth";

  String SPEEDO_CHART_GREEN_AREA_POSITION = "options.speedo.greenAreaPosition";

  String SALESFUNNEL_NORMALIZED = "options.salesfunnel.normalized";
  String SALESFUNNEL_CALC_CONVERSION_RATE = "options.salesfunnel.calcConversionRate";

  String VENN_NUMBER_OF_CIRCLES = "options.venn.numberOfCircles";

  /**
   * Indicates the value from which the animation should be started. If not set, the animation starts from 0.
   */
  String FULFILLMENT_START_VALUE = "options.fulfillment.startValue";

  String BUBBLE_SIZE_OF_LARGEST_BUBBLE = "options.bubble.sizeOfLargestBubble";
  String BUBBLE_MIN_BUBBLE_SIZE = "options.bubble.minBubbleSize";

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

  /**
   * Get property map.
   *
   * @return A recursive map with all properties. E.g.:
   *
   *         <pre>
   *         "options"<br>
   *          - "legend"<br>
   *            - "position": "bottom"<br>
   *            - "display": true<br>
   *         </pre>
   */
  Map<String, Object> getProperties();

  /**
   * Get flat property map.
   *
   * @return A flat map with all properties. E.g.:
   *
   *         <pre>
   *         "options.legend.position": "bottom"<br>
   *         "options.legend.display": true<br>
   *         </pre>
   */
  Map<String, Object> getPropertiesFlat();

  IChartConfig addProperties(IChartConfig config, boolean override);

  IChartConfig withType(String type);

  IChartConfig removeType();

  String getType();

  IChartConfig withAutoColor(boolean autoColor);

  IChartConfig removeAutoColor();

  boolean isAutoColor();

  IChartConfig withColorMode(ColorMode colorMode);

  IChartConfig removeColorMode();

  ColorMode getColorMode();

  IChartConfig withColorScheme(IColorScheme colorScheme);

  IChartConfig removeColorScheme();

  IColorScheme getColorScheme();

  IChartConfig withTransparent(boolean transparent);

  IChartConfig removeTransparent();

  boolean isTransparent();

  IChartConfig withMaxSegments(int maxSegments);

  IChartConfig removeMaxSegments();

  int getMaxSegments();

  IChartConfig withClickable(boolean clickable);

  IChartConfig removeClickable();

  boolean isClickable();

  IChartConfig withCheckable(boolean checkable);

  IChartConfig removeCheckable();

  boolean isCheckable();

  IChartConfig withAnimationDuration(int duration);

  IChartConfig removeAnimationDuration();

  int getAnimationDuration();

  IChartConfig withAnimated(boolean animated);

  IChartConfig removeAnimated();

  boolean isAnimated();

  IChartConfig withTooltipsEnabled(boolean tooltipsEnabled);

  IChartConfig removeTooltipsEnabled();

  boolean isTooltipsEnabled();

  IChartConfig withLegendDisplay(boolean legendDisplay);

  IChartConfig removeLegendDisplay();

  boolean isLegendDisplay();

  IChartConfig withLegendClickable(boolean legendClickable);

  IChartConfig removeLegendClickable();

  boolean isLegendClickable();

  IChartConfig withLegendPosition(String legendPosition);

  IChartConfig removeLegendPosition();

  String getLegendPosition();

  IChartConfig withLegendPositionTop();

  IChartConfig withLegendPositionBottom();

  IChartConfig withLegendPositionLeft();

  IChartConfig withLegendPositionRight();

  IChartConfig withLineTension(BigDecimal tension);

  IChartConfig removeLineTension();

  BigDecimal getLineTension();

  IChartConfig withLineFill(boolean fill);

  IChartConfig removeLineFill();

  boolean isLineFill();

  IChartConfig removeScales();

  IChartConfig withScaleLabelByTypeMap(Map<String, String> scaleLabelByTypeMap);

  IChartConfig removeScaleLabelByTypeMap();

  Map<String, String> getScaleLabelByTypeMap();

  IChartConfig withXLabelMap(Map<String, String> xLabelMap);

  IChartConfig removeXLabelMap();

  Map<String, String> getXLabelMap();

  IChartConfig withXAxisStacked(boolean stacked);

  IChartConfig removeXAxisStacked();

  boolean isXAxisStacked();

  IChartConfig withXAxisLabelDisplay(boolean display);

  IChartConfig removeXAxisLabelDisplay();

  boolean isXAxisLabelDisplay();

  IChartConfig withXAxisLabel(String label);

  IChartConfig removeXAxisLabel();

  String getXAxisLabel();

  IChartConfig withYLabelMap(Map<String, String> yLabelMap);

  IChartConfig removeYLabelMap();

  Map<String, String> getYLabelMap();

  IChartConfig withYAxisStacked(boolean stacked);

  IChartConfig removeYAxisStacked();

  boolean isYAxisStacked();

  IChartConfig withYAxisLabelDisplay(boolean display);

  IChartConfig removeYAxisLabelDisplay();

  boolean isYAxisLabelDisplay();

  IChartConfig withYAxisLabel(String label);

  IChartConfig removeYAxisLabel();

  String getYAxisLabel();

  IChartConfig withDatalabelsDisplay(boolean display);

  IChartConfig removeDatalabelsDisplay();

  boolean isDatalabelsDisplay();
}
