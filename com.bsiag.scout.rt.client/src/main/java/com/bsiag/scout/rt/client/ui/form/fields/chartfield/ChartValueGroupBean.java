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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ChartValueGroupBean implements IChartValueGroupBean {
  private String m_groupName;
  private List<BigDecimal> m_values = new ArrayList<BigDecimal>();
  private List<String> m_colorHexValue = new ArrayList<String>();

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
  public void setValues(List<BigDecimal> values) {
    m_values = values;
  }

  @Override
  public List<String> getColorHexValue() {
    return m_colorHexValue;
  }

  @Override
  public void setColorHexValue(List<String> colorHexValue) {
    m_colorHexValue = colorHexValue;
  }
}
