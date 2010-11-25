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
package org.eclipse.scout.rt.ui.swt.basic.table;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class TableColumnManager {

  private IColumn<?>[] m_initialColumns;
  private IColumn<?>[] m_currentOrder;

  public void initialize(IColumn<?>[] initialColumns) {
    m_initialColumns = initialColumns;
    m_currentOrder = m_initialColumns;
  }

  public IColumn<?>[] getOrderedColumns(int[] columnOrder) {
    IColumn<?>[] columns = new IColumn<?>[columnOrder.length];
    for (int i = 0; i < columnOrder.length; i++) {
      columns[i] = m_initialColumns[columnOrder[i]];
    }
    return columns;
  }

  public boolean applyNewOrder(IColumn<?>[] newOrder) {
    if (CompareUtility.equals(newOrder, m_currentOrder)) {
      return false;
    }
    else {
      m_currentOrder = newOrder;
      return true;
    }
  }

  /**
   * @param modelIndex
   *          beginning with 0
   */
  public IColumn<?> getColumnByModelIndex(int modelIndex) {
    if (modelIndex >= 0 && modelIndex < m_initialColumns.length) {
      return m_initialColumns[modelIndex];
    }
    else {
      return null;
    }
  }

  /**
   * @param visualIndex
   *          beginning with 1
   */
  public IColumn<?> getColumnByVisualIndex(int visualIndex) {
    if (visualIndex - 1 >= 0 && visualIndex - 1 < m_currentOrder.length) {
      return m_currentOrder[visualIndex - 1];
    }
    else {
      return null;
    }
  }

  public boolean isIconColumn(int columnIndex) {
    return false;
  }

}
