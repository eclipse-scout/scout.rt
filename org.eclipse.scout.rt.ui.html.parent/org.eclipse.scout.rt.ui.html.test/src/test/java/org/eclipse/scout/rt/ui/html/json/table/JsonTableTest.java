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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableControl;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWith3Cols;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithNotDisplayableMenu;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithNotDisplayableMenu.DisplayableMenu;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithNotDisplayableMenu.NotDisplayableMenu;
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
  private JsonSessionMock m_jsonSession;

  @Before
  public void setUp() {
    m_jsonSession = new JsonSessionMock();
  }

  /**
   * Tests whether the model row gets correctly selected
   */
  @Test
  public void testSelectionEvent() throws ProcessingException, JSONException {
    Table table = createTableFixture(5);

    assertNull(table.getSelectedRow());

    ITableRow row = table.getRow(2);
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    JsonEvent event = createJsonSelectedEvent(jsonTable.getOrCreatedRowId(row));
    jsonTable.handleUiEvent(event, new JsonResponse());

    assertTrue(row.isSelected());
  }

  /**
   * Tests whether the model rows get correctly unselected
   */
  @Test
  public void testClearSelectionEvent() throws ProcessingException, JSONException {
    Table table = createTableFixture(5);
    ITableRow row1 = table.getRow(1);

    table.selectRow(row1);

    assertTrue(row1.isSelected());

    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);
    JsonEvent event = createJsonSelectedEvent(null);

    jsonTable.handleUiEvent(event, new JsonResponse());

    assertTrue(table.getSelectedRows().size() == 0);
  }

  /**
   * Response must not contain the selection event if the selection was triggered by the request
   */
  @Test
  public void testIgnorableSelectionEvent() throws ProcessingException, JSONException {
    Table table = createTableFixture(5);

    ITableRow row = table.getRow(2);
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    JsonEvent event = createJsonSelectedEvent(jsonTable.getOrCreatedRowId(row));
    jsonTable.handleUiEvent(event, new JsonResponse());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertTrue(responseEvents.size() == 0);
  }

  /**
   * If the selection event triggers the selection of another row, the selection event must not be ignored.
   */
  @Test
  public void testIgnorableSelectionEvent2() throws ProcessingException, JSONException {
    Table table = new Table() {
      @Override
      protected void execRowsSelected(List<? extends ITableRow> rows) throws ProcessingException {
        selectRow(4);
      }
    };
    table.fill(5);
    table.initTable();

    ITableRow row2 = table.getRow(2);
    ITableRow row4 = table.getRow(4);

    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);
    JsonEvent event = createJsonSelectedEvent(jsonTable.getOrCreatedRowId(row2));

    assertFalse(row2.isSelected());
    assertFalse(row4.isSelected());

    jsonTable.handleUiEvent(event, new JsonResponse());

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertTrue(responseEvents.size() == 1);

    List<ITableRow> tableRows = jsonTable.extractTableRows(responseEvents.get(0).getData());
    assertEquals(row4, tableRows.get(0));
  }

  /**
   * Same as {@link #testIgnorableSelectionEvent2()} but with an empty selection
   */
  @Test
  public void testIgnorableSelectionEvent3() throws ProcessingException, JSONException {
    Table table = new Table() {
      @Override
      protected void execRowsSelected(List<? extends ITableRow> rows) throws ProcessingException {
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

    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);
    JsonEvent event = createJsonSelectedEvent(null);

    assertTrue(row2.isSelected());
    assertFalse(row4.isSelected());

    jsonTable.handleUiEvent(event, new JsonResponse());

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertTrue(responseEvents.size() == 1);

    List<ITableRow> tableRows = jsonTable.extractTableRows(responseEvents.get(0).getData());
    assertEquals(row4, tableRows.get(0));
  }

  @Test
  public void testColumnOrderChangedEvent() throws ProcessingException, JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetDisplayableColumns();

    IColumn<?> column0 = table.getColumns().get(0);
    IColumn<?> column1 = table.getColumns().get(1);
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    assertEquals(table.getColumnSet().getVisibleColumn(0), column0);
    assertEquals(table.getColumnSet().getVisibleColumn(1), column1);

    JsonEvent event = createJsonColumnMovedEvent(column0.getColumnId(), 2);
    jsonTable.handleUiEvent(event, new JsonResponse());

    assertEquals(table.getColumnSet().getVisibleColumn(2), column0);

    event = createJsonColumnMovedEvent(column1.getColumnId(), 0);
    jsonTable.handleUiEvent(event, new JsonResponse());

    assertEquals(table.getColumnSet().getVisibleColumn(0), column1);
  }

  @Test
  public void testColumnOrderChangedEvent_assertCellOrder() throws ProcessingException, JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetDisplayableColumns();

    IColumn<?> column0 = table.getColumns().get(0);
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    JSONObject jsonRow = jsonTable.tableRowToJson(table.getRow(0));
    JSONArray jsonCells = (JSONArray) jsonRow.get("cells");

    JsonEvent event = createJsonColumnMovedEvent(column0.getColumnId(), 2);
    jsonTable.handleUiEvent(event, new JsonResponse());

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
  public void testIgnorableColumnOrderChangedEvent() throws ProcessingException, JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetDisplayableColumns();

    IColumn<?> column = table.getColumns().get(0);
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    JsonEvent event = createJsonColumnMovedEvent(column.getColumnId(), 2);
    jsonTable.handleUiEvent(event, new JsonResponse());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), "columnOrderChanged");
    assertTrue(responseEvents.size() == 0);
  }

  /**
   * Sends header update event if header cell has changed, but only for visible columns.
   */
  @Test
  public void testColumnHeadersUpdatedEvent() throws ProcessingException, JSONException {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetDisplayableColumns();
    table.getColumnSet().getColumn(0).setDisplayable(false);

    IColumn<?> column0 = table.getColumns().get(0);
    IColumn<?> column1 = table.getColumns().get(1);
    m_jsonSession.newJsonAdapter(table, null, null);

    ((HeaderCell) column0.getHeaderCell()).setText("newHeaderText");
    table.getColumnSet().updateColumn(column0);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTable.EVENT_COLUMN_HEADERS_UPDATED);
    assertTrue(responseEvents.size() == 0);

    ((HeaderCell) column1.getHeaderCell()).setText("newHeaderText2");
    table.getColumnSet().updateColumn(column1);

    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTable.EVENT_COLUMN_HEADERS_UPDATED);
    assertTrue(responseEvents.size() == 1);
  }

  /**
   * Tests whether non displayable menus are sent.
   * <p>
   * This limits response size and also leverages security because the menus are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNotDisplayableMenus() throws Exception {
    TableWithNotDisplayableMenu table = new TableWithNotDisplayableMenu();
    table.initTable();

    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);
    JsonContextMenu<IContextMenu> jsonContextMenu = jsonTable.getAdapter(table.getContextMenu());
    JsonMenu<IMenu> jsonDisplayableMenu = jsonContextMenu.getAdapter(table.getMenu(TableWithNotDisplayableMenu.DisplayableMenu.class));
    JsonMenu<IMenu> jsonNotDisplayableMenu = jsonContextMenu.getAdapter(table.getMenu(TableWithNotDisplayableMenu.NotDisplayableMenu.class));

    // Adapter for NotDisplayableMenu must not exist
    assertNull(jsonNotDisplayableMenu);

    // Json response must not contain NotDisplayableMenu
    JSONObject json = jsonTable.toJson();
    JSONArray jsonMenus = json.getJSONArray("menus");
    assertEquals(1, jsonMenus.length());
    assertEquals(jsonDisplayableMenu.getId(), jsonMenus.get(0));
  }

  @Test
  public void testMenuDisposalOnPropertyChange() throws ProcessingException, JSONException {
    ITable table = new TableWithoutMenus();

    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);
    JsonContextMenu<IContextMenu> jsonContextMenu = jsonTable.getAdapter(table.getContextMenu());

    Menu menu1 = new Menu();
    table.getContextMenu().addChildAction(menu1);
    assertNotNull(jsonContextMenu.getAdapter(menu1));
    assertTrue(jsonContextMenu.getAdapter(menu1).isAttached());

    table.getContextMenu().removeChildAction(menu1);
    m_jsonSession.flush();
    assertNull(jsonContextMenu.getAdapter(menu1));
  }

  /**
   * Tests whether it is possible to dispose (or replace) menus if at least one menu is not displayable.<br>
   */
  @Test
  public void testMenuDisposalOnPropertyChangeWithNotDisplayableMenu() throws ProcessingException, JSONException {
    ITable table = new TableWithNotDisplayableMenu();
    table.initTable();

    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);
    JsonContextMenu<IContextMenu> jsonContextMenu = jsonTable.getAdapter(table.getContextMenu());

    DisplayableMenu displayableMenu = table.getMenu(TableWithNotDisplayableMenu.DisplayableMenu.class);
    NotDisplayableMenu notDisplayableMenu = table.getMenu(TableWithNotDisplayableMenu.NotDisplayableMenu.class);
    assertNull(jsonContextMenu.getAdapter(notDisplayableMenu));
    assertNotNull(jsonContextMenu.getAdapter(displayableMenu));
    assertTrue(jsonContextMenu.getAdapter(displayableMenu).isAttached());

    table.getContextMenu().removeChildAction(notDisplayableMenu);
    table.getContextMenu().removeChildAction(displayableMenu);
    m_jsonSession.flush();

    assertNull(jsonContextMenu.getAdapter(notDisplayableMenu));
    assertNull(jsonContextMenu.getAdapter(displayableMenu));
  }

  @Test
  public void testTableControlDisposalOnPropertyChange() throws ProcessingException, JSONException {
    ITable table = new TableWithoutMenus();
    table.initTable();
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    TableControl control = new TableControl();
    table.addTableControl(control);
    assertNotNull(jsonTable.getAdapter(control));
    assertTrue(jsonTable.getAdapter(control).isAttached());

    table.removeTableControl(control);
    m_jsonSession.flush();
    assertNull(jsonTable.getAdapter(control));
  }

  @Test
  public void testMultipleTableControlDisposallOnPropertyChange() throws ProcessingException, JSONException {
    ITable table = new TableWithoutMenus();
    table.initTable();
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    TableControl tableControl1 = new TableControl();
    TableControl tableControl2 = new TableControl();
    table.addTableControl(tableControl1);
    table.addTableControl(tableControl2);
    assertNotNull(jsonTable.getAdapter(tableControl1));
    assertTrue(jsonTable.getAdapter(tableControl1).isAttached());
    assertNotNull(jsonTable.getAdapter(tableControl2));
    assertTrue(jsonTable.getAdapter(tableControl2).isAttached());

    table.removeTableControl(tableControl1);
    m_jsonSession.flush();
    assertNull(jsonTable.getAdapter(tableControl1));
    assertNotNull(jsonTable.getAdapter(tableControl2));
    assertTrue(jsonTable.getAdapter(tableControl2).isAttached());
  }

  /**
   * {@link AbstractStringColumn} considers the locale when sorting. This test is used to compare the results of the
   * java sorting algorithm with the javascript sorting algorithm.
   *
   * @see TableSpec.js
   */
  @Test
  public void testTextualSortingWithCollator() throws ProcessingException, JSONException {
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
    JsonTable<ITable> object = m_jsonSession.newJsonAdapter(table, null, null);

    WeakReference<JsonTable> ref = new WeakReference<JsonTable>(object);
    object.dispose();
    m_jsonSession.flush();
    m_jsonSession = null;
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test(expected = JsonException.class)
  public void testRowFilter() throws ProcessingException, JSONException {
    TableWith3Cols table = new TableWith3Cols();

    table.fill(2);
    table.initTable();

    JsonTable<ITable> jsonTable = m_jsonSession.createJsonAdapter(table, null);
    ITableRow row = table.getRow(0);

    String row0Id = jsonTable.getOrCreatedRowId(row);
    assertNotNull(row0Id);
    assertNotNull(jsonTable.getTableRowForRowId(row0Id));

    table.addRowFilter(new ITableRowFilter() {

      @Override
      public boolean accept(ITableRow r) {
        return r.getRowIndex() == 0;
      }
    });

    // After flushing the event buffers and applying the model changes
    // to the JsonTable, the row should not exist anymore on the JsonTable
    JsonTestUtility.processBufferedEvents(m_jsonSession);
    assertNull(jsonTable.getTableRowForRowId(row0Id));
  }

  /**
   * Tests that multiple model events are coalseced in JSON layer
   */
  @Test
  public void testTableEventCoalesceInUi_TwoRowsAdded() throws Exception {
    TableWith3Cols table = new TableWith3Cols();
    table.fill(2);
    table.initTable();
    table.resetDisplayableColumns();
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    // Response should contain no events
    assertEquals(0, m_jsonSession.currentJsonResponse().getEventList().size());

    // Add two rows sequentially --> this should trigger two TableEvents
    table.addRowsByMatrix(new Object[]{new Object[]{"NewCell_0", "NewCell_1", "NewCell_2"}});
    table.addRowsByMatrix(new Object[]{new Object[]{"AnotherNewCell_0", "AnotherNewCell_1", "AnotherNewCell_2"}});

    // Events should not yet be in the response
    assertEquals(0, m_jsonSession.currentJsonResponse().getEventList().size());
    // But they should be in the event buffer
    assertEquals(2, jsonTable.eventBuffer().size());
    // When converting to JSON, the event buffer should be cleared and the events should
    // be coalesced and written to the response. -->  Only one insert event (with two rows)
    JSONObject response = m_jsonSession.currentJsonResponse().toJson();
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
    table.resetDisplayableColumns();
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    // Response should contain no events
    assertEquals(0, m_jsonSession.currentJsonResponse().getEventList().size());

    // Add one row, then update it --> this should trigger two TableEvents
    List<ITableRow> newRows = table.addRowsByMatrix(new Object[]{new Object[]{"NewCell_0", "NewCell_1", "NewCell_2"}});
    newRows.get(0).getCellForUpdate(0).setValue("UPDATED");

    // Events should not yet be in the response
    assertEquals(0, m_jsonSession.currentJsonResponse().getEventList().size());
    // But they should be in the event buffer
    assertEquals(2, jsonTable.eventBuffer().size());
    // When converting to JSON, the event buffer should be cleared and the events should
    // be coalesced and written to the response. --> Update should be merged with inserted
    JSONObject response = m_jsonSession.currentJsonResponse().toJson();
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
    table.resetDisplayableColumns();
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    // Response should contain no events
    assertEquals(0, m_jsonSession.currentJsonResponse().getEventList().size());

    // Add one row, then delete it --> this should trigger two TableEvents
    List<ITableRow> newRows = table.addRowsByMatrix(new Object[]{new Object[]{"NewCell_0", "NewCell_1", "NewCell_2"}});
    table.discardRows(newRows);

    // Events should not yet be in the response
    assertEquals(0, m_jsonSession.currentJsonResponse().getEventList().size());
    // But they should be in the event buffer
    assertEquals(2, jsonTable.eventBuffer().size());
    // When converting to JSON, the event buffer should be cleared and the events should
    // be coalesced and written to the response. --> Both events should cancel each other
    JSONObject response = m_jsonSession.currentJsonResponse().toJson();
    assertEquals(0, jsonTable.eventBuffer().size());
    JSONArray events = response.getJSONArray("events");

    // FIXME BSH Actually, we would expect 0. But coalesce does not do this yet. Change this later.
    assertEquals(2, events.length());
  }

  public static Table createTableFixture(int numRows) throws ProcessingException {
    Table table = new Table();
    table.fill(numRows);
    table.initTable();
    return table;
  }

  public static JsonEvent createJsonSelectedEvent(String rowId) throws JSONException {
    String tableId = "x"; // never used
    JSONObject data = new JSONObject();
    JSONArray rowIds = new JSONArray();
    if (rowId != null) {
      rowIds.put(rowId);
    }
    data.put(JsonTable.PROP_ROW_IDS, rowIds);
    return new JsonEvent(tableId, JsonTable.EVENT_ROWS_SELECTED, data);
  }

  public static JsonEvent createJsonColumnMovedEvent(String columnId, int index) throws JSONException {
    String tableId = "x"; // never used
    JSONObject data = new JSONObject();
    data.put(JsonTable.PROP_COLUMN_ID, columnId);
    data.put("index", index);
    return new JsonEvent(tableId, JsonTable.EVENT_COLUMN_MOVED, data);
  }
}
