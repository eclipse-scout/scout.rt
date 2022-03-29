/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICellObserver;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

/**
 * Internal representation of a Table row. Contains some more information than {@link TableRow} like validation.
 */
public class InternalTableRow extends TableRow implements ICellObserver {

  private final Map<ICell, Integer> m_updatedCells;
  private ITable m_table;
  private int m_rowIndex;
  private int m_rowChanging = 0;

  public InternalTableRow(ITable table) {
    super(table.getColumnSet());
    m_updatedCells = new HashMap<>(table.getColumnSet().getColumnCount());
    setFilterAcceptedInternal(true);
    m_table = table;
  }

  public InternalTableRow(ITable table, ITableRow row) {
    super(table.getColumnSet(), row);
    m_updatedCells = new HashMap<>(table.getColumnSet().getColumnCount());
    setFilterAcceptedInternal(true);
    setEnabled(row.isEnabled());
    m_rowIndex = row.getRowIndex();
    for (IColumn<?> c : table.getColumns()) {
      c.parseValueAndSet(this, c.getValue(row));
    }
    // copy status and properties
    setStatus(row.getStatus());
    setCssClass(row.getCssClass());
    setIconId(row.getIconId());
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
      if (getStatus() != status) {
        super.setStatus(status);
        setRowPropertiesChanged(true);
      }
    }
    finally {
      setRowChanging(false);
    }
  }

  /**
   * Sets the row status without firing any events.
   */
  public void setStatusInternal(int status) {
    super.setStatus(status);
  }

  @Override
  public void setEnabled(boolean b) {
    try {
      setRowChanging(true);
      //
      if (isEnabled() != b) {
        super.setEnabled(b);
        setRowPropertiesChanged(true);
      }
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
    return FLAGS_BIT_HELPER.isBitSet(FILTER_ACCEPTED, m_flags);
  }

  /**
   * do not use this internal method
   */
  public void setFilterAcceptedInternal(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(FILTER_ACCEPTED, b, m_flags);
  }

  @Override
  public boolean isRejectedByUser() {
    return FLAGS_BIT_HELPER.isBitSet(REJECTED_BY_USER, m_flags);
  }

  public void setRejectedByUser(boolean rejectedByUser) {
    m_flags = FLAGS_BIT_HELPER.changeBit(REJECTED_BY_USER, rejectedByUser, m_flags);
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
    if (getTable() == null) {
      return new ArrayList<>(0);
    }
    ColumnSet columnSet = getTable().getColumnSet();
    int[] keyColumns = columnSet.getKeyColumnCount() > 0 ? columnSet.getKeyColumnIndexes() : columnSet.getAllColumnIndexes();
    List<Object> pk = new ArrayList<>(keyColumns.length);
    for (int keyIndex : keyColumns) {
      pk.add(getCell(keyIndex).getValue());
    }
    return pk;
  }

  @Override
  public List<Object> getParentKeyValues() {
    List<Object> pk = new ArrayList<>();
    if (getTable() != null) {
      int[] keyColumns = getTable().getColumnSet().getParentKeyColumnIndexes();
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
      if (m_rowChanging == 0 && isRowPropertiesChanged()) {
        setRowPropertiesChanged(false);
        if (getTable() != null) {
          getTable().updateRow(this);
        }
        if (m_updatedCells != null) {
          m_updatedCells.clear();
        }
      }
    }
  }

  @Override
  public boolean/* changed */ setCellValue(int columnIndex, Object value) {
    boolean changed = getCellForUpdate(columnIndex).setValue(value);
    return changed;
  }

  @Override
  public boolean setCellValues(List<?> values) {
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
      setRowPropertiesChanged(true);
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
      if (ObjectUtility.notEquals(getIconId(), id)) {
        super.setIconId(id);
        setRowPropertiesChanged(true);
      }
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
    if (ObjectUtility.equals(oldValue, value)) {
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
      if (isStatusNonchanged() && isRowUpdate(changedBit)) {
        setStatusUpdated();
      }
      setRowPropertiesChanged(true);
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
    m_updatedCells.compute(cell, (iCell, bitMask) -> setBit(bitMask, changeBit));
  }

  private List<ICell> getChangedCells(int changedBit) {
    List<ICell> cells = new ArrayList<>();
    for (Entry<ICell, Integer> e : m_updatedCells.entrySet()) {
      if (isBitSet(e.getValue(), changedBit)) {
        cells.add(e.getKey());
      }
    }
    return cells;
  }

  private List<ICell> getChangedCells() {
    List<ICell> cells = new ArrayList<>(m_updatedCells.size());
    for (Entry<ICell, Integer> e : m_updatedCells.entrySet()) {
      Integer changedBits = e.getValue();
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

  @SuppressWarnings("SuspiciousMethodCalls")
  private Set<Integer> getColumnIndexes(List<ICell> cells) {
    if (CollectionUtility.isEmpty(cells)) {
      return Collections.emptySet();
    }

    Set<Integer> result = new HashSet<>();
    for (ICell cell : cells) {
      result.add(m_cells.indexOf(cell));
    }
    return result;
  }

  private Integer setBit(Integer bitMask, int bitPos) {
    return bitMask == null
        ? 1 << bitPos
        : bitMask.intValue() | 1 << bitPos;
  }

  private boolean isBitSet(Integer bitMask, int bitPos) {
    return bitMask != null
        && (bitMask.intValue() & 1 << bitPos) != 0;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + VerboseUtility.dumpObjects(m_cells.toArray()) + "]";
  }
}
