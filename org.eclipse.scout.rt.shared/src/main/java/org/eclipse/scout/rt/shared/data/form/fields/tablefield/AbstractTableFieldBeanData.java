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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.holders.ITableBeanHolder;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

/**
 * Form field data for table fields. This class uses {@link AbstractTableRowData} beans for storing the table's
 * contents.
 * <p>
 * {@link #isValueSet()} does not behave the same as on {@link AbstractTableFieldData}. It does not track any
 * modifications of table row properties or their row states. The following methods may change the result of
 * {@link #isValueSet()}:
 * </p>
 * <ul>
 * <li>{@link #addRow()}</li>
 * <li>{@link #addRow(int)}</li>
 * <li>{@link #setRows(AbstractTableRowData[])}</li>
 * <li>{@link #removeRow(AbstractTableRowData)}</li>
 * <li>{@link #removeRow(int)}</li>
 * <li>{@link #clearRows()}</li>
 * </ul>
 *
 * @since 3.8.2
 */
public abstract class AbstractTableFieldBeanData extends AbstractFormFieldData implements ITableBeanHolder {
  private static final long serialVersionUID = 1L;

  private List<AbstractTableRowData> m_rowList = new ArrayList<AbstractTableRowData>();

  public AbstractTableFieldBeanData() {
  }

  @Override
  protected void initConfig() {
    super.initConfig();
  }

  /**
   * @return Returns the number of available table row data.
   */
  @Override
  public int getRowCount() {
    return m_rowList.size();
  }

  /**
   * @return Returns all rows.
   */
  @Override
  public AbstractTableRowData[] getRows() {
    return m_rowList.toArray((AbstractTableRowData[]) Array.newInstance(getRowType(), m_rowList.size()));
  }

  /**
   * Replaces the rows with the given array.
   *
   * @param rows
   */
  public void setRows(AbstractTableRowData[] rows) {
    m_rowList.clear();
    Class<? extends AbstractTableRowData> rowType = getRowType();
    if (rowType == null) {
      throw new IllegalStateException("row type is not initialized");
    }
    for (AbstractTableRowData row : rows) {
      if (row == null) {
        continue;
      }
      if (!rowType.isInstance(row)) {
        throw new IllegalArgumentException("wrong row type. Expected [" + rowType.getName() + "], actual: [" + row.getClass().getName() + "]");
      }
      m_rowList.add(row);
    }
    setValueSet(true);
  }

  /**
   * Returns the row at the given index.
   *
   * @param index
   * @return
   */
  public AbstractTableRowData rowAt(int index) {
    return m_rowList.get(index);
  }

  /**
   * @return Creates, adds and returns a new {@link AbstractTableRowData}. Its row state is initialized with
   *         {@link AbstractTableRowData#STATUS_NON_CHANGED} and its type is the one returned by {@link #getRowType()}.
   */
  @Override
  public AbstractTableRowData addRow() {
    return addRow(AbstractTableRowData.STATUS_NON_CHANGED);
  }

  /**
   * Create, adds and returns a new {@link AbstractTableRowData} which row state is initialized with the given value.
   *
   * @param rowState
   * @return
   */
  public AbstractTableRowData addRow(int rowState) {
    AbstractTableRowData row = createRow();
    row.setRowState(rowState);
    m_rowList.add(row);
    setValueSet(true);
    return row;
  }

  /**
   * @return Creates a new {@link AbstractTableRowData} without adding it to this {@link AbstractTableFieldBeanData}.
   *         Its actual type is the one returned by {@link #getRowType()}.
   */
  public abstract AbstractTableRowData createRow();

  /**
   * @return Returns the type of the rows managed by this {@link AbstractTableFieldBeanData}.
   */
  @Override
  public abstract Class<? extends AbstractTableRowData> getRowType();

  /**
   * Removes the row at the given index.
   *
   * @param index
   */
  @Override
  public void removeRow(int index) {
    m_rowList.remove(index);
    setValueSet(true);
  }

  /**
   * Removes the given row.
   *
   * @param row
   * @return <code>true</code>, if the row was removed. Otherwise <code>false</code>.
   */
  public boolean removeRow(AbstractTableRowData row) {
    if (m_rowList.remove(row)) {
      setValueSet(true);
      return true;
    }
    return false;
  }

  /**
   * Removes all rows.
   */
  public void clearRows() {
    m_rowList.clear();
    setValueSet(true);
  }
}
