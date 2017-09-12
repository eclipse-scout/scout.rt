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
public abstract class AbstractTableRowData extends AbstractContributionComposite implements ITableBeanRowHolder {
  private static final long serialVersionUID = 1L;

  private int m_rowState;
  private Map<String, Object> m_customValues;

  public static final String CUSTOM_VALUES_ID_GEO_LOCATION = "geoLocationCustomValuesId";

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
  public Map<String, Object> getCustomValues() {
    return m_customValues;
  }

  /**
   * Sets a map with custom values.
   *
   * @param customValues
   */
  public void setCustomValues(Map<String, Object> customValues) {
    m_customValues = customValues;
  }

  /**
   * Returns the custom value with the given <code>id</code> or <code>null</code> if it does not exist.
   *
   * @param id
   * @return
   */
  public Object getCustomValue(String id) {
    if (m_customValues == null) {
      return null;
    }
    return m_customValues.get(id);
  }

  /**
   * Sets a custom value for the given <code>id</code>. If <code>value</code> is <code>null</code>, the custom column
   * entry is removed by {@link #removeCustomValue(String)}.
   *
   * @param id
   * @param value
   */
  public void setCustomValue(String id, Object value) {
    if (value == null) {
      removeCustomValue(id);
      return;
    }

    if (m_customValues == null) {
      m_customValues = new HashMap<>();
    }
    m_customValues.put(id, value);
  }

  /**
   * Removes the custom value from the map.
   * <p>
   * Returns the custom value to which the map previously associated the <code>id</code>, or <code>null</code> if the
   * map contained no mapping for the <code>id</code>.
   * </p>
   *
   * @param id
   *          id whose mapping is to be removed
   * @return the previous custom column value associated with <code>id</code>, or <code>null</code> if there was no
   *         mapping for <code>id</code>.
   */
  public Object removeCustomValue(String id) {
    if (m_customValues == null) {
      return null;
    }
    Object value = m_customValues.remove(id);
    if (m_customValues.isEmpty()) {
      m_customValues = null;
    }
    return value;
  }
}
