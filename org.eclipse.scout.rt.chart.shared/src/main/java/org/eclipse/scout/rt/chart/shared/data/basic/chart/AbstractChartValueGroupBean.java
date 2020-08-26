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

import org.eclipse.scout.rt.platform.annotations.IgnoreProperty;

public abstract class AbstractChartValueGroupBean implements IChartValueGroupBean {
  private static final long serialVersionUID = 1L;

  private String m_type;
  private Object m_groupKey;
  private String m_groupName;
  private String m_colorHexValue;
  private String m_cssClass;
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
  public String getCssClass() {
    return m_cssClass;
  }

  @Override
  public void setCssClass(String cssClass) {
    m_cssClass = cssClass;
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
