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
package org.eclipse.scout.rt.shared.data.form.fields.tablefield;

import java.util.ArrayList;

import org.eclipse.scout.commons.holders.ITableHolder;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

public abstract class AbstractTableFieldData extends AbstractFormFieldData implements ITableHolder {
  private static final long serialVersionUID = 1L;

  private ArrayList<Object[]> m_rowList = new ArrayList<Object[]>();

  public AbstractTableFieldData() {
  }

  @Override
  public Object clone() {
    AbstractTableFieldData copy = (AbstractTableFieldData) super.clone();
    if (this.m_rowList != null) {
      copy.m_rowList = new ArrayList<Object[]>(this.m_rowList.size());
      for (Object[] row : this.m_rowList) {
        Object[] copyRow = null;
        if (row != null) {
          copyRow = new Object[row.length];
          System.arraycopy(row, 0, copyRow, 0, row.length);
        }
        copy.m_rowList.add(copyRow);
      }
    }
    return copy;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
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

  public int getRowState(int row) {
    Number n = (Number) getValueInternal(row, getColumnCount());
    if (n != null) return n.intValue();
    else return STATUS_NON_CHANGED;
  }

  public void setRowState(int row, int state) {
    setValueInternal(row, getColumnCount(), new Integer(state));
  }

  /**
   * sets all rows in the table data to a new state
   */
  public void setRowStates(int state) {
    for (int r = 0, n = getRowCount(); r < n; r++) {
      setRowState(r, state);
    }
  }

  public int/* newIndex */addRow() {
    return addRow(STATUS_NON_CHANGED);
  }

  public int/* newIndex */addRow(int rowState) {
    return addRow(null, rowState);
  }

  public int/* newIndex */addRow(Object[] values) {
    return addRow(values, STATUS_NON_CHANGED);
  }

  public int/* newIndex */addRow(Object[] values, int rowState) {
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
