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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.menu.IContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonTable<T extends ITable> extends AbstractJsonPropertyObserver<T> implements IContextMenuOwner {

  private static final String ROWS = "rows";
  public static final String EVENT_ROW_CLICKED = "rowClicked";
  public static final String EVENT_ROW_ACTION = "rowAction";
  public static final String EVENT_HYPERLINK_ACTION = "hyperlinkAction";
  public static final String EVENT_ROWS_SELECTED = "rowsSelected";
  public static final String EVENT_ROWS_UPDATED = "rowsUpdated";
  public static final String EVENT_ROWS_DELETED = "rowsDeleted";
  public static final String EVENT_ALL_ROWS_DELETED = "allRowsDeleted";
  public static final String EVENT_ROWS_SORTED = "rowsSorted";
  public static final String EVENT_SORT_ROWS = "sortRows";
  public static final String EVENT_COLUMN_MOVED = "columnMoved";
  public static final String EVENT_COLUMN_RESIZED = "columnResized";
  public static final String EVENT_RELOAD = "reload";
  public static final String EVENT_RESET_COLUMNS = "resetColumns";
  public static final String EVENT_ROWS_CHECKED = "rowsChecked";
  public static final String EVENT_COLUMN_ORDER_CHANGED = "columnOrderChanged";
  public static final String EVENT_COLUMN_HEADERS_UPDATED = "columnHeadersUpdated";

  public static final String PROP_ROW_IDS = "rowIds";
  public static final String PROP_ROW_ID = "rowId";
  public static final String PROP_COLUMN_ID = "columnId";
  public static final String PROP_COLUMN_IDS = "columnIds";
  public static final String PROP_COLUMNS = "columns";
  public static final String PROP_CONTROLS = "controls";
  public static final String PROP_SELECTED_ROW_IDS = "selectedRowIds";

  private TableListener m_tableListener;
  private final Map<String, ITableRow> m_tableRows;
  private final Map<ITableRow, String> m_tableRowIds;
  private final TableEventFilter m_tableEventFilter;

  public JsonTable(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
    m_tableRows = new HashMap<>();
    m_tableRowIds = new HashMap<>();
    m_tableEventFilter = new TableEventFilter(this);
  }

  @Override
  protected void initJsonProperties(T model) {
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_DEFAULT_ICON, model) {
      @Override
      protected String modelValue() {
        return getModel().getDefaultIconId();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_MULTI_SELECT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultiSelect();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_MULTI_CHECK, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultiCheck();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_MULTILINE_TEXT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultilineText();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_ROW_HEIGHT_HINT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getRowHeightHint();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_CHECKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCheckable();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_HEADER_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHeaderVisible();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_KEYBOARD_NAVIGATION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().hasKeyboardNavigation();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_AUTO_RESIZE_COLUMNS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoResizeColumns();
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITable>(ITable.PROP_KEY_STROKES, model, getJsonSession()) {
      @Override
      protected List<IKeyStroke> modelValue() {
        return getModel().getKeyStrokes();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_SCROLL_TO_SELECTION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollToSelection();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_TABLE_STATUS_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTableStatusVisible();
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITable>(ITable.PROP_TABLE_CONTROLS, model, getJsonSession()) {
      @Override
      protected List<ITableControl> modelValue() {
        return getModel().getTableControls();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "Table";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getContextMenu());
//    attachAdapters(getModel().getTableControls());
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_tableListener != null) {
      throw new IllegalStateException();
    }
    m_tableListener = new P_TableListener();
    getModel().addUITableListener(m_tableListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_tableListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeTableListener(m_tableListener);
    m_tableListener = null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, PROP_COLUMNS, columnsToJson(getColumns()));
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : getModel().getRows()) {
      JSONObject jsonRow = tableRowToJson(row);
      jsonRows.put(jsonRow);
    }
    putProperty(json, ROWS, jsonRows);
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      JsonObjectUtility.putProperty(json, PROP_MENUS, JsonObjectUtility.adapterIdsToJson(jsonContextMenu.getJsonChildActions()));
    }
//    putAdapterIdsProperty(json, "controls", getModel().getTableControls()); // FIXME AWE: rename to "tableControls" in JS/Java
    putProperty(json, PROP_SELECTED_ROW_IDS, rowIdsToJson(getModel().getSelectedRows()));
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_ROW_CLICKED.equals(event.getType())) {
      handleUiRowClicked(event, res);
    }
    else if (EVENT_ROW_ACTION.equals(event.getType())) {
      handleUiRowAction(event, res);
    }
    else if (EVENT_ROWS_SELECTED.equals(event.getType())) {
      handleUiRowsSelected(event, res);
    }
    else if (EVENT_RELOAD.equals(event.getType())) {
      handleUiReload(event, res);
    }
    else if (EVENT_RESET_COLUMNS.equals(event.getType())) {
      handleUiResetColumns(event, res);
    }
    else if (EVENT_SORT_ROWS.equals(event.getType())) {
      handleUiSortRows(event, res);
    }
    else if (EVENT_ROWS_SORTED.equals(event.getType())) {
      handleUiRowsSorted(event, res);
    }
    else if (EVENT_COLUMN_MOVED.equals(event.getType())) {
      handleUiColumnMoved(event, res);
    }
    else if (EVENT_COLUMN_RESIZED.equals(event.getType())) {
      handleUiColumnResized(event, res);
    }
    else if (EVENT_HYPERLINK_ACTION.equals(event.getType())) {
      handleUiHyperlinkAction(event, res);
    }
    else if (EVENT_ROWS_CHECKED.equals(event.getType())) {
      handleUiRowChecked(event, res);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  protected void handleUiRowClicked(JsonEvent event, JsonResponse res) {
    ITableRow tableRow = extractTableRow(event.getData());
    IColumn column = extractColumn(event.getData());
    ArrayList<ITableRow> rows = new ArrayList<ITableRow>();
    rows.add(tableRow);
    addTableEventFilterCondition(TableEvent.TYPE_ROWS_UPDATED).setRows(rows);
    getModel().getUIFacade().setContextColumnFromUI(column);
    getModel().getUIFacade().fireRowClickFromUI(tableRow, MouseButton.Left);
  }

  protected void handleUiRowChecked(JsonEvent event, JsonResponse res) {
    CheckedInfo tableRowsChecked = jsonToCheckeInfo(event.getData());
    addTableEventFilterCondition(TableEvent.TYPE_ROWS_CHECKED).setRows(tableRowsChecked.m_allRows);
    addTableEventFilterCondition(TableEvent.TYPE_ROWS_UPDATED).setRows(tableRowsChecked.m_allRows);

    if (tableRowsChecked.m_checkedRows.size() > 0) {
      getModel().getUIFacade().setCheckedRowsFromUI(tableRowsChecked.m_checkedRows, true);
    }
    if (tableRowsChecked.m_uncheckedRows.size() > 0) {
      getModel().getUIFacade().setCheckedRowsFromUI(tableRowsChecked.m_uncheckedRows, false);
    }
  }

  private TableEventFilterCondition addTableEventFilterCondition(int tableEventType) {
    TableEventFilterCondition conditon = new TableEventFilterCondition(tableEventType);
    m_tableEventFilter.addCondition(conditon);
    return conditon;
  }

  protected void handleUiRowsSelected(JsonEvent event, JsonResponse res) {
    List<ITableRow> tableRows = extractTableRows(event.getData());
    addTableEventFilterCondition(TableEvent.TYPE_ROWS_SELECTED).setRows(tableRows);
    getModel().getUIFacade().setSelectedRowsFromUI(tableRows);
  }

  protected void handleUiReload(JsonEvent event, JsonResponse res) {
    getModel().getUIFacade().fireTableReloadFromUI();
  }

  protected void handleUiResetColumns(JsonEvent event, JsonResponse res) {
    //FIXME AWE/CGU Code from ResetColumnsMenu, move to ITableUiFacade
    try {
      getModel().setTableChanging(true);
      getModel().resetDisplayableColumns();
      try {
        ITableColumnFilterManager m = getModel().getColumnFilterManager();
        if (m != null) {
          m.reset();
        }
        ITableCustomizer cst = getModel().getTableCustomizer();
        if (cst != null) {
          cst.removeAllColumns();
        }
      }
      catch (ProcessingException e) {
        throw new JsonException("", e);
      }
    }
    finally {
      getModel().setTableChanging(false);
    }
  }

  /**
   * Makes sure that no rowOrderChanged event is returned to the client after sorting because the sorting already
   * happened on client as well.
   */
  protected void handleUiRowsSorted(JsonEvent event, JsonResponse res) {
    IColumn column = extractColumn(event.getData());
    boolean multiSort = event.getData().optBoolean("multiSort");
    boolean sortingRemoved = event.getData().optBoolean("sortingRemoved");
    addTableEventFilterCondition(TableEvent.TYPE_ROW_ORDER_CHANGED);
    fireSortRowsFromUi(column, multiSort, sortingRemoved);
  }

  protected void handleUiSortRows(JsonEvent event, JsonResponse res) {
    IColumn column = extractColumn(event.getData());
    boolean multiSort = event.getData().optBoolean("multiSort");
    boolean sortingRemoved = event.getData().optBoolean("sortingRemoved");
    fireSortRowsFromUi(column, multiSort, sortingRemoved);
  }

  protected void fireSortRowsFromUi(IColumn<?> column, boolean multiSort, boolean sortingRemoved) {
    // FIXME CGU: add filter for HEADER_UPDATE event with json data of column (execDecorateHeaderCell is called which may change other header properties (text etc)
    if (sortingRemoved) {
      getModel().getUIFacade().fireSortColumnRemovedFromUI(column);
    }
    else {
      getModel().getUIFacade().fireHeaderSortFromUI(column, multiSort);
    }
  }

  protected void handleUiColumnMoved(JsonEvent event, JsonResponse res) {
    IColumn column = extractColumn(event.getData());
    int viewIndex = JsonObjectUtility.getInt(event.getData(), "index");

    // Create column list with expected order
    List<IColumn<?>> columns = getColumns();
    columns.remove(column);
    columns.add(viewIndex, column);
    addTableEventFilterCondition(TableEvent.TYPE_COLUMN_ORDER_CHANGED).setColumns(columns);
    getModel().getUIFacade().fireColumnMovedFromUI(column, viewIndex);
  }

  protected void handleUiColumnResized(JsonEvent event, JsonResponse res) {
    IColumn column = extractColumn(event.getData());
    int width = JsonObjectUtility.getInt(event.getData(), "width");

    getModel().getUIFacade().setColumnWidthFromUI(column, width);
  }

  protected void handleUiRowAction(JsonEvent event, JsonResponse res) {
    ITableRow tableRow = extractTableRow(event.getData());
    IColumn column = extractColumn(event.getData());
    getModel().getUIFacade().setContextColumnFromUI(column);
    getModel().getUIFacade().fireRowActionFromUI(tableRow);
  }

  protected void handleUiHyperlinkAction(JsonEvent event, JsonResponse res) {
    ITableRow row = extractTableRow(event.getData());
    IColumn column = extractColumn(event.getData());
    URL url = null;
    try {
      url = new URL("http://local/" + JsonObjectUtility.getString(event.getData(), "hyperlink"));
    }
    catch (MalformedURLException e) {
      //TODO [15.0] imo change in scout and only send the path, not the complete url, also ignore the column! hyperlinks are per row only and use a path only [a href='path']text[/a]
      e.printStackTrace();
    }
    if (row != null && column != null && url != null) {
      getModel().getUIFacade().fireHyperlinkActionFromUI(row, column, url);
    }
  }

  protected JSONObject tableRowToJson(ITableRow row) {
    JSONArray jsonCells = new JSONArray();
    for (IColumn<?> column : getColumns()) {
      jsonCells.put(cellToJson(row.getCell(column), row, column));
    }
    JSONObject jsonRow = new JSONObject();
    putProperty(jsonRow, "id", getOrCreatedRowId(row));
    putProperty(jsonRow, "cells", jsonCells);
    putProperty(jsonRow, "checked", row.isChecked());
    return jsonRow;
  }

  protected Object cellToJson(ICell cell, ITableRow row, IColumn column) {
    // Prepare cell value
    Object cellValue = cell.getValue();
    if (column instanceof IDateColumn) {
      Date date = (Date) cell.getValue();
      if (date != null) {
        IDateColumn dateColumn = (IDateColumn) column;
        cellValue = new JsonDate(date).asJsonString(false, dateColumn.isHasDate(), dateColumn.isHasTime());
      }
    }
    return new JsonCell(getJsonSession(), cell, cellValue).toJsonOrString();
  }

  protected JSONArray columnsToJson(Collection<IColumn<?>> columns) {
    JSONArray jsonColumns = new JSONArray();
    for (IColumn<?> column : columns) {
      jsonColumns.put(columnToJson(column));
    }
    return jsonColumns;
  }

  public List<IColumn<?>> getColumns() {
    return getModel().getColumnSet().getVisibleColumns();
  }

  protected JSONObject columnToJson(IColumn column) {
    try {
      JSONObject json = new JSONObject();
      json.put("id", column.getColumnId());
      json.put("text", getJsonSession().getCustomHtmlRenderer().convert(column.getHeaderCell().getText(), true));
      json.put("type", computeColumnType(column));
      json.put(IColumn.PROP_WIDTH, column.getWidth());
      json.put("summary", column.isSummary());
      json.put(IColumn.PROP_HORIZONTAL_ALIGNMENT, column.getHorizontalAlignment());
      if (column.isSortActive() && column.isSortExplicit()) {
        json.put("sortActive", true);
        json.put("sortAscending", column.isSortAscending());
        json.put("sortIndex", column.getSortIndex());
      }
      if (column instanceof ICustomColumn) {
        json.put("custom", true);
      }
      if (column instanceof INumberColumn<?>) {
        // Use localized pattern which contains the relevant chars for the current locale using DecimalFormatSymbols
        json.put("format", ((INumberColumn) column).getFormat().toLocalizedPattern());
      }
      else if (column instanceof IDateColumn) {
        // FIXME CGU: update IDateColumnInterface
        // getDateFormat uses LocaleThreadLocal. IMHO getDateFormat should not perform any logic because it just a getter-> refactor. same on AbstractDateField
        // Alternative would be to use a clientJob or set localethreadlocal in ui thread as well, as done in rap
        LocaleThreadLocal.set(getJsonSession().getClientSession().getLocale());
        try {
          Method method = AbstractDateColumn.class.getDeclaredMethod("getDateFormat");
          method.setAccessible(true);
          SimpleDateFormat dateFormat = (SimpleDateFormat) method.invoke(column);
          json.put("format", dateFormat.toPattern()); //Don't use toLocalizedPattern, it translates the chars ('d' to 't' for german).
        }
        finally {
          LocaleThreadLocal.set(null);
        }
      }
      // FIXME CGU: complete
      JsonObjectUtility.filterDefaultValues(json, "TableColumn");
      return json;
    }
    catch (JSONException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new JsonException(e.getMessage(), e);
    }
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

  protected String getOrCreatedRowId(ITableRow row) {
    if (row == null) {
      return null;
    }

    String id = m_tableRowIds.get(row);
    if (id == null) {
      id = getJsonSession().createUniqueIdFor(null);
      m_tableRows.put(id, row);
      m_tableRowIds.put(row, id);
    }
    return id;
  }

  protected JSONArray rowIdsToJson(Collection<ITableRow> modelRows) {
    JSONArray jsonRowIds = new JSONArray();
    for (ITableRow row : modelRows) {
      jsonRowIds.put(getOrCreatedRowId(row));
    }
    return jsonRowIds;
  }

  public List<ITableRow> extractTableRows(JSONObject json) {
    return jsonToTableRows(JsonObjectUtility.getJSONArray(json, PROP_ROW_IDS));
  }

  public ITableRow extractTableRow(JSONObject json) {
    return getTableRowForRowId(JsonObjectUtility.getString(json, PROP_ROW_ID));
  }

  public IColumn extractColumn(JSONObject json) {
    String columnId = JsonObjectUtility.getString(json, PROP_COLUMN_ID);
    return getModel().getColumnSet().getColumnById(columnId);
  }

  protected JSONArray columnIdsToJson(Collection<IColumn<?>> columns) {
    JSONArray jsonColumnIds = new JSONArray();
    for (IColumn column : columns) {
      jsonColumnIds.put(column.getColumnId());
    }
    return jsonColumnIds;
  }

  protected List<ITableRow> jsonToTableRows(JSONArray rowIds) {
    List<ITableRow> rows = new ArrayList<>(rowIds.length());
    for (int i = 0; i < rowIds.length(); i++) {
      rows.add(m_tableRows.get(JsonObjectUtility.get(rowIds, i)));
    }
    return rows;
  }

  protected CheckedInfo jsonToCheckeInfo(JSONObject data) {
    JSONArray jsonRows = data.optJSONArray("rows");
    CheckedInfo checkInfo = new CheckedInfo();
    for (int i = 0; i < jsonRows.length(); i++) {
      JSONObject jsonObject = jsonRows.optJSONObject(i);
      ITableRow row = m_tableRows.get(jsonObject.optString("rowId"));
      checkInfo.m_allRows.add(row);
      if (jsonObject.optBoolean("checked")) {
        checkInfo.m_checkedRows.add(row);
      }
      else {
        checkInfo.m_uncheckedRows.add(row);
      }
    }
    return checkInfo;

  }

  protected ITableRow getTableRowForRowId(String rowId) {
    ITableRow row = m_tableRows.get(rowId);
    if (row == null) {
      throw new JsonException("No row found for id " + rowId);
    }
    return row;
  }

  protected void handleModelTableEventBatch(List<? extends TableEvent> events) {
    for (TableEvent event : events) {
      handleModelTableEvent(event);
    }
  }

  protected void handleModelTableEvent(TableEvent event) {
    /* FIXME CGU we need to coalesce the events during the same request (something like AbstractTable.processEventBuffer). I don't think the developer should be responsible for this (using setTableChanging(true))
     * Example: The developer calls addRow several times for the same table -> must generate only one json event -> reduces data and improves redrawing performance on client
     * Another usecase: If a table row gets edited the developer typically reloads the whole table -> transmits every row again -> actually only the difference needs to be sent. Not sure if this is easy to solve
     *
     * Detected by A.WE: when the Firma-Node is loaded the following happens:
     * - event rowsInserted (3 rows)
     * - event allRowsDeleted
     * - event rowsInserted again (the same 3 rows)
     *
     * Only one rowsInserted event should be sent. Must analyze what happens in the model.
     */
    event = m_tableEventFilter.filter(event);
    if (event == null) {
      return;
    }
    switch (event.getType()) {
      case TableEvent.TYPE_ROWS_INSERTED:
        handleModelRowsInserted(event);
        break;
      case TableEvent.TYPE_ROWS_UPDATED:
        handleModelRowsUpdated(event.getRows());
        break;
      case TableEvent.TYPE_ROWS_DELETED:
        handleModelRowsDeleted(event.getRows());
        break;
      case TableEvent.TYPE_ALL_ROWS_DELETED:
        handleModelAllRowsDeleted(event.getRows());
        break;
      case TableEvent.TYPE_ROWS_SELECTED:
        handleModelRowsSelected(event.getRows());
        break;
      case TableEvent.TYPE_ROW_ORDER_CHANGED:
        handleModelRowOrderChanged(event.getRows());
        break;
      case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED:
        handleModelColumnStructureChanged();
        break;
      case TableEvent.TYPE_COLUMN_ORDER_CHANGED:
        handleModelColumnOrderChanged();
        break;
      case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
        handleModelColumnHeadersUpdated(event.getColumns());
        break;
      case TableEvent.TYPE_ROWS_CHECKED:
        handleModelRowsChecked(event.getRows());
        break;
      default:
        // NOP
    }
  }

  protected void handleModelRowsInserted(TableEvent event) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : event.getRows()) {
      JSONObject jsonRow = tableRowToJson(row);
      jsonRows.put(jsonRow);
    }
    putProperty(jsonEvent, ROWS, jsonRows);
    addActionEvent("rowsInserted", jsonEvent);
  }

  protected void handleModelRowsDeleted(Collection<ITableRow> modelRows) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROW_IDS, rowIdsToJson(modelRows));
    JSONArray jsonRowIds = new JSONArray();
    for (ITableRow row : modelRows) {
      String rowId = m_tableRowIds.get(row);
      jsonRowIds.put(rowId);
      m_tableRowIds.remove(row);
      m_tableRows.remove(rowId);
    }
    addActionEvent(EVENT_ROWS_DELETED, jsonEvent);
  }

  protected void handleModelAllRowsDeleted(Collection<ITableRow> modelRows) {
    addActionEvent(EVENT_ALL_ROWS_DELETED, new JSONObject());
  }

  protected void handleModelRowsSelected(Collection<ITableRow> modelRows) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROW_IDS, rowIdsToJson(modelRows));
    addActionEvent(EVENT_ROWS_SELECTED, jsonEvent);
  }

  protected void handleModelRowsUpdated(Collection<ITableRow> modelRows) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : modelRows) {
      JSONObject jsonRow = tableRowToJson(row);
      jsonRows.put(jsonRow);
    }
    putProperty(jsonEvent, ROWS, jsonRows);
    addActionEvent(EVENT_ROWS_UPDATED, jsonEvent);
  }

  protected void handleModelRowsChecked(Collection<ITableRow> modelRows) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : modelRows) {
      JSONObject jsonRow = new JSONObject();
      putProperty(jsonRow, "id", getOrCreatedRowId(row));
      putProperty(jsonRow, "checked", row.isChecked());
      jsonRows.put(jsonRow);
    }
    putProperty(jsonEvent, ROWS, jsonRows);
    addActionEvent(EVENT_ROWS_CHECKED, jsonEvent);
  }

  protected void handleModelRowOrderChanged(Collection<ITableRow> modelRows) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROW_IDS, rowIdsToJson(modelRows));
    JSONArray jsonRowIds = new JSONArray();
    for (ITableRow row : modelRows) {
      String rowId = m_tableRowIds.get(row);
      jsonRowIds.put(rowId);
    }
    addActionEvent("rowOrderChanged", jsonEvent);
  }

  protected void handleModelColumnStructureChanged() {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COLUMNS, columnsToJson(getColumns()));
    addActionEvent("columnStructureChanged", jsonEvent);
  }

  protected void handleModelColumnOrderChanged() {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COLUMN_IDS, columnIdsToJson(getColumns()));
    addActionEvent(EVENT_COLUMN_ORDER_CHANGED, jsonEvent);
  }

  protected void handleModelColumnHeadersUpdated(Collection<IColumn<?>> columns) {
    JSONObject jsonEvent = new JSONObject();
    Collection<IColumn<?>> visibleColumns = filterVisibleColumns(columns);
    if (visibleColumns.size() > 0) {
      putProperty(jsonEvent, PROP_COLUMNS, columnsToJson(visibleColumns));
      addActionEvent(EVENT_COLUMN_HEADERS_UPDATED, jsonEvent);
    }
  }

  protected static Collection<IColumn<?>> filterVisibleColumns(Collection<IColumn<?>> columns) {
    List<IColumn<?>> visibleColumns = new LinkedList<IColumn<?>>();
    for (IColumn<?> column : columns) {
      if (column.isVisible()) {
        visibleColumns.add(column);
      }
    }
    return visibleColumns;
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  private class P_TableListener implements TableListener {
    @Override
    public void tableChanged(final TableEvent e) {
      handleModelTableEvent(e);
    }

    @Override
    public void tableChangedBatch(List<? extends TableEvent> events) {
      handleModelTableEventBatch(events);
    }
  }

  private class CheckedInfo {
    private ArrayList<ITableRow> m_allRows = new ArrayList<ITableRow>();
    private ArrayList<ITableRow> m_checkedRows = new ArrayList<ITableRow>();
    private ArrayList<ITableRow> m_uncheckedRows = new ArrayList<ITableRow>();
  }

  @Override
  public void cleanUpEventFilters() {
    super.cleanUpEventFilters();
    m_tableEventFilter.removeAllConditions();
  }
}
