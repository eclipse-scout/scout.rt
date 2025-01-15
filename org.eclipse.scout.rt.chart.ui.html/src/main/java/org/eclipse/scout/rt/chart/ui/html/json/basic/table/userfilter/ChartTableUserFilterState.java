/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.ui.html.json.basic.table.userfilter;

import java.util.List;

import org.eclipse.scout.rt.client.services.common.bookmark.internal.BookmarkUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.userfilter.AbstractUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IColumnAwareUserFilterState;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

public class ChartTableUserFilterState extends AbstractUserFilterState implements IColumnAwareUserFilterState {
  private static final long serialVersionUID = 1L;

  public static final String TYPE = "CHART";

  private String m_text;
  private List<Object> m_filters;
  private transient IColumn<?> m_columnX;
  private transient IColumn<?> m_columnY;
  private String m_columnIdX;
  private String m_columnIdY;

  public ChartTableUserFilterState() {
    setType(TYPE);
  }

  public String getText() {
    return m_text;
  }

  public void setText(String text) {
    m_text = text;
  }

  public IColumn<?> getColumnX() {
    return m_columnX;
  }

  public void setColumnX(IColumn<?> columnX) {
    m_columnX = columnX;
    m_columnIdX = columnX.getColumnId();
  }

  public IColumn<?> getColumnY() {
    return m_columnY;
  }

  public void setColumnY(IColumn<?> columnY) {
    m_columnY = columnY;
    m_columnIdY = (columnY != null) ? columnY.getColumnId() : null;
  }

  public List<Object> getFilters() {
    return m_filters;
  }

  public void setFilters(List<Object> filters) {
    m_filters = filters;
  }

  @Override
  public String getDisplayText() {
    return m_text;
  }

  @Override
  public boolean notifyDeserialized(Object obj) {
    ITable table = (ITable) obj;
    m_columnX = BookmarkUtility.resolveColumn(table.getColumns(), m_columnIdX);
    if (m_columnIdY == null) {
      return m_columnX != null;
    }
    m_columnY = BookmarkUtility.resolveColumn(table.getColumns(), m_columnIdY);
    return m_columnX != null && m_columnY != null;
  }

  @Override
  public void replaceColumn(IColumn<?> col) {
    if (ObjectUtility.equals(getColumnX().getColumnId(), col.getColumnId())) {
      setColumnX(col);
    }
    if (getColumnY() != null && ObjectUtility.equals(getColumnY().getColumnId(), col.getColumnId())) {
      setColumnY(col);
    }
  }
}
