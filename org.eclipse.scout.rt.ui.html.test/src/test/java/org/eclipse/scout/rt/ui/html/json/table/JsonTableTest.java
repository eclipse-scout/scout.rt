/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table;

import static org.junit.Assert.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.UserTableRowFilter;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonAdapterMock;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.FormTableControl;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.HierarchicalTable;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.ListBoxTable;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWith3Cols;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithNonDisplayableMenu;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithNonDisplayableMenu.DisplayableMenu;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithNonDisplayableMenu.NonDisplayableMenu;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithoutMenus;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonTableTest {
  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Tests whether the model row gets correctly selected
   */
  @Test
  public void testSelectionEvent() throws JSONException {
    Table table = createTableFixture(5);

    assertNull(table.getSelectedRow());

    ITableRow row = table.getRow(2);
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(jsonTable.getOrCreateRowId(row));
    jsonTable.handleUiEvent(event);

    assertTrue(row.isSelected());
  }

  /**
   * Tests whether the model rows get correctly unselected
   */
  @Test
  public void testClearSelectionEvent() throws JSONException {
    Table table = createTableFixture(5);
    ITableRow row1 = table.getRow(1);

    table.selectRow(row1);

    assertTrue(row1.isSelected());

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(null);

    jsonTable.handleUiEvent(event);

    assertEquals(0, table.getSelectedRows().size());
  }

  /**
   * Response must not contain the selection event if the selection was triggered by the request
   */
  @Test
  public void testIgnorableSelectionEvent() throws JSONException {
    Table table = createTableFixture(5);

    ITableRow row = table.getRow(2);
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(jsonTable.getOrCreateRowId(row));
    jsonTable.handleUiEvent(event);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertEquals(0, responseEvents.size());
  }

  /**
   * If the selection event triggers the selection of another row, the selection event must not be ignored.
   */
  @Test
  public void testIgnorableSelectionEvent2() throws JSONException {
    Table table = new Table() {
      @Override
      protected void execRowsSelected(List<? extends ITableRow> rows) {
        selectRow(4);
      }
    };
    table.fill(5);
    table.init();

    ITableRow row2 = table.getRow(2);
    ITableRow row4 = table.getRow(4);

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(jsonTable.getOrCreateRowId(row2));

    assertFalse(row2.isSelected());
    assertFalse(row4.isSelected());

    jsonTable.handleUiEvent(event);

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertEquals(1, responseEvents.size());

    List<ITableRow> tableRows = jsonTable.extractTableRows(responseEvents.get(0).getData());
    assertEquals(row4, tableRows.get(0));
  }

  /**
   * Same as {@link #testIgnorableSelectionEvent2()} but with an empty selection
   */
  @Test
  public void testIgnorableSelectionEvent3() throws JSONException {
    Table table = new Table() {
      @Override
      protected void execRowsSelected(List<? extends ITableRow> rows) {
        if (rows.size() == 0) {
          selectRow(4);
        }
      }
    };
    table.fill(5);
    table.init();

    ITableRow row2 = table.getRow(2);
    ITableRow row4 = table.getRow(4);
    table.selectRow(row2);

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(null);

    assertTrue(row2.isSelected());
    assertFalse(row4.isSelected());

    jsonTable.handleUiEvent(event);

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertEquals(1, responseEvents.size());

    List<ITableRow> tableRows = jsonTable.extractTableRows(responseEvents.get(0).getData());
    assertEquals(row4, tableRows.get(0));
  }

  /**
   * Selection must not be cleared if rowIds cannot be resolved.
   */
  @Test
  public void testIgnorableSelectionEventInconsistentState() throws JSONException {
    Table table = new Table();
    table.fill(5);
    table.init();

    ITableRow row2 = table.getRow(2);
    ITableRow row4 = table.getRow(4);
    table.selectRow(row2);

    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);
    jsonTable.toJson();

    assertTrue(row2.isSelected());
    assertFalse(row4.isSelected());

    // ----------

    // Model selection MUST NOT be cleared when an invalid selection is sent from the UI

    JsonEvent event = createJsonRowsSelectedEvent("not-existing-id");
    jsonTable.handleUiEvent(event);
    jsonTable.cleanUpEventFilters();

    assertTrue(row2.isSelected());
    assertFalse(row4.isSelected());

    // No reply (we assume that the UI state is correct and only the event was wrong, e.g. due to caching)
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertEquals(0, responseEvents.size());
    JsonTestUtility.endRequest(m_uiSession);

    // ----------

    // Model selection MUST be cleared when an empty selection is sent from the UI

    event = createJsonRowsSelectedEvent(null);
    jsonTable.handleUiEvent(event);
    jsonTable.cleanUpEventFilters();

    assertFalse(row2.isSelected());
    assertFalse(row4.isSelected());

    // No reply (states should be equal)
    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertEquals(0, responseEvents.size());
    JsonTestUtility.endRequest(m_uiSession);

    // ----------

    // Model selection MUST be updated when a partially invalid selection is sent from the UI

    event = createJsonRowsSelectedEvent("not-existing-id");
    event.getData().getJSONArray(JsonTable.PROP_ROW_IDS).put(jsonTable.getTableRowId(row4));
    jsonTable.handleUiEvent(event);
    jsonTable.cleanUpEventFilters();

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    // Inform the UI about the change
    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertEquals(1, responseEvents.size());
    List<ITableRow> tableRows = jsonTable.extractTableRows(responseEvents.get(0).getData());
    assertEquals(row4, tableRows.get(0));
    JsonTestUtility.endRequest(m_uiSession);
  }

  /**
   * Response must not contain the row_order_changed event if the sort was triggered by the request
   */
  @Test
  public void testIgnorableRowOrderChangedEvent() throws JSONException {
    Table table = createTableFixture(5);

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();
    IColumn column = table.getColumnSet().getFirstVisibleColumn();

    // ----------

    JsonEvent event = createJsonRowsSortedEvent(jsonTable.getColumnId(column), !column.isSortAscending());
    jsonTable.handleUiEvent(event);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), "rowOrderChanged");
    assertEquals(0, responseEvents.size());
  }

  @Test
  public void testColumnOrderChangedEvent() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    table.getColumns().forEach(c -> c.setVisible(true));

    IColumn<?> column0 = table.getColumns().get(0);
    IColumn<?> column1 = table.getColumns().get(1);
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    assertEquals(table.getColumnSet().getVisibleColumn(0), column0);
    assertEquals(table.getColumnSet().getVisibleColumn(1), column1);

    JsonEvent event = createJsonColumnMovedEvent(jsonTable.getColumnId(column0), 2);
    jsonTable.handleUiEvent(event);

    assertEquals(table.getColumnSet().getVisibleColumn(2), column0);

    event = createJsonColumnMovedEvent(jsonTable.getColumnId(column1), 0);
    jsonTable.handleUiEvent(event);

    assertEquals(table.getColumnSet().getVisibleColumn(0), column1);
  }

  @Test
  public void testColumnOrderChangedEvent_assertCellOrder() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();

    IColumn<?> column0 = table.getColumns().get(0);
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    JSONObject jsonRow = jsonTable.tableRowToJson(table.getRow(0));
    JSONArray jsonCells = (JSONArray) jsonRow.get("cells");

    JsonEvent event = createJsonColumnMovedEvent(jsonTable.getColumnId(column0), 2);
    jsonTable.handleUiEvent(event);

    JSONObject jsonRowAfterMoving = jsonTable.tableRowToJson(table.getRow(0));
    JSONArray jsonCellsAfterMoving = (JSONArray) jsonRowAfterMoving.get("cells");

    // Expect same cell order, even if the columns are moved
    for (int i = 0; i < jsonCellsAfterMoving.length(); i++) {
      assertEquals(jsonCells.get(i), jsonCellsAfterMoving.get(i));
    }
  }

  /**
   * Response must not contain the column order changed event if the event was triggered by the request and the order
   * hasn't changed
   */
  @Test
  public void testIgnorableColumnOrderChangedEvent() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();

    IColumn<?> column = table.getColumns().get(0);
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonColumnMovedEvent(jsonTable.getColumnId(column), 2);
    jsonTable.handleUiEvent(event);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), "columnOrderChanged");
    assertEquals(0, responseEvents.size());
  }

  /**
   * Sends header update event if header cell has changed, but only for visible columns.
   */
  @Test
  public void testColumnHeadersUpdatedEvent() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    table.getColumnSet().getColumn(0).setDisplayable(false);

    IColumn<?> column0 = table.getColumns().get(0);
    IColumn<?> column1 = table.getColumns().get(1);
    IJsonAdapter<? super TableWith3Cols> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    column0.getHeaderCell().setText("newHeaderText");
    table.getColumnSet().updateColumn(column0);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_COLUMN_HEADERS_UPDATED);
    assertEquals(0, responseEvents.size());

    column1.getHeaderCell().setText("newHeaderText2");
    table.getColumnSet().updateColumn(column1);

    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_COLUMN_HEADERS_UPDATED);
    assertEquals(1, responseEvents.size());
  }

  /**
   * If column structure changes, we need to resend every row including its new cells.
   */
  @Test
  public void testColumnStructureChangedEvent() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    table.getColumnSet().getColumn(0).setVisible(false);

    IJsonAdapter<? super TableWith3Cols> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----

    table.getColumnSet().getColumn(0).setVisible(true);

    JsonResponse response = m_uiSession.currentJsonResponse();
    response.fireProcessBufferedEvents();
    List<JsonEvent> events = response.getEventList();
    assertEquals(4, events.size());
    assertEquals(JsonTable.EVENT_COLUMN_STRUCTURE_CHANGED, events.get(0).getType());
    assertEquals(JsonEventType.PROPERTY.getEventType(), events.get(1).getType());
    assertEquals(JsonTable.EVENT_ALL_ROWS_DELETED, events.get(2).getType());
    assertEquals(JsonTable.EVENT_ROWS_INSERTED, events.get(3).getType());
  }

  /**
   * If column structure changes, old columns are disposed and the new ones attached
   */
  @Test
  public void testColumnStructureChangedEvent_dispose() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();

    IColumn column0 = table.getColumnSet().getColumn(0);
    IColumn column1 = table.getColumnSet().getColumn(1);
    IColumn column2 = table.getColumnSet().getColumn(2);
    column0.setVisible(false);

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    String column0Id = jsonTable.getColumnId(column0);
    String column1Id = jsonTable.getColumnId(column1);
    String column2Id = jsonTable.getColumnId(column2);
    assertNull(column0Id);
    assertNotNull(column1Id);
    assertNotNull(column2Id);

    column0.setVisible(true);
    column2.setVisible(false);

    JsonResponse response = m_uiSession.currentJsonResponse();
    response.fireProcessBufferedEvents();

    String newColumn0Id = jsonTable.getColumnId(column0);
    String newColumn1Id = jsonTable.getColumnId(column1);
    String newColumn2Id = jsonTable.getColumnId(column2);
    assertNotNull(newColumn0Id);
    assertNotNull(newColumn1Id);
    Assert.assertNotEquals(column1Id, newColumn1Id);
    assertNull(newColumn2Id);
  }

  /**
   * Tests whether non displayable menus are sent.
   * <p>
   * This reduces response size and also leverages security because the menus are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNonDisplayableMenus() {
    TableWithNonDisplayableMenu table = new TableWithNonDisplayableMenu();
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    JsonMenu<IMenu> jsonDisplayableMenu = jsonTable.getAdapter(table.getMenuByClass(TableWithNonDisplayableMenu.DisplayableMenu.class));
    JsonMenu<IMenu> jsonNonDisplayableMenu = jsonTable.getAdapter(table.getMenuByClass(TableWithNonDisplayableMenu.NonDisplayableMenu.class));

    // Adapter for NonDisplayableMenu must not exist
    assertNull(jsonNonDisplayableMenu);

    // Json response must not contain NonDisplayableMenu
    JSONObject json = jsonTable.toJson();
    JSONArray jsonMenus = json.getJSONArray("menus");
    assertEquals(1, jsonMenus.length());
    assertEquals(jsonDisplayableMenu.getId(), jsonMenus.get(0));
  }

  @Test
  public void testMenuDisposalOnPropertyChange() throws JSONException {
    ITable table = new TableWithoutMenus();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    Menu menu1 = new Menu();
    table.getContextMenu().addChildAction(menu1);
    assertNotNull(jsonTable.getAdapter(menu1));
    assertTrue(jsonTable.getAdapter(menu1).isInitialized());

    table.getContextMenu().removeChildAction(menu1);
    assertNull(jsonTable.getAdapter(menu1));
  }

  /**
   * Tests whether it is possible to dispose (or replace) menus if at least one menu is not displayable.<br>
   */
  @Test
  public void testMenuDisposalOnPropertyChangeWithNonDisplayableMenu() throws JSONException {
    ITable table = new TableWithNonDisplayableMenu();
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    DisplayableMenu displayableMenu = table.getMenuByClass(TableWithNonDisplayableMenu.DisplayableMenu.class);
    NonDisplayableMenu NonDisplayableMenu = table.getMenuByClass(TableWithNonDisplayableMenu.NonDisplayableMenu.class);
    assertNull(jsonTable.getAdapter(NonDisplayableMenu));
    assertNotNull(jsonTable.getAdapter(displayableMenu));
    assertTrue(jsonTable.getAdapter(displayableMenu).isInitialized());

    table.getContextMenu().removeChildAction(NonDisplayableMenu);
    table.getContextMenu().removeChildAction(displayableMenu);

    assertNull(jsonTable.getAdapter(NonDisplayableMenu));
    assertNull(jsonTable.getAdapter(displayableMenu));
  }

  @Test
  public void testTableControlDisposalOnPropertyChange() throws JSONException {
    ITable table = new TableWithoutMenus();
    table.init();
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    FormTableControl control = new FormTableControl();
    table.addTableControl(control);
    assertNotNull(jsonTable.getAdapter(control));
    assertTrue(jsonTable.getAdapter(control).isInitialized());

    table.removeTableControl(control);
    assertNull(jsonTable.getAdapter(control));
  }

  @Test
  public void testMultipleTableControlDisposallOnPropertyChange() throws JSONException {
    ITable table = new TableWithoutMenus();
    table.init();
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // ----------

    FormTableControl tableControl1 = new FormTableControl();
    FormTableControl tableControl2 = new FormTableControl();
    table.addTableControl(tableControl1);
    table.addTableControl(tableControl2);
    assertNotNull(jsonTable.getAdapter(tableControl1));
    assertTrue(jsonTable.getAdapter(tableControl1).isInitialized());
    assertNotNull(jsonTable.getAdapter(tableControl2));
    assertTrue(jsonTable.getAdapter(tableControl2).isInitialized());

    table.removeTableControl(tableControl1);
    assertNull(jsonTable.getAdapter(tableControl1));
    assertNotNull(jsonTable.getAdapter(tableControl2));
    assertTrue(jsonTable.getAdapter(tableControl2).isInitialized());
  }

  /**
   * {@link AbstractStringColumn} considers the locale when sorting. This test is used to compare the results of the
   * java sorting algorithm with the javascript sorting algorithm.
   *
   * @see "TableSpec.js"
   */
  @Test
  public void testTextualSortingWithCollator() throws JSONException {
    String[] data = {"Österreich", "Italien", "Zypern"};

    Arrays.sort(data, new TextSortComparator(true, new Locale("de")));
    Assert.assertArrayEquals(new String[]{"Italien", "Österreich", "Zypern"}, data);

    Arrays.sort(data, new TextSortComparator(false, new Locale("de")));
    Assert.assertArrayEquals(new String[]{"Zypern", "Österreich", "Italien"}, data);

    Arrays.sort(data, new TextSortComparator(true, new Locale("sv")));
    Assert.assertArrayEquals(new String[]{"Italien", "Zypern", "Österreich"}, data);

    Arrays.sort(data, new TextSortComparator(false, new Locale("sv")));
    Assert.assertArrayEquals(new String[]{"Österreich", "Zypern", "Italien"}, data);
  }

  private class TextSortComparator implements Comparator<String> {
    private boolean m_asc;
    private Locale m_locale;

    public TextSortComparator(boolean asc, Locale locale) {
      m_asc = asc;
      m_locale = locale;
    }

    @Override
    public int compare(String a, String b) {
      int result = StringUtility.compareIgnoreCase(m_locale, a, b);
      if (!m_asc) {
        result = -result;
      }
      return result;
    }
  }

  @SuppressWarnings("UnusedAssignment")
  @Test
  public void testDispose() {
    Table table = new Table();
    JsonTable<ITable> object = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());

    WeakReference<JsonTable<?>> ref = new WeakReference<>(object);
    object.dispose();
    m_uiSession = null;
    object = null;
    TestingUtility.assertGC(ref);
  }

  @Test
  public void testRowFilter() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(3);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    ITableRow row0 = table.getRow(0);
    ITableRow row1 = table.getRow(1);
    ITableRow row2 = table.getRow(2);

    jsonTable.toJson();
    assertNotNull(jsonTable.tableRowIdsMap().get(row0));
    assertNotNull(jsonTable.tableRowIdsMap().get(row1));
    assertNotNull(jsonTable.tableRowIdsMap().get(row2));

    String row0Id = jsonTable.getOrCreateRowId(row0);
    String row1Id = jsonTable.getOrCreateRowId(row1);
    assertNotNull(row0Id);
    assertNotNull(jsonTable.getTableRow(row0Id));
    assertNotNull(row1Id);
    assertNotNull(jsonTable.getTableRow(row1Id));

    table.addRowFilter(r -> {
      return r.getRowIndex() > 0; // hide first row
    });

    // After flushing the event buffers and applying the model changes
    // to the JsonTable, the row should not exist anymore on the JsonTable
    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(3, table.getRowCount());
    assertEquals(2, table.getFilteredRowCount());
    assertNull(jsonTable.tableRowIdsMap().get(row0));
    assertNotNull(jsonTable.tableRowIdsMap().get(row1));
    assertNotNull(jsonTable.tableRowIdsMap().get(row2));

    jsonTable.getTableRow(row1Id); // should still exist -> should NOT throw an exception
    try {
      jsonTable.getTableRow(row0Id); // throws exception
      fail("Expected an exception, but no exception was thrown");
    }
    catch (UiException e) {
      // ok
    }
  }

  /**
   * Usecase:
   * <p>
   * 1. Add filter to table<br>
   * 2. Remove same filter so that no rows are removed<br>
   * Assert that no events are generated, especially no rowsDeleted event
   */
  @Test
  public void testRowFilter_nop() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(3);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    ITableRow row0 = table.getRow(0);
    ITableRow row1 = table.getRow(1);
    ITableRow row2 = table.getRow(2);

    jsonTable.toJson();
    assertNotNull(jsonTable.tableRowIdsMap().get(row0));
    assertNotNull(jsonTable.tableRowIdsMap().get(row1));
    assertNotNull(jsonTable.tableRowIdsMap().get(row2));

    ITableRowFilter filter = r -> {
      return r.getRowIndex() > 0; // hide first row
    };
    table.addRowFilter(filter);
    assertEquals(2, table.getFilteredRowCount());

    // Remove the just added filter -> Must not create any request
    table.removeRowFilter(filter);
    assertEquals(3, table.getFilteredRowCount());

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertNotNull(jsonTable.tableRowIdsMap().get(row1));
    assertNotNull(jsonTable.tableRowIdsMap().get(row1));
    assertNotNull(jsonTable.tableRowIdsMap().get(row2));
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
  }

  @Test
  public void testAddRowFilterAfterUpdates() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(3);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    // Simulate that the full table is sent to the UI
    jsonTable.toJson();
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // Now filter the first row
    ITableRow row = table.getRow(0);
    String row0Id = jsonTable.getOrCreateRowId(row);
    assertNotNull(row0Id);
    assertNotNull(jsonTable.getTableRow(row0Id));

    table.addRowFilter(r -> {
      return r.getRowIndex() > 0; // hide first row
    });

    // Update the (now hidden) row --> should not trigger an update event, because the row does not exist in the UI
    row.getCellForUpdate(0).setValue("Updated text");

    // We expect the first row to be removed from the table, and no update event!
    assertEquals(3, table.getRowCount());
    assertEquals(2, table.getFilteredRowCount());
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(2, jsonTable.eventBuffer().size()); // contains row_filter_changed and rows_updated

    // Filtering is implemented by Only one deletion event should be emitted (no update event!)
    JsonTestUtility.processBufferedEvents(m_uiSession); // Conversion of rowFilterChanged event happens here -> // TYPE_ROW_FILTER_CHANGED + TYPE_ROWS_UPDATED = TYPE_ROWS_DELETED
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("rowsDeleted", m_uiSession.currentJsonResponse().getEventList().get(0).getType());
  }

  @Test
  public void testRemoveRowFilterAfterUpdates() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(3);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    // Filter the first row
    ITableRowFilter filter = r -> {
      return r.getRowIndex() > 0; // hide first row
    };
    table.addRowFilter(filter);
    assertEquals(3, table.getRowCount());
    assertEquals(2, table.getFilteredRowCount());

    // Simulate that the full table is sent to the UI
    jsonTable.toJson();
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // Hidden row must not be sent to ui -> no row id
    ITableRow row = table.getRow(0);
    assertNull(jsonTable.getTableRowId(row));

    // Update the hidden row
    row.getCellForUpdate(0).setValue("Updated text");

    // Remove filter -> Insert event is generated, inserted row is removed from update event in JsonTable.preprocessBufferedEvents
    table.removeRowFilter(filter);
    assertEquals(3, table.getFilteredRowCount());
    JsonTestUtility.processBufferedEvents(m_uiSession);

    // Filtering is implemented by Only one deletion event should be emitted (no update event!)
    List<JsonEvent> eventList = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(2, eventList.size());
    assertEquals("rowsInserted", eventList.get(0).getType());
    assertEquals("rowOrderChanged", eventList.get(1).getType());
    JsonEvent jsonEvent = eventList.get(0);
    assertEquals(1, jsonEvent.getData().getJSONArray("rows").length());
    assertEquals(jsonTable.getTableRowId(row), jsonEvent.getData().getJSONArray("rows").getJSONObject(0).get("id"));
  }

  /**
   * {@link JsonTable#preprocessBufferedEvents()} converts some {@link TableEvent#TYPE_ROW_FILTER_CHANGED} to artificial
   * {@link TableEvent#TYPE_ROWS_INSERTED}, this conversion must not insert one row twice (e.g. if the same row is
   * inserted afterwards by a real {@link TableEvent#TYPE_ROWS_INSERTED} event).
   */
  @Test
  public void testRowFilterMustNotConvertToDuplicateInserts() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(1);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    // Filter the first row
    ITableRowFilter filter = r -> {
      return r.getRowIndex() > 0; // hide first row
    };
    table.addRowFilter(filter);
    assertEquals(1, table.getRowCount());
    assertEquals(0, table.getFilteredRowCount());

    // Simulate that the full table is sent to the UI
    jsonTable.toJson();
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // Hidden row must not be sent to ui -> no row id
    ITableRow row = table.getRow(0);
    assertNull(jsonTable.getTableRowId(row));

    // Update the hidden row
    row.getCellForUpdate(0).setValue("Updated text");

    // Remove filter -> Insert event is generated, inserted row is removed from update event in JsonTable.preprocessBufferedEvents
    table.removeRowFilter(filter);
    assertEquals(1, table.getFilteredRowCount());

    // Create yet another event (between the filtering and the next row insert event)
    table.updateAllRows();

    // Add yet another row (not yet available at time of filtering)
    table.fill(1, false);

    // Process events
    JsonTestUtility.processBufferedEvents(m_uiSession);

    // Filtering is implemented by Only one deletion event should be emitted (no update event!)
    assertEquals(3, m_uiSession.currentJsonResponse().getEventList().size());
    List<JsonEvent> eventList = m_uiSession.currentJsonResponse().getEventList();
    Set<String> insertedRowIds = new HashSet<>();
    for (JsonEvent e : eventList) {
      if ("rowsInserted".equals(e.getType())) {
        JSONArray rows = e.getData().getJSONArray("rows");
        for (int i = 0; i < rows.length(); i++) {
          JSONObject r = rows.getJSONObject(i);
          String id = r.getString("id");
          assertTrue("Row " + id + " contained in more than one insert event", insertedRowIds.add(id));
        }
      }
    }
    assertTrue(insertedRowIds.contains(jsonTable.getTableRowId(table.getRow(0)))); // converted TYPE_ROW_FILTER_CHANGED event
    assertTrue(insertedRowIds.contains(jsonTable.getTableRowId(table.getRow(1)))); // actual new row inserted
  }

  /**
   * If the rows are filtered using {@link UserTableRowFilter}, the rows must not be deleted from json table and no
   * delete event must be sent -> gui knows which rows are filtered and keeps the invisible rows in memory
   */
  @Test
  public void testUserRowFilter() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(3);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    ITableRow row0 = table.getRow(0);
    ITableRow row2 = table.getRow(2);

    jsonTable.toJson();

    String row0Id = jsonTable.getOrCreateRowId(row0);
    String row2Id = jsonTable.getOrCreateRowId(row2);

    JsonEvent event = createJsonRowsFilteredEvent(row0Id, row2Id);
    jsonTable.handleUiEvent(event);

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(3, table.getRowCount());
    assertEquals(2, table.getFilteredRowCount());
    assertEquals(3, jsonTable.tableRowIdsMap().size());

    // expect that NO delete event is sent
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_DELETED);
    assertEquals(0, responseEvents.size());
  }

  /**
   * If toJson is called and a user filter is active, every row must be returned and not only the filtered ones
   */
  @Test
  public void testUserRowFilter_toJson() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(3);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    ITableRow row0 = table.getRow(0);
    ITableRow row2 = table.getRow(2);

    List<ITableRow> filteredRows = new ArrayList<>();
    filteredRows.add(row0);
    filteredRows.add(row2);
    table.getUIFacade().setFilteredRowsFromUI(filteredRows);

    jsonTable.toJson();

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(3, table.getRowCount());
    assertEquals(2, table.getFilteredRowCount());
    assertEquals(3, jsonTable.tableRowIdsMap().size());
  }

  @Test
  public void testUserRowFilter_UpdateEvent() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(3);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    // Simulate that the full table is sent to the UI
    jsonTable.toJson();
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // Now reject the first row
    ITableRow row0 = table.getRow(0);
    ITableRow row1 = table.getRow(1);
    ITableRow row2 = table.getRow(2);
    String row0Id = jsonTable.getOrCreateRowId(row0);
    String row1Id = jsonTable.getOrCreateRowId(row1);
    String row2Id = jsonTable.getOrCreateRowId(row2);
    assertNotNull(row0Id);
    assertNotNull(jsonTable.getTableRow(row0Id));

    table.addRowFilter(r -> {
      return r.getRowIndex() > 0; // hide first row
    });

    // and reject the third row by the user row filter
    JsonEvent event = createJsonRowsFilteredEvent(row0Id, row1Id);
    jsonTable.handleUiEvent(event);

    // Update the (now hidden) row 0 --> should not trigger an update event, because the row does not exist in the UI
    row0.getCellForUpdate(0).setValue("Updated text");
    row2.getCellForUpdate(0).setValue("Updated text");

    // We expect the first row to be removed from the table, but not the third row
    assertEquals(3, table.getRowCount());
    assertEquals(1, table.getFilteredRowCount());
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    //TODO [7.0] bsh: CGU delete ? assertEquals(6, jsonTable.eventBuffer().size()); // TYPE_ROW_FILTER_CHANGED + TYPE_ROWS_UPDATED = TYPE_ROWS_DELETED + TYPE_ROWS_INSERTED + TYPE_ROWS_UPDATED (row0) + TYPE_ROWS_UPDATED (row2)

    // Rejection of row 0 generates a deleted event, rejection of row 2 an update event
    JsonTestUtility.processBufferedEvents(m_uiSession);
    List<JsonEvent> eventList = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(2, eventList.size());
    JsonEvent jsonEvent = eventList.get(0);
    assertEquals("rowsDeleted", jsonEvent.getType());
    assertEquals(1, jsonEvent.getData().getJSONArray(JsonTable.PROP_ROW_IDS).length());
    assertEquals(row0Id, jsonEvent.getData().getJSONArray(JsonTable.PROP_ROW_IDS).get(0));
    jsonEvent = eventList.get(1);
    assertEquals("rowsUpdated", jsonEvent.getType());
    assertEquals(1, jsonEvent.getData().getJSONArray("rows").length());
    assertEquals(row2Id, ((JSONObject) jsonEvent.getData().getJSONArray("rows").get(0)).getString("id"));
  }

  /**
   * If the rows are filtered using {@link UserTableRowFilter}, the rows must not be deleted from json table and no
   * delete event must be sent, EXCEPT if the row is filtered by a model based row filter. In that case rows must be
   * deleted in the ui.
   */
  @Test
  public void testUserRowFilter_AndAnotherRowFilter() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(3);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    ITableRow row0 = table.getRow(0);
    ITableRow row2 = table.getRow(2);

    jsonTable.toJson();

    String row0Id = jsonTable.getOrCreateRowId(row0);
    String row2Id = jsonTable.getOrCreateRowId(row2);

    table.addRowFilter(r -> {
      return r.getRowIndex() > 0; // hide first row
    });

    JsonEvent event = createJsonRowsFilteredEvent(row0Id, row2Id);
    jsonTable.handleUiEvent(event);

    // In the model, 3 rows exist, but only 1 is visible (accepted by filters), in the ui 2 rows exist and 1 is visible
    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(3, table.getRowCount());
    assertEquals(1, table.getFilteredRowCount());
    assertEquals(2, jsonTable.tableRowIdsMap().size());

    // expect that first row gets deleted because a "real" row filter does not accept the first row
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_DELETED);
    assertEquals(1, responseEvents.size());
  }

  /**
   * JsonTable generates an allRowsDeleted event if a row is deleted and filteredRowCount is 0. This must not happen if
   * the row has been filtered by the user.
   */
  @Test
  public void testUserRowFilter_preventAllRowsDeleted() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(2);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    ITableRow row0 = table.getRow(0);

    jsonTable.toJson();

    String row0Id = jsonTable.getOrCreateRowId(row0);

    JsonEvent event = createJsonRowsFilteredEvent(row0Id);
    jsonTable.handleUiEvent(event);

    table.deleteRow(row0);

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(1, table.getRowCount());
    assertEquals(0, table.getFilteredRowCount());
    assertEquals(1, jsonTable.tableRowIdsMap().size());

    // expect that NO deleteAll event is sent
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ALL_ROWS_DELETED);
    assertEquals(0, responseEvents.size());
  }

  /**
   * JsonTable generates an allRowsDeleted event if a row is deleted and filteredRowCount is 0. This happens only if a
   * filter is active, otherwise table generates a all rows deleted event by itself
   */
  @Test
  public void testAllRowsDeleted_whenFilterActive() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(2);
    table.init();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    ITableRow row0 = table.getRow(0);

    jsonTable.toJson();

    table.addRowFilter(r -> {
      return r.getRowIndex() == 0; // hide second row
    });

    table.deleteRow(row0);

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(1, table.getRowCount());
    assertEquals(0, table.getFilteredRowCount());
    assertEquals(0, jsonTable.tableRowIdsMap().size());

    // expect that deleteAll event is sent
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ALL_ROWS_DELETED);
    assertEquals(1, responseEvents.size());
  }

  /**
   * Tests that multiple model events are coalseced in JSON layer
   */
  @Test
  public void testTableEventCoalesceInUi_TwoRowsAdded() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    // Response should contain no events
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    // Add two rows sequentially --> this should trigger two TableEvents
    table.addRowsByMatrix(new Object[]{new Object[]{"NewCell_0", "NewCell_1", "NewCell_2"}});
    table.addRowsByMatrix(new Object[]{new Object[]{"AnotherNewCell_0", "AnotherNewCell_1", "AnotherNewCell_2"}});

    // Events should not yet be in the response
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    // But they should be in the event buffer
    assertEquals(2, jsonTable.eventBuffer().size());
    // When converting to JSON, the event buffer should be cleared and the events should
    // be coalesced and written to the response. -->  Only one insert event (with two rows)
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    assertEquals(0, jsonTable.eventBuffer().size());
    JSONArray events = response.getJSONArray("events");
    assertEquals(1, events.length());
    assertEquals(2, events.getJSONObject(0).getJSONArray("rows").length());
  }

  /**
   * Tests that multiple model events are coalseced in JSON layer
   */
  @Test
  public void testTableEventCoalesceInUi_RowInsertedAndUpdated() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    // Response should contain no events
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    // Add one row, then update it --> this should trigger two TableEvents
    List<ITableRow> newRows = table.addRowsByMatrix(new Object[]{new Object[]{"NewCell_0", "NewCell_1", "NewCell_2"}});
    newRows.get(0).getCellForUpdate(0).setValue("UPDATED");

    // Events should not yet be in the response
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    // But they should be in the event buffer
    assertEquals(2, jsonTable.eventBuffer().size());
    // When converting to JSON, the event buffer should be cleared and the events should
    // be coalesced and written to the response. --> Update should be merged with inserted
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    assertEquals(0, jsonTable.eventBuffer().size());
    JSONArray events = response.getJSONArray("events");
    assertEquals(1, events.length());
    assertEquals(1, events.getJSONObject(0).getJSONArray("rows").length());
    assertEquals("UPDATED", events.getJSONObject(0).getJSONArray("rows").getJSONObject(0).getJSONArray("cells").getString(0));
  }

  /**
   * Tests that multiple model events are coalseced in JSON layer
   */
  @Test
  public void testTableEventCoalesceInUi_RowInsertedAndDeleted() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());

    // Response should contain no events
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    // Add one row, then delete it --> this should trigger two TableEvents
    List<ITableRow> newRows = table.addRowsByMatrix(new Object[]{new Object[]{"NewCell_0", "NewCell_1", "NewCell_2"}});
    table.discardRows(newRows);

    // Events should not yet be in the response
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    // But they should be in the event buffer
    assertEquals(2, jsonTable.eventBuffer().size());
    // When converting to JSON, the event buffer should be cleared and the events should
    // be coalesced and written to the response. --> Both events should cancel each other
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    assertEquals(0, jsonTable.eventBuffer().size());
    JSONArray events = response.optJSONArray("events");
    assertNull(events);
  }

  /**
   * Tests that multiple model events are coalesced in JSON layer
   */
  @Test
  public void testTableEventCoalesceInUi_InsertAndRowOrderChanged() {
    TableWith3Cols table = new TableWith3Cols();
    table.init();
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    table.fill(2, false);
    table.fill(3, false);
    table.getColumnSet().setSortColumn(table.getColumns().get(1), false);
    table.fill(2, false);
    table.getColumnSet().setSortColumn(table.getColumns().get(1), true);

    // Apply event buffer --> only one insertion (with the correct order and a COLUMN_HEADERS_UPDATED event should remain)
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    assertEquals(0, jsonTable.eventBuffer().size());
    JSONArray events = response.optJSONArray("events");
    assertNotNull(events);
    assertEquals(2, events.length());
  }

  @Test
  public void testTableEventCoalesceInUi_UpdateEventOnFilteredRow() {
    TableWith3Cols table = new TableWith3Cols();
    table.init();
    table.fill(1, false);
    table.resetColumns();
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTable);
    m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);

    // -------------

    table.fill(2, false);
    table.getRow(2).setChecked(true); // would normally trigger an event, but we filter the row in the next step
    table.addRowFilter(r -> {
      return r.getRowIndex() == 0; // hide everything expect the first (already existing row)
    });

    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    assertEquals(0, jsonTable.eventBuffer().size());
    JSONArray events = response.optJSONArray("events");
    assertNull(events); // No events should be emitted
  }

  @Test
  public void testTableEventCoalesceInUi_DeleteEventOnFilteredRow() {
    TableWith3Cols table = new TableWith3Cols();
    table.init();
    table.fill(1, false);
    table.resetColumns();
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTable);
    m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);

    // -------------

    table.fill(2, false);
    table.addRowFilter(r -> {
      return false; // filter all rows
    });

    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);
    JSONArray events = response.optJSONArray("events");
    assertEquals(1, events.length());
    assertEquals("allRowsDeleted", events.getJSONObject(0).getString("type"));

    // -------------

    table.deleteRow(0); // should not trigger any events

    response = m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);
    events = response.optJSONArray("events");
    assertNull(events); // No events should be emitted
  }

  @Test
  public void testTableEventCoalesceInUi_ReplaceRows() {

    ListBoxTable listbox = new ListBoxTable();
    ILookupRow<Long> row1 = new LookupRow<>(1L, "Row1");
    ILookupRow<Long> row2 = new LookupRow<>(2L, "Row2").withActive(false);
    ILookupRow<Long> row3 = new LookupRow<>(3L, "Row3");
    ILookupRow<Long> row4 = new LookupRow<>(4L, "Row4");

    ArrayList<ITableRow> rowsInitial = new ArrayList<>();

    rowsInitial.add(listbox.getTableRowBuilder().createTableRow(row1));
    rowsInitial.add(listbox.getTableRowBuilder().createTableRow(row2));
    rowsInitial.add(listbox.getTableRowBuilder().createTableRow(row3));

    ITable table = listbox.getTable();
    table.addRows(rowsInitial);

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTable);
    m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);

    ArrayList<ITableRow> rowsUpdate = new ArrayList<>();
    rowsUpdate.add(listbox.getTableRowBuilder().createTableRow(row1));
    rowsUpdate.add(listbox.getTableRowBuilder().createTableRow(row2));
    rowsUpdate.add(listbox.getTableRowBuilder().createTableRow(row3));
    rowsUpdate.add(listbox.getTableRowBuilder().createTableRow(row4));

    table.replaceRows(rowsUpdate);

    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);
    JSONArray events = response.optJSONArray("events");
    JSONObject lastEvent = events.optJSONObject(events.length() - 1);
    assertEquals(lastEvent.optString("type"), "rowOrderChanged");
    JSONArray rowIds = lastEvent.optJSONArray("rowIds");
    assertEquals(3, rowIds.length());
  }

  @Test
  public void testDeleteAfterMove() {
    TableWith3Cols table = new TableWith3Cols();
    table.init();
    table.fill(3, false);
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTable);
    m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);
    String row0Id = jsonTable.getTableRowId(table.getRow(0));
    String row1Id = jsonTable.getTableRowId(table.getRow(1));
    String row2Id = jsonTable.getTableRowId(table.getRow(2));

    table.moveRow(0, 2);
    table.deleteRow(0);

    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);
    JSONArray events = response.optJSONArray("events");
    assertEquals(2, events.length());
    JSONObject event0 = events.getJSONObject(0);
    JSONObject event1 = events.getJSONObject(1);

    assertEquals("rowOrderChanged", event0.getString("type"));
    JSONArray rowIds = event0.optJSONArray("rowIds");
    assertEquals(3, rowIds.length());
    assertEquals(row2Id, rowIds.get(0));
    assertEquals(row0Id, rowIds.get(1));
    assertEquals(row1Id, rowIds.get(2)); // <-- this is not correct but since it will be deleted it is fine
    assertEquals("rowsDeleted", event1.getString("type"));
  }

  /**
   * Use case: Insert, addFilter, removeFilter, delete row of an already inserted row.
   * <p>
   * This exotic case will generate a row order changed event with not enough rows because at the time the row order
   * change event is generated (in JsonTable.preprocessBufferedEvents), getTable().getRows() returns the already
   * modified list where the row is already deleted. This is the difference to the test above, where the deletion really
   * happens after the row order change.
   */
  @Test
  public void testDeleteAfterInsertAndFilterNop() {
    TableWith3Cols table = new TableWith3Cols();
    table.init();
    table.fill(2, false);
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTable);
    m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);
    final ITableRow row0 = table.getRow(0);
    String row0Id = jsonTable.getTableRowId(table.getRow(0));
    String row1Id = jsonTable.getTableRowId(table.getRow(1));

    ITableRow newRow0 = table.addRow();
    ITableRow newRow1 = table.addRow();
    table.addRowFilter(r -> r == row0);
    // Remove filter again -> NOP
    table.removeRowFilter(table.getRowFilters().get(0));
    // delete the first row -> this will "destroy" the row order changed event
    table.deleteRow(0);

    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);
    JSONArray events = response.optJSONArray("events");
    assertEquals(3, events.length());
    JSONObject event0 = events.getJSONObject(0);
    JSONObject event1 = events.getJSONObject(1);
    JSONObject event2 = events.getJSONObject(2);

    assertEquals("rowsInserted", event0.getString("type"));
    assertEquals("rowOrderChanged", event1.getString("type"));
    JSONArray rowIds = event1.optJSONArray("rowIds");
    assertEquals(4, rowIds.length());
    assertEquals(row1Id, rowIds.get(0));
    assertEquals(jsonTable.getTableRowId(newRow0), rowIds.get(1));
    assertEquals(jsonTable.getTableRowId(newRow1), rowIds.get(2));
    assertEquals(row0Id, rowIds.get(3)); // <-- this is not correct but since it will be deleted it is fine
    assertEquals("rowsDeleted", event2.getString("type"));
  }

  /**
   * Tests if a RequestFocusInCell-Event get discarded when now row is set/available in the Event
   */
  @Test
  public void testRequestFocusInCellRowRequired() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());

    // Response should contain no events
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    // Request Focus without a row
    table.requestFocusInCell(table.getColumns().get(0), null);

    // Events should not be in the response
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    // And also not in the event buffer
    assertEquals(0, jsonTable.eventBuffer().size());

    // So there should be no events at all in the response
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    assertNull(response.optJSONArray("events"));
  }

  /**
   * Tests if a RequestFocusInCell-Event gets discarded when all rows of the table get deleted
   */
  @Test
  public void testRequestFocusInCellCoalesce() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());

    // Response should contain no events
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    // Request Focus without a row
    table.requestFocusInCell(table.getColumns().get(0), table.getRow(0));

    table.deleteAllRows();

    // Events should not yet be in the response
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    // And there should only one delete event
    assertEquals(1, jsonTable.eventBuffer().size());
    assertEquals(TableEvent.TYPE_ALL_ROWS_DELETED, jsonTable.eventBuffer().getBufferInternal().get(0).getType());
  }

  /**
   * Tests if a RequestFocusInCell-Event gets discarded when all rows of the table get deleted and the table has
   * setAutoDiscardOnDelete set to true
   */
  @Test
  public void testRequestFocusInCellCoalesceWithtAutoDiscardOnDelete() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.setAutoDiscardOnDelete(true);
    table.resetColumns();
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());

    // Response should contain no events
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    // Request Focus without a row
    table.requestFocusInCell(table.getColumns().get(0), table.getRow(0));

    table.deleteAllRows();

    // Events should not yet be in the response
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    // And there should only one delete event
    assertEquals(1, jsonTable.eventBuffer().size());
    assertEquals(TableEvent.TYPE_ALL_ROWS_DELETED, jsonTable.eventBuffer().getBufferInternal().get(0).getType());
  }

  @Test
  public void testRequestFocusInCellCoalesceInMultipleResponses() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.init();
    table.resetColumns();
    table.setAutoDiscardOnDelete(true);
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());

    // Response should contain no events
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    // Request Focus without a row
    final ITableRow row = table.getRow(0);
    table.requestFocusInCell(table.getColumns().get(0), row);

    table.deleteAllRows();

    // Events should not yet be in the response
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    // And there should only one delete event
    assertEquals(1, jsonTable.eventBuffer().size());
    assertEquals(TableEvent.TYPE_ALL_ROWS_DELETED, jsonTable.eventBuffer().getBufferInternal().get(0).getType());

    // So there should be no events at all in the response
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    assertNull(response.optJSONArray("events"));

    // end current request
    JsonTestUtility.endRequest(m_uiSession);

    // try to requestFocusInCell on deleted row
    table.requestFocusInCell(table.getColumns().get(0), row);

    // Events should not be in the response
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    // And also not in the event buffer
    assertEquals(0, jsonTable.eventBuffer().size());

    // So there should be no events at all in the response
    JSONObject response2 = m_uiSession.currentJsonResponse().toJson();
    assertNull(response2.optJSONArray("events"));
  }

  @Test
  public void testOptTableRow() {
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(new TableWith3Cols(), null);
    assertNull(jsonTable.optTableRow("foo"));
  }

  @Test(expected = UiException.class)
  public void testGetTableRow() {
    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(new TableWith3Cols(), null);
    jsonTable.getTableRow("foo");
  }

  /**
   * <pre>
   * 1
   * |--2
   * |--3
   * 4
   * |--5
   *    |--6
   * </pre>
   */
  @Test
  public void testExpandAll() {
    HierarchicalTable table = new HierarchicalTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{0, null}));
    rows.add(table.createRow(new Object[]{1, 0}));
    rows.add(table.createRow(new Object[]{2, 0}));
    rows.add(table.createRow(new Object[]{3, null}));
    rows.add(table.createRow(new Object[]{4, 3}));
    rows.add(table.createRow(new Object[]{5, 4}));
    table.replaceRows(rows);
    table.collapseAll(null);

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());

    // Response should contain no events
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    table.expandAll(null);
    assertEquals(1, jsonTable.eventBuffer().size());
    TableEvent tableEvent = jsonTable.eventBuffer().getBufferInternal().get(0);
    assertEquals(TableEvent.TYPE_ROWS_EXPANDED, tableEvent.getType());
    assertArrayEquals(new Integer[]{0, 1, 2, 3, 4, 5},
        tableEvent.getRows().stream()
            .map(row -> (Integer) row.getCellValue(0)).toArray(Integer[]::new));

    JsonTestUtility.processBufferedEvents(m_uiSession);

    // expect that
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_EXPANDED);
    assertEquals(1, responseEvents.size());
  }

  @Test
  public void testTriggerStructureChangesDuringInitColumn() {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(1);

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, new JsonAdapterMock());
    jsonTable.toJson();

    // init is called after table has been converted to json, for example a wrapped form field might start form
    // (e.g. call init on table) after the json has already been created
    table.init();

    assertFalse(jsonTable.eventBuffer().isEmpty());
    // contains TYPE_COLUMN_STRUCTURE_CHANGED, TYPE_ALL_ROWS_DELETED, TYPE_ROWS_INSERTED, TYPE_ROW_ORDER_CHANGED

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertTrue(m_uiSession.currentJsonResponse().getEventList()
        .stream()
        .anyMatch(p -> "columnStructureChanged".equals(p.getType())));
  }

  public static Table createTableFixture(int numRows) {
    Table table = new Table();
    table.fill(numRows);
    table.init();
    return table;
  }

  public static JsonEvent createJsonRowsSelectedEvent(String rowId) throws JSONException {
    String tableId = "x"; // never used
    JSONObject data = new JSONObject();
    JSONArray rowIds = new JSONArray();
    if (rowId != null) {
      rowIds.put(rowId);
    }
    data.put(JsonTable.PROP_ROW_IDS, rowIds);
    return new JsonEvent(tableId, JsonTable.EVENT_ROWS_SELECTED, data);
  }

  public static JsonEvent createJsonRowsSortedEvent(String columnId, boolean asc) throws JSONException {
    String tableId = "x"; // never used
    JSONObject data = new JSONObject();
    data.put("columnId", columnId);
    data.put("sortAscending", asc);
    return new JsonEvent(tableId, JsonTable.EVENT_SORT, data);
  }

  public static JsonEvent createJsonRowsFilteredEvent(String... rowIds) throws JSONException {
    String tableId = "x"; // never used
    JSONObject data = new JSONObject();
    JSONArray jsonRowIds = new JSONArray();
    if (rowIds != null) {
      for (String rowId : rowIds) {
        jsonRowIds.put(rowId);
      }
    }
    data.put(JsonTable.PROP_ROW_IDS, jsonRowIds);
    return new JsonEvent(tableId, JsonTable.EVENT_FILTER, data);
  }

  public static JsonEvent createJsonColumnMovedEvent(String columnId, int index) throws JSONException {
    String tableId = "x"; // never used
    JSONObject data = new JSONObject();
    data.put(JsonTable.PROP_COLUMN_ID, columnId);
    data.put("index", index);
    return new JsonEvent(tableId, JsonTable.EVENT_COLUMN_MOVED, data);
  }
}
