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
package org.eclipse.scout.rt.platform.holders;

import java.util.HashSet;

public class TableHolderFilter {
  private ITableHolder m_table;
  private HashSet<Integer> m_rowStates;

  public TableHolderFilter(ITableHolder table, int... rowStates) {
    m_table = table;
    m_rowStates = new HashSet<Integer>();
    if (rowStates != null) {
      for (int i : rowStates) {
        m_rowStates.add(i);
      }
    }
  }

  public ITableHolder getTableHolder() {
    return m_table;
  }

  public int[] getFilteredRows() {
    int[] a = new int[m_table.getRowCount()];
    int filterIndex = 0;
    for (int r = 0, nr = m_table.getRowCount(); r < nr; r++) {
      int state = m_table.getRowState(r);
      if (m_rowStates.contains(state)) {
        a[filterIndex] = r;
        filterIndex++;
      }
    }
    int[] b = new int[filterIndex];
    System.arraycopy(a, 0, b, 0, b.length);
    return b;
  }
}
