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
package org.eclipse.scout.rt.client.ui.basic.table.internal;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public class InternalTableRow implements ITableRow, ICellObserver {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(InternalTableRow.class);

  private ITable m_table;
  private int m_rowIndex;
  private boolean m_enabled;
  private boolean m_checked;
  private String m_iconId;
  private int m_status = STATUS_NON_CHANGED;
  private Cell[] m_cells;
  private int m_rowChanging = 0;
  private boolean m_rowPropertiesChanged;
  private boolean m_filterAccepted;

  private InternalTableRow() {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTableRow(this);
    }
  }

  public InternalTableRow(ITable table) {
    this();
    m_table = table;
    m_enabled = true;
    m_status = STATUS_NON_CHANGED;
    m_filterAccepted = true;
    m_cells = new Cell[table.getColumnCount()];
    for (int i = 0; i < m_cells.length; i++) {
      m_cells[i] = new Cell(this);
    }
  }

  public InternalTableRow(ITable table, ITableRow row) throws ProcessingException {
    this();
    m_rowIndex = row.getRowIndex();
    m_enabled = row.isEnabled();
    m_checked = row.isChecked();
    m_status = row.getStatus();
    m_cells = new Cell[table.getColumnCount()];
    for (int i = 0; i < m_cells.length; i++) {
      m_cells[i] = new Cell(this, row.getCell(i));
    }
    // validate values
    IColumn[] cols = table.getColumns();
    for (int i = 0; i < m_cells.length && i < cols.length; i++) {
      Object value = m_cells[i].getValue();
      value = cols[i].parseValue(this, value);
      m_cells[i].setValue(value);
    }
    // reset status
    m_status = row.getStatus();
    // set table at end to avoid events before the row is even attached
    m_table = table;
  }

  @Override
  public int getRowIndex() {
    return m_rowIndex;
  }

  public void setRowIndex(int index) {
    m_rowIndex = index;
  }

  @Override
  public int getStatus() {
    return m_status;
  }

  @Override
  public void setStatus(int status) {
    try {
      setRowChanging(true);
      //
      if (m_status != status) {
        m_status = status;
        m_rowPropertiesChanged = true;
      }
    }
    finally {
      setRowChanging(false);
    }
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
    try {
      setRowChanging(true);
      //
      m_enabled = b;
      for (int i = 0; i < m_cells.length; i++) {
        m_cells[i].setEnabled(m_enabled);
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public boolean isSelected() {
    return getTable().isSelectedRow(this);
  }

  @Override
  public boolean isChecked() {
    return m_checked;
  }

  @Override
  public void setChecked(boolean b) {
    if (m_checked != b) {
      try {
        setRowChanging(true);
        //
        m_checked = b;
        m_rowPropertiesChanged = true;
        //
        //uncheck others in single-check mode
        ITable table = getTable();
        if (table != null) {
          if (table.getCheckableColumn() != null) {
            try {
              table.getCheckableColumn().setValue(this, b);
            }
            catch (ProcessingException e) {
              LOG.warn("Value could not be set on CheckableColumn", e);
            }
          }
          if (b && !table.isMultiCheck()) {
            for (ITableRow cr : table.getCheckedRows()) {
              if (cr != this) {
                cr.setChecked(false);
              }
            }
          }
        }
      }
      finally {
        setRowChanging(false);
      }
    }
  }

  @Override
  public boolean isFilterAccepted() {
    return m_filterAccepted;
  }

  /**
   * do not use this internal method
   */
  public void setFilterAcceptedInternal(boolean b) {
    m_filterAccepted = b;
  }

  @Override
  public int getCellCount() {
    return m_cells.length;
  }

  @Override
  public ICell getCell(IColumn column) {
    return getCell(column.getColumnIndex());
  }

  @Override
  public ICell getCell(int columnIndex) {
    if (columnIndex < 0 || columnIndex >= m_cells.length) {
      return new Cell();
    }
    return m_cells[columnIndex];
  }

  @Override
  public void setCell(IColumn column, ICell cell) throws ProcessingException {
    setCell(column.getColumnIndex(), cell);
  }

  @Override
  public void setCell(int columnIndex, ICell cell) throws ProcessingException {
    if (cell != null) {
      try {
        setRowChanging(true);
        //
        // copy all fields
        m_cells[columnIndex] = new Cell(this, cell);
      }
      finally {
        setRowChanging(false);
      }
    }
  }

  @Override
  public Cell getCellForUpdate(IColumn column) {
    return getCellForUpdate(column.getColumnIndex());
  }

  @Override
  public Cell getCellForUpdate(int columnIndex) {
    return m_cells[columnIndex];
  }

  @Override
  public Object getCellValue(int columnIndex) {
    return m_cells[columnIndex].getValue();
  }

  @Override
  public Object[] getKeyValues() {
    int[] keyColumns = getTable().getColumnSet().getKeyColumnIndexes();
    if (keyColumns.length == 0) {
      keyColumns = getTable().getColumnSet().getAllColumnIndexes();
    }
    Object[] pk = new Object[keyColumns.length];
    for (int i = 0; i < keyColumns.length; i++) {
      pk[i] = m_cells[keyColumns[i]].getValue();
    }
    return pk;
  }

  @Override
  public boolean isRowChanging() {
    return m_rowChanging > 0;
  }

  @Override
  public void setRowChanging(boolean b) {
    if (b) {
      m_rowChanging++;
    }
    else {
      m_rowChanging--;
      if (m_rowChanging == 0 && m_rowPropertiesChanged) {
        m_rowPropertiesChanged = false;
        if (getTable() != null) {
          getTable().updateRow(this);
        }
      }
    }
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
  public boolean/* changed */setCellValue(int columnIndex, Object value) throws ProcessingException {
    return m_cells[columnIndex].setValue(value);
  }

  @Override
  public boolean setCellValues(Object[] values) throws ProcessingException {
    boolean changed = false;
    for (int i = 0; i < values.length; i++) {
      boolean b = setCellValue(i, values[i]);
      changed = changed || b;
    }
    return changed;
  }

  @Override
  public ITable getTable() {
    return m_table;
  }

  /**
   * do not use this internal method
   */
  public void setTableInternal(ITable table) {
    m_table = table;
  }

  @Override
  public void touch() throws ProcessingException {
    try {
      setRowChanging(true);
      //
      m_rowPropertiesChanged = true;
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public void delete() throws ProcessingException {
    if (getTable() != null) {
      getTable().deleteRow(this);
    }
  }

  @Override
  public void setBackgroundColor(String c) {
    try {
      setRowChanging(true);
      //
      for (int i = 0; i < m_cells.length; i++) {
        m_cells[i].setBackgroundColor(c);
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public void setForegroundColor(String c) {
    try {
      setRowChanging(true);
      //
      for (int i = 0; i < m_cells.length; i++) {
        m_cells[i].setForegroundColor(c);
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public void setFont(FontSpec f) {
    try {
      setRowChanging(true);
      //
      for (int i = 0; i < m_cells.length; i++) {
        m_cells[i].setFont(f);
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public void setTooltipText(String s) {
    try {
      setRowChanging(true);
      //
      for (int i = 0; i < m_cells.length; i++) {
        m_cells[i].setTooltipText(s);
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public void setIconId(String id) {
    try {
      setRowChanging(true);
      //
      m_iconId = id;
      m_rowPropertiesChanged = true;
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public void moveToTop() {
    if (getTable() != null) {
      getTable().moveRow(getRowIndex(), 0);
    }
  }

  @Override
  public void moveToBottom() {
    if (getTable() != null) {
      getTable().moveRow(getRowIndex(), getTable().getRowCount());
    }
  }

  @Override
  public void moveUp() {
    if (getTable() != null) {
      getTable().moveRow(getRowIndex(), getRowIndex() - 1);
    }
  }

  @Override
  public void moveDown() {
    if (getTable() != null) {
      getTable().moveRow(getRowIndex(), getRowIndex() + 1);
    }
  }

  /*
   * Implementation of ICellObserver
   */
  @Override
  public Object validateValue(ICell cell, Object value) throws ProcessingException {
    Object oldValue = cell.getValue();
    if (CompareUtility.equals(oldValue, value)) {
      // no change in value
      return value;
    }
    else {
      // validate value
      if (getTable() != null) {
        int colIndex = -1;
        for (int i = 0; i < m_cells.length; i++) {
          if (m_cells[i] == cell) {
            colIndex = i;
            break;
          }
        }
        if (colIndex >= 0) {
          IColumn column = getTable().getColumnSet().getColumn(colIndex);
          value = column.parseValue(this, value);
        }
      }
      return value;
    }
  }

  @Override
  public void cellChanged(ICell cell, int changedBit) {
    try {
      setRowChanging(true);
      //
      if (changedBit == ICell.VALUE_BIT) {
        if (isStatusNonchanged()) {
          setStatusUpdated();
        }
        else {
          // keep inserted, deleted
        }
      }
      m_rowPropertiesChanged = true;
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + VerboseUtility.dumpObjects(m_cells) + "]";
  }
}
