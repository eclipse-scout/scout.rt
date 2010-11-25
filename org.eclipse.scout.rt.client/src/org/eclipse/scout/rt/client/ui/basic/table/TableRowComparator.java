/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.Comparator;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * compares two TableRow objects based on 1 ore more columns
 */
public class TableRowComparator implements Comparator<ITableRow> {
  private final IColumn[] m_columns;

  public TableRowComparator(IColumn[] columns) {
    m_columns = columns;
  }

  public int compare(ITableRow row1, ITableRow row2) {
    for (int i = 0; i < m_columns.length; i++) {
      int c = m_columns[i].compareTableRows(row1, row2);
      if (!m_columns[i].getHeaderCell().isSortAscending()) {
        c = -c;
      }
      if (c != 0) return c;
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj.getClass() == this.getClass() && ((TableRowComparator) obj).m_columns == this.m_columns;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
