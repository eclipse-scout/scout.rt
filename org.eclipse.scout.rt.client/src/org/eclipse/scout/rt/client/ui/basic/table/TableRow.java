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
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
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
  private final List<Cell> m_cells;
  private boolean m_rowPropertiesChanged;
  private int m_rowChanging = 0;

  /**
   * @param columnSet
   *          may be null
   */
  public TableRow(ColumnSet columnSet) {
    m_columnSet = columnSet;
    m_enabled = true;
    m_status = STATUS_NON_CHANGED;
    int colCount = columnSet != null ? columnSet.getColumnCount() : 0;
    m_cells = new ArrayList<Cell>(colCount);
    for (int i = 0; i < colCount; i++) {
      m_cells.add(new Cell());
    }
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTableRow(this);
    }
  }

  public TableRow(ColumnSet columnSet, List<? extends Object> values) throws ProcessingException {
    this(columnSet);
    if (CollectionUtility.hasElements(values)) {
      for (int i = 0; i < values.size(); i++) {
        Cell cell = getCellForUpdate(i);
        cell.setValue(values.get(i));
      }
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
    return m_enabled;
  }

  @Override
  public void setEnabled(boolean b) {
    m_enabled = b;
    for (int i = 0; i < m_cells.size(); i++) {
      m_cells.get(i).setEnabled(b);
    }
  }

  @Override
  public boolean isSelected() {
    return false;
  }

  @Override
  public boolean isChecked() {
    return m_checked;
  }

  @Override
  public void setChecked(boolean b) {
    m_checked = b;
  }

  @Override
  public boolean isFilterAccepted() {
    return true;
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
  public void setCell(IColumn column, ICell cell) throws ProcessingException {
    setCell(column.getColumnIndex(), cell);
  }

  @Override
  public void setCell(int columnIndex, ICell cell) throws ProcessingException {
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
  public List<Object> getKeyValues() {
    if (m_columnSet == null) {
      throw new UnsupportedOperationException("can only be called when TableRow was constructed with a non-null columnSet");
    }
    int[] keyColumns = m_columnSet.getKeyColumnIndexes();
    if (keyColumns.length == 0) {
      keyColumns = m_columnSet.getAllColumnIndexes();
    }
    List<Object> pk = new ArrayList<Object>();
    for (int keyIndex : keyColumns) {
      pk.add(getCellValue(keyIndex));
    }
    return Collections.unmodifiableList(pk);
  }

  @Override
  public boolean isRowChanging() {
    return false;
  }

  @Override
  public void setRowChanging(boolean b) {
    if (b) {
      m_rowChanging++;
    }
    else {
      m_rowChanging--;
    }
  }

  @Override
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

  @Override
  public boolean setCellValues(List<? extends Object> values) throws ProcessingException {
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
  public void touch() throws ProcessingException {
  }

  @Override
  public void delete() throws ProcessingException {
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
  public void setIconId(String id) {
    if (m_columnSet == null) {
      throw new UnsupportedOperationException("can only be called when TableRow was constructed with a non-null columnSet");
    }
    IColumn col = m_columnSet.getFirstVisibleColumn();
    if (col != null) {
      m_cells.get(col.getColumnIndex()).setIconId(id);
    }
  }

  @Override
  public String getIconId() {
    if (m_columnSet == null) {
      throw new UnsupportedOperationException("can only be called when TableRow was constructed with a non-null columnSet");
    }
    IColumn col = m_columnSet.getFirstVisibleColumn();
    if (col != null) {
      return m_cells.get(col.getColumnIndex()).getIconId();
    }
    else {
      return null;
    }
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
    return m_rowPropertiesChanged;
  }

  @Override
  public void setRowPropertiesChanged(boolean b) {
    m_rowPropertiesChanged = b;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + m_cells;
  }

}
