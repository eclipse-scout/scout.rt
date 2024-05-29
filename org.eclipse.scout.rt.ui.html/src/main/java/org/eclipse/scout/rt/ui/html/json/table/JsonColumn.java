/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.InspectorObjectIdProvider;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.InspectorInfo;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.basic.cell.ICellValueReader;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTextColumnUserFilter;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONObject;

public class JsonColumn<T extends IColumn<?>> implements IJsonObject {

  public static final String OBJECT_TYPE = "Column";

  private String m_id;
  private final T m_column;
  private JsonTable<?> m_jsonTable;
  private int m_indexOffset;

  protected static final LazyValue<InspectorObjectIdProvider> INSPECTOR_ID_PROVIDER = new LazyValue<>(InspectorObjectIdProvider.class);
  private static final String PROP_INITIAL_ALWAYS_INCLUDE_SORT_AT_BEGIN = "initialAlwaysIncludeSortAtBegin";
  private static final String PROP_INITIAL_ALWAYS_INCLUDE_SORT_AT_END = "initialAlwaysIncludeSortAtEnd";

  private static final String PROP_REMOVABLE = "removable";
  private static final String PROP_MODIFIABLE = "modifiable";
  private static final String PROP_COMPACTED = "compacted";

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

  /**
   * @return true, if the value should be sent to client, false if not
   */
  public boolean isValueRequired() {
    return false;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("id", getId());
    json.put("objectType", getObjectTypeVariant());
    T column = getColumn();
    json.put("index", column.getColumnIndex() - m_indexOffset);
    json.put("text", column.getHeaderCell().getText());
    json.put(IColumn.PROP_WIDTH, column.getWidth());
    json.put(IColumn.PROP_MIN_WIDTH, column.getMinWidth());
    json.put(IColumn.PROP_AUTO_OPTIMIZE_MAX_WIDTH, column.getAutoOptimizeMaxWidth());
    if (column.getInitialWidth() != column.getWidth()) {
      json.put("initialWidth", column.getInitialWidth());
    }
    json.put(IColumn.PROP_HORIZONTAL_ALIGNMENT, column.getHorizontalAlignment());
    if (column.isSortActive()) {
      json.put("sortActive", true);
      json.put("sortAscending", column.isSortAscending());
      json.put("sortIndex", column.getSortIndex());
      json.put("grouped", column.isGroupingActive());
    }
    if (column.getTable().getCheckableColumn() == column) {
      json.put("checkable", true);
    }
    json.put(IColumn.PROP_FIXED_WIDTH, column.isFixedWidth());
    json.put(IColumn.PROP_FIXED_POSITION, column.isFixedPosition());
    json.put(IColumn.PROP_AUTO_OPTIMIZE_WIDTH, column.isAutoOptimizeWidth());
    json.put(IColumn.PROP_EDITABLE, column.isEditable());
    json.put("mandatory", column.isMandatory());
    json.put("textWrap", column.isTextWrap());
    json.put(IColumn.PROP_HTML_ENABLED, column.isHtmlEnabled());
    json.put(IColumn.PROP_CSS_CLASS, column.getCssClass());
    json.put("headerCssClass", column.getHeaderCell().getCssClass());
    json.put("headerHtmlEnabled", column.getHeaderCell().isHtmlEnabled());
    json.put("headerMenuEnabled", column.getHeaderCell().isMenuEnabled());
    json.put("headerBackgroundColor", column.getHeaderCell().getBackgroundColor());
    json.put("headerForegroundColor", column.getHeaderCell().getForegroundColor());
    json.put("headerFont", column.getHeaderCell().getFont() != null ? column.getHeaderCell().getFont().toPattern() : null);
    json.put("headerTooltipText", column.getHeaderCell().getTooltipText());
    json.put("headerTooltipHtmlEnabled", column.getHeaderCell().isTooltipHtmlEnabled());
    json.put("headerIconId", BinaryResourceUrlUtility.createIconUrl(column.getHeaderCell().getIconId()));
    BEANS.get(InspectorInfo.class).put(getUiSession().currentHttpRequest(), json, column, c -> INSPECTOR_ID_PROVIDER.get().getIdForColumn(c));
    json.put(IColumn.PROP_UI_SORT_POSSIBLE, column.isUiSortPossible());
    json.put(PROP_INITIAL_ALWAYS_INCLUDE_SORT_AT_BEGIN, column.isInitialAlwaysIncludeSortAtBegin());
    json.put(PROP_INITIAL_ALWAYS_INCLUDE_SORT_AT_END, column.isInitialAlwaysIncludeSortAtEnd());
    json.put(PROP_REMOVABLE, column.isRemovable());
    json.put(PROP_MODIFIABLE, column.isModifiable());
    json.put(IColumn.PROP_NODE_COLUMN_CANDIDATE, column.isNodeColumnCandidate());
    json.put(PROP_COMPACTED, column.isCompacted());

    return json;
  }

  /**
   * This method creates a type specific filter-state model for the given column and JSON data. Sub-classes may
   * implement this method to return a different type. The default impl. returns a {@link ColumnUserFilterState}.
   */
  protected ColumnUserFilterState createFilterStateFromJson(JSONObject json) {
    return new JsonTextColumnUserFilter(null).createFilterStateFromJson(getColumn(), json);
  }

  public Object cellValueToJson(Object value) {
    if (isValueRequired()) {
      return value;
    }
    return null;
  }

  protected ICellValueReader createCellValueReader(ICell cell) {
    return new TableCellValueReader(this, cell);
  }

  public JsonCell createJsonCell(ICell cell, IJsonAdapter<?> parentAdapter) {
    return new JsonCell(cell, parentAdapter, createCellValueReader(cell));
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
