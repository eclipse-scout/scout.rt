/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class SummaryCellBuilder implements ISummaryCellBuilder {
  private ITable m_table;

  public SummaryCellBuilder(ITable table) {
    m_table = table;
  }

  public ITable getTable() {
    return m_table;
  }

  @Override
  public ICell build(ITableRow row) {
    List<IColumn<?>> summaryColumns = getTable().getColumnSet().getSummaryColumns();
    if (summaryColumns.isEmpty()) {
      IColumn<?> col = getTable().getColumnSet().getFirstDefinedVisibleColumn();
      if (col != null) {
        summaryColumns = CollectionUtility.arrayList(col);
      }
    }
    if (summaryColumns.isEmpty()) {
      return new Cell();
    }
    else if (summaryColumns.size() == 1) {
      Cell cell = new Cell(getTable().getCell(row, summaryColumns.get(0)));
      if (cell.getIconId() == null) {
        // use icon of row
        cell.setIconId(row.getIconId());
      }
      return cell;
    }
    else {
      Cell cell = new Cell(getTable().getCell(row, summaryColumns.get(0)));
      if (cell.getIconId() == null) {
        // use icon of row
        cell.setIconId(row.getIconId());
      }
      StringBuilder b = new StringBuilder();
      for (IColumn<?> c : summaryColumns) {
        if (b.length() > 0) {
          b.append(" ");
        }
        b.append(getTable().getCell(row, c).toPlainText());
      }
      cell.setText(b.toString());
      return cell;
    }
  }
}
