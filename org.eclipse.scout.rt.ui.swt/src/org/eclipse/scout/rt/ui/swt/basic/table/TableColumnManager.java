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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class TableColumnManager {

  private List<IColumn<?>> m_initialColumns;
  private List<IColumn<?>> m_currentOrder;

  public void initialize(List<IColumn<?>> initialColumns) {
    m_initialColumns = initialColumns;
    m_currentOrder = m_initialColumns;
  }

  public List<IColumn<?>> getOrderedColumns(int[] columnOrder) {
    List<IColumn<?>> result = new ArrayList<IColumn<?>>();
    for (int index : columnOrder) {
      result.add(m_initialColumns.get(index));
    }
    return Collections.unmodifiableList(result);
  }

  public boolean applyNewOrder(List<IColumn<?>> newOrder) {
    if (CollectionUtility.equalsCollection(newOrder, m_currentOrder)) {
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
    return CollectionUtility.getElement(m_initialColumns, modelIndex);
  }

  /**
   * @param visualIndex
   *          beginning with 1
   */
  public IColumn<?> getColumnByVisualIndex(int visualIndex) {
    return CollectionUtility.getElement(m_currentOrder, visualIndex - 1);
  }

  public boolean isIconColumn(int columnIndex) {
    return false;
  }

}
