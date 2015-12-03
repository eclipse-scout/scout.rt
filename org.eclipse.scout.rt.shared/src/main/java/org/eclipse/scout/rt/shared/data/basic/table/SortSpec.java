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
package org.eclipse.scout.rt.shared.data.basic.table;

import java.io.Serializable;

public class SortSpec implements Serializable {
  private static final long serialVersionUID = 0L;

  private int[] m_columns;
  private boolean[] m_ascending;

  public SortSpec(int columnIndex, boolean ascending) {
    m_columns = new int[]{columnIndex};
    m_ascending = new boolean[]{ascending};
  }

  public SortSpec(int columnIndex1, boolean ascending1, int columnIndex2, boolean ascending2) {
    m_columns = new int[]{columnIndex1, columnIndex2};
    m_ascending = new boolean[]{ascending1, ascending2};
  }

  public SortSpec(int[] columnIndexes, boolean[] ascending) {
    m_columns = columnIndexes;
    m_ascending = ascending;
  }

  /**
   * @return sort column count
   */
  public int size() {
    return m_columns.length;
  }

  /**
   * @return the index of the i-th sort column
   */
  public int getColumnIndex(int i) {
    return m_columns[i];
  }

  /**
   * @return the ascending flag of the i-th sort column
   */
  public boolean isColumnAscending(int i) {
    return m_ascending[i];
  }

  /**
   * @return true if the column with index columnIndex is a sort column
   */
  public boolean isSortColumn(int columnIndex) {
    for (int i : m_columns) {
      if (i == columnIndex) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the order of the sort column in the list of sort columns (starting with 0)
   */
  public int getSortColumnOrder(int columnIndex) {
    for (int i = 0; i < m_columns.length; i++) {
      if (m_columns[i] == columnIndex) {
        return i;
      }
    }
    return -1;
  }

}
