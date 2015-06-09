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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

/**
 * Internal representation of a Table row. Contains some more information than {@link TableRow} like validation.
 */
public class InternalTableRow extends TableRow implements ITableRow, ICellObserver {

  private ITable m_table;
  private int m_rowIndex;
  private int m_rowChanging = 0;
  private boolean m_rowPropertiesChanged;
  private boolean m_filterAccepted = true;

  private InternalTableRow() {
    super(null);
  }

  public InternalTableRow(ITable table) {
    super(table.getColumnSet());
    m_table = table;
  }

  @SuppressWarnings("unchecked")
  public InternalTableRow(ITable table, ITableRow row) throws ProcessingException {
    super(table.getColumnSet(), row);
    setEnabled(row.isEnabled());
    m_rowIndex = row.getRowIndex();

    int columnCount = table.getColumnCount();
    // import and validate cell values
    for (int i = 0; i < table.getColumnCount(); i++) {
      Cell newCell = row.getCellForUpdate(i);
      IColumn col = table.getColumnSet().getColumn(i);
      tryParseAndSetValue(col, m_cells.get(i), newCell);
    }
    // reset status
    setStatus(row.getStatus());

    //add observer
    for (int i = 0; i < columnCount; i++) {
      Cell cell = m_cells.get(i);
      cell.setObserver(this);
    }

    // set table at end to avoid events before the row is even attached
    m_table = table;
  }

  @SuppressWarnings("unchecked")
  private <T> void tryParseAndSetValue(IColumn<T> col, Cell internalCell, ICell newCell) {
    internalCell.removeErrorStatus(ParsingFailedStatus.class);
    internalCell.removeErrorStatus(ValidationFailedStatus.class);
    T value = (T) newCell.getValue();
    try {
      internalCell.setText(newCell.getText());
      T parsedValue = col.parseValue(this, value);
      col.setValue(this, parsedValue);
      internalCell.setValue(parsedValue);
    }
    catch (ProcessingException e) {
      internalCell.setText(format(value));
      internalCell.addErrorStatus(new ValidationFailedStatus<Object>(e, value));
    }
  }

  private <T> String format(T value) {
    return StringUtility.nvl(value, "");
  }

  @Override
  public int getRowIndex() {
    return m_rowIndex;
  }

  public void setRowIndex(int index) {
    m_rowIndex = index;
  }

  @Override
  public void setStatus(int status) {
    try {
      setRowChanging(true);
      //
      if (getStatus() != status) {
        super.setStatus(status);
        m_rowPropertiesChanged = true;
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public void setEnabled(boolean b) {
    try {
      setRowChanging(true);
      //
      super.setEnabled(b);
      m_rowPropertiesChanged = true;
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public boolean isSelected() {
    if (getTable() != null) {
      return getTable().isSelectedRow(this);
    }
    return false;
  }

  @Override
  public boolean isChecked() {
    if (getTable() != null) {
      return getTable().isCheckedRow(this);
    }
    return false;
  }

  @Override
  public void setChecked(boolean b) {
    if (getTable() != null) {
      getTable().checkRow(this, b);
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
    return m_cells.size();
  }

  @Override
  public ICell getCell(IColumn column) {
    return getCell(column.getColumnIndex());
  }

  @Override
  public ICell getCell(int columnIndex) {
    return getCellForUpdate(columnIndex);
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
        m_cells.set(columnIndex, new Cell(this, cell));
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
    if (columnIndex < 0 || columnIndex >= m_cells.size()) {
      return new Cell();
    }
    return m_cells.get(columnIndex);
  }

  @Override
  public Object getCellValue(int columnIndex) {
    return getCell(columnIndex).getValue();
  }

  @Override
  public List<Object> getKeyValues() {
    List<Object> pk = new ArrayList<Object>();
    if (getTable() != null) {
      int[] keyColumns = getTable().getColumnSet().getKeyColumnIndexes();
      if (keyColumns.length == 0) {
        keyColumns = getTable().getColumnSet().getAllColumnIndexes();
      }
      for (int keyIndex : keyColumns) {
        pk.add(getCell(keyIndex).getValue());
      }
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
    return getCellForUpdate(columnIndex).setValue(value);
  }

  @Override
  public boolean setCellValues(List<? extends Object> values) throws ProcessingException {
    boolean changed = false;
    for (int i = 0; i < values.size(); i++) {
      boolean b = setCellValue(i, values.get(i));
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
      for (Cell cell : m_cells) {
        cell.setBackgroundColor(c);
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
      for (Cell cell : m_cells) {
        cell.setForegroundColor(c);
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  @Override
  public void setCssClass(String cssClass) {
    try {
      setRowChanging(true);
      super.setCssClass(cssClass);
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
      for (Cell cell : m_cells) {
        cell.setFont(f);
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
      for (Cell cell : m_cells) {
        cell.setTooltipText(s);
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
      super.setIconId(id);
      m_rowPropertiesChanged = true;
    }
    finally {
      setRowChanging(false);
    }
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
        for (int i = 0; i < m_cells.size(); i++) {
          if (getCell(i) == cell) {
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
    return getClass().getSimpleName() + "[" + VerboseUtility.dumpObjects(m_cells.toArray()) + "]";
  }
}
