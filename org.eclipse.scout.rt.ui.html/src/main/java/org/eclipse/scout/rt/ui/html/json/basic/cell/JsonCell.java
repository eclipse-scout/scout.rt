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
package org.eclipse.scout.rt.ui.html.json.basic.cell;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONObject;

public class JsonCell implements IJsonObject {
  private final ICell m_cell;
  private final String m_cellText;
  private Object m_cellValue;

  public JsonCell(ICell cell) {
    this(cell, null);
  }

  public JsonCell(ICell cell, ICellValueReader cellValueReader) {
    m_cell = cell;
    m_cellText = cell.getText();
    if (cellValueReader != null) {
      m_cellValue = cellValueReader.read();
    }
  }

  public final ICell getCell() {
    return m_cell;
  }

  public final Object getCellValue() {
    return m_cellValue;
  }

  public final String getCellText() {
    return m_cellText;
  }

  @Override
  public JSONObject toJson() {
    if (m_cell == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "value", m_cellValue);
    JsonObjectUtility.putProperty(json, "text", m_cellText);
    JsonObjectUtility.putProperty(json, "iconId", BinaryResourceUrlUtility.createIconUrl(m_cell.getIconId()));
    JsonObjectUtility.putProperty(json, "tooltipText", m_cell.getTooltipText());
    JsonObjectUtility.putProperty(json, "cssClass", m_cell.getCssClass());
    JsonObjectUtility.putProperty(json, "horizontalAlignment", m_cell.getHorizontalAlignment());
    JsonObjectUtility.putProperty(json, "foregroundColor", m_cell.getForegroundColor());
    JsonObjectUtility.putProperty(json, "backgroundColor", m_cell.getBackgroundColor());
    JsonObjectUtility.putProperty(json, "font", (m_cell.getFont() == null ? null : m_cell.getFont().toPattern()));
    //TODO CGU maybe only send if it differs from column, same for horizontal alignment. Could be difficult, depends on event order (update before column_structure_changed is bad. maybe solve in model?
    JsonObjectUtility.putProperty(json, "editable", m_cell.isEditable());
    JsonObjectUtility.putProperty(json, "htmlEnabled", m_cell.isHtmlEnabled());
    // TODO BSH Table | Add property "errorStatus"
    // TODO BSH Table | Add generic "cssStyle" property
    return json;
  }

  /**
   * Returns the cell as a {@link JSONObject}. If only the "text" property is set, the text itself is returned as
   * string. This is an optimization to reduce the amount of data (not a full object has to be sent).
   */
  public Object toJsonOrString() {
    JSONObject json = toJson();
    JsonObjectUtility.filterDefaultValues(json, "Cell");
    if (json.length() == 1 && json.has("text")) {
      return json.opt("text");
    }
    return json;
  }
}
