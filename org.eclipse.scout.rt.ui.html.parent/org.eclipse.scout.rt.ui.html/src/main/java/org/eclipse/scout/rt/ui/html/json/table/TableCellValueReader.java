package org.eclipse.scout.rt.ui.html.json.table;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.basic.cell.ICellValueReader;

public class TableCellValueReader implements ICellValueReader {
  private IColumn<?> m_column;
  private ICell m_cell;

  public TableCellValueReader(IColumn<?> column, ICell cell) {
    m_column = column;
    m_cell = cell;
  }

  /**
   * Only returns a value for Date and Number columns. For other columns, null is returned.
   */
  @Override
  public Object read() {
    Object cellValue = null;
    if (m_column instanceof IDateColumn) {
      Date date = (Date) m_cell.getValue();
      if (date != null) {
        IDateColumn dateColumn = (IDateColumn) m_column;
        cellValue = new JsonDate(date).asJsonString(false, dateColumn.isHasDate(), dateColumn.isHasTime());
      }
    }
    else if (m_column instanceof INumberColumn) {
      cellValue = m_cell.getValue();
    }
    // only return value if it is different than the text to prevent sending duplicate values
    if (cellValue != null && !String.valueOf(cellValue).equals(m_cell.getText())) {
      return cellValue;
    }
    return null;

  }
}
