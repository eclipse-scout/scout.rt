/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.InspectorInfo;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTextColumnUserFilter;
import org.json.JSONObject;

public class JsonColumn<T extends IColumn<?>> implements IJsonObject {

  public static final String OBJECT_TYPE = "Column";

  private String m_id;
  private T m_column;
  private JsonTable<?> m_jsonTable;
  private int m_indexOffset;

  private static final String PROP_INITIAL_ALWAYS_INCLUDE_SORT_AT_BEGIN = "initialAlwaysIncludeSortAtBegin";
  private static final String PROP_INITIAL_ALWAYS_INCLUDE_SORT_AT_END = "initialAlwaysIncludeSortAtEnd";

  private static final String PROP_REMOVABLE = "removable";
  private static final String PROP_MODIFIABLE = "modifiable";

  public JsonColumn(T model) {
    m_column = model;
  }

  public String getObjectType() {
    return OBJECT_TYPE;
  }

  protected String getObjectTypeVariant() {
    return JsonAdapterUtility.getObjectType(getObjectType(), m_column);
  }

  public void setColumnIndexOffset(int indexOffset) {
    m_indexOffset = indexOffset;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("id", getId());
    json.put("objectType", getObjectTypeVariant());
    json.put("index", getColumn().getColumnIndex() - m_indexOffset);
    json.put("text", getColumn().getHeaderCell().getText());
    json.put(IColumn.PROP_WIDTH, getColumn().getWidth());
    if (getColumn().getInitialWidth() != getColumn().getWidth()) {
      json.put("initialWidth", getColumn().getInitialWidth());
    }
    json.put("summary", getColumn().isSummary());
    json.put(IColumn.PROP_HORIZONTAL_ALIGNMENT, getColumn().getHorizontalAlignment());
    if (getColumn().isSortActive()) {
      json.put("sortActive", true);
      json.put("sortAscending", getColumn().isSortAscending());
      json.put("sortIndex", getColumn().getSortIndex());
      json.put("grouped", getColumn().isGroupingActive());
    }
    if (getColumn().getTable().isCheckable() && getColumn().getTable().getCheckableColumn() == getColumn()) {
      json.put("checkable", true);
    }
    //TODO [5.2] cgu: remove this properties, they get sent by cell, or change behaviour in model, see also todo in Column.js
    json.put(IColumn.PROP_FIXED_WIDTH, getColumn().isFixedWidth());
    json.put(IColumn.PROP_EDITABLE, getColumn().isEditable());
    json.put("mandatory", getColumn().isMandatory());
    json.put(IColumn.PROP_HTML_ENABLED, getColumn().isHtmlEnabled());
    json.put(IColumn.PROP_CSS_CLASS, getColumn().getCssClass());
    json.put("headerCssClass", getColumn().getHeaderCell().getCssClass());
    json.put("headerBackgroundColor", getColumn().getHeaderCell().getBackgroundColor());
    json.put("headerForegroundColor", getColumn().getHeaderCell().getForegroundColor());
    json.put("headerFont", getColumn().getHeaderCell().getFont() != null ? getColumn().getHeaderCell().getFont().toPattern() : null);
    json.put("headerTooltipText", getColumn().getHeaderCell().getTooltipText());
    json.put("headerIconId", getColumn().getHeaderCell().getIconId());
    BEANS.get(InspectorInfo.class).put(getUiSession(), json, getColumn());
    json.put(IColumn.PROP_UI_SORT_POSSIBLE, getColumn().isUiSortPossible());
    json.put(PROP_INITIAL_ALWAYS_INCLUDE_SORT_AT_BEGIN, getColumn().isInitialAlwaysIncludeSortAtBegin());
    json.put(PROP_INITIAL_ALWAYS_INCLUDE_SORT_AT_END, getColumn().isInitialAlwaysIncludeSortAtEnd());
    json.put(PROP_REMOVABLE, getColumn().isRemovable());
    json.put(PROP_MODIFIABLE, getColumn().isModifiable());

    return json;
  }

  /**
   * This method creates a type specific filter-state model for the given column and JSON data. Sub-classes may
   * implement this method to return a different type. The default impl. returns a {@link ColumnUserFilterState}.
   *
   * @return
   */
  protected ColumnUserFilterState createFilterStateFromJson(JSONObject json) {
    return new JsonTextColumnUserFilter(null).createFilterStateFromJson(getColumn(), json);
  }

  public Object cellValueToJson(Object value) {
    // In most cases it is not necessary to send the value to the client because text is sufficient
    return null;
  }

  public T getColumn() {
    return m_column;
  }

  public IUiSession getUiSession() {
    return getJsonTable().getUiSession();
  }

  public String getId() {
    return m_id;
  }

  public void setId(String id) {
    m_id = id;
  }

  public void setJsonTable(JsonTable<?> jsonTable) {
    m_jsonTable = jsonTable;
  }

  public JsonTable<?> getJsonTable() {
    return m_jsonTable;
  }
}
