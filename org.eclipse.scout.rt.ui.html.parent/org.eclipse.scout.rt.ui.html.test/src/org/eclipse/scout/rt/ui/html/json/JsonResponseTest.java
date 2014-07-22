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
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class JsonResponseTest {

  @Test
  public void testJsonAdapterPropertyChange() throws JSONException {
    JsonSessionMock jsonSession = new JsonSessionMock();

    JsonTable jsonTable = createJsonTable(jsonSession);
    ITable table = jsonTable.getModel();

    Menu menu = new Menu();
    menu.setText("first menu");
    table.setMenus(CollectionUtility.arrayList(menu));

    IJsonAdapter<?> jsonMenu = jsonSession.getJsonAdapter(menu);
    List<JSONObject> eventList = jsonSession.currentJsonResponse().getEventList();

    //Property change for table
    JSONObject event = eventList.get(0);
    assertEquals(event.get("id"), jsonTable.getId());
    assertEquals(event.get("type"), "property");

    //Complete menu must be sent
    event = JsonTestUtility.resolveJsonObject(event);
    JSONObject props = event.getJSONObject("properties");

    assertEquals(1, props.getJSONArray("menus").length());
    JSONObject menuObj = props.getJSONArray("menus").getJSONObject(0);
    assertEquals(jsonMenu.getId(), menuObj.get("id"));
    assertEquals(menu.getText(), menuObj.get("text"));
  }

  /**
   * Executes ITable setMenus two times. Due to the coalescing only one property change event is sent. This property
   * change event must contain the complete menu objects, not only the id.
   */
  @Test
  public void testJsonAdapterPropertyChangeAgain() throws JSONException {
    JsonSessionMock jsonSession = new JsonSessionMock();

    JsonTable jsonTable = createJsonTable(jsonSession);
    ITable table = jsonTable.getModel();

    assertEquals(0, jsonSession.currentJsonResponse().getEventList().size());

    Menu menu = new Menu();
    menu.setText("first menu");
    table.setMenus(CollectionUtility.arrayList(menu));

    assertEquals(1, jsonSession.currentJsonResponse().getEventList().size());

    Menu menu2 = new Menu();
    menu2.setText("second text");
    table.setMenus(CollectionUtility.arrayList(menu, menu2));

    IJsonAdapter<?> jsonMenu = jsonSession.getJsonAdapter(menu);
    List<JSONObject> eventList = jsonSession.currentJsonResponse().getEventList();
    eventList = JsonTestUtility.resolveJsonAdapters(eventList);

    //There is still only one property change event containing the complete menus
    assertEquals(1, eventList.size());
    JSONObject event = eventList.get(0);
    assertEquals(event.get("id"), jsonTable.getId());
    assertEquals(event.get("type"), "property");
    JSONObject props = event.getJSONObject("properties");

    assertEquals(2, props.getJSONArray("menus").length());
    JSONObject menuObj = props.getJSONArray("menus").getJSONObject(0);
    assertEquals(jsonMenu.getId(), menuObj.get("id"));
    assertEquals(menu.getText(), menuObj.get("text"));

    jsonMenu = jsonSession.getJsonAdapter(menu2);
    menuObj = props.getJSONArray("menus").getJSONObject(1);
    assertEquals(jsonMenu.getId(), menuObj.get("id"));
    assertEquals(menu2.getText(), menuObj.get("text"));
  }

  private static JsonTable createJsonTable(IJsonSession jsonSession) {
    Table table = new Table();
    table.setEnabled(true);

    JsonTable jsonTable = new JsonTable(table, jsonSession, jsonSession.createUniqueIdFor(null));
    jsonTable.attach();

    jsonTable.toJson();

    return jsonTable;
  }

  @Test
  public void testJsonEventPropertyChangeEvent() throws JSONException {
    // Check empty response
    JsonSessionMock jsonSession = new JsonSessionMock();
    JSONObject json = jsonSession.currentJsonResponse().toJson();

    assertNotNull(json);
    JSONArray events = json.getJSONArray("events");
    assertEquals(0, events.length());

    // Check single property change event
    final String TEST_ID = "ID007";
    final String TEST_PROP_NAME = "a stränge prøpertÿ name";
    final String TEST_VALUE = "#";
    jsonSession.currentJsonResponse().addPropertyChangeEvent(TEST_ID, TEST_PROP_NAME, TEST_VALUE);
    json = jsonSession.currentJsonResponse().toJson();

    assertNotNull(json);
    events = json.getJSONArray("events");
    assertEquals(1, events.length());

    JSONObject event = events.getJSONObject(0);
    assertEquals(TEST_ID, event.get("id"));
    assertEquals("property", event.get("type"));
    JSONObject props = event.getJSONObject("properties");
    assertNotNull(props);
    assertEquals(1, props.length());
    assertEquals(TEST_PROP_NAME, props.keys().next());
    Object value = props.get(TEST_PROP_NAME);
    assertEquals(TEST_VALUE, value);
  }

  /**
   * Property with the value null get converted to "" (empty string)
   */
  @Test
  public void testJsonEventPropertyNullToEmptyString() throws JSONException {

    JsonSessionMock jsonSession = new JsonSessionMock();
    jsonSession.currentJsonResponse().addPropertyChangeEvent("-1", "name", null);
    JSONObject json = jsonSession.currentJsonResponse().toJson();

    JSONArray events = json.getJSONArray("events");
    JSONObject props = events.getJSONObject(0).getJSONObject("properties");
    assertEquals(props.get("name"), "");
  }
}
