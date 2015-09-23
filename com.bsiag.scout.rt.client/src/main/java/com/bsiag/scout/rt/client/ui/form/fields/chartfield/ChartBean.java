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
package com.bsiag.scout.rt.client.ui.form.fields.chartfield;

import java.util.List;

/**
 *
 */
public class ChartBean implements IChartBean {
  private List<List<String>> m_axes;
  private List<IChartValueGroupBean> m_valueGroups;

  @Override
  public void setAxes(List<List<String>> axes) {
    m_axes = axes;
  }

  @Override
  public List<List<String>> getAxes() {
    return m_axes;
  }

  @Override
  public List<IChartValueGroupBean> getChartValueGroups() {
    return m_valueGroups;
  }

  @Override
  public void setChartValueGroup(List<IChartValueGroupBean> valueGroups) {
    m_valueGroups = valueGroups;
  }

}
