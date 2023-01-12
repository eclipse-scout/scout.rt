/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import org.eclipse.scout.rt.platform.BEANS;

public class ChartBean implements IChartBean {
  private static final long serialVersionUID = 1L;

  private final IChartData m_data;
  private final IChartConfig m_config;

  public ChartBean() {
    this(new ChartData(), BEANS.get(IChartConfig.class));
  }

  public ChartBean(IChartData data, IChartConfig config) {
    m_data = data;
    m_config = config;
  }

  @Override
  public IChartData getData() {
    return m_data;
  }

  @Override
  public IChartConfig getConfig() {
    return m_config;
  }
}
