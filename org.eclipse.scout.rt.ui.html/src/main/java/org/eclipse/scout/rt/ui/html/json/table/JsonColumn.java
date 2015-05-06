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
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonColumn<T extends IColumn<?>> implements IJsonObject {

  private IUiSession m_uiSession;
  private T m_column;
  private int m_indexOffset;

  public JsonColumn(T model) {
    m_column = model;
  }

  public String getObjectType() {
    return "Column";
  }

  protected String getObjectTypeVariant() {
    return JsonAdapterUtility.getObjectType(getObjectType(), m_column);
  }

  public void setColumnIndexOffset(int indexOffset) {
    m_indexOffset = indexOffset;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    JsonObjectUtility.putProperty(json, "id", getColumn().getColumnId());
    JsonObjectUtility.putProperty(json, "objectType", getObjectTypeVariant());
    JsonObjectUtility.putProperty(json, "index", getColumn().getColumnIndex() - m_indexOffset);
    JsonObjectUtility.putProperty(json, "text", getColumn().getHeaderCell().getText());
    JsonObjectUtility.putProperty(json, "type", computeColumnType(getColumn()));
    JsonObjectUtility.putProperty(json, IColumn.PROP_WIDTH, getColumn().getWidth());
    if (getColumn().getInitialWidth() != getColumn().getWidth()) {
      JsonObjectUtility.putProperty(json, "initialWidth", getColumn().getInitialWidth());
    }
    JsonObjectUtility.putProperty(json, "summary", getColumn().isSummary());
    JsonObjectUtility.putProperty(json, IColumn.PROP_HORIZONTAL_ALIGNMENT, getColumn().getHorizontalAlignment());
    if (getColumn().isSortActive() && getColumn().isSortExplicit()) {
      JsonObjectUtility.putProperty(json, "sortActive", true);
      JsonObjectUtility.putProperty(json, "sortAscending", getColumn().isSortAscending());
      JsonObjectUtility.putProperty(json, "sortIndex", getColumn().getSortIndex());
    }
    if (getColumn() instanceof ICustomColumn) {
      JsonObjectUtility.putProperty(json, "custom", true);
    }
    //TODO CGU remove this properties, they get sent by cell, or change behaviour in model, see also todo in Column.js
    JsonObjectUtility.putProperty(json, IColumn.PROP_FIXED_WIDTH, getColumn().isFixedWidth());
    JsonObjectUtility.putProperty(json, IColumn.PROP_EDITABLE, getColumn().isEditable());
    JsonObjectUtility.putProperty(json, "mandatory", getColumn().isMandatory());
    JsonObjectUtility.putProperty(json, IColumn.PROP_HTML_ENABLED, getColumn().isHtmlEnabled());
    JsonObjectUtility.putProperty(json, IColumn.PROP_CSS_CLASS, getColumn().getCssClass());
    JsonObjectUtility.putProperty(json, "headerCssClass", getColumn().getHeaderCell().getCssClass());
    return json;
  }

  protected String computeColumnType(IColumn column) {
    if (column instanceof INumberColumn) {
      return "number";
    }
    if (column instanceof IDateColumn) {
      return "date";
    }
    return "text";
  }

  public Object cellValueToJson(Object value) {
    // In most cases it is not necessary to send the value to the client because text is sufficient
    return null;
  }

  public T getColumn() {
    return m_column;
  }

  public IUiSession getUiSession() {
    return m_uiSession;
  }

  public void setUiSession(IUiSession uiSession) {
    m_uiSession = uiSession;
  }
}
