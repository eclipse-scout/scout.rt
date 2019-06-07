/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.basic.chart;

import java.math.BigDecimal;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

/**
 * @since 5.2
 */
public class ChartEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_VALUE_CLICK = 1;

  private final int m_type;
  private int m_valueIndex;
  private BigDecimal m_value;
  private int m_groupIndex;
  private String m_groupName;

  public ChartEvent(IChart source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public IChart getSource() {
    return (IChart) super.getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public BigDecimal getValue() {
    return m_value;
  }

  public void setValue(BigDecimal value) {
    m_value = value;
  }

  public int getValueIndex() {
    return m_valueIndex;
  }

  public void setValueIndex(int valueIndex) {
    m_valueIndex = valueIndex;
  }

  public int getGroupIndex() {
    return m_groupIndex;
  }

  public void setGroupIndex(int groupIndex) {
    m_groupIndex = groupIndex;
  }

  public String getGroupName() {
    return m_groupName;
  }

  public void setGroupName(String groupName) {
    m_groupName = groupName;
  }

}
