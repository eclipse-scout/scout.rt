/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.shared.data.basic.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.annotations.IgnoreProperty;

/**
 * @since 5.2
 */
public class ChartValueGroupBean implements IChartValueGroupBean {
  private static final long serialVersionUID = 1L;

  private Object m_groupKey;
  private String m_groupName;
  private final List<BigDecimal> m_values = new ArrayList<>();
  private String m_colorHexValue = null;
  private String m_cssClass = null;

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
  public List<BigDecimal> getValues() {
    return m_values;
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
}
