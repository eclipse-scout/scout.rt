/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.ui.html.json.basic.cell.ICellValueReader;
import org.json.JSONObject;

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
    if (cellValue == null && m_jsonColumn.isValueRequired()) {
      // Explicitly set to null so that the UI knows that the value is null and does not try to parse the value from the text
      return JSONObject.NULL;
    }
    // only return value if it is different than the text to prevent sending duplicate values
    if (cellValue != null && !String.valueOf(cellValue).equals(m_cell.getText())) {
      return cellValue;
    }
    // In case of custom columns (if the Column directly extends AbstractColumn and if there is no UI representation) the value may not be sent.
    // But the UI needs a value -> give the UI a hint so that it can use the text as value
    return null;
  }
}
