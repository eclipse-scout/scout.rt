package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.ui.html.json.basic.cell.ICellValueReader;

public class TableCellValueReader implements ICellValueReader {

  private final JsonColumn<?> m_jsonColumn;
  private final ICell m_cell;

  public TableCellValueReader(JsonColumn<?> jsonColumn, ICell cell) {
    if (jsonColumn == null) {
      throw new IllegalArgumentException("Argument 'jsonColumn' must not be null");
    }
    if (cell == null) {
      throw new IllegalArgumentException("Argument 'cell' must not be null");
    }
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
