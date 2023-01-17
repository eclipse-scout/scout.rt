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

import org.eclipse.scout.rt.platform.annotations.IgnoreProperty;

public abstract class AbstractChartValueGroupBean implements IChartValueGroupBean {
  private static final long serialVersionUID = 1L;

  private String m_type;
  private Object m_groupKey;
  private String m_groupName;
  private String m_colorHexValue;
  private boolean m_clickable = true;

  @Override
  public String getType() {
    return m_type;
  }

  @Override
  public void setType(String type) {
    m_type = type;
  }

  @Override
  @IgnoreProperty
  public Object getGroupKey() {
    return m_groupKey;
  }

  @Override
  @IgnoreProperty
  public void setGroupKey(Object groupKey) {
    m_groupKey = groupKey;
  }

  @Override
  public String getGroupName() {
    return m_groupName;
  }

  @Override
  public void setGroupName(String groupName) {
    m_groupName = groupName;
  }

  @Override
  public String getColorHexValue() {
    return m_colorHexValue;
  }

  @Override
  public void setColorHexValue(String colorHexValue) {
    m_colorHexValue = colorHexValue;
  }

  @Override
  public boolean isClickable() {
    return m_clickable;
  }

  @Override
  public void setClickable(boolean clickable) {
    this.m_clickable = clickable;
  }
}
