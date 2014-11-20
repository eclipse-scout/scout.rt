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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWith3Cols;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonTableTest {

  /**
   * Tests whether the model row gets correctly selected
   */
  @Test
  public void testSelectionEvent() throws ProcessingException, JSONException {
    Table table = createTableFixture(5);

    assertNull(table.getSelectedRow());

    ITableRow row = table.getRow(2);
    JsonTable jsonTable = createJsonTableWithMocks(table);

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

    JsonTable jsonTable = createJsonTableWithMocks(table);
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
    JsonTable jsonTable = createJsonTableWithMocks(table);

    JsonEvent event = createJsonSelectedEvent(jsonTable.getOrCreatedRowId(row));
    jsonTable.handleUiEvent(event, new JsonResponse());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        jsonTable.getJsonSession().currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
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

    JsonTable<ITable> jsonTable = createJsonTableWithMocks(table);
    JsonEvent event = createJsonSelectedEvent(jsonTable.getOrCreatedRowId(row2));

    assertFalse(row2.isSelected());
    assertFalse(row4.isSelected());

    jsonTable.handleUiEvent(event, new JsonResponse());

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        jsonTable.getJsonSession().currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
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

    JsonTable<ITable> jsonTable = createJsonTableWithMocks(table);
    JsonEvent event = createJsonSelectedEvent(null);

    assertTrue(row2.isSelected());
    assertFalse(row4.isSelected());

    jsonTable.handleUiEvent(event, new JsonResponse());

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        jsonTable.getJsonSession().currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
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
    JsonTable jsonTable = createJsonTableWithMocks(table);

    assertEquals(table.getColumnSet().getVisibleColumn(0), column0);
    assertEquals(table.getColumnSet().getVisibleColumn(1), column1);

    JsonEvent event = createJsonColumnMovedEvent(column0.getColumnId(), 2);
    jsonTable.handleUiEvent(event, new JsonResponse());

    assertEquals(table.getColumnSet().getVisibleColumn(2), column0);

    event = createJsonColumnMovedEvent(column1.getColumnId(), 0);
    jsonTable.handleUiEvent(event, new JsonResponse());

    assertEquals(table.getColumnSet().getVisibleColumn(0), column1);
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
    JsonTable jsonTable = createJsonTableWithMocks(table);

    JsonEvent event = createJsonColumnMovedEvent(column.getColumnId(), 2);
    jsonTable.handleUiEvent(event, new JsonResponse());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        jsonTable.getJsonSession().currentJsonResponse(), "columnOrderChanged");
    assertTrue(responseEvents.size() == 0);
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
    JsonTable object = createJsonTableWithMocks(table);
    WeakReference<JsonTable> ref = new WeakReference<JsonTable>(object);
    object.dispose();
    object.getJsonSession().flush();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  public static Table createTableFixture(int numRows) throws ProcessingException {
    Table table = new Table();
    table.fill(numRows);
    table.initTable();
    return table;
  }

  public static JsonTable<ITable> createJsonTableWithMocks(ITable table) {
    JsonSessionMock jsonSession = new JsonSessionMock();
    JsonTable<ITable> jsonTable = new JsonTable<ITable>(table, jsonSession, jsonSession.createUniqueIdFor(null));
    jsonTable.attach();
    return jsonTable;
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
