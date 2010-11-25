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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public class TableRow implements ITableRow {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableRow.class);

  private final ColumnSet m_columnSet;
  private int m_status = STATUS_NON_CHANGED;
  private boolean m_enabled;
  private boolean m_checked;
  private final ArrayList<Cell> m_cells;
  private boolean m_rowPropertiesChanged;
  private int m_rowChanging = 0;

  public TableRow(ColumnSet columnSet) {
    m_columnSet = columnSet;
    m_enabled = true;
    m_status = STATUS_NON_CHANGED;
    m_cells = new ArrayList<Cell>();
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTableRow(this);
    }
  }

  public TableRow(ColumnSet columnSet, Object[] values) throws ProcessingException {
    this(columnSet);
    if (values == null) values = new Object[0];
    for (int i = 0; i < values.length; i++) {
      Cell cell = getCellForUpdate(i);
      cell.setValue(values[i]);
    }
  }

  public int getRowIndex() {
    return -1;
  }

  public int getStatus() {
    return m_status;
  }

  public void setStatus(int status) {
    m_status = status;
  }

  public boolean isStatusInserted() {
    return m_status == STATUS_INSERTED;
  }

  public void setStatusInserted() {
    setStatus(STATUS_INSERTED);
  }

  public boolean isStatusUpdated() {
    return m_status == STATUS_UPDATED;
  }

  public void setStatusUpdated() {
    setStatus(STATUS_UPDATED);
  }

  public boolean isStatusDeleted() {
    return m_status == STATUS_DELETED;
  }

  public void setStatusDeleted() {
    setStatus(STATUS_DELETED);
  }

  public boolean isStatusNonchanged() {
    return m_status == STATUS_NON_CHANGED;
  }

  public void setStatusNonchanged() {
    setStatus(STATUS_NON_CHANGED);
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean b) {
    m_enabled = b;
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setEnabled(b);
    }
  }

  public boolean isSelected() {
    return false;
  }

  public boolean isChecked() {
    return m_checked;
  }

  public void setChecked(boolean b) {
    m_checked = b;
  }

  public boolean isFilterAccepted() {
    return true;
  }

  public int getCellCount() {
    return m_cells.size();
  }

  public ICell getCell(IColumn column) {
    return getCell(column.getColumnIndex());
  }

  public ICell getCell(int columnIndex) {
    if (columnIndex < m_cells.size()) {
      return m_cells.get(columnIndex);
    }
    else {
      return null;
    }
  }

  public void setCell(IColumn column, ICell cell) throws ProcessingException {
    setCell(column.getColumnIndex(), cell);
  }

  public void setCell(int columnIndex, ICell cell) throws ProcessingException {
    if (cell != null) {
      getCellForUpdate(columnIndex);
      m_cells.set(columnIndex, new Cell(null, cell));
    }
  }

  public Cell getCellForUpdate(IColumn column) {
    return getCellForUpdate(column.getColumnIndex());
  }

  public Cell getCellForUpdate(int columnIndex) {
    while (columnIndex >= m_cells.size()) {
      m_cells.add(new Cell());
    }
    return m_cells.get(columnIndex);
  }

  public Object getCellValue(int columnIndex) {
    ICell cell = getCell(columnIndex);
    if (cell != null) {
      return cell.getValue();
    }
    else {
      return null;
    }
  }

  public Object[] getKeyValues() {
    if (m_columnSet == null) throw new UnsupportedOperationException("can only be called when TableRow was constructed with a non-null columnSet");
    int[] keyColumns = m_columnSet.getKeyColumnIndexes();
    if (keyColumns.length == 0) {
      keyColumns = m_columnSet.getAllColumnIndexes();
    }
    Object[] pk = new Object[keyColumns.length];
    for (int i = 0; i < keyColumns.length; i++) {
      pk[i] = getCellValue(keyColumns[i]);
    }
    return pk;
  }

  public boolean isRowChanging() {
    return false;
  }

  public void setRowChanging(boolean b) {
    if (b) {
      m_rowChanging++;
    }
    else {
      m_rowChanging--;
    }
  }

  public boolean/* changed */setCellValue(int columnIndex, Object value) throws ProcessingException {
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

  public boolean setCellValues(Object[] values) throws ProcessingException {
    try {
      setRowChanging(true);
      //
      boolean changed = false;
      for (int i = 0; i < values.length; i++) {
        boolean b = setCellValue(i, values[i]);
        changed = changed || b;
      }
      return changed;
    }
    finally {
      setRowChanging(false);
    }
  }

  public ITable getTable() {
    return null;
  }

  public void touch() throws ProcessingException {
  }

  public void delete() throws ProcessingException {
  }

  public void setBackgroundColor(String c) {
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setBackgroundColor(c);
    }
  }

  public void setForegroundColor(String c) {
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setForegroundColor(c);
    }
  }

  public void setFont(FontSpec f) {
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setFont(f);
    }
  }

  public void setTooltipText(String s) {
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setTooltipText(s);
    }
  }

  public void setIconId(String id) {
    if (m_columnSet == null) throw new UnsupportedOperationException("can only be called when TableRow was constructed with a non-null columnSet");
    IColumn col = m_columnSet.getFirstVisibleColumn();
    if (col != null) {
      m_cells.get(col.getColumnIndex()).setIconId(id);
    }
  }

  public String getIconId() {
    if (m_columnSet == null) throw new UnsupportedOperationException("can only be called when TableRow was constructed with a non-null columnSet");
    IColumn col = m_columnSet.getFirstVisibleColumn();
    if (col != null) {
      return m_cells.get(col.getColumnIndex()).getIconId();
    }
    else {
      return null;
    }
  }

  public void moveDown() {
    // no effect
  }

  public void moveToBottom() {
    // no effect
  }

  public void moveToTop() {
    // no effect
  }

  public void moveUp() {
    // no effect
  }

  public boolean isRowPropertiesChanged() {
    return m_rowPropertiesChanged;
  }

  public void setRowPropertiesChanged(boolean b) {
    m_rowPropertiesChanged = b;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + m_cells;
  }

}
