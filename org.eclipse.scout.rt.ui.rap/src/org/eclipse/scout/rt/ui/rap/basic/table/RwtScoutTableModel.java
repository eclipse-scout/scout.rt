/*******************************************************************************
 * Copyright (c) 2011,2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.table;

import java.util.HashMap;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.swt.graphics.Font;

public class RwtScoutTableModel implements IStructuredContentProvider, ITableFontProvider {
  private static final long serialVersionUID = 1L;

  private final ITable m_scoutTable;
  private HashMap<ITableRow, HashMap<IColumn<?>, ICell>> m_cachedCells;
  private final RwtScoutTable m_uiTable;
  private final TableColumnManager m_columnManager;

  public RwtScoutTableModel(ITable scoutTable, RwtScoutTable uiTable, TableColumnManager columnManager) {
    m_scoutTable = scoutTable;
    m_uiTable = uiTable;
    m_columnManager = columnManager;
    rebuildCache();
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (m_scoutTable != null) {
      return m_scoutTable.getFilteredRows().toArray();
    }
    else {
      return new Object[0];
    }
  }

  @Override
  public Font getFont(Object element, int columnIndex) {
    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        return getRwtScoutTable().getUiEnvironment().getFont(cell.getFont(), getRwtScoutTable().getUiField().getFont());
      }
    }
    return null;
  }

  @Override
  public void dispose() {
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  public void consumeTableModelEvent(RwtScoutTableEvent uiTableEvent) {
    rebuildCache();
  }

  protected ICell getCell(Object row, int colIndex) {
    IColumn<?> column = m_columnManager.getColumnByModelIndex(colIndex - 1);
    if (column != null) {
      if (m_cachedCells.get(row) == null) {
        rebuildCache();
      }
      return m_cachedCells.get(row).get(column);
    }
    else {
      return null;
    }
  }

  private void rebuildCache() {
    m_cachedCells = new HashMap<ITableRow, HashMap<IColumn<?>, ICell>>();
    if (m_scoutTable != null) {
      for (ITableRow scoutRow : m_scoutTable.getRows()) {
        HashMap<IColumn<?>, ICell> cells = new HashMap<IColumn<?>, ICell>();
        for (IColumn<?> col : m_scoutTable.getColumnSet().getVisibleColumns()) {
          cells.put(col, m_scoutTable.getCell(scoutRow, col));
        }
        m_cachedCells.put(scoutRow, cells);
      }
    }
  }

  public RwtScoutTable getRwtScoutTable() {
    return m_uiTable;
  }
}
