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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.holders.ITableBeanRowHolder;
import org.eclipse.scout.rt.shared.extension.AbstractContributionComposite;

/**
 * Bean that stores the contents of a scout table row. This class is intended to be extended for every table type and
 * the column values are expected to be added as Java bean properties.
 *
 * @since 3.8.2
 */
public abstract class AbstractTableRowData extends AbstractContributionComposite implements ITableBeanRowHolder, Serializable {
  private static final long serialVersionUID = 1L;

  private int m_rowState;
  private Map<String, Object> m_customColumnValues;

  /**
   * @return Returns this row's state.
   * @see #STATUS_NON_CHANGED
   * @see #STATUS_INSERTED
   * @see #STATUS_UPDATED
   * @see #STATUS_DELETED
   */
  @Override
  public int getRowState() {
    return m_rowState;
  }

  /**
   * Sets this row's state
   *
   * @param rowState
   * @see #STATUS_NON_CHANGED
   * @see #STATUS_INSERTED
   * @see #STATUS_UPDATED
   * @see #STATUS_DELETED
   */
  public void setRowState(int rowState) {
    m_rowState = rowState;
  }

  /**
   * @return Returns a map with custom column values or <code>null</code>, if none have been set.
   */
  public Map<String, Object> getCustomColumnValues() {
    return m_customColumnValues;
  }

  /**
   * Sets a map with custom column values.
   *
   * @param customColumnValues
   */
  public void setCustomColumnValues(Map<String, Object> customColumnValues) {
    m_customColumnValues = customColumnValues;
  }

  /**
   * Returns the custom column value with the given <code>columnId</code> or <code>null</code> if it does not exist.
   *
   * @param columnId
   * @return
   */
  public Object getCustomColumnValue(String columnId) {
    if (m_customColumnValues == null) {
      return null;
    }
    return m_customColumnValues.get(columnId);
  }

  /**
   * Sets a custom column value for the given <code>columnId</code>. If <code>value</code> is <code>null</code>, the
   * custom column entry is removed by {@link #removeCustomColumnValue(String)}.
   *
   * @param columnId
   * @param value
   */
  public void setCustomColumnValue(String columnId, Object value) {
    if (value == null) {
      removeCustomColumnValue(columnId);
      return;
    }

    if (m_customColumnValues == null) {
      m_customColumnValues = new HashMap<String, Object>();
    }
    m_customColumnValues.put(columnId, value);
  }

  /**
   * Removes the custom column value from the map.
   * <p>
   * Returns the custom column value to which the map previously associated the <code>columnId</code>, or
   * <code>null</code> if the map contained no mapping for the <code>columnId</code>.
   * </p>
   *
   * @param columnId
   *          columnId whose mapping is to be removed
   * @return the previous custom column value associated with <code>columnId</code>, or <code>null</code> if there was
   *         no mapping for <code>columnId</code>.
   */
  public Object removeCustomColumnValue(String columnId) {
    if (m_customColumnValues == null) {
      return null;
    }
    Object value = m_customColumnValues.remove(columnId);
    if (m_customColumnValues.isEmpty()) {
      m_customColumnValues = null;
    }
    return value;
  }
}
