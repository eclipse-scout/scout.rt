/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.basic.cell;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONObject;

public class JsonCell implements IJsonObject {
  private final ICell m_cell;
  private final String m_cellText;
  private final IJsonAdapter<?> m_parentAdapter;
  private Object m_cellValue;
  private final Integer m_cellSortCode;

  public JsonCell(ICell cell, IJsonAdapter<?> parentAdapter) {
    this(cell, parentAdapter, null);
  }

  public JsonCell(ICell cell, IJsonAdapter<?> parentAdapter, ICellValueReader cellValueReader) {
    this(cell, parentAdapter, cellValueReader, null);
  }

  public JsonCell(ICell cell, IJsonAdapter<?> parentAdapter, ICellValueReader cellValueReader, Integer cellSortCode) {
    m_cell = cell;
    m_cellText = cell.getText();
    m_parentAdapter = parentAdapter;
    if (cellValueReader != null) {
      m_cellValue = cellValueReader.read();
    }
    m_cellSortCode = cellSortCode;
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

  public final Integer getCellSortCode() {
    return m_cellSortCode;
  }

  @Override
  public JSONObject toJson() {
    if (m_cell == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("value", m_cellValue);
    if (getCell().isHtmlEnabled()) {
      json.put("text", BinaryResourceUrlUtility.replaceImageUrls(m_parentAdapter, m_cellText));
    }
    else {
      json.put("text", m_cellText);
    }
    json.put("iconId", BinaryResourceUrlUtility.createIconUrl(m_cell.getIconId()));
    json.put("tooltipText", m_cell.getTooltipText());
    if (m_cell.getErrorStatus() != null && m_cell.getErrorStatus().getSeverity() == IStatus.ERROR) {
      json.put("errorStatus", JsonStatus.toJson(m_cell.getErrorStatus()));
    }
    json.put("cssClass", m_cell.getCssClass());
    json.put("horizontalAlignment", m_cell.getHorizontalAlignment());
    json.put("foregroundColor", m_cell.getForegroundColor());
    json.put("backgroundColor", m_cell.getBackgroundColor());
    json.put("font", (m_cell.getFont() == null ? null : m_cell.getFont().toPattern()));
    json.put("editable", m_cell.isEditable());
    json.put("htmlEnabled", m_cell.isHtmlEnabled());
    json.put("mandatory", m_cell.isMandatory());
    if (getCellSortCode() != null) {
      json.put("sortCode", getCellSortCode());
    }
    return json;
  }

  /**
   * Returns the cell as a {@link JSONObject}. If only the "text" property is set, the text itself is returned as
   * string. This is an optimization to reduce the amount of data (not a full object has to be sent).
   */
  public Object toJsonOrString() {
    JSONObject json = toJson();
    JsonObjectUtility.filterDefaultValues(json, "Cell");
    String text = json.optString("text");
    Object value = json.opt("value");
    // If value is null and text too, no need to send both -> remove value
    if (JSONObject.NULL.equals(value) && StringUtility.isNullOrEmpty(text)) {
      json.remove("value");
    }
    if (json.length() == 1 && json.has("text")) {
      return text;
    }
    return json;
  }
}
