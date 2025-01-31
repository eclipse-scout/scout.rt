/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.listbox;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonAdapterMock;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonListBoxTest {
  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Usecase:
   * <p>
   * Listbox has a filter.<br>
   * 1. Reload the listbox -> generates rowsUpdated and rowOrderChanged events<br>
   * 2. Add a new filter -> generates rowFilterChanged which are converted into rowsDeleted or rowsInserted Asserts that
   * rowOrderChanged event contains the correct rows.
   */
  @Test
  public void testReloadAndRowFilterChange() {
    ListBox listBox = new ListBox();
    ITable table = listBox.getTable();
    listBox.init();

    JsonListBox<Long, IListBox<Long>> jsonListBox = m_uiSession.createJsonAdapter(listBox, new JsonAdapterMock());
    JsonTable<ITable> jsonTable = jsonListBox.getAdapter(table);

    // Filter the first row
    table.addRowFilter(row -> {
      Long key = (Long) row.getKeyValues().get(0);
      if (key.equals(0L)) {
        return false;
      }
      return true;
    });
    assertEquals(3, table.getRowCount());
    assertEquals(2, table.getFilteredRowCount());

    // "Send" listbox to UI
    jsonListBox.toJson();
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);
    assertEquals(0, m_uiSession.currentJsonResponse().getEventList().size());

    // Load listbox BEFORE adding a new filter -> generates rowOrderChanged before the filter events
    listBox.loadListBoxData();

    // Filter second row as well
    String row1Id = jsonTable.getTableRowId(table.getRowByKey(Arrays.asList(1L)));
    table.addRowFilter(row -> {
      Long key = (Long) row.getKeyValues().get(0);
      if (key.equals(1L)) {
        return false;
      }
      return true;
    });
    assertEquals(1, table.getFilteredRowCount());

    JsonTestUtility.processBufferedEvents(m_uiSession);
    List<JsonEvent> eventList = m_uiSession.currentJsonResponse().getEventList();

    JsonEvent jsonEvent = eventList.get(0);
    assertEquals("rowsDeleted", jsonEvent.getType());
    assertEquals(1, jsonEvent.getData().getJSONArray(JsonTable.PROP_ROW_IDS).length());
    assertEquals(row1Id, jsonEvent.getData().getJSONArray(JsonTable.PROP_ROW_IDS).get(0));

    jsonEvent = eventList.get(2); // eventList.get(1) is the rows_updated event, not of interest here
    assertEquals("rowOrderChanged", jsonEvent.getType());
    JSONArray jsonRowIds = jsonEvent.getData().getJSONArray(JsonTable.PROP_ROW_IDS);
    assertEquals(1, jsonRowIds.length());
  }

  @ClassId("8bc2da35-a37d-4a4c-8860-9e31a361648f")
  public static class ListBox extends AbstractListBox<Long> {
    @Override
    protected void execFilterLookupResult(ILookupCall<Long> call, List<ILookupRow<Long>> result) {
      result.add(new LookupRow<>(0L, "a"));
      result.add(new LookupRow<>(1L, "b"));
      result.add(new LookupRow<>(2L, "c"));
    }
  }
}
