/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
    m_rowStates = new HashSet<>();
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
