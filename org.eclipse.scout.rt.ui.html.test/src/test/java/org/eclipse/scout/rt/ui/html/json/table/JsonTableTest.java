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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.UserTableRowFilter;
import org.eclipse.scout.rt.platform.util.StringUtility;
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
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableControl;
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
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(jsonTable.getOrCreatedRowId(row));
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

    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(null);

    jsonTable.handleUiEvent(event);

    assertTrue(table.getSelectedRows().size() == 0);
  }

  /**
   * Response must not contain the selection event if the selection was triggered by the request
   */
  @Test
  public void testIgnorableSelectionEvent() throws JSONException {
    Table table = createTableFixture(5);

    ITableRow row = table.getRow(2);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(jsonTable.getOrCreatedRowId(row));
    jsonTable.handleUiEvent(event);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertTrue(responseEvents.size() == 0);
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
    table.initTable();

    ITableRow row2 = table.getRow(2);
    ITableRow row4 = table.getRow(4);

    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonRowsSelectedEvent(jsonTable.getOrCreatedRowId(row2));

    assertFalse(row2.isSelected());
    assertFalse(row4.isSelected());

    jsonTable.handleUiEvent(event);

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertTrue(responseEvents.size() == 1);

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
    table.initTable();

    ITableRow row2 = table.getRow(2);
    ITableRow row4 = table.getRow(4);
    table.selectRow(row2);

    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
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
    assertTrue(responseEvents.size() == 1);

    List<ITableRow> tableRows = jsonTable.extractTableRows(responseEvents.get(0).getData());
    assertEquals(row4, tableRows.get(0));
  }

  @Test
  public void testColumnOrderChangedEvent() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetColumns();

    IColumn<?> column0 = table.getColumns().get(0);
    IColumn<?> column1 = table.getColumns().get(1);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
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
    table.initTable();
    table.resetColumns();

    IColumn<?> column0 = table.getColumns().get(0);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
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
    table.initTable();
    table.resetColumns();

    IColumn<?> column = table.getColumns().get(0);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    JsonEvent event = createJsonColumnMovedEvent(jsonTable.getColumnId(column), 2);
    jsonTable.handleUiEvent(event);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), "columnOrderChanged");
    assertTrue(responseEvents.size() == 0);
  }

  /**
   * Sends header update event if header cell has changed, but only for visible columns.
   */
  @Test
  public void testColumnHeadersUpdatedEvent() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetColumns();
    table.getColumnSet().getColumn(0).setDisplayable(false);

    IColumn<?> column0 = table.getColumns().get(0);
    IColumn<?> column1 = table.getColumns().get(1);
    IJsonAdapter<? super TableWith3Cols> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    ((HeaderCell) column0.getHeaderCell()).setText("newHeaderText");
    table.getColumnSet().updateColumn(column0);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_COLUMN_HEADERS_UPDATED);
    assertTrue(responseEvents.size() == 0);

    ((HeaderCell) column1.getHeaderCell()).setText("newHeaderText2");
    table.getColumnSet().updateColumn(column1);

    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_COLUMN_HEADERS_UPDATED);
    assertTrue(responseEvents.size() == 1);
  }

  /**
   * If column structure changes, we need to resend every row including its new cells.
   */
  @Test
  public void testColumnStructureChangedEvent() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetColumns();
    table.getColumnSet().getColumn(0).setVisible(false);

    IJsonAdapter<? super TableWith3Cols> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
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
    table.initTable();
    table.resetColumns();
    IColumn column0 = table.getColumnSet().getColumn(0);
    IColumn column1 = table.getColumnSet().getColumn(1);
    IColumn column2 = table.getColumnSet().getColumn(2);
    column0.setVisible(false);

    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
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
  public void testDontSendNonDisplayableMenus() throws Exception {
    TableWithNonDisplayableMenu table = new TableWithNonDisplayableMenu();
    table.initTable();

    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    JsonContextMenu<IContextMenu> jsonContextMenu = jsonTable.getAdapter(table.getContextMenu());
    JsonMenu<IMenu> jsonDisplayableMenu = jsonContextMenu.getAdapter(table.getMenuByClass(TableWithNonDisplayableMenu.DisplayableMenu.class));
    JsonMenu<IMenu> jsonNonDisplayableMenu = jsonContextMenu.getAdapter(table.getMenuByClass(TableWithNonDisplayableMenu.NonDisplayableMenu.class));

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

    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    JsonContextMenu<IContextMenu> jsonContextMenu = jsonTable.getAdapter(table.getContextMenu());

    Menu menu1 = new Menu();
    table.getContextMenu().addChildAction(menu1);
    assertNotNull(jsonContextMenu.getAdapter(menu1));
    assertTrue(jsonContextMenu.getAdapter(menu1).isInitialized());

    table.getContextMenu().removeChildAction(menu1);
    assertNull(jsonContextMenu.getAdapter(menu1));
  }

  /**
   * Tests whether it is possible to dispose (or replace) menus if at least one menu is not displayable.<br>
   */
  @Test
  public void testMenuDisposalOnPropertyChangeWithNonDisplayableMenu() throws JSONException {
    ITable table = new TableWithNonDisplayableMenu();
    table.initTable();

    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    JsonContextMenu<IContextMenu> jsonContextMenu = jsonTable.getAdapter(table.getContextMenu());

    DisplayableMenu displayableMenu = table.getMenuByClass(TableWithNonDisplayableMenu.DisplayableMenu.class);
    NonDisplayableMenu NonDisplayableMenu = table.getMenuByClass(TableWithNonDisplayableMenu.NonDisplayableMenu.class);
    assertNull(jsonContextMenu.getAdapter(NonDisplayableMenu));
    assertNotNull(jsonContextMenu.getAdapter(displayableMenu));
    assertTrue(jsonContextMenu.getAdapter(displayableMenu).isInitialized());

    table.getContextMenu().removeChildAction(NonDisplayableMenu);
    table.getContextMenu().removeChildAction(displayableMenu);

    assertNull(jsonContextMenu.getAdapter(NonDisplayableMenu));
    assertNull(jsonContextMenu.getAdapter(displayableMenu));
  }

  @Test
  public void testTableControlDisposalOnPropertyChange() throws JSONException {
    ITable table = new TableWithoutMenus();
    table.initTable();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    TableControl control = new TableControl();
    table.addTableControl(control);
    assertNotNull(jsonTable.getAdapter(control));
    assertTrue(jsonTable.getAdapter(control).isInitialized());

    table.removeTableControl(control);
    assertNull(jsonTable.getAdapter(control));
  }

  @Test
  public void testMultipleTableControlDisposallOnPropertyChange() throws JSONException {
    ITable table = new TableWithoutMenus();
    table.initTable();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    jsonTable.toJson();

    // ----------

    TableControl tableControl1 = new TableControl();
    TableControl tableControl2 = new TableControl();
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
   * @see TableSpec.js
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

  @Test
  public void testDispose() {
    Table table = new Table();
    JsonTable<ITable> object = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);

    WeakReference<JsonTable> ref = new WeakReference<JsonTable>(object);
    object.dispose();
    m_uiSession = null;
    object = null;
    TestingUtility.assertGC(ref);
  }

  @Test
  public void testRowFilter() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(3);
    table.initTable();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, null);
    ITableRow row0 = table.getRow(0);
    ITableRow row1 = table.getRow(1);
    ITableRow row2 = table.getRow(2);

    jsonTable.toJson();
    assertNotNull(jsonTable.tableRowIdsMap().get(row0));
    assertNotNull(jsonTable.tableRowIdsMap().get(row1));
    assertNotNull(jsonTable.tableRowIdsMap().get(row2));

    String row0Id = jsonTable.getOrCreatedRowId(row0);
    String row1Id = jsonTable.getOrCreatedRowId(row1);
    assertNotNull(row0Id);
    assertNotNull(jsonTable.getTableRow(row0Id));
    assertNotNull(row1Id);
    assertNotNull(jsonTable.getTableRow(row1Id));

    table.addRowFilter(new ITableRowFilter() {
      @Override
      public boolean accept(ITableRow r) {
        return r.getRowIndex() > 0; // hide first row
      }
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

  @Test
  public void testRowFilterWithUpdates() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(3);
    table.initTable();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, null);
    // Simulate that the full table is sent to the UI
    jsonTable.toJson();
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // Now filter the first row
    ITableRow row = table.getRow(0);
    String row0Id = jsonTable.getOrCreatedRowId(row);
    assertNotNull(row0Id);
    assertNotNull(jsonTable.getTableRow(row0Id));

    table.addRowFilter(new ITableRowFilter() {
      @Override
      public boolean accept(ITableRow r) {
        return r.getRowIndex() > 0; // hide first row
      }
    });

    // Update the (now hidden) row --> should not trigger an update event, because the row does not exist in the UI
    row.getCellForUpdate(0).setValue("Updated text");

    // We expect the first row to be removed from the table, and no update event!
    assertEquals(3, table.getRowCount());
    assertEquals(2, table.getFilteredRowCount());
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(3, jsonTable.eventBuffer().size()); // TYPE_ROW_FILTER_CHANGED + TYPE_ROWS_UPDATED = TYPE_ROWS_DELETED + TYPE_ROWS_INSERTED + TYPE_ROWS_UPDATED

    // Filtering is implemented by Only one deletion event should be emitted (no update event!)
    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("rowsDeleted", m_uiSession.currentJsonResponse().getEventList().get(0).getType());
  }

  /**
   * If the rows are filtered using {@link UserTableRowFilter}, the rows must not be deleted from json table and no
   * delete event must be sent -> gui knows which rows are filtered and keeps the invisible rows in memory
   */
  @Test
  public void testUserRowFilter() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(3);
    table.initTable();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, null);
    ITableRow row0 = table.getRow(0);
    ITableRow row2 = table.getRow(2);

    jsonTable.toJson();

    String row0Id = jsonTable.getOrCreatedRowId(row0);
    String row2Id = jsonTable.getOrCreatedRowId(row2);

    JsonEvent event = createJsonRowsFilteredEvent(row0Id, row2Id);
    jsonTable.handleUiEvent(event);

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(3, table.getRowCount());
    assertEquals(2, table.getFilteredRowCount());
    assertEquals(3, jsonTable.tableRowIdsMap().size());

    // expect that NO delete event is sent
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ROWS_DELETED);
    assertTrue(responseEvents.size() == 0);
  }

  /**
   * If toJson is called and a user filter is active, every row must be returned and not only the filtered ones
   */
  @Test
  public void testUserRowFilter_toJson() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(3);
    table.initTable();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, null);
    ITableRow row0 = table.getRow(0);
    ITableRow row2 = table.getRow(2);

    List<ITableRow> filteredRows = new ArrayList<ITableRow>();
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
  public void testUserRowFilter_UpdateEvent() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(3);
    table.initTable();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, null);
    // Simulate that the full table is sent to the UI
    jsonTable.toJson();
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // Now reject the first row
    ITableRow row0 = table.getRow(0);
    ITableRow row1 = table.getRow(1);
    ITableRow row2 = table.getRow(2);
    String row0Id = jsonTable.getOrCreatedRowId(row0);
    String row1Id = jsonTable.getOrCreatedRowId(row1);
    String row2Id = jsonTable.getOrCreatedRowId(row2);
    assertNotNull(row0Id);
    assertNotNull(jsonTable.getTableRow(row0Id));

    table.addRowFilter(new ITableRowFilter() {
      @Override
      public boolean accept(ITableRow r) {
        return r.getRowIndex() > 0; // hide first row
      }
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
    //TODO [5.2] bsh: CGU delete ? assertEquals(6, jsonTable.eventBuffer().size()); // TYPE_ROW_FILTER_CHANGED + TYPE_ROWS_UPDATED = TYPE_ROWS_DELETED + TYPE_ROWS_INSERTED + TYPE_ROWS_UPDATED (row0) + TYPE_ROWS_UPDATED (row2)

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
    table.initTable();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, null);
    ITableRow row0 = table.getRow(0);
    ITableRow row2 = table.getRow(2);

    jsonTable.toJson();

    String row0Id = jsonTable.getOrCreatedRowId(row0);
    String row2Id = jsonTable.getOrCreatedRowId(row2);

    table.addRowFilter(new ITableRowFilter() {
      @Override
      public boolean accept(ITableRow r) {
        return r.getRowIndex() > 0; // hide first row
      }
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
    assertTrue(responseEvents.size() == 1);
  }

  /**
   * JsonTable generates an allRowsDeleted event if a row is deleted and filteredRowCount is 0. This must not happen if
   * the row has been filtered by the user.
   */
  @Test
  public void testUserRowFilter_preventAllRowsDeleted() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(2);
    table.initTable();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, null);
    ITableRow row0 = table.getRow(0);

    jsonTable.toJson();

    String row0Id = jsonTable.getOrCreatedRowId(row0);

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
    assertTrue(responseEvents.size() == 0);
  }

  /**
   * JsonTable generates an allRowsDeleted event if a row is deleted and filteredRowCount is 0. This happens only if a
   * filter is active, otherwise table generates a all rows deleted event by itself
   */
  @Test
  public void testAllRowsDeleted_whenFilterActive() throws JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(2);
    table.initTable();

    JsonTable<ITable> jsonTable = m_uiSession.createJsonAdapter(table, null);
    ITableRow row0 = table.getRow(0);

    jsonTable.toJson();

    table.addRowFilter(new ITableRowFilter() {
      @Override
      public boolean accept(ITableRow r) {
        return r.getRowIndex() == 0; // hide second row
      }
    });

    table.deleteRow(row0);

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(1, table.getRowCount());
    assertEquals(0, table.getFilteredRowCount());
    assertEquals(0, jsonTable.tableRowIdsMap().size());

    // expect that deleteAll event is sent
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTable.EVENT_ALL_ROWS_DELETED);
    assertTrue(responseEvents.size() == 1);
  }

  /**
   * Tests that multiple model events are coalseced in JSON layer
   */
  @Test
  public void testTableEventCoalesceInUi_TwoRowsAdded() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);

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
  public void testTableEventCoalesceInUi_RowInsertedAndUpdated() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);

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
  public void testTableEventCoalesceInUi_RowInsertedAndDeleted() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);

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
   * Tests that multiple model events are coalseced in JSON layer
   */
  @Test
  public void testTableEventCoalesceInUi_InsertAndRowOrderChanged() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.initTable();
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);

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
  public void testTableEventCoalesceInUi_UpdateEventOnFilteredRow() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.initTable();
    table.fill(1, false);
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    m_uiSession.currentJsonResponse().addAdapter(jsonTable);
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);

    // -------------

    table.fill(2, false);
    table.getRow(2).setChecked(true); // would normally trigger an event, but we filter the row in the next step
    table.addRowFilter(new ITableRowFilter() {
      @Override
      public boolean accept(ITableRow r) {
        return r.getRowIndex() == 0; // hide everything expect the first (already existing row)
      }
    });

    response = m_uiSession.currentJsonResponse().toJson();
    assertEquals(0, jsonTable.eventBuffer().size());
    JSONArray events = response.optJSONArray("events");
    assertNull(events); // No events should be emitted
  }

  @Test
  public void testTableEventCoalesceInUi_DeleteEventOnFilteredRow() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.initTable();
    table.fill(1, false);
    table.resetColumns();
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table, null);
    m_uiSession.currentJsonResponse().addAdapter(jsonTable);
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    JsonTestUtility.endRequest(m_uiSession);

    // -------------

    table.fill(2, false);
    table.addRowFilter(new ITableRowFilter() {
      @Override
      public boolean accept(ITableRow r) {
        return false; // filter all rows
      }
    });

    response = m_uiSession.currentJsonResponse().toJson();
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
  public void testOptTableRow() throws Exception {
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, new TableWith3Cols(), null);
    assertNull(jsonTable.optTableRow("foo"));
  }

  @Test(expected = UiException.class)
  public void testGetTableRow() throws Exception {
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, new TableWith3Cols(), null);
    jsonTable.getTableRow("foo");
  }

  public static Table createTableFixture(int numRows) {
    Table table = new Table();
    table.fill(numRows);
    table.initTable();
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
    return new JsonEvent(tableId, JsonTable.EVENT_ROWS_FILTERED, data);
  }

  public static JsonEvent createJsonColumnMovedEvent(String columnId, int index) throws JSONException {
    String tableId = "x"; // never used
    JSONObject data = new JSONObject();
    data.put(JsonTable.PROP_COLUMN_ID, columnId);
    data.put("index", index);
    return new JsonEvent(tableId, JsonTable.EVENT_COLUMN_MOVED, data);
  }
}
