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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableTextUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TextTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.security.CopyToClipboardPermission;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.basic.cell.ICellValueReader;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTableUserFilter;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTable<TABLE extends ITable> extends AbstractJsonPropertyObserver<TABLE> implements IJsonContextMenuOwner, IBinaryResourceConsumer {
  private static final Logger LOG = LoggerFactory.getLogger(JsonTable.class);

  public static final String EVENT_ROW_CLICKED = "rowClicked";
  public static final String EVENT_ROW_ACTION = "rowAction";
  public static final String EVENT_ROWS_SELECTED = "rowsSelected";
  public static final String EVENT_ROWS_INSERTED = "rowsInserted";
  public static final String EVENT_ROWS_UPDATED = "rowsUpdated";
  public static final String EVENT_ROWS_DELETED = "rowsDeleted";
  public static final String EVENT_ALL_ROWS_DELETED = "allRowsDeleted";
  public static final String EVENT_ROWS_SORTED = "rowsSorted";
  public static final String EVENT_SORT_ROWS = "sortRows";
  public static final String EVENT_ROWS_GROUPED = "rowsGrouped";
  public static final String EVENT_GROUP_ROWS = "groupRows";
  public static final String EVENT_COLUMN_AGGR_FUNC_CHANGED = "aggregationFunctionChanged";
  public static final String EVENT_COLUMN_MOVED = "columnMoved";
  public static final String EVENT_COLUMN_RESIZED = "columnResized";
  public static final String EVENT_RELOAD = "reload";
  public static final String EVENT_RESET_COLUMNS = "resetColumns";
  public static final String EVENT_ROWS_CHECKED = "rowsChecked";
  public static final String EVENT_COLUMN_ORDER_CHANGED = "columnOrderChanged";
  public static final String EVENT_COLUMN_STRUCTURE_CHANGED = "columnStructureChanged";
  public static final String EVENT_COLUMN_HEADERS_UPDATED = "columnHeadersUpdated";
  public static final String EVENT_COLUMN_BACKGROUND_EFFECT_CHANGED = "columnBackgroundEffectChanged";
  public static final String EVENT_REQUEST_FOCUS_IN_CELL = "requestFocusInCell";
  public static final String EVENT_START_CELL_EDIT = "startCellEdit";
  public static final String EVENT_END_CELL_EDIT = "endCellEdit";
  public static final String EVENT_PREPARE_CELL_EDIT = "prepareCellEdit";
  public static final String EVENT_COMPLETE_CELL_EDIT = "completeCellEdit";
  public static final String EVENT_CANCEL_CELL_EDIT = "cancelCellEdit";
  public static final String EVENT_REQUEST_FOCUS = "requestFocus";
  public static final String EVENT_SCROLL_TO_SELECTION = "scrollToSelection";
  public static final String EVENT_EXPORT_TO_CLIPBOARD = "exportToClipboard";
  public static final String EVENT_ADD_FILTER = "addFilter";
  public static final String EVENT_REMOVE_FILTER = "removeFilter";
  public static final String EVENT_ROWS_FILTERED = "rowsFiltered";

  public static final String PROP_ROWS = "rows";
  public static final String PROP_ROW_IDS = "rowIds";
  public static final String PROP_ROW_ID = "rowId";
  public static final String PROP_COLUMN_ID = "columnId";
  public static final String PROP_COLUMN_IDS = "columnIds";
  public static final String PROP_COLUMNS = "columns";
  public static final String PROP_SELECTED_ROWS = "selectedRows";
  public static final String PROP_FILTERS = "filters";
  public static final String PROP_HAS_RELOAD_HANDLER = "hasReloadHandler";

  private TableListener m_tableListener;
  private final Map<String, ITableRow> m_tableRows;
  private final Map<ITableRow, String> m_tableRowIds;
  private final Map<String, IColumn> m_columns;
  private final TableEventFilter m_tableEventFilter;
  private final Map<IColumn, JsonColumn> m_jsonColumns;
  private final AbstractEventBuffer<TableEvent> m_eventBuffer;

  public JsonTable(TABLE model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_tableRows = new HashMap<>();
    m_tableRowIds = new HashMap<>();
    m_columns = new HashMap<>();
    m_tableEventFilter = new TableEventFilter(this);
    m_jsonColumns = new HashMap<IColumn, JsonColumn>();
    m_eventBuffer = model.createEventBuffer();
  }

  @Override
  public String getObjectType() {
    return "Table";
  }

  @Override
  protected void initJsonProperties(TABLE model) {
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
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
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_ROW_ICON_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isRowIconVisible();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_HEADER_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHeaderVisible();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_HEADER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHeaderEnabled();
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
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_TABLE_STATUS, model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getTableStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonStatus.toJson((IStatus) value);
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITable>(ITable.PROP_TABLE_CONTROLS, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<ITableControl>()).build();
      }

      @Override
      protected List<ITableControl> modelValue() {
        return getModel().getTableControls();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_DROP_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDropType();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_DROP_MAXIMUM_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getDropMaximumSize();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(PROP_HAS_RELOAD_HANDLER, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getReloadHandler() != null;
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_SORT_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSortEnabled();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_UI_SORT_POSSIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isUiSortPossible();
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITable>(ITable.PROP_KEY_STROKES, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<IAction>()).build();
      }

      @Override
      protected List<IKeyStroke> modelValue() {
        return getModel().getKeyStrokes();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getContextMenu(), new DisplayableActionFilter<IMenu>());
    attachColumns();
  }

  protected void attachColumns() {
    int offset = 0;
    for (IColumn<?> column : getModel().getColumns()) {
      if (!column.isVisible()) {
        // since we don't send row data for invisible columns, we have to adjust the column index
        offset += 1;
        continue;
      }
      String id = getUiSession().createUniqueId();
      JsonColumn jsonColumn = (JsonColumn) MainJsonObjectFactory.get().createJsonObject(column);
      jsonColumn.setUiSession(getUiSession());
      jsonColumn.setId(id);
      jsonColumn.setColumnIndexOffset(offset);
      m_jsonColumns.put(column, jsonColumn);
      m_columns.put(id, column);
    }
  }

  /**
   * Removes all column mappings without querying the model. Can be useful when the model is already updated (e.g. while
   * handling the "column structure changed" event).
   */
  protected void disposeAllColumns() {
    m_jsonColumns.clear();
    m_columns.clear();
  }

  protected void disposeColumn(IColumn<?> column) {
    JsonColumn jsonColumn = m_jsonColumns.get(column);
    m_jsonColumns.remove(column);
    if (jsonColumn != null) {
      m_columns.remove(jsonColumn.getId());
    }
  }

  protected void disposeColumns(Collection<IColumn<?>> columns) {
    for (IColumn<?> column : columns) {
      disposeColumn(column);
    }
  }

  /**
   * Removes all row mappings without querying the model.
   */
  protected void disposeAllRows() {
    m_tableRowIds.clear();
    m_tableRows.clear();
  }

  protected void disposeRow(ITableRow row) {
    String rowId = m_tableRowIds.get(row);
    m_tableRowIds.remove(row);
    m_tableRows.remove(rowId);
  }

  protected void disposeRows(Collection<ITableRow> rows) {
    for (ITableRow row : rows) {
      disposeRow(row);
    }
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    disposeAllColumns();
    disposeAllRows();
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
    putProperty(json, PROP_COLUMNS, columnsToJson(getColumnsInViewOrder()));
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : getModel().getRows()) {
      if (!isRowAccepted(row)) {
        continue;
      }
      JSONObject jsonRow = tableRowToJson(row);
      jsonRows.put(jsonRow);
    }
    putProperty(json, PROP_ROWS, jsonRows);
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      json.put(PROP_MENUS, jsonContextMenu.childActionsToJson());
    }
    putProperty(json, PROP_SELECTED_ROWS, rowIdsToJson(getModel().getSelectedRows()));
    if (getModel().getUserFilterManager() != null) {
      putProperty(json, PROP_FILTERS, filtersToJson(getModel().getUserFilterManager().getFilters()));
    }
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_ROW_CLICKED.equals(event.getType())) {
      handleUiRowClicked(event);
    }
    else if (EVENT_ROW_ACTION.equals(event.getType())) {
      handleUiRowAction(event);
    }
    else if (EVENT_ROWS_SELECTED.equals(event.getType())) {
      handleUiRowsSelected(event);
    }
    else if (EVENT_RELOAD.equals(event.getType())) {
      handleUiReload(event);
    }
    else if (EVENT_RESET_COLUMNS.equals(event.getType())) {
      handleUiResetColumns(event);
    }
    else if (EVENT_SORT_ROWS.equals(event.getType())) {
      handleUiSortRows(event);
    }
    else if (EVENT_ROWS_SORTED.equals(event.getType())) {
      handleUiRowsSorted(event);
    }
    else if (EVENT_GROUP_ROWS.equals(event.getType())) {
      handleUiGroupRows(event);
    }
    else if (EVENT_ROWS_GROUPED.equals(event.getType())) {
      handleUiRowsGrouped(event);
    }
    else if (EVENT_COLUMN_MOVED.equals(event.getType())) {
      handleUiColumnMoved(event);
    }
    else if (EVENT_COLUMN_RESIZED.equals(event.getType())) {
      handleUiColumnResized(event);
    }
    else if (JsonEventType.APP_LINK_ACTION.matches(event.getType())) {
      handleUiAppLinkAction(event);
    }
    else if (EVENT_ROWS_CHECKED.equals(event.getType())) {
      handleUiRowChecked(event);
    }
    else if (EVENT_PREPARE_CELL_EDIT.equals(event.getType())) {
      handleUiPrepareCellEdit(event);
    }
    else if (EVENT_COMPLETE_CELL_EDIT.equals(event.getType())) {
      handleUiCompleteCellEdit(event);
    }
    else if (EVENT_CANCEL_CELL_EDIT.equals(event.getType())) {
      handleUiCancelCellEdit(event);
    }
    else if (EVENT_EXPORT_TO_CLIPBOARD.equals(event.getType())) {
      handleUiExportToClipboard(event);
    }
    else if (EVENT_ADD_FILTER.equals(event.getType())) {
      handleUiAddFilter(event);
    }
    else if (EVENT_REMOVE_FILTER.equals(event.getType())) {
      handleUiRemoveFilter(event);
    }
    else if (EVENT_ROWS_FILTERED.equals(event.getType())) {
      handleUiRowsFiltered(event);
    }
    else if (EVENT_COLUMN_AGGR_FUNC_CHANGED.equals(event.getType())) {
      handleColumnAggregationFunctionChanged(event);
    }
    else if (EVENT_COLUMN_BACKGROUND_EFFECT_CHANGED.equals(event.getType())) {
      handleColumnBackgroundEffectChanged(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiRowClicked(JsonEvent event) {
    ITableRow tableRow = extractTableRow(event.getData());
    if (tableRow == null) {
      LOG.warn("Requested table-row doesn't exist anymore -> skip rowClicked event");
      return;
    }
    IColumn column = extractColumn(event.getData());
    ArrayList<ITableRow> rows = new ArrayList<ITableRow>();
    rows.add(tableRow);
    getModel().getUIFacade().setContextColumnFromUI(column);
    MouseButton mouseButton = extractMouseButton(event.getData());
    getModel().getUIFacade().fireRowClickFromUI(tableRow, mouseButton);
  }

  protected MouseButton extractMouseButton(JSONObject json) {
    int mouseButton = json.getInt("mouseButton");
    switch (mouseButton) {
      case 1:
        return MouseButton.Left;
      case 3:
        return MouseButton.Right;
      default:
        return MouseButton.Unknown;
    }
  }

  protected void handleUiRowChecked(JsonEvent event) {
    CheckedInfo tableRowsChecked = jsonToCheckedInfo(event.getData());
    addTableEventFilterCondition(TableEvent.TYPE_ROWS_CHECKED).setRows(tableRowsChecked.getAllRows());

    if (tableRowsChecked.getCheckedRows().size() > 0) {
      getModel().getUIFacade().setCheckedRowsFromUI(tableRowsChecked.getCheckedRows(), true);
    }
    if (tableRowsChecked.getUncheckedRows().size() > 0) {
      getModel().getUIFacade().setCheckedRowsFromUI(tableRowsChecked.getUncheckedRows(), false);
    }
  }

  protected void handleUiRowsSelected(JsonEvent event) {
    List<ITableRow> tableRows = extractTableRows(event.getData());
    addTableEventFilterCondition(TableEvent.TYPE_ROWS_SELECTED).setRows(tableRows);
    getModel().getUIFacade().setSelectedRowsFromUI(tableRows);
  }

  protected void handleUiReload(JsonEvent event) {
    getModel().getUIFacade().fireTableReloadFromUI();
  }

  protected void handleUiResetColumns(JsonEvent event) {
    getModel().getUIFacade().fireTableResetFromUI();
  }

  /**
   * Makes sure that no rowOrderChanged event is returned to the client after sorting because the sorting already
   * happened on client as well.
   */
  protected void handleUiRowsSorted(JsonEvent event) {
    addTableEventFilterCondition(TableEvent.TYPE_ROW_ORDER_CHANGED);
    fireSortRowsFromUi(event.getData());
  }

  protected void handleUiSortRows(JsonEvent event) {
    fireSortRowsFromUi(event.getData());
  }

  protected void fireSortRowsFromUi(JSONObject data) {
    IColumn column = extractColumn(data);
    boolean sortingRemoved = data.optBoolean("sortingRemoved");

    // FIXME cgu: add filter for HEADER_UPDATE event with json data of column (execDecorateHeaderCell is called which may change other header properties (text etc)
    if (sortingRemoved) {
      getModel().getUIFacade().fireSortColumnRemovedFromUI(column);
    }
    else {
      boolean multiSort = data.optBoolean("multiSort");
      boolean sortAscending = data.getBoolean("sortAscending");
      getModel().getUIFacade().fireHeaderSortFromUI(column, multiSort, sortAscending);
    }
  }

  protected void handleUiRowsGrouped(JsonEvent event) {
    addTableEventFilterCondition(TableEvent.TYPE_ROW_ORDER_CHANGED);
    fireGroupRowsFromUi(event.getData());
  }

  protected void handleUiGroupRows(JsonEvent event) {
    fireGroupRowsFromUi(event.getData());
  }

  protected void fireGroupRowsFromUi(JSONObject data) {

    IColumn column = extractColumn(data);
    boolean groupingRemoved = data.optBoolean("groupingRemoved");

    if (groupingRemoved) {
      getModel().getUIFacade().fireGroupColumnRemovedFromUI(column);
    }
    else {
      boolean multiGroup = data.optBoolean("multiGroup");
      boolean groupAscending = data.getBoolean("groupAscending");
      getModel().getUIFacade().fireHeaderGroupFromUI(column, multiGroup, groupAscending);
    }

  }

  protected void handleColumnAggregationFunctionChanged(JsonEvent event) {
    addTableEventFilterCondition(TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED);
    IColumn column = extractColumn(event.getData());
    Assertions.assertInstance(column, INumberColumn.class, "Aggregation can only be specified on numeric columns");
    getModel().getUIFacade().fireAggregationFunctionChanged((INumberColumn<?>) column, event.getData().getString("aggregationFunction"));
  }

  protected void handleColumnBackgroundEffectChanged(JsonEvent event) {
    addTableEventFilterCondition(TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED);
    IColumn column = extractColumn(event.getData());
    Assertions.assertInstance(column, INumberColumn.class, "BackgroundEffect can only be specified on numeric columns");
    getModel().getUIFacade().setColumnBackgroundEffect((INumberColumn<?>) column, event.getData().optString("backgroundEffect"));
  }

  protected void handleUiColumnMoved(JsonEvent event) {
    IColumn column = extractColumn(event.getData());
    int viewIndex = event.getData().getInt("index");

    // Create column list with expected order
    List<IColumn<?>> columns = getColumnsInViewOrder();
    columns.remove(column);
    columns.add(viewIndex, column);
    addTableEventFilterCondition(TableEvent.TYPE_COLUMN_ORDER_CHANGED).setColumns(columns);
    getModel().getUIFacade().fireColumnMovedFromUI(column, viewIndex);
  }

  protected void handleUiColumnResized(JsonEvent event) {
    IColumn column = extractColumn(event.getData());
    int width = event.getData().getInt("width");

    getModel().getUIFacade().setColumnWidthFromUI(column, width);
  }

  protected void handleUiRowAction(JsonEvent event) {
    ITableRow tableRow = extractTableRow(event.getData());
    IColumn column = extractColumn(event.getData());
    getModel().getUIFacade().setContextColumnFromUI(column);
    getModel().getUIFacade().fireRowActionFromUI(tableRow);
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    IColumn column = extractColumn(event.getData());
    String ref = event.getData().getString("ref");
    if (column != null) {
      getModel().getUIFacade().setContextColumnFromUI(column);
    }
    getModel().getUIFacade().fireAppLinkActionFromUI(ref);
  }

  protected void handleUiPrepareCellEdit(JsonEvent event) {
    ITableRow row = extractTableRow(event.getData());
    if (row == null) {
      LOG.warn("Requested table-row doesn't exist anymore. Skip prepareCellEdit event");
      return;
    }
    IColumn column = extractColumn(event.getData());
    IFormField field = getModel().getUIFacade().prepareCellEditFromUI(row, column);
    if (field == null) {
      throw new IllegalStateException("PrepareCellEditFromUi returned null for " + row + " and " + column);
    }

    IJsonAdapter<?> jsonField = attachAdapter(field);
    LOG.debug("Created new field adapter for cell editing. Adapter: {}", jsonField);
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    putProperty(json, "columnId", event.getData().getString(PROP_COLUMN_ID));
    putProperty(json, "rowId", event.getData().getString(PROP_ROW_ID));
    putProperty(json, "fieldId", jsonField.getId());
    addActionEvent(EVENT_START_CELL_EDIT, json);
  }

  protected void handleUiCompleteCellEdit(JsonEvent event) {
    String fieldId = event.getData().getString("fieldId");
    getModel().getUIFacade().completeCellEditFromUI();
    endCellEdit(fieldId);
  }

  protected void handleUiCancelCellEdit(JsonEvent event) {
    String fieldId = event.getData().getString("fieldId");
    getModel().getUIFacade().cancelCellEditFromUI();
    endCellEdit(fieldId);
  }

  protected void endCellEdit(String fieldId) {
    IJsonAdapter<?> jsonField = getUiSession().getJsonAdapter(fieldId);
    if (jsonField == null) {
      throw new IllegalStateException("No field adapter found for id " + fieldId);
    }

    // Confirm end cell edit so that gui can dispose the adapter.
    // It is not possible to dispose the adapter on the gui before sending complete or cancelCellEdit, because the field may send property change events back)
    // It would be possible if we added a filter mechanism so that events for disposed adapters won't be sent to client
    // TODO [5.2] cgu: maybe optimize by adding a filter for disposed adapters
    JSONObject json = new JSONObject();
    putProperty(json, "fieldId", jsonField.getId());
    addActionEvent(EVENT_END_CELL_EDIT, json);

    //FIXME cgu: feld merken, revert bei toJson für page reload
    jsonField.dispose();
  }

  protected void handleUiExportToClipboard(JsonEvent event) {
    if (!ACCESS.check(new CopyToClipboardPermission())) {
      return;
    }

    TransferObject scoutTransferable = getModel().getUIFacade().fireRowsCopyRequestFromUI();
    if (scoutTransferable != null && scoutTransferable instanceof TextTransferObject) {
      try {
        BEANS.get(IClipboardService.class).setContents(scoutTransferable);
      }
      catch (RuntimeException e) {
        throw new UiException("Unable to copy to clipboard.", e);
      }
    }
  }

  protected void handleUiAddFilter(JsonEvent event) {
    JSONObject data = event.getData();
    IUserFilterState filter = createFilterState(data);
    TableEventFilterCondition condition = addTableEventFilterCondition(TableEvent.TYPE_USER_FILTER_ADDED);
    condition.setUserFilter(filter);
    condition.checkUserFilter();
    getModel().getUIFacade().fireFilterAddedFromUI(filter);
  }

  @SuppressWarnings("unchecked")
  protected IUserFilterState createFilterState(JSONObject data) {
    String filterType = data.getString("filterType");
    if ("column".equals(filterType)) {
      IColumn column = extractColumn(data);
      // filter table
      JSONArray jsonSelectedValues = data.getJSONArray("selectedValues");
      Set<Object> selectedValues = new HashSet<Object>();
      for (int i = 0; i < jsonSelectedValues.length(); i++) {
        if (jsonSelectedValues.isNull(i)) {
          selectedValues.add(null);
        }
        else {
          selectedValues.add(jsonSelectedValues.get(i));
        }
      }
      // filter fields
      // FIXME AWE: (filter) implement other filter types
      String freeText = data.getString("freeText");

      ColumnUserFilterState filter = new ColumnUserFilterState(column);
      filter.setSelectedValues(selectedValues);
      filter.setFreeText(freeText);
      return filter;
    }
    else if ("text".equals(filterType)) {
      String text = data.getString("text");
      TableTextUserFilterState filter = new TableTextUserFilterState();
      filter.setText(text);
      return filter;
    }
    return null;
  }

  protected IUserFilterState getFilterState(JSONObject data) {
    String type = data.getString("filterType");
    if ("column".equals(type)) {
      IColumn column = extractColumn(data);
      return getModel().getUserFilterManager().getFilter(column);
    }

    return getModel().getUserFilterManager().getFilter(type);
  }

  protected void handleUiRemoveFilter(JsonEvent event) {
    IUserFilterState filter = getFilterState(event.getData());
    TableEventFilterCondition condition = addTableEventFilterCondition(TableEvent.TYPE_USER_FILTER_REMOVED);
    condition.setUserFilter(filter);
    condition.checkUserFilter();
    getModel().getUIFacade().fireFilterRemovedFromUI(filter);
  }

  protected void handleUiRowsFiltered(JsonEvent event) {
    if (event.getData().optBoolean("remove")) {
      getModel().getUIFacade().removeFilteredRowsFromUI();
    }
    else {
      List<ITableRow> tableRows = extractTableRows(event.getData());
      getModel().getUIFacade().setFilteredRowsFromUI(tableRows);
    }
  }

  protected JSONObject tableRowToJson(ITableRow row) {
    JSONArray jsonCells = new JSONArray();
    for (IColumn<?> column : getModel().getColumnSet().getColumns()) {
      // Don't use getColumnsInViewOrder because the cells of the rows have to be returned in the model order. The ui does a lookup using column.index.
      if (column.isVisible()) {
        jsonCells.put(cellToJson(row, column));
      }
    }
    JSONObject jsonRow = JsonObjectUtility.newOrderedJSONObject();
    putProperty(jsonRow, "id", getOrCreatedRowId(row));
    putProperty(jsonRow, "cells", jsonCells);
    putProperty(jsonRow, "checked", row.isChecked());
    putProperty(jsonRow, "enabled", row.isEnabled());
    putProperty(jsonRow, "iconId", BinaryResourceUrlUtility.createIconUrl(row.getIconId()));
    putProperty(jsonRow, "cssClass", row.getCssClass());
    JsonObjectUtility.filterDefaultValues(jsonRow, "TableRow");
    return jsonRow;
  }

  protected Object cellToJson(final ITableRow row, final IColumn column) {
    ICell cell = row.getCell(column);
    JsonColumn<?> jsonColumn = m_jsonColumns.get(column);
    ICellValueReader reader = new TableCellValueReader(jsonColumn, cell);
    return new JsonCell(cell, reader).toJsonOrString();
  }

  protected JSONArray columnsToJson(Collection<IColumn<?>> columns) {
    JSONArray jsonColumns = new JSONArray();
    for (IColumn<?> column : columns) {
      JsonColumn jsonColumn = m_jsonColumns.get(column);
      JSONObject json = jsonColumn.toJson();
      JsonObjectUtility.filterDefaultValues(json, jsonColumn.getObjectTypeVariant());
      jsonColumns.put(json);
    }
    return jsonColumns;
  }

  /**
   * @return columns in the right order to be presented to the user
   */
  protected List<IColumn<?>> getColumnsInViewOrder() {
    return getModel().getColumnSet().getVisibleColumns();
  }

  protected String getOrCreatedRowId(ITableRow row) {
    if (row == null) {
      return null;
    }

    String id = m_tableRowIds.get(row);
    if (id == null) {
      id = getUiSession().createUniqueId();
      m_tableRows.put(id, row);
      m_tableRowIds.put(row, id);
    }
    return id;
  }

  protected JSONArray rowIdsToJson(Collection<ITableRow> modelRows) {
    JSONArray jsonRowIds = new JSONArray();
    for (ITableRow row : modelRows) {
      if (!isRowAccepted(row)) {
        continue;
      }
      String rowId = getTableRowId(row);
      if (rowId == null) { // Ignore rows that are not yet sent to the UI
        continue;
      }
      jsonRowIds.put(rowId);
    }
    return jsonRowIds;
  }

  /**
   * Ignore deleted or filtered rows, because for the UI, they don't exist
   */
  protected boolean isRowAccepted(ITableRow row) {
    if (row.isStatusDeleted()) {
      return false;
    }
    if (!row.isFilterAccepted()) {
      // Accept if rejected by user row filter because gui is and should be aware of that row
      return row.isRejectedByUser();
    }
    return true;
  }

  protected List<ITableRow> extractTableRows(JSONObject json) {
    JSONArray rowIds = json.getJSONArray(PROP_ROW_IDS);
    List<ITableRow> rows = new ArrayList<>(rowIds.length());
    for (int i = 0; i < rowIds.length(); i++) {
      ITableRow tableRow = optTableRow((String) rowIds.get(i));
      if (tableRow != null) {
        rows.add(tableRow);
      }
    }
    return rows;
  }

  protected ITableRow extractTableRow(JSONObject json) {
    return optTableRow(json.getString(PROP_ROW_ID));
  }

  protected IColumn extractColumn(JSONObject json) {
    String columnId = json.optString(PROP_COLUMN_ID, null);
    if (columnId == null) {
      return null;
    }
    return getColumn(columnId);
  }

  protected JSONArray columnIdsToJson(Collection<IColumn<?>> columns) {
    JSONArray jsonColumnIds = new JSONArray();
    for (IColumn column : columns) {
      jsonColumnIds.put(getColumnId(column));
    }
    return jsonColumnIds;
  }

  protected IColumn getColumn(String columnId) {
    IColumn column = m_columns.get(columnId);
    if (column == null) {
      throw new UiException("No column found for id " + columnId);
    }
    return column;
  }

  public String getColumnId(IColumn column) {
    if (column == null) {
      return null;
    }
    JsonColumn jsonColumn = m_jsonColumns.get(column);
    if (jsonColumn == null) {
      return null;
    }
    return jsonColumn.getId();
  }

  /**
   * Returns a tableRow for the given rowId, or null when no row is found for the given rowId.
   */
  protected ITableRow optTableRow(String rowId) {
    return m_tableRows.get(rowId);
  }

  /**
   * Returns a tableRow for the given rowId.
   *
   * @throws UiException
   *           when no rowiss found for the given rowId
   */
  protected ITableRow getTableRow(String rowId) {
    ITableRow row = optTableRow(rowId);
    if (row == null) {
      throw new UiException("No table-row found for ID " + rowId);
    }
    return row;
  }

  public String getTableRowId(ITableRow row) {
    if (row == null) {
      return null;
    }
    return m_tableRowIds.get(row);
  }

  protected JSONArray filtersToJson(Collection<IUserFilterState> filters) {
    JSONArray jsonFilters = new JSONArray();
    for (IUserFilterState filter : filters) {
      JsonTableUserFilter jsonFilter = (JsonTableUserFilter) MainJsonObjectFactory.get().createJsonObject(filter);
      jsonFilter.setJsonTable(this);
      if (jsonFilter.isValid()) {
        jsonFilters.put(jsonFilter.toJson());
      }
      else {
        LOG.info("Filter is not valid, maybe column is invisible. {}", jsonFilter);
      }
    }
    return jsonFilters;
  }

  protected void handleModelTableEvent(TableEvent event) {
    event = m_tableEventFilter.filter(event);
    if (event == null) {
      return;
    }
    // Add event to buffer instead of handling it immediately. (This allows coalescing the events at JSON response level.)
    bufferModelEvent(event);
    registerAsBufferedEventsAdapter();
  }

  protected void bufferModelEvent(TableEvent event) {
    switch (event.getType()) {
      case TableEvent.TYPE_ROW_FILTER_CHANGED: {
        // Convert the "filter changed" event to a ROWS_DELETED and a ROWS_INSERTED event. This prevents sending unnecessary
        // data to the UI. We convert the event before adding it to the event buffer to allow coalescing on UI-level.
        // NOTE: This may lead to a temporary inconsistent situation, where row events exist in the buffer after the
        // row itself is deleted. This is because the row is not really deleted from the model. However, when processing
        // the buffered events, the "wrong" events will be ignored and everything is fixed again.
        List<ITableRow> rowsToInsert = new ArrayList<>();
        List<ITableRow> rowsToDelete = new ArrayList<>();
        for (ITableRow row : getModel().getRows()) {
          String existingRowId = getTableRowId(row);
          if (row.isFilterAccepted()) {
            if (existingRowId == null) {
              // Row is not filtered but JsonTable does not know it yet --> handle as insertion event
              rowsToInsert.add(row);
            }
          }
          else if (!row.isRejectedByUser()) {
            if (existingRowId != null) {
              // Row is filtered, but JsonTable has it in its list --> handle as deletion event
              rowsToDelete.add(row);
            }
          }
        }
        m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_ROWS_DELETED, rowsToDelete));
        m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_ROWS_INSERTED, rowsToInsert));
        break;
      }
      case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED: {
        m_eventBuffer.add(event);
        // If a column got visible it is necessary to resend all rows to inform the gui about the new cells of the new column
        m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_ALL_ROWS_DELETED));
        m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_ROWS_INSERTED, getModel().getRows()));
        break;
      }
      default: {
        m_eventBuffer.add(event);
      }
    }
  }

  @Override
  public void processBufferedEvents() {
    if (m_eventBuffer.isEmpty()) {
      return;
    }
    List<TableEvent> coalescedEvents = m_eventBuffer.consumeAndCoalesceEvents();
    for (TableEvent event : coalescedEvents) {
      processEvent(event);
    }
  }

  protected void processEvent(TableEvent event) {
    switch (event.getType()) {
      case TableEvent.TYPE_ROWS_INSERTED:
        handleModelRowsInserted(event.getRows());
        break;
      case TableEvent.TYPE_ROWS_UPDATED:
        handleModelRowsUpdated(event.getRows());
        break;
      case TableEvent.TYPE_ROWS_DELETED:
        handleModelRowsDeleted(event.getRows());
        break;
      case TableEvent.TYPE_ALL_ROWS_DELETED:
        handleModelAllRowsDeleted();
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
      case TableEvent.TYPE_ROW_FILTER_CHANGED:
        // See special handling in bufferModelEvent()
        throw new IllegalStateException("Unsupported event type: " + event);
      case TableEvent.TYPE_REQUEST_FOCUS:
        handleModelRequestFocus(event);
        break;
      case TableEvent.TYPE_SCROLL_TO_SELECTION:
        handleModelScrollToSelection(event);
        break;
      case TableEvent.TYPE_USER_FILTER_ADDED:
      case TableEvent.TYPE_USER_FILTER_REMOVED:
        handleModelUserFilterChanged(event);
        break;
      case TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED:
        handleModelColumnAggregationChanged(event);
        break;
      case TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED:
        handleModelColumnBackgroundEffectChanged(event);
        break;
      case TableEvent.TYPE_REQUEST_FOCUS_IN_CELL:
        handleModelRequestFocusInCell(event);
        break;
      default:
        // NOP
    }
  }

  protected void handleModelRowsInserted(Collection<ITableRow> modelRows) {
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : modelRows) {
      if (!isRowAccepted(row)) {
        continue;
      }
      JSONObject jsonRow = tableRowToJson(row);
      jsonRows.put(jsonRow);
    }
    if (jsonRows.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROWS, jsonRows);
    addActionEvent(EVENT_ROWS_INSERTED, jsonEvent);
  }

  protected void handleModelRowsUpdated(Collection<ITableRow> modelRows) {
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : modelRows) {
      if (!isRowAccepted(row)) {
        continue;
      }
      JSONObject jsonRow = tableRowToJson(row);
      jsonRows.put(jsonRow);
    }
    if (jsonRows.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROWS, jsonRows);
    addActionEvent(EVENT_ROWS_UPDATED, jsonEvent);
  }

  protected void handleModelRowsDeleted(Collection<ITableRow> modelRows) {
    if (modelRows.isEmpty()) {
      return;
    }
    if (getFilteredRowCount() == 0) {
      handleModelAllRowsDeleted();
      return;
    }
    JSONArray jsonRowIds = new JSONArray();
    for (ITableRow row : modelRows) {
      String rowId = getTableRowId(row);
      if (rowId == null) { // Ignore rows that are not yet sent to the UI (may happen when a filtered row is deleted)
        continue;
      }
      jsonRowIds.put(rowId);
      disposeRow(row);
    }
    if (jsonRowIds.length() == 0) {
      return;
    }
    JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    jsonEvent.put(PROP_ROW_IDS, jsonRowIds);
    addActionEvent(EVENT_ROWS_DELETED, jsonEvent);
  }

  /**
   * @return the filtered row count excluding rows filtered by the user
   */
  protected int getFilteredRowCount() {
    if (getModel().getRowFilters().size() == 0) {
      return getModel().getRowCount();
    }
    int filteredRowCount = 0;
    for (ITableRow row : getModel().getRows()) {
      if (row.isFilterAccepted() || row.isRejectedByUser()) {
        filteredRowCount++;
      }
    }
    return filteredRowCount;
  }

  protected void handleModelAllRowsDeleted() {
    if (m_tableRows.isEmpty()) {
      return;
    }
    m_tableRows.clear();
    m_tableRowIds.clear();
    addActionEvent(EVENT_ALL_ROWS_DELETED);
  }

  protected void handleModelRowsSelected(Collection<ITableRow> modelRows) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROW_IDS, rowIdsToJson(modelRows));
    addActionEvent(EVENT_ROWS_SELECTED, jsonEvent);
  }

  protected void handleModelRowsChecked(Collection<ITableRow> modelRows) {
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : modelRows) {
      if (!isRowAccepted(row)) {
        continue;
      }
      JSONObject jsonRow = JsonObjectUtility.newOrderedJSONObject();
      putProperty(jsonRow, "id", getTableRowId(row));
      putProperty(jsonRow, "checked", row.isChecked());
      jsonRows.put(jsonRow);
    }
    if (jsonRows.length() == 0) {
      return;
    }
    JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    putProperty(jsonEvent, PROP_ROWS, jsonRows);
    addActionEvent(EVENT_ROWS_CHECKED, jsonEvent);
  }

  protected void handleModelRowOrderChanged(Collection<ITableRow> modelRows) {
    JSONArray jsonRowIds = new JSONArray();
    for (ITableRow row : modelRows) {
      if (!isRowAccepted(row)) {
        continue;
      }
      jsonRowIds.put(getTableRowId(row));
    }
    if (jsonRowIds.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROW_IDS, jsonRowIds);
    addActionEvent("rowOrderChanged", jsonEvent);
  }

  protected void handleModelColumnStructureChanged() {
    disposeAllColumns();
    attachColumns();

    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COLUMNS, columnsToJson(getColumnsInViewOrder()));
    addActionEvent(EVENT_COLUMN_STRUCTURE_CHANGED, jsonEvent);

    // Resend filters because a column with a filter may got invisible.
    // Since the gui does not know invisible columns, the filter would fail.
    // True means only resend if there are filters at all
    addUserFiltersChanged(true);
  }

  protected void handleModelColumnOrderChanged() {
    // Ignore columns that are not currently attached. They will be later
    // attached with the event COLUMN_STRUCTURE_CHANGED.
    List<IColumn<?>> filteredColumns = filterAttachedColumns(getColumnsInViewOrder());
    if (filteredColumns.isEmpty()) {
      return;
    }

    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COLUMN_IDS, columnIdsToJson(filteredColumns));
    addActionEvent(EVENT_COLUMN_ORDER_CHANGED, jsonEvent);
  }

  protected void handleModelColumnHeadersUpdated(Collection<IColumn<?>> columns) {
    JSONObject jsonEvent = new JSONObject();
    Collection<IColumn<?>> visibleColumns = filterVisibleColumns(columns);
    // Ignore columns that are not currently attached. They will be later
    // attached with the event COLUMN_STRUCTURE_CHANGED.
    List<IColumn<?>> filteredColumns = filterAttachedColumns(visibleColumns);
    if (filteredColumns.isEmpty()) {
      return;
    }

    putProperty(jsonEvent, PROP_COLUMNS, columnsToJson(filteredColumns));
    addActionEvent(EVENT_COLUMN_HEADERS_UPDATED, jsonEvent);
  }

  protected void handleModelRequestFocus(TableEvent event) {
    addActionEvent(EVENT_REQUEST_FOCUS);
  }

  protected void handleModelScrollToSelection(TableEvent event) {
    addActionEvent(EVENT_SCROLL_TO_SELECTION);
  }

  protected void handleModelUserFilterChanged(TableEvent event) {
    addUserFiltersChanged();
  }

  protected void addUserFiltersChanged() {
    addUserFiltersChanged(false);
  }

  protected void addUserFiltersChanged(boolean onlyIfThereAreFilters) {
    if (getModel().getUserFilterManager() == null) {
      return;
    }
    Collection<IUserFilterState> filters = getModel().getUserFilterManager().getFilters();
    if (!onlyIfThereAreFilters || filters.size() > 0) {
      addPropertyChangeEvent(PROP_FILTERS, filtersToJson(filters));
    }
  }

  protected void handleModelColumnAggregationChanged(TableEvent event) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray eventParts = new JSONArray();
    for (IColumn<?> c : event.getColumns()) {
      Assertions.assertInstance(c, INumberColumn.class, "ColumnAggregation is only supported on NumberColumns");
      JSONObject eventPart = new JSONObject();
      putProperty(eventPart, "columnId", getColumnId(c));
      putProperty(eventPart, "aggregationFunction", ((INumberColumn) c).getAggregationFunction());
      eventParts.put(eventPart);
    }
    putProperty(jsonEvent, "eventParts", eventParts);
    addActionEvent(EVENT_COLUMN_AGGR_FUNC_CHANGED, jsonEvent);
  }

  protected void handleModelColumnBackgroundEffectChanged(TableEvent event) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray eventParts = new JSONArray();
    for (IColumn<?> c : event.getColumns()) {
      Assertions.assertInstance(c, INumberColumn.class, "ColumnBackgroundEffect is only supported on NumberColumns");
      JSONObject eventPart = new JSONObject();
      putProperty(eventPart, "columnId", getColumnId(c));
      putProperty(eventPart, "backgroundEffect", ((INumberColumn) c).getBackgroundEffect());
      eventParts.put(eventPart);
    }
    putProperty(jsonEvent, "eventParts", eventParts);
    addActionEvent(EVENT_COLUMN_BACKGROUND_EFFECT_CHANGED, jsonEvent);
  }

  protected void handleModelRequestFocusInCell(TableEvent event) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROW_ID, getOrCreatedRowId(event.getRows().iterator().next()));
    putProperty(jsonEvent, PROP_COLUMN_ID, getColumnId(event.getColumns().iterator().next()));
    addActionEvent(EVENT_REQUEST_FOCUS_IN_CELL, jsonEvent);
  }

  protected Collection<IColumn<?>> filterVisibleColumns(Collection<IColumn<?>> columns) {
    List<IColumn<?>> visibleColumns = new LinkedList<IColumn<?>>();
    for (IColumn<?> column : columns) {
      if (column.isVisible()) {
        visibleColumns.add(column);
      }
    }
    return visibleColumns;
  }

  /**
   * @return returns a new list with all columns of the given list that are attached to the model adapter. If a column
   *         is not (yet) attached, it will not be returned.
   */
  protected List<IColumn<?>> filterAttachedColumns(Collection<IColumn<?>> columns) {
    if (columns == null) {
      return null;
    }
    List<IColumn<?>> result = new ArrayList<>();
    for (IColumn<?> column : columns) {
      if (m_jsonColumns.containsKey(column)) {
        result.add(column);
      }
    }
    return result;
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  protected Map<String, ITableRow> tableRowsMap() {
    return m_tableRows;
  }

  protected Map<ITableRow, String> tableRowIdsMap() {
    return m_tableRowIds;
  }

  protected Map<IColumn, JsonColumn> jsonColumns() {
    return m_jsonColumns;
  }

  protected AbstractEventBuffer<TableEvent> eventBuffer() {
    return m_eventBuffer;
  }

  protected TableEventFilterCondition addTableEventFilterCondition(int tableEventType) {
    TableEventFilterCondition conditon = new TableEventFilterCondition(tableEventType);
    m_tableEventFilter.addCondition(conditon);
    return conditon;
  }

  @Override
  public void cleanUpEventFilters() {
    super.cleanUpEventFilters();
    m_tableEventFilter.removeAllConditions();
  }

  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    if ((getModel().getDropType() & IDNDSupport.TYPE_FILE_TRANSFER) == IDNDSupport.TYPE_FILE_TRANSFER) {
      ResourceListTransferObject transferObject = new ResourceListTransferObject(binaryResources);
      ITableRow row = null;
      if (uploadProperties != null && uploadProperties.containsKey("rowId")) {
        String rowId = uploadProperties.get("rowId");
        if (!StringUtility.isNullOrEmpty(rowId)) {
          row = getTableRow(rowId);
        }
      }
      getModel().getUIFacade().fireRowDropActionFromUI(row, transferObject);
    }
  }

  @Override
  public long getMaximumBinaryResourceUploadSize() {
    return getModel().getDropMaximumSize();
  }

  protected CheckedInfo jsonToCheckedInfo(JSONObject data) {
    JSONArray jsonRows = data.optJSONArray("rows");
    CheckedInfo checkInfo = new CheckedInfo();
    for (int i = 0; i < jsonRows.length(); i++) {
      JSONObject jsonObject = jsonRows.optJSONObject(i);
      ITableRow row = optTableRow(jsonObject.getString("rowId"));
      if (row != null) {
        checkInfo.getAllRows().add(row);
        if (jsonObject.optBoolean("checked")) {
          checkInfo.getCheckedRows().add(row);
        }
        else {
          checkInfo.getUncheckedRows().add(row);
        }
      }
    }
    return checkInfo;
  }

  protected static class CheckedInfo {
    private final List<ITableRow> m_allRows = new ArrayList<ITableRow>();
    private final List<ITableRow> m_checkedRows = new ArrayList<ITableRow>();
    private final List<ITableRow> m_uncheckedRows = new ArrayList<ITableRow>();

    public List<ITableRow> getAllRows() {
      return m_allRows;
    }

    public List<ITableRow> getCheckedRows() {
      return m_checkedRows;
    }

    public List<ITableRow> getUncheckedRows() {
      return m_uncheckedRows;
    }
  }

  private class P_TableListener extends TableAdapter {

    @Override
    public void tableChanged(final TableEvent e) {
      handleModelTableEvent(e);
    }
  }
}
