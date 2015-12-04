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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 5.2
 */
public class ChartValueGroupBean implements IChartValueGroupBean {
  private static final long serialVersionUID = 1L;

  private String m_groupName;
  private final List<BigDecimal> m_values = new ArrayList<BigDecimal>();
  private String m_colorHexValue = null;

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
}
