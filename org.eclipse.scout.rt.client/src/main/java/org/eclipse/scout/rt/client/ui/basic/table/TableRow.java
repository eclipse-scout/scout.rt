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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.dimension.IDimensions;

public class TableRow implements ITableRow {

  private static final String CHECKED = "CHECKED";
  private static final String ROW_PROPERTIES_CHANGED = "ROW_PROPERTIES_CHANGED";
  protected static final String REJECTED_BY_USER = "REJECTED_BY_USER";
  protected static final String FILTER_ACCEPTED = "FILTER_ACCEPTED";

  protected static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(CHECKED, IDimensions.ENABLED, ROW_PROPERTIES_CHANGED, REJECTED_BY_USER, FILTER_ACCEPTED);

  private final ColumnSet m_columnSet;
  private final Map<String, Object> m_customValues;
  protected final List<Cell> m_cells;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link IDimensions#ENABLED}, {@link #CHECKED}, {@link #ROW_PROPERTIES_CHANGED},
   * {@link #FILTER_ACCEPTED} (in subclass), {@link #REJECTED_BY_USER} (in subclass).
   */
  protected byte m_flags;

  private int m_status = STATUS_NON_CHANGED;
  private String m_iconId;
  private String m_cssClass;
  private Set<Integer> m_updatedColumnIndexes;

  /**
   * @param columnSet
   *          may be null
   */
  public TableRow(ColumnSet columnSet) {
    m_columnSet = columnSet;
    m_customValues = new HashMap<>(0);

    int colCount = columnSet != null ? columnSet.getColumnCount() : 0;
    m_cells = new ArrayList<Cell>(colCount);
    setEnabled(true);
    addCells(colCount);
  }

  public TableRow(ColumnSet columnSet, ITableRow row) {
    m_columnSet = columnSet;
    m_customValues = new HashMap<>(row.getCustomValues());

    int colCount = columnSet != null ? columnSet.getColumnCount() : 0;
    m_cells = new ArrayList<Cell>(colCount);
    setEnabled(true);
    copyCells(row);
  }

  public TableRow(ColumnSet columnSet, List<? extends Object> values) {
    this(columnSet);
    if (CollectionUtility.hasElements(values)) {
      for (int i = 0; i < values.size(); i++) {
        Cell cell = getCellForUpdate(i);
        cell.setValue(values.get(i));
      }
    }
  }

  private void addCells(int colCount) {
    for (int i = 0; i < colCount; i++) {
      m_cells.add(new Cell());
    }
  }

  private void copyCells(ITableRow row) {
    for (int i = 0; i < row.getCellCount(); i++) {
      m_cells.add(new Cell(row.getCell(i)));
    }
  }

  @Override
  public int getRowIndex() {
    return -1;
  }

  @Override
  public int getStatus() {
    return m_status;
  }

  @Override
  public void setStatus(int status) {
    m_status = status;
  }

  @Override
  public boolean isStatusInserted() {
    return m_status == STATUS_INSERTED;
  }

  @Override
  public void setStatusInserted() {
    setStatus(STATUS_INSERTED);
  }

  @Override
  public boolean isStatusUpdated() {
    return m_status == STATUS_UPDATED;
  }

  @Override
  public void setStatusUpdated() {
    setStatus(STATUS_UPDATED);
  }

  @Override
  public boolean isStatusDeleted() {
    return m_status == STATUS_DELETED;
  }

  @Override
  public void setStatusDeleted() {
    setStatus(STATUS_DELETED);
  }

  @Override
  public boolean isStatusNonchanged() {
    return m_status == STATUS_NON_CHANGED;
  }

  @Override
  public void setStatusNonchanged() {
    setStatus(STATUS_NON_CHANGED);
  }

  @Override
  public boolean isEnabled() {
    return FLAGS_BIT_HELPER.isBitSet(IDimensions.ENABLED, m_flags);
  }

  @Override
  public void setEnabled(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(IDimensions.ENABLED, b, m_flags);
  }

  @Override
  public boolean isSelected() {
    return false;
  }

  @Override
  public boolean isChecked() {
    return FLAGS_BIT_HELPER.isBitSet(CHECKED, m_flags);
  }

  @Override
  public void setChecked(boolean checked) {
    m_flags = FLAGS_BIT_HELPER.changeBit(CHECKED, checked, m_flags);
  }

  @Override
  public boolean isFilterAccepted() {
    return true;
  }

  @Override
  public boolean isRejectedByUser() {
    return false;
  }

  @Override
  public int getCellCount() {
    return m_cells.size();
  }

  @Override
  public ICell getCell(IColumn column) {
    return getCell(column.getColumnIndex());
  }

  @Override
  public ICell getCell(int columnIndex) {
    if (columnIndex < m_cells.size()) {
      return m_cells.get(columnIndex);
    }
    else {
      return null;
    }
  }

  @Override
  public void setCell(IColumn column, ICell cell) {
    setCell(column.getColumnIndex(), cell);
  }

  @Override
  public void setCell(int columnIndex, ICell cell) {
    if (cell != null) {
      getCellForUpdate(columnIndex);
      m_cells.set(columnIndex, new Cell(null, cell));
    }
  }

  @Override
  public Cell getCellForUpdate(IColumn column) {
    return getCellForUpdate(column.getColumnIndex());
  }

  @Override
  public Cell getCellForUpdate(int columnIndex) {
    while (columnIndex >= m_cells.size()) {
      m_cells.add(new Cell());
    }
    return m_cells.get(columnIndex);
  }

  @Override
  public Object getCellValue(int columnIndex) {
    ICell cell = getCell(columnIndex);
    if (cell != null) {
      return cell.getValue();
    }
    else {
      return null;
    }
  }

  @Override
  public Object getCustomValue(String id) {
    return m_customValues.get(id);
  }

  @Override
  public Map<String, Object> getCustomValues() {
    return m_customValues;
  }

  @Override
  public void setCustomValue(String id, Object value) {
    m_customValues.put(id, value);
  }

  @Override
  public List<Object> getKeyValues() {
    if (m_columnSet == null) {
      throw new UnsupportedOperationException("can only be called when TableRow was constructed with a non-null columnSet");
    }
    int[] keyColumns = m_columnSet.getKeyColumnIndexes();
    if (keyColumns.length == 0) {
      keyColumns = m_columnSet.getAllColumnIndexes();
    }
    List<Object> pk = new ArrayList<Object>(keyColumns.length);
    for (int keyIndex : keyColumns) {
      pk.add(getCellValue(keyIndex));
    }
    return pk;
  }

  @Override
  public boolean isRowChanging() {
    return false;
  }

  @Override
  public void setRowChanging(boolean b) {
    //nop
  }

  @Override
  public boolean/* changed */ setCellValue(int columnIndex, Object value) {
    try {
      setRowChanging(true);
      //
      Object oldValue = getCellValue(columnIndex);
      if (oldValue == value || (oldValue != null && oldValue.equals(value))) {
        // no change in value
        return false;
      }
      else {
        getCellForUpdate(columnIndex);
        m_cells.get(columnIndex).setValue(value);
        if (isStatusNonchanged()) {
          setStatusUpdated();
        }
        else {
          // keep inserted, deleted
        }
        return true;
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public boolean setCellValues(List<? extends Object> values) {
    try {
      setRowChanging(true);
      //
      boolean changed = false;
      for (int i = 0; i < values.size(); i++) {
        boolean b = setCellValue(i, values.get(i));
        changed = changed || b;
      }
      return changed;
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public ITable getTable() {
    return null;
  }

  @Override
  public void touch() {
    // TableRow is not managed by a table
  }

  @Override
  public void delete() {
    // TableRow is not managed by a table
  }

  @Override
  public void setBackgroundColor(String c) {
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setBackgroundColor(c);
    }
  }

  @Override
  public void setForegroundColor(String c) {
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setForegroundColor(c);
    }
  }

  @Override
  public void setCssClass(String cssClass) {
    m_cssClass = cssClass;
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setCssClass(cssClass);
    }
  }

  @Override
  public String getCssClass() {
    return m_cssClass;
  }

  @Override
  public void setFont(FontSpec f) {
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setFont(f);
    }
  }

  @Override
  public void setTooltipText(String s) {
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setTooltipText(s);
    }
  }

  @Override
  public void setIconId(String iconId) {
    m_iconId = iconId;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public void moveDown() {
    // no effect
  }

  @Override
  public void moveToBottom() {
    // no effect
  }

  @Override
  public void moveToTop() {
    // no effect
  }

  @Override
  public void moveUp() {
    // no effect
  }

  @Override
  public boolean isRowPropertiesChanged() {
    return FLAGS_BIT_HELPER.isBitSet(ROW_PROPERTIES_CHANGED, m_flags);
  }

  @Override
  public void setRowPropertiesChanged(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ROW_PROPERTIES_CHANGED, b, m_flags);
  }

  @Override
  public Set<Integer> getUpdatedColumnIndexes() {
    return CollectionUtility.hashSet(m_updatedColumnIndexes);
  }

  @Override
  public void setUpdatedColumnIndexes(Set<Integer> updatedColumnIndexes) {
    m_updatedColumnIndexes = updatedColumnIndexes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + m_cells;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<Integer> getUpdatedColumnIndexes(int changedBit) {
    return Collections.EMPTY_SET;
  }
}
