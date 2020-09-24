/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.client.ui.basic.chart;

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
  private BigDecimal m_xIndex;
  private BigDecimal m_yIndex;
  private Integer m_datasetIndex;

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

  public BigDecimal getXIndex() {
    return m_xIndex;
  }

  public void setXIndex(BigDecimal xIndex) {
    m_xIndex = xIndex;
  }

  public BigDecimal getYIndex() {
    return m_yIndex;
  }

  public void setYIndex(BigDecimal yIndex) {
    m_yIndex = yIndex;
  }

  public Integer getDatasetIndex() {
    return m_datasetIndex;
  }

  public void setDatasetIndex(Integer datasetIndex) {
    m_datasetIndex = datasetIndex;
  }
}
