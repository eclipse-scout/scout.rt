/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.shared.data.basic.chart;

import org.eclipse.scout.rt.platform.annotations.IgnoreProperty;

public class ChartAxisBean implements IChartAxisBean {

  private static final long serialVersionUID = 1L;

  private Object m_axisKey;
  private String m_label;

  public ChartAxisBean() {
  }

  public ChartAxisBean(Object axisKey, String label) {
    m_axisKey = axisKey;
    m_label = label;
  }

  @Override
  @IgnoreProperty
  public Object getAxisKey() {
    return m_axisKey;
  }

  @Override
  @IgnoreProperty
  public void setAxisKey(Object axisKey) {
    m_axisKey = axisKey;
  }

  @Override
  public String getLabel() {
    return m_label;
  }

  @Override
  public void setLabel(String label) {
    m_label = label;
  }

}
