/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.bookmark;

import java.io.Serializable;

public class TableColumnState implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  private String m_className;
  private int m_width;
  private int m_sortOrder = -1;
  private boolean m_sortAscending;
  private Boolean m_displayable;
  private Boolean m_visible;
  private byte[] m_columnFilterData;
  private boolean m_groupingActive;
  private String m_aggregationFunction;
  private String m_backgroundEffect;

  public TableColumnState() {
    super();
  }

  protected TableColumnState(TableColumnState state) {
    this.m_className = state.m_className;
    this.m_width = state.m_width;
    this.m_sortOrder = state.m_sortOrder;
    this.m_sortAscending = state.m_sortAscending;
    this.m_displayable = state.m_displayable;
    this.m_visible = state.m_visible;
    this.m_columnFilterData = state.m_columnFilterData;
    this.m_groupingActive = state.m_groupingActive;
    this.m_aggregationFunction = state.m_aggregationFunction;
    this.m_backgroundEffect = state.m_backgroundEffect;
  }

  public String getClassName() {
    return m_className;
  }

  public void setColumnClassName(String name) {
    m_className = name;
  }

  public int getWidth() {
    return m_width;
  }

  public void setWidth(int i) {
    m_width = i;
  }

  public int getSortOrder() {
    return m_sortOrder;
  }

  public void setSortOrder(int i) {
    m_sortOrder = i;
  }

  public boolean isSortAscending() {
    return m_sortAscending;
  }

  public void setSortAscending(boolean b) {
    m_sortAscending = b;
  }

  public Boolean getDisplayable() {
    return m_displayable;
  }

  public void setDisplayable(Boolean displayable) {
    m_displayable = displayable;
  }

  public Boolean getVisible() {
    return m_visible;
  }

  public void setVisible(Boolean visible) {
    m_visible = visible;
  }

  @Override
  public Object clone() {
    return new TableColumnState(this);
  }

  public byte[] getColumnFilterData() {
    return m_columnFilterData;
  }

  public void setColumnFilterData(byte[] columnFilterData) {
    m_columnFilterData = columnFilterData;
  }

  public boolean isGroupingActive() {
    return m_groupingActive;
  }

  public void setGroupingActive(boolean b) {
    m_groupingActive = b;
  }

  public String getAggregationFunction() {
    return m_aggregationFunction;
  }

  public void setAggregationFunction(String aggregationFunction) {
    m_aggregationFunction = aggregationFunction;
  }

  public String getBackgroundEffect() {
    return m_backgroundEffect;
  }

  public void setBackgroundEffect(String backgroundEffect) {
    m_backgroundEffect = backgroundEffect;
  }

}
