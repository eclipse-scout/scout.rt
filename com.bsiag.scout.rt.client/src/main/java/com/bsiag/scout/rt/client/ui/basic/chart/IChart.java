/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.client.ui.basic.chart;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

import com.bsiag.scout.rt.shared.data.basic.chart.IChartBean;

/**
 * @since 5.2
 */
public interface IChart extends IWidget {

  String PROP_AUTO_COLOR = "autoColor";
  String PROP_CHART_TYPE = "chartType";
  String PROP_CHART_DATA = "chartData";
  String PROP_VISIBLE = "visible";
  String PROP_MAX_SEGMENTS = "maxSegments";
  String PROP_CLICKABLE = "clickable";
  String PROP_ANIMATED = "animated";
  String PROP_LEGEND_VISIBLE = "legendVisible";
  String PROP_INTERACTIVE_LEGEND_VISIBLE = "interactiveLegendVisible";
  String PROP_LEGEND_POSITION = "legendPosition";

  int DEFAULT_MAX_SEGMENTS_PIE = 5;

  int LEGEND_POSITION_BOTTOM = 0;
  int LEGEND_POSITION_TOP = 2;
  int LEGEND_POSITION_RIGHT = 4;
  int LEGEND_POSITION_LEFT = 5;

  IChartUIFacade getUIFacade();

  /**
   * @deprecated Will be removed in Scout 11. Use {@link #getParent()} or {@link #getParentOfType(Class)} instead.
   */
  @Deprecated
  ITypeWithClassId getContainer();

  IFastListenerList<ChartListener> chartListeners();

  default void addChartListener(ChartListener listener) {
    chartListeners().add(listener);
  }

  default void removeChartListener(ChartListener listener) {
    chartListeners().remove(listener);
  }

  /**
   * Triggers a property change event for {@link #PROP_CHART_DATA}. Useful when the contents of the chart data bean has
   * been changed but not the bean itself (property support does not fire events when the same object is set again).
   */
  void fireChartDataChanged();

  void setChartType(int chartType);

  int getChartType();

  void setAutoColor(boolean autoColor);

  boolean isAutoColor();

  void setChartData(IChartBean chartData);

  IChartBean getChartData();

  void setVisible(boolean visible);

  boolean isVisible();

  /**
   * used for pie charts to limit segments-> if set, smallest segments are collapsed
   */
  int getMaxSegments();

  /**
   * used for pie charts to limit segments-> if set, smallest segments are collapsed
   */
  void setMaxSegments(int maxSegments);

  void setClickable(boolean clickable);

  boolean isClickable();

  boolean isAnimated();

  void setAnimated(boolean animated);

  void setLegendPosition(int position);

  int getLegendPosition();

  boolean isLegendVisible();

  void setLegendVisible(boolean visible);

  boolean isInteractiveLegendVisible();

  void setInteractiveLegendVisible(boolean visible);
}
