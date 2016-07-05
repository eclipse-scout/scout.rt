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
package org.eclipse.scout.rt.shared.data.form.fields.tablefield;

import java.util.ArrayList;

import org.eclipse.scout.rt.platform.holders.ITableHolder;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

/**
 * @Deprecated: 'Array based TableData' are not supported by the Scout SDK in Neon. Use
 *              {@link AbstractTableFieldBeanData} instead. This class will be removed with Oxygen. See Bug 496292.
 */
@Deprecated
public abstract class AbstractTableFieldData extends AbstractFormFieldData implements ITableHolder {
  private static final long serialVersionUID = 1L;

  private ArrayList<Object[]> m_rowList = new ArrayList<Object[]>();

  @Override
  public Class<?> getFieldStopClass() {
    return AbstractTableFieldData.class;
  }

  @Override
  public int getRowCount() {
    return m_rowList.size();
  }

  @Override
  public int getColumnCount() {
    return 0;
  }

  /**
   * Configurator will add column-named getters and setters for value access<br>
   * for example getCity(), getZip(), ...
   */

  /**
   * The configurator will delegate this getter to one of the above getters
   */
  @Override
  public Object getValueAt(int row, int column) {
    return null;
  }

  protected Object getValueInternal(int row, int column) {
    Object[] a = m_rowList.get(row);
    return a[column];
  }

  /**
   * The configurator will delegate this setter to one of the above setters
   */
  @Override
  public void setValueAt(int row, int column, Object value) {
  }

  protected void setValueInternal(int row, int column, Object value) {
    Object[] a = m_rowList.get(row);
    a[column] = value;
    setValueSet(true);
  }

  @Override
  public int getRowState(int row) {
    Number n = (Number) getValueInternal(row, getColumnCount());
    if (n != null) {
      return n.intValue();
    }
    else {
      return STATUS_NON_CHANGED;
    }
  }

  @Override
  public void setRowState(int row, int state) {
    setValueInternal(row, getColumnCount(), Integer.valueOf(state));
  }

  /**
   * sets all rows in the table data to a new state
   */
  public void setRowStates(int state) {
    for (int r = 0, n = getRowCount(); r < n; r++) {
      setRowState(r, state);
    }
  }

  @Override
  public int/* newIndex */ addRow() {
    return addRow(STATUS_NON_CHANGED);
  }

  public int/* newIndex */ addRow(int rowState) {
    return addRow(null, rowState);
  }

  public int/* newIndex */ addRow(Object[] values) {
    return addRow(values, STATUS_NON_CHANGED);
  }

  public int/* newIndex */ addRow(Object[] values, int rowState) {
    m_rowList.add(new Object[getColumnCount() + 1]);// +1 for status
    int newRowIndex = m_rowList.size() - 1;
    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        setValueAt(newRowIndex, i, values[i]);
      }
    }
    setRowState(newRowIndex, rowState);
    setValueSet(true);
    return newRowIndex;
  }

  @Override
  public void ensureSize(int size) {
    while (m_rowList.size() < size) {
      addRow();
    }
    while (m_rowList.size() > size) {
      removeRow(m_rowList.size() - 1);
    }
    setValueSet(true);
  }

  public void removeRow(int index) {
    m_rowList.remove(index);
    setValueSet(true);
  }

  public void clearRows() {
    m_rowList.clear();
    setValueSet(true);
  }

}
