package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.ui.html.json.basic.cell.ICellValueReader;

public class TableCellValueReader implements ICellValueReader {
  private JsonColumn<?> m_jsonColumn;
  private ICell m_cell;

  public TableCellValueReader(JsonColumn<?> jsonColumn, ICell cell) {
    m_jsonColumn = jsonColumn;
    m_cell = cell;
  }

  @Override
  public Object read() {
    Object cellValue = m_jsonColumn.cellValueToJson(m_cell.getValue());

    // only return value if it is different than the text to prevent sending duplicate values
    if (cellValue != null && !String.valueOf(cellValue).equals(m_cell.getText())) {
      return cellValue;
    }
    return null;

  }
}
