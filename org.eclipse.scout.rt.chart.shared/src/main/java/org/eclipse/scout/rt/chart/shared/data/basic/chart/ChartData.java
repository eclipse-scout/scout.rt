/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 5.2
 */
public class ChartData implements IChartData {
  private static final long serialVersionUID = 1L;

  private final List<List<IChartAxisBean>> m_axes = new ArrayList<>();
  private final List<IChartValueGroupBean> m_valueGroups = new ArrayList<>();

  @Override
  public List<List<IChartAxisBean>> getAxes() {
    return m_axes;
  }

  @Override
  public List<IChartValueGroupBean> getChartValueGroups() {
    return m_valueGroups;
  }
}
