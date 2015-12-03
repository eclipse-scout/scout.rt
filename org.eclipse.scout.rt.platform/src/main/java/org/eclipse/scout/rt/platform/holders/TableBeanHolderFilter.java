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
import java.util.Set;

/**
 * Filter for {@link ITableBeanHolder}: provide an array of {@link ITableBeanRowHolder} depending on their row state.
 */
public class TableBeanHolderFilter {
  private final ITableBeanHolder m_table;
  private final Set<Integer> m_rowStates;

  public TableBeanHolderFilter(ITableBeanHolder table, int... rowStates) {
    m_table = table;
    m_rowStates = new HashSet<Integer>();
    if (rowStates != null) {
      for (int i : rowStates) {
        m_rowStates.add(i);
      }
    }
  }

  public ITableBeanHolder getTableBeanHolder() {
    return m_table;
  }

  public ITableBeanRowHolder[] getFilteredRows() {
    ITableBeanRowHolder[] a = new ITableBeanRowHolder[m_table.getRowCount()];
    int filterIndex = 0;
    for (ITableBeanRowHolder row : m_table.getRows()) {
      int state = row.getRowState();
      if (m_rowStates.contains(state)) {
        a[filterIndex] = row;
        filterIndex++;
      }
    }
    ITableBeanRowHolder[] b = new ITableBeanRowHolder[filterIndex];
    System.arraycopy(a, 0, b, 0, b.length);
    return b;
  }
}
