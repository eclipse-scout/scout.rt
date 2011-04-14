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
package org.eclipse.scout.rt.ui.swing.basic.table;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public class SwingTableModel extends AbstractTableModel {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingTableModel.class);
  private static final long serialVersionUID = 1L;

  private ISwingEnvironment m_env;
  private SwingScoutTable m_swingScoutTable;
  private int m_rowCount;

  public SwingTableModel(ISwingEnvironment env, SwingScoutTable swingScoutTable) {
    m_env = env;
    m_swingScoutTable = swingScoutTable;
    ITable table = m_swingScoutTable.getScoutObject();
    m_rowCount = table != null ? table.getFilteredRowCount() : 0;
  }

  @Override
  public int getRowCount() {
    return m_rowCount;
  }

  @Override
  public int getColumnCount() {
    ITable table = m_swingScoutTable.getScoutObject();
    return table != null ? table.getVisibleColumnCount() : 0;
  }

  @Override
  public String getColumnName(int col) {
    return null;
  }

  @Override
  public Object getValueAt(int row, int col) {
    ITable table = m_swingScoutTable.getScoutObject();
    if (table != null) {
      //fast access: try first and ignore error later
      ITableRow scoutRow = table.getFilteredRow(row);
      try {
        ICell visibleCell = table.getVisibleCell(scoutRow, col);
        return visibleCell;
      }
      catch (Throwable ex) {
        //fast access: ignore
      }
    }
    return null;
  }

  @Override
  public void setValueAt(final Object value, int rowIndex, final int colIndex) {
    //not implemented
  }

  @Override
  public boolean isCellEditable(final int x, final int y) {
    ITable table = m_swingScoutTable.getScoutObject();
    if (table == null) {
      return false;
    }
    ITableRow row = table.getFilteredRow(x);
    IColumn column = table.getColumnSet().getVisibleColumn(y);
    if (row == null || column == null) {
      return false;
    }
    return table.getCell(row, column).isEditable();
  }

  public void updateModelState(int newRowCount) {
    int oldRowCount = m_rowCount;
    m_rowCount = newRowCount;
    if (oldRowCount == 0 && newRowCount == 0) {
      //nop
    }
    else if (oldRowCount < newRowCount) {
      fireTableChanged(new TableModelEvent(this, oldRowCount, newRowCount - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
      fireTableChanged(new TableModelEvent(this, 0, oldRowCount - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }
    else if (oldRowCount > newRowCount) {
      fireTableChanged(new TableModelEvent(this, newRowCount, oldRowCount - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
      fireTableChanged(new TableModelEvent(this, 0, newRowCount - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }
    else {
      fireTableChanged(new TableModelEvent(this, 0, newRowCount - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }
  }

}
