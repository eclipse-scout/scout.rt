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

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

/**
 * @since 5.2
 */
public class ChartEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_VALUE_CLICK = 1;

  private final int m_type;
  private int m_axisIndex;
  private int m_valueIndex;
  private int m_groupIndex;

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

  public int getAxisIndex() {
    return m_axisIndex;
  }

  public void setAxisIndex(int axisIndex) {
    m_axisIndex = axisIndex;
  }

}
