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

import static org.eclipse.scout.rt.ui.json.testing.JsonTestUtility.extractEventsFromResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonTableTest {

  /**
   * Tests whether the model row gets correctly selected
   */
  @Test
  public void testSelectionEvent() throws ProcessingException, JSONException {
    Table table = new Table();
    table.fill(5);

    assertNull(table.getSelectedRow());

    ITableRow row = table.getRow(2);
    JsonTable jsonTable = createJsonTableWithMocks(table);

    JsonEvent event = createJsonSelectedEvent(jsonTable.getOrCreatedRowId(row));
    jsonTable.handleUiEvent(event, new JsonResponse());

    assertTrue(row.isSelected());
  }

  /**
   * Response must not contain the selection event if the selection was triggered by the request
   */
  @Test
  public void testIgnorableSelectionEvent() throws ProcessingException, JSONException {
    Table table = new Table();
    table.fill(5);

    ITableRow row = table.getRow(2);
    JsonTable jsonTable = createJsonTableWithMocks(table);

    JsonEvent event = createJsonSelectedEvent(jsonTable.getOrCreatedRowId(row));
    jsonTable.handleUiEvent(event, new JsonResponse());

    List<JSONObject> responseEvents = extractEventsFromResponse(jsonTable.getJsonSession().currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
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

    ITableRow row2 = table.getRow(2);
    ITableRow row4 = table.getRow(4);

    JsonTable jsonTable = createJsonTableWithMocks(table);
    JsonEvent event = createJsonSelectedEvent(jsonTable.getOrCreatedRowId(row2));

    assertFalse(row2.isSelected());
    assertFalse(row4.isSelected());

    jsonTable.handleUiEvent(event, new JsonResponse());

    assertFalse(row2.isSelected());
    assertTrue(row4.isSelected());

    List<JSONObject> responseEvents = extractEventsFromResponse(jsonTable.getJsonSession().currentJsonResponse(), JsonTable.EVENT_ROWS_SELECTED);
    assertTrue(responseEvents.size() == 1);

    List<ITableRow> tableRows = jsonTable.extractTableRows(responseEvents.get(0));
    assertEquals(row4, tableRows.get(0));
  }

  @Test
  public void testDispose() {
    Table table = new Table();
    JsonTable object = createJsonTableWithMocks(table);
    WeakReference<JsonTable> ref = new WeakReference<JsonTable>(object);

    object.dispose();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  public static JsonTable createJsonTableWithMocks(ITable table) {
    JsonSessionMock jsonSession = new JsonSessionMock();

    JsonTable jsonTable = new JsonTable(table, jsonSession);
    jsonTable.init();

    //init treeNode map
    jsonTable.toJson();

    return jsonTable;
  }

  public static JsonEvent createJsonSelectedEvent(String rowId) throws JSONException {
    JsonEvent event = JsonTestUtility.createJsonEvent(JsonTable.EVENT_ROWS_SELECTED);
    JSONArray rowIds = new JSONArray();
    rowIds.put(rowId);
    event.getEventObject().put(JsonTable.PROP_ROW_IDS, rowIds);
    return event;
  }

}
