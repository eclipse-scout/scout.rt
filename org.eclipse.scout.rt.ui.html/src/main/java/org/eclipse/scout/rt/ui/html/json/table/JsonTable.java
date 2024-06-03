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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.CheckableStyle;
import org.eclipse.scout.rt.client.ui.basic.table.GroupingStyle;
import org.eclipse.scout.rt.client.ui.basic.table.HierarchicalStyle;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableTileGridMediator;
import org.eclipse.scout.rt.client.ui.basic.table.ITileTableHeader;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IReloadReason;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TextTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.security.ACCESS;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.security.CopyToClipboardPermission;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.IUserFilterStateFactory;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTableUserFilter;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTreeEvent;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceMediator;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTable<T extends ITable> extends AbstractJsonWidget<T> implements IJsonContextMenuOwner, IBinaryResourceConsumer, IBinaryResourceProvider {
  private static final Logger LOG = LoggerFactory.getLogger(JsonTable.class);

  public static final String EVENT_ROW_CLICK = "rowClick";
  public static final String EVENT_ROW_ACTION = "rowAction";
  public static final String EVENT_ROWS_SELECTED = "rowsSelected";
  public static final String EVENT_ROWS_INSERTED = "rowsInserted";
  public static final String EVENT_ROWS_UPDATED = "rowsUpdated";
  public static final String EVENT_ROWS_DELETED = "rowsDeleted";
  public static final String EVENT_ALL_ROWS_DELETED = "allRowsDeleted";
  public static final String EVENT_SORT = "sort";
  public static final String EVENT_GROUP = "group";
  public static final String EVENT_COLUMN_AGGR_FUNC_CHANGED = "aggregationFunctionChanged";
  public static final String EVENT_COLUMN_MOVED = "columnMoved";
  public static final String EVENT_COLUMN_RESIZED = "columnResized";
  public static final String EVENT_RELOAD = "reload";
  public static final String EVENT_RESET_COLUMNS = "resetColumns";
  public static final String EVENT_ROWS_CHECKED = "rowsChecked";
  public static final String EVENT_ROWS_EXPANDED = "rowsExpanded";
  public static final String EVENT_COLUMN_ORDER_CHANGED = "columnOrderChanged";
  public static final String EVENT_COLUMN_STRUCTURE_CHANGED = "columnStructureChanged";
  public static final String EVENT_COLUMN_HEADERS_UPDATED = "columnHeadersUpdated";
  public static final String EVENT_COLUMN_BACKGROUND_EFFECT_CHANGED = "columnBackgroundEffectChanged";
  public static final String EVENT_COLUMN_ORGANIZE_ACTION = "columnOrganizeAction";
  public static final String EVENT_REQUEST_FOCUS_IN_CELL = "requestFocusInCell";
  public static final String EVENT_START_CELL_EDIT = "startCellEdit";
  public static final String EVENT_END_CELL_EDIT = "endCellEdit";
  public static final String EVENT_PREPARE_CELL_EDIT = "prepareCellEdit";
  public static final String EVENT_COMPLETE_CELL_EDIT = "completeCellEdit";
  public static final String EVENT_CANCEL_CELL_EDIT = "cancelCellEdit";
  public static final String EVENT_REQUEST_FOCUS = "requestFocus";
  public static final String EVENT_SCROLL_TO_SELECTION = "scrollToSelection";
  public static final String EVENT_CLIPBOARD_EXPORT = "clipboardExport";
  public static final String EVENT_FILTER_ADDED = "filterAdded";
  public static final String EVENT_FILTER_REMOVED = "filterRemoved";
  public static final String EVENT_FILTERS_CHANGED = "filtersChanged";
  public static final String EVENT_FILTER = "filter";

  public static final String PROP_ROWS = "rows";
  public static final String PROP_ROW_IDS = "rowIds";
  public static final String PROP_ROW_ID = "rowId";
  public static final String PROP_EXPANDED = "expanded";
  public static final String PROP_COLUMN_ID = "columnId";
  public static final String PROP_COLUMN_IDS = "columnIds";
  public static final String PROP_COLUMNS = "columns";
  public static final String PROP_COLUMN_ADDABLE = "columnAddable";
  public static final String PROP_SELECTED_ROWS = "selectedRows";
  public static final String PROP_FILTERS = "filters";
  public static final String PROP_HAS_RELOAD_HANDLER = "hasReloadHandler";
  public static final String PROP_USER_PREFERENCE_CONTEXT = "userPreferenceContext";

  private TableListener m_tableListener;
  private final Map<String, ITableRow> m_tableRows;
  private final Map<ITableRow, String> m_tableRowIds;
  private final Map<String, IColumn<?>> m_columns;
  private final TableEventFilter m_tableEventFilter;
  private final Map<IColumn<?>, JsonColumn<?>> m_jsonColumns;
  private final AbstractEventBuffer<TableEvent> m_eventBuffer;
  private JsonContextMenu<IContextMenu> m_jsonContextMenu;
  private final BinaryResourceMediator m_binaryResourceMediator;
  private final JsonTableListeners m_listeners = new JsonTableListeners();

  public JsonTable(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_tableRows = new HashMap<>();
    m_tableRowIds = new HashMap<>();
    m_columns = new HashMap<>();
    m_tableEventFilter = new TableEventFilter(this);
    m_jsonColumns = new HashMap<>();
    m_eventBuffer = model.createEventBuffer();
    m_binaryResourceMediator = createBinaryResourceMediator();
  }

  protected BinaryResourceMediator createBinaryResourceMediator() {
    return new BinaryResourceMediator(this);
  }

  @Override
  public String getObjectType() {
    return "Table";
  }

  public JsonContextMenu<IContextMenu> getJsonContextMenu() {
    return m_jsonContextMenu;
  }

  public BinaryResourceMediator getBinaryResourceMediator() {
    return m_binaryResourceMediator;
  }

  @Override
  public void init() {
    super.init();

    // Replay missed events
    IEventHistory<TableEvent> eventHistory = getModel().getEventHistory();
    if (eventHistory != null) {
      for (TableEvent event : eventHistory.getRecentEvents()) {
        // Immediately execute events (no buffering), because this method is not called
        // from the model but from the JSON layer. If Response.toJson() is in progress,
        // adding this adapter to the list of buffered event providers would cause
        // an exception.
        processEvent(event);
      }
    }
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
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
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_CHECKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCheckable();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_COMPACT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCompact();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_ROW_ICON_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isRowIconVisible();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_ROW_ICON_COLUMN_WIDTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getRowIconColumnWidth();
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
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_HEADER_MENUS_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHeaderMenusEnabled();
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
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_LOADING, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLoading();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_GROUPING_STYLE, model) {
      @Override
      protected GroupingStyle modelValue() {
        return getModel().getGroupingStyle();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return ((GroupingStyle) value).name().toLowerCase();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_ESTIMATED_ROW_COUNT, model) {
      @Override
      protected Long modelValue() {
        return getModel().getEstimatedRowCount();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_MAX_ROW_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMaxRowCount();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_HIERARCHICAL_STYLE, model) {
      @Override
      protected HierarchicalStyle modelValue() {
        return getModel().getHierarchicalStyle();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return ((HierarchicalStyle) value).name().toLowerCase();
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITable>(ITable.PROP_KEY_STROKES, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<>()).build();
      }

      @Override
      protected List<IKeyStroke> modelValue() {
        return getModel().getKeyStrokes();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_CONTEXT_COLUMN, model) {
      @Override
      protected Object modelValue() {
        return getModel().getContextColumn();
      }

      @SuppressWarnings("SuspiciousMethodCalls")
      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        JsonColumn<?> jsonColumn = m_jsonColumns.get(value);
        if (jsonColumn == null) {
          logContextColumnInconsistency(value);
          return null;
        }
        return jsonColumn.getId();
      }
    });

    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_CHECKABLE_STYLE, model) {
      @Override
      protected CheckableStyle modelValue() {
        return getModel().getCheckableStyle();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return ((CheckableStyle) value).name().toLowerCase();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_TRUNCATED_CELL_TOOLTIP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTruncatedCellTooltipEnabled().getBooleanValue();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_TILE_MODE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTileMode();
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITable>(ITable.PROP_TILE_TABLE_HEADER, model, getUiSession()) {
      @Override
      protected ITileTableHeader modelValue() {
        return getModel().getTileTableHeader();
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITable>(ITable.PROP_TABLE_TILE_GRID_MEDIATOR, model, getUiSession()) {
      @Override
      protected ITableTileGridMediator modelValue() {
        return getModel().getTableTileGridMediator();
      }
    });
    putJsonProperty(new JsonProperty<ITable>(ITable.PROP_TEXT_FILTER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTextFilterEnabled();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachColumns();
    attachRows();
    m_jsonContextMenu = new JsonContextMenu<>(getModel().getContextMenu(), this);
    m_jsonContextMenu.init();
  }

  protected void attachRows() {
    List<ITableRow> rows = getModel().getRows();
    for (ITableRow row : rows) {
      if (isRowAccepted(row)) {
        getOrCreateRowId(row);
      }
    }
  }

  protected void attachColumns() {
    int offset = 0;
    for (IColumn<?> column : getModel().getColumns()) {
      if (!column.isVisible() || column.isCompacted()) {
        // since we don't send row data for invisible columns, we have to adjust the column index
        offset += 1;
        continue;
      }
      String id = getUiSession().createUniqueId();
      JsonColumn<?> jsonColumn = (JsonColumn<?>) MainJsonObjectFactory.get().createJsonObject(column);
      jsonColumn.setId(id);
      jsonColumn.setColumnIndexOffset(offset);
      jsonColumn.setJsonTable(this);
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
    JsonColumn<?> jsonColumn = m_jsonColumns.get(column);
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
    // Ensure cell editor is closed and context set to null
    if (ModelJobs.isModelThread()) {
      getModel().getUIFacade().cancelCellEditFromUI();
    }
    else {
      final ClientRunContext clientRunContext = ClientRunContexts.copyCurrent(true).withSession(getUiSession().getClientSession(), true);
      ModelJobs.schedule(getModel().getUIFacade()::cancelCellEditFromUI, ModelJobs.newInput(clientRunContext)
          .withName("Cancelling cell editor")
          .withExceptionHandling(null, false)); // Propagate exception to caller (UIServlet)
    }

    disposeAllColumns();
    disposeAllRows();
    getJsonContextMenu().dispose();
    super.disposeChildAdapters();
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
    json.put(PROP_COLUMNS, columnsToJson(getColumnsInViewOrder()));
    json.put(PROP_COLUMN_ADDABLE, getModel().getTableOrganizer().isColumnAddable());
    json.put(PROP_ROWS, tableRowsToJson(getModel().getRows()));
    json.put(PROP_MENUS, getJsonContextMenu().childActionsToJson());
    json.put(PROP_SELECTED_ROWS, rowIdsToJson(getModel().getSelectedRows()));
    if (getModel().getUserFilterManager() != null) {
      json.put(PROP_FILTERS, filtersToJson(getModel().getUserFilterManager().getFilters()));
    }
    json.put(PROP_HAS_RELOAD_HANDLER, getModel().getReloadHandler() != null);
    json.put(PROP_USER_PREFERENCE_CONTEXT, getModel().getUserPreferenceContext());
    return json;
  }

  protected JSONArray tableRowsToJson(Collection<ITableRow> rows) {
    return tableRowsToJson(rows, new HashSet<>());
  }

  protected JSONArray tableRowsToJson(Collection<ITableRow> rows, Set<ITableRow> acceptedRows) {
    JSONArray jsonRows = new JSONArray();
    for (ITableRow row : rows) {
      if (isRowAccepted(row)) {
        jsonRows.put(tableRowToJson(row));
        acceptedRows.add(row);
      }
    }
    return jsonRows;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_ROW_CLICK.equals(event.getType())) {
      handleUiRowClick(event);
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
    else if (EVENT_SORT.equals(event.getType())) {
      handleUiSort(event);
    }
    else if (EVENT_GROUP.equals(event.getType())) {
      handleUiGroup(event);
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
    else if (EVENT_ROWS_EXPANDED.equals(event.getType())) {
      handleUiRowsExpanded(event);
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
    else if (EVENT_CLIPBOARD_EXPORT.equals(event.getType())) {
      handleUiClipboardExport(event);
    }
    else if (EVENT_FILTER_ADDED.equals(event.getType())) {
      handleUiFilterAdded(event);
    }
    else if (EVENT_FILTER_REMOVED.equals(event.getType())) {
      handleUiFilterRemoved(event);
    }
    else if (EVENT_FILTER.equals(event.getType())) {
      handleUiFilter(event);
    }
    else if (EVENT_COLUMN_AGGR_FUNC_CHANGED.equals(event.getType())) {
      handleColumnAggregationFunctionChanged(event);
    }
    else if (EVENT_COLUMN_BACKGROUND_EFFECT_CHANGED.equals(event.getType())) {
      handleColumnBackgroundEffectChanged(event);
    }
    else if (EVENT_COLUMN_ORGANIZE_ACTION.equals(event.getType())) {
      handleUiColumnOrganizeAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (ITable.PROP_CONTEXT_COLUMN.equals(propertyName)) {
      String contextColumnId = data.optString(propertyName);
      IColumn<?> column = optColumn(contextColumnId);
      addPropertyEventFilterCondition(propertyName, column);
      getModel().getUIFacade().setContextColumnFromUI(column);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  protected void handleUiColumnOrganizeAction(JsonEvent event) {
    JSONObject data = event.getData();
    String action = data.getString("action");
    IColumn<?> column = extractColumn(data);
    switch (action) {
      case "add":
        getModel().getUIFacade().fireOrganizeColumnAddFromUI(column);
        break;
      case "remove":
        getModel().getUIFacade().fireOrganizeColumnRemoveFromUI(column);
        break;
      case "modify":
        getModel().getUIFacade().fireOrganizeColumnModifyFromUI(column);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  protected void handleUiRowClick(JsonEvent event) {
    ITableRow tableRow = extractTableRow(event.getData());
    if (tableRow == null) {
      LOG.info("Requested table-row doesn't exist anymore -> skip rowClicked event");
      return;
    }
    MouseButton mouseButton = extractMouseButton(event.getData());
    getModel().getUIFacade().fireRowClickFromUI(tableRow, mouseButton);
  }

  @SuppressWarnings("DuplicatedCode")
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
    CheckedInfo checkedInfo = jsonToCheckedInfo(event.getData());
    addTableEventFilterCondition(TableEvent.TYPE_ROWS_CHECKED).setCheckedRows(checkedInfo.getCheckedRows(), checkedInfo.getUncheckedRows());
    if (!checkedInfo.getCheckedRows().isEmpty()) {
      getModel().getUIFacade().setCheckedRowsFromUI(checkedInfo.getCheckedRows(), true);
    }
    if (!checkedInfo.getUncheckedRows().isEmpty()) {
      getModel().getUIFacade().setCheckedRowsFromUI(checkedInfo.getUncheckedRows(), false);
    }
  }

  protected void handleUiRowsExpanded(JsonEvent event) {
    List<ITableRow> expandedRows = new ArrayList<>();
    List<ITableRow> collapsedRows = new ArrayList<>();
    List<ITableRow> rows = new ArrayList<>();
    JSONArray jsonRows = event.getData().optJSONArray("rows");
    if (jsonRows != null) {
      for (int i = 0; i < jsonRows.length(); i++) {
        JSONObject jsonRow = jsonRows.getJSONObject(i);
        ITableRow row = optTableRow(jsonRow.getString("rowId"));
        if (row != null) {
          rows.add(row);
          if (jsonRow.optBoolean("expanded")) {
            expandedRows.add(row);
          }
          else {
            collapsedRows.add(row);
          }
        }
      }
    }
    addTableEventFilterCondition(TableEvent.TYPE_ROWS_EXPANDED).setRows(rows);
    if (!expandedRows.isEmpty()) {
      getModel().getUIFacade().setExpandedRowsFromUI(expandedRows, true);
    }
    if (!collapsedRows.isEmpty()) {
      getModel().getUIFacade().setExpandedRowsFromUI(collapsedRows, false);
    }
  }

  protected void handleUiRowsSelected(JsonEvent event) {
    JSONArray rowIds = event.getData().getJSONArray(PROP_ROW_IDS);
    List<ITableRow> tableRows = extractTableRows(rowIds);
    if (tableRows.isEmpty() && rowIds.length() > 0) {
      // Ignore inconsistent selections from UI (probably an obsolete cached event)
      return;
    }
    if (tableRows.size() == rowIds.length()) {
      addTableEventFilterCondition(TableEvent.TYPE_ROWS_SELECTED).setRows(tableRows);
    }
    getModel().getUIFacade().setSelectedRowsFromUI(tableRows);
  }

  protected void handleUiReload(JsonEvent event) {
    String reloadReason = event.getData().optString("reloadReason", IReloadReason.UNSPECIFIED);
    getModel().getUIFacade().fireTableReloadFromUI(reloadReason);
  }

  protected void handleUiResetColumns(JsonEvent event) {
    getModel().getUIFacade().fireTableResetFromUI();
  }

  /**
   * Makes sure that no rowOrderChanged event is returned to the client after sorting because the sorting already
   * happened on client as well.
   */
  protected void handleUiSort(JsonEvent event) {
    if (!event.getData().optBoolean("sortingRequested")) {
      addTableEventFilterCondition(TableEvent.TYPE_ROW_ORDER_CHANGED);
    }
    fireSortFromUi(event.getData());
  }

  protected void handleUiSortRows(JsonEvent event) {
    fireSortFromUi(event.getData());
  }

  protected void fireSortFromUi(JSONObject data) {
    IColumn<?> column = extractColumn(data);
    boolean sortingRemoved = data.optBoolean("sortingRemoved");

    // TODO [7.0] cgu: add filter for HEADER_UPDATE event with json data of column (execDecorateHeaderCell is called which may change other header properties (text etc)
    if (sortingRemoved) {
      getModel().getUIFacade().fireSortColumnRemovedFromUI(column);
    }
    else {
      boolean multiSort = data.optBoolean("multiSort");
      boolean sortAscending = data.getBoolean("sortAscending");
      getModel().getUIFacade().fireHeaderSortFromUI(column, multiSort, sortAscending);
    }
  }

  protected void handleUiGroup(JsonEvent event) {
    if (!event.getData().optBoolean("groupingRequested")) {
      addTableEventFilterCondition(TableEvent.TYPE_ROW_ORDER_CHANGED);
    }
    fireGroupFromUi(event.getData());
  }

  protected void fireGroupFromUi(JSONObject data) {
    IColumn<?> column = extractColumn(data);
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
    IColumn<?> column = extractColumn(event.getData());
    Assertions.assertInstance(column, INumberColumn.class, "Aggregation can only be specified on numeric columns");
    getModel().getUIFacade().fireAggregationFunctionChanged((INumberColumn<?>) column, event.getData().getString("aggregationFunction"));
  }

  protected void handleColumnBackgroundEffectChanged(JsonEvent event) {
    addTableEventFilterCondition(TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED);
    IColumn<?> column = extractColumn(event.getData());
    Assertions.assertInstance(column, INumberColumn.class, "BackgroundEffect can only be specified on numeric columns");
    getModel().getUIFacade().setColumnBackgroundEffect((INumberColumn<?>) column, event.getData().optString("backgroundEffect", null));
  }

  protected void handleUiColumnMoved(JsonEvent event) {
    IColumn<?> column = extractColumn(event.getData());
    int viewIndex = event.getData().getInt("index");

    // Create column list with expected order
    List<IColumn<?>> columns = getColumnsInViewOrder();
    columns.remove(column);
    columns.add(viewIndex, column);
    addTableEventFilterCondition(TableEvent.TYPE_COLUMN_ORDER_CHANGED).setColumns(columns);
    getModel().getUIFacade().fireColumnMovedFromUI(column, viewIndex);
  }

  protected void handleUiColumnResized(JsonEvent event) {
    IColumn<?> column = extractColumn(event.getData());
    if (column == null) {
      LOG.info("Requested column doesn't exist anymore -> skip columnResized event");
      return;
    }
    int width = event.getData().getInt("width");

    getModel().getUIFacade().setColumnWidthFromUI(column, width);
  }

  protected void handleUiRowAction(JsonEvent event) {
    ITableRow tableRow = extractTableRow(event.getData());
    IColumn<?> column = extractColumn(event.getData());
    getModel().getUIFacade().setContextColumnFromUI(column);
    getModel().getUIFacade().fireRowActionFromUI(tableRow);
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    IColumn<?> column = extractColumn(event.getData());
    String ref = event.getData().optString("ref", null);
    if (column != null) {
      getModel().getUIFacade().setContextColumnFromUI(column);
    }
    getModel().getUIFacade().fireAppLinkActionFromUI(ref);
  }

  protected void handleUiPrepareCellEdit(JsonEvent event) {
    ITableRow row = extractTableRow(event.getData());
    if (row == null) {
      LOG.info("Requested table-row doesn't exist anymore. Skip prepareCellEdit event");
      return;
    }
    IColumn<?> column = extractColumn(event.getData());
    getModel().getUIFacade().prepareCellEditFromUI(row, column);
  }

  protected void startCellEdit(ITableRow row, IColumn<?> column, IFormField field) {
    if (row == null || !isRowAccepted(row)) {
      return;
    }
    if (field == null) {
      // Cell is not editable, simply ignore the request for editing it.
      // This may happen if the JSON request contained other events that
      // caused the initially editable cell to be become non-editable.
      return;
    }

    IJsonAdapter<?> jsonField = attachAdapter(field);
    LOG.debug("Created new field adapter for cell editing. Adapter: {}", jsonField);
    JSONObject json = new JSONObject();
    json.put("columnId", getColumnId(column));
    json.put("rowId", getTableRowId(row));
    json.put("fieldId", jsonField.getId());
    addActionEvent(EVENT_START_CELL_EDIT, jsonField, json).protect();
  }

  protected void handleUiCompleteCellEdit(JsonEvent event) {
    getModel().getUIFacade().completeCellEditFromUI();
  }

  protected void handleUiCancelCellEdit(JsonEvent event) {
    getModel().getUIFacade().cancelCellEditFromUI();
  }

  protected void endCellEdit(IFormField field) {
    IJsonAdapter<?> jsonField = getAdapter(field);
    if (jsonField == null) {
      LOG.info("No field adapter found for cell-editor " + field + ". Maybe the editor or the corresponding form had been closed during completeCellEdit.");
      return;
    }

    // Confirm end cell edit so that gui can dispose the adapter.
    // (It is not possible to dispose the adapter on the gui before sending complete or cancelCellEdit, because the field may send property change events back)
    JSONObject json = new JSONObject();
    json.put("fieldId", jsonField.getId());
    addActionEvent(EVENT_END_CELL_EDIT, jsonField, json).protect();

    jsonField.dispose();
  }

  protected void handleUiClipboardExport(JsonEvent event) {
    if (!ACCESS.check(new CopyToClipboardPermission())) {
      return;
    }

    TransferObject scoutTransferable = getModel().getUIFacade().fireRowsCopyRequestFromUI();
    if (scoutTransferable instanceof TextTransferObject) {
      try {
        BEANS.get(IClipboardService.class).setContents(scoutTransferable);
      }
      catch (RuntimeException e) {
        throw new UiException("Unable to copy to clipboard.", e);
      }
    }
  }

  protected void handleUiFilterAdded(JsonEvent event) {
    JSONObject data = event.getData();
    IUserFilterState filterState = createFilterState(data);
    if (filterState == null) {
      // in case it is a JS only filter that has no Java representation.
      return;
    }

    TableEventFilterCondition condition = addTableEventFilterCondition(TableEvent.TYPE_USER_FILTER_ADDED);
    condition.setUserFilter(filterState);
    getModel().getUIFacade().fireFilterAddedFromUI(filterState);
  }

  protected IUserFilterState createFilterState(JSONObject data) {
    for (IUserFilterStateFactory factory : BEANS.all(IUserFilterStateFactory.class)) {
      IUserFilterState userFilterState = factory.createUserFilterState(this, data);
      if (userFilterState != null) {
        return userFilterState;
      }
    }
    return null;
  }

  protected IUserFilterState getFilterState(JSONObject data) {
    String type = data.getString("filterType");
    if ("column".equals(type)) {
      IColumn<?> column = extractColumn(data);
      return getModel().getUserFilterManager().getFilter(column.getColumnId());
    }

    return getModel().getUserFilterManager().getFilter(type);
  }

  protected void handleUiFilterRemoved(JsonEvent event) {
    IUserFilterState filter = getFilterState(event.getData());
    if (filter == null) {
      // in case it is a JS only filter that has no Java representation.
      return;
    }

    TableEventFilterCondition condition = addTableEventFilterCondition(TableEvent.TYPE_USER_FILTER_REMOVED);
    condition.setUserFilter(filter);
    getModel().getUIFacade().fireFilterRemovedFromUI(filter);
  }

  protected void handleUiFilter(JsonEvent event) {
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
      if (column.isVisible() && !column.isCompacted()) {
        jsonCells.put(cellToJson(row, column));
      }
    }
    JSONObject jsonRow = new JSONObject();
    putProperty(jsonRow, "id", getOrCreateRowId(row));
    putProperty(jsonRow, "parentRow", getOrCreateRowId(getModel().findParentRow(row)));
    putProperty(jsonRow, "cells", jsonCells);
    putProperty(jsonRow, "checked", row.isChecked());
    putProperty(jsonRow, "enabled", row.isEnabled());
    putProperty(jsonRow, "expanded", row.isExpanded());
    putProperty(jsonRow, "iconId", BinaryResourceUrlUtility.createIconUrl(row.getIconId()));
    putProperty(jsonRow, "cssClass", row.getCssClass());
    if (row.getCustomValue(AbstractTableRowData.CUSTOM_VALUES_ID_GEO_LOCATION) != null) {
      JSONObject geoLocations = new JSONObject((Map<?, ?>) row.getCustomValue(AbstractTableRowData.CUSTOM_VALUES_ID_GEO_LOCATION));
      putProperty(jsonRow, "geoLocationValues", geoLocations);
    }
    putProperty(jsonRow, "compactValue", BinaryResourceUrlUtility.replaceImageUrls(this, row.getCompactValue()));
    JsonObjectUtility.filterDefaultValues(jsonRow, "TableRow");
    return jsonRow;
  }

  protected Object cellToJson(final ITableRow row, final IColumn<?> column) {
    ICell cell = row.getCell(column);
    JsonColumn<?> jsonColumn = m_jsonColumns.get(column);
    if (jsonColumn == null) {
      throw new ProcessingException("No JsonColumn for column " + column);
    }
    JsonCell jsonCell = jsonColumn.createJsonCell(cell, this);
    return jsonCell.toJsonOrString();
  }

  protected JSONArray columnsToJson(Collection<IColumn<?>> columns) {
    JSONArray jsonColumns = new JSONArray();
    for (IColumn<?> column : columns) {
      JsonColumn<?> jsonColumn = m_jsonColumns.get(column);
      JSONObject json = jsonColumn.toJson();
      JsonObjectUtility.filterDefaultValues(json, jsonColumn.getObjectTypeVariant());
      jsonColumns.put(json);
    }
    return jsonColumns;
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filename) {
    BinaryResource attachment = getModel().getAttachment(filename);
    return attachment == null ? getBinaryResourceMediator().getBinaryResourceHolder(filename) : new BinaryResourceHolder(attachment);
  }

  /**
   * @return columns in the right order to be presented to the user
   */
  protected List<IColumn<?>> getColumnsInViewOrder() {
    return getModel().getColumnSet().getVisibleColumns().stream().filter(column -> !column.isCompacted()).collect(Collectors.toList());
  }

  protected String getOrCreateRowId(ITableRow row) {
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
    if (row.getTable() == null || row.isStatusDeleted()) {
      return false;
    }
    if (row.isFilterAccepted()) {
      return true;
    }
    // Accept if rejected by user row filter because gui is and should be aware of that row
    return row.isRejectedByUser();
  }

  public List<ITableRow> extractTableRows(JSONObject json) {
    JSONArray rowIds = json.getJSONArray(PROP_ROW_IDS);
    return extractTableRows(rowIds);
  }

  public List<ITableRow> extractTableRows(JSONArray rowIds) {
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

  protected IColumn<?> extractColumn(JSONObject json) {
    String columnId = json.optString(PROP_COLUMN_ID, null);
    if (columnId == null) {
      return null;
    }
    return optColumn(columnId);
  }

  protected JsonColumn<?> extractJsonColumn(JSONObject json) {
    IColumn<?> column = extractColumn(json);
    return getJsonColumn(column);
  }

  protected JSONArray columnIdsToJson(Collection<IColumn<?>> columns) {
    JSONArray jsonColumnIds = new JSONArray();
    for (IColumn<?> column : columns) {
      jsonColumnIds.put(getColumnId(column));
    }
    return jsonColumnIds;
  }

  public IColumn<?> optColumn(String columnId) {
    return m_columns.get(columnId);
  }

  protected IColumn<?> getColumn(String columnId) {
    IColumn<?> column = m_columns.get(columnId);
    if (column == null) {
      throw new UiException("No column found for id " + columnId);
    }
    return column;
  }

  protected JsonColumn<?> getJsonColumn(IColumn<?> column) {
    if (column == null) {
      return null;
    }
    return m_jsonColumns.get(column);
  }

  public String getColumnId(IColumn<?> column) {
    JsonColumn<?> jsonColumn = getJsonColumn(column);
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
   *           when no row is found for the given rowId
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
      JsonTableUserFilter<?> jsonFilter = (JsonTableUserFilter<?>) MainJsonObjectFactory.get().createJsonObject(filter);
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

  @SuppressWarnings("SwitchStatementWithTooFewBranches")
  protected void bufferModelEvent(TableEvent event) {
    switch (event.getType()) {
      case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED: {
        bufferColumnStructureChanged(event);
        break;
      }
      case TableEvent.TYPE_COLUMN_HEADERS_UPDATED: {
        bufferColumnHeadersUpdated(event);
        break;
      }
      default: {
        m_eventBuffer.add(event);
      }
    }
  }

  protected void bufferColumnStructureChanged(TableEvent event) {
    m_eventBuffer.add(event);

    // If a column got visible it is necessary to resend all rows to inform the gui about the new cells of the new column
    // Before doing this, we need to delete all rows
    m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_ALL_ROWS_DELETED));

    // Resend filters because a column with a filter may got invisible (since the gui does not know invisible columns, the filter would fail).
    // Also necessary because column ids have changed.
    if (getModel().getUserFilterManager() != null && !getModel().getUserFilterManager().getFilters().isEmpty()) {
      m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_USER_FILTER_ADDED));
    }

    // Now resend the rows
    m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_ROWS_INSERTED, getModel().getRows()));

    // Ensure selection is preserved.
    if (getModel().getSelectedRowCount() > 0) {
      m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_ROWS_SELECTED, getModel().getSelectedRows()));
    }
  }

  protected void bufferColumnHeadersUpdated(TableEvent event) {
    m_eventBuffer.add(event);
    if (getModel().isCompact()) {
      // If header is updated the compact value may have changed -> ensure ui will be informed about the change
      m_eventBuffer.add(new TableEvent(getModel(), TableEvent.TYPE_ROWS_UPDATED, getModel().getRows()));
    }
  }

  protected void preprocessBufferedEvents() {
    List<TableEvent> bufferInternal = m_eventBuffer.getBufferInternal();
    Map<ITableRow, Integer> rowsContainedInActualInsertEvents = new HashMap<>();
    for (int i = 0; i < bufferInternal.size(); i++) {
      TableEvent event = bufferInternal.get(i);
      if (event.getType() != TableEvent.TYPE_ROWS_INSERTED) {
        continue;
      }
      for (ITableRow r : event.getRows()) {
        rowsContainedInActualInsertEvents.put(r, i);
      }
    }
    for (int i = 0; i < bufferInternal.size(); i++) {
      TableEvent event = bufferInternal.get(i);
      if (event.getType() != TableEvent.TYPE_ROW_FILTER_CHANGED) {
        continue;
      }

      // Convert the "filter changed" event to a ROWS_DELETED and a ROWS_INSERTED event. This prevents sending unnecessary
      // data to the UI. We convert the event before adding it to the event buffer to allow coalescing on UI-level.
      // NOTE: This may lead to a temporary inconsistent situation, where row events exist in the buffer after the
      // row itself is deleted. This is because the row is not really deleted from the model. However, when processing
      // the buffered events, the "wrong" events will be ignored and everything is fixed again.
      List<ITableRow> rowsToInsert = new ArrayList<>();
      List<ITableRow> rowsToDelete = new ArrayList<>();
      List<ITableRow> rowsToIgnoreForOrderChanged = new ArrayList<>();
      for (ITableRow row : getModel().getRows()) {
        String existingRowId = getTableRowId(row);
        if (row.isFilterAccepted()) {
          if (rowsContainedInActualInsertEvents.containsKey(row) && rowsContainedInActualInsertEvents.get(row) > i) {
            // do not add an artificial insert for this row as it is contained in a following (real) insert event
            // row must also not be added to another row order changed event if the insert event is after our event
            // rows already inserted by previous insert events will be removed from these
            rowsToIgnoreForOrderChanged.add(row);
            continue;
          }
          if (existingRowId == null) {
            // Row is not filtered but JsonTable does not know it yet --> handle as insertion event
            rowsToInsert.add(row);
          }
        }
        else if (!row.isRejectedByUser() && existingRowId != null) {
          // Row is filtered, but JsonTable has it in its list --> handle as deletion event
          rowsToDelete.add(row);
        }
      }

      // Put at the same position as the row_filter_changed event (replace it) to keep the order of multiple insert events
      bufferInternal.set(i, new TableEvent(getModel(), TableEvent.TYPE_ROWS_INSERTED, rowsToInsert));
      if (!rowsToInsert.isEmpty()) {
        // Generate an artificial "row order changed" event so that the inserted rows are at the correct position in the UI
        ArrayList<ITableRow> rowOrderChangedRows = CollectionUtility.arrayList(getModel().getRows());
        rowOrderChangedRows.removeAll(rowsToIgnoreForOrderChanged);
        bufferInternal.add(i + 1, new TableEvent(getModel(), TableEvent.TYPE_ROW_ORDER_CHANGED, rowOrderChangedRows));
      }

      // Make sure no previous event contains the newly inserted rows
      for (int j = i - 1; j >= 0; j--) {
        bufferInternal.get(j).removeRows(rowsToInsert);
      }

      // Put at the beginning to make sure no subsequent event contains the deleted row
      bufferInternal.add(0, new TableEvent(getModel(), TableEvent.TYPE_ROWS_DELETED, rowsToDelete));
      i++; // NOSONAR
    }
  }

  @Override
  public void processBufferedEvents() {
    if (m_eventBuffer.isEmpty()) {
      return;
    }
    preprocessBufferedEvents();
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
      case TableEvent.TYPE_ROWS_EXPANDED:
        handleModelRowsExpanded(event.getRows());
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
        handleModelUserFilterChange(event);
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
      case TableEvent.TYPE_START_CELL_EDIT:
        handleModelStartCellEdit(event);
        break;
      case TableEvent.TYPE_END_CELL_EDIT:
        handleModelEndCellEdit(event);
        break;
      default:
        // NOP
    }
  }

  protected void handleModelRowsInserted(Collection<ITableRow> modelRows) {
    Set<ITableRow> acceptedRows = new HashSet<>();
    JSONArray jsonRows = tableRowsToJson(modelRows, acceptedRows);
    if (jsonRows.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROWS, jsonRows);
    addActionEvent(EVENT_ROWS_INSERTED, jsonEvent);
    m_listeners.fireEvent(new JsonTableEvent(this, JsonTreeEvent.TYPE_NODES_INSERTED, acceptedRows));
  }

  protected void handleModelRowsUpdated(Collection<ITableRow> modelRows) {
    JSONArray jsonRows = tableRowsToJson(modelRows);
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
    Set<ITableRow> disposedRows = new HashSet<>();
    JSONArray jsonRowIds = new JSONArray();
    for (ITableRow row : modelRows) {
      String rowId = getTableRowId(row);
      if (rowId == null) { // Ignore rows that are not yet sent to the UI (may happen when a filtered row is deleted)
        continue;
      }
      jsonRowIds.put(rowId);
      disposeRow(row);
      disposedRows.add(row);
    }
    if (jsonRowIds.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put(PROP_ROW_IDS, jsonRowIds);
    addActionEvent(EVENT_ROWS_DELETED, jsonEvent);
    m_listeners.fireEvent(new JsonTableEvent(this, JsonTableEvent.TYPE_ROWS_DELETED, disposedRows));
  }

  /**
   * @return the filtered row count excluding rows filtered by the user
   */
  protected int getFilteredRowCount() {
    if (getModel().getRowFilters().isEmpty()) {
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
    Collection<ITableRow> disposedRows = null;
    if (m_listeners.list(JsonTableEvent.TYPE_ROWS_DELETED).size() > 0) {
      disposedRows = new ArrayList<>(m_tableRows.values());
    }
    m_tableRows.clear();
    m_tableRowIds.clear();
    addActionEvent(EVENT_ALL_ROWS_DELETED);
    m_listeners.fireEvent(new JsonTableEvent(this, JsonTableEvent.TYPE_ROWS_DELETED, disposedRows));
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
      JSONObject jsonRow = new JSONObject();
      putProperty(jsonRow, "id", getTableRowId(row));
      putProperty(jsonRow, "checked", row.isChecked());
      jsonRows.put(jsonRow);
    }
    if (jsonRows.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROWS, jsonRows);
    addActionEvent(EVENT_ROWS_CHECKED, jsonEvent);
  }

  protected void handleModelRowsExpanded(List<ITableRow> rows) {
    JSONArray jsonRows = new JSONArray();
    rows.stream().filter(this::isRowAccepted)
        .map(row -> {
          JSONObject jsonRow = new JSONObject();
          putProperty(jsonRow, "id", getTableRowId(row));
          putProperty(jsonRow, "expanded", row.isExpanded());
          return jsonRow;
        }).forEach(jsonRows::put);

    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROWS, jsonRows);
    addActionEvent(EVENT_ROWS_EXPANDED, jsonEvent);
  }

  protected void handleModelRowOrderChanged(Collection<ITableRow> modelRows) {
    JSONArray jsonRowIds = new JSONArray();
    List<String> rowIds = new ArrayList<>();
    for (ITableRow row : modelRows) {
      if (isRowAccepted(row)) {
        String rowId = getTableRowId(row);
        jsonRowIds.put(rowId);
        rowIds.add(rowId);
      }
    }

    if (jsonRowIds.length() < m_tableRows.size()) {
      // Append missing rows to the end, otherwise the UI cannot not update the order.
      // This may happen if rows are deleted after a row order change.
      // In that case rows are deleted anyway so it is fine if they are not ordered correctly
      List<String> missingRowIds = new ArrayList<>(m_tableRows.keySet());
      missingRowIds.removeAll(rowIds);
      for (String id : missingRowIds) {
        jsonRowIds.put(id);
      }
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
    addPropertyChangeEvent(PROP_COLUMN_ADDABLE, getModel().getTableOrganizer().isColumnAddable());
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
    addActionEvent(EVENT_REQUEST_FOCUS).protect();
  }

  protected void handleModelScrollToSelection(TableEvent event) {
    addActionEvent(EVENT_SCROLL_TO_SELECTION).protect();
  }

  protected void handleModelUserFilterChange(TableEvent event) {
    Collection<IUserFilterState> filters = getModel().getUserFilterManager().getFilters();
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put(PROP_FILTERS, filtersToJson(filters));
    addActionEvent(EVENT_FILTERS_CHANGED, jsonEvent);
  }

  protected void handleModelColumnAggregationChanged(TableEvent event) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray eventParts = new JSONArray();
    for (IColumn<?> c : event.getColumns()) {
      Assertions.assertInstance(c, INumberColumn.class, "ColumnAggregation is only supported on NumberColumns");
      JSONObject eventPart = new JSONObject();
      putProperty(eventPart, "columnId", getColumnId(c));
      putProperty(eventPart, "aggregationFunction", ((INumberColumn<?>) c).getAggregationFunction());
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
      putProperty(eventPart, "backgroundEffect", ((INumberColumn<?>) c).getBackgroundEffect());
      eventParts.put(eventPart);
    }
    putProperty(jsonEvent, "eventParts", eventParts);
    addActionEvent(EVENT_COLUMN_BACKGROUND_EFFECT_CHANGED, jsonEvent);
  }

  protected void handleModelRequestFocusInCell(TableEvent event) {
    final ITableRow row = CollectionUtility.firstElement(event.getRows());
    if (row == null || !isRowAccepted(row)) {
      return;
    }

    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_ROW_ID, getOrCreateRowId(row));
    putProperty(jsonEvent, PROP_COLUMN_ID, getColumnId(CollectionUtility.firstElement(event.getColumns())));
    addActionEvent(EVENT_REQUEST_FOCUS_IN_CELL, jsonEvent).protect();
  }

  protected void handleModelStartCellEdit(TableEvent event) {
    ITableRow row = CollectionUtility.firstElement(event.getRows());
    IColumn<?> column = CollectionUtility.firstElement(event.getColumns());
    startCellEdit(row, column, event.getCellEditor());
  }

  protected void handleModelEndCellEdit(TableEvent event) {
    endCellEdit(event.getCellEditor());
  }

  protected Collection<IColumn<?>> filterVisibleColumns(Collection<IColumn<?>> columns) {
    List<IColumn<?>> visibleColumns = new LinkedList<>();
    for (IColumn<?> column : columns) {
      if (column.isVisible() && !column.isCompacted()) {
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
  public void handleModelContextMenuChanged(FilteredJsonAdapterIds<?> filteredAdapters) {
    addPropertyChangeEvent(PROP_MENUS, filteredAdapters);
  }

  protected Map<String, ITableRow> tableRowsMap() {
    return m_tableRows;
  }

  protected Map<ITableRow, String> tableRowIdsMap() {
    return m_tableRowIds;
  }

  protected Map<IColumn<?>, JsonColumn<?>> jsonColumns() {
    return m_jsonColumns;
  }

  protected AbstractEventBuffer<TableEvent> eventBuffer() {
    return m_eventBuffer;
  }

  protected TableEventFilterCondition addTableEventFilterCondition(int tableEventType) {
    TableEventFilterCondition condition = new TableEventFilterCondition(tableEventType);
    m_tableEventFilter.addCondition(condition);
    return condition;
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
  public long getMaximumUploadSize() {
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
    private final List<ITableRow> m_allRows = new ArrayList<>();
    private final List<ITableRow> m_checkedRows = new ArrayList<>();
    private final List<ITableRow> m_uncheckedRows = new ArrayList<>();

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

  public JsonTableListeners listeners() {
    return m_listeners;
  }

  public void addListener(JsonTableListener listener, Integer... eventTypes) {
    listeners().add(listener, false, eventTypes);
  }

  public void removeListener(JsonTableListener listener, Integer... eventTypes) {
    listeners().remove(listener, eventTypes);
  }

  // Temporary logging to analyze non-reproducible bug
  protected void logContextColumnInconsistency(Object value) {
    String debugInfo = "\nAdapterId : " + getId();
    debugInfo += "\nParent    : " + getParent();
    debugInfo += "\nDisposed  : " + isDisposed();
    debugInfo += "\nModel     : " + getModel();
    debugInfo += "\nValue     : " + (value instanceof IColumn ? toDebugInfo((IColumn<?>) value) : value);
    debugInfo += "\nCtxColumn : " + toDebugInfo(getModel().getContextColumn());
    debugInfo += "\nColumns:\n" + getModel().getColumns().stream()
        .map(column -> "  " + toDebugInfo(column))
        .collect(Collectors.joining("\n"));
    debugInfo += "\nAdapters:\n" + m_jsonColumns.entrySet().stream()
        .map(entry -> "  " + Integer.toHexString(entry.getKey().hashCode()) + " = " + toDebugInfo(entry.getValue()))
        .collect(Collectors.joining("\n"));
    debugInfo += "\nEvent Buffer";
    if (m_eventBuffer.isEmpty()) {
      debugInfo += " is empty";
    }
    else {
      debugInfo += " contains " + m_eventBuffer.size() + " events:\n  - ";
      debugInfo += m_eventBuffer.getBufferInternal().stream()
          .map(event -> StringUtility.substring(StringUtility.removeNewLines(event.toString()), 0, 250))
          .collect(Collectors.joining("\n  - "));
    }
    LOG.info("Could not resolve context column, assuming null.\n--- DEBUG INFO --- {}", debugInfo);
  }

  protected String toDebugInfo(IColumn<?> column) {
    if (column == null) {
      return "null";
    }
    String text = column.getHeaderCell().getText();
    return column.getClass().getName() + "@" + Integer.toHexString(column.hashCode()) +
        " [" + (text == null ? "null" : "\"" + text + "\"") +
        " viewIndexHint=" + column.getVisibleColumnIndexHint() +
        " visible=" + column.isVisible() +
        " table=" + column.getTable() + "]";
  }

  protected String toDebugInfo(JsonColumn<?> jsonColumn) {
    if (jsonColumn == null) {
      return "null";
    }
    return jsonColumn.getClass().getName() + "@" + Integer.toHexString(jsonColumn.hashCode()) +
        " adapterId: " + jsonColumn.getId() +
        " model: " + toDebugInfo(jsonColumn.getColumn());
  }

  protected class P_TableListener extends TableAdapter {

    @Override
    public void tableChanged(final TableEvent e) {
      ModelJobs.assertModelThread();
      handleModelTableEvent(e);
    }
  }
}
