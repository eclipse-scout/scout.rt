/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * Specify chart column
 */
public class ChartColumnParam implements IChartColumnParam {
  private final IColumn<?> m_column;
  private final int m_columnModifier;

  public ChartColumnParam(int columnModifier) {
    m_column = null;
    m_columnModifier = columnModifier;
  }

  /**
   * @param column
   *     may be <code>null</code>
   */
  public ChartColumnParam(IColumn<?> column, int columnModifier) {
    m_column = column;
    m_columnModifier = columnModifier;
  }

  @Override
  public int getColumnIndex() {
    if (getColumn() != null) {
      return getColumn().getColumnIndex();
    }
    return -1;
  }

  @Override
  public int getColumnModifier() {
    return m_columnModifier;
  }

  @Override
  public IColumn getColumn() {
    return m_column;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_column == null) ? 0 : m_column.hashCode());
    result = prime * result + m_columnModifier;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ChartColumnParam other = (ChartColumnParam) obj;
    if (m_column == null) {
      if (other.m_column != null) {
        return false;
      }
    }
    else if (!m_column.equals(other.m_column)) {
      return false;
    }
    if (m_columnModifier != other.m_columnModifier) {
      return false;
    }
    return true;
  }
}
