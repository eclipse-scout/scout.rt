/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
