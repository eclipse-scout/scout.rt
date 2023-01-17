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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithBooleanColumn;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithDateColumn;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithLongColumn;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithStringColumn;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonCellToJsonTest {
  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Don't generate a cell object if text is the only property.
   */
  @Test
  public void testStringColumn() throws JSONException {
    TableWithStringColumn table = new TableWithStringColumn();
    table.init();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, "A string");
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    Object jsonObj = jsonTable.cellToJson(row, table.getColumn());
    assertTrue(jsonObj instanceof String);
  }

  /**
   * Don't send value and text if they are equal.
   */
  @Test
  public void testLongColumn() throws JSONException {
    TableWithLongColumn table = new TableWithLongColumn();
    table.init();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, 15L);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    JSONObject jsonObj = (JSONObject) jsonTable.cellToJson(row, table.getColumn());
    Assert.assertEquals("15", jsonObj.get("text"));
    assertNull(jsonObj.opt("value"));
  }

  /**
   * Don't generate a cell object if text is the only property.
   */
  @Test
  public void testLongColumn_leftAlignment() throws JSONException {
    TableWithLongColumn table = new TableWithLongColumn();
    table.getColumn().setHorizontalAlignment(-1);
    table.init();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, 15L);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    Object jsonObj = jsonTable.cellToJson(row, table.getColumn());
    assertTrue(jsonObj instanceof String);
  }

  /**
   * Send {@link JSONObject#NULL} if value is null if value is required.
   */
  @Test
  public void testNullValue() throws JSONException {
    TableWithLongColumn table = new TableWithLongColumn();
    table.init();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, null);
    row.getCellForUpdate(table.getColumn()).setText("-empty-");
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    JSONObject jsonObj = (JSONObject) jsonTable.cellToJson(row, table.getColumn());
    assertEquals("-empty-", jsonObj.get("text"));
    assertEquals(JSONObject.NULL, jsonObj.get("value"));
  }

  /**
   * Send only empty text if both are empty
   */
  @Test
  public void testNullValueAndEmptyText() throws JSONException {
    TableWithLongColumn table = new TableWithLongColumn();
    table.init();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, null);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    JSONObject jsonObj = (JSONObject) jsonTable.cellToJson(row, table.getColumn());
    assertEquals("", jsonObj.get("text"));
    assertNull(jsonObj.opt("value"));
  }

  /**
   * Send only empty text if both are empty
   */
  @Test
  public void testNullValueAndEmptyText_leftAlignment() throws JSONException {
    TableWithLongColumn table = new TableWithLongColumn();
    table.getColumn().setHorizontalAlignment(-1);
    table.init();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, null);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    Object jsonObj = jsonTable.cellToJson(row, table.getColumn());
    assertEquals("", jsonObj);
  }

  @Test
  public void testDateColumn() throws JSONException {
    TableWithDateColumn table = new TableWithDateColumn();
    table.init();
    ITableRow row = table.addRow(table.createRow());

    table.getColumn().setFormat("dd.MM.yyyy");
    table.getColumn().setValue(row, DateUtility.parse("01.01.2015", "dd.MM.yyyy"));
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    JSONObject jsonObj = (JSONObject) jsonTable.cellToJson(row, table.getColumn());
    Assert.assertEquals("01.01.2015", jsonObj.get("text"));
    Assert.assertEquals("2015-01-01", jsonObj.get("value")); //Pattern used by JsonDate
  }

  /**
   * Don't send value and text if they are equal. They may be equal if the date column uses the same pattern as
   * {@link JsonDate}.
   */
  @Test
  public void testDateColumn_jsonDatePattern() throws JSONException {
    TableWithDateColumn table = new TableWithDateColumn();
    table.init();
    ITableRow row = table.addRow(table.createRow());

    table.getColumn().setFormat(JsonDate.JSON_PATTERN_DATE_ONLY);
    table.getColumn().setValue(row, DateUtility.parse("01.01.2015", "dd.MM.yyyy"));
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    Object jsonObj = jsonTable.cellToJson(row, table.getColumn());
    assertTrue(jsonObj instanceof String);
  }

  @Test
  public void testBooleanColumn() throws JSONException {
    TableWithBooleanColumn table = new TableWithBooleanColumn();
    table.init();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, true);
    JsonTable<ITable> jsonTable = UiSessionTestUtility.newJsonAdapter(m_uiSession, table);

    JSONObject jsonObj = (JSONObject) jsonTable.cellToJson(row, table.getColumn());
    Assert.assertEquals(true, jsonObj.get("value"));

    table.getColumn().setValue(row, false);
    jsonObj = (JSONObject) jsonTable.cellToJson(row, table.getColumn());
    Assert.assertEquals(false, jsonObj.get("value"));
  }
}
