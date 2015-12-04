/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.shared.data.basic.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 5.2
 */
public class ChartBean implements IChartBean {
  private static final long serialVersionUID = 1L;

  private final List<List<String>> m_axes = new ArrayList<List<String>>();
  private final List<IChartValueGroupBean> m_valueGroups = new ArrayList<IChartValueGroupBean>();
  private final Map<String, Object> m_customProperties = new HashMap<String, Object>();

  @Override
  public List<List<String>> getAxes() {
    return m_axes;
  }

  @Override
  public List<IChartValueGroupBean> getChartValueGroups() {
    return m_valueGroups;
  }

  @Override
  public void addCustomProperty(String name, Object prop) {
    m_customProperties.put(name, prop);
  }

  @Override
  public Object getCustomProperty(String name) {
    return m_customProperties.get(name);
  }

  @Override
  public void removeCustomProperty(String name) {
    m_customProperties.remove(name);
  }

  @Override
  public Map<String, Object> getCustomProperties() {
    return m_customProperties;
  }
}
