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
package org.eclipse.scout.rt.ui.json.table;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.ui.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.JsonException;
import org.eclipse.scout.rt.ui.json.desktop.MenuManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonTable extends AbstractJsonPropertyObserverRenderer<ITable> {
  public static final String EVENT_ROW_CLICKED = "rowClicked";
  public static final String EVENT_ROW_ACTION = "rowAction";
  public static final String EVENT_ROWS_SELECTED = "rowsSelected";
  public static final String EVENT_SELECTION_MENUS_CHANGED = "selectionMenusChanged";
  public static final String PROP_ROW_IDS = "rowIds";
  public static final String PROP_ROW_ID = "rowId";
  public static final String PROP_MENUS = "menus";
  public static final String PROP_SELECTION_MENUS = "selectionMenus";
  public static final String PROP_EMPTY_SPACE_MENUS = "emptySpaceMenus";

  private P_ModelTableListener m_modelTableListener;
  private Map<String, ITableRow> m_tableRows;
  private Map<ITableRow, String> m_tableRowIds;
  private TableEventFilter m_tableEventFilter;
  private MenuManager m_menuManager;

  public JsonTable(ITable modelObject, IJsonSession jsonSession) {
    super(modelObject, jsonSession);
    m_tableRows = new HashMap<>();
    m_tableRowIds = new HashMap<>();
    m_tableEventFilter = new TableEventFilter(modelObject);
    m_menuManager = new MenuManager(getJsonSession());

    delegateProperty(ITable.PROP_HEADER_VISIBLE);
    //FIXME add missing
  }

  @Override
  public String getObjectType() {
    return "Table";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_modelTableListener == null) {
      m_modelTableListener = new P_ModelTableListener();
      getModelObject().addUITableListener(m_modelTableListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_modelTableListener != null) {
      getModelObject().removeTableListener(m_modelTableListener);
      m_modelTableListener = null;
    }
  }

  public TableEventFilter getTableEventFilter() {
    return m_tableEventFilter;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    try {
      JSONArray jsonColumns = new JSONArray();
      for (IColumn<?> column : getModelObject().getColumns()) {
        if (column.isDisplayable()) {
          jsonColumns.put(columnToJson(column));
        }
      }
      json.put("columns", jsonColumns);

      JSONArray jsonRows = new JSONArray();
      for (ITableRow row : getModelObject().getRows()) {
        JSONObject jsonRow = tableRowToJson(row);
        jsonRows.put(jsonRow);
      }
      json.put("rows", jsonRows);

      m_menuManager.replaceSelectionMenus(fetchMenusForSelection());
      json.put(PROP_SELECTION_MENUS, m_menuManager.getJsonSelectionMenus());
      m_menuManager.replaceSelectionMenus(fetchMenusForEmptySpace());
      json.put(PROP_EMPTY_SPACE_MENUS, m_menuManager.getJsonEmptySpaceMenus());

      json.put(ITable.PROP_HEADER_VISIBLE, getModelObject().isHeaderVisible());

      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_ROW_CLICKED.equals(event.getEventType())) {
      handleUiRowClicked(event, res);
    }
    else if (EVENT_ROW_ACTION.equals(event.getEventType())) {
      handleUiRowAction(event, res);
    }
    else if (EVENT_ROWS_SELECTED.equals(event.getEventType())) {
      handleUiRowsSelected(event, res);
    }
  }

  protected void handleUiRowClicked(JsonEvent event, JsonResponse res) {
    try {
      final ITableRow tableRow = extractTableRow(event.getEventObject());

      new ClientSyncJob("Row clicked", getJsonSession().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          getModelObject().getUIFacade().fireRowClickFromUI(tableRow);
        }
      }.runNow(new NullProgressMonitor());
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected void handleUiRowsSelected(JsonEvent event, JsonResponse res) {
    try {
      final List<ITableRow> tableRows = extractTableRows(event.getEventObject());

      new ClientSyncJob("Rows selected", getJsonSession().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          TableEvent tableEvent = new TableEvent(getModelObject(), TableEvent.TYPE_ROWS_SELECTED, tableRows);
          getTableEventFilter().addIgnorableModelEvent(tableEvent);

          try {
            getModelObject().getUIFacade().setSelectedRowsFromUI(tableRows);
          }
          finally {
            getTableEventFilter().removeIgnorableModelEvent(tableEvent);
          }
        }
      }.runNow(new NullProgressMonitor());
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected void handleUiRowAction(JsonEvent event, JsonResponse res) {
    try {
      final ITableRow tableRow = extractTableRow(event.getEventObject());

      new ClientSyncJob("Row action", getJsonSession().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          getModelObject().getUIFacade().fireRowActionFromUI(tableRow);
        }
      }.runNow(new NullProgressMonitor());
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected List<IMenu> fetchMenusForSelection() {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    new ClientSyncJob("Fetching menus", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        menuList.addAll(getModelObject().getUIFacade().fireRowPopupFromUI());
      }
    }.runNow(new NullProgressMonitor());

    return menuList;
  }

  protected List<IMenu> fetchMenusForEmptySpace() {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    new ClientSyncJob("Fetching menus", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        menuList.addAll(getModelObject().getUIFacade().fireEmptySpacePopupFromUI());
      }
    }.runNow(new NullProgressMonitor());

    return menuList;
  }

  protected JSONObject tableRowToJson(ITableRow row) {
    JSONArray jsonCells = new JSONArray();
    for (int colIndex = 0; colIndex < row.getCellCount(); colIndex++) {
      IColumn column = row.getTable().getColumnSet().getColumn(colIndex);
      if (column.isDisplayable()) {
        jsonCells.put(cellToJson(row.getCell(colIndex), column));
      }
    }

    JSONObject jsonRow = new JSONObject();
    try {
      jsonRow.put("id", getOrCreatedRowId(row));
      jsonRow.put("cells", jsonCells);
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }

    return jsonRow;
  }

  protected Object cellToJson(ICell cell, IColumn column) {
    JSONObject jsonCell = new JSONObject();
    try {
      jsonCell.put("value", getCellValue(cell, column));
      jsonCell.put("foregroundColor", cell.getForegroundColor());
      jsonCell.put("backgroundColor", cell.getBackgroundColor());
      //FIXME implement missing

      if (jsonCell.length() > 0) {
        jsonCell.put("text", cell.getText());
        return jsonCell;
      }
      else {
        //Don't generate an object if only the text is returned to reduce the amount of data
        return cell.getText();
      }
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  protected Object getCellValue(ICell cell, IColumn column) {
    Object retVal = null;
    if (column instanceof IDateColumn) {
      Date date = (Date) cell.getValue();
      if (date != null) {
        IDateColumn dateColumn = (IDateColumn) column;
        if (dateColumn.isHasDate() && !dateColumn.isHasTime()) {
          retVal = DateUtility.format(date, "yyyy-MM-dd");
        }
        else {
          retVal = date.getTime();
        }
      }
    }
    else if (Number.class.isAssignableFrom(column.getDataType())) {
      retVal = cell.getValue();
    }
    //not necessary to send duplicate values
    if (retVal != null && !String.valueOf(retVal).equals(cell.getText())) {
      return retVal;
    }
    return null;
  }

  protected JSONObject columnToJson(IColumn column) {
    try {
      JSONObject json = new JSONObject();
      json.put("id", column.getColumnId());
      json.put("text", column.getHeaderCell().getText());
      json.put("type", computeColumnType(column));
      json.put(IColumn.PROP_WIDTH, column.getWidth());
      json.put("summary", column.isSummary());
      json.put(IColumn.PROP_VISIBLE, column.isVisible()); //FIXME property change, really transmit invisible cols?

      if (column instanceof INumberColumn<?>) {
        //Use localized pattern which contains the relevant chars for the current locale using DecimalFormatSymbols
        json.put("format", ((INumberColumn) column).getFormat().toLocalizedPattern());
      }
      else if (column instanceof IDateColumn) {
        //FIXME CGU update IDateColumnInterface
        //getDateFormat uses LocaleThreadLocal. IMHO getDateFormat should not perform any logic because it just a getter-> refactor. same on AbstractDateField
        //Alternative would be to use a clientJob or set localethreadlocal in ui thread as well, as done in rap
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
      //FIXME complete
      return json;
    }
    catch (JSONException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected String computeColumnType(IColumn column) {
    if (Number.class.isAssignableFrom(column.getDataType())) {
      return "number";
    }
    if (Date.class.isAssignableFrom(column.getDataType())) {
      return "date";
    }
    return "text";
  }

  protected String getOrCreatedRowId(ITableRow row) {
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
      jsonRowIds.put(m_tableRowIds.get(row));
    }
    return jsonRowIds;
  }

  public List<ITableRow> extractTableRows(JSONObject jsonObject) throws JSONException {
    return jsonToTableRows(jsonObject.getJSONArray(PROP_ROW_IDS));
  }

  public ITableRow extractTableRow(JSONObject jsonObject) throws JSONException {
    return getTableRowForRowId(jsonObject.getString(PROP_ROW_ID));
  }

  protected List<ITableRow> jsonToTableRows(JSONArray rowIds) {
    try {
      List<ITableRow> rows = new ArrayList<>(rowIds.length());
      for (int i = 0; i < rowIds.length(); i++) {
        rows.add(m_tableRows.get(rowIds.get(i)));
      }
      return rows;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  private ITableRow getTableRowForRowId(String rowId) {
    ITableRow row = m_tableRows.get(rowId);
    if (row == null) {
      throw new JsonException("No row found for id " + rowId);
    }
    return row;
  }

  protected void handleModelTableEventBatch(List<? extends TableEvent> events) {
    for (TableEvent event : events) {
      handleModelTableEvent(event); //FIXME sufficient?
    }
  }

  protected void handleModelTableEvent(TableEvent event) {
    switch (event.getType()) {
      case TableEvent.TYPE_ROWS_SELECTED: {
        handleModelSelectionMenusChanged(event.getRows());
        break;
      }
    }

    event = getTableEventFilter().filterIgnorableModelEvent(event);
    if (event == null) {
      return;
    }
    switch (event.getType()) {
      case TableEvent.TYPE_ROWS_INSERTED: {
        handleModelRowsInserted(event);
        break;
      }
      case TableEvent.TYPE_ROWS_DELETED: {
        handleModelRowsDeleted(event.getRows());
        break;
      }
      case TableEvent.TYPE_ROWS_SELECTED: {
        handleModelRowsSelected(event.getRows());
        break;
      }
      case TableEvent.TYPE_ROW_ORDER_CHANGED: {
        handleModelRowOrderChanged(event.getRows());
        break;
      }
    }
  }

  protected void handleModelRowsInserted(TableEvent event) {
    try {
      JSONObject jsonEvent = new JSONObject();

      JSONArray jsonRows = new JSONArray();
      for (ITableRow row : event.getRows()) {
        JSONObject jsonRow = tableRowToJson(row);
        jsonRows.put(jsonRow);
      }
      jsonEvent.put("rows", jsonRows);

      getJsonSession().currentJsonResponse().addActionEvent("rowsInserted", getId(), jsonEvent);
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  protected void handleModelRowsDeleted(Collection<ITableRow> modelRows) {
    try {
      JSONObject jsonEvent = new JSONObject();
      jsonEvent.put(PROP_ROW_IDS, rowIdsToJson(modelRows));

      JSONArray jsonRowIds = new JSONArray();
      for (ITableRow row : modelRows) {
        String rowId = m_tableRowIds.get(row);
        jsonRowIds.put(rowId);

        m_tableRowIds.remove(row);
        m_tableRows.remove(rowId);
      }

      getJsonSession().currentJsonResponse().addActionEvent("rowsDeleted", getId(), jsonEvent);
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  protected void handleModelRowsSelected(Collection<ITableRow> modelRows) {
    try {
      JSONObject jsonEvent = new JSONObject();
      jsonEvent.put(PROP_ROW_IDS, rowIdsToJson(modelRows));
      getJsonSession().currentJsonResponse().addActionEvent(EVENT_ROWS_SELECTED, getId(), jsonEvent);
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  protected void handleModelRowOrderChanged(Collection<ITableRow> modelRows) {
    try {
      JSONObject jsonEvent = new JSONObject();
      jsonEvent.put(PROP_ROW_IDS, rowIdsToJson(modelRows));

      JSONArray jsonRowIds = new JSONArray();
      for (ITableRow row : modelRows) {
        String rowId = m_tableRowIds.get(row);
        jsonRowIds.put(rowId);
      }

      getJsonSession().currentJsonResponse().addActionEvent("rowOrderChanged", getId(), jsonEvent);
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  protected void handleModelSelectionMenusChanged(Collection<ITableRow> modelRows) {
    try {
      JSONObject jsonEvent = new JSONObject();
      jsonEvent.put(PROP_ROW_IDS, rowIdsToJson(modelRows));

      if (m_menuManager.replaceSelectionMenus(fetchMenusForSelection())) {
        jsonEvent.put(PROP_MENUS, m_menuManager.getJsonSelectionMenus());

        getJsonSession().currentJsonResponse().addActionEvent(EVENT_SELECTION_MENUS_CHANGED, getId(), jsonEvent);
      }
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  private class P_ModelTableListener implements TableListener {
    @Override
    public void tableChanged(final TableEvent e) {
      handleModelTableEvent(e);
    }

    @Override
    public void tableChangedBatch(List<? extends TableEvent> events) {
      handleModelTableEventBatch(events);
    }
  }
}
