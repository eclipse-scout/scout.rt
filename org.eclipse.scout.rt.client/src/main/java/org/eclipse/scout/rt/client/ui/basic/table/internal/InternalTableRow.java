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
package org.eclipse.scout.rt.client.ui.basic.table.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
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
  private boolean m_rejectedByUser;
  private final Map<ICell, Set<Integer>> m_updatedCells = new HashMap<>();

  private InternalTableRow() {
    super(null);
  }

  public InternalTableRow(ITable table) {
    super(table.getColumnSet());
    m_table = table;
  }

  public InternalTableRow(ITable table, ITableRow row) {
    super(table.getColumnSet(), row);
    setEnabled(row.isEnabled());
    m_rowIndex = row.getRowIndex();
    for (IColumn<?> c : table.getColumns()) {
      c.parseValueAndSet(this, c.getValue(row));
    }
    // reset status
    setStatus(row.getStatus());
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

  /**
   * Sets the row status without firing any events.
   */
  @Internal
  public void setStatusInternal(int status) {
    super.setStatus(status);
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
  public boolean isRejectedByUser() {
    return m_rejectedByUser;
  }

  public void setRejectedByUser(boolean rejectedByUser) {
    m_rejectedByUser = rejectedByUser;
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
  public void setCell(IColumn column, ICell cell) {
    setCell(column.getColumnIndex(), cell);
  }

  @Override
  public void setCell(int columnIndex, ICell cell) {
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
        m_updatedCells.clear();
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
  public boolean/* changed */ setCellValue(int columnIndex, Object value) {
    return getCellForUpdate(columnIndex).setValue(value);
  }

  @Override
  public boolean setCellValues(List<? extends Object> values) {
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
  public void touch() {
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
  public void delete() {
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
  public Object validateValue(ICell cell, Object value) {
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
      if (isStatusNonchanged() && isRowUpdate(changedBit)) {
        setStatusUpdated();
      }
      m_rowPropertiesChanged = true;
      // Remember changed column
      setCellChanged(cell, changedBit);
    }
    finally {
      setRowChanging(false);
    }
  }

  private boolean isRowUpdate(int changedBit) {
    return changedBit == ICell.VALUE_BIT;
  }

  private void setCellChanged(ICell cell, int changeBit) {
    Set<Integer> updatedBits = m_updatedCells.get(cell);
    if (updatedBits == null) {
      updatedBits = new HashSet<Integer>();
    }
    updatedBits.add(changeBit);
    m_updatedCells.put(cell, updatedBits);
  }

  private List<ICell> getChangedCells(int changedBit) {
    ArrayList<ICell> cells = new ArrayList<ICell>();
    for (Entry<ICell, Set<Integer>> e : m_updatedCells.entrySet()) {
      Set<Integer> value = e.getValue();
      if (value != null && value.contains(changedBit)) {
        cells.add(e.getKey());
      }
    }
    return cells;
  }

  private List<ICell> getChangedCells() {
    ArrayList<ICell> cells = new ArrayList<ICell>();
    for (Entry<ICell, Set<Integer>> e : m_updatedCells.entrySet()) {
      Set<Integer> changedBits = e.getValue();
      if (changedBits != null) {
        cells.add(e.getKey());
      }
    }
    return cells;
  }

  @Override
  public Set<Integer> getUpdatedColumnIndexes() {
    return getColumnIndexes(getChangedCells());
  }

  @Override
  public Set<Integer> getUpdatedColumnIndexes(int changedBit) {
    return getColumnIndexes(getChangedCells(changedBit));
  }

  private Set<Integer> getColumnIndexes(List<ICell> cells) {
    Map<ICell, Integer> indexesByCell = createCellIndexMap();
    Set<Integer> result = new HashSet<>();
    for (ICell cell : cells) {
      Integer index = indexesByCell.get(cell);
      if (index != null) {
        result.add(index);
      }
    }
    return result;
  }

  private Map<ICell, Integer> createCellIndexMap() {
    Map<ICell, Integer> indexesByCell = new HashMap<>();
    for (int i = 0; i < m_cells.size(); i++) {
      indexesByCell.put(m_cells.get(i), i);
    }
    return indexesByCell;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + VerboseUtility.dumpObjects(m_cells.toArray()) + "]";
  }
}
