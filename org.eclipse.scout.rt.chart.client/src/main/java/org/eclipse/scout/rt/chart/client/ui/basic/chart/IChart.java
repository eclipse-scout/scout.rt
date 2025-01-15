/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.client.ui.basic.chart;

import org.eclipse.scout.rt.chart.shared.data.basic.chart.IChartConfig;
import org.eclipse.scout.rt.chart.shared.data.basic.chart.IChartData;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

/**
 * @since 5.2
 */
public interface IChart extends IWidget {

  String PROP_DATA = "data";
  String PROP_CONFIG = "config";

  String PROP_VISIBLE = "visible";

  IChartUIFacade getUIFacade();

  IFastListenerList<ChartListener> chartListeners();

  default void addChartListener(ChartListener listener) {
    chartListeners().add(listener);
  }

  default void removeChartListener(ChartListener listener) {
    chartListeners().remove(listener);
  }

  void setData(IChartData data);

  IChartData getData();

  void setConfig(IChartConfig config);

  IChartConfig getConfig();

  /**
   * Resets the config to its initial value (e.g. the value of a {@code getConfigured}-method).
   */
  void resetConfig();

  /**
   * Extends the current config with the given {@link IChartConfig}.
   *
   * @param config
   *     An {@link IChartConfig} object whose properties should be copied to the current config.
   * @param override
   *     Whether properties in the current config should be overridden or not.
   */
  void extendConfig(IChartConfig config, boolean override);

  void setVisible(boolean visible);

  boolean isVisible();
}
