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
package org.eclipse.scout.rt.ui.html.jsonn;

import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
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
    Assert.assertEquals(event.get("id"), jsonTable.getId());

    //Complete menu must be sent
    event = JsonTestUtility.resolveJsonObject(event);
    Assert.assertEquals(1, event.getJSONArray("menus").length());
    JSONObject menuObj = event.getJSONArray("menus").getJSONObject(0);
    Assert.assertEquals(jsonMenu.getId(), menuObj.get("id"));
    Assert.assertEquals(menu.getText(), menuObj.get("text"));
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

    Assert.assertEquals(0, jsonSession.currentJsonResponse().getEventList().size());

    Menu menu = new Menu();
    menu.setText("first menu");
    table.setMenus(CollectionUtility.arrayList(menu));

    Assert.assertEquals(1, jsonSession.currentJsonResponse().getEventList().size());

    Menu menu2 = new Menu();
    menu2.setText("second text");
    table.setMenus(CollectionUtility.arrayList(menu, menu2));

    IJsonAdapter<?> jsonMenu = jsonSession.getJsonAdapter(menu);
    List<JSONObject> eventList = jsonSession.currentJsonResponse().getEventList();
    eventList = JsonTestUtility.resolveJsonAdapters(eventList);

    //There is still only one property change event containing the complete menus
    Assert.assertEquals(1, eventList.size());
    JSONObject event = eventList.get(0);
    Assert.assertEquals(event.get("id"), jsonTable.getId());

    Assert.assertEquals(2, event.getJSONArray("menus").length());
    JSONObject menuObj = event.getJSONArray("menus").getJSONObject(0);
    Assert.assertEquals(jsonMenu.getId(), menuObj.get("id"));
    Assert.assertEquals(menu.getText(), menuObj.get("text"));

    jsonMenu = jsonSession.getJsonAdapter(menu2);
    menuObj = event.getJSONArray("menus").getJSONObject(1);
    Assert.assertEquals(jsonMenu.getId(), menuObj.get("id"));
    Assert.assertEquals(menu2.getText(), menuObj.get("text"));
  }

  private static JsonTable createJsonTable(IJsonSession jsonSession) {
    Table table = new Table();
    table.setEnabled(true);

    JsonTable jsonTable = new JsonTable(table, jsonSession, jsonSession.createUniqueIdFor(null));
    jsonTable.attach();

    jsonTable.toJson();

    return jsonTable;
  }
}
